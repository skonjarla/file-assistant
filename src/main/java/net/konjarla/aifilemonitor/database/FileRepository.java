package net.konjarla.aifilemonitor.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileItem, String> {
    FileItem findByFilePath(String filePath);

    @Query("SELECT f FROM FileItem f LEFT JOIN FETCH f.chunks WHERE f.id = :id")
    Optional<FileItem> findByIdWithChunks(@Param("id") String id);
}