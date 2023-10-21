package io.sim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import io.sim.Pagamentos.Account;

public class AccountTest {
    private Account account;

    @Before
    public void setUp() {
        this.account = new Account("TestAccount", 1000.0);
    }

    @Test
    public void testGetAccountID() {
        assertEquals("TestAccount", account.getAccountID());
    }

    @Test
    public void testGetSenha() {
        assertTrue(account.getSenha().matches("\\d{6}"));
    }

    @Test
    public void testGetSaldo() {
        assertEquals(1000.0, account.getSaldo(), 0.001);
    }

    @Test
    public void testSetBalance() {
        account.setBalance(1500.0);
        assertEquals(1500.0, account.getSaldo(), 0.001);
    }

    @Test
    public void testDeposito() {
        account.deposito(500.0);
        assertEquals(1500.0, account.getSaldo(), 0.001);
    }

    @Test
    public void testDepositoComValorNegativo() {
        account.deposito(-500.0);
        assertEquals(1000.0, account.getSaldo(), 0.001);
    }

    @Test
    public void testSaque() {
        account.saque(500.0);
        assertEquals(500.0, account.getSaldo(), 0.001);
    }

    @Test
    public void testSaqueComValorNegativo() {
        account.saque(-500.0);
        assertEquals(1000.0, account.getSaldo(), 0.001);
    }

    @Test
    public void testSaqueComSaldoInsuficiente() {
        account.saque(1500.0);
        assertEquals(1000.0, account.getSaldo(), 0.001);
    }

}

