package com.verygoodbank.tes.web.controller;

import com.verygoodbank.tes.web.service.TradeService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import static com.verygoodbank.tes.web.utils.AppUtils.CSV_CONTENT_TYPE;
import static java.nio.file.Files.size;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.parseMediaType;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1")
public class TradeEnrichmentController {
    private final TradeService tradeService;

    public TradeEnrichmentController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping(value = "/enrich", produces = CSV_CONTENT_TYPE)
    public ResponseEntity<Resource> enrichTradeData(@RequestParam("file") MultipartFile file) {
        File csvFile = tradeService.enrichTrades(file);
        return getResourceEntity(csvFile);
    }
    
    private ResponseEntity<Resource> getResourceEntity(File file) {
        checkFileExistence(file);
        HttpHeaders headers = createCorrespondHeaders(file);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamResource resource = new InputStreamResource(fileInputStream);
            return ok()
                    .headers(headers)
                    .contentLength(size(file.toPath()))
                    .contentType(parseMediaType(CSV_CONTENT_TYPE))
                    .body(resource);
        } catch (FileNotFoundException exception) {
            throw new RuntimeException("File with the translated trades not found: " + file.getName(), exception);
        } catch (IOException exception) {
            throw new RuntimeException("An error occurred during creating csv input stream", exception);
        }
    }

    private void checkFileExistence(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new RuntimeException(
                "File with with the translated trades doesn't exist or is not a file: "
                    + (file != null ? file.getName() : "null")
            );
        }
    }

    private HttpHeaders createCorrespondHeaders(File file) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
        return headers;
    }
}
