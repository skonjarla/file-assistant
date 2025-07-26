package net.konjarla.aifilemonitor.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "files", createIndex = true)
@Setting(settingPath = "/elasticsearch_setting.json")
public class DocumentMetadata {
    @Id
    private String fileId;

    //@Field(type = FieldType.Keyword)
    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String filename;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String filenameKeyword;
    
    @Field(type = FieldType.Long)
    private Long fileSize;
    
    @Field(type = FieldType.Keyword)
    private String extension;
    
    @Field(type = FieldType.Date)
    private Instant lastModified;
    
    @Field(type = FieldType.Date)
    private Instant creationTime;
    
    @Field(type = FieldType.Boolean)
    private Boolean isHidden;
    
    @Field(type = FieldType.Boolean)
    private Boolean isReadonly;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String mimeType;
    
    //@Field(type = FieldType.Keyword)
    @Field(type = FieldType.Text, analyzer = "lowercase_analyzer", searchAnalyzer = "lowercase_analyzer")
    private List<String> classification;
}
