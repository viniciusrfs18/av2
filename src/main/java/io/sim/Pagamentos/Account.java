package io.sim.Pagamentos;

public class Account {
    private int identifier;
    private double balance;

    public Account(int identifier, double initialBalance) {
        this.identifier = identifier;
        this.balance = initialBalance;
    }

    public int getIdentifier() {
        return identifier;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        } else {
            System.out.println("O valor do depósito deve ser positivo.");
        }
    }

    public void withdraw(double amount) {
        if (amount > 0) {
            if (balance >= amount) {
                balance -= amount;
                //System.out.println(getBalance());
            } else {
                System.out.println("Saldo insuficiente para efetuar o saque.");
            }
        } else {
            System.out.println("O valor do saque deve ser positivo.");
        }
    }
}

