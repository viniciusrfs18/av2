package io.sim;

import static org.junit.Assert.*;
import org.junit.Test;

import io.sim.simulator.bank.TransferData;
import io.sim.simulator.company.Rota;
import io.sim.simulator.comunication.JSONConverter;
import io.sim.simulator.driver.DrivingData;

import org.json.JSONObject;

public class JSONConverterTest {
    
    // Testa a criação de JSON para representar o tamanho em bytes
    @Test
    public void testCriaJSONTamanhoBytes() {
        int numBytes = 1024;
        String json = JSONConverter.criaJSONTamanhoBytes(numBytes);
        JSONObject jsonObject = new JSONObject(json);
        int extractedBytes = jsonObject.getInt("Num Bytes");
        assertEquals(numBytes, extractedBytes);
    }

    // Testa a extração do tamanho em bytes a partir de um JSON
    @Test
    public void testExtraiTamanhoBytes() {
        int numBytes = 1024;
        String json = JSONConverter.criaJSONTamanhoBytes(numBytes);
        int extractedBytes = JSONConverter.extraiTamanhoBytes(json);
        assertEquals(numBytes, extractedBytes);
    }

    // Testa a criação de JSON para representar informações de transferência
    @Test
    public void testCriaJSONTransferData() {
        TransferData transferData = new TransferData("Pagador1", "Operacao1", "Recebedor1", 100.0);
        String json = JSONConverter.criaJSONTransferData(transferData);
        JSONObject jsonObject = new JSONObject(json);

        assertEquals("Pagador1", jsonObject.getString("ID do Pagador"));
        assertEquals("Operacao1", jsonObject.getString("Operacao"));
        assertEquals("Recebedor1", jsonObject.getString("ID do Recebedor"));
        assertEquals(100.0, jsonObject.getDouble("Quantia"), 0.001);
    }

     // Testa a extração de informações de transferência a partir de um JSON
    @Test
    public void testExtraiTransferData() {
        String json = "{\"ID do Pagador\":\"Pagador1\",\"Operacao\":\"Operacao1\",\"ID do Recebedor\":\"Recebedor1\",\"Quantia\":100.0}";
        TransferData extractedTransferData = JSONConverter.extraiTransferData(json);

        assertEquals("Pagador1", extractedTransferData.getPagador());
        assertEquals("Operacao1", extractedTransferData.getOperacao());
        assertEquals("Recebedor1", extractedTransferData.getRecebedor());
        assertEquals(100.0, extractedTransferData.getQuantia(), 0.001);
    }

    // Testa a criação de JSON para representar dados de direção
    @Test
    public void testCriaJSONDrivingData() {
        DrivingData drivingData = new DrivingData("Car1", "Driver1", "Status1", 1.0, 2.0, 3.0, 4.0, 1, 12345, "Route1", 60.0, 100.0, 5.0, 1, 2.0);
        String json = JSONConverter.criarJSONDrivingData(drivingData);
        JSONObject jsonObject = new JSONObject(json);

        assertEquals("Car1", jsonObject.getString("Car ID"));
        assertEquals("Driver1", jsonObject.getString("Driver ID"));
        assertEquals("Status1", jsonObject.getString("Car Status"));
        assertEquals(1.0, jsonObject.getDouble("Latitude Anterior"), 0.001);
        assertEquals(2.0, jsonObject.getDouble("Longitude Anterior"), 0.001);
        assertEquals(3.0, jsonObject.getDouble("Latitude Atual"), 0.001);
        assertEquals(4.0, jsonObject.getDouble("Longitude Atual"), 0.001);
        assertEquals(1, jsonObject.getInt("Precisa Att Excel"));
        assertEquals(12345, jsonObject.getLong("TimeStamp"));
        assertEquals("Route1", jsonObject.getString("RouteIDSUMO"));
        assertEquals(60.0, jsonObject.getDouble("Speed"), 0.001);
        assertEquals(100.0, jsonObject.getDouble("Distance"), 0.001);
        assertEquals(5.0, jsonObject.getDouble("FuelConsumption"), 0.001);
        assertEquals(1, jsonObject.getInt("FuelType"));
        assertEquals(2.0, jsonObject.getDouble("Co2Emission"), 0.001);
    }

    // Testa a extração de dados de direção a partir de um JSON
    @Test
    public void testExtraiDrivingData() {
        String json = "{\"Car ID\":\"Car1\",\"Driver ID\":\"Driver1\",\"Car Status\":\"Status1\",\"Latitude Anterior\":1.0,\"Longitude Anterior\":2.0,\"Latitude Atual\":3.0,\"Longitude Atual\":4.0,\"Precisa Att Excel\":1,\"TimeStamp\":12345,\"RouteIDSUMO\":\"Route1\",\"Speed\":60.0,\"Distance\":100.0,\"FuelConsumption\":5.0,\"FuelType\":1,\"Co2Emission\":2.0}";
        DrivingData extractedDrivingData = JSONConverter.extraiDrivingData(json);

        assertEquals("Car1", extractedDrivingData.getCarID());
        assertEquals("Driver1", extractedDrivingData.getDriverID());
        assertEquals("Status1", extractedDrivingData.getCarStatus());
        assertEquals(1.0, extractedDrivingData.getLatAnt(), 0.001);
        assertEquals(2.0, extractedDrivingData.getLonAnt(), 0.001);
        assertEquals(3.0, extractedDrivingData.getLatAtual(), 0.001);
        assertEquals(4.0, extractedDrivingData.getLonAtual(), 0.001);
        assertEquals(1, extractedDrivingData.getPrecisaAttExcel(), 0.001);
        assertEquals(12345, extractedDrivingData.getTimeStamp());
        assertEquals("Route1", extractedDrivingData.getRouteIDSUMO());
        assertEquals(60.0, extractedDrivingData.getSpeed(), 0.001);
        assertEquals(100.0, extractedDrivingData.getDistance(), 0.001);
        assertEquals(5.0, extractedDrivingData.getFuelConsumption(), 0.001);
        assertEquals(1, extractedDrivingData.getFuelType());
        assertEquals(2.0, extractedDrivingData.getCo2Emission(), 0.001);
    }

    // Testa a criação de JSON para representar informações de rota
    @Test
    public void testCriaJSONRota() {
        Rota rota = new Rota("Route1", "Edge1,Edge2,Edge3");
        String json = JSONConverter.criaJSONRota(rota);
        JSONObject jsonObject = new JSONObject(json);

        assertEquals("Route1", jsonObject.getString("ID da Rota"));
        assertEquals("Edge1,Edge2,Edge3", jsonObject.getString("Edges"));
    }

    // Testa a extração de informações de rota a partir de um JSON
    @Test
    public void testExtraiRota() {
        String json = "{\"ID da Rota\":\"Route1\",\"Edges\":\"Edge1,Edge2,Edge3\"}";
        Rota extractedRota = JSONConverter.extraiRota(json);

        assertEquals("Route1", extractedRota.getID());
        assertEquals("Edge1,Edge2,Edge3", extractedRota.getEdges());
    }
}
