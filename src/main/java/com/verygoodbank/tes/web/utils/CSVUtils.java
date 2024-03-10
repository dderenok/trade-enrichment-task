package com.verygoodbank.tes.web.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.csv.CSVFormat.DEFAULT;

/**
 * CSVUtils is a utility class providing static methods to handle CSV (Comma-Separated Values) operations.
 * This class is designed to assist in reading and writing CSV data, commonly used in data interchange and storage.
 * It offers methods to read CSV data from
 * an {@link InputStream}, create a {@link CSVParser} for CSV parsing,
 * and create a {@link CSVPrinter} for CSV writing.
 */
public class CSVUtils {
    public static String[] PRODUCT_HEADERS = { "product_id", "product_name" };
    public static String[] TRADE_IN_HEADERS = { "date", "product_id", "currency", "price" };
    public static String[] TRADE_OUT_HEADERS = { "date", "product_name", "currency", "price" };

    public static Iterable<CSVRecord> readCsv(InputStream inputStream, String[] headers) {
        try (CSVParser csvParser = getCsvParser(inputStream, headers)) {
            return csvParser.getRecords();
        } catch (Exception exception) {
            throw new RuntimeException("An error occurred during csv file parsing ", exception);
        }
    }

    public static CSVParser getCsvParser(InputStream inputStream, String[] headers) {
        try {
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStream, UTF_8));
            CSVFormat csvFormat = DEFAULT.builder()
                .setHeader(headers)
                .setSkipHeaderRecord(true)
                .build();
            return new CSVParser(fileReader, csvFormat);
        } catch (IOException exception) {
            throw new RuntimeException("An error occurred during csv parser creating ", exception);
        }
    }

    public static CSVPrinter getCsvPrinter(File file, String[] headers) {
        try {
            FileWriter writer = new FileWriter(file);
            CSVFormat csvFormat = DEFAULT.builder()
                .setHeader(headers)
                .build();
            return new CSVPrinter(writer, csvFormat);
        } catch (IOException exception) {
            throw new RuntimeException("An error occurred during csv printer creating ", exception);
        }
    }
}
