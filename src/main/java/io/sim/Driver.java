package io.sim;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import io.sim.Pagamentos.Account;
import io.sim.Pagamentos.BotPayment;
import io.sim.Rotas.Rotas;
import io.sim.MobilityCompany.Company;

public class Driver extends Thread
{
    private String driverID;
    
    // cliente AlphaBank
    private Socket socket;
    
    private Car car; // private Car car;
    // private static final double FUEL_PRICE = 5.87;
   
    private Account account;
    private FuelStation fs;
    private long acquisitionRate;
    private ArrayList<Rotas> routeToExe = new ArrayList<Rotas>();
    private ArrayList<Rotas> routesExecuted = new ArrayList<Rotas>();
    private ArrayList<Rotas> routesInExe = new ArrayList<Rotas>();
    private boolean initRoute = false;

    public Driver(String _driverID, Car _car, long _acquisitionRate, Account account, FuelStation fs)
    {
        this.driverID = _driverID;
        this.car = _car;
        this.acquisitionRate = _acquisitionRate;
        this.account = account;
        this.fs = fs;
        
        try {
            this.socket = new Socket("localhost", 33333);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        };

        this.start();
        // pensar na logica de inicializacao do TransporteService e do Car
        // this.car.start();
        // BotPayment payment = new BotPayment(fuelPrice);
    }

    @Override
    public void run()
    {
        try {
            
            System.out.println("Iniciando " + this.driverID);
            this.car.start();

            while(Company.areRoutesAvailable()){

                Thread.sleep(this.car.getAcquisitionRate());

                if(this.car.getCarRepport().getCarState() == "finalizado"){

                    // retirar de routesInExe e colocar em routesExecuted
                    System.out.println(this.driverID + " rota "+ this.routesInExe.get(0).getRouteID() +" finalizada");
                    this.routesExecuted.add((this.routesInExe.remove(0)));
                    initRoute = false;
                
                }
                
                else if((this.car.getCarRepport().getCarState() == "rodando") && !initRoute){

                    System.out.println(this.driverID + " rota "+ this.car.getRoute().getRouteID() +" iniciada");
                    this.routesInExe.add(this.car.getRoute());
                    initRoute = true; 
                
                }

                //System.out.println(this.car.getFuelLevel());

                System.out.println(this.driverID + " possui o saldo de " + this.account.getBalance());

                if (this.car.getFuelLevel() < 7.5){
                    this.car.stopToFuel();
                    double qtd = this.car.qtdRefuel();
                    fs.refuelCar(this.car);
                    fsPay(qtd*fs.getFuelPrice());
                    //System.out.println("Pagamento realizado para a fuel station");
                    
                }

            }

            this.car.setfinished(true);  
            System.out.println("Encerrando " + this.driverID);
            // this.car.join();
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Account getAccount(){
        return this.account;
    }

    public String getDriverId(){
        return this.driverID;
    }

    public void fsPay(double amount){
        BotPayment bt = new BotPayment(socket, getAccount().getIdentifier(), 1, amount);
        bt.start();
      
        // Criar uma BotPayment - Syncronized -> passar para ele o socket, id do motorista que precisa receber e passar o valor()
        // Neste momento, fazer o start do BotPaymento
    }
}
