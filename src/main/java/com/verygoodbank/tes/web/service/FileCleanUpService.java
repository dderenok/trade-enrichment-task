package com.verygoodbank.tes.web.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import static com.verygoodbank.tes.web.utils.AppUtils.*;
import static java.nio.file.Files.*;
import static java.nio.file.Paths.get;
import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.ofInstant;
import static java.time.ZoneId.systemDefault;

/**
 * FileCleanUpService is a service class responsible for performing periodic file cleanup
 * operations in a current directory. It is designed to run a background job that identifies
 * and deletes specific files based on predefined criteria.
 *
 * <p> The cleanup operation is scheduled to run every hour at the 0th minute.
 *
 * <p> The method {@code wasFileCreatedAnHourAgo} is a utility used within this service to
 * check if a file's creation time was more than an hour ago, aiding in determining whether
 * the file should be deleted.
 */
@Service
public class FileCleanUpService {
    @Async
    @Scheduled(cron = "0 0 * * * ?")
    protected void cleanUpFiles() {
        System.out.println("Clean background job has just started.");
        try (Stream<Path> files = list(get(DIRECTORY_TO_CLEAN))) {
            files.filter(file -> isFileNameStartsWith(file, "trade-"))
                .forEach(file -> {
                    if (wasFileCreatedAnHourAgo(file)) deleteFile(file);
                });
        } catch (IOException exception) {
            System.err.println("An error occurred during cleaning file job: " + exception.getMessage());
        }
        System.out.println("Clean background job has just finished.");
    }

    private boolean wasFileCreatedAnHourAgo(Path path) {
        try {
            BasicFileAttributes attr = readAttributes(path, BasicFileAttributes.class);
            LocalDateTime creationTime = ofInstant(attr.creationTime().toInstant(), systemDefault());
            return creationTime.isBefore(now().minusHours(1));
        } catch (IOException exception) {
            System.err.println("Error reading file attributes: " + exception.getMessage());
            return false;
        }
    }
}
