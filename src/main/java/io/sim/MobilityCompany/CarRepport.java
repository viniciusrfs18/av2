package io.sim.MobilityCompany;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.json.JSONObject;

import io.sim.Transport.CarDriver.DrivingData;
import io.sim.Transport.Rotas.Rota;

public class CarRepport extends Thread {
    private Socket carSocket;       // Socket para comunicação com o carro
    private DataInputStream input;  // Stream de entrada para receber dados do carro
    private DataOutputStream output; // Stream de saída para enviar dados ao carro
    private Company company;        // Instância da empresa de mobilidade

    // Atributo para sincronização
    private Object sync = new Object();

    public CarRepport(Socket _carSocket, Company _company) {
        this.company = _company;
        this.carSocket = _carSocket;
        try {
            // Variáveis de input e output do servidor
            input = new DataInputStream(carSocket.getInputStream());
            output = new DataOutputStream(carSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.sync = new Object();
    }

    @Override
    public void run() {
        try {
            String status = "";
            double distance = 0;

            while (!status.equals("encerrado")) {

                DrivingData com = extractDrivingData(input.readUTF());
                status = com.getCarStatus(); // Lê solicitação do cliente

                company.sendCommunication(com);

                double latInicial = com.getLatInicial();
                double lonInicial = com.getLonInicial();
                double latAtual = com.getLatAtual();
                double lonAtual = com.getLonAtual();

                double currentDistance = calculateDistance(latInicial, lonInicial, latAtual, lonAtual);

                if (currentDistance > (distance + 1000)) {
                    distance += 1000;
                    String driverID = com.getDriverID();
                    company.oneKmPay(driverID);
                }

                if (status.equals("aguardando")) {

                    if (!Company.routesAvaliable()) {
                        System.out.println("Sem mais rotas disponíveis");
                        Rota rota = new Rota("-1", "00000");
                        output.writeUTF(routeJSON(rota));
                        break;
                    }

                    if (Company.routesAvaliable()) {
                        synchronized (sync) {
                            Rota response = company.execRoute();
                            output.writeUTF(routeJSON(response));
                        }
                    }

                } else if (status.equals("finalizado")) {

                    String routeID = com.getRouteIDSUMO();
                    company.endRoute(routeID);
                    distance = 0;

                } else if (status.equals("rodando")) {
                } else if (status.equals("encerrado")) {
                    break;
                }
            }

            System.out.println("Encerrando canal.");
            input.close();
            output.close();
            carSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método responsável por calcular a distância percorrida pelo Carro com base nas latitudes.
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double raioTerra = 6371000;

        // Diferenças das latitudes e longitudes
        double latDif = Math.toRadians(lat2 - lat1);
        double lonDif = Math.toRadians(lon2 - lon1);

        // Fórmula de Haversine
        double a = Math.sin(latDif / 2) * Math.sin(latDif / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDif / 2) * Math.sin(lonDif / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double currentDistance = raioTerra * c;

        return currentDistance;
    }

    // Converte uma instância de Rota em um objeto JSON e retorna uma representação em string
    private String routeJSON(Rota rota) {
        JSONObject rotaJSON = new JSONObject();
        rotaJSON.put("ID da Rota", rota.getID());
        rotaJSON.put("Edges", rota.getEdges());
        return rotaJSON.toString();
    }

    // Extrai dados de condução a partir de uma representação JSON e retorna um objeto de DrivingData
    private DrivingData extractDrivingData(String drivingDataJSON) {
        JSONObject drivingDataJSONObj = new JSONObject(drivingDataJSON);
        String carID = drivingDataJSONObj.getString("Car ID");
        String driverID = drivingDataJSONObj.getString("Driver ID");
        String carStatus = drivingDataJSONObj.getString("Car Status");
        double latInicial = drivingDataJSONObj.getDouble("Latitude Inicial");
        double lonInicial = drivingDataJSONObj.getDouble("Longitude Inicial");
        double latAtual = drivingDataJSONObj.getDouble("Latitude Atual");
        double lonAtual = drivingDataJSONObj.getDouble("Longitude Atual");
        long timeStamp = drivingDataJSONObj.getLong("TimeStamp");
        double X_Position = drivingDataJSONObj.getDouble("X_Position");
        double Y_Position = drivingDataJSONObj.getDouble("Y_Position");
        String roadIDSUMO = drivingDataJSONObj.getString("RoadIDSUMO");
        String routeIDSUMO = drivingDataJSONObj.getString("RouteIDSUMO");
        double speed = drivingDataJSONObj.getDouble("Speed");
        double odometer = drivingDataJSONObj.getDouble("Odometer");
        double fuelConsumption = drivingDataJSONObj.getDouble("FuelConsumption");
        double averageFuelConsumption = drivingDataJSONObj.getDouble("AverageFuelConsumption");
        int fuelType = drivingDataJSONObj.getInt("FuelType");
        double fuelPrice = drivingDataJSONObj.getDouble("FuelPrice");
        double Co2Emission = drivingDataJSONObj.getDouble("Co2Emission");
        double HCEmission = drivingDataJSONObj.getDouble("HCEmission");
        int personCapacity = drivingDataJSONObj
.getInt("PersonCapacity");
        int personNumber = drivingDataJSONObj.getInt("PersonNumber");

        DrivingData drivingData = new DrivingData(carID, driverID, carStatus, latInicial, lonInicial, latAtual, lonAtual, timeStamp, 
        X_Position, Y_Position, roadIDSUMO, routeIDSUMO, speed, odometer, fuelConsumption, averageFuelConsumption, 
        fuelType, fuelPrice, Co2Emission, HCEmission, personCapacity, personNumber);

		return drivingData;
	}

}