package net.konjarla.aifilemonitor.search;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VectorSearchService {
    @NonNull
    private VectorStore fileVectorStore;

    @Value("${vector.store.similarity.threshold}")
    private Double similarityThreshold;


    public List<Document> searchSimilar(String text, int topK) {
        return fileVectorStore.similaritySearch(SearchRequest.builder()
                .query(text)
                .similarityThreshold(similarityThreshold)
                .topK(topK).build());
    }

    public List<Document> searchSimilarWithFilter(String text, int topK, Filter.Expression expression) {
        return fileVectorStore.similaritySearch(SearchRequest.builder()
                .query(text)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .filterExpression(expression)
                .build());
    }

    public List<Document> searchWithFilter(int topK, Filter.Expression expression) {
        return fileVectorStore.similaritySearch(SearchRequest.builder()
                .filterExpression(expression)
                .topK(topK)
                .similarityThreshold(0.0f)
                .build());
    }
} 