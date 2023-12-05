package io.sim.simulator.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import io.sim.simulator.comunication.AESencrypt;
import io.sim.simulator.comunication.JSONConverter;
import io.sim.simulator.driver.DrivingData;

/**
 *      A classe CarManipulator é responsável por manipular a comunicação com carros que estão realizando rotas o código analisa 
 * comportamento do carro e processa as informações que chegam. A classe monitora o status do carro, calcula a distância percorrida, 
 * faz pagamentos, atribui rotas e lida com os diferentes estados do carro, como "esperando", "finalizado", "rodando" e "abastecendo". 
 */
public class CarManipulator extends Thread {
    private Socket carSocket;      // O socket para comunicação com o carro
    private DataInputStream entrada;  // Stream de entrada para receber dados do carro
    private DataOutputStream saida;  // Stream de saída para enviar dados para o carro

    private Company company;  // Referência à empresa que gerencia as rotas

    // Atributos para sincronização
    //private Object sincroniza = new Object();

    public CarManipulator(Socket _carSocket, Company _company) {
        this.company = _company;
        this.carSocket = _carSocket;
        try {
            // Variáveis de entrada e saída do servidor
            entrada = new DataInputStream(carSocket.getInputStream());
            saida = new DataOutputStream(carSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //this.sincroniza = new Object();
    }

    @Override
    public void run() {
        try {
            String StatusDoCarro = "";
            double distancia = 0;
            double distanciaPercorrida = 0;
            int numBytesMsg;
            byte[] mensagemEncriptada;
            boolean sair = false;

            // Loop principal para interagir com o carro
            while (!StatusDoCarro.equals("encerrado") && !sair) {
                numBytesMsg = JSONConverter.extraiTamanhoBytes(AESencrypt.decripta(entrada.readNBytes(AESencrypt.getTamNumBytes())));
                DrivingData comunicacao = JSONConverter.extraiDrivingData(AESencrypt.decripta(entrada.readNBytes(numBytesMsg)));

                StatusDoCarro = comunicacao.getCarStatus();  // Lê o status do carro
                //System.out.println("CarManipulator: " + StatusDoCarro);

                // Lê as informações sobre latitude e longitude
                double latInicial = comunicacao.getLatAnt();
                double lonInicial = comunicacao.getLonAnt();
                double latAtual = comunicacao.getLatAtual();
                double lonAtual = comunicacao.getLonAtual();

                // Calcula a distância a partir das latitude e longitude iniciais e atuais
                distancia = atualizaDistancia(distancia, latInicial, lonInicial, latAtual, lonAtual);

                // Verifica se o carro percorreu 1 Km adicionaL
                // System.out.println(comunicacao.getCarID() + " percorreu " + distancia + " metros");
                if (distancia > (distanciaPercorrida + 1000)) {
                    distanciaPercorrida = (Math.floor(distancia / 1000)) * 1000; // Atualiza distânciaPercorrida
                    String driverID = comunicacao.getDriverID();
                    company.fazerPagamento(driverID); // Chama o método que contrói um BotPayment na classe Company
                }

                // Atualiza as informações recebidas do cliente
                comunicacao.setDistance(distancia);

                if (comunicacao.getPrecisaAttExcel() == 1) {
                    company.addComunicacao(comunicacao); // Adiciona a informação na lista de espera para atualização da planilha Excel
                }

                // Estado "esperando", nesse estado o carro espera receber uma rota do servidor
                if (StatusDoCarro.equals("esperando")) {
                    // Caso não tenha mais nenhuma rota no vetor de rotas disponíveis da Company
                    // O CarManipulator cria uma rota com informações incoerentes para avisar o Car que as rotas terminaram
                    if (!Company.temRotasDisponiveis()) {
                        System.out.println("SMC - Sem mais rotas para liberar.");
                        Rota rota = new Rota("-1", "00000");
                        mensagemEncriptada = AESencrypt.encripta(JSONConverter.criaJSONRota(rota));
                        saida.write(AESencrypt.encripta(JSONConverter.criaJSONTamanhoBytes(mensagemEncriptada.length)));
                        saida.write(mensagemEncriptada);
                        break;
                    }

                    // Caso tenha mais rotas no vetor de rotas disponíveis da Company
                    // O CarManipulator solicita a próxima rota para company e manda para o Car
                    if (Company.temRotasDisponiveis()) {
                        Rota resposta = company.executarRota();
                        mensagemEncriptada = AESencrypt.encripta(JSONConverter.criaJSONRota(resposta));
                        saida.write(AESencrypt.encripta(JSONConverter.criaJSONTamanhoBytes(mensagemEncriptada.length)));
                        saida.write(mensagemEncriptada);

                        if (resposta.getID().equals("-1")) {
                            sair = true;
                        }
                    }

                // Estado "finalizado", indica que o carro finalizou uma rota
                } else if (StatusDoCarro.equals("finalizado")) {
                    String routeID = comunicacao.getRouteIDSUMO();
                    System.out.println("SMC - Rota " + routeID + " finalizada.");
                    company.terminarRota(routeID); // A partir do ID da rota chama-se o método que passa as rotas das lista em execução pra lista de terminadas
                
                // Estado "rodando", indica que o carro está cumprindo uma rota
                } else if (StatusDoCarro.equals("rodando")) {
                    // Não faz nada

                // Estado "abastecendo", indica que o carro está parado para abastecer
                } else if (StatusDoCarro.equals("abastecendo")) {
                    // Não faz nada
                
                // Estado "encerrado", indica que a thread do carro foi encerrada
                } else if (StatusDoCarro.equals("encerrado")) {
                    break; // Sai do while imediatamente
                }
            }
            
            // Encerra o canal de comunicações
            System.out.println("Encerrando canal.");
            //entrada.close();
            //saida.close();
            carSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método que usa a fórmula de Haversine para calcular a distância entre dois pontos na Terra
    private double atualizaDistancia(double distancia, double lat1, double lon1, double lat2, double lon2) {
        double raioTerra = 6371000;

        // Diferenças das latitudes e longitudes
        double diferancaLat = Math.toRadians(lat2 - lat1);
        double diferancaLon = Math.toRadians(lon2 - lon1);

        // Fórmula de Haversine
        double a = Math.sin(diferancaLat / 2) * Math.sin(diferancaLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(diferancaLon / 2) * Math.sin(diferancaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double novaDistancia = raioTerra * c;

        return distancia + novaDistancia;
    }
}
