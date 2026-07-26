package com.br.devsami.util;

import java.math.BigDecimal;

public class PriceValidator {

    public static boolean validate(String price){
        if(!price.matches("^(?=.*\\d)[\\d,.]+$")) {
            throw new IllegalArgumentException("O preço deve ser um valor númerico");
        }

        if(Double.parseDouble(price) < 0){
            throw new IllegalArgumentException("O preço deve ser um valor positivo");
        }

        return true;
    }

    public static String format(String price){
        return price.replace(",", ".");
    }

    public static BigDecimal parsePrice(String price){
        String formatedPrice = format(price);
        double doublePrice = Double.parseDouble(!formatedPrice.isEmpty() ? formatedPrice : "0");
        return BigDecimal.valueOf(doublePrice);
    }

}
