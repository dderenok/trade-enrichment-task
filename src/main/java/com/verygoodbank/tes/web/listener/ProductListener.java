package com.verygoodbank.tes.web.listener;

import com.hazelcast.map.QueryResultSizeExceededException;
import com.hazelcast.map.ReachedMaxSizeException;
import com.verygoodbank.tes.web.dto.cache.Product;
import com.verygoodbank.tes.web.service.cache.ProductCacheService;
import org.apache.commons.csv.CSVRecord;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map.Entry;
import static com.verygoodbank.tes.web.utils.CSVUtils.PRODUCT_HEADERS;
import static com.verygoodbank.tes.web.utils.CSVUtils.readCsv;
import static java.lang.Long.parseLong;
import static org.springframework.util.ResourceUtils.getFile;

/**
 * ProductListener is responsible for initializing and maintaining the product cache.
 * It listens to application context events and performs cache refresh and cleanup operations.
 * This class is tightly coupled with the {@link ProductCacheService} for cache operations.
 *
 * <p> The {@link #refreshProductCache()} method, triggered by the {@link ContextRefreshedEvent},
 * before the app is started, loads product data from a CSV file and populates the cache.
 * It also starts a background process for clearing products that are marked as ready to remove.
 * This method ensures that the product
 * cache is up-to-date and consistent with the source data upon application startup or context refresh.
 */
@Component
public class ProductListener {
    private final ProductCacheService productCacheService;
    private final Object cacheLock = new Object();

    public ProductListener(ProductCacheService productCacheService) {
        this.productCacheService = productCacheService;
    }

    @EventListener(ContextRefreshedEvent.class)
    private void refreshProductCache() {
        try {
            putProductsIntoCache(getCsvRecords());
            System.out.println("Put products from static resource into the cache.");
            runBackgroundClear();
        } catch (FileNotFoundException exception) {
            throw new RuntimeException("There is no product file in resources: ", exception);
        }
    }

    private Iterable<CSVRecord> getCsvRecords() throws FileNotFoundException {
        File resourceFile = getFile("classpath:product.csv");
        InputStream inputStream = new FileInputStream(resourceFile);
        return readCsv(inputStream, PRODUCT_HEADERS);
    }

    private void putProductsIntoCache(Iterable<CSVRecord> csvRecords) {
        for (CSVRecord csvRecord : csvRecords) {
            productCacheService.put(
                parseLong(csvRecord.get("product_id")),
                new Product(csvRecord.get("product_name"), false)
            );
        }
    }

    private void runBackgroundClear() {
        new Thread(this::clearNotExistProducts)
            .start();
    }

    private void clearNotExistProducts() {
        try {
            System.out.println("Start ready-to-delete products clear process.");
            Iterator<Entry<Long, Product>> cacheMapIterator = productCacheService.getKeySet();
            while (cacheMapIterator.hasNext()) {
                Entry<Long, Product> cacheEntry = cacheMapIterator.next();
                processProduct(cacheEntry.getKey(), cacheEntry.getValue());
            }
            System.out.println("Ready-to-delete products clear process has just finished.");
        } catch (ConcurrentModificationException exception) {
            System.err.println(
                "A concurrent modification detected: " + exception
            );
        } catch (QueryResultSizeExceededException | IllegalStateException exception) {
            System.err.println(
                "An error occurred during retrieving a set of keys and values: " + exception
            );
        } catch (Exception exception) {
            System.err.println(
                "An error occurred during clear process: " + exception
            );
        }
    }

    private void processProduct(Long productId, Product product) {
        if (isProductNull(productId, product)) return;
        synchronized(cacheLock) {
            try {
                if (product.isReadyToRemove()) {
                    System.out.println("The product with id " + productId + " was evicted.");
                    productCacheService.evict(productId);
                } else {
                    product.setReadyToRemove(true);
                    productCacheService.put(productId, product);
                }
            } catch (ReachedMaxSizeException exception) {
                System.err.println("An error occurred during putting updated map entry into the cache: " + exception);
            } catch (Exception exception) {
                System.err.println("An error occurred during modifying the cache map: " + exception);
            }
        }
    }

    private boolean isProductNull(Long productId, Product product) {
        if (product == null) {
            System.err.println("Product with id " + productId + " is null.");
            return true;
        }
        return false;
    }
}
