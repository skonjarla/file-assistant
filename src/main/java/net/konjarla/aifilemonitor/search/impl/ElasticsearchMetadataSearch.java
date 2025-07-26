package net.konjarla.aifilemonitor.search.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.konjarla.aifilemonitor.search.MetadataSearch;
import net.konjarla.aifilemonitor.search.model.DocumentMetadata;
import net.konjarla.aifilemonitor.search.model.DocumentSearchCriteria;
import net.konjarla.aifilemonitor.search.service.DocumentMetadataService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ElasticsearchMetadataSearch implements MetadataSearch {

    @NonNull
    private final DocumentMetadataService documentMetadataService;

    @Override
    public DocumentMetadata indexDocument(DocumentMetadata metadata) {
        return documentMetadataService.save(metadata);
    }

    @Override
    public Optional<DocumentMetadata> getDocument(String fileId) {
        return documentMetadataService.findById(fileId);
    }

    @Override
    public List<DocumentMetadata> searchByFilename(String filename) {
        return documentMetadataService.findByFilenameContaining(filename);
    }

    @Override
    public List<DocumentMetadata> searchByExtension(String extension) {
        return documentMetadataService.findByExtension(extension);
    }

    @Override
    public List<DocumentMetadata> searchByClassification(String classification) {
        return documentMetadataService.findByClassification(classification);
    }

    @Override
    public Page<DocumentMetadata> searchDocuments(DocumentSearchCriteria criteria, Pageable pageable) {
        return documentMetadataService.searchDocuments(criteria, pageable);
    }

    @Override
    public void deleteDocument(String fileId) {
        documentMetadataService.delete(fileId);
    }
}
