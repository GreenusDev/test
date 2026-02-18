package com.jpa.market.config.exception;

public class OutOfStockException extends RuntimeException{

    public OutOfStockException(String message) {

        super(message);
    }

}