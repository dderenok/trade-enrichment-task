package com.verygoodbank.tes.web.config;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.verygoodbank.tes.web.config.serializer.ProductSerializer;
import com.verygoodbank.tes.web.dto.cache.BaseWrapper;
import com.verygoodbank.tes.web.dto.cache.Product;
import org.springframework.stereotype.Component;
import static com.hazelcast.client.HazelcastClient.newHazelcastClient;

/**
 * HazelcastConfig is a component class responsible for configuring and providing access
 * to Hazelcast distributed data structures.
 * It encapsulates the initialization and configuration
 * of a Hazelcast client instance and offers methods to retrieve distributed maps for caching purposes.
 *
 * <p> This class specifically handles the creation of a Hazelcast {@link HazelcastInstance}
 * and provides utility methods to access Hazelcast {@link IMap} instances.
 *
 * <p> The configuration for Hazelcast client is set up by the {@link #createClientConfig()} method,
 * which includes custom serialization configuration for distributed objects.
 */
@Component
public class HazelcastConfig {
    private final HazelcastInstance hazelcastInstance = newHazelcastClient(createClientConfig());

    public <T extends BaseWrapper> IMap<Long, T> getCacheMap(String cacheMapName) {
        return hazelcastInstance.getMap(cacheMapName);
    }

    public ClientConfig createClientConfig() {
        ClientConfig config = new ClientConfig();
        config.getSerializationConfig()
            .addSerializerConfig(serializerConfig());
        return config;
    }

    private SerializerConfig serializerConfig() {
        return new SerializerConfig()
            .setImplementation(new ProductSerializer())
            .setTypeClass(Product.class);
    }
}
