package com.verygoodbank.tes.integration;

import com.verygoodbank.tes.base.BaseConfigurationTest;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import static com.verygoodbank.tes.utils.TestUtils.TREASURY_BILLS_DOMESTIC_PRODUCT_NAME;
import static com.verygoodbank.tes.web.utils.AppUtils.CSV_CONTENT_TYPE;
import static com.verygoodbank.tes.web.utils.CSVUtils.TRADE_OUT_HEADERS;
import static com.verygoodbank.tes.web.utils.CSVUtils.getCsvParser;
import static java.nio.file.Files.readAllBytes;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.ResourceUtils.getFile;

@SpringBootTest
@AutoConfigureMockMvc
public class TradeEnrichmentApiTest extends BaseConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    private static MockMultipartFile mockFile;
    private static final String ENRICH_ENDPOINT = "/api/v1/enrich";

    @BeforeAll
    public static void setup() throws Exception {
        mockFile = loadFileAsMockMultipartFile("trade.csv");
    }

    private static MockMultipartFile loadFileAsMockMultipartFile(String filename) throws Exception {
        File file = getFile("classpath:" + filename);
        try {
            byte[] content = readAllBytes(file.toPath());
            return new MockMultipartFile("file", filename, CSV_CONTENT_TYPE, content);
        } catch (Exception exception) {
            throw new RuntimeException("An issue occurred during file reading ", exception);
        }
    }

    @Test
    public void enrichTradeSuccessfullyTranslateFile() throws Exception {
        mockMvc.perform(multipart(ENRICH_ENDPOINT)
                    .file(mockFile)
                    .contentType(MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(content().contentType(CSV_CONTENT_TYPE));
    }

    @Test
    public void enrichTradeReturnsNoFileIfThereIsNoRequestFile() throws Exception {
        mockMvc.perform(multipart(ENRICH_ENDPOINT)
                    .contentType(MULTIPART_FORM_DATA))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void enrichTradeReturnsValidContent() throws Exception {
        MockMultipartFile mockMultipartFile = loadFileAsMockMultipartFile("singleTrade.csv");
        MockHttpServletResponse response = mockMvc.perform(multipart(ENRICH_ENDPOINT)
                    .file(mockMultipartFile)
                    .contentType(MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(content().contentType(CSV_CONTENT_TYPE))
            .andReturn()
            .getResponse();

        InputStream inputStream = new ByteArrayInputStream(response.getContentAsByteArray());
        try (CSVParser parser = getCsvParser(inputStream, TRADE_OUT_HEADERS)) {
            for (CSVRecord record : parser) {
                assertThat(TREASURY_BILLS_DOMESTIC_PRODUCT_NAME, is(equalTo(record.get("product_name"))));
            }
        }
    }

    @Test
    public void enrichTradeReturnsValidContents() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(multipart(ENRICH_ENDPOINT)
                    .file(mockFile)
                    .contentType(MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(content().contentType(CSV_CONTENT_TYPE))
            .andReturn()
            .getResponse();

        InputStream inputStream = new ByteArrayInputStream(response.getContentAsByteArray());
        try (CSVParser parser = getCsvParser(inputStream, TRADE_OUT_HEADERS)) {
            for (CSVRecord record : parser) {
                assertThat(record.get("product_name"), is(not(nullValue())));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalidTrade.csv", "emptyTrade.csv", "emptyFile.csv"})
    public void enrichTradeHasEmptyTradesIfThereIsInvalidRequestTrades(String fileName) throws Exception {
        MockMultipartFile mockMultipartFile = loadFileAsMockMultipartFile(fileName);
        MockHttpServletResponse response = mockMvc.perform(multipart(ENRICH_ENDPOINT)
                    .file(mockMultipartFile)
                    .contentType(MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(content().contentType(CSV_CONTENT_TYPE))
            .andReturn()
            .getResponse();

        InputStream inputStream = new ByteArrayInputStream(response.getContentAsByteArray());
        try (CSVParser parser = getCsvParser(inputStream, TRADE_OUT_HEADERS)) {
            assertThat(parser.iterator().hasNext(), is(false));
        }
    }
}
