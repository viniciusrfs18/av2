package io.sim;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import io.sim.Car;

public class FuelStation extends Thread {
    private Semaphore pump;
    private double price;
    private Socket socket;
    private Account account;
    private int alphaBankServerPort;
    private String alphaBankServerHost;
    private DataInputStream input;
    private DataOutputStream output;

    public FuelStation(int _alphaBankServerPort, String _alphaBankServerHost) {
        this.pump = new Semaphore(2);
        this.price = 5.87;
        alphaBankServerPort = _alphaBankServerPort;
        alphaBankServerHost = _alphaBankServerHost;
    }

    @Override
    public void run() {
        try {
            System.out.println("start Fuel Station ");

            socket = new Socket(this.alphaBankServerHost, this.alphaBankServerPort);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            this.account = new Account("FuelStation", 0);
            AlphaBank.addAccount(account);
            account.start();

            while (true) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    public String getFSAccountID() {
        return this.account.getAccountID();
    }

    public double getprice() {
        return this.price;
    }

    public void fuelCar(Car car, double litros) {
        try {
            pump.acquire();

            System.out.println(car.getIdCar() + " Abastecendo ");

            Thread.sleep(1500);

            car.abastecido(litros);

            System.out.println(car.getIdCar() + " abastecido ");
            car.recalcularParcials();
            pump.release();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
