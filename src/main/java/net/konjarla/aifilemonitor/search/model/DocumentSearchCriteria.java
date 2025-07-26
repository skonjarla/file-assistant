package net.konjarla.aifilemonitor.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchCriteria {
    private String filename;
    private List<String> extensions;
    private List<String> classifications;
    private Long minSize;
    private Long maxSize;
    private Instant modifiedAfter;
    private Instant modifiedBefore;
    private Boolean isHidden;
    private Boolean isReadonly;
    private String mimeType;
    
    @Builder.Default
    private boolean matchAll = true; // true for AND, false for OR
}
