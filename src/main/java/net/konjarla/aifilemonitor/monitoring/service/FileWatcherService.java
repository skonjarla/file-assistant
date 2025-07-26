package net.konjarla.aifilemonitor.monitoring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for monitoring file system events in specified directories.
 * Supports watching multiple directories with per-directory control.
 */
@Service
@Qualifier("fileWatcherService")
public class FileWatcherService {

    private final WatchService watchService;
    private final Map<Path, WatchKey> pathToWatchKey;
    private final Map<WatchKey, Path> watchKeyToPath;
    private final List<Path> directoriesToWatch;
    private final FileIndexService fileIndexService;

    /**
     * Creates a new FileWatcherService with the specified directories to watch.
     *
     * @param directoryPaths List of directory paths to watch
     * @throws IOException if the WatchService cannot be created or no valid directories are provided
     */
    public FileWatcherService(@Value("${file.watch.directories}") List<String> directoryPaths, @Autowired
    FileIndexService fileIndexService) throws IOException {
        this.fileIndexService = fileIndexService;
        this.watchService = FileSystems.getDefault().newWatchService();
        this.pathToWatchKey = new HashMap<>();
        this.watchKeyToPath = new HashMap<>();
        this.directoriesToWatch = directoryPaths.stream()
                .map(Paths::get)
                .filter(Files::isDirectory)
                .collect(Collectors.toList());

        if (this.directoriesToWatch.isEmpty()) {
            throw new IllegalArgumentException("No valid directories to watch. Please check your configuration.");
        }

        registerDirectories();
    }

    /**
     * Register a single directory for watching.
     *
     * @param dir The directory to watch
     * @return true if registration was successful, false otherwise
     */
    public boolean registerDirectory(Path dir) {
        try {
            if (!Files.isDirectory(dir)) {
                System.err.println("Cannot watch " + dir + ": not a directory");
                return false;
            }

            if (pathToWatchKey.containsKey(dir)) {
                System.out.println("Already watching directory: " + dir.toAbsolutePath());
                return true;
            }

            WatchKey key = dir.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY
            );

            pathToWatchKey.put(dir, key);
            watchKeyToPath.put(key, dir);
            System.out.println("Now watching directory: " + dir.toAbsolutePath());
            return true;

        } catch (IOException e) {
            System.err.println("Error watching directory " + dir + ": " + e.getMessage());
            return false;
        }
    }

    private void registerDirectories() throws IOException {
        boolean atLeastOneRegistered = false;

        for (Path dir : directoriesToWatch) {
            if (registerDirectory(dir)) {
                atLeastOneRegistered = true;
            }
        }

        if (!atLeastOneRegistered) {
            throw new IOException("Failed to register any directories for watching");
        }
    }

    /**
     * Starts the file watching service in a separate thread.
     * This method runs asynchronously and will continue to monitor
     * the registered directories until stopped.
     */
    @Async("watcherTaskExecutor")
    public void startWatching() {
        try {
            System.out.println("Starting file watcher service...");
            WatchKey key;
            while ((key = watchService.take()) != null) {
                Path dir = watchKeyToPath.get(key);
                if (dir == null) {
                    System.err.println("WatchKey not recognized!");
                    boolean valid = key.reset();
                    if (!valid) {
                        watchKeyToPath.remove(key);
                        if (watchKeyToPath.isEmpty()) {
                            break;
                        }
                    }
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    System.out.printf("Event kind: %s. File: %s in %s%n",
                            event.kind(), event.context(), dir);
                    handleFileEvent(dir, event);
                }

                boolean valid = key.reset();
                if (!valid) {
                    Path watchedDir = watchKeyToPath.remove(key);
                    if (watchedDir != null) {
                        pathToWatchKey.remove(watchedDir);
                    }
                    if (watchKeyToPath.isEmpty()) {
                        System.out.println("No more directories being watched. Shutting down watcher.");
                        break;
                    }
                }
            }
        } catch (InterruptedException e) {
            System.out.println("WatchService interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (ClosedWatchServiceException e) {
            System.out.println("WatchService has been closed");
        } finally {
            try {
                if (watchService != null) {
                    watchService.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing watch service: " + e.getMessage());
            }
        }
    }

    //@Async("watcherTaskExecutor")
    private void handleFileEvent(Path dir, WatchEvent<?> event) {
        try {
            Path fullPath = dir.resolve((Path) event.context());
            System.out.printf("Processing %s event for: %s%n", event.kind(), fullPath);
            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                if(fullPath.toFile().isDirectory()) {
                    registerDirectory(fullPath);
                    fileIndexService.indexDirectoryRecursively(fullPath.toFile().getAbsolutePath());
                } else {
                    fileIndexService.indexFile(fullPath.toFile().getAbsolutePath());
                }
            } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                if(fullPath.toFile().isDirectory()) {
                    fileIndexService.indexDirectoryRecursively(fullPath.toFile().getAbsolutePath());
                } else {
                    fileIndexService.indexFile(fullPath.toFile().getAbsolutePath());
                }
            } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                fileIndexService.deleteFile(fullPath.toFile().getAbsolutePath());
            }
            // Your event handling logic here
            // For example:
            // if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
            //     // Handle file creation
            // } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
            //     // Handle file modification
            // } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
            //     // Handle file deletion
            // }

        } catch (Exception e) {
            System.err.println("Error handling file event: " + e.getMessage());
        }
    }

    /**
     * Stop watching a specific directory.
     *
     * @param directoryPath The path of the directory to stop watching
     * @return true if the directory was being watched and is now stopped, false otherwise
     */
    public boolean stopWatching(Path directoryPath) {
        try {
            WatchKey key = pathToWatchKey.get(directoryPath);
            if (key != null) {
                key.cancel();
                pathToWatchKey.remove(directoryPath);
                watchKeyToPath.remove(key);
                System.out.println("Stopped watching directory: " + directoryPath);
                return true;
            }
            System.out.println("Directory not being watched: " + directoryPath);
            return false;
        } catch (Exception e) {
            System.err.println("Error stopping watch for directory " + directoryPath + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Stop watching all directories and close the watch service.
     * This will stop the file watching service completely.
     */
    public void stopWatchingAll() {
        try {
            // Cancel all watch keys
            for (WatchKey key : watchKeyToPath.keySet()) {
                key.cancel();
            }

            // Clear the maps
            pathToWatchKey.clear();
            watchKeyToPath.clear();

            // Close the watch service
            if (watchService != null) {
                watchService.close();
            }

            System.out.println("Stopped watching all directories");
        } catch (IOException e) {
            System.err.println("Error stopping file watcher: " + e.getMessage());
        }
    }

    /**
     * Get a list of all currently watched directories.
     *
     * @return List of paths being watched
     */
    public List<Path> getWatchedDirectories() {
        return List.copyOf(pathToWatchKey.keySet());
    }
}