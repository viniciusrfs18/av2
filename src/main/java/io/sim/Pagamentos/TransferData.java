package io.sim.Pagamentos;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;

import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class TransferData {
    private String accountID;
    private String pagador;
    private String operacao;
    private String recebedor;
    private double valor;
    private Timestamp timestamp;

    public TransferData(String _pagador, String _operacao, String _recebedor, double _valor) {
        this.pagador = _pagador;
        this.operacao = _operacao;
        this.recebedor = _recebedor;
        this.valor = _valor;
        atualizaPlanilha();
    }

    public void setAccountID(String _accoutID) {
        accountID = _accoutID;
    }

    public String getAccountID() {
        return accountID;
    }

    public String getPagador() {
        return this.pagador;
    }

    public String getOperacao() {
        return this.operacao;
    }

    public String getRecebedor() {
        return this.recebedor;
    }

    public double getvalor() {
        return this.valor;
    }

    public void setTimestamp() {
        timestamp = new Timestamp(System.currentTimeMillis());
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getDescricao() {
        String descricao = "";
        if (operacao.equals("Pagamento")) {
            descricao = pagador + " transferiu R$" + valor + " para " + recebedor;
        } else if (operacao.equals("Recebimento")) {
            descricao = recebedor + " recebeu R$" + valor + " de " + pagador;
        }

        return descricao;
    }

    private void atualizaPlanilha(){
        
        String nomeDoArquivo = "transacoes.xlsx";

        try (FileInputStream inputStream = new FileInputStream(nomeDoArquivo);
             Workbook workbook = WorkbookFactory.create(inputStream);
             FileOutputStream outputStream = new FileOutputStream(nomeDoArquivo)) {
           
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet("All");    
        
        int lastRowNum = sheet.getLastRowNum();
        Row newRow = sheet.createRow(lastRowNum + 1);

            // Preencha as células da nova linha com os dados da classe TransferData
        newRow.createCell(0).setCellValue(getAccountID());
        newRow.createCell(1).setCellValue(getPagador());
        newRow.createCell(2).setCellValue(getOperacao());
        newRow.createCell(3).setCellValue(getRecebedor());
        newRow.createCell(4).setCellValue(getvalor());
        
        // Salve as alterações na planilha
        workbook.write(outputStream);

        System.out.println("Dados adicionados com sucesso!");
        
        } catch (IOException e) {
            e.printStackTrace();
        }
            
    }
}

