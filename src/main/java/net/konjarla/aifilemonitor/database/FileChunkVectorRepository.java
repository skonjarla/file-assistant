package net.konjarla.aifilemonitor.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FileChunkVectorRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public List<FileChunk> findSimilarChunks(List<Double> embedding, int topK) {
        // Convert embedding to Postgres array string
        String embeddingStr = embedding.toString().replace("[", "{").replace("]", "}");

        String sql = "SELECT * FROM file_chunks " +
                     "ORDER BY embedding <-> CAST(:embedding AS vector) " +
                     "LIMIT :topK";

        Query query = entityManager.createNativeQuery(sql, FileChunk.class);
        query.setParameter("embedding", embeddingStr);
        query.setParameter("topK", topK);

        return query.getResultList();
    }
} 