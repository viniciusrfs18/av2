package io.sim.reconciliation.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ReconciliationReport {

    private static String fileName = "ReconciliationReport.xlsx";

    public ReconciliationReport() {

    }

    // Método para criar a planilha com os cabeçalhos "t0, d0, t1, d1, t2, d2"
    public static void criaReconciliationReport(int numeroParticoes) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("TimeDistanceReport");

            // Cria o cabeçalho da planilha
            criaCabecalhoTimeDistance(sheet, numeroParticoes);

            // Salva o arquivo Excel
            try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
                workbook.write(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para criar o cabeçalho da planilha "t0, d0, t1, d1, t2, d2"
    private static void criaCabecalhoTimeDistance(Sheet sheet, int numeroParticoes) {
        Row headerRow = sheet.createRow(0);

        // Define os cabeçalhos das colunas
        for (int i = 0; i < (numeroParticoes - 1); i++) {
            headerRow.createCell(i * 2).setCellValue("t" + (i + 1));
            headerRow.createCell(i * 2 + 1).setCellValue("d" + (i + 1));
        }

        headerRow.createCell((numeroParticoes - 1)*2).setCellValue("tTOTAL");
        headerRow.createCell((numeroParticoes - 1)*2 + 1).setCellValue("dTOTAL");
    }


    // Adiciona os pares de valores de dois ArrayLists a uma linha específica
    public static void adicionaValoresALinhaTimeDistance(int linha, ArrayList<Double> temposParciais, ArrayList<Double> distanciasParciais) {
        try (Workbook workbook = WorkbookFactory.create(new FileInputStream(fileName))) {
            // Adiciona os pares de valores à linha especificada na primeira sheet
            Sheet sheet = workbook.getSheetAt(0); // Obtém a primeira sheet
            Row row = sheet.getRow(linha); // Obtém a linha específica

            if (row == null) {
                row = sheet.createRow(linha);
            }

            int colNum = 0; // Começar da primeira coluna

            // Itera sobre os pares de valores de temposParciais e distanciasParciais
            for (int i = 0; i < temposParciais.size(); i++) {
                double tempoParcial = temposParciais.get(i);
                double distanciaParcial = distanciasParciais.get(i);

                // Adiciona o valor de temposParciais
                Cell cellTempo = row.createCell(colNum);
                cellTempo.setCellValue(tempoParcial);
                colNum++;

                // Adiciona o valor de distanciasParciais
                Cell cellDistancia = row.createCell(colNum);
                cellDistancia.setCellValue(distanciaParcial);
                colNum++;
            }

            // Salva as alterações na planilha
            try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
                workbook.write(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Adiciona uma nova sheet com o cabeçalho especificado
    public static void adicionaSheetEstatisticas(int numeroParticoes) {
        try (Workbook workbook = WorkbookFactory.create(new FileInputStream(fileName))) {
            Sheet sheet = workbook.createSheet("Statistics"); // Cria uma nova sheet

            // Cria o cabeçalho da planilha de estatísticas
            criaCabecalhoEstatisticas(sheet, numeroParticoes);

            // Salva as alterações na planilha
            try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
                workbook.write(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Cria o cabeçalho da planilha de estatísticas
    private static void criaCabecalhoEstatisticas(Sheet sheet, int numeroParticoes) {
        Row headerRow = sheet.createRow(0);

        // Define os cabeçalhos das colunas
        headerRow.createCell(0).setCellValue("Tempos");
        headerRow.createCell(1).setCellValue("Médias");
        headerRow.createCell(2).setCellValue("Desvio Padrão");
        headerRow.createCell(3).setCellValue("Reconciliado");
        headerRow.createCell(4).setCellValue("Polarização (bias)");
        headerRow.createCell(5).setCellValue("Precisão");
        headerRow.createCell(6).setCellValue("Incerteza");

        headerRow.createCell(9).setCellValue("Distâncias");
        headerRow.createCell(10).setCellValue("Médias");
        headerRow.createCell(11).setCellValue("Desvio Padrão");
        headerRow.createCell(12).setCellValue("Reconciliado");
        headerRow.createCell(13).setCellValue("Polarização (bias)");
        headerRow.createCell(14).setCellValue("Precisão");
        headerRow.createCell(15).setCellValue("Incerteza");

        for (int i = 1; i <= (numeroParticoes - 1); i++) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue("t" + i);
            row.createCell(9).setCellValue("d" + i);
        }
        Row row = sheet.createRow(numeroParticoes);
        row.createCell(0).setCellValue("tTOTAL");
        row.createCell(9).setCellValue("dTOTAL");
    }

    // Adiciona uma nova sheet com o cabeçalho especificado
    public static void adicionaSheetVelocidade(int numeroParticoes) {
        try (Workbook workbook = WorkbookFactory.create(new FileInputStream(fileName))) {
            Sheet sheet = workbook.createSheet("Speeds"); // Cria uma nova sheet

            // Cria o cabeçalho da planilha de estatísticas
            criaCabecalhoSpeed(sheet, numeroParticoes);

            // Salva as alterações na planilha
            try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
                workbook.write(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Cria o cabeçalho da planilha de estatísticas
    private static void criaCabecalhoSpeed(Sheet sheet, int numeroParticoes) {
        Row headerRow = sheet.createRow(0);

        // Define os cabeçalhos das colunas
        headerRow.createCell(0).setCellValue("Parciais");
        headerRow.createCell(1).setCellValue("Velocidade");
        headerRow.createCell(2).setCellValue("Incerteza");
        headerRow.createCell(3).setCellValue("Unidade de medida");

        headerRow.createCell(6).setCellValue("Parciais");
        headerRow.createCell(7).setCellValue("Velocidade");
        headerRow.createCell(8).setCellValue("Incerteza");
        headerRow.createCell(9).setCellValue("Unidade de medida");

        for (int i = 1; i <= (numeroParticoes - 1); i++) {
            Row row = sheet.createRow(i);
            String texto = "v" + i;
            row.createCell(0).setCellValue(texto);
            row.createCell(3).setCellValue("m/s");
            row.createCell(6).setCellValue(texto);
            row.createCell(9).setCellValue("Km/h");
        }
        Row row = sheet.createRow(numeroParticoes);
        row.createCell(0).setCellValue("vTOTAL");
        row.createCell(3).setCellValue("m/s");
        row.createCell(6).setCellValue("vTOTAL");
        row.createCell(9).setCellValue("Km/h");
    }

    // Lê os dados de uma coluna específica e retorna como ArrayList<Double>
    public static ArrayList<Double> lerColunaReconciliation(int numSheet, int coluna) {
        ArrayList<Double> valores = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(new FileInputStream(fileName))) {
            Sheet sheet = workbook.getSheetAt(numSheet); // Obtém a primeira sheet

            // Itera sobre as linhas da sheet, começando da segunda linha (índice 1)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(coluna);
                    if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                        double valor = cell.getNumericCellValue();
                        valores.add(valor);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return valores;
    }

    public static void escreverDadosColunaReconciliation(int numSheet,int coluna, ArrayList<Double> valores) {
        try (Workbook workbook = WorkbookFactory.create(new FileInputStream(fileName))) {
            Sheet sheet = workbook.getSheetAt(numSheet);

            // Itera sobre os valores e adiciona à coluna específica
            for (int i = 0; i < valores.size(); i++) {
                Row row = sheet.getRow(i + 1); // Começa da segunda linha (índice 1)
                if (row == null) {
                    row = sheet.createRow(i + 1);
                }

                Cell cell = row.createCell(coluna);
                cell.setCellValue(valores.get(i));
            }

            // Salva as alterações na planilha
            try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
                workbook.write(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

