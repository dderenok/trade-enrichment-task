package com.verygoodbank.tes.web.utils;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import static java.nio.channels.FileChannel.open;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isWritable;
import static java.nio.file.StandardOpenOption.WRITE;

/**
 * AppUtils is a utility class providing static methods and constants for various common operations
 * and values used throughout the application. It serves as a centralized place for methods that
 * are reusable across different parts of the application.
 */
public class AppUtils {
    public static final String DIRECTORY_TO_CLEAN = "./";
    public static final String CSV_CONTENT_TYPE = "text/csv";
    public static final String COMMA = ",";

    public static void deleteFile(Path file) {
        try {
            if (isWritable(file) && !isDirectory(file) && !isFileLocked(file)) {
                delete(file);
            } else {
                System.err.print(
                    "Skipped deleting file (not writable, is directory or is locked by another process): "
                        + file.getFileName()
                );
            }
        } catch (IOException exception) {
            System.err.println(
                "An error occurred during cleaning file with path " + file.getFileName() + exception.getMessage()
            );
        }
    }

    public static boolean isFileLocked(Path path) {
        try (FileChannel channel = open(path, WRITE)) {
            FileLock lock = channel.tryLock();
            if (lock != null) {
                lock.release();
                return false;
            }
        } catch (IOException exception) {
            System.err.println(
                "An error occurred during acquire lock for file " + path.getFileName() + " " + exception.getMessage()
            );
        }
        return true;
    }

    public static boolean isFileNameStartsWith(Path file, String prefix) {
        return file.getFileName()
            .toString()
            .startsWith(prefix);
    }
}
