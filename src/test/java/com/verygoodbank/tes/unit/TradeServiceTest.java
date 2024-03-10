package com.verygoodbank.tes.unit;

import com.verygoodbank.tes.base.BaseConfigurationTest;
import com.verygoodbank.tes.web.dto.cache.Product;
import com.verygoodbank.tes.web.service.TradeService;
import com.verygoodbank.tes.web.service.cache.ProductCacheService;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import java.io.File;
import java.io.IOException;
import static com.verygoodbank.tes.web.utils.CSVUtils.TRADE_OUT_HEADERS;
import static com.verygoodbank.tes.web.utils.CSVUtils.getCsvParser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class TradeServiceTest extends BaseConfigurationTest {

    @Mock
    private ProductCacheService productCacheService;

    private TradeService tradeService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        tradeService = new TradeService(productCacheService);
    }

    @Test
    void enrichTradesShouldReturnFileWithEnrichedData() {
        MockMultipartFile file = new MockMultipartFile(
            "data",
            "filename.csv",
            "text/plain",
            "20160101,1,EUR,10.0".getBytes()
        );
        when(productCacheService.get(1L)).thenReturn(new Product("Dummy Product Name", true));

        File enrichedFile = tradeService.enrichTrades(file);

        try {
            CSVParser parser = getCsvParser(file.getInputStream(), TRADE_OUT_HEADERS);
            for (CSVRecord record : parser) {
                assertThat(record.get("product_name"), is(not(nullValue())));
            }
        } catch (IOException exception) {
            throw new RuntimeException("An error occurred during the getting input stream: ", exception);
        }

        assertNotNull(enrichedFile);
    }

    @Test
    void enrichTradesShouldHandleMissingProductName() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "data",
            "filename.csv",
            "text/plain",
            "20160101,1,EUR,10.0".getBytes()
        );
        when(productCacheService.get(1L)).thenReturn(null);

        File enrichedFile = tradeService.enrichTrades(file);

        assertNotNull(enrichedFile);
    }
}