package br.com.snapsales.utils;

import java.util.List;

/**
 *
 * @author Daniel Dutra
 */
public class ValidadorUtil {
    
    public static boolean validarCampoTexto(String texto){
        return texto != null && !texto.trim().isEmpty();
    }
    
    public static boolean validarCampoInt(Integer quantidade){
        return quantidade != null && quantidade > 0;
    }
    
    public static boolean validarCampoDouble(Double valor){
        return valor != null && valor > 0;
    }
    
    public static boolean validarLista(List<?> lista){
        return lista != null && !lista.isEmpty();
    }

    public static boolean validarQuantidadeNaoNegativa(Integer quantidade) {
        return quantidade != null && quantidade >= 0;
    }

    public static boolean validarValorNaoNegativo(Double valor) {
        return valor != null && valor >= 0;
    }
}
