package net.konjarla.aifilemonitor.search.repository;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import net.konjarla.aifilemonitor.search.model.DocumentMetadata;
import net.konjarla.aifilemonitor.search.model.DocumentSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomDocumentMetadataRepository {
    Page<DocumentMetadata> searchDocuments(DocumentSearchCriteria criteria, Pageable pageable);
    Page<DocumentMetadata> searchDocuments(String dslQuery, Pageable pageable);

    Page<DocumentMetadata> searchDocuments(SearchRequest searchRequest, Pageable pageable);
}
