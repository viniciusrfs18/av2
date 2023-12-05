package io.sim;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import io.sim.simulator.bank.Account;
import io.sim.simulator.bank.TransferData;

public class AccountTest {
    private Account account;

    // Cria a conta para a realização de testes
    @Before
    public void setUp() {
        // Iniciando uma instância de Account
        account = new Account("TestAccount", "password", 1000.0);
    }

    // Teste para verificar se um depósito é realizado com sucesso
    @Test
    public void testDeposito() {
        account.deposito(500.0);
        assertEquals(1500.0, account.getSaldo(), 0.001);
    }

    // Teste para verificar se um saque é realizado com sucesso
    @Test
    public void testSaque() {
        account.saque(500.0);
        assertEquals(500.0, account.getSaldo(), 0.001);
    }

    // Teste para verificar se um registro é adicionado ao histórico com sucesso
    @Test
    public void testAddHistorico() {
        TransferData data = new TransferData("PagadorAccount", "Transferência", "RecebedorAccount", 200.0);
        account.addHistorico(data);
        assertTrue(account.getHistorico().contains(data));
    }

    // Teste para verificar se uma nova conta é criada com sucesso
    @Test
    public void testCriaAccount() {
        Account newAccount = Account.criaAccount("NovaAccount", 1000);
        assertNotNull(newAccount);
        assertEquals("NovaAccount", newAccount.getAccountID());
        assertEquals(1000.0, newAccount.getSaldo(), 0.001);
    }

}

