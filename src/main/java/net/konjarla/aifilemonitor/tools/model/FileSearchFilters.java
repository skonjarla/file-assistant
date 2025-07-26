package net.konjarla.aifilemonitor.tools.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.springframework.lang.Nullable;

import java.util.List;

@Getter
@Builder
@ToString
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FileSearchFilters {
    @Nullable
    private List<String> extension;
    @Nullable
    private String modifiedAfter;
    @Nullable
    private String createdAfter;
    @Nullable
    private String modifiedAfterYear;
    @Nullable
    private String modifiedAfterMonth;
    @Nullable
    private String modifiedAfterDay;
    @Nullable
    private String pathContains;
    @Nullable
    private List<String> classifications;
}
