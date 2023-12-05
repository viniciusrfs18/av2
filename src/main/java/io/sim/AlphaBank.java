package io.sim;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AlphaBank extends Thread {
    
    private ServerSocket serverAlphaBank; 
    private static ArrayList<Account> accounts; 
    private static ArrayList<Auxilio> records; 

    
    private Object sync;

    public AlphaBank(ServerSocket serverSocket) throws IOException {

        this.serverAlphaBank = serverSocket;
        accounts = new ArrayList<Account>();
        records = new ArrayList<Auxilio>();
        this.sync = new Object();
        createTransfSheet();
    }

    @Override
    public void run() {
    
        try {

            System.out.println("AlphaBank iniciado. Aguardando conexões...");
            
            while (true) {

                Socket clientSocket = serverAlphaBank.accept();

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
        String password = login[1];

        for (Account account : accounts) {
            if (account.getAccountID().equals(accountID)) {
                if (account.getPassword().equals(password)) {
                    return true;
                }
            }
        }

        return false;

    }

   
    public boolean transferencia(String pagId, String recId, double amount) {
        Account payer = searchAccount(pagId);
        Account receiver = searchAccount(recId);
        
        synchronized (sync) {

            if (payer != null && receiver != null) {
                if (payer.getBalance() >= amount) {

                    payer.withdraw(amount); 
                    receiver.deposit(amount);
                    return true;
                
                } else {
                
                    System.out.println(payer );
                
                }

            } else {
                
                System.out.println("NULO");
            
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
    public void addRecords(Auxilio registerPag) {

        registerPag.setTimestamp();
        registerPag.setAccountID(registerPag.getpayer());
        records.add(registerPag);

        Auxilio registerReceb = new Auxilio(registerPag.getpayer(), "Recebimento", registerPag.getreceiver(), registerPag.getamount());
        registerReceb.setTimestamp();
        registerReceb.setAccountID(registerPag.getreceiver());
        records.add(registerReceb);
    }
    public static int pendingRecords() {

        synchronized (AlphaBank.class) {
            if (records != null) {
                return records.size();
            }
            return 0;
        }

    }
    public static Auxilio getRecord(String accountID) {

        synchronized (AlphaBank.class) {
            if (records != null) {
                for (int i = 0; i < records.size(); i++) {
                    if (accountID.equals(records.get(i).getAccountID())) {
                        return records.remove(i);
                    }
                }
            }
            return null;
        }

    }
    private void createTransfSheet(){
        
        String nomeDoArquivo = "transacoes.xlsx";

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream outputStream = new FileOutputStream(nomeDoArquivo)) {

    
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("All");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Account ID");
            headerRow.createCell(1).setCellValue("payer");
            headerRow.createCell(2).setCellValue("operation");
            headerRow.createCell(3).setCellValue("receiver");
            headerRow.createCell(4).setCellValue("amount");
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
