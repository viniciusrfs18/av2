package io.sim;


import io.sim.reconciliation.CalcularEstatisticas;
import io.sim.simulator.simulation.EnvSimulator;

/**
 * Classe que inicia toda a aplicação
 */
public class App {
    public static void main(String[] args) throws InterruptedException {
	    long taxaAquisicao = 40;
        int numeroDeAmostras = 100;
        
        // Cria uma instância da classe EnvSimulator
        EnvSimulator ev = new EnvSimulator(taxaAquisicao, numeroDeAmostras);
        
        // Inicia a execução da simulação chamando o método "start" na instância
        ev.start();
        ev.join();
        
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        CalcularEstatisticas calc = new CalcularEstatisticas(taxaAquisicao, numeroDeAmostras);
        calc.start();
        calc.join();

        System.out.println("Encerando APP!");
        System.exit(0);
    }
}
