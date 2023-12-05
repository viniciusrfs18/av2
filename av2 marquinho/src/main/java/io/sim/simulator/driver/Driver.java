package io.sim.simulator.driver;

import io.sim.simulator.bank.Account;
import io.sim.simulator.bank.AlphaBank;
import io.sim.simulator.bank.BotPayment;
import io.sim.simulator.bank.EndAccount;
import io.sim.simulator.company.Rota;
import io.sim.simulator.fuelStation.FuelStation;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 *      A classe Driver é responsável por representar um motorista na simulação. Ela gerencia a interação entre o 
 * motorista, o veículo (Car) que ele dirige e outros elementos, como o AlphaBank (Como cliente) e a estação de 
 * combustível (FuelStation).
 */
public class Driver extends Thread {
    // Cliente de AlphaBank
    private Account account;
    private Socket socket;
    private int alphaBankServerPort;
    private String alphaBankServerHost;
    
    // Atributos da Classe
    private String driverID;
    private Car car;
    private long taxaAquisicao;
    private ArrayList<Rota> rotasDisp = new ArrayList<Rota>();
    private Rota rotaAtual;
    private ArrayList<Rota> rotasTerminadas = new ArrayList<Rota>();
    private boolean initRoute = false;
    private long saldoInicial;

    private FuelStation postoCombustivel;

    public Driver(String _driverID, Car _car, long _taxaAquisicao, FuelStation _postoCombustivel, int _alphaBankServerPort, String _alphaBankServerHost) {
        this.driverID = _driverID;
        this.car = _car;
        this.taxaAquisicao = _taxaAquisicao;
        this.rotasDisp = new ArrayList<Rota>();
        rotaAtual = null;
        this.rotasTerminadas = new ArrayList<Rota>();
        this.saldoInicial = 30;
        this.alphaBankServerPort = _alphaBankServerPort;
        this.alphaBankServerHost = _alphaBankServerHost;
        this.postoCombustivel = _postoCombustivel;
    }

    @Override
    public void run() {
        try {
            System.out.println("Iniciando " + this.driverID);

            // Estabelece uma conexão com o servidor AlphaBank
            socket = new Socket(this.alphaBankServerHost, this.alphaBankServerPort);

            // Cria uma conta bancária para o motorista
            this.account = Account.criaAccount(driverID, saldoInicial);
            AlphaBank.adicionarAccount(account);
            account.start();
            System.out.println(driverID + " se conectou ao Servidor do AlphaBank!!");

            // Inicia uma thread para o veículo (Car) associado ao motorista
            Thread threadCar = new Thread(this.car);
            threadCar.start();

            // Loop principal da execução do motorista
            while(threadCar.isAlive()) {
                Thread.sleep(taxaAquisicao);

                // Verifica o status do veículo
                if(car.getCarStatus() == "finalizado") {
                    // System.out.println(this.driverID + " rota " + rotaAtual.getID() + " finalizada");
                    rotasTerminadas.add(rotaAtual);
                    initRoute = false;
                } else if((this.car.getCarStatus() == "rodando") && !initRoute) {
                    rotasDisp.add(this.car.getRota());
                    rotaAtual = rotasDisp.remove(0);
                    // System.out.println(this.driverID + " rota "+ rotaAtual.getID() +" iniciada");
                    initRoute = true;
                }

                // Verifica o nível de combustível do veículo
                if (this.car.getNivelDoTanque() < 3000){
                    double litros = (this.car.getCapacidadeDoTanque() - this.car.getNivelDoTanque())/1000; // Verifica quantos litros faltando
                    double[] info = postoCombustivel.decideQtdLitros(litros, this.account.getSaldo()); // Decide quantos litros irá comprar com base no saldo da conta do Driver
                    double precoAPagar = info[0];
                    double qtdML = info[1];
                    if (qtdML != 0) {
                        try {
                            System.out.println(driverID + " decidiu abastecer " + qtdML);
                            // Abastece o veículo na estação de combustível
                            postoCombustivel.abastecerCarro(this.car, qtdML);

                            // Inicia uma transação de pagamento no AlphaBank
                            BotPayment bt = new BotPayment(socket, account.getAccountID(), account.getSenha(), postoCombustivel.getFSAccountID(), precoAPagar);
                            bt.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Se o carro não tiver dinheiro para abastecer ele fica parado pra sempre
                        System.out.println("/////////////////////////////////////// " + driverID + " NÃO TEM DINHEIRO PRA ABASTECER!!");
                    }
                }
            }

            // Encerra a execução do veículo e a conta bancária
            car.setEncerrado(true);
            System.out.println("Encerrando " + driverID);
            EndAccount endAccount = new EndAccount(socket, account);
            endAccount.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public String getID() {
        return driverID;
    }

    public String getCarID() {
        return this.car.getIdCar();
    }
}
