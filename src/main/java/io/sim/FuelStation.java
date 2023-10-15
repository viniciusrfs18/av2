package io.sim;

import java.util.concurrent.Semaphore;

import io.sim.Pagamentos.Account;

public class FuelStation extends Thread {
    private Semaphore fuelPumps;
    private Account account;
    //private AlphaBankClient alphaBankClient;

    public FuelStation() {
        this.fuelPumps = new Semaphore(2); // Duas bombas de combustível disponíveis
        //this.alphaBankClient = alphaBankClient;
        this.account = new Account(1, 0);
    }

    public void refuelCar(Car car) throws Exception {
        try {
        
            fuelPumps.acquire(); // Tenta adquirir uma bomba de combustível

            System.out.println("Car " + car.getIdAuto() + " is refueling at Fuel Station.");
            
            //Thread.sleep(12000); // Tempo de abastecimento de 2 minutos (em milissegundos)
            Thread.sleep(2000); // Tempo de abastecimento de 2s (em milissegundos)
            
            car.fillFuelTank();
            
            System.out.println("Car " + car.getIdAuto() + " finished refueling.");
            
            fuelPumps.release(); // Libera a bomba de combustível
        
        } catch (InterruptedException e) {
        
            e.printStackTrace();
        
        }
    }

    @Override
    public void run() {
        // Implemente a lógica para a Fuel Station realizar transações com o AlphaBank
        // para receber pagamentos dos Drivers.
        // Você pode usar o alphaBankClient para se comunicar com o AlphaBank.
    }

    public Account getAccount(){
        return this.account;
    }

}