package io.sim.MobilityCompany;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import io.sim.Car;
import io.sim.Rotas.Rotas;

public class CommunicationThread extends Thread {
    private Socket socket;
    private Object oWatch;
    private Company company;

    public CommunicationThread(Socket socket, Object oWatch, Company company) {
        this.socket = socket;
        this.oWatch = oWatch;
        this.company = company;
    }

    @Override
    public void run() {
        try {
            //System.out.println("SMC - Entrou no try.");
            ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
            //System.out.println("SMC - Passou da entrada.");
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
            //System.out.println("SMC - Passou da saída.");

            String mensagem = "";
            String mensagem1 = "";
            //String mensagemPag = "";
            while (!mensagem.equals("encerrado")) {
                
                //System.out.println("Aguardando mensagem...");
                DrivingData objIn = (DrivingData) entrada.readObject();
                String aux = objIn.getCarState();
                
                
                String[] separa = aux.split(",");
                mensagem = separa[0];

                if (separa.length == 2){
                    mensagem1 = separa[1];    
                }
                
                //Car objIn2 = (Car) entrada.readObject();
                //mensagemPag = objIn2.getAux();
                //System.out.println("SMC ouviu " + mensagem);

                if (mensagem.equals("aguardando")) {
                    
                    synchronized (oWatch) {
                        Rotas resposta = company.liberarRota();
                        System.out.println("SMC - Liberando rota:\n" + resposta.getRouteID());
                        saida.writeUTF(company.RoutetoString(resposta));
                    }

                } else if (mensagem.equals("finalizado")) {

                    String routeID = objIn.getRouteIDSUMO();
                    System.out.println("SMC - Rota " + routeID + " finalizada.");
                    this.company.arquivarRota(routeID);
                    //System.out.println("Rotas para executar: " + company.routesToExeSize() + "\nRotas em execução: " + company.routesInExeSize() + "\nRotas executadas: " + company.routesExecutedSize());
                
                } else if (mensagem.equals("rodando")) {
                    // Continua a Rodar
                    //if(aux.equals("pagamento")){
                    //    company.OneKmPay();
                    //}
                } else if (mensagem.equals("encerrado")) {
                    break; // fim do Loop
                } else if (mensagem.equals("pagamento")) {
                    System.out.println("Chegou para Pagamento");
                    company.OneKmPay(mensagem1);
                    //getDriverID - objIn.getAutoID()
                } 

                /***/
            }

            System.out.println("Encerrando canal.");
            entrada.close();
            saida.close();
            socket.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
