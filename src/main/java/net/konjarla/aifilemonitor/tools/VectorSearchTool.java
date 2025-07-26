package net.konjarla.aifilemonitor.tools;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.konjarla.aifilemonitor.search.VectorSearchService;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VectorSearchTool {
    @NonNull
    private final VectorSearchService vectorSearchService;

    @Tool(description = "Retrieve similar vector database results based on a input text.")
    public List<Document> vectorSearch(String text, Integer topK) {
        if(topK == null || topK < 1) {
            topK = 5;
        }
        return vectorSearchService.searchSimilar(text, topK);
    }
}