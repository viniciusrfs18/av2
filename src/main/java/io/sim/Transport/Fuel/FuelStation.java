package io.sim.Transport.Fuel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import io.sim.Pagamentos.Account;
import io.sim.Pagamentos.AlphaBank;
import io.sim.Transport.CarDriver.Car;

public class FuelStation extends Thread {
    // Atributos da classe
    private Semaphore bombas;
    private double preco;
    
    // Atributos como cliente de AlphaBank
    private Socket socket;
    private Account account;
    private int alphaBankServerPort;
    private String alphaBankServerHost; 
    private DataInputStream entrada;
    private DataOutputStream saida;

    public FuelStation(int _alphaBankServerPort, String _alphaBankServerHost) {
        this.bombas = new Semaphore(2); // Duas bombas de combustível disponíveis
        this.preco = 5.87;
        
        // Atributos como cliente de AlphaBank
        alphaBankServerPort = _alphaBankServerPort;
        alphaBankServerHost = _alphaBankServerHost;
    }

    @Override
    public void run() {
        try {
            System.out.println("Fuel Station iniciando...");

            socket = new Socket(this.alphaBankServerHost, this.alphaBankServerPort);
            entrada = new DataInputStream(socket.getInputStream());
            saida = new DataOutputStream(socket.getOutputStream());

            this.account = new Account("Fuel Station", 0);
            
            AlphaBank.adicionarAccount(account);
            account.start();
            
            System.out.println("Fuel Station se conectou ao Servido do AlphaBank!!");

            while (true) {
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Encerrando a Fuel Station...");
    }

    public String getFSAccountID() {
        return this.account.getAccountID();
    }

    public double getPrecoLitro() {
        return this.preco;
    }

    public double[] decideQtdLitros(double litros, double saldoDisp) {
        double precoTotal = litros * preco;

        if (saldoDisp > precoTotal) {
            double[] info = new double[] { precoTotal, litros*1000 };
            return info;
        } else {
            double precoReduzindo = precoTotal;
            while (saldoDisp < precoReduzindo) {
                litros--;
                precoReduzindo = litros*preco;
                
                if (litros <= 0) {
                    double[] info = new double[] { 0, 0 };
                    return info;
                }
            }
            double[] info = new double[] { precoReduzindo, litros*1000 };
            return info;
        }
    }

    public void abastecerCarro(Car car, double litros) {
        try {
            boolean jaAbasteceu = false;
            while (!jaAbasteceu) {
                if (car.getSpeed() == 0) {
                    bombas.acquire(); // Tenta adquirir uma bomba de combustível
                    System.out.println(car.getIdCar() + " está abastecendo no Posto de Gasolina");
                    Thread.sleep(30000); // Tempo de abastecimento de 2 minutos (120000 em milissegundos)
                    car.abastecido();
                    System.out.println(car.getIdCar() + " terminou de abastecer");
                    bombas.release(); // Libera a bomba de combustível
                    jaAbasteceu = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

