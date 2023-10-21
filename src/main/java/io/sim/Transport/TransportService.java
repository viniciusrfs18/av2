package io.sim.Transport;

import de.tudresden.sumo.cmd.Route;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.Transport.CarDriver.Car;
import io.sim.Transport.Rotas.Rota;
import it.polito.appeal.traci.SumoTraciConnection;

/**Cria o objeto veiculo no SUMO
 * Define cor e a rota.
 */
public class TransportService extends Thread {

	private String idTransportService;
	private boolean on_off;
	private SumoTraciConnection sumo;
	private Car car; // Veiculo correspondente 
	private Rota rota; // representa a rota a ser cumprida
	private SumoStringList edge; // NEWF
	private boolean terminado; // chamado pelo Car
	private boolean sumoInit;
	// private boolean sumoReady;

	public TransportService(boolean _on_off, String _idTransportService, Rota _route, Car _car, SumoTraciConnection _sumo) {
		this.on_off = _on_off;
		this.idTransportService = _idTransportService;
		this.rota = _route;
		this.car = _car;
		this.sumo = _sumo;
		this.terminado = false;
		this.sumoInit = false;
		// this.sumoReady = false;
	}

	@Override
	public void run() {
		System.out.println("Iniciando TransportService - " + this.car.getIdCar());
		this.initializeRoutes();
		System.out.println(this.car.getIdCar() + " - TS - Rota: " + edge + " adcionada!");
		String edgeFinal = edge.get(edge.size()-1);
		System.out.println(this.car.getIdCar() + " - TS - Edge final: " + edgeFinal);
		System.out.println(this.car.getIdCar() + " - TS - on");
		try {
			sleep(this.car.getAcquisitionRate());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


		System.out.println("Encerrando TransportService.");
	}

	private void initializeRoutes() {

		// Adiciona todas as edges em uma lista de Strings
		edge = new SumoStringList();
		edge.clear();
		String aux = this.rota.getEdges();

		for(String e : aux.split(" ")) {
			edge.add(e);
		}

		// Inicializa a rota, veiculo e a cor do veiculo
		try {
			sumo.do_job_set(Route.add(this.rota.getID(), edge));
			//sumo.do_job_set(Vehicle.add(this.auto.getIdAuto(), "DEFAULT_VEHTYPE", this.itinerary.getIdItinerary(), 0,
			//		0.0, 0, (byte) 0));
			
			sumo.do_job_set(Vehicle.addFull(this.car.getIdCar(), 				//vehID
											this.rota.getID(), 					//carID
											"DEFAULT_VEHTYPE", 					//typeID 
											"now", 								//depart  
											"0", 								//departLane 
											"0", 								//departPos 
											"0",								//departSpeed
											"current",							//arrivalLane 
											"max",								//arrivalPos 
											"current",							//arrivalSpeed 
											"",									//fromTaz 
											"",									//toTaz 
											"", 								//line 
											this.car.getPersonCapacity(),		//personCapacity 
											this.car.getPersonNumber())		//personNumber
					);
			
			sumo.do_job_set(Vehicle.setColor(this.car.getIdCar(), this.car.getColorCar()));
			
			this.sumo.do_job_set(Vehicle.setSpeed(this.car.getIdCar(), 50));
			this.sumo.do_job_set(Vehicle.setSpeedMode(this.car.getIdCar(), 31));
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		this.sumoInit = true;
		// this.sumoReady = true;
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

	// public boolean isSumoReady() {
	// 	return sumoReady;
	// }

}