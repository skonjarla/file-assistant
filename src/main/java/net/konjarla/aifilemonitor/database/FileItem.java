package net.konjarla.aifilemonitor.database;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "files")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Slf4j
public class FileItem {
    @Id
    //@GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String filePath;

    @Column(name = "file_name", nullable = false)
    private String fileName;
    private String fileExtension;

    @Column(name = "file_size")
    private Long fileSize; // in bytes

    @Column(name = "mime_type", length = 255)
    private String mimeType;

    @Column(name = "file_last_modified")
    private LocalDateTime lastModified;

    @Column(name = "file_creation_time")
    private LocalDateTime creationTime;

    @Column(name = "checksum", length = 64)
    private String checksum; // SHA-256 hash of file content

    @Column(name = "is_hidden")
    private Boolean isHidden;

    @Column(name = "is_readonly")
    private Boolean isReadOnly;

    @Column(name = "owner", length = 100)
    private String owner;

    @Column(name = "group_name", length = 100)
    private String groupName;

    @Column(name = "permissions", length = 10)
    private String permissions; // e.g., "rwxr-xr-x"

    private LocalDateTime lastIndexed;
    @Column(name = "is_sensitive", columnDefinition = "boolean default false")
    private Boolean isSensitive;
    @Column(name = "sensitive_reason", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> sensitiveReason;
    @CreationTimestamp
    @Column(name="entry_created_at",updatable = false)
    private Instant created;
    @UpdateTimestamp
    @Column(name="entry_modified_at")
    private Instant updated;

    @OneToMany(mappedBy = "fileItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<FileChunk> chunks;
} 