package io.sim.simulator.bank;

import java.util.ArrayList;
import java.util.Random;

/**
 *      A classe Account representa uma conta-corrente bancária. Ela é executada como uma thread e permite depósitos, saques, 
 * verificações de saldo e histórico de transações. A conta é inicializada com uma identificação, senha e saldo.
 */
public class Account extends Thread {
    private String accountID; // Identificação da conta
    private String senha; // Senha da conta
    private double saldo; // Saldo da conta
    private ArrayList<TransferData> historico; // Histórico de transações
    private boolean funcionando; // Indica se a conta está funcionando

    // Atributos de sincronização
    private Object sincroniza; // Objeto para sincronização de threads

    public Account(String _accountID, String _senha, double _saldo) {
        this.accountID = _accountID; // Inicializa a identificação da conta
        this.senha = _senha; // Inicializa a senha da conta
        this.saldo = _saldo; // Inicializa o saldo da conta
        this.historico = new ArrayList<TransferData>(); // Inicializa o histórico de transações
        this.funcionando = true; // Inicializa a conta como funcionando
        this.sincroniza = new Object(); // Inicializa o objeto de sincronização
    }

    @Override
    public void run() {
        try {
            System.out.println("Account: " + accountID + " iniciando...");
            int tamHistoricoAnt = historico.size();
            while (funcionando) {
                Thread.sleep(2000); // Espera por 2 segundos
                if (historico.size() > tamHistoricoAnt) {
                    System.out.println(historico.get(tamHistoricoAnt).getDescricao()); // Imprime a descrição de transação do histórico
                    tamHistoricoAnt = historico.size();
                }
            }
            System.out.println("Account: " + accountID + " FINALIZADA...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Retorna a identificação da conta
    public String getAccountID() {
        return accountID;
    }

    // Retorna a senha da conta
    public String getSenha() {
        return senha;
    }

    // Retorna o saldo da conta
    public double getSaldo() {
        return saldo;
    }

    // Define o saldo da conta
    public void setBalance(double _saldo) {
        this.saldo = _saldo; 
    }

    // Adiciona uma transação ao histórico
    public void addHistorico(TransferData data) {
        historico.add(data);
    }

    public ArrayList<TransferData> getHistorico() {
        return historico;
    }

    // Define se a conta está funcionando
    public void setFuncionando(boolean _funcionando) {
        funcionando = _funcionando;
    }

    // Realiza um depósito, aumentando o saldo
    public void deposito(double quantia) {
        synchronized (sincroniza) {
            if (quantia > 0) {
                saldo += quantia; 
            } else {
                System.out.println("O valor do depósito deve ser positivo.");
            }
        }
    }

    // Realiza um saque, diminuindo o saldo
    public void saque(double quantia) {
        synchronized (sincroniza) {
            if (quantia > 0) {
                if (saldo >= quantia) {
                    saldo -= quantia; 
                } else {
                    System.out.println("Saldo insuficiente para efetuar o saque.");
                }
            } else {
                System.out.println("O valor do saque deve ser positivo.");
            }
        }
    }

    // Cria uma nova conta com identificação, senha aleatória e saldo iniciais
    public static Account criaAccount(String accountID, long saldo) {
        // Gera uma senha de 6 dígitos aleatória
        String numerosPermitidos = "0123456789"; // Define os números permitidos na senha
        Random random = new Random(); // Cria um objeto Random para gerar números aleatórios
        StringBuilder sb = new StringBuilder(6); // Inicializa um StringBuilder para construir a senha
    
        // Loop para gerar a senha de 6 dígitos
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(numerosPermitidos.length()); // Gera um índice aleatório
            char randomNumber = numerosPermitidos.charAt(index); // Obtém um número aleatório
            sb.append(randomNumber); // Adiciona o número ao StringBuilder
        }
        
        String senha = sb.toString(); // Converte o StringBuilder em uma String, obtendo a senha gerada
    
        Account account = new Account(accountID, senha, saldo);
        return account;
    }
    
}
