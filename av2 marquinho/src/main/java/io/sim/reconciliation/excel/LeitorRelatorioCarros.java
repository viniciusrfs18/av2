package io.sim.reconciliation.excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class LeitorRelatorioCarros {

    public LeitorRelatorioCarros() {

    }

    public static void lerExcel(String filePath, List<Double> timestamps, List<Double> distances) throws IOException {
        FileInputStream excelFile = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(excelFile);

        // Considerando que os dados estão na primeira planilha (Sheet)
        Sheet sheet = workbook.getSheetAt(0);

        // Iterar sobre as linhas da planilha, começando da segunda linha (índice 1)
        for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);

            // Obter o valor da coluna de Timestamp (assumindo que é a primeira coluna)
            Cell timestampCell = row.getCell(0);
            if (timestampCell != null) {
                timestamps.add(Double.parseDouble(timestampCell.toString()));
            }

            // Obter o valor da coluna de Distance (assumindo que é a última coluna)
            Cell distanceCell = row.getCell(row.getLastCellNum() - 1);
            if (distanceCell != null) {
                distances.add(Double.parseDouble(distanceCell.toString()));
            }
        }

        // Fechar o workbook para liberar recursos
        workbook.close();
    }
}
