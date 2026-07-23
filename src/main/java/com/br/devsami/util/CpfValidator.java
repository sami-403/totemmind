package com.br.devsami.util;

/**
 * Utility class for CPF validation (simplified to 11 digits/numeric check).
 */
public class CpfValidator {

    // Regex enforces exactly 11 characters, all of which must be digits [0-9].
    private static final String CP_REGEX = "^\\d{11}$";

    /**
     * Validates a given CPF string by ensuring it consists of exactly 11 numeric digits.
     *
     * @param cpf The CPF string to validate.
     * @return null if the CPF is valid, or a descriptive error message string if it is invalid.
     */
    public static String validate(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return "O CPF não pode estar vazio.";
        }

        String cleanCpf = cpf.trim();

        // Check if contains letters or special characters (non-digits)
        if (!cleanCpf.matches("\\d+")) {
            return "O CPF deve conter apenas números (sem letras ou caracteres especiais).";
        }

        // Check length
        if (cleanCpf.length() < 11) {
            return "O CPF informado é muito curto. Deve conter exatamente 11 dígitos.";
        } else if (cleanCpf.length() > 11) {
            return "O CPF informado é muito longo. Deve conter exatamente 11 dígitos.";
        }

        // If it passes the basic format check
        return null;
    }

    /**
     * Helper method that returns true if the given CPF is valid (passes basic validation).
     *
     * @param cpf The CPF string to validate.
     * @return true if valid, false otherwise.
     */
    public static boolean isValidCpf(String cpf) {
        return validate(cpf) == null;
    }
}