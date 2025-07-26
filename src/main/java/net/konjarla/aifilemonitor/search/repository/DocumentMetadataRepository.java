package net.konjarla.aifilemonitor.search.repository;

import net.konjarla.aifilemonitor.search.model.DocumentMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentMetadataRepository extends ElasticsearchRepository<DocumentMetadata, String>, 
                                                 PagingAndSortingRepository<DocumentMetadata, String>,
                                                 CustomDocumentMetadataRepository {
    
    List<DocumentMetadata> findByFilenameContaining(String filename);
    Page<DocumentMetadata> findByFilenameContaining(String filename, Pageable pageable);
    
    List<DocumentMetadata> findByExtension(String extension);
    Page<DocumentMetadata> findByExtension(String extension, Pageable pageable);
    
    /**
     * Find documents that have all the specified classifications
     * @param classifications List of classifications to search for
     * @return List of matching documents
     */
    List<DocumentMetadata> findByClassificationIn(List<String> classifications);
    
    /**
     * Find documents that have all the specified classifications with pagination
     * @param classifications List of classifications to search for
     * @param pageable Pagination information
     * @return Page of matching documents
     */
    Page<DocumentMetadata> findByClassificationIn(List<String> classifications, Pageable pageable);
    
    /**
     * Find documents that have the specified classification
     * @param classification The classification to search for
     * @return List of matching documents
     */
    List<DocumentMetadata> findByClassification(String classification);
    
    /**
     * Find documents that have the specified classification with pagination
     * @param classification The classification to search for
     * @param pageable Pagination information
     * @return Page of matching documents
     */
    Page<DocumentMetadata> findByClassification(String classification, Pageable pageable);
}
