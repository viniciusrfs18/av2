package io.sim;

import de.tudresden.sumo.cmd.Route;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.Car;
import io.sim.Rota;
import it.polito.appeal.traci.SumoTraciConnection;

public class TransportService extends Thread {
    private String idTransportService;
    private boolean on_off;
    private SumoTraciConnection sumo;
    private Car car;
    private Rota rota;
    private SumoStringList edge;
    private boolean terminado;
    private boolean sumoInit;

    public TransportService(boolean _on_off, String _idTransportService, Rota _route, Car _car, SumoTraciConnection _sumo) {
        this.on_off = _on_off;
        this.idTransportService = _idTransportService;
        this.rota = _route;
        this.car = _car;
        this.sumo = _sumo;
        this.terminado = false;
        this.sumoInit = false;
    }

    @Override
    public void run() {
        this.initializeRoutes();
        System.out.println(this.car.getIdCar() + "Rota: " + edge + " adicionada!");
        String edgeFinal = edge.get(edge.size() - 1);
        System.out.println(this.car.getIdCar() + "- Edge final: " + edgeFinal);

        try {
            sleep(this.car.getAcquisitionRate());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Encerrando TransportService.");
    }

    private void initializeRoutes() {
        edge = new SumoStringList();
        edge.clear();
        String aux = this.rota.getEdges();

        for (String e : aux.split(" ")) {
            edge.add(e);
        }

        try {
            sumo.do_job_set(Route.add(this.rota.getID(), edge));
            sumo.do_job_set(Vehicle.addFull(this.car.getIdCar(), this.rota.getID(), "DEFAULT_VEHTYPE", "now", "0", "0", "0", "current", "max", "current", "", "", "", this.car.getPersonCapacity(), this.car.getPersonNumber()));
            //sumo.do_job_set(Vehicle.setColor(this.car.getIdCar(), this.car.getColorCar()));
            this.sumo.do_job_set(Vehicle.setSpeed(this.car.getIdCar(), 50));
            this.sumo.do_job_set(Vehicle.setSpeedMode(this.car.getIdCar(), 31));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        this.sumoInit = true;
    }

    public boolean isOn_off() {
        return on_off;
    }

    public void setOn_off(boolean _on_off) {
        this.on_off = _on_off;
    }

    public void setTerminado(boolean _terminado) {
        this.terminado = _terminado;
    }

    public String getIdTransportService() {
        return this.idTransportService;
    }

    public SumoTraciConnection getSumo() {
        return this.sumo;
    }

    public Car getCar() {
        return this.car;
    }

    public Rota getRota() {
        return this.rota;
    }

    public void setRoute(Rota _rota) {
        this.rota = _rota;
    }
}
