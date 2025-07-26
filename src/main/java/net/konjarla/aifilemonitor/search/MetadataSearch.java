package net.konjarla.aifilemonitor.search;

import net.konjarla.aifilemonitor.search.model.DocumentMetadata;
import net.konjarla.aifilemonitor.search.model.DocumentSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MetadataSearch {
    DocumentMetadata indexDocument(DocumentMetadata metadata);
    Optional<DocumentMetadata> getDocument(String fileId);
    List<DocumentMetadata> searchByFilename(String filename);
    List<DocumentMetadata> searchByExtension(String extension);
    List<DocumentMetadata> searchByClassification(String classification);
    Page<DocumentMetadata> searchDocuments(DocumentSearchCriteria criteria, Pageable pageable);
    void deleteDocument(String fileId);
}
