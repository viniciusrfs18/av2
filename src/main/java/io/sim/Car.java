package io.sim;

import de.tudresden.sumo.cmd.Vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.net.Socket;
import java.util.ArrayList;

import java.util.Random;
import java.util.Collections;


import org.json.JSONObject;

import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.objects.SumoStringList;


/**Define os atributos que coracterizam um Carro.
 * Por meio de metodos get da classe Vehicle, 
 */
public class Car extends Vehicle implements Runnable {
	// atributos de cliente
    private Socket socket;
    private int companyServerPort;
    private String companyServerHost; 
	private DataInputStream input;
	private DataOutputStream output;
	
	// atributos da classe
	private String idCar; // id do carro
	private SumoColor colorCar;
	private String driverID; // id do motorista
	private SumoTraciConnection sumo;
	private boolean on_off;
	private boolean finalizado; // chamado pelo Driver
	private long acquisitionRate; // taxa de aquisicao de dados dos sensores
	private int fuelType; 			// 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private int fuelPreferential; 	// 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
	private double fuelPrice; 		// price in liters
	private int personCapacity;		// the total number of persons that can ride in this vehicle
	private int personNumber;		// the total number of persons which are riding in this vehicle
	private double speed; //NEWF
	private Rota rota;
	private double fuelTank;
	private String carStatus;
	private double latInicial;
	private double lonInicial;
	private double latAtual;
	private double lonAtual;
	private DrivingData drivingDataAtual;
	private ArrayList<DrivingData> drivingRepport; // dados de conducao do veiculo
	private TransportService ts;
	private long timerIni  = 0;
	private static int tempoPrevisto = 432;
	private static int distanciaRota = 21648;
	private int numeroDeParcials = 10;
	private int numeroDeParcialsExecutadas = 0;
	public static int valorPercentualMargem = 10;
	public static ArrayList<Integer> listaParcialTempo = new ArrayList<>();
	public static ArrayList<Integer> listaParcialDistancia = new ArrayList<>();
	private static int pointer = 0;


	public Car(boolean _on_off, String _idCar, SumoColor _colorCar, String _driverID, SumoTraciConnection _sumo, long _acquisitionRate,
            int _fuelType, int _fuelPreferential, double _fuelPrice, int _personCapacity, int _personNumber, String _companyServerHost, 
            int _companyServerPort) throws Exception {
    

    this.companyServerPort = _companyServerPort;
    this.companyServerHost = _companyServerHost;
    this.on_off = _on_off;
    this.idCar = _idCar;
    this.colorCar = _colorCar;
    this.driverID = _driverID;
    this.sumo = _sumo;
    this.acquisitionRate = _acquisitionRate;
    
    // Verificação do tipo de combustível e preferência, com valores padrão se estiverem fora do intervalo.
    if((_fuelType < 0) || (_fuelType > 4)) {
        this.fuelType = 4;
    } else {
        this.fuelType = _fuelType;
    }
    
    if((_fuelPreferential < 0) || (_fuelPreferential > 4)) {
        this.fuelPreferential = 4;
    } else {
        this.fuelPreferential = _fuelPreferential;
    }

    // Inicialização de diversos atributos.
    this.finalizado = false;
    this.fuelPrice = _fuelPrice;
    this.personCapacity = _personCapacity;
    this.personNumber = _personNumber;
    this.speed = 600;
    this.rota = null;
    this.fuelTank = 10;
    this.carStatus = "esperando rota";
    this.drivingRepport = new ArrayList<DrivingData>();
	

    // Inicialização do objeto drivingDataAtual com valores iniciais.
    this.drivingDataAtual = new DrivingData(idCar, driverID, "esperando rota", 0, 0, 0, 
                                            0, 0, 0, 0, "", "", 
                                            0, 0, 0, 1, this.fuelType,
                                            this.fuelPrice, 0, 0, this.personCapacity, this.personNumber);
}


	@Override
	public void run() {
		try{
		if(pointer > 0){
			Thread.sleep(999999);
				
		}
		pointer++;
		System.err.println("iniciado ");
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(this.idCar);
		this.timerIni = System.currentTimeMillis();
		calcularParcials();
		
		SetFuelLevel sf = new SetFuelLevel(this, (0.001*speed)); 
		sf.start();

		try {
			
			// Cria um socket para conectar ao servidor da empresa
			socket = new Socket(this.companyServerHost, this.companyServerPort);
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
		
			// Enquanto o veículo não estiver finalizado, ele continua sua execução
			while (!finalizado) {
				// Envia o estado atual do veículo para a empresa
				output.writeUTF(criarJSONDrivingData(drivingDataAtual));
				//System.out.println(this.idCar + " aguardando rota");
				// Recebe a rota da empresa
				rota = extraiRota(input.readUTF());
		
				// Se a rota tiver ID igual a "-1," significa que não há mais rotas a receber
				if (rota.getID().equals("-1")) {
					System.out.println(this.idCar + " - Sem rotas a receber.");
					finalizado = true;
					break;
				}
		
				// Inicia um serviço de transporte com a nova rota
				ts = new TransportService(true, this.idCar, rota, this, this.sumo);
				ts.start();
		
				// Obtém a aresta final da rota
				String edgeFinal = this.getEdgeFinal();
				this.on_off = true;
		
				// Aguarda até que o veículo tenha partido (não esteja mais na aresta de partida)
				while (!Company.existeNoSumo(this.idCar, this.sumo)) {
					
					Thread.sleep(this.acquisitionRate);
				}

				System.out.println(this.idCar + " leu " + rota.getID());
				
				
				// Obtém a aresta atual do veículo
				String edgeAtual = (String) this.sumo.do_job_get(Vehicle.getRoadID(this.idCar));
		
				boolean initRoute = true;
		
				// Loop principal de simulação do veículo
				while (this.on_off) {
					// Se a rota está iniciando, obtém a posição inicial em coordenadas geográficas
					
					if (initRoute) {
						double[] coordGeo = calculaCoordGeograficas();
						latInicial = coordGeo[0];
						lonInicial = coordGeo[1];
						initRoute = false;
					}
		
					// Verifica se a rota foi concluída
					if (verificaRotaTerminada(edgeAtual, edgeFinal)) {
						System.out.println(this.idCar + " acabou a rota.");
						this.carStatus = "finalizado";
						output.writeUTF(criarJSONDrivingData(drivingDataAtual));
						this.on_off = false;
						break;
					}
		
					Thread.sleep(this.acquisitionRate);
		
					if (!verificaRotaTerminada(edgeAtual, edgeFinal)) {
						double[] coordGeo = calculaCoordGeograficas();
						latAtual = coordGeo[0];
						lonAtual = coordGeo[1];
						atualizaSensores();
						executaReconciliacaoDeDados(); // comeca 
						if (carStatus != "abastecendo") {
							this.carStatus = "rodando";
						
						}
		
						output.writeUTF(criarJSONDrivingData(drivingDataAtual));
		
						
						if (this.carStatus.equals("finalizado")) {
							this.on_off = false;
							break;
						} else {
							edgeAtual = (String) this.sumo.do_job_get(Vehicle.getRoadID(this.idCar));
						}
					}
				}
				System.out.println(this.idCar + " off.");
				System.out.println("Tempo de rodagem "+ (drivingDataAtual.getTimeStamp() - this.timerIni)/1000 + " "+getTempoAtualPercursoSegundos());
				
				if (!finalizado) {
					this.carStatus = "aguardando";
				}
		
				if (finalizado) {
					this.carStatus = "encerrado";
				}
			}
		
			
			//System.out.println("Encerrando: " + idCar);
			input.close();
			output.close();
			socket.close();
			this.ts.setTerminado(true);
		} catch (Exception e) {
			e.printStackTrace();
		}		

		System.out.println(this.idCar + " encerrado.");
	} 
  //parte 2
	private void calcularParcials(){
		for(int i = 0;i < numeroDeParcials; i++) {
			if(this.carStatus.equals( "aguardando")) {
				listaParcialDistancia.add(getParcialIdealDistancia() * i);
				listaParcialTempo.add(getParcialIdealTempo() * i);
			}
		}
	}

	public void recalcularParcials(){ // vai rodar 10
		for(int i = numeroDeParcialsExecutadas; i < numeroDeParcials;i++){
			Integer distanciaAPercorre = this.getDistanciaApercorrer();
			Integer tempoAPercorrer = this.getTempoApercorrer();
			Integer parciaisApercorrer = numeroDeParcials - numeroDeParcialsExecutadas;
			System.err.println(((distanciaAPercorre / parciaisApercorrer) * i) +" Testes");
			
			listaParcialDistancia.add(i, ((distanciaAPercorre / parciaisApercorrer) * i));
			listaParcialTempo.add(i, ((tempoAPercorrer / parciaisApercorrer) * i));

		}
		
		ajustarVelocidade();
	}

	public Integer getDistanciaApercorrer(){
		return (distanciaRota - (int) Math.round(this.getDistance()));
	}

	public Integer getTempoApercorrer(){
		return (tempoPrevisto - this.getTempoAtualPercursoSegundos());
	}

	private void validarParcials(){
		//ParcialDistancia
		System.err.println("Validando parciais");
		if(!isDistanciaDentroDaMargem() || !isTempoDentroDaMargem()){
			recalcularParcials();
		}

		numeroDeParcialsExecutadas++;
	}

	public void ajustarVelocidade(){
		this.speed = getDistanciaApercorrer() / getTempoApercorrer();
		System.err.println("Velocidade ajustada para "+ this.speed);
	}

	private boolean isTempoDentroDaMargem(){
		return isValorDentroDaMargem(getTempoAtualPercursoSegundos(), getParcialIdealTempo(), getValorDeMargemTempo());
	}

	private boolean isDistanciaDentroDaMargem(){
		return isValorDentroDaMargem((int)Math.round(this.getDistance()), getParcialIdealDistancia(), getValorDeMargemDistancia());
	}

	private Integer getParcialIdealTempo(){ // se for =o vai chamar o recalcula 
		if(listaParcialTempo.size() ==0 ) {
			recalcularParcials();
		}
		return listaParcialTempo.get(numeroDeParcialsExecutadas);
	}
	
	private Integer getParcialIdealDistancia(){
		if(listaParcialDistancia.size() ==0 ) {
			recalcularParcials();
		}
		return listaParcialDistancia.get(numeroDeParcialsExecutadas);
	}

	public Integer getValorDeMargemTempo(){
		return tempoPrevisto / Car.valorPercentualMargem;
	}

	public Integer getValorDeMargemDistancia(){
		return distanciaRota / Car.valorPercentualMargem;
	}

	public boolean isValorDentroDaMargem(Integer valor1, Integer valor2,Integer valorMargem){

		return  Math.abs((valor1 - valor2)) <= valorMargem ? true: false;
	}


	public void executaReconciliacaoDeDados(){
		
		Collections.reverse(listaParcialDistancia);
		System.out.println(this.getDistance() + " "+listaParcialDistancia.get(numeroDeParcialsExecutadas) );
		if(this.getDistance() > listaParcialDistancia.get(numeroDeParcialsExecutadas) 
			&& this.getDistance() < listaParcialDistancia.get((1 + numeroDeParcialsExecutadas))) {
				validarParcials();
			}
	}

	public ArrayList<Object> reverse(ArrayList<Object> list) {
		if(list.size() > 1) {                   
			Object value = list.remove(0);
			reverse(list);
			list.add(value);
		}
		return list;
	}

	public Integer getTempoAtualPercursoSegundos(){
		Long retu  = ((System.currentTimeMillis() - this.timerIni) /1000);
		return retu.intValue();
	}

	public double getDistance(){
		return drivingDataAtual.getOdometer();
	}

	private void atualizaSensores() {
		try {
			if (!this.getSumo().isClosed()) {
				
				SumoPosition2D sumoPosition2D;
				sumoPosition2D = (SumoPosition2D) sumo.do_job_get(Vehicle.getPosition(this.idCar));
	
				
				drivingDataAtual = new DrivingData(
					this.idCar, this.driverID, this.carStatus, this.latInicial, this.lonInicial,
					this.latAtual, this.lonAtual,
					
					System.currentTimeMillis(), sumoPosition2D.x, sumoPosition2D.y,
					(String) this.sumo.do_job_get(Vehicle.getRoadID(this.idCar)),
					(String) this.sumo.do_job_get(Vehicle.getRouteID(this.idCar)),
					(double) this.sumo.do_job_get(Vehicle.getSpeed(this.idCar)),
					(double) this.sumo.do_job_get(Vehicle.getDistance(this.idCar)),
	
					(double) this.sumo.do_job_get(Vehicle.getFuelConsumption(this.idCar)),
					1/*averageFuelConsumption (calcular)*/,
					
					
					this.fuelType, this.fuelPrice,
	
					(double) this.sumo.do_job_get(Vehicle.getCO2Emission(this.idCar)),
					
	
					(double) this.sumo.do_job_get(Vehicle.getHCEmission(this.idCar)),
					
					
					this.personCapacity,
					
					
					this.personNumber
					
				);
	
				
				this.drivingRepport.add(drivingDataAtual);
				
				
				if (carStatus != "abastecendo") {
					this.sumo.do_job_set(Vehicle.setSpeed(this.idCar, speed));
					this.sumo.do_job_set(Vehicle.setSpeedMode(this.idCar, 0));
				}
	
			} else {
				
				this.on_off = false;
				System.out.println("SUMO is closed...");
			}
		} catch (Exception e) {
			
		}
	}
	public class SetFuelLevel extends Thread {
		Car car;       
		double litros;  
	
		public SetFuelLevel(Car _car, double _litros) {
			this.car = _car;     
			this.litros = _litros; 
		}
	
		@Override
		public void run() {
			try {
				boolean toStart = true; 
				while (!car.getFinalizado()) { 
					
					if (toStart) {
						Thread.sleep(200); 
						toStart = false;
					}
	
					while (car.isOn_off()) { 
						if (car.getSpeed() > 0) { 
							car.gastaCombustivel(litros); 
						}
						Thread.sleep(1000); 
					}
	
					if (!car.isOn_off()) {
						toStart = true; 
					}
				}
	
				System.out.println("Finalizando SetFuelLevel"); 
	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String getCarStatus() {
		return carStatus;
	}

	public Rota getRota() {
		return rota;
	}

	public boolean isOn_off() {
		return this.on_off;
	}

	public void setOn_off(boolean _on_off) {
		this.on_off = _on_off;
	}

	public boolean getFinalizado() {
		return finalizado;
	}

	public void setFinalizado(boolean _finalizado) {
		this.finalizado = _finalizado;
	}

	public long getAcquisitionRate() {
		return this.acquisitionRate;
	}

	public void setAcquisitionRate(long _acquisitionRate) {
		this.acquisitionRate = _acquisitionRate;
	}

	public String getIdCar() {
		return this.idCar;
	}

	public SumoTraciConnection getSumo() {
		return this.sumo;
	}

	public int getFuelType() {
		return this.fuelType;
	}

	public void setFuelType(int _fuelType) {
		if((_fuelType < 0) || (_fuelType > 4)) {
			this.fuelType = 4;
		} else {
			this.fuelType = _fuelType;
		}
	}

	
	public double getFuelPrice() {
		return this.fuelPrice;
	}

	
	public void setFuelPrice(double _fuelPrice) {
		this.fuelPrice = _fuelPrice;
	}

	
	public void gastaCombustivel(double litros) {
		if (fuelTank >= litros) {
			fuelTank -= litros;
		} else {
			try {
				pararCarro();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	public double getNivelDoTanque() {
		return this.fuelTank;
	}

	public void abastecido(double litros) throws Exception{
		this.fuelTank += litros;
		carStatus = "rodando";
		voltarAndar();
	}

	
	public int getFuelPreferential() {
		return this.fuelPreferential;
	}

	
	public void setFuelPreferential(int _fuelPreferential) {
		if((_fuelPreferential < 0) || (_fuelPreferential > 4)) {
			this.fuelPreferential = 4;
		} else {
			this.fuelPreferential = _fuelPreferential;
		}
	}

	public int getPersonCapacity() {
		return this.personCapacity;
	}

	public int getPersonNumber() {
		return this.personNumber;
	}


	public void voltarAndar() throws Exception {
		this.speed = setRandomSpeed();
		this.sumo.do_job_set(Vehicle.setSpeed(this.idCar, speed));
	}

	public double setRandomSpeed(){
		Random random = new Random();
		double range = 25 - 15;
		double scaled = random.nextDouble() * range;
		double generatedNumber = scaled + 15;
		return generatedNumber;
	}

	
	public double getSpeed() throws Exception{
		return (double) this.sumo.do_job_get(Vehicle.getSpeed(this.idCar));
	}

	
	public void pararCarro() throws Exception{
		this.sumo.do_job_set(Vehicle.setSpeedMode(this.idCar, 0));
		this.sumo.do_job_set(Vehicle.setSpeed(this.idCar, 0));
	}

	
	public void stopToFuel() throws Exception{
		carStatus = "abastecendo";
		pararCarro();
	}


	
	private String getEdgeFinal() {
		SumoStringList edge = new SumoStringList();
		edge.clear();
		String aux = this.rota.getEdges();
		for(String e : aux.split(" ")) {
			edge.add(e);
		}
		return edge.get(edge.size() - 1);
	}

	
	private boolean verificaRotaTerminada(String _edgeAtual, String _edgeFinal) throws Exception {
		
		SumoStringList lista = (SumoStringList) this.sumo.do_job_get(Vehicle.getIDList());

		
		if (!lista.contains(idCar) && (_edgeFinal.equals(_edgeAtual))) {
			return true;
		} else {
			return false;
		}
	}

	
	private double[] calculaCoordGeograficas() throws Exception {
		SumoPosition2D sumoPosition2D = (SumoPosition2D) sumo.do_job_get(Vehicle.getPosition(this.idCar));

		double x = sumoPosition2D.x; 
		double y = sumoPosition2D.y; 

		double raioTerra = 6871000; 
		double latRef = -22.7667898; 
		double lonRef = -45.40890787; 

		double lat = latRef + (y / raioTerra) * (180 / Math.PI);
		double lon = lonRef + (x / raioTerra) * (180 / Math.PI) / Math.cos(latRef * Math.PI / 180);

		double[] coordGeo = new double[] { lat, lon };
		return coordGeo;
	}

	private Rota extraiRota(String rotaJSON) {
		JSONObject rotaJSONObj = new JSONObject(rotaJSON);
		Rota rota = new Rota(rotaJSONObj.getString("ID da Rota"), rotaJSONObj.getString("Edges"));
		return rota;
	}

	private String criarJSONDrivingData(DrivingData drivingData) {
		JSONObject drivingDataJSON = new JSONObject();
		drivingDataJSON.put("Car ID", drivingData.getCarID());
		drivingDataJSON.put("Driver ID", drivingData.getDriverID());
		drivingDataJSON.put("Car Status", drivingData.getCarStatus());
		drivingDataJSON.put("Latitude Inicial", drivingData.getLatInicial());
		drivingDataJSON.put("Longitude Inicial", drivingData.getLonInicial());
		drivingDataJSON.put("Latitude Atual", drivingData.getLatAtual());
		drivingDataJSON.put("Longitude Atual", drivingData.getLonAtual());
		drivingDataJSON.put("TimeStamp", drivingData.getTimeStamp());
		drivingDataJSON.put("X_Position", drivingData.getX_Position());
		drivingDataJSON.put("Y_Position", drivingData.getY_Position());
		drivingDataJSON.put("RoadIDSUMO", drivingData.getRoadIDSUMO());
		drivingDataJSON.put("RouteIDSUMO", drivingData.getRouteIDSUMO());
		drivingDataJSON.put("Speed", drivingData.getSpeed());
		drivingDataJSON.put("Odometer", drivingData.getOdometer());
		drivingDataJSON.put("FuelConsumption", drivingData.getFuelConsumption());
		drivingDataJSON.put("AverageFuelConsumption", drivingData.getAverageFuelConsumption());
		drivingDataJSON.put("FuelType", drivingData.getFuelType());
		drivingDataJSON.put("FuelPrice", drivingData.getFuelPrice());
		drivingDataJSON.put("Co2Emission", drivingData.getCo2Emission());
		drivingDataJSON.put("HCEmission", drivingData.getHCEmission());
		drivingDataJSON.put("PersonCapacity", drivingData.getPersonCapacity());
		drivingDataJSON.put("PersonNumber", drivingData.getPersonNumber());
		return drivingDataJSON.toString();
	}


}