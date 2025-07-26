package net.konjarla.aifilemonitor.database.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.konjarla.aifilemonitor.database.FileChunk;
import net.konjarla.aifilemonitor.database.FileChunkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileChunkService {
    @NonNull
    private final FileChunkRepository fileChunkRepository;

    @Transactional
    public void addFileChunk(FileChunk fileChunk) {
       fileChunkRepository.save(fileChunk);
       fileChunkRepository.flush();
    }

    @Transactional
    public void updateEmbedding(String id, float[] embedding) {
        fileChunkRepository.updateEmbedding(id, embedding);
        fileChunkRepository.flush();
    }
}
