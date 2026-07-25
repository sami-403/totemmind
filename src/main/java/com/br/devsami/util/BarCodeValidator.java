package com.br.devsami.util;


public class BarCodeValidator {

    public static boolean validate(String barCode) {
        if(isEmptyOrNull(barCode)){
            return true;
        }

        String cleanBarCode = barCode.trim();

        if (!cleanBarCode.matches("\\d+")) {
            throw  new IllegalArgumentException("O código de barras só pode possuir números");
        }

        // Check length
        if (cleanBarCode.length() != 13) {
            throw new IllegalArgumentException("O código de barras deve conter exatamente 13 dígitos");
        }

        return true;
    }

    public static boolean compare(String barCodeA, String barCodeB){
        if (barCodeA == null || barCodeA.isEmpty()){
            return false;
        }
        if (barCodeB == null || barCodeB.isEmpty()){
            return  false;
        }

        return barCodeA.equals(barCodeB);
    }

    public static boolean isEmptyOrNull(String barCode){
        return barCode == null || barCode.trim().isEmpty();
    }
}