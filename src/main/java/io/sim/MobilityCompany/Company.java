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
import io.sim.Transport.Rotas.Rota;

public class Company extends Thread {
    // Atributos de Servidor
    private ServerSocket serverSocket;

    // Atributos para sincronização
    private static Object sincroniza;
    private static boolean rotasDisponiveis;
    private boolean conectandoCars;

    // Atributos da classe
    private ArrayList<Rota> rotasDisp;
    private ArrayList<Rota> rotasEmExec;
    private ArrayList<Rota> rotasTerminadas;
    private static double preco;
    private static int numDrivers;

    // Atributos como cliente de AlphaBank
    private Socket socket;
    private Account account;
    private int alphaBankServerPort;
    private String alphaBankServerHost; 
    private DataInputStream entrada;
    private DataOutputStream saida;
    
    public Company(ServerSocket serverSocket, ArrayList<Rota> rotas, int _numDrivers, int _alphaBankServerPort, String _alphaBankServerHost) {
        
        // Inicializa servidor
        this.serverSocket = serverSocket;

        // Inicializa atributos de sincronização
        sincroniza = new Object();
        rotasDisponiveis = true;
        this.conectandoCars = true;

        // Atributos da classe
        this.rotasDisp = rotas;
		System.out.println("Rotas: "+ rotasDisp.size()+" rotas disponiveis");
        rotasEmExec = new ArrayList<Rota>();
        rotasTerminadas = new ArrayList<Rota>();
        preco = 3.25;
        numDrivers = _numDrivers;

        // Atributos como cliente de AlphaBank
        alphaBankServerPort = _alphaBankServerPort;
        alphaBankServerHost = _alphaBankServerHost;

    }

    @Override
    public void run() {
        try {
            System.out.println("Company iniciando...");

            socket = new Socket(this.alphaBankServerHost, this.alphaBankServerPort);
            entrada = new DataInputStream(socket.getInputStream());
			saida = new DataOutputStream(socket.getOutputStream());
            
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
                if(rotasDisp.size() == 0 && rotasEmExec.size() == 0) {
                    System.out.println("Rotas terminadas");
                    rotasDisponiveis = false;
                }

                // Talvez passar essa função pra outra classe
                if(conectandoCars) {
                    for(int i = 0; i < numDrivers; i++) {
                        // conecta os clientes -> IMP mudar para ser feito paralelamente (ou n)
                        System.out.println("Company - Esperando para conectar " + (i + 1));
                        Socket socket = serverSocket.accept();
                        System.out.println("Car conectado");

                        // Cria uma thread para comunicacao de cada Car
                        CarRepport cr = new CarRepport(socket, this);
                        cr.start();
                    }
                    System.out.println("Company: Todos os drivers criados");
                    conectandoCars = false;
                }

                System.out.println(account.getAccountID() + " tem R$" + account.getSaldo() + " de saldo");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Encerrando a Company...");
    }

    // Método responsável por retornar a informação
    public static boolean routesAvaliable() {
        return rotasDisponiveis;
    }

    // Método responsável por verificar se o carro passado ainda existe no SUMO, ele deve existir pois o simulador estava apresentando problemas 
    // para mudar as rotas dos veículos uma vez que a Thread de algum deles deixa de existir.
    public static boolean stillOnSUMO(String _idCar, SumoTraciConnection _sumo) {
        synchronized(sincroniza){
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

    // Libera uma rota para o cliente que a solicitou. Para isso, remove de "rotasDisp" e adiciona em "rotasEmExec"
    public Rota executarRota() {
        synchronized (sincroniza) {
            Rota rota = rotasDisp.remove(0);
            rotasEmExec.add(rota);
            return rota;
        }
    }

    // Método responsável por adicionar a rota terminada ao ArrayList de Rotas Finalizadas
    public void terminarRota(String _routeID) {
        synchronized (sincroniza) {
            System.out.println("Arquivando rota: " + _routeID);
            int i = 0;
            while (!rotasEmExec.get(i).getID().equals(_routeID)) {
                i++;
            }
            rotasTerminadas.add(rotasEmExec.remove(i));
        }
    }

    // Método responsável por criar o BotPayment que realizará o pagamento por 1Km percorrido ao motorista.
    public void oneKmPay(String driverID) throws IOException {
        BotPayment bt = new BotPayment(socket, "Company",  account.getSenha(), driverID, preco);
        bt.start();
    }
}