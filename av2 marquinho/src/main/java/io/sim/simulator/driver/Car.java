package io.sim.simulator.driver;

import de.tudresden.sumo.cmd.Vehicle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.simulator.company.Company;
import io.sim.simulator.company.Rota;
import io.sim.simulator.comunication.AESencrypt;
import io.sim.simulator.comunication.JSONConverter;

/**
 * 		A classe Car representa um veículo autônomo que se move em um ambiente de simulação.
 * Ela gerencia a comunicação com um servidor da empresa, atualiza sensores, controla o status do veículo e registra informações 
 * de condução.
 */

public class Car extends Vehicle implements Runnable {

	// atributos de cliente
	private Socket socket;              	// Referência para o socket de comunicação com o servidor da empresa.
	private int companyServerPort;      	// Porta do servidor da empresa para a comunicação.
	private String companyServerHost;   	// Host ou endereço do servidor da empresa.
	private DataInputStream entrada;    	// Fluxo de entrada para receber dados do servidor.
	private DataOutputStream saida;     	// Fluxo de saída para enviar dados para o servidor.

	// atributos da classe
	private String idCar;               	// Identificador exclusivo do carro na simulação.
	private SumoColor colorCar;         	// Cor do carro na simulação.
	private String driverID;            	// Identificador exclusivo do motorista associado ao carro.
	private SumoTraciConnection sumo;   	// Conexão com o simulador SUMO.
	private boolean on_off;            		// Estado ligado/desligado do veículo (true: ligado, false: desligado).
	private boolean encerrado;         		// Indica se o carro terminou seu funcionamento (true: terminado).
	private long acquisitionRate;       	// Taxa de aquisição de dados dos sensores do veículo.
	private int fuelType;              		// Tipo de combustível (1-diesel, 2-gasoline, 3-ethanol, 4-hybrid).
	private int fuelPreferential;       	// Tipo de combustível preferencial.
	private double fuelPrice;           	// Preço do combustível por litro.
	private int personCapacity;        		// Capacidade máxima de passageiros no veículo.
	private int personNumber;          		// Número atual de passageiros no veículo.
	private double speed;              		// Velocidade do veículo.
	private Rota rota;                 		// Rota atual do veículo.
	private boolean considerarConsumoComb;
	private double fuelTank;           		// Nível atual do tanque de combustível.
	private double maxFuelCapacity;    		// Capacidade máxima do tanque de combustível.
	private double consumoCombustivel; 		// Consumo de combustível.
	private String carStatus;          		// Status atual do veículo.
	private double latAnt;         	   		// Latitude anterior.
	private double lonAnt;             		// Longitude anterior.
	private double latAtual;           		// Latitude atual.
	private double lonAtual;           		// Longitude atual.
	private int precisaAttExcel;
	private DrivingData drivingDataAtual; 	// Dados de condução atuais do veículo.
	private ArrayList<DrivingData> drivingRepport; // Dados de condução registrados.
	private TransportService ts;      		// Serviço de transporte associado ao veículo.


	public Car(boolean _on_off, String _idCar, SumoColor _colorCar, String _driverID, SumoTraciConnection _sumo, long _acquisitionRate,
			int _fuelType, int _fuelPreferential, double _fuelPrice, boolean _considerarConsumoComb, int _personCapacity, int _personNumber, String _companyServerHost, 
			int _companyServerPort) throws Exception {

		this.companyServerPort = _companyServerPort;
		this.companyServerHost = _companyServerHost;
		this.on_off = _on_off;
		this.idCar = _idCar;
		this.colorCar = _colorCar;
		this.driverID = _driverID;
		this.sumo = _sumo;
		this.acquisitionRate = _acquisitionRate;
		
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

		this.encerrado = false;
		this.fuelPrice = _fuelPrice;
		this.considerarConsumoComb = _considerarConsumoComb;
		this.personCapacity = _personCapacity;
		this.personNumber = _personNumber;
		this.speed = 30;
		this.rota = null;
		this.fuelTank = 10000;
		this.consumoCombustivel = 0;
		this.maxFuelCapacity = 55000;
		this.carStatus = "esperando";
		this.drivingRepport = new ArrayList<DrivingData>();
		this.precisaAttExcel = 0;
		
		this.drivingDataAtual = new DrivingData(idCar, driverID, "esperando", 0, 0, 0, 0,  precisaAttExcel,
												0 , "", 0, 0, 0, this.fuelType, 0);
	}

	@Override
	public void run() {
		System.out.println(this.idCar + " iniciando");
		// Inicializa a Thread responsável por gastar o combustível do carro
		if (considerarConsumoComb) {
			SpendFuel sf = new SpendFuel(this);
			sf.start();
		}

		try {
			// Configura a conexão com o servidor da empresa
            socket = new Socket(this.companyServerHost, this.companyServerPort);
            entrada = new DataInputStream(socket.getInputStream());
			saida = new DataOutputStream(socket.getOutputStream());

			// System.out.println(this.idCar + " conectado!!");
			int numBytesMsg;
			byte[] mensagemEncriptada;

			// Loop principal do sistema
			while (!encerrado) {
				// Manda "esperando" da primeira vez, e espera o recebimento de uma rota
				mensagemEncriptada = AESencrypt.encripta(JSONConverter.criarJSONDrivingData(drivingDataAtual));
				saida.write(AESencrypt.encripta(JSONConverter.criaJSONTamanhoBytes(mensagemEncriptada.length)));
				saida.write(mensagemEncriptada);

				System.out.println(this.idCar + " esperando rota");

				// Recebe a rota
				numBytesMsg = JSONConverter.extraiTamanhoBytes(AESencrypt.decripta(entrada.readNBytes(AESencrypt.getTamNumBytes())));
                rota = JSONConverter.extraiRota(AESencrypt.decripta(entrada.readNBytes(numBytesMsg)));

				// Verifica se é uma rota válida
				if(rota.getID().equals("-1")) {
					// Caso não seja, isso significa que não há mais rotas e termina a execução
					System.out.println(this.idCar +" - Sem rotas a receber.");
					encerrado = true;
					break;
				}

				System.out.println("------------> " + this.idCar + " está iniciando a rota: " + rota.getID());

				// Cria um novo transport Service que criará o Carro no SUMO
				ts = new TransportService(true, this.idCar, rota, this, this.sumo);
				ts.start();
			
				// Pega a edge final da rota para usar em verificações
				String edgeFinal = this.getEdgeFinal(); 
				this.on_off = true; // Liga o carro

				// Verifica se o carro já está no ambiente do SUMO
				while(!Company.estaNoSUMO(this.idCar, this.sumo)) {
					// Caso não esteja, espera!
					Thread.sleep(this.acquisitionRate);
				}

				// Pega a edge inicial da rota para usar em verificações
				String edgeAtual = (String) this.sumo.do_job_get(Vehicle.getRoadID(this.idCar));
				
				ArrayList<String> edges = Rota.criaListaEdges(rota);
				System.out.println("A rota " + rota.getID() + " precisa ser dividida em " + edges.size()/2 + " parciais");

				int i = 0;
				this.precisaAttExcel = 1;
				boolean initRoute = true;
				while (this.on_off) {
					//System.out.println("Car: " + carStatus);
					if (carStatus != "abastecendo") {
						this.carStatus = "rodando";
					}

					// Calcula a latitude e longitude iniciais da Rota Atual
					if (initRoute) {
						double[] coordGeo = calculaCoordGeograficas();
						latAnt = coordGeo[0];
						lonAnt = coordGeo[1];
						initRoute = false; // Só executa uma vez por rota
					}

					if(!edges.isEmpty()) {
						if(edgeAtual.equals(edges.get(0))){
							String edge1 = edges.remove(0);
							String texto;
							if (!edges.isEmpty()){
								String edge2 = edges.remove(0);
								texto = "Partição " + i + ": " + edge1 + " " + edge2 + " percorridas";
							} else {
								texto = "Partição " + i + ": " + edge1 + " percorrida";
							}
							this.precisaAttExcel = 1;
							i++;
							System.out.println(texto);
						}
					}

					// Se a Rota terminou 
					if(verificaRotaTerminada(edgeAtual, edgeFinal)) {
						System.out.println(this.idCar + " acabou a rota.");
						this.precisaAttExcel = 1;
						this.carStatus = "finalizado";

						// Manda informações com o status "finalizado"
						drivingDataAtual.setCarStatus(this.carStatus);
						mensagemEncriptada = AESencrypt.encripta(JSONConverter.criarJSONDrivingData(drivingDataAtual));
						saida.write(AESencrypt.encripta(JSONConverter.criaJSONTamanhoBytes(mensagemEncriptada.length)));
						saida.write(mensagemEncriptada);
						this.on_off = false;
						break; // Finaliza o loop
					} 

					// Garante que os dados sejam gerados de acordo com a taxa de aquisição
					Thread.sleep(this.acquisitionRate);
					
					// Se a Rota ainda não terminou 
					if(!verificaRotaTerminada(edgeAtual, edgeFinal)) {
						// System.out.println(this.idCar + " -> edge atual: " + edgeAtual);
						// System.out.println(this.idCar + " -> fuelTank: " + fuelTank);

						// Atualiza sensores e informações de condução
						atualizaSensores(edgeAtual, edgeFinal);
						
						// Se o status do carro não foi finalizado no "atualizaSensores" ele continua com o processo de obtenção de dados
						if(this.carStatus.equals("finalizado")) {
							this.on_off = false;
							break;
						} else {
							// Atualiza as latitude e longitudes anteriores
							latAnt = latAtual;
							lonAnt = lonAtual;

							// Manda informações com o status atualizado
							mensagemEncriptada = AESencrypt.encripta(JSONConverter.criarJSONDrivingData(drivingDataAtual));
							saida.write(AESencrypt.encripta(JSONConverter.criaJSONTamanhoBytes(mensagemEncriptada.length)));
							saida.write(mensagemEncriptada);
							
							precisaAttExcel = 0;

							if(!verificaRotaTerminada(edgeAtual, edgeFinal)) {
								edgeAtual = (String) this.sumo.do_job_get(Vehicle.getRoadID(this.idCar)); // TODO: ERRO FREQUENTE AQUI
							}
						}
					}
				}
				System.out.println(this.idCar + " off.");

				// Se não está ecerrado o carro está esperando outra rota
				if(!encerrado) {
					this.carStatus = "esperando";
					drivingDataAtual.setCarStatus(this.carStatus);
				}

				// Se está ecerrado a thread do carro pode finalizar
				if(encerrado) {
					this.carStatus = "encerrado";
					drivingDataAtual.setCarStatus(this.carStatus);
				}
			}

			// Finalizando o carro
			System.out.println("Encerrando: " + idCar);
			entrada.close();
			saida.close();
			socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

		System.out.println(this.idCar + " encerrado.");
	}

	private void atualizaSensores(String edgeAtual, String edgeFinal) {
		try {
			if (!this.getSumo().isClosed()) {
				double[] coordGeo = calculaCoordGeograficas(); // Calcula as coordenadas geográficas atuais.
				latAtual = coordGeo[0];
				lonAtual = coordGeo[1];

				// System.out.println("CarID: " + this.getIdCar());
				// System.out.println("RoadID: " + (String) this.sumo.do_job_get(Vehicle.getRoadID(this.idCar)));
				// System.out.println("RouteID: " + (String) this.sumo.do_job_get(Vehicle.getRouteID(this.idCar)));
				// System.out.println("RouteIndex: " + this.sumo.do_job_get(Vehicle.getRouteIndex(this.idCar)));

				while(!Company.estaNoSUMO(idCar, sumo)) {
					Thread.sleep(acquisitionRate/10);
				}
				
				// Atualiza os dados de condução do veículo.
				drivingDataAtual = new DrivingData(
						this.idCar, this.driverID, this.carStatus, this.latAnt, this.lonAnt,
						this.latAtual, this.lonAtual, precisaAttExcel,
						
						System.nanoTime(), (String) this.sumo.do_job_get(Vehicle.getRouteID(this.idCar)), 
						(double) this.sumo.do_job_get(Vehicle.getSpeed(this.idCar)), 
						0, // CarManipulator faz o Calculo e usa setDistance() no DrivingData
						this.consumoCombustivel, this.fuelType,
						(double) this.sumo.do_job_get(Vehicle.getCO2Emission(this.idCar)));
						// Vehicle's fuel consumption in ml/s during this time step,
						// to get the value for one step multiply with the step length; error value:
						// -2^30

						// Vehicle's CO2 emissions in mg/s during this time step,
						// to get the value for one step multiply with the step length; error value:
						// -2^30
						
						// 1/*averageFuelConsumption (calcular)*/,
				
				// Guarda a informação na lista de DrivingDatas
				this.drivingRepport.add(drivingDataAtual);
				
				// Se não está "abastecendo" seta sua velocidade padrão
				if ((carStatus != "abastecendo") && (!verificaRotaTerminada(edgeAtual, edgeFinal))) {
					this.setSpeed(speed);
				}

			} else {
				// Caso o SUMO feche
				this.on_off = false; // Desliga o carro
				this.carStatus = "finalizado"; // Finaliza o carro
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

	public String getDriverID() {
		return driverID;
	}

	public boolean isOn_off() {
		return this.on_off;
	}

	public void setOn_off(boolean _on_off) {
		this.on_off = _on_off;
	}

	public boolean getEncerrado() {
		return encerrado;
	}

	public void setEncerrado(boolean _encerrado) {
		this.encerrado = _encerrado;
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

	public void setConsumoCombustivel(double _consumoCombustivel) {
		consumoCombustivel = _consumoCombustivel;
	}

	// Gasta combustível do carro
	public void gastaCombustivel(double litros) {
		// Verifica se há combustível suficiente no tanque
		if (fuelTank >= litros) {
			fuelTank -= litros; // Subtrai a quantidade de litros consumidos do tanque
			setConsumoCombustivel(litros); // Atualiza o consumo de combustível
		} else {
			try {
				pararCarro(); // Para o veículo se não houver combustível suficiente
				setConsumoCombustivel(0); // Define o consumo de combustível como zero
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public double getNivelDoTanque() {
		return this.fuelTank;
	}

	public double getCapacidadeDoTanque() {
		return this.maxFuelCapacity;
	}

	public SumoColor getColorCar() {
		return this.colorCar;
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

	public double getSpeed() throws Exception{
		return (double) this.sumo.do_job_get(Vehicle.getSpeed(this.idCar));
	}

	public void setSpeed(double speed) throws Exception {           // TODO: ERRO FREQUENTE AQUI
		this.sumo.do_job_set(Vehicle.setSpeedMode(this.idCar, 27)); // [0 1 1 0 1 1] (binário) ou 27 (decimal)
		this.sumo.do_job_set(Vehicle.setSpeed(this.idCar, speed));
	}

	public void pararCarro() throws Exception{
		setSpeed(0);
	}

	// Para o carro e muda o status do carro para "abastecendo"
	public void preparaAbastecimento() throws Exception{
		carStatus = "abastecendo";
		drivingDataAtual.setCarStatus(this.carStatus);
		pararCarro();
	}

	// Adiciona litros ao tanque do carro, atualiza o status do carro para "rodando", faz o carro voltar a andar
	public void abastecido(double litros) throws Exception{
		fuelTank += litros;
		carStatus = "rodando";
		setSpeed(speed);
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

	// Calcula as coordenadas do carro no SUMO e retorna em um vetor de double
	private double[] calculaCoordGeograficas() throws Exception {
		SumoPosition2D sumoPosition2D = (SumoPosition2D) sumo.do_job_get(Vehicle.getPosition(this.idCar)); // TODO BOzaço aqui IMPORTANTE TRATAR ISSO

		double x = sumoPosition2D.x; // coordenada X em metros
		double y = sumoPosition2D.y; // coordenada Y em metros

		double raioTerra = 6371000; // raio médio da Terra em metros

		double latRef = -22.986731;
		double lonRef = -43.217054;

		// Conversão de metros para graus
		double lat = latRef + (y / raioTerra) * (180 / Math.PI);
		double lon = lonRef + (x / raioTerra) * (180 / Math.PI) / Math.cos(latRef * Math.PI / 180);

		double[] coordGeo = new double[] { lat, lon };
		return coordGeo;
	}

}