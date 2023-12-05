package io.sim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

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
    public void testgetPassword() {
        assertTrue(account.getPassword().matches("\\d{6}"));
    }

    @Test
    public void testgetBalance() {
        assertEquals(1000.0, account.getBalance(), 0.001);
    }

    @Test
    public void testSetBalance() {
        account.setBalance(1500.0);
        assertEquals(1500.0, account.getBalance(), 0.001);
    }

    @Test
    public void testdeposit() {
        account.deposit(500.0);
        assertEquals(1500.0, account.getBalance(), 0.001);
    }

    @Test
    public void testdepositComamountNegativo() {
        account.deposit(-500.0);
        assertEquals(1000.0, account.getBalance(), 0.001);
    }

    @Test
    public void testwithdraw() {
        account.withdraw(500.0);
        assertEquals(500.0, account.getBalance(), 0.001);
    }

    @Test
    public void testwithdrawComamountNegativo() {
        account.withdraw(-500.0);
        assertEquals(1000.0, account.getBalance(), 0.001);
    }

    @Test
    public void testwithdrawCombalanceInsuficiente() {
        account.withdraw(1500.0);
        assertEquals(1000.0, account.getBalance(), 0.001);
    }

}

