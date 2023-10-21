package io.sim.Simulador;

import it.polito.appeal.traci.SumoTraciConnection;

public class ExecutaSimulador extends Thread {
    private SumoTraciConnection sumo;
    private long acquisitionRate;

    public ExecutaSimulador(SumoTraciConnection _sumo, long _acquisitionRate) {
        this.sumo = _sumo;
        this.acquisitionRate = _acquisitionRate;
    }

    @Override
    public void run()
    {
        while(true)
        {
            try {
                this.sumo.do_timestep();
                sleep(acquisitionRate);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}