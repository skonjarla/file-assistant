package net.konjarla.aifilemonitor.monitoring.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileIndexService {

    @NonNull
    ApplicationContext context;
    @NonNull
    FileProcessorTaskService fileProcessorTaskService;
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            ".txt", ".pdf", ".doc", ".docx", ".xls", ".xlsx",
            ".ppt", ".pptx", ".md", ".html", ".csv", ".jpg", ".png",
            ".gif", ".jpeg", ".bmp", ".svg"
    );

    private static final Set<String> IGNORED_FILES = Set.of(".DS_Store");
    @Async("scanTaskExecutor")
    public void index() {
        FileWatcherService fileWatcherService = (FileWatcherService) context.getBean("fileWatcherService");
        Set<Path> directories = new HashSet<>(fileWatcherService.getWatchedDirectories());

        directories.forEach(dir -> {
            try (Stream<Path> walk = Files.walk(dir)) {
                walk.filter(Files::isRegularFile)
                        .filter(this::isSupportedFile)
                        .filter(path -> !IGNORED_FILES.contains(path.getFileName().toString()))
                        .forEach(this::processFile);
            } catch (IOException e) {
                log.error("Error walking through directory: " + dir, e);
            }
        });
    }

    /**
     * Indexes all supported files in the specified directory (non-recursive).
     * @param directoryPath The path of the directory to index
     * @return The number of files processed
     */
    @Async("scanTaskExecutor")
    public void indexDirectory(String directoryPath) {
        Path dir = Path.of(directoryPath);
        if (!Files.isDirectory(dir)) {
            log.error("Path is not a directory: {}", directoryPath);
        }

        try (Stream<Path> files = Files.list(dir)) {
            List<Path> fileList = files
                    .filter(Files::isRegularFile)
                    .filter(path -> !IGNORED_FILES.contains(path.getFileName().toString()))
                    .filter(this::isSupportedFile)
                    .toList();

            fileList.forEach(this::processFile);
        } catch (IOException e) {
            log.error("Error listing files in directory: " + directoryPath, e);
        }
    }

    /**
     * Recursively indexes all supported files in the specified directory and its subdirectories.
     * @param directoryPath The path of the directory to index
     * @return The number of files processed
     */
    @Async("scanTaskExecutor")
    public void indexDirectoryRecursively(String directoryPath) {
        Path dir = Path.of(directoryPath);
        if (!Files.isDirectory(dir)) {
            log.error("Path is not a directory: {}", directoryPath);
        }

        try (Stream<Path> walk = Files.walk(dir)) {
            List<Path> fileList = walk
                    .filter(Files::isRegularFile)
                    .filter(this::isSupportedFile)
                    .filter(path -> !IGNORED_FILES.contains(path.getFileName().toString()))
                    .toList();

            fileList.forEach(this::processFile);
        } catch (IOException e) {
            log.error("Error walking through directory: " + directoryPath, e);
        }
    }

    /**
     * Indexes a single file if it's supported.
     * @param filePath The path of the file to index
     * @return true if the file was processed, false otherwise
     */
    @Async("scanTaskExecutor")
    public void indexFile(String filePath) {
        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            log.error("File does not exist: {}", filePath);
        }

        if (!Files.isRegularFile(path)) {
            log.error("Path is not a file: {}", filePath);
        }

        if (!isSupportedFile(path)) {
            log.warn("File type not supported: {}", filePath);
        }

        try {
            if (!IGNORED_FILES.contains(path.getFileName().toString())) {
                processFile(path);
            }
            else {
                log.warn("File ignored: {}", filePath);
            }
        } catch (Exception e) {
            log.error("Error processing file: " + filePath, e);
        }
    }

    @Async("scanTaskExecutor")
    public void deleteFile(String filePath) {
        log.info("Deleting file: {}", filePath);
        try {
            fileProcessorTaskService.performAsyncDeleteFile(new File(filePath));
        } catch (Exception e) {
            log.error("Error deleting file: " + filePath, e);
        }
    }

    private boolean isSupportedFile(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        return SUPPORTED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    //@Async("scanTaskExecutor")
    private void processFile(Path filePath) {
        try {
            File file = filePath.toFile();
            log.info("Indexing file: {}", file.getAbsolutePath());
            fileProcessorTaskService.performAsyncProcessFile(file);
        } catch (Exception e) {
            log.error("Error processing file: " + filePath, e);
        }
    }
}