package io.sim.simulator.simulation;

import it.polito.appeal.traci.SumoTraciConnection;

// A classe ExecutaSimulador é responsável por executar a simulação no SUMO
public class ExecutaSimulador extends Thread {
    private SumoTraciConnection sumo;
    private long taxaAquisicao;
    private boolean funcionando;

    public ExecutaSimulador(SumoTraciConnection _sumo, long _taxaAquisicao) {
        this.sumo = _sumo;
        this.taxaAquisicao = _taxaAquisicao;
        this.funcionando = true;
    }

    @Override
    public void run() {
         // Loop infinito para continuar a execução do simulador
        while(funcionando) {
            try {
                this.sumo.do_timestep();
                // Executa um passo de simulação no SUMO
                sleep(taxaAquisicao);
                // Aguarda um determinado tempo (taxa de aquisição) antes de continuar
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        System.out.println("Executa Simulador encerrado!!!");
    }

    public void setFuncionando(boolean _funcionando) {
        funcionando = _funcionando;
    }
}
