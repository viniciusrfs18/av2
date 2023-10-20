package io.sim.MobilityCompany;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import io.sim.Transport.CarDriver.DrivingData;

public class atualizaSheet extends Thread{
    private Company company;

    public atualizaSheet(Company _company) {
        this.company = _company;
    }

    @Override
    public void run() {
        try {
            while (Company.routesAvaliable()) {
                Thread.sleep(10);
                if (!company.temReport()) {
                    atualizaPlanilhaCar(company.pegaComunicacao());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void atualizaPlanilhaCar(DrivingData data){
        
        String nomeDoArquivo = "carData.xlsx";

        try (FileInputStream inputStream = new FileInputStream(nomeDoArquivo);
        Workbook workbook = WorkbookFactory.create(inputStream);
        FileOutputStream outputStream = new FileOutputStream(nomeDoArquivo)) {
            
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(data.getCarID());    
            
        int lastRowNum = sheet.getLastRowNum();
        Row newRow = sheet.createRow(lastRowNum + 1);

                // Preencha as células da nova linha com os dados da classe TransferData
        newRow.createCell(0).setCellValue(data.getTimeStamp());
        newRow.createCell(1).setCellValue(data.getCarID());
        newRow.createCell(2).setCellValue(data.getRouteIDSUMO());
        newRow.createCell(3).setCellValue(data.getSpeed());
        newRow.createCell(4).setCellValue(data.getOdometer()); 
        newRow.createCell(5).setCellValue(data.getFuelConsumption());
        newRow.createCell(6).setCellValue(data.getFuelType());
        newRow.createCell(7).setCellValue(data.getCo2Emission());
        newRow.createCell(8).setCellValue(data.getLonAtual());
        newRow.createCell(9).setCellValue(data.getLatAtual());
            
        // Salve as alterações na planilha
        workbook.write(outputStream);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

}
