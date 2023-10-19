package io.sim.MobilityCompany;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import io.sim.JSONConverter;
import io.sim.Transport.CarDriver.DrivingData;
import io.sim.Transport.Rotas.Rota;

public class CarManipulator extends Thread {
    private Socket carSocket;
    private DataInputStream entrada;
    private DataOutputStream saida;

    private Company company;

    // Atributos para sincronização
    private Object sincroniza = new Object();

    public CarManipulator(Socket _carSocket, Company _company) {
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
                // System.out.println("Aguardando mensagem...");
                DrivingData comunicacao = JSONConverter.extraiDrivingData(entrada.readUTF());
                StatusDoCarro = comunicacao.getCarStatus(); // lê solicitacao do cliente
                
                double latInicial = comunicacao.getLatInicial();
                double lonInicial = comunicacao.getLonInicial();
                double latAtual = comunicacao.getLatAtual();
                double lonAtual = comunicacao.getLonAtual();

                double distancia = calculaDistancia(latInicial, lonInicial, latAtual, lonAtual);

                System.out.println(comunicacao.getCarID() + " percorreu " + distancia + " metros");
		        if (distancia > (distanciaPercorrida + 1000)) {
			        distanciaPercorrida += 1000;
                    String driverID = comunicacao.getDriverID();
                    company.fazerPagamento(driverID);
		        }
                
                if (StatusDoCarro.equals("aguardando")) {
                    if(!Company.temRotasDisponiveis()) {
                        System.out.println("SMC - Sem mais rotas para liberar.");
                        Rota rota = new Rota("-1", "00000");
                        saida.writeUTF(JSONConverter.criaJSONRota(rota));
                        break;
                    }

                    if(Company.temRotasDisponiveis()) {
                        synchronized (sincroniza) {
                            Rota resposta = company.executarRota();
                            saida.writeUTF(JSONConverter.criaJSONRota(resposta));
                        }
                    }
                } else if(StatusDoCarro.equals("finalizado")) {
                    String routeID = comunicacao.getRouteIDSUMO();
                    System.out.println("SMC - Rota " + routeID + " finalizada.");
                    company.terminarRota(routeID);
                    distanciaPercorrida = 0;
                    System.out.println("Aguardando mensagem...");
                } else if(StatusDoCarro.equals("rodando")) {
                    // a principio, nao faz nada
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
    
}