package io.sim.simulator.driver;

/**
 * 		A classe DrivingData é responsável por armazenar dados do veículo que são utilizados para fins organizacionais e 
 * relatórios via Excel. Ela inclui informações relevantes para o tratamento de rotas, bem como métricas relacionadas à 
 * simulação fornecidas pelo SUMO (Simulator of Urban Mobility), como velocidade, consumo de combustível e emissão de CO2 
 * durante intervalos de tempo específicos.
 */
public class DrivingData {

	/* Informações adicionais para lógica de tratamento de rotas */
	
	private String carID;           // ID do veículo
	private String driverID;        // ID do motorista associado ao veículo
	private String carStatus;       // Status atual do veículo
	private double latAnt;          // Latitude anterior do veículo
	private double lonAnt;          // Longitude anterior do veículo
	private double latAtual;        // Latitude atual do veículo
	private double lonAtual;        // Longitude atual do veículo
	private int precisaAttExcel;

	/* SUMO's data */

	private long timeStamp;         // Hora do sistema na hora da execução (System.currentTimeMillis())
    private String routeIDSUMO;     // ID da rota obtido a partir do SUMO (this.sumo.do_job_get(Vehicle.getRouteID(this.idAuto)))
    private double speed;           // Velocidade em m/s no último passo de tempo
    private double distance;        // Distância percorrida
    private double fuelConsumption; // Consumo de combustível em mg/s no último passo de tempo
    private int fuelType;           // Tipo de combustível (1-diesel, 2-gasolina, 3-etanol, 4-híbrido)
    private double co2Emission;     // Emissão de CO2 em mg/s no último passo de tempo

	public DrivingData(String carID, String driverID, String carStatus, double latAnt, double lonAnt, double latAtual, double lonAtual, 
		int _precisaAttExcel, long timeStamp, String routeIDSUMO, double speed, double distance, double fuelConsumption, int fuelType,
		double co2Emission) {
		
		this.carID = carID;
		this.driverID = driverID;
		this.carStatus = carStatus;
		this.latAnt = latAnt;
		this.lonAnt = lonAnt;
		this.latAtual = latAtual;
		this.lonAtual = lonAtual;
		this.precisaAttExcel = _precisaAttExcel;

		this.timeStamp = timeStamp;
		this.routeIDSUMO = routeIDSUMO;
		this.speed = speed;
		this.distance = distance;
		this.fuelConsumption = fuelConsumption;
		this.fuelType = fuelType;
		this.co2Emission = co2Emission;
	}

	/* Getters e Setters das informações adicionais para lógica de tratamento de rotas */

	public String getCarID() {
		return this.carID;
	}

	public String getDriverID() {
		return this.driverID;
	}

	public String getCarStatus() {
		return this.carStatus;
	}

	public void setCarStatus(String _carStatus) {
		carStatus = _carStatus;
	}

	public double getLatAnt() {
		return this.latAnt;
	}

	public double getLonAnt() {
		return this.lonAnt;
	}

	public double getLatAtual() {
		return this.latAtual;
	}

	public double getLonAtual() {
		return this.lonAtual;
	}

	public int getPrecisaAttExcel() {
		return this.precisaAttExcel;
	}

	/* SUMO's data getters e setters */

	public long getTimeStamp() {
		return timeStamp;
	}

	public String getRouteIDSUMO() {
		return routeIDSUMO;
	}

	public double getSpeed() {
		return speed;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double _distance) {
		distance = _distance;
	}

	public double getFuelConsumption() {
		return fuelConsumption;
	}

	public void setFuelConsumption(double _fuelConsumption) {
		fuelConsumption = _fuelConsumption;
	}

	public int getFuelType() {
		return fuelType;
	}

	public double getCo2Emission() {
		return co2Emission;
	}

}