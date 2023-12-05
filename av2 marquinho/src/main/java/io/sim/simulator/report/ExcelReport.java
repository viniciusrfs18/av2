package io.sim.simulator.report;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import io.sim.simulator.bank.TransferData;
import io.sim.simulator.company.Company;
import io.sim.simulator.driver.Driver;
import io.sim.simulator.driver.DrivingData;
import io.sim.simulator.fuelStation.FuelStation;

/**
 *      A classe ExcelReport é reponsável por lidar com a criação e atualização de planilhas em formato Excel (.xlsx) 
 * para relatórios de carros e contas correntes
 */
public class ExcelReport {
    // Nomes dos arquivos de planilha
    private static final String fileNameDD = "RelatorioCarros.xlsx";
    private static final String fileNameTD = "RelatorioContasCorrente.xlsx";

    // Método para criar as planilhas
    public static void criaPlanilhas(Company company, ArrayList<Driver> drivers, FuelStation fuelStation) {
        criaPlanilhaCar(drivers); // Cria planilhas para carros
        criaPlanilhaAccount(company, drivers, fuelStation); // Cria planilhas para contas correntes
    }

    //                                             Método para criar planilhas de carros
    //--------------------------------------------------------------------------------------------------------------------------

    // Método para criar cabeçalhos em diversas abas de planilhas
    private static void criaPlanilhaCar(ArrayList<Driver> drivers) {
        try (Workbook workbook = new XSSFWorkbook()) {
            for (Driver driver : drivers) {
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet(driver.getCarID());
                criaCabecalhoCar(sheet); // Cria o cabeçalho da planilha
            }

            // Salva o arquivo Excel após criar todas as abas de planilha
            try (FileOutputStream outputStream = new FileOutputStream(fileNameDD)) {
                workbook.write(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para criar o cabeçalho da planilha de carros
    private static void criaCabecalhoCar(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        // Define os cabeçalhos das colunas
        headerRow.createCell(0).setCellValue("Timestamp");
        headerRow.createCell(1).setCellValue("ID Car");
        headerRow.createCell(2).setCellValue("ID Route");
        headerRow.createCell(3).setCellValue("Speed");
        headerRow.createCell(4).setCellValue("FuelConsumption");
        headerRow.createCell(5).setCellValue("FuelType");
        headerRow.createCell(6).setCellValue("CO2Emission");
        headerRow.createCell(7).setCellValue("Longitude (Lon)");
        headerRow.createCell(8).setCellValue("Latitude (Lat)");
        headerRow.createCell(9).setCellValue("Distance");
    }

    // Método para atualizar a planilha de carros com novos dados
    public static void atualizaPlanilhaCar(DrivingData carReport) {
        synchronized (ExcelReport.class) {
            try (FileInputStream inputStream = new FileInputStream(fileNameDD);
                Workbook workbook = WorkbookFactory.create(inputStream);
                FileOutputStream outputStream = new FileOutputStream(fileNameDD)) {
            
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(carReport.getCarID());    
                
                int lastRowNum = sheet.getLastRowNum();
                Row newRow = sheet.createRow(lastRowNum + 1);

                // Preenche as células da nova linha com os dados da classe DrivingData
                newRow.createCell(0).setCellValue(carReport.getTimeStamp());
                newRow.createCell(1).setCellValue(carReport.getCarID());
                newRow.createCell(2).setCellValue(carReport.getRouteIDSUMO());
                newRow.createCell(3).setCellValue(carReport.getSpeed());
                newRow.createCell(4).setCellValue(carReport.getFuelConsumption());
                newRow.createCell(5).setCellValue(carReport.getFuelType());
                newRow.createCell(6).setCellValue(carReport.getCo2Emission());
                newRow.createCell(7).setCellValue(carReport.getLonAtual());
                newRow.createCell(8).setCellValue(carReport.getLatAtual());
                newRow.createCell(9).setCellValue(carReport.getDistance()); 
                
                // Salva as alterações na planilha
                workbook.write(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }   
    }

    //                                             Método para criar contas correntes
    //--------------------------------------------------------------------------------------------------------------------------

    // Método para criar cabeçalhos em diversas abas de planilhas
    private static void criaPlanilhaAccount(Company company, ArrayList<Driver> drivers, FuelStation fuelStation){
        try (Workbook workbook = new XSSFWorkbook()) {
            // Cria a aba de planilha para Company
            org.apache.poi.ss.usermodel.Sheet sheet1 = workbook.createSheet(company.getAccountID());
            criaCabecalhoAccount(sheet1);

            // Cria a aba de planilha para Fuel Station
            org.apache.poi.ss.usermodel.Sheet sheet2 = workbook.createSheet(fuelStation.getFSAccountID());
            criaCabecalhoAccount(sheet2);
            
            for (Driver driver : drivers) {
                // Cria as abas de planilha para os Drivers
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet(driver.getID());
                criaCabecalhoAccount(sheet);
            }

            // Salva o arquivo Excel após criar todas as abas de planilha
            try (FileOutputStream outputStream = new FileOutputStream(fileNameTD)) {
                workbook.write(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para criar o cabeçalho da planilha de contas correntes
    private static void criaCabecalhoAccount(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        // Define os cabeçalhos das colunas
        headerRow.createCell(0).setCellValue("Account ID");
        headerRow.createCell(1).setCellValue("Pagador");
        headerRow.createCell(2).setCellValue("Operacao");
        headerRow.createCell(3).setCellValue("Recebedor");
        headerRow.createCell(4).setCellValue("Valor");
        headerRow.createCell(5).setCellValue("Timestamp");
        headerRow.createCell(6).setCellValue("Saldo Atual");
    }

    // Método para atualizar a planilha de contas correntes com novos dados
    public static void atualizaPlanilhaAccount(TransferData transferData) {
        synchronized (ExcelReport.class) {
            try (FileInputStream inputStream = new FileInputStream(fileNameTD);
                Workbook workbook = WorkbookFactory.create(inputStream);
                FileOutputStream outputStream = new FileOutputStream(fileNameTD)) {
            
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(transferData.getAccountID());    
                
                int lastRowNum = sheet.getLastRowNum();
                Row newRow = sheet.createRow(lastRowNum + 1);

                // Preenche as células da nova linha com os dados da classe TransferData
                newRow.createCell(0).setCellValue(transferData.getAccountID());
                newRow.createCell(1).setCellValue(transferData.getPagador());
                newRow.createCell(2).setCellValue(transferData.getOperacao());
                newRow.createCell(3).setCellValue(transferData.getRecebedor());
                newRow.createCell(4).setCellValue(transferData.getQuantia()); 
                newRow.createCell(5).setCellValue(transferData.getTimestamp());
                newRow.createCell(6).setCellValue(transferData.getSaldoAtual());
                
                // Salva as alterações na planilha
                workbook.write(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }      
    }
}
