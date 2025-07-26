package net.konjarla.aifilemonitor.database.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.konjarla.aifilemonitor.database.FileChunkDTO;
import net.konjarla.aifilemonitor.database.FileItem;
import net.konjarla.aifilemonitor.database.FileItemDTO;
import net.konjarla.aifilemonitor.database.FileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileItemService {
    @NonNull
    private final FileRepository fileRepository;

    @Transactional
    public FileItem addFileItem(FileItem fileItem) {
        return fileRepository.saveAndFlush(fileItem);
    }

    public FileItem findByFilePath(String absolutePath) {
        return fileRepository.findByFilePath(absolutePath);
    }

    public Optional<FileItem> findById(String docId) {
        try {
            return fileRepository.findById(docId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    public Optional<FileItem> findByIdWithChunks(String docId) {
        try {
            return fileRepository.findByIdWithChunks(docId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    public Optional<FileItemDTO> findByIdWithChunksDTO(String docId) {
        try {
            FileItem fileItem = fileRepository.findByIdWithChunks(docId).orElse(null);
            if (fileItem == null) {
                return Optional.empty();
            }
            FileItemDTO dto = toDTO(fileItem);
            return Optional.of(dto);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Transactional
    public void deleteById(String docId) {
        fileRepository.deleteById(docId);
        fileRepository.flush();
    }

    private FileItemDTO toDTO(FileItem fileItem) {
        FileItemDTO dto = new FileItemDTO();
        dto.setId(fileItem.getId());
        dto.setFilePath(fileItem.getFilePath());
        dto.setFileName(fileItem.getFileName());
        dto.setFileExtension(fileItem.getFileExtension());
        dto.setFileSize(fileItem.getFileSize());
        dto.setMimeType(fileItem.getMimeType());
        dto.setLastModified(fileItem.getLastModified());
        dto.setCreationTime(fileItem.getCreationTime());
        dto.setChecksum(fileItem.getChecksum());
        dto.setIsHidden(fileItem.getIsHidden());
        dto.setIsReadOnly(fileItem.getIsReadOnly());
        dto.setOwner(fileItem.getOwner());
        dto.setGroupName(fileItem.getGroupName());
        dto.setPermissions(fileItem.getPermissions());
        dto.setLastIndexed(fileItem.getLastIndexed());
        dto.setIsSensitive(fileItem.getIsSensitive());
        dto.setSensitiveReason(fileItem.getSensitiveReason());
        dto.setCreated(fileItem.getCreated());
        dto.setUpdated(fileItem.getUpdated());
        if (fileItem.getChunks() != null) {
            List<FileChunkDTO> chunkDTOs = fileItem.getChunks().stream().map(chunk -> {
                FileChunkDTO chunkDTO = new FileChunkDTO();
                chunkDTO.setId(chunk.getId());
                chunkDTO.setContent(chunk.getContent());
                chunkDTO.setMetadata(chunk.getMetadata());
                chunkDTO.setEmbedding(chunk.getEmbedding());
                chunkDTO.setChunkIndex(chunk.getChunkIndex());
                chunkDTO.setFilePath(chunk.getFilePath());
                chunkDTO.setFileName(chunk.getFileName());
                chunkDTO.setFileExtension(chunk.getFileExtension());
                chunkDTO.setLastIndexed(chunk.getLastIndexed());
                chunkDTO.setCreated(chunk.getCreated());
                chunkDTO.setUpdated(chunk.getUpdated());
                return chunkDTO;
            }).toList();
            dto.setChunks(chunkDTOs);
        }
        return dto;
    }
}
