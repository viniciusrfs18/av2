package io.sim.MobilityCompany;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.JSONObject;

import io.sim.Transport.CarDriver.DrivingData;
import io.sim.Transport.Rotas.Rota;

public class CarRepport extends Thread {
    private Socket carSocket;
    private DataInputStream entrada;
    private DataOutputStream saida;
    private Company company;

    // Atributos para sincronização
    private Object sincroniza = new Object();

    public CarRepport(Socket _carSocket, Company _company) {
        this.company = _company;
        this.carSocket = _carSocket;
        try {
            // variaveis de entrada e saida do servidor
            entrada = new DataInputStream(carSocket.getInputStream());
            saida = new DataOutputStream(carSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.sincroniza = new Object();
    }

    @Override
    public void run() {

        try {
            String StatusDoCarro = "";
            double distanciaPercorrida = 0;

            // loop principal
            while(!StatusDoCarro.equals("encerrado")) {
                
                DrivingData comunicacao = extraiDrivingData(entrada.readUTF());
                StatusDoCarro = comunicacao.getCarStatus(); // lê solicitacao do cliente
                
                company.sendComunicacao(comunicacao);

                double latInicial = comunicacao.getLatInicial();
                double lonInicial = comunicacao.getLonInicial();
                double latAtual = comunicacao.getLatAtual();
                double lonAtual = comunicacao.getLonAtual();

                double distancia = calculaDistancia(latInicial, lonInicial, latAtual, lonAtual);

                //System.out.println(comunicacao.getCarID() + " percorreu " + distancia + " metros");
		        
                if (distancia > (distanciaPercorrida + 1000)) {
			        distanciaPercorrida += 1000;
                    String driverID = comunicacao.getDriverID();
                    company.oneKmPay(driverID);
		        }
                
                if (StatusDoCarro.equals("aguardando")) {
                
                    if(!Company.routesAvaliable()) {
                        System.out.println("SMC - Sem mais rotas para liberar.");
                        Rota rota = new Rota("-1", "00000");
                        saida.writeUTF(criaJSONRota(rota));
                        break;
                    }

                    if(Company.routesAvaliable()) {
                        synchronized (sincroniza) {
                            Rota resposta = company.executarRota();
                            saida.writeUTF(criaJSONRota(resposta));
                        }
                    }
                
                } else if(StatusDoCarro.equals("finalizado")) {

                    String routeID = comunicacao.getRouteIDSUMO();
                    System.out.println("SMC - Rota " + routeID + " finalizada.");
                    company.terminarRota(routeID);
                    distanciaPercorrida = 0;
                
                } else if(StatusDoCarro.equals("rodando")) {
                    
                } else if (StatusDoCarro.equals("encerrado")) {
                    break;
                }
            }

            System.out.println("Encerrando canal.");
            entrada.close();
            saida.close();
            carSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }

    // Método responsável por calcular a distância percorrida pelo Carro com base nas latitudes.
    private double calculaDistancia(double lat1, double lon1, double lat2, double lon2) {
		double raioTerra = 6371000;
	
		// Diferenças das latitudes e longitudes
		double diferancaLat = Math.toRadians(lat2 - lat1);
		double diferancaLon = Math.toRadians(lon2 - lon1);
	
		// Fórmula de Haversine
		double a = Math.sin(diferancaLat / 2) * Math.sin(diferancaLat / 2) +
				   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
				   Math.sin(diferancaLon / 2) * Math.sin(diferancaLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distancia = raioTerra * c;
	
		return distancia;
	}
    
    private String criaJSONRota(Rota rota) {
        JSONObject rotaJSON = new JSONObject();
        rotaJSON.put("ID da Rota", rota.getID());
        rotaJSON.put("Edges", rota.getEdges());
        return rotaJSON.toString();
    }

    private DrivingData extraiDrivingData(String drivingDataJSON) {
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
        int personCapacity = drivingDataJSONObj.getInt("PersonCapacity");
        int personNumber = drivingDataJSONObj.getInt("PersonNumber");

        DrivingData drivingData = new DrivingData(carID, driverID, carStatus, latInicial, lonInicial, latAtual, lonAtual, timeStamp, 
        X_Position, Y_Position, roadIDSUMO, routeIDSUMO, speed, odometer, fuelConsumption, averageFuelConsumption, 
        fuelType, fuelPrice, Co2Emission, HCEmission, personCapacity, personNumber);

		return drivingData;
	}

}