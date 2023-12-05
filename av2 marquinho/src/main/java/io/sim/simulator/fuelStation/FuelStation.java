package io.sim.simulator.fuelStation;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import io.sim.simulator.bank.Account;
import io.sim.simulator.bank.AlphaBank;
import io.sim.simulator.bank.EndAccount;
import io.sim.simulator.company.Company;
import io.sim.simulator.driver.Car;

/**
 *      A classe FuelStation representa um posto de combustível que é executada como uma thread. Ela gerencia o abastecimento 
 * de carros, controla o acesso às bombas de combustível, e se conecta ao servidor do AlphaBank para lidar com transações 
 * financeiras relacionadas ao abastecimento.
 */
public class FuelStation extends Thread {
    // Atributos da classe
    private Semaphore bombas; // Controla o acesso às bombas de combustível
    private double preco; // O preço por litro de combustível

    // Atributos como cliente de AlphaBank
    private Socket socket; // Socket de comunicação com o AlphaBank
    private Account account; // A conta da estação de combustível no AlphaBank
    private int alphaBankServerPort; // A porta do servidor do AlphaBank
    private String alphaBankServerHost; // O host do servidor do AlphaBank

    public FuelStation(int _alphaBankServerPort, String _alphaBankServerHost) {
        // Inicializa os atributos da estação de combustível
        this.bombas = new Semaphore(2); // Duas bombas de combustível disponíveis
        this.preco = 5.87; // Define o preço por litro de combustível

        // Inicializa os atributos para comunicação com o AlphaBank
        alphaBankServerPort = _alphaBankServerPort;
        alphaBankServerHost = _alphaBankServerHost;
    }

    @Override
    public void run() {
        try {
            System.out.println("Fuel Station iniciando...");

            socket = new Socket(this.alphaBankServerHost, this.alphaBankServerPort); // Conecta-se ao servidor AlphaBank
            this.account = Account.criaAccount("Fuel Station", 0); // Cria uma conta para a estação de combustível
            AlphaBank.adicionarAccount(account); // Adiciona a conta ao AlphaBank
            account.start(); // Inicia a conta para processar transações
            System.out.println("Fuel Station se conectou ao Servido do AlphaBank!!");

            Thread.sleep(5000);
            // Aguarda até que não haja mais rotas estejam 
            while (Company.temRotasDisponiveis()) {
                Thread.sleep(10000);
            }
            System.out.println("Encerrando a Fuel Station...");
            EndAccount endAccount = new EndAccount(socket, account);
            endAccount.start(); // Encerra a conta e fecha a conexão com o AlphaBank
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Retorna o ID da conta da estação de combustível
    public String getFSAccountID() {
        return this.account.getAccountID();
    }

    // Retorna o preço por litro de combustível
    public double getPrecoLitro() {
        return this.preco;
    }

    // Decide a quantidade de litros a serem abastecidos com base no saldo disponível
    public double[] decideQtdLitros(double litros, double saldoDisp) {
        double precoTotal = litros * preco; // Calcula o preço total com base na quantidade desejada

        if (saldoDisp > precoTotal) {
            double[] info = new double[] { precoTotal, litros * 1000 }; // Se o saldo é suficiente, abastece o máximo possível
            return info;
        } else {
            double precoReduzindo = precoTotal;
            while (saldoDisp < precoReduzindo) {
                litros--; // Reduz a quantidade de litros
                precoReduzindo = litros * preco;
                
                if (litros <= 0) {
                    double[] info = new double[] { 0, 0 }; // Se o saldo não é suficiente para abastecer nada, retorna zero litros e custo
                    return info;
                }
            }
            double[] info = new double[] { precoReduzindo, litros * 1000 }; // Retorna o custo e a quantidade de litros ajustados
            return info;
        }
    }

    // Método que faz o abastecimento dos carros
    public void abastecerCarro(Car car, double litros) {
        try {
            bombas.acquire(); // Tenta adquirir uma bomba de combustível
            boolean jaAbasteceu = false;
            car.preparaAbastecimento();
            while (!jaAbasteceu) {
                if (car.getSpeed() == 0) {
                    System.out.println(car.getIdCar() + " está abastecendo no Posto de Gasolina");
                    Thread.sleep(120000); // Tempo de abastecimento de 2 minutos (120000 em milissegundos)
                    car.abastecido(litros);
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
