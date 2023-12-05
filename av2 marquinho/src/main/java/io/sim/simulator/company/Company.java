package io.sim.simulator.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.simulator.bank.Account;
import io.sim.simulator.bank.AlphaBank;
import io.sim.simulator.bank.BotPayment;
import io.sim.simulator.bank.EndAccount;
import io.sim.simulator.driver.DrivingData;

public class Company extends Thread {
    // Atributos de Servidor
    private ServerSocket serverSocket;

    // Atributos para sincronização
    private static Object sincroniza;
    private static boolean rotasDisponiveis;
    private boolean canectandoCars;

    // Atributos da classe
    private static ArrayList<Rota> rotasDisp;
    private static ArrayList<Rota> rotasEmExec;
    private static ArrayList<Rota> rotasTerminadas;
    private static double preco;
    private static int numDrivers;
    private static ArrayList<DrivingData> carReports;

    // Atributos como cliente de AlphaBank
    private Socket socket;
    private Account account;
    private int alphaBankServerPort;
    private String alphaBankServerHost; 
    
    public Company(ServerSocket serverSocket, ArrayList<Rota> _rotasDisp, int _numDrivers, int _alphaBankServerPort, String _alphaBankServerHost) {
        // Inicializa servidor
        this.serverSocket = serverSocket;

        // Inicializa atributos de sincronização
        sincroniza = new Object();
        rotasDisponiveis = true;
        this.canectandoCars = true;

        // Atributos da classe
        rotasDisp = _rotasDisp;
		System.out.println("Rotas: "+ rotasDisp.size()+" rotas disponiveis");
        rotasEmExec = new ArrayList<Rota>();
        rotasTerminadas = new ArrayList<Rota>();
        preco = 3.25;
        numDrivers = _numDrivers;
        carReports = new ArrayList<DrivingData>();

        // Atributos como cliente de AlphaBank
        alphaBankServerPort = _alphaBankServerPort;
        alphaBankServerHost = _alphaBankServerHost;
    }

    @Override
    public void run() {
        try {
            System.out.println("Company iniciando...");

            socket = new Socket(this.alphaBankServerHost, this.alphaBankServerPort);
            this.account = Account.criaAccount("Company", 1999999999);
            AlphaBank.adicionarAccount(account);
            account.start();
            System.out.println("Company se conectou ao Servido do AlphaBank!!");
            CompanyAttExcel attExcel = new CompanyAttExcel(this);

            while (rotasDisponiveis || !carReports.isEmpty()) {
                // Pequeno delay para evitar problemas
                if(!canectandoCars) {
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Verifica se ainda tem rotas disponíveis
                if(rotasDisp.size() == 0 && rotasEmExec.size() == 0) {
                    rotasDisponiveis = false;
                }

                // Talvez passar essa função pra outra classe
                if(canectandoCars) {
                    for(int i = 0; i < numDrivers; i++) {
                        // conecta os clientes -> IMP mudar para ser feito paralelamente (ou n)
                        System.out.println("Company - Esperando para conectar " + (i + 1));
                        Socket socket = serverSocket.accept();
                        System.out.println("Car conectado");

                        // Cria uma thread para comunicacao de cada Car
                        CarManipulator carManipulator = new CarManipulator(socket, this);
                        carManipulator.start();

                        if (i == 0) {
                            attExcel.start();
                        }

                    }
                    System.out.println("Company: Todos os drivers criados");
                    canectandoCars = false;
                }

                //System.out.println(account.getAccountID() + " tem R$" + account.getSaldo() + " de saldo");
            }
            System.out.println("Encerrando a Company...");
            EndAccount endAccount = new EndAccount(socket, account);
            endAccount.start();
            attExcel.setFuncionando(false);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getAccountID() {
        return account.getAccountID();
    }

    public static boolean temRotasDisponiveis() {
        return rotasDisponiveis;
    }

    public synchronized void addComunicacao(DrivingData comunicacao) {
        carReports.add(comunicacao);
    }

    public boolean temReport() {
        return !carReports.isEmpty();
    }

    public DrivingData pegaComunicacao() {
        return carReports.remove(0);
    }

    public static boolean estaNoSUMO(String _idCar, SumoTraciConnection _sumo) {
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
            if(rotasDisponiveis) {
                if (rotasDisp.size() != 0) {
                    Rota rota = rotasDisp.remove(0);
                    rotasEmExec.add(rota);
                    System.out.println("Rotas a serem executadas: " + rotasDisp.size());
                    return rota;
                }
                Rota rota = new Rota("-1", "00000 00000 00000");
                return rota;
            } else {
                System.out.println("Rotas terminadas");
                Rota rota = new Rota("-1", "00000 00000 00000");
                return rota;
            }
        }
    }

    public void terminarRota(String _routeID) {
        synchronized (sincroniza) {
            if (!rotasEmExec.isEmpty()){
                System.out.println("Arquivando rota: " + _routeID);
                int i = 0;
                while (!rotasEmExec.get(i).getID().equals(_routeID)) {
                    i++;
                }
                rotasTerminadas.add(rotasEmExec.remove(i));
            }
        }
    }

    public synchronized void fazerPagamento(String driverID) throws IOException {
        BotPayment bt = new BotPayment(socket, account.getAccountID(),  account.getSenha(), driverID, preco);
        bt.start();
    }
}