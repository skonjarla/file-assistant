package net.konjarla.aifilemonitor.tools;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import jakarta.json.Json;
import jakarta.json.stream.JsonParser;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.konjarla.aifilemonitor.search.VectorSearchService;
import net.konjarla.aifilemonitor.search.model.DocumentMetadata;
import net.konjarla.aifilemonitor.search.model.DocumentSearchCriteria;
import net.konjarla.aifilemonitor.search.service.DocumentMetadataService;
import net.konjarla.aifilemonitor.tools.model.FileSearch;
import net.konjarla.aifilemonitor.tools.model.FileSearchRequest;
import net.konjarla.aifilemonitor.tools.model.FileSearchResult;
import net.konjarla.aifilemonitor.util.FileUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileSearchTool {
    @NonNull
    VectorSearchService vectorSearchService;
    @NonNull
    DocumentMetadataService documentMetadataService;
    @NonNull
    ElasticsearchClient elasticsearchClient;

    public List<FileSearchResult> fileSearch(FileSearchRequest fileSearchRequest) {
        log.debug("fileSearchRequest: {}", fileSearchRequest);
        // return getFileSearchResultsFromVectorDB(fileSearchRequest);
        return getFileSearchResultsFromElastic(fileSearchRequest);
    }

    @Tool(description = "Use this tool to search for files based on user queries that can be translated into Elasticsearch queries. It accepts two inputs: 'elasticSearchQuery' (a JSON query object based on the file metadata schema) and 'prompt' (the original user query)")
    public List<FileSearchResult> fileSearchWithElasticsearchQuery(FileSearch fileSearch) {
        String userPrompt = fileSearch.getPrompt();
        // Perform Vector DB search
        FileSearchRequest fileSearchRequest = FileSearchRequest.builder()
                .query(userPrompt)
                .topK(5)
                .build();

        List<FileSearchResult> vectorDbResults = getResultsForQueryFromVectorDB(fileSearchRequest);

        String elasticSearchQuery = fileSearch.getElasticsearchQuery();
        log.debug("userPrompt: {}", userPrompt);
        log.debug("elasticSearchQuery: {}", elasticSearchQuery);
        //String elasticSearchQuery = fileSearch;
        JsonParser jsonParser = null;
        try {
            jsonParser = Json.createParser(new StringReader(elasticSearchQuery));
        } catch (Exception e) {
            log.error("Error parsing elasticSearchQuery as JSON: {}", e.getMessage());
            return vectorDbResults;
        }
        SearchRequest searchRequest = SearchRequest._DESERIALIZER.deserialize(jsonParser, elasticsearchClient._jsonpMapper());

        assert searchRequest.query() != null;

        Pageable pageable = Pageable.ofSize(5);
        Page<DocumentMetadata> results;
        log.debug("Searching with elastic: {}", searchRequest.q());
        try {
            results = documentMetadataService.searchDocuments(searchRequest, pageable);
            log.debug("Found {} Elastic results", results.getTotalElements());
            results.forEach(doc -> {
                log.debug("Found doc: {}", doc);
            });
        } catch (Exception e) {
            log.error("Error searching documents: {}", e.getMessage());
            return vectorDbResults;
        }
        //List<DocumentMetadata> results = documentMetadataService.searchDocuments(criteria, pageable).getContent();
        List<FileSearchResult> elasticDbResults = new ArrayList<>(results.stream().map(doc -> {
            String fileId = doc.getFileId();
            FilterExpressionBuilder b = new FilterExpressionBuilder();
            Filter.Expression expression = b.eq("doc_id", fileId).build();
            //List<Document> result = vectorSearchService.searchSimilarWithFilter(fileSearchRequest.getQuery(),
            //        fileSearchRequest.getTopK(), expression);

            List<Document> result = vectorSearchService.searchWithFilter(fileSearchRequest.getTopK(), expression);

            if (!result.isEmpty()) {
                String excerpt = result.get(0).getText();
                Double score = result.get(0).getScore();
                return FileSearchResult.builder()
                        .filename(doc.getFilename())
                        .path(doc.getFilename())
                        .lastModified(doc.getLastModified().toString())
                        .fileType(doc.getExtension())
                        .excerpt(excerpt)
                        .score(score)
                        .build();
            } else {
                return FileSearchResult.builder().build();
            }
        }).toList());
        log.debug("Found {} elastic results", elasticDbResults.size());
        elasticDbResults.forEach(elasticDbResult -> {
            log.debug("elasticDbResult: {}", elasticDbResult);
        });
        elasticDbResults.addAll(vectorDbResults);
        Comparator<FileSearchResult> fileSearchResultComparator = (e1, e2) -> CharSequence.compare(e1.getFilename(), e2.getFilename());
        //return elasticDbResults;
        return elasticDbResults.stream()
                .filter(fileSearchResult -> fileSearchResult.getFilename() != null)
                .filter(distinctByKey(FileSearchResult::getFilename))
                .toList();

    }

    private List<FileSearchResult> getFileSearchResultsFromElastic(FileSearchRequest fileSearchRequest) {
        log.debug("fileSearchRequest with elastic: {}", fileSearchRequest);
        DocumentSearchCriteria criteria = DocumentSearchCriteria.builder()
                .matchAll(false)
                .build();
        if (fileSearchRequest.getFilters() != null) {
            if (fileSearchRequest.getFilters().getModifiedAfter() != null) {
                if (!fileSearchRequest.getFilters().getModifiedAfter().trim().isEmpty()) {
                    try {
                        Instant modifiedAfter = FileUtils.convertDateToInstant(fileSearchRequest.getFilters().getModifiedAfter());
                        criteria.setModifiedAfter(modifiedAfter);
                    } catch (Exception DateFormatException) {
                        log.error("Error parsing date: {}", fileSearchRequest.getFilters().getModifiedAfter());
                    }
                }
            }
            if (fileSearchRequest.getFilters().getExtension() != null) {
                criteria.setExtensions(fileSearchRequest.getFilters().getExtension()); //fileSearchRequest.getFilters().getExtension());
            }
            if (fileSearchRequest.getFilters().getPathContains() != null) {
                criteria.setFilename(fileSearchRequest.getFilters().getPathContains());
            }
        }
        List<FileSearchResult> vectorDbResults = getResultsForQueryFromVectorDB(fileSearchRequest);
        Pageable pageable = Pageable.ofSize(5);
        Page<DocumentMetadata> results;
        log.debug("Searching with elastic: {}", criteria);
        try {
            results = documentMetadataService.searchDocuments(criteria, pageable);
        } catch (Exception e) {
            log.error("Error searching documents: {}", e.getMessage());
            return vectorDbResults;
        }
        //List<DocumentMetadata> results = documentMetadataService.searchDocuments(criteria, pageable).getContent();
        List<FileSearchResult> elasticDbResults = new ArrayList<>(results.stream().map(doc -> {
            String fileId = doc.getFileId();
            FilterExpressionBuilder b = new FilterExpressionBuilder();
            Filter.Expression expression = b.eq("doc_id", fileId).build();
            List<Document> result = vectorSearchService.searchSimilarWithFilter(fileSearchRequest.getQuery(),
                    fileSearchRequest.getTopK(), expression);
            if (!result.isEmpty()) {
                String excerpt = result.get(0).getText();
                Double score = result.get(0).getScore();
                return FileSearchResult.builder()
                        .filename(doc.getFilename())
                        .path(doc.getFilename())
                        .lastModified(doc.getLastModified().toString())
                        .fileType(doc.getExtension())
                        .excerpt(excerpt)
                        .score(score)
                        .build();
            } else {
                return FileSearchResult.builder().build();
            }
        }).toList());
        log.debug("Found {} elastic results", elasticDbResults.size());
        elasticDbResults.forEach(elasticDbResult -> {
            log.debug("elasticDbResult: {}", elasticDbResult);
        });
        elasticDbResults.addAll(vectorDbResults);
        Comparator<FileSearchResult> fileSearchResultComparator = (e1, e2) -> CharSequence.compare(e1.getFilename(), e2.getFilename());
        //return elasticDbResults;
        return elasticDbResults.stream()
                .filter(fileSearchResult -> fileSearchResult.getFilename() != null)
                .filter(distinctByKey(FileSearchResult::getFilename))
                .toList();

        //return new ArrayList<>(new LinkedHashSet<>(elasticDbResults.stream().sorted(fileSearchResultComparator).toList()));
    }

    private List<FileSearchResult> getResultsForQueryFromVectorDB(FileSearchRequest fileSearchRequest) {
        List<Document> result = vectorSearchService.searchSimilarWithFilter(fileSearchRequest.getQuery(),
                fileSearchRequest.getTopK(), null);
        log.debug("Found {} results", result.size());
        return getFileSearchResults(result);
    }

    private List<FileSearchResult> getFileSearchResults(List<Document> results) {
        return results.stream().map(doc -> {
            return FileSearchResult.builder()
                    .filename(doc.getMetadata().get("filename").toString())
                    .path(doc.getMetadata().get("filename").toString())
                    .lastModified(doc.getMetadata().get("last_modified").toString())
                    .fileType(doc.getMetadata().get("extension").toString())
                    .excerpt(doc.getText())
                    .score(doc.getScore())
                    .build();
        }).toList();
    }

    public static <T> Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {

        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}