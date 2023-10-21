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
    private Semaphore pump; // Um semáforo para controlar o acesso às bombas de combustível
    private double price; // Preço por litro de gasolina
    
    // Atributos para comunicação com o AlphaBank
    private Socket socket; // Um socket para se comunicar com o AlphaBank
    private Account account; // Uma conta bancária associada à estação de combustível
    private int alphaBankServerPort; // Porta do servidor AlphaBank
    private String alphaBankServerHost; // Host do servidor AlphaBank
    private DataInputStream input; // Canal de entrada de dados
    private DataOutputStream output; // Canal de saída de dados

    public FuelStation(int _alphaBankServerPort, String _alphaBankServerHost) {
        this.pump = new Semaphore(2); // Duas bombas de combustível disponíveis
        this.price = 5.87; // Preço por litro de gasolina
        
        // Atributos para comunicação com o AlphaBank
        alphaBankServerPort = _alphaBankServerPort;
        alphaBankServerHost = _alphaBankServerHost;
    }

    @Override
    public void run() {
        try {
            System.out.println("Fuel Station iniciando..."); // Mensagem de início da estação de combustível

            // Inicia a conexão com o servidor AlphaBank
            socket = new Socket(this.alphaBankServerHost, this.alphaBankServerPort);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            // Cria uma conta bancária para a estação de combustível com saldo zerado
            this.account = new Account("FuelStation", 0);

            // Adiciona a conta ao AlphaBank (classe estática) e inicia sua thread
            AlphaBank.addAccount(account);
            account.start();

            while (true) {
                // A execução principal da estação de combustível fica em loop indefinido
                // aguardando por operações de abastecimento de carros
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Encerrando a Fuel Station...");
    }

    // Método que retorna a ID da conta da estação de combustível
    public String getFSAccountID() {
        return this.account.getAccountID();
    }

    // Método que retorna o preço por litro de gasolina
    public double getprice() {
        return this.price;
    }

    // Método responsável por representar o abastecimento de um carro na estação de combustível
    public void fuelCar(Car car, double litros) {
        try {
            pump.acquire(); // Tenta adquirir uma bomba de combustível

            System.out.println(car.getIdCar() + " está abastecendo no Posto de Gasolina");

            Thread.sleep(120000); // Tempo de abastecimento de 2 minutos (120000 milissegundos)

            // Chama o método do carro para abastecer com a quantidade de litros especificada
            car.abastecido(litros);

            System.out.println(car.getIdCar() + " terminou de abastecer");

            pump.release(); // Libera a bomba de combustível

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
