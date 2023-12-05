package io.sim.simulator.comunication;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *      A classe AESencrypt é responsável por lidar com a criptografia e decriptação de dados usando o algoritmo AES 
 * (Advanced Encryption Standard) com modo CBC (Cipher Block Chaining) e preenchimento PKCS5. Ele fornece métodos para 
 * encriptar e decriptar textos, utilizando uma chave de encriptação especificada. O vetor de inicialização (IV) é 
 * usado para garantir que textos iguais não gerem resultados de encriptação idênticos.
 */
public class AESencrypt {
    private static final String chaveEncriptacao = "9876543210abcdef";
    private static final String IV = "AAAAAAAAAAAAAAAA";
    private static final int tamNumBytes = 32;

    public AESencrypt() {
        // Construtor vazio, não realiza nenhuma ação especial.
    }

    public static int getTamNumBytes() {
        return tamNumBytes;
        // Retorna o tamanho dos bytes usados na encriptação (32 bytes).
    }

    // Método para encriptar uma String.
    public static byte[] encripta(String textopuro) throws Exception {
        Cipher encripta = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
        SecretKeySpec key = new SecretKeySpec(chaveEncriptacao.getBytes("UTF-8"), "AES");
        encripta.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV.getBytes("UTF-8")));
        // Encripta os bytes do texto puro fornecido em UTF-8 e retorna os bytes encriptados.
        return encripta.doFinal(textopuro.getBytes("UTF-8"));
    }

    // Método para decriptar um texto encriptado.
    public static String decripta(byte[] textoEncriptado) throws Exception {
        Cipher decripta = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
        SecretKeySpec key = new SecretKeySpec(chaveEncriptacao.getBytes("UTF-8"), "AES");
        decripta.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV.getBytes("UTF-8")));
        // Decripta os bytes do texto encriptado fornecido e retorna o texto em UTF-8.
        return new String(decripta.doFinal(textoEncriptado), "UTF-8");
    }
}
