package com.br.devsami.util;


public class CpfValidator {

    public static String validate(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return "O CPF não pode estar vazio.";
        }

        String cleanCpf = cpf.trim();

       
        if (!cleanCpf.matches("\\d+")) {
            return "O CPF deve conter apenas números (sem letras ou caracteres especiais).";
        }

        // Check length
        if (cleanCpf.length() < 11) {
            return "O CPF informado é muito curto. Deve conter exatamente 11 dígitos.";
        } else if (cleanCpf.length() > 11) {
            return "O CPF informado é muito longo. Deve conter exatamente 11 dígitos.";
        }

        return null;
    }

    public static boolean isValidCpf(String cpf) {
        return validate(cpf) == null;
    }
}