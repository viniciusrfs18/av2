package io.sim.Transport.CarDriver;

import de.tudresden.sumo.cmd.Vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.MobilityCompany.Company;
import io.sim.Transport.TransportService;
import io.sim.Transport.Fuel.SetFuelLevel;
import io.sim.Transport.Rotas.Rota;

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

	public Car(boolean _on_off, String _idCar, SumoColor _colorCar, String _driverID, SumoTraciConnection _sumo, long _acquisitionRate,
            int _fuelType, int _fuelPreferential, double _fuelPrice, int _personCapacity, int _personNumber, String _companyServerHost, 
            int _companyServerPort) throws Exception {
    // Inicialização do construtor da classe Car com diversos parâmetros.

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
    this.speed = setRandomSpeed();
    this.rota = null;
    this.fuelTank = 10;
    this.carStatus = "aguardando";
    this.drivingRepport = new ArrayList<DrivingData>();

    // Inicialização do objeto drivingDataAtual com valores iniciais.
    this.drivingDataAtual = new DrivingData(idCar, driverID, "aguardando", 0, 0, 0, 
                                            0, 0, 0, 0, "", "", 
                                            0, 0, 0, 1, this.fuelType,
                                            this.fuelPrice, 0, 0, this.personCapacity, this.personNumber);
}


	@Override
	public void run() {
		System.out.println(this.idCar + " iniciando");
		
		SetFuelLevel sf = new SetFuelLevel(this, (0.001*speed)); //VERIFICAR CONSUMO
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
				System.out.println(this.idCar + " aguardando rota");
				// Recebe a rota da empresa
				rota = extraiRota(input.readUTF());
		
				// Se a rota tiver ID igual a "-1," significa que não há mais rotas a receber
				if (rota.getID().equals("-1")) {
					System.out.println(this.idCar + " - Sem rotas a receber.");
					finalizado = true;
					break;
				}
		
				System.out.println(this.idCar + " leu " + rota.getID());
		
				// Inicia um serviço de transporte com a nova rota
				ts = new TransportService(true, this.idCar, rota, this, this.sumo);
				ts.start();
		
				// Obtém a aresta final da rota
				String edgeFinal = this.getEdgeFinal();
				this.on_off = true;
		
				// Aguarda até que o veículo tenha partido (não esteja mais na aresta de partida)
				while (!Company.stillOnSUMO(this.idCar, this.sumo)) {
					Thread.sleep(this.acquisitionRate);
				}
		
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
		
					// Atualiza a posição do veículo, sensores e estado
					if (!verificaRotaTerminada(edgeAtual, edgeFinal)) {
						double[] coordGeo = calculaCoordGeograficas();
						latAtual = coordGeo[0];
						lonAtual = coordGeo[1];
						atualizaSensores();
		
						if (carStatus != "abastecendo") {
							this.carStatus = "rodando";
						}
		
						output.writeUTF(criarJSONDrivingData(drivingDataAtual));
		
						// Se o veículo estiver marcado como "finalizado," encerra a execução
						if (this.carStatus.equals("finalizado")) {
							this.on_off = false;
							break;
						} else {
							edgeAtual = (String) this.sumo.do_job_get(Vehicle.getRoadID(this.idCar));
						}
					}
				}
				System.out.println(this.idCar + " off.");
		
				// Atualiza o estado do veículo com base na finalização
				if (!finalizado) {
					this.carStatus = "aguardando";
				}
		
				if (finalizado) {
					this.carStatus = "encerrado";
				}
			}
		
			// Encerra os canais e a conexão com a empresa
			System.out.println("Encerrando: " + idCar);
			input.close();
			output.close();
			socket.close();
			this.ts.setTerminado(true);
		} catch (Exception e) {
			e.printStackTrace();
		}		

		System.out.println(this.idCar + " encerrado.");
	}

	private void atualizaSensores() {
		try {
			if (!this.getSumo().isClosed()) {
				// Declaração e inicialização do objeto SumoPosition2D para obter informações de posição do veículo.
				SumoPosition2D sumoPosition2D;
				sumoPosition2D = (SumoPosition2D) sumo.do_job_get(Vehicle.getPosition(this.idCar));
	
				// Criação dos dados de direção do veículo com base nas informações obtidas.
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
					// Consumo de combustível do veículo durante este intervalo de tempo.
					
					this.fuelType, this.fuelPrice,
	
					(double) this.sumo.do_job_get(Vehicle.getCO2Emission(this.idCar)),
					// Emissão de CO2 do veículo durante este intervalo de tempo.
	
					(double) this.sumo.do_job_get(Vehicle.getHCEmission(this.idCar)),
					// Emissão de HC (hidrocarbonetos) do veículo durante este intervalo de tempo.
					
					this.personCapacity,
					// O número total de pessoas que podem viajar neste veículo.
					
					this.personNumber
					// O número total de pessoas que estão viajando neste veículo.
				);
	
				// Adição dos dados de direção atual à lista de relatórios de direção do veículo.
				this.drivingRepport.add(drivingDataAtual);
				
				// Verificação e atualização da velocidade do veículo se não estiver em processo de abastecimento.
				if (carStatus != "abastecendo") {
					this.sumo.do_job_set(Vehicle.setSpeed(this.idCar, speed));
					this.sumo.do_job_set(Vehicle.setSpeedMode(this.idCar, 0));
				}
	
			} else {
				// Se a conexão com o SUMO estiver fechada, o veículo é desligado.
				this.on_off = false;
				System.out.println("SUMO is closed...");
			}
		} catch (Exception e) {
			e.printStackTrace();
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

	// Método que retorna o preço do combustível do veículo
	public double getFuelPrice() {
		return this.fuelPrice;
	}

	// Método para definir o preço do combustível para o veículo
	public void setFuelPrice(double _fuelPrice) {
		this.fuelPrice = _fuelPrice;
	}

	// Método que simula o gasto de combustível pelo veículo
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

	// Método que retorna o nível atual do tanque de combustível
	public double getNivelDoTanque() {
		return this.fuelTank;
	}

	// Método que simula o abastecimento do veículo
	public void abastecido(double litros) throws Exception{
		this.fuelTank += litros;
		carStatus = "rodando";
		voltarAndar();
	}

	// Método que retorna a cor do veículo
	public SumoColor getColorCar() {
		return this.colorCar;
	}

	// Método que retorna o tipo de combustível preferencial do veículo
	public int getFuelPreferential() {
		return this.fuelPreferential;
	}

	// Método para definir o tipo de combustível preferencial do veículo
	public void setFuelPreferential(int _fuelPreferential) {
		if((_fuelPreferential < 0) || (_fuelPreferential > 4)) {
			this.fuelPreferential = 4;
		} else {
			this.fuelPreferential = _fuelPreferential;
		}
	}

	// Método que retorna a capacidade de passageiros do veículo
	public int getPersonCapacity() {
		return this.personCapacity;
	}

	// Método que retorna o número de passageiros a bordo do veículo
	public int getPersonNumber() {
		return this.personNumber;
	}

	// Método que simula a ação de fazer o veículo voltar a andar
	public void voltarAndar() throws Exception {
		this.speed = setRandomSpeed();
		this.sumo.do_job_set(Vehicle.setSpeed(this.idCar, speed));
	}

	// Método que gera uma velocidade aleatória dentro de um intervalo
	public double setRandomSpeed(){
		Random random = new Random();
		double range = 25 - 15;
		double scaled = random.nextDouble() * range;
		double generatedNumber = scaled + 15;
		return generatedNumber;
	}

	// Método que retorna a velocidade atual do veículo
	public double getSpeed() throws Exception{
		return (double) this.sumo.do_job_get(Vehicle.getSpeed(this.idCar));
	}

	// Método que simula a ação de parar o veículo
	public void pararCarro() throws Exception{
		this.sumo.do_job_set(Vehicle.setSpeedMode(this.idCar, 0));
		this.sumo.do_job_set(Vehicle.setSpeed(this.idCar, 0));
	}

	// Método que simula o processo de parar o veículo para abastecimento
	public void stopToFuel() throws Exception{
		carStatus = "abastecendo";
		pararCarro();
	}


	// Pega a última posição da Rota
	// Método auxiliar para verificar se a rota terminou
	private String getEdgeFinal() {
		SumoStringList edge = new SumoStringList();
		edge.clear();
		String aux = this.rota.getEdges();
		for(String e : aux.split(" ")) {
			edge.add(e);
		}
		return edge.get(edge.size() - 1);
	}

	// Verifica se a rota atual terminou
	private boolean verificaRotaTerminada(String _edgeAtual, String _edgeFinal) throws Exception {
		// Cria lista de IDs dos carros do SUMO
		SumoStringList lista = (SumoStringList) this.sumo.do_job_get(Vehicle.getIDList());

		// Verificação dupla para determinar o término da Rota
		if (!lista.contains(idCar) && (_edgeFinal.equals(_edgeAtual))) {
			return true;
		} else {
			return false;
		}
	}

	// Método que calcula as coordenadas geográficas (latitude e longitude) do veículo
	private double[] calculaCoordGeograficas() throws Exception {
		SumoPosition2D sumoPosition2D = (SumoPosition2D) sumo.do_job_get(Vehicle.getPosition(this.idCar));

		double x = sumoPosition2D.x; // Obtém a coordenada X em metros
		double y = sumoPosition2D.y; // Obtém a coordenada Y em metros

		double raioTerra = 6371000; // Define o raio médio da Terra em metros

		double latRef = -22.986731; // Latitude de referência
		double lonRef = -43.217054; // Longitude de referência

		// Conversão de coordenadas em metros para graus (latitude e longitude)
		double lat = latRef + (y / raioTerra) * (180 / Math.PI);
		double lon = lonRef + (x / raioTerra) * (180 / Math.PI) / Math.cos(latRef * Math.PI / 180);

		double[] coordGeo = new double[] { lat, lon };
		return coordGeo;
	}

	// Método para extrair uma rota a partir de dados JSON
	private Rota extraiRota(String rotaJSON) {
		JSONObject rotaJSONObj = new JSONObject(rotaJSON);
		Rota rota = new Rota(rotaJSONObj.getString("ID da Rota"), rotaJSONObj.getString("Edges"));
		return rota;
	}

	// Método para criar um objeto JSON a partir dos dados de condução (DrivingData)
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