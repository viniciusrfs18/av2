package io.sim.Pagamentos;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AlphaBank extends Thread {
    
    private ServerSocket serverAlphaBank; //Socket do Servidor (AlphaBank)
    private static ArrayList<Account> accounts; //ArrayList com todas as contas criadas
    private static ArrayList<TransferData> registrosPendentes; 
    static int qtdClientes = 0;

    // Atributo de sincronização
    private Object sincroniza;

    public AlphaBank(ServerSocket serverSocket) throws IOException {

        this.serverAlphaBank = serverSocket;
        accounts = new ArrayList<Account>();
        registrosPendentes = new ArrayList<TransferData>();
        this.sincroniza = new Object();
        createTransfSheet();
    }

    @Override
    public void run() {
    
        try {

            System.out.println("AlphaBank iniciado. Aguardando conexões...");
            
            while (true) {

                Socket clientSocket = serverAlphaBank.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                AccountCommunication accountManipulator = new AccountCommunication(clientSocket, this);
                accountManipulator.start();
            
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    
    }

    public static void addAccount(Account conta) {

        synchronized (AlphaBank.class) {

            if (accounts != null) {
                accounts.add(conta);
            } else {
                System.out.println("AlphaBank não iniciado.");
            }
        
        }

    }

    public boolean conect(String[] login) {

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

    public boolean transferencia(String pagId, String recId, double valor) {
        Account pagador = searchAccount(pagId);
        Account recebedor = searchAccount(recId);
        
        synchronized (sincroniza) {

            if (pagador != null && recebedor != null) {
                if (pagador.getSaldo() >= valor) {

                    pagador.saque(valor);
                    System.out.println("PAGADOR - Saldo pós pagamento: " + pagador.getSaldo()); 
                    
                    recebedor.deposito(valor);
                    System.out.println("RECEBEDOR - Saldo pós pagamento: " + recebedor.getSaldo());
                    return true;
                
                } else {
                
                    System.out.println("AB - Problemas de transferencia: " + pagador + " nao tem saldo suficiente");
                
                }

            } else {
                
                System.out.println("AB - Problemas de transferencia: ID do recebedor");
            
            }
            return false;
        }

    }

    private static Account searchAccount(String accountID) {

        for (Account account : accounts) {
            if (account.getAccountID().equals(accountID)) {
                return account;
            }
        }
        return null;

    }

    public void adicionaRegistros(TransferData registerPag) {

        registerPag.setTimestamp();
        registerPag.setAccountID(registerPag.getPagador());
        registrosPendentes.add(registerPag);
        TransferData registerReceb = new TransferData(registerPag.getPagador(), "Recebimento", registerPag.getRecebedor(), registerPag.getvalor());
        registerReceb.setTimestamp();
        registerReceb.setAccountID(registerPag.getRecebedor());
        registrosPendentes.add(registerReceb);
    
    }

    public static int numeroDeRegistrosPend() {

        synchronized (AlphaBank.class) {
            if (registrosPendentes != null) {
                return registrosPendentes.size();
            }
            return 0;
        }

    }

    public static TransferData pegarRegistro(String accountID) {

        synchronized (AlphaBank.class) {
            if (registrosPendentes != null) {
                for (int i = 0; i < registrosPendentes.size(); i++) {
                    if (accountID.equals(registrosPendentes.get(i).getAccountID())) {
                        return registrosPendentes.remove(i);
                    }
                }
                System.out.println("Não há registros para essa conta");
            }
            return null;
        }

    }

    private void createTransfSheet(){
        
        String nomeDoArquivo = "transacoes.xlsx";

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream outputStream = new FileOutputStream(nomeDoArquivo)) {

            // Crie uma nova planilha (aba)
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("All");

            // Crie o cabeçalho na primeira linha
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Account ID");
            headerRow.createCell(1).setCellValue("Pagador");
            headerRow.createCell(2).setCellValue("Operacao");
            headerRow.createCell(3).setCellValue("Recebedor");
            headerRow.createCell(4).setCellValue("Valor");

            // Salve a planilha com o cabeçalho
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}