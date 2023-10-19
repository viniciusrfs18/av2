package io.sim.Simulador;

import it.polito.appeal.traci.SumoTraciConnection;

public class ExecutaSimulador extends Thread {
    private SumoTraciConnection sumo;
    private long taxaAquisicao;

    public ExecutaSimulador(SumoTraciConnection _sumo, long _taxaAquisicao) {
        this.sumo = _sumo;
        this.taxaAquisicao = _taxaAquisicao;
    }

    @Override
    public void run()
    {
        while(true)
        {
            try {
                this.sumo.do_timestep();
                sleep(taxaAquisicao);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                break;
            }
        }
    }
}