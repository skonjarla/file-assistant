package net.konjarla.aifilemonitor.search.service;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import net.konjarla.aifilemonitor.search.model.DocumentMetadata;
import net.konjarla.aifilemonitor.search.model.DocumentSearchCriteria;
import net.konjarla.aifilemonitor.search.repository.DocumentMetadataRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentMetadataService {

    private final DocumentMetadataRepository repository;

    public DocumentMetadata save(DocumentMetadata metadata) {
        return repository.save(metadata);
    }

    public Optional<DocumentMetadata> findById(String fileId) {
        return repository.findById(fileId);
    }

    public List<DocumentMetadata> findAll() {
        return (List<DocumentMetadata>) repository.findAll();
    }

    public Page<DocumentMetadata> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public void delete(String fileId) {
        repository.deleteById(fileId);
    }

    public List<DocumentMetadata> findByFilenameContaining(String filename) {
        return repository.findByFilenameContaining(filename);
    }

    public List<DocumentMetadata> findByExtension(String extension) {
        return repository.findByExtension(extension);
    }

    public List<DocumentMetadata> findByClassification(String classification) {
        return repository.findByClassification(classification);
    }
    
    public boolean exists(String fileId) {
        return repository.existsById(fileId);
    }
    
    public long count() {
        return repository.count();
    }
    
    /**
     * Advanced search with multiple criteria
     * @param criteria Search criteria
     * @param pageable Pagination information
     * @return Page of matching documents
     */
    public Page<DocumentMetadata> searchDocuments(DocumentSearchCriteria criteria, Pageable pageable) {
        return repository.searchDocuments(criteria, pageable);
    }

    public Page<DocumentMetadata> searchDocuments(String dslQuery, Pageable pageable) {
        return repository.searchDocuments(dslQuery, pageable);
    }

    public Page<DocumentMetadata> searchDocuments(SearchRequest searchRequest, Pageable pageable) {
        return repository.searchDocuments(searchRequest, pageable);
    }

}
