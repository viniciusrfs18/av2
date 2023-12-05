package io.sim;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import io.sim.simulator.bank.Account;
import io.sim.simulator.bank.AlphaBank;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *  /\/\/\/\/\/\/\/\/\/\//\/\/\\//\/\/\ ATENÇÃO!! /\/\/\/\/\/\/\/\/\//\/\/\/\/\/\/\/\/\/\/\
 *  Se você tentar realizar os testes todos de uma vez irá ocorrer uma falha dizendo que o
 *  endereço já foi usado. Realize os testes "UM A UM" e todos irão funcionar perfeitamente!
 */
public class AlphaBankTest {
    private AlphaBank alphaBank;

    // Configuração inicial para os testes
    @Before
    public void setUp() throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345); // Porta fícticia
        alphaBank = new AlphaBank(1,serverSocket);
    }

    // Teste para adição de conntas-correntes
    @Test
    public void testAdicionarAccount() {
        Account account = new Account("TestAccount", "password", 1599.25);
        AlphaBank.adicionarAccount(account);
        assertTrue(alphaBank.getAccountPeloID("TestAccount") != null);
    }

    // Teste para remoção de conntas-correntes
    @Test
    public void testRemoverAccount() {
        Account account = new Account("TestAccount", "password", 1599.25);
        AlphaBank.adicionarAccount(account);
        alphaBank.removerAccount("TestAccount");
        assertTrue(alphaBank.getAccountPeloID("TestAccount") == null);
    }

    // Teste para feitura de login
    @Test
    public void testFazerLogin() {
        // Teste para verificar o login de um cliente com credenciais corretas
        Account account = new Account("TestAccount", "password", 1599.25);
        AlphaBank.adicionarAccount(account);
        assertTrue(alphaBank.fazerLogin(new String[] { "TestAccount", "password" }));
    }

    // Teste para feitura de transferência bancária
    @Test
    public void testTransferencia() {
        // Teste para verificar uma transferência entre contas
        Account pagador = new Account("PagadorAccount", "password", 1599.25);
        Account recebedor = new Account("RecebedorAccount", "password", 500.0);
        AlphaBank.adicionarAccount(pagador);
        AlphaBank.adicionarAccount(recebedor);

        assertTrue(alphaBank.transferencia("PagadorAccount", "RecebedorAccount", 200.0));
        assertEquals(1399.25, pagador.getSaldo(), 0.001);
        assertEquals(700.0, recebedor.getSaldo(), 0.001);
        assertFalse(alphaBank.transferencia("PagadorAccount", "RecebedorAccount", 2000.0));
    }
}

