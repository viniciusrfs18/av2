package io.sim;

import java.util.ArrayList;
import java.util.Random;

public class Account extends Thread {
    
    private String accountID;
    private String password;
    private double balance;
    private ArrayList<TransferData> transactionHistory;

    private Object sync;

    public Account(String _accountID, double _balance) {
        this.accountID = _accountID;
        this.password = createPassword();
        this.balance = _balance;

        this.transactionHistory = new ArrayList<TransferData>();
        this.sync = new Object();
    }

    @Override
    public void run() {
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

    public String getAccountID() {
        return accountID;
    }

    public String getPassword() {
        return password;
    }

    public double getBalance() {
        synchronized (sync) {
            return balance;
        }
    }

    public void setBalance(double _balance) {
        this.balance = _balance;
    }

    public void deposit(double amount) {
        synchronized (sync) {
            if (amount > 0) {
                balance += amount;
            } else {
                System.out.println("O valor do depósito deve ser positivo.");
            }
        }
    }

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

    private String createPassword(){
        Random random = new Random();
        StringBuilder password = new StringBuilder();
    
        for (int i = 0; i < 6; i++) {
            int randomDigit = random.nextInt(10);
            password.append(randomDigit);
        }
    
        return password.toString();
    }
}