package com.verygoodbank.tes.web.service;

import com.verygoodbank.tes.web.dto.Trade;
import com.verygoodbank.tes.web.dto.cache.Product;
import com.verygoodbank.tes.web.service.cache.ProductCacheService;
import com.verygoodbank.tes.web.validation.DateFormatValidator;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import static com.verygoodbank.tes.web.utils.AppUtils.COMMA;
import static com.verygoodbank.tes.web.utils.CSVUtils.*;
import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;
import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;
import static java.util.UUID.randomUUID;

/**
 * TradeService is responsible for processing and enriching trade data.
 * It leverages {@link ProductCacheService} to enrich trades with additional product information.
 * This class provides functionality to read trade data from a file, enrich it, and
 * write the updated data back to a new file.
 *
 * <p> The primary method, {@link #enrichTrades(MultipartFile)}, handles the full process of
 * reading trade data, enriching it with product names from the cache, and writing the updated
 * information to a new file.
 * This method is designed to process CSV format files.
 */
@Service
public class TradeService {
    private final ProductCacheService productCacheService;

    public TradeService(ProductCacheService productCacheService) {
        this.productCacheService = productCacheService;
    }

    public File enrichTrades(MultipartFile file) {
        try {
            List<Trade> trades = getTrades(file.getInputStream());
            setTradeProductNames(trades);
            return writeUpdatedTradesToFile(trades, generateTradeTranslatedFileName());
        } catch (IOException exception) {
            throw new RuntimeException("An error occurred during getting file input stream ", exception);
        }
    }

    private List<Trade> getTrades(InputStream inputStream) {
        try {
            Iterable<CSVRecord> csvRecords = readCsv(inputStream, TRADE_IN_HEADERS);
            DateFormatValidator dateFormatValidator = new DateFormatValidator(BASIC_ISO_DATE);
            return collectValidTrades(csvRecords, dateFormatValidator);
        } catch (Exception exception) {
            throw new RuntimeException("An error occurred during csv file parsing ", exception);
        }
    }

    private List<Trade> collectValidTrades(Iterable<CSVRecord> csvRecords, DateFormatValidator dateFormatValidator) {
        List<Trade> trades = new ArrayList<>();
        for (CSVRecord csvRecord : csvRecords) {
            String productDate = csvRecord.get("date");
            long productId = parseLong(csvRecord.get("product_id"));
            if (!dateFormatValidator.isValid(productDate)) {
                System.err.println(
                    "There is invalid date format for product with id " + productId +
                        ". Discard product from csv file."
                );
            } else {
                trades.add(createTrade(productId, productDate, csvRecord));
            }
        }
        return trades;
    }

    private Trade createTrade(Long productId, String productDate, CSVRecord csvRecord) {
        return new Trade(
            productDate,
            productId,
            csvRecord.get("currency"),
            parseDouble(csvRecord.get("price"))
        );
    }

    private void setTradeProductNames(List<Trade> trades) {
        for (Trade trade : trades) {
            Product product = productCacheService.get(trade.getProductId());
            if (product == null) {
                System.err.println(
                    "There is no name for product with id " + trade.getProductId() +
                        ". Discard product from final csv."
                );
                trade.setProductName("Missing Product Name");
            } else {
                trade.setProductName(product.getProductName());
            }
        }
    }

    public File writeUpdatedTradesToFile(List<Trade> trades, String filePath) throws IOException {
        File file = new File(filePath);
        CSVPrinter csvPrinter = getCsvPrinter(file, TRADE_OUT_HEADERS);
        try {
            addRecordsToFile(trades, csvPrinter);
        } catch (IOException exception){
            throw new RuntimeException("An error occurred during csv file writing ", exception);
        } finally {
            csvPrinter.flush();
            csvPrinter.close();
        }
        return file;
    }

    private void addRecordsToFile(List<Trade> trades, CSVPrinter csvPrinter) throws IOException {
        for (Trade trade : trades) {
            String[] record = trade.toString()
                    .split(COMMA);
            csvPrinter.printRecord(record);
        }
    }

    private String generateTradeTranslatedFileName() {
        return "trade-" + randomUUID() + ".csv";
    }
}
