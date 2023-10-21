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
    private DataInputStream input;
    private DataOutputStream output;

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
            
            System.out.println("Fuel Station iniciando..."); // Mensagem para verificar que a FuelStation iniciou

            socket = new Socket(this.alphaBankServerHost, this.alphaBankServerPort); // cria um socket 
            input = new DataInputStream(socket.getInputStream()); // inicia um canal para receber a input de dados
            output = new DataOutputStream(socket.getOutputStream()); // inicia um canal para receber a saída de dados

            this.account = new Account("FuelStation", 0); // cria a conta bancária para o FuelStation com o balance zerado
            
            AlphaBank.addAccount(account); // Por meio do método estático da classe AlphaBank a conta criada para a FuelStation é adicionada ao array de contas do AlphaBank
            account.start(); // Inicia a Thread da conta criada para a FuelStation

            while (true) {
                //NADA NADA NADA    
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Encerrando a Fuel Station...");
    }

    // Método que retorna a ID da conta da fuelstation
    public String getFSAccountID() { 
        return this.account.getAccountID();
    }

    // Método que retorna o Preço do Litro de Gasolina
    public double getPreco() { 
        return this.preco;
    }


    // Método responsável por representar o abastecimento do objeto carro
    public void fuelCar(Car car, double litros) {
       
        try {
           
            bombas.acquire(); // Tenta adquirir uma bomba de combustível
           
            System.out.println(car.getIdCar() + " está abastecendo no Posto de Gasolina");
                    
            Thread.sleep(120000); // Tempo de abastecimento de 2 minutos (120000 em milissegundos)
            
            car.abastecido(litros); // Chama o método da classe carro responsável por colocar a quantidade possível de gasolina no tanque do objeto e fazer o carro voltar a andar.
            
            System.out.println(car.getIdCar() + " terminou de abastecer");
                    
            bombas.release(); // Libera a bomba de combustível


        } catch (Exception e) {
            e.printStackTrace();
        }
    
    }

}

