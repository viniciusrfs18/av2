package io.sim;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;


public class Company extends Thread {
   
    private ServerSocket serverSocket;

  
    private static Object sync;
    private static boolean rotasDisponiveis;
    private boolean conectandoCars;

    
    private ArrayList<Rota> rotasDisp;
    private ArrayList<Rota> routesInExec;
    private ArrayList<Rota> finishedRoutes;
    private double price;
    private static int numDrivers;
    private static ArrayList<DrivingData> dd;

    private Socket socket;
    private Account account;
    private int alphaBankServerPort;
    private String alphaBankServerHost; 
    private DataInputStream input;
    private DataOutputStream output;
    
    public Company(ServerSocket serverSocket, ArrayList<Rota> rotas, int _numDrivers, int _alphaBankServerPort, String _alphaBankServerHost) {
        
        
        this.serverSocket = serverSocket;

       
        sync = new Object();
        rotasDisponiveis = true;
        this.conectandoCars = true;

        this.rotasDisp = rotas;
		
        routesInExec = new ArrayList<Rota>();
        finishedRoutes = new ArrayList<Rota>();
        price = 3.25;
        numDrivers = _numDrivers;
        dd = new ArrayList<DrivingData>();
        alphaBankServerPort = _alphaBankServerPort;
        alphaBankServerHost = _alphaBankServerHost;

    }

    @Override
    public void run() {
        try {
            System.out.println("Company iniciando...");

            socket = new Socket(this.alphaBankServerHost, this.alphaBankServerPort);
            input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
            
            this.account = new Account("Company", 100000);
            AlphaBank.addAccount(account);
            account.start();
        

            while (rotasDisponiveis) {
                
                if(!conectandoCars) {
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                
                if(rotasDisp.size() == 0 && routesInExec.size() == 0) {
                    System.out.println("Rotas terminadas");
                    rotasDisponiveis = false;
                }
                if(conectandoCars) {
                    boolean start = true;
                    for(int i = 0; i < numDrivers; i++) {
                       
                        Socket socket = serverSocket.accept();

                        
                        AtualizaCar cr = new AtualizaCar(socket, this);
                        cr.start();

                        if (start) {
                            SalvaExel att = new SalvaExel(this);
                            att.start();
                            start = false;
                        }
                    }
                    conectandoCars = false;
                }

            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Encerrando a Company.");
    }
    public static boolean routesAvaliable() {
        return rotasDisponiveis;
    }

    public boolean rotasDispVazio(){
        return (routesInExec.isEmpty());
    }

    public static boolean existeNoSumo(String _idCar, SumoTraciConnection _sumo) {
        synchronized(sync){
            try {
                SumoStringList lista;
                lista = (SumoStringList) _sumo.do_job_get(Vehicle.getIDList());
                return lista.contains(_idCar);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
	}

    public Rota execRoute() {
        synchronized (sync) {
            Rota rota = rotasDisp.remove(0);
            routesInExec.add(rota);
            return rota;
        }
    }

    public void endRoute(String _routeID) {
        synchronized (sync) {
            System.out.println("Arquivando rota: " + _routeID);
            int i = 0;
            while (!routesInExec.get(i).getID().equals(_routeID)) {
                i++;
            }
            finishedRoutes.add(routesInExec.remove(i));
        }
    }

    public void oneKmPay(String driverID) throws IOException {
        BotPayment bt = new BotPayment(socket, "Company",  account.getPassword(), driverID, price);
        bt.start();
    }

    public synchronized void sendCommunication(DrivingData com) {
        dd.add(com); // Adiciona o objeto com à lista dd para ser processado pelos carros.
    }

    // Verifica se existem relatórios (DrivingData) disponíveis.
    public boolean temReport() {
        return dd.isEmpty(); // Retorna true se a lista dd estiver vazia, caso contrário, retorna false.
    }
    
    // Método para pegar um relatório (DrivingData) da lista.
    public DrivingData pegacom() {
        return dd.remove(0); // Remove o primeiro relatório da lista e o retorna.
    }

    // Método para obter o preço por quilômetro utilizado para calcular os pagamentos aos motoristas.
    public double getprice() {
        return this.price; // Retorna o valor armazenado no atributo price.
    }

}