package net.konjarla.aifilemonitor.monitoring;

import net.konjarla.aifilemonitor.monitoring.service.FileIndexService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FileIndexServiceRunner implements CommandLineRunner {

    private final FileIndexService fileIndexService;

    public FileIndexServiceRunner(FileIndexService fileIndexService) {
        this.fileIndexService = fileIndexService;
    }

    @Override
    public void run(String... args) throws Exception {
        fileIndexService.index();
    }
}
