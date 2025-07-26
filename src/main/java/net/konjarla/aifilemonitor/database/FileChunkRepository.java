package net.konjarla.aifilemonitor.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileChunkRepository extends JpaRepository<FileChunk, String> {
    List<FileChunk> findByFileItem(FileItem fileItem);

    @Modifying
    @Query("update FileChunk u SET u.chunkIndex = :chunkIndex, u.fileItem = :fileItem where u.id = :id")
    void updateChunk(@Param(value = "id") String id,
                     @Param(value = "chunkIndex") Integer chunkIndex,
                     @Param(value = "fileItem") FileItem fileItem);

    @Modifying
    @Query(value = "update file_chunks SET embedding = :embedding where id = :id", nativeQuery = true)
    void updateEmbedding(@Param(value = "id") String id,
                     @Param(value = "embedding") float[] embedding);
} 