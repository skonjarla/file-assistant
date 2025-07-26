package net.konjarla.aifilemonitor.fileprocessor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.konjarla.aifilemonitor.database.FileChunk;
import net.konjarla.aifilemonitor.database.FileItem;
import net.konjarla.aifilemonitor.database.service.FileChunkService;
import net.konjarla.aifilemonitor.database.service.FileItemService;
import net.konjarla.aifilemonitor.llm.LlmService;
import net.konjarla.aifilemonitor.search.MetadataSearch;
import net.konjarla.aifilemonitor.search.model.DocumentMetadata;
import net.konjarla.aifilemonitor.tools.model.FileClassification;
import net.konjarla.aifilemonitor.util.FileUtils;
import net.konjarla.aifilemonitor.util.TextExtractor;
import org.apache.tika.exception.TikaException;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileProcessor {
    @NonNull
    private MetadataSearch metadataSearch;
    @NonNull
    EmbeddingModel embeddingModel;
    @NonNull
    FileChunkService fileChunkService;
    @NonNull
    FileItemService fileItemService;
    @NonNull
    FileUtils fileUtils;
    @NonNull
    LlmService llmService;

    public void processFile(String fileName) {
        File file = new File(fileName);
        try {
            processFile(file, false);
        } catch (IOException | InterruptedException | TikaException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public void processFile(File file) {
        try {
            processFile(file, false);
        } catch (IOException | InterruptedException | TikaException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public void processFile(File file, Boolean force) throws IOException, InterruptedException, TikaException, SAXException {
        String fileName = file.getName();
        log.info("Processing file for index: {}", fileName);
        FileItem fileItem = fileItemService.findByFilePath(file.getAbsolutePath());
        log.info("File item :: {}", fileItem);
        if (fileItem == null || force) {
            // Process the file
            String fileId = UUID.randomUUID().toString();
            Map<String, Object> metadata = new HashMap<>();
            fileUtils.populateFileMetadata(file, metadata);
            Map<String, String> fileMetadata = TextExtractor.extractMetadata(file);
            String mimeType = fileMetadata.get("Content-Type");
            fileItem = FileItem.builder()
                    .id(fileId)
                    .filePath(file.getAbsolutePath())
                    .fileName(fileName)
                    .fileExtension(getFileExtension(fileName))
                    .lastIndexed(LocalDateTime.now())
                    .fileSize((Long) metadata.get("fileSize"))
                    .lastModified((LocalDateTime) metadata.get("lastModified"))
                    .creationTime((LocalDateTime) metadata.get("creationTime"))
                    .isHidden((Boolean) metadata.get("isHidden"))
                    .isReadOnly((Boolean) metadata.get("isReadOnly"))
                    .checksum((String) metadata.get("checksum"))
                    .mimeType(mimeType)
                    .owner((String) metadata.get("owner"))
                    .groupName((String) metadata.get("groupName"))
                    .permissions((String) metadata.get("permissions"))
                    .build();

            FileItem savedFileItem = fileItemService.addFileItem(fileItem);

            log.info("Extracting text from file: {}", fileName);

            Map<String, Integer> modifyDateComponents = parseDateComponents(metadata.get("lastModified").toString());
            String modifyDateYear = modifyDateComponents.get("year").toString();
            String modifyDateMonth = modifyDateComponents.get("month").toString();
            String modifyDateDay = modifyDateComponents.get("day").toString();

            Map<String, Integer> createDateComponents = parseDateComponents(metadata.get("creationTime").toString());
            String createDateYear = createDateComponents.get("year").toString();
            String createDateMonth = createDateComponents.get("month").toString();
            String createDateDay = createDateComponents.get("day").toString();

            List<Document> documents = new ArrayList<>();

            Optional<MediaType> mediaType = MediaTypeFactory.getMediaType(file.getAbsolutePath());
            if (mediaType.isPresent()) {
                log.info("mediaType: {}", mediaType.get());
                if (mediaType.get().equals(MediaType.IMAGE_JPEG) ||
                        mediaType.get().equals(MediaType.IMAGE_PNG) ||
                        mediaType.get().equals(MediaType.IMAGE_GIF)) {
                    FileClassification classification = llmService.processPhotoContents(file);
                    String text = classification.getText();
                    List<String> classifications = classification.getClassifications();
                    Document document = new Document(text);
                    document.getMetadata().put("file_id", fileId);
                    document.getMetadata().put("filename", file.getAbsolutePath());
                    document.getMetadata().put("file_size", metadata.get("fileSize"));
                    document.getMetadata().put("extension", getFileExtension(fileName));
                    document.getMetadata().put("last_modified", metadata.get("lastModified"));
                    document.getMetadata().put("modify_date_year", modifyDateYear);
                    document.getMetadata().put("modify_date_month", modifyDateMonth);
                    document.getMetadata().put("modify_date_day", modifyDateDay);
                    document.getMetadata().put("create_date_year", createDateYear);
                    document.getMetadata().put("create_date_month", createDateMonth);
                    document.getMetadata().put("create_date_day", createDateDay);
                    document.getMetadata().put("creation_time", metadata.get("creationTime"));
                    document.getMetadata().put("is_hidden", metadata.get("isHidden"));
                    document.getMetadata().put("is_readonly", metadata.get("isReadOnly"));
                    document.getMetadata().put("checksum", metadata.get("checksum"));
                    document.getMetadata().put("mimeType", mimeType);
                    document.getMetadata().put("owner", metadata.get("owner"));
                    document.getMetadata().put("classification", classifications);
                    log.info("Extracted text from image: {}", text);
                    documents.add(document);
                } else {
                    documents = new OCRTikaDocumentReader(new FileSystemResource(file.getAbsolutePath()))
                            .get()
                            .stream().peek(document -> {
                                //FileClassification classification = llmService.classifyText(document.getText());
                                //List<String> classifications = classification.getClassifications();
                                document.getMetadata().put("file_id", fileId);
                                document.getMetadata().put("filename", file.getAbsolutePath());
                                document.getMetadata().put("file_size", metadata.get("fileSize"));
                                document.getMetadata().put("extension", getFileExtension(fileName));
                                document.getMetadata().put("last_modified", metadata.get("lastModified"));
                                document.getMetadata().put("modify_date_year", modifyDateYear);
                                document.getMetadata().put("modify_date_month", modifyDateMonth);
                                document.getMetadata().put("modify_date_day", modifyDateDay);
                                document.getMetadata().put("create_date_year", createDateYear);
                                document.getMetadata().put("create_date_month", createDateMonth);
                                document.getMetadata().put("create_date_day", createDateDay);
                                document.getMetadata().put("creation_time", metadata.get("creationTime"));
                                document.getMetadata().put("is_hidden", metadata.get("isHidden"));
                                document.getMetadata().put("is_readonly", metadata.get("isReadOnly"));
                                document.getMetadata().put("checksum", metadata.get("checksum"));
                                document.getMetadata().put("mimeType", mimeType);
                                //document.getMetadata().put("classification", classifications);
                                log.info("Reading file :: {}", fileName);
                                log.info("File Id :: {}", fileId);
                            }).toList();
                }
            }

            TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
            log.info("Total number of documents :: {}", documents.size());
            documents.forEach(document -> {
                AtomicInteger counter = new AtomicInteger(0);
                tokenTextSplitter.split(document)
                        .forEach(chunk -> {
                            Integer count = counter.incrementAndGet();
                            String chunkId = chunk.getId();
                            log.info("Processing chunk :: {}", count);
                            FileClassification classification = llmService.classifyText(chunk.getText());
                            List<String> classifications = classification.getClassifications();
                            chunk.getMetadata().put("classification", classifications);
                            chunk.getMetadata().put("doc_id", chunkId);
                            //fileVectorStore.add(List.of(chunk));
                            FileChunk fileChunk = FileChunk.builder()
                                    .id(chunkId)
                                    .chunkIndex(count)
                                    .content(chunk.getFormattedContent())
                                    .metadata(chunk.getMetadata())
                                    //.embedding(embeddingModel.embed(chunk.getFormattedContent()))
                                    .fileItem(savedFileItem)
                                    .build();
                            fileChunkService.addFileChunk(fileChunk);
                            fileChunkService.updateEmbedding(chunkId, embeddingModel.embed(chunk.getFormattedContent()));
                            // Add to elasticsearch
                            DocumentMetadata documentMetadata = DocumentMetadata.builder()
                                    .fileId(chunkId)
                                    .filename(chunk.getMetadata().get("filename").toString())
                                    .filenameKeyword(chunk.getMetadata().get("filename").toString())
                                    .lastModified(parseDateTime(chunk.getMetadata().get("last_modified").toString()))
                                    .fileSize(Long.parseLong(chunk.getMetadata().get("file_size").toString()))
                                    .extension(chunk.getMetadata().get("extension").toString())
                                    .mimeType(chunk.getMetadata().get("mimeType").toString())
                                    .creationTime(parseDateTime(chunk.getMetadata().get("creation_time").toString()))
                                    .isHidden(Boolean.parseBoolean(chunk.getMetadata().get("is_hidden").toString()))
                                    .isReadonly(Boolean.parseBoolean(chunk.getMetadata().get("is_readonly").toString()))
                                    .classification(classifications)
                                    .build();
                            metadataSearch.indexDocument(documentMetadata);
                        });
            });
            // savedFileItem.setIsSensitive(false);
            // fileRepository.save(savedFileItem);
            log.info("Finished processing file for index: {}", fileName);
        } else {
            log.info("File already indexed: {}", fileName);
        }
    }

    public String removeFile(File file) throws IOException, InterruptedException {
        String filePath = file.getAbsolutePath();
        FileItem fileItem = fileItemService.findByFilePath(filePath);
        if (fileItem != null) {
            removeFile(fileItem.getId().toString());
            return "Document with path: " + filePath + " deleted.";
        } else {
            return "No doc exists with path: " + filePath;
        }
    }

    public String removeFile(String fileId) throws IOException, InterruptedException {
        Optional<FileItem> fileItemOpt = fileItemService.findByIdWithChunks(fileId);
        if (fileItemOpt.isPresent()) {
            fileItemOpt.get().getChunks().forEach(fileChunk -> {
                try {
                    metadataSearch.deleteDocument(fileChunk.getId().toString());
                }
                catch (Exception e) {
                    log.error("Error deleting chunk: {}", fileChunk.getId(), e);
                }
            });
            //metadataSearch.
            // fileVectorStore.delete(List.of(fileId));
            fileItemService.deleteById(fileId);
            return "Document with Id: " + fileId + " deleted.";
        } else {
            return "No doc exists with id: " + fileId;
        }
    }

    private String getFileExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx > 0 ? fileName.substring(idx + 1) : "";
    }

    private static LocalDateTime parseDate(String dateTimeString) throws DateTimeParseException {
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ISO_DATE_TIME,  // Handles "2022-04-19T15:49:12"
                DateTimeFormatter.ISO_LOCAL_DATE_TIME, // Handles "2022-04-19T15:49:12"
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"), // Explicit format
                DateTimeFormatter.ISO_DATE,  // Handles "2022-04-19"
                DateTimeFormatter.ofPattern("yyyy-MM-dd")  // Fallback format
        };

        LocalDateTime date = null;
        DateTimeParseException parseException = null;

        // Try each formatter until one works
        for (DateTimeFormatter formatter : formatters) {
            try {
                if (formatter.toString().contains("'T'") || formatter.toString().contains("HH:mm")) {
                    // For date-time formatters, parse to LocalDateTime first
                    date = LocalDateTime.parse(dateTimeString, formatter);
                } else {
                    // For date-only formatters
                    date = LocalDateTime.parse(dateTimeString, formatter);
                }
                break; // If we get here, parsing was successful
            } catch (DateTimeParseException e) {
                parseException = e;
                // Continue to next formatter
            }
        }
        return date;
    }

    private static Instant parseDateTime(String dateTimeString) throws DateTimeParseException {
        return parseDate(dateTimeString).atZone(ZoneId.systemDefault()).toInstant();
    }

    private static Map<String, Integer> parseDateComponents(String dateTimeString) throws DateTimeParseException {
        LocalDate date = null;
        DateTimeParseException parseException = null;
        try {
            date = parseDate(dateTimeString).toLocalDate();
        } catch (DateTimeParseException e) {
            parseException = e;
        }


        if (date == null) {
            throw parseException != null ? parseException :
                    new DateTimeParseException("Could not parse date: " + dateTimeString, dateTimeString, 0);
        }

        Map<String, Integer> dateComponents = new HashMap<>();
        dateComponents.put("year", date.getYear());
        dateComponents.put("month", date.getMonthValue());
        dateComponents.put("day", date.getDayOfMonth());

        return dateComponents;
    }
}
