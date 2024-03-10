package com.verygoodbank.tes.web.service.cache;

import com.hazelcast.map.IMap;
import com.verygoodbank.tes.web.config.HazelcastConfig;
import com.verygoodbank.tes.web.dto.cache.Product;
import org.springframework.stereotype.Service;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * ProductCacheService provides a layer of abstraction over a distributed cache, specifically
 * for caching product-related information.
 * It utilizes Hazelcast as the underlying caching mechanism, configured through {@link HazelcastConfig}.
 * This service allows for the storage, retrieval, and eviction of product data within the cache.
 *
 * <p> The class defines basic cache operations such as 'put', 'get', 'evict', and a method
 * to get an iterator over cache entries.
 * The actual cache map is obtained from the HazelcastConfig, and operations are performed on this map.
 *
 * <p> The {@code PRODUCTS} constant defines the name of the cache map that this service interacts with.
 */
@Service
public class ProductCacheService {
    private final HazelcastConfig hazelcastConfig;
    public static final String PRODUCTS = "products";

    public ProductCacheService(HazelcastConfig hazelcastConfig) {
        this.hazelcastConfig = hazelcastConfig;
    }

    public void put(Long productId, Product product) {
        IMap<Long, Product> map = hazelcastConfig.getCacheMap(PRODUCTS);
        map.put(productId, product);
    }

    public Product get(Long productId) {
        IMap<Long, Product> map = hazelcastConfig.getCacheMap(PRODUCTS);
        return map.get(productId);
    }

    public void evict(Long productId) {
        IMap<Long, Product> map = hazelcastConfig.getCacheMap(PRODUCTS);
        map.evict(productId);
    }

    public Iterator<Entry<Long, Product>> getKeySet() {
        IMap<Long, Product> map = hazelcastConfig.getCacheMap(PRODUCTS);
        return map.entrySet()
            .iterator();
    }
}
