package net.konjarla.aifilemonitor.search.repository;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.json.JsonData;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.konjarla.aifilemonitor.search.model.DocumentMetadata;
import net.konjarla.aifilemonitor.search.model.DocumentSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CustomDocumentMetadataRepositoryImpl implements CustomDocumentMetadataRepository {

    @NonNull
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public Page<DocumentMetadata> searchDocuments(DocumentSearchCriteria criteria, Pageable pageable) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        
        // Start with a match all query if needed
        if (criteria.isMatchAll()) {
            boolQueryBuilder.must(QueryBuilders.matchAll().build()._toQuery());
        } else {
            boolQueryBuilder.should(QueryBuilders.matchAll().build()._toQuery());
        }

        // Filename search (partial match)
        if (StringUtils.hasText(criteria.getFilename())) {
            addCondition(boolQueryBuilder, 
                QueryBuilders.wildcard()
                    .field("filename")
                    .wildcard("*" + criteria.getFilename().toLowerCase() + "*")
                    .build()
                    ._toQuery(),
                criteria.isMatchAll());
        }

        // Extension filter (exact match)
        if (criteria.getExtensions() != null && !criteria.getExtensions().isEmpty()) {
            addCondition(boolQueryBuilder, 
                QueryBuilders.terms()
                    .field("extension")
                    .terms(t -> t.value(criteria.getExtensions().stream()
                        .map(FieldValue::of)
                        .collect(java.util.stream.Collectors.toList())))
                    .build()
                    ._toQuery(),
                criteria.isMatchAll());
        }

        // Classification filter with wildcard support
        if (criteria.getClassifications() != null && !criteria.getClassifications().isEmpty()) {
            BoolQuery.Builder classificationBoolQuery = new BoolQuery.Builder();
            criteria.getClassifications().forEach(classification -> {
                classificationBoolQuery.should(QueryBuilders.wildcard()
                    .field("classification")
                    .wildcard("*" + classification.toLowerCase() + "*")
                    .build()
                    ._toQuery());
            });
            addCondition(boolQueryBuilder, 
                classificationBoolQuery.build()._toQuery(),
                criteria.isMatchAll());
        }

        // File size range query
        if (criteria.getMinSize() != null || criteria.getMaxSize() != null) {

            UntypedRangeQuery.Builder rangeQueryBuilder = RangeQueryBuilders.untyped().field("fileSize");
            
            if (criteria.getMinSize() != null) {
                rangeQueryBuilder.gte(JsonData.of(criteria.getMinSize())); //rangeQueryBuilder.gte(FieldValue.of(criteria.getMinSize()));
            }
            if (criteria.getMaxSize() != null) {
                rangeQueryBuilder.lte(JsonData.of(criteria.getMaxSize())); //rangeQueryBuilder.lte(FieldValue.of(criteria.getMaxSize()));
            }
            
            addCondition(boolQueryBuilder, rangeQueryBuilder.build()._toRangeQuery()._toQuery(), criteria.isMatchAll());
        }

        // Last modified date range query
        if (criteria.getModifiedAfter() != null || criteria.getModifiedBefore() != null) {
            // RangeQuery.Builder dateRangeQueryBuilder = QueryBuilders.range().field("lastModified");
            UntypedRangeQuery.Builder rangeQueryBuilder = RangeQueryBuilders.untyped().field("fileSize");
            
            if (criteria.getModifiedAfter() != null) {
                rangeQueryBuilder.gte(JsonData.of(criteria.getModifiedAfter().toString()));
            }
            if (criteria.getModifiedBefore() != null) {
                rangeQueryBuilder.lte(JsonData.of(criteria.getModifiedBefore().toString()));
            }
            
            addCondition(boolQueryBuilder, rangeQueryBuilder.build()._toRangeQuery()._toQuery(), criteria.isMatchAll());
        }

        // Boolean flags
        if (criteria.getIsHidden() != null) {
            addCondition(boolQueryBuilder, 
                QueryBuilders.term()
                    .field("isHidden")
                    .value(criteria.getIsHidden())
                    .build()
                    ._toQuery(),
                criteria.isMatchAll());
        }
        if (criteria.getIsReadonly() != null) {
            addCondition(boolQueryBuilder, 
                QueryBuilders.term()
                    .field("isReadonly")
                    .value(criteria.getIsReadonly())
                    .build()
                    ._toQuery(),
                criteria.isMatchAll());
        }

        // MIME type filter
        if (StringUtils.hasText(criteria.getMimeType())) {
            addCondition(boolQueryBuilder, 
                QueryBuilders.term()
                    .field("mimeType")
                    .value(criteria.getMimeType())
                    .build()
                    ._toQuery(),
                criteria.isMatchAll());
        }

        // Build the native query
        NativeQuery searchQuery = new NativeQueryBuilder()
            .withQuery(boolQueryBuilder.build()._toQuery())
            .withPageable(pageable)
            .build();

        // Execute the search
        SearchHits<DocumentMetadata> searchHits = elasticsearchOperations.search(searchQuery, DocumentMetadata.class);
        List<DocumentMetadata> results = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return new PageImpl<>(results, pageable, searchHits.getTotalHits());
    }

    @Override
    public Page<DocumentMetadata> searchDocuments(String dslQuery, Pageable pageable) {
        StringQuery stringQuery = new StringQuery(dslQuery);
        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(stringQuery)
                .build();
        assert searchQuery.getSpringDataQuery() != null;
        SearchHits<DocumentMetadata> searchHits = elasticsearchOperations.search(searchQuery, DocumentMetadata.class);
        List<DocumentMetadata> results = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();
        return new PageImpl<>(results, pageable, searchHits.getTotalHits());
    }

    @Override
    public Page<DocumentMetadata> searchDocuments(SearchRequest searchRequest, Pageable pageable) {
        assert searchRequest.query() != null;
        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(searchRequest.query())
                .build();
        SearchHits<DocumentMetadata> searchHits = elasticsearchOperations.search(searchQuery, DocumentMetadata.class);
        List<DocumentMetadata> results = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();
        return new PageImpl<>(results, pageable, searchHits.getTotalHits());
    }

    private void addCondition(BoolQuery.Builder boolQuery, co.elastic.clients.elasticsearch._types.query_dsl.Query query, boolean isMatchAll) {
        if (isMatchAll) {
            boolQuery.must(query);
        } else {
            boolQuery.should(query);
        }
    }
}
