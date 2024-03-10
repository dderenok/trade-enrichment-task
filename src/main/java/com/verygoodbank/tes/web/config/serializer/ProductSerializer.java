package com.verygoodbank.tes.web.config.serializer;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import com.verygoodbank.tes.web.dto.cache.Product;
import java.io.IOException;

public class ProductSerializer implements StreamSerializer<Product> {

    @Override
    public void write(ObjectDataOutput out, Product product) throws IOException {
        out.writeString(product.getProductName());
        out.writeBoolean(product.isReadyToRemove());
    }

    @Override
    public Product read(ObjectDataInput in) throws IOException {
        return new Product(in.readString(), in.readBoolean());
    }

    @Override
    public int getTypeId() {
        return 1;
    }
}
