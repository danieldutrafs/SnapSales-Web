package br.com.snapsales.controllers;

import java.time.LocalDate;

/** Conversoes de texto usadas entre as telas (JSON) e as classes do dominio. */
final class Formatos {

    private Formatos() {
    }

    /** 7 -> "0007" */
    static String codigo(long id) {
        return String.format("%04d", id);
    }

    /** "51999991111" -> "51 999991111" (o mesmo formato da mascara da tela) */
    static String telefone(String digitos) {
        if (digitos == null) {
            return "";
        }
        return digitos.length() > 2 ? digitos.substring(0, 2) + " " + digitos.substring(2) : digitos;
    }

    /** LocalDate -> "2026-10-23" (ou vazio) */
    static String data(LocalDate data) {
        return data == null ? "" : data.toString();
    }

    /** "2026-10-23" -> LocalDate (vazio vira null; formato errado gera DateTimeParseException -> erro 400) */
    static LocalDate paraData(String texto) {
        return texto == null || texto.isBlank() ? null : LocalDate.parse(texto.trim());
    }

    static double arredonda(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
