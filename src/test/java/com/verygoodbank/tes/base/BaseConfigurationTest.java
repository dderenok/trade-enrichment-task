package com.verygoodbank.tes.base;

import com.verygoodbank.tes.web.utils.AppUtils;
import org.junit.jupiter.api.AfterEach;
import java.nio.file.Path;
import java.util.stream.Stream;
import static com.verygoodbank.tes.web.utils.AppUtils.DIRECTORY_TO_CLEAN;
import static com.verygoodbank.tes.web.utils.AppUtils.isFileNameStartsWith;
import static java.nio.file.Files.list;
import static java.nio.file.Paths.get;

public class BaseConfigurationTest {
    @AfterEach
    public void cleanUp() {
        try (Stream<Path> files = list(get(DIRECTORY_TO_CLEAN))) {
            files.filter(file -> isFileNameStartsWith(file, "trade-"))
                .forEach(AppUtils::deleteFile);
        } catch (Exception exception) {
            throw new RuntimeException("An error occurred during clearing of files after the tests: ", exception);
        }
    }
}
