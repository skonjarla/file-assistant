package net.konjarla.aifilemonitor.monitoring;

import net.konjarla.aifilemonitor.monitoring.service.FileWatcherService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class WatchServiceRunner implements CommandLineRunner {

    private final FileWatcherService fileWatcherService;

    public WatchServiceRunner(FileWatcherService fileWatcherService) {
        this.fileWatcherService = fileWatcherService;
    }

    @Override
    public void run(String... args) throws Exception {
        fileWatcherService.startWatching();
    }
}
