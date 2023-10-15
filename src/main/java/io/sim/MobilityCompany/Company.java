package io.sim.MobilityCompany;


import java.io.ObjectInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import io.sim.Driver;
import io.sim.Pagamentos.Account;
import io.sim.Pagamentos.BotPayment;
import io.sim.Rotas.Rotas;

public class Company extends Thread {
    // atributos de servidor
    private ServerSocket serverSocket;
    
    // atributos de sincronizacao
    private Object oWatch = new Object();
    private static boolean liberado = true;
    
    // cliente AlphaBank
    private Socket socket;

    // atributos da classe
    private static ArrayList<Rotas> routesToExe = new ArrayList<Rotas>();
    private static ArrayList<Rotas> routesInExe = new ArrayList<Rotas>();
    private static ArrayList<Rotas> routesExecuted = new ArrayList<Rotas>();
    private ArrayList<Driver> drivers = new ArrayList<Driver>();

    // private static Account account;
    private static final double RUN_PRICE = 3.25;
    private static int numDrivers;
    private static boolean routesAvailable = true;
    private static boolean allDriversCreated = false;
    
    private Account account;

    public Company(ServerSocket serverSocket, ArrayList<Rotas> routes, int _numDrivers)
    {
        // BotPayment payment = new BotPayment(RUN_PRICE);
        // Adicionar as rotas em routesToExe a partir de um arquivo
        this.serverSocket = serverSocket;
        numDrivers = _numDrivers;
        routesToExe = routes;
        this.account = new Account(0, 10000);

        try {
            this.socket = new Socket("localhost", 33333);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        };
    }

    @Override
    public void run()
    {
        try
        {
            System.out.println("MobilityCompany iniciada...");

            while (routesAvailable) // IMP tentar trocar para 
            {

                if(allDriversCreated)
                {
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                
                if(routesToExe.size() == 0 && routesInExe.size() == 0){

                    System.out.println("Rotas terminadas");
                    routesAvailable = false;
                
                }
                
                if(!allDriversCreated){
                    for(int i=0; i<numDrivers;i++) { // conecta os clientes -> IMP mudar para ser feito paralelamente (ou n)
                        
                        System.out.println("MC - Aguardando conexao" + (i+1));
                        Socket socket = serverSocket.accept();
                        System.out.println("Car conectado");

                        Thread mc = new CommunicationThread(socket, oWatch, this);
                        mc.start();
                        if(i == (numDrivers - 1)){
                            System.out.println("MC - Todos os drivers criados.");
                        }
                    }
                    allDriversCreated = true;
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("MobilityCompany encerrada...");
    }
    

    /**Libera uma rota para o cliente que a solicitou. Para isso, remove de routesToExe e adiciona em routesInExe
     * @return route Route - Rota do topo da ArrayList de rotas
     */
    public Rotas liberarRota(){
        synchronized (oWatch)
        {
            Rotas route = routesToExe.remove(0);
            routesInExe.add(route); // mudar para routesInExe.add(car.getID(),route) ou route.getID()
            return route;
        }
    }

    public void arquivarRota(String _routeID){
        synchronized (oWatch)
        {
            System.out.println("Arquivando rota: " + _routeID);
            for(int i=0;i<routesInExe.size();i++)
            {
                if(routesInExe.get(i).getRouteID().equals(_routeID))
                {
                    routesExecuted.add(routesInExe.remove(i));
                    break;
                }
            }
        }
    }

    public static boolean areRoutesAvailable() {
        return routesAvailable;
    }

    public String RoutetoString(Rotas _route){
        String convert;
        convert = _route.getRouteID() + "," + _route.getEdges();
        return convert;
    }

    public int routesToExeSize(){
        return routesToExe.size();
    }
    
    public int routesInExeSize(){
        return routesInExe.size();
    }

    public int routesExecutedSize(){
        return routesExecuted.size();
    }

    public Account getAccount(){
        return this.account;
    }
    
    public void setDrivers(ArrayList<Driver> drivers){
        this.drivers = drivers;
    }


    public int searchAccount(String driverID){
        int a = -1;
        for (int i=0; i<numDrivers; i++){
            if(drivers.get(i).getDriverId().equals(driverID)){
                a = drivers.get(i).getAccount().getIdentifier();
                return a;
            }
        }
        return a;
    }

    public void OneKmPay(String driverID){
        System.out.println(driverID);
        int contaDriv = searchAccount(driverID);
        if (contaDriv == -1){
            System.out.println("Conta inexistente");
        } else  {
            System.out.println("Entrou no OnePay e a Conta foi encontrada");
            BotPayment bt = new BotPayment(socket, 0, contaDriv, RUN_PRICE);
            bt.start();
        }        
        
        // Criar uma BotPaymento - Syncronized -> passar para ele o socket, id do motorista que precisa receber e passar o valor()
        // Neste momento, fazer o start do BotPaymento
    }

}
