package io.sim;

import static org.junit.Assert.*;
import org.junit.Test;

import io.sim.simulator.comunication.AESencrypt;

public class AESencryptTest {

    // Teste de encriptação e decriptação 
    @Test
    public void testEncriptaDecripta() {
        try {
            String textoOriginal = "Texto de teste para encriptação e decriptação.";

            // Encripta o texto
            byte[] textoEncriptado = AESencrypt.encripta(textoOriginal);

            // Decripta o texto
            String textoDecriptado = AESencrypt.decripta(textoEncriptado);

            assertEquals(textoOriginal, textoDecriptado);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
