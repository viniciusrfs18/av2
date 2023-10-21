package io.sim.Pagamentos;

import java.util.ArrayList;
import java.util.Random;

public class Account extends Thread {
    
    // Atributos da classe
    private String accountID;         // Identificação da conta
    private String password;          // Senha da conta
    private double balance;           // Saldo da conta
    private ArrayList<TransferData> transactionHistory; // Histórico de transações da conta

    // Atributos de sincronização
    private Object sync;              // Objeto de sincronização para operações concorrentes

    // Construtor da classe Account
    public Account(String _accountID, double _balance) {
        
        this.accountID = _accountID;  // Inicializa o ID da conta
        this.password = createPassword();  // Cria e atribui uma senha aleatória
        this.balance = _balance;    // Inicializa o saldo da conta

        this.transactionHistory = new ArrayList<TransferData>();  // Inicializa o histórico de transações
        this.sync = new Object();    // Inicializa o objeto de sincronização
    }

    @Override
    public void run() {
        
        // Sempre que houver uma operação de retirada (withdraw) ou depósito (deposit),
        // cria um registro de transação e armazena-o no histórico.
        try {
            while (true) {
                if (AlphaBank.pendingRecords() != 0) {
                    Thread.sleep(500);
                    TransferData register = AlphaBank.getRecord(accountID);
                    if (register != null) {
                        transactionHistory.add(register);
                        System.out.println(register.getDescricao());
                    }
                }
            }
        } catch (InterruptedException e) {
                e.printStackTrace();
        }
    }

    // Método para obter o ID da conta
    public String getAccountID() {
        return accountID;
    }

    // Método para obter a senha da conta
    public String getPassword() {
        return password;
    }

    // Método para obter o saldo da conta
    public double getBalance() {
        synchronized (sync) {
            return balance;
        }
    }

    // Método para definir o saldo da conta
    public void setBalance(double _balance) {
        this.balance = _balance;
    }

    // Método para depositar dinheiro na conta
    public void deposit(double amount) {
        synchronized (sync) {
            if (amount > 0) {
                balance += amount;
            } else {
                System.out.println("O valor do depósito deve ser positivo.");
            }
        }
    }

    // Método para retirar dinheiro da conta
    public void withdraw(double amount) {
        synchronized (sync) {
            if (amount > 0) {
                if (balance >= amount) {
                    balance -= amount;
                } else {
                    System.out.println("Saldo insuficiente para efetuar o saque.");
                }
            } else {
                System.out.println("O valor do saque deve ser positivo.");
            }
        }
    }

    // Método para criar uma senha aleatória de 6 dígitos
    private String createPassword(){
        Random random = new Random();
        StringBuilder password = new StringBuilder();
    
        for (int i = 0; i < 6; i++) {
            int randomDigit = random.nextInt(10); // Gera um dígito aleatório de 0 a 9
            password.append(randomDigit);
        }
    
        return password.toString();
    }
}



