package io.sim.Transport.CarDriver;

import io.sim.MobilityCompany.Company;
import io.sim.Pagamentos.Account;
import io.sim.Pagamentos.AlphaBank;
import io.sim.Pagamentos.BotPayment;
import io.sim.Transport.Fuel.FuelStation;
import io.sim.Transport.Rotas.Rota;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Driver extends Thread {
    // Cliente de AlphaBank
    private Account account;
    private Socket socket;
    private int alphaBankServerPort;
    private String alphaBankServerHost; 
    private DataInputStream entrada;
    private DataOutputStream saida;
    
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
        this.saldoInicial = 10000;
        this.alphaBankServerPort = _alphaBankServerPort;
        this.alphaBankServerHost = _alphaBankServerHost;
        this.postoCombustivel = _postoCombustivel;
    }

    @Override
    public void run() {
        try {
            System.out.println("Iniciando " + this.driverID);
            
            socket = new Socket(this.alphaBankServerHost, this.alphaBankServerPort);
            entrada = new DataInputStream(socket.getInputStream());
			saida = new DataOutputStream(socket.getOutputStream());

            this.account = new Account(driverID, 0);
            AlphaBank.adicionarAccount(account);
            account.start();
            
            System.out.println(driverID + " se conectou ao Servido do AlphaBank!!");
            
            Thread threadCar = new Thread(this.car);
            threadCar.start();

            while(threadCar.isAlive()) {
                Thread.sleep(this.car.getAcquisitionRate());
                
                if(car.getCarStatus() == "finalizado") {
                    System.out.println(this.driverID + " rota " + this.rotasDisp.get(0).getID() + " finalizada");
                    rotasTerminadas.add(rotaAtual);
                    initRoute = false;
                } else if((this.car.getCarStatus() == "rodando") && !initRoute) {
                    System.out.println(this.driverID + " rota "+ this.car.getRota().getID() +" iniciada");
                    rotaAtual = car.getRota();
                    initRoute = true; 
                }

                if (this.car.getNivelDoTanque() < 7500){
                    
                    //this.car.setSpeed(0);
                    double litros = (this.car.getCapacidadeDoTanque() - this.car.getNivelDoTanque())/1000;
                    double[] info = postoCombustivel.decideQtdLitros(litros, this.account.getSaldo());
                    double precoAPagar = info[0];
                    double qtdML = info[1];

                    if (qtdML != 0) {
                        try {
                            System.out.println(driverID + " decidiu abastecer " + qtdML);
                            this.car.preparaAbastecimento();
                            postoCombustivel.abastecerCarro(this.car, qtdML);
                            BotPayment bt = new BotPayment(socket, account.getAccountID(), account.getSenha(), postoCombustivel.getFSAccountID(), precoAPagar);
                            bt.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("/////////////////////////////////////// " + driverID + " NÃO TEM DINHEIRO PRA ABASTECER!!");
                    }
                }

                System.out.println(account.getAccountID() + " tem R$" + account.getSaldo() + " de saldo");
            }
            car.setFinalizado(true);  
            System.out.println("Encerrando " + driverID);
            // this.car.join();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
