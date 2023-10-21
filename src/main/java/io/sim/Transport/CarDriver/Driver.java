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
    private DataInputStream input;
    private DataOutputStream output;
    
    // Atributos da Classe
    private String driverID;
    private Car car;
    private long acquisitionRate;
    private ArrayList<Rota> rotasDisp = new ArrayList<Rota>();
    private Rota rotaAtual;
    private ArrayList<Rota> rotasTerminadas = new ArrayList<Rota>();
    private boolean initRoute = false;
    private long balanceInicial;

    private FuelStation fs;

    public Driver(String _driverID, Car _car, long _acquisitionRate, FuelStation _postoCombustivel, int _alphaBankServerPort, String _alphaBankServerHost) {
        this.driverID = _driverID;
        this.car = _car;
        this.acquisitionRate = _acquisitionRate;
        this.rotasDisp = new ArrayList<Rota>();
        rotaAtual = null;
        this.rotasTerminadas = new ArrayList<Rota>();
        this.balanceInicial = 10000;
        this.alphaBankServerPort = _alphaBankServerPort;
        this.alphaBankServerHost = _alphaBankServerHost;
        this.fs = _postoCombustivel;
    }

    @Override
    public void run() {
        try {
            System.out.println("Iniciando " + this.driverID);
            
            socket = new Socket(this.alphaBankServerHost, this.alphaBankServerPort);
            input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());

            this.account = new Account(driverID, 50);
            AlphaBank.addAccount(account);
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

                if (this.car.getNivelDoTanque() < 3){
                    
                    //this.car.setSpeed(0);
                    double litros = (10 - this.car.getNivelDoTanque());
                    double qtdFuel = qtdToFuel(litros, this.account.getBalance());
                        
                    try {
                        System.out.println(driverID + " decidiu abastecer " + qtdFuel);
                            
                        this.car.stopToFuel();
                            
                        fs.fuelCar(this.car, qtdFuel);
                            
                        fsPayment(socket, (qtdFuel*fs.getPreco()));
                        
                    } catch (Exception e) {
                            e.printStackTrace();
                    }
                }

                System.out.println(account.getAccountID() + " tem R$" + account.getBalance() + " de balance");
            }
            
            car.setFinalizado(true);  
            System.out.println("Encerrando " + driverID);
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Função utilizada para criar o BotPayment responsável por realizar o pagamento do Driver a FuelStation
    private void fsPayment(Socket socket, double amount){
        BotPayment bt = new BotPayment(socket, account.getAccountID(), account.getPassword(), "FuelStation", amount);
        bt.start();
    }

    public Car getCar(){
        return this.car;
    }

     // Método responsável por informar a quantidade de litros que o carro irá abastecer, esta quantidade será definida de acordo com o balance bancário do motorista, simulando melhor a realidade.
     public double qtdToFuel(double litros, double balanceDisp) { //
        double preco = fs.getPreco();
        double precoTotal = litros * preco;

        if (balanceDisp > precoTotal) {
            return litros;
        } else {
            double precoReduzindo = precoTotal;
            while (balanceDisp < precoReduzindo) {
                litros--;
                precoReduzindo = litros*preco;
                
                if (litros <= 0) {
                    return 0;
                }
            }
            return litros;
        }
    }

    public String getDriverId() {
        return this.driverID;
    }

    public FuelStation getFuelStation() {
        return this.fs;
    }


}
