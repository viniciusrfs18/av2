package io.sim;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {

    private static final String chaveEncriptacao = "9876543210abcdef";
    private static final String IV = "AAAAAAAAAAAAAAAA";
    private static final int tamNumBytes = 32;

    public Crypto() {

    }

    public static int getTamNumBytes() {
        return tamNumBytes;
    }

    public static byte[] encripta(String textopuro) throws Exception {
        Cipher encripta = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
        SecretKeySpec key = new SecretKeySpec(chaveEncriptacao.getBytes("UTF-8"), "AES");
        encripta.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
        byte[] en = encripta.doFinal(textopuro.getBytes("UTF-8"));
        return en;
    }

    public static String decripta(byte[] textoEncriptado) throws Exception{
        Cipher decripta = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
        SecretKeySpec key = new SecretKeySpec(chaveEncriptacao.getBytes("UTF-8"), "AES");
        decripta.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
        return new String(decripta.doFinal(textoEncriptado),"UTF-8");
    }

}