package io.sim;

import java.util.ArrayList;

import io.sim.Pagamentos.Account;
import io.sim.Rotas.Rotas;
import io.sim.MobilityCompany.Company;

public class Driver extends Thread
{
    private String driverID;
    // // Cliente de AlphaBank
    // private Account account;
    // private TransportService ts;
    private Car car; // private Car car;
    // private static final double FUEL_PRICE = 5.87;
   
    private Account account;
    private FuelStation fs;
    private long acquisitionRate;
    private ArrayList<Rotas> routeToExe = new ArrayList<Rotas>();
    private ArrayList<Rotas> routesExecuted = new ArrayList<Rotas>();
    private ArrayList<Rotas> routesInExe = new ArrayList<Rotas>();
    private boolean initRoute = false;

    public Driver(String _driverID, Car _car, long _acquisitionRate, Account account)
    {
        this.driverID = _driverID;
        this.car = _car;
        this.acquisitionRate = _acquisitionRate;
        this.account = account;
        //this.fs = fs;
        
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
}
