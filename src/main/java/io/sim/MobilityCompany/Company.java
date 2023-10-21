package io.sim.MobilityCompany;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.Pagamentos.Account;
import io.sim.Pagamentos.AlphaBank;
import io.sim.Pagamentos.BotPayment;
import io.sim.Transport.CarDriver.DrivingData;
import io.sim.Transport.Rotas.Rota;

public class Company extends Thread {
    // Atributos de Servidor
    private ServerSocket serverSocket;

    // Atributos para syncção
    private static Object sync;
    private static boolean rotasDisponiveis;
    private boolean conectandoCars;

    // Atributos da classe
    private ArrayList<Rota> rotasDisp;
    private ArrayList<Rota> routesInExec;
    private ArrayList<Rota> finishedRoutes;
    private double price;
    private static int numDrivers;
    private static ArrayList<DrivingData> dd;

    // Atributos como cliente de AlphaBank
    private Socket socket;
    private Account account;
    private int alphaBankServerPort;
    private String alphaBankServerHost; 
    private DataInputStream input;
    private DataOutputStream output;
    
    public Company(ServerSocket serverSocket, ArrayList<Rota> rotas, int _numDrivers, int _alphaBankServerPort, String _alphaBankServerHost) {
        
        // Inicializa servidor
        this.serverSocket = serverSocket;

        // Inicializa atributos de syncção
        sync = new Object();
        rotasDisponiveis = true;
        this.conectandoCars = true;

        // Atributos da classe
        this.rotasDisp = rotas;
		//System.out.println("Rotas: "+ rotasDisp.size()+" rotas disponiveis");
        routesInExec = new ArrayList<Rota>();
        finishedRoutes = new ArrayList<Rota>();
        price = 3.25;
        numDrivers = _numDrivers;
        dd = new ArrayList<DrivingData>();

        // Atributos como cliente de AlphaBank
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
            
            System.out.println("Company se conectou ao Servido do AlphaBank!!");

            while (rotasDisponiveis) {
                
                if(!conectandoCars) {
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Verifica se ainda tem roas disponíveis
                if(rotasDisp.size() == 0 && routesInExec.size() == 0) {
                    System.out.println("Rotas terminadas");
                    rotasDisponiveis = false;
                }

                // Talvez passar essa função pra outra classe
                if(conectandoCars) {
                    boolean start = true;
                    for(int i = 0; i < numDrivers; i++) {
                       
                        Socket socket = serverSocket.accept();

                        // Cria uma thread para com de cada Car
                        CarRepport cr = new CarRepport(socket, this);
                        cr.start();

                        if (start) {
                            atualizaSheet att = new atualizaSheet(this);
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

    // Método responsável por retornar a informação
    public static boolean routesAvaliable() {
        return rotasDisponiveis;
    }

    public boolean rotasDispVazio(){
        return (routesInExec.isEmpty());
    }

    // Método responsável por verificar se o carro passado ainda existe no SUMO, ele deve existir pois o simulador estava apresentando problemas 
    // para mudar as rotas dos veículos uma vez que a Thread de algum deles deixa de existir.
    public static boolean stillOnSUMO(String _idCar, SumoTraciConnection _sumo) {
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

    // Libera uma rota para o cliente que a solicitou. Para isso, remove de "rotasDisp" e adiciona em "routesInExec"
    public Rota execRoute() {
        synchronized (sync) {
            Rota rota = rotasDisp.remove(0);
            routesInExec.add(rota);
            return rota;
        }
    }

    // Método responsável por adicionar a rota terminada ao ArrayList de Rotas Finalizadas
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

    // Método responsável por criar o BotPayment que realizará o pagamento por 1Km percorrido ao motorista.
    public void oneKmPay(String driverID) throws IOException {
        BotPayment bt = new BotPayment(socket, "Company",  account.getPassword(), driverID, price);
        bt.start();
    }

    // Método para enviar informações de comunicação aos carros (DrivingData)
    // Synchronized para garantir o acesso seguro a partir de várias threads simultaneamente.
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