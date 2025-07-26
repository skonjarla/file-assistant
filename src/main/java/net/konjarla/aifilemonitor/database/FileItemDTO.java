package net.konjarla.aifilemonitor.database;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class FileItemDTO {
    private String id;
    private String filePath;
    private String fileName;
    private String fileExtension;
    private Long fileSize;
    private String mimeType;
    private LocalDateTime lastModified;
    private LocalDateTime creationTime;
    private String checksum;
    private Boolean isHidden;
    private Boolean isReadOnly;
    private String owner;
    private String groupName;
    private String permissions;
    private LocalDateTime lastIndexed;
    private Boolean isSensitive;
    private Map<String, Object> sensitiveReason;
    private Instant created;
    private Instant updated;
    private List<FileChunkDTO> chunks;
}