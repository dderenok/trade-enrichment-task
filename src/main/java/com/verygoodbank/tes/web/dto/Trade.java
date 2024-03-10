package com.verygoodbank.tes.web.dto;

import static com.verygoodbank.tes.web.utils.AppUtils.COMMA;

/**
 * Trade represents a financial trade transaction, encapsulating details such as date,
 * product identifier, currency, and price.
 * It is designed to hold the information necessary to describe a single trade activity.
 */
public class Trade {
    private String date;
    private long productId;
    private String currency;
    private double price;
    private String productName;

    public Trade(String date, long productId, String currency, double price) {
        this.date = date;
        this.productId = productId;
        this.currency = currency;
        this.price = price;
    }

    public long getProductId() {
        return productId;
    }


    public void setProductName(String productName) {
        this.productName = productName;
    }

    @Override
    public String toString() {
        return date + COMMA + productName + COMMA + currency + COMMA + price;
    }
}
