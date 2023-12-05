package io.sim;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import org.apache.poi.ss.usermodel.*;

public class Auxilio {
    private String accountID;   // ID da conta associada à transferência
    private String payer;       // Pagador
    private String operation;   // Tipo de operação (Pagamento ou Recebimento)
    private String receiver;    // Recebedor
    private double amount;      // Valor da transferência
    private Timestamp timestamp; // Timestamp para registrar o momento da transferência

    public Auxilio(String _payer, String _operation, String _receiver, double _amount) {
        this.payer = _payer;       // Inicializa o pagador
        this.operation = _operation; // Inicializa o tipo de operação
        this.receiver = _receiver;   // Inicializa o recebedor
        this.amount = _amount;       // Inicializa o valor da transferência
        updateSheet();               // Chama o método para atualizar a planilha Excel
    }

    public void setAccountID(String _accoutID) {
        accountID = _accoutID; // Define o ID da conta associada à transferência
    }

    public String getAccountID() {
        return accountID; // Obtém o ID da conta associada à transferência
    }

    public String getpayer() {
        return this.payer; // Obtém o pagador
    }

    public String getoperation() {
        return this.operation; // Obtém o tipo de operação
    }

    public String getreceiver() {
        return this.receiver; // Obtém o recebedor
    }

    public double getamount() {
        return this.amount; // Obtém o valor da transferência
    }

    public void setTimestamp() {
        timestamp = new Timestamp(System.currentTimeMillis()); // Define o timestamp como o momento atual
    }

    public Timestamp getTimestamp() {
        return timestamp; // Obtém o timestamp
    }

    public String getDescricao() {
        String descricao = "";
        if (operation.equals("Pagamento")) {
            descricao = payer + " transferiu R$" + amount + " para " + receiver; // Descrição para operação de pagamento
        } else if (operation.equals("Recebimento")) {
            descricao = receiver + " recebeu R$" + amount + " de " + payer; // Descrição para operação de recebimento
        }

        return descricao; // Retorna a descrição da transferência
    }

    private void updateSheet() {
        
        String nomeDoArquivo = "transacoes.xlsx";

        try (FileInputStream inputStream = new FileInputStream(nomeDoArquivo);
             Workbook workbook = WorkbookFactory.create(inputStream);
             FileOutputStream outputStream = new FileOutputStream(nomeDoArquivo)) {
           
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet("All");    
        
        int lastRowNum = sheet.getLastRowNum();
        Row newRow = sheet.createRow(lastRowNum + 1);

       
        newRow.createCell(0).setCellValue(getAccountID());
        newRow.createCell(1).setCellValue(getpayer());
        newRow.createCell(2).setCellValue(getoperation());
        newRow.createCell(3).setCellValue(getreceiver());
        newRow.createCell(4).setCellValue(getamount());
        
        workbook.write(outputStream);
        
        } catch (IOException e) {
            e.printStackTrace();
        }
            
    }
}
