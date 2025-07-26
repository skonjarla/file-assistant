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
import java.util.Map;

@Entity
@Table(name = "file_chunks")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Slf4j
public class FileChunk {
    @Id
    private String id;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    @Column(name = "metadata", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;

    @Transient
    @Column(name = "embedding", columnDefinition = "vector(1024)")
    private float[] embedding;

    private Integer chunkIndex;
    private String filePath;
    private String fileName;
    private String fileExtension;

    private LocalDateTime lastIndexed;
    @CreationTimestamp
    @Column(name="created_at",updatable = false)
    private Instant created;
    @UpdateTimestamp
    @Column(name="modified_at")
    private Instant updated;

    @ManyToOne(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "file_id")
    private FileItem fileItem;
} 