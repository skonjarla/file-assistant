package net.konjarla.aifilemonitor.database;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@Setter
@Getter
public class FileChunkDTO {
    private String id;
    private String content;
    private Map<String, Object> metadata;
    private float[] embedding;
    private Integer chunkIndex;
    private String filePath;
    private String fileName;
    private String fileExtension;
    private LocalDateTime lastIndexed;
    private Instant created;
    private Instant updated;
}