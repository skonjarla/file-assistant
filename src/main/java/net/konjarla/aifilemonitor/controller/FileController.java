package net.konjarla.aifilemonitor.controller;

import lombok.extern.slf4j.Slf4j;
import net.konjarla.aifilemonitor.database.FileItem;
import net.konjarla.aifilemonitor.database.FileItemDTO;
import net.konjarla.aifilemonitor.database.service.FileItemService;
import net.konjarla.aifilemonitor.fileprocessor.FileProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/files")
@Slf4j
public class FileController {
    @Autowired
    private FileProcessor fileProcessingService;
    @Autowired
    private FileItemService fileItemService;

    @PostMapping("/remove")
    public ResponseEntity<String> removeFile(@RequestParam("id") String fileId) throws IOException {
        try {
            fileProcessingService.removeFile(fileId);
            return ResponseEntity.ok("Document with Id: " + fileId + " deleted.");
        } catch (InterruptedException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/get")
    public ResponseEntity<FileItemDTO> getFile(@RequestParam("id") String fileId) throws IOException {
        try {
            Optional<FileItem> fileItemOpt = fileItemService.findByIdWithChunks(fileId);
            if (fileItemOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            //FileItemDTO dto = toDTO(fileItemOpt.get());
            FileItemDTO dto = fileItemService.findByIdWithChunksDTO(fileId).orElse(null);
            if (dto == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }
}