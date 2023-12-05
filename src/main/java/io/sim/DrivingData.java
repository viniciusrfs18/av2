package io.sim;

//import java.io.Serializable;

/**Armazena dados do veiculo
 * Funcao organizacional, para ser usada no relatorio via Excel.
 */

//public class DrivingData implements Serializable{
public class DrivingData {

		/* Informações adicionais para lógica de tratamento de rotas */
	
	private String carID;
	private String driverID;
	private String carStatus;
	private double latInicial;
	private double lonInicial;
	private double latAtual;
	private double lonAtual;

		/* SUMO's data */

	private long timeStamp; 			// System.currentTimeMillis()
	private double x_Position; 			// sumoPosition2D (x)
	private double y_Position; 			// sumoPosition2D (y)
	private String roadIDSUMO; 			// this.sumo.do_job_get(Vehicle.getRoadID(this.idAuto))
	private String routeIDSUMO; 		// this.sumo.do_job_get(Vehicle.getRouteID(this.idAuto))
	private double speed; 				// in m/s for the last time step
	private double odometer; 			// the distance in (m) that was already driven by this vehicle.
	private double fuelConsumption; 	// in mg/s for the last time step
	private double fuelPrice; 			// price in liters
	private int fuelType; 				// 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private double averageFuelConsumption;
	private int personCapacity;			// the total number of persons that can ride in this vehicle
	private int personNumber;			// the total number of persons which are riding in this vehicle
	private double co2Emission; 		// in mg/s for the last time step
	private double HCEmission; 			// in mg/s for the last time step

	public DrivingData(
			String _carID, String _driverID, String _carStatus, double _latInicial, double _lonInicial, 
			double _latAtual, double _lonAtual, long _timeStamp, double _x_Position, double _y_Position,
			String _roadIDSUMO, String _routeIDSUMO, double _speed, double _odometer, double _fuelConsumption,
			double _averageFuelConsumption, int _fuelType, double _fuelPrice, double _co2Emission, 
			double _HCEmission, int _personCapacity, int _personNumber) {
		
		this.carID = _carID;
		this.driverID = _driverID;
		this.carStatus = _carStatus;
		this.latInicial = _latInicial;
		this.lonInicial = _lonInicial;
		this.latAtual = _latAtual;
		this.lonAtual = _lonAtual;

		this.timeStamp = _timeStamp;
		this.x_Position = _x_Position;
		this.y_Position = _y_Position;
		this.roadIDSUMO = _roadIDSUMO;
		this.routeIDSUMO = _routeIDSUMO;
		this.speed = _speed;
		this.odometer = _odometer;
		this.fuelConsumption = _fuelConsumption;
		this.averageFuelConsumption = _averageFuelConsumption;
		this.fuelType = _fuelType;
		this.fuelPrice = _fuelPrice;
		this.co2Emission = _co2Emission;
		this.HCEmission = _HCEmission;
		this.personCapacity = _personCapacity;
		this.personNumber = _personNumber;

	}

	public String getCarID() {
		return this.carID;
	}

	public String getDriverID() {
		return this.driverID;
	}

	public String getCarStatus() {
		return this.carStatus;
	}

	public double getLatInicial() {
		return this.latInicial;
	}

	public double getLonInicial() {
		return this.lonInicial;
	}

	public double getLatAtual() {
		return this.latAtual;
	}
	public double getLonAtual() {
		return this.lonAtual;
	}


	/* SUMO's data getters e setters */

	public double getSpeed() {
		return this.speed;
	}

	public double getOdometer() {
		return this.odometer;
	}

	public double getFuelConsumption() {
		return this.fuelConsumption;
	}

	public double getCo2Emission() {
		return this.co2Emission;
	}

	public double getX_Position() {
		return this.x_Position;
	}

	public double getY_Position() {
		return this.y_Position;
	}

	public long getTimeStamp() {
		return this.timeStamp;
	}

	public String getRoadIDSUMO() {
		return this.roadIDSUMO;
	}

	public String getRouteIDSUMO() {
		return this.routeIDSUMO;
	}

	public double getHCEmission() {
		return this.HCEmission;
	}

	public double getFuelPrice() {
		return this.fuelPrice;
	}

	public int getFuelType() {
		return this.fuelType;
	}

	public int getPersonCapacity() {
		return this.personCapacity;
	}

	public int getPersonNumber() {
		return this.personNumber;
	}

	public double getAverageFuelConsumption() {
		return this.averageFuelConsumption;
	}

	public void setAverageFuelConsumption(double _averageFuelConsumption) {
		this.averageFuelConsumption = _averageFuelConsumption;
	}
}