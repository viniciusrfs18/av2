package io.sim.simulator.bank;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *      A classe AlphaBank é responsável por criar e gerenciar threads de manipulação de contas, controlar as operações de 
 * transferência de fundos e manter registros de transações pendentes. Além disso, lida com a adição e remoção de contas 
 * e verificações de login.
 */
public class AlphaBank extends Thread {
    
    // Atributos de servidor 
    private ServerSocket serverAlphaBank;

    // Atributos da classe
    private static ArrayList<Account> accounts;
    private static ArrayList<TransferData> registrosPendentes;
    private int numClientes;

    // Atributo de sincronização
    private Object sincroniza;

    public AlphaBank(int numClientes, ServerSocket serverSocket) throws IOException {
        this.numClientes = numClientes;
        this.serverAlphaBank = serverSocket;
        accounts = new ArrayList<Account>();
        registrosPendentes = new ArrayList<TransferData>();
        this.sincroniza = new Object();
    }

    @Override
    public void run() {
        try {
            System.out.println("AlphaBank iniciado. Aguardando conexões...");
            AlphaBankAttExcel attExcel = new AlphaBankAttExcel(this);
            boolean primeiraVez = true;
            int i = 0;

            // Aguarda por conexões enquanto houver contas ou durante a primeira execução
            while ( i < numClientes || primeiraVez) {
                Socket clientSocket = serverAlphaBank.accept();
                i++;

                // Inicializa um manipulador de conta para lidar com a conexão
                AccountManipulator accountManipulator = new AccountManipulator(clientSocket, this);
                accountManipulator.start();

                // Inicializa a atualização das planilhas Excel apenas na primeira execução
                if (primeiraVez) {
                    attExcel.start();
                    primeiraVez = false;
                }
            }

            System.out.println("Todos as contas forma connectadas no Alpha Bank!");

            while (!accounts.isEmpty() || !registrosPendentes.isEmpty()) {
                Thread.sleep(1000);
            }

            System.out.println("Encerrando AlphaBank");
            attExcel.setFuncionado(false);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Método para adicionar uma conta bancária
    public static void adicionarAccount(Account conta) {
        synchronized (AlphaBank.class) {
            if (accounts != null) {
                accounts.add(conta);
            } else {
                System.out.println("Adição de conta mal sucedida: AlphaBank não foi iniciado ainda");
            }
        }
    }

    // Método para remover uma conta bancária com base no ID
    public void removerAccount(String accountID) {
        synchronized (AlphaBank.class) {
            for (int i = 0; i < accounts.size(); i++) {
                if (accounts.get(i).getAccountID().equals(accountID)) {
                    Account conta = accounts.remove(i);
                    conta.setFuncionando(false);
                }
            }
        }
    }

    // Método para realizar o login de um cliente
    public boolean fazerLogin(String[] login) {
        String accountID = login[0];
        String senha = login[1];

        for (Account account : accounts) {
            if (account.getAccountID().equals(accountID)) {
                if (account.getSenha().equals(senha)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Método para realizar uma transferência entre contas
    public boolean transferencia(String pagadorID, String recebedorID, double quantia) {
        Account pagador = getAccountPeloID(pagadorID);
        Account recebedor = getAccountPeloID(recebedorID);
        
        synchronized (sincroniza) {
            if (pagador != null && recebedor != null) {
                if (pagador.getSaldo() >= quantia) {
                    pagador.saque(quantia);
                    recebedor.deposito(quantia);
                    return true;
                } else {
                    System.out.println("AB - Problemas de transferência: " + pagador + " não tem saldo suficiente");
                }
            } else {
                System.out.println("AB - Problemas de transferência: ID do recebedor não encontrado");
            }
            return false;
        }
    }

    // Método para obter uma conta com base em seu ID
    public Account getAccountPeloID(String accountID) {
        for (Account account : accounts) {
            if (account.getAccountID().equals(accountID)) {
                return account;
            }
        }
        return null;
    }

    // Método para adicionar registros de transferência bancária na lista para atualização da planilha Excel
    public void adicionaRegistros(TransferData registerPag) {
        registerPag.setTimestamp();
        Account conta = getAccountPeloID(registerPag.getPagador());
        registerPag.setAccountID(conta.getAccountID());
        registerPag.setSaldoAtual(conta.getSaldo());
        registrosPendentes.add(registerPag);
        TransferData registerReceb = new TransferData(registerPag.getPagador(), "Recebimento", registerPag.getRecebedor(), registerPag.getQuantia());
        registerReceb.setTimestamp();
        conta = getAccountPeloID(registerPag.getRecebedor());
        registerReceb.setAccountID(conta.getAccountID());
        registerReceb.setSaldoAtual(conta.getSaldo());
        registrosPendentes.add(registerReceb);
    }

    // Método para verificar se existem registros pendentes
    public boolean temRegistro() {
        return !registrosPendentes.isEmpty();
    }

    // Método para obter e remover o próximo registro pendente
    public TransferData pegaRegistro() {
        return registrosPendentes.remove(0);
    }

    // Método para associar um registro ao histórico de uma conta
    public void mandaRegistroAcc(TransferData data) {
        for (Account account : accounts) {
            if (account.getAccountID().equals(data.getAccountID())) {
                account.addHistorico(data);
            }
        }
    }
}
