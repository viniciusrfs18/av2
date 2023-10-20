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
import io.sim.Crypto;
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
	private DataInputStream entrada;
	private DataOutputStream saida;
	
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

		this.finalizado = false;
		this.fuelPrice = _fuelPrice;
		this.personCapacity = _personCapacity;
		this.personNumber = _personNumber;
		this.speed = 100;
		this.rota = null;
		this.fuelTank = 10;
		this.carStatus = "aguardando";
		this.drivingRepport = new ArrayList<DrivingData>();
		
		this.drivingDataAtual = new DrivingData(idCar, driverID, "aguardando", 0, 0, 0, 
												0, 0, 0, 0, "", "", 
												0, 0, 0, 1, this.fuelType,
												this.fuelPrice,0, 0, this.personCapacity, this.personNumber);
		
	}

	@Override
	public void run() {
		System.out.println(this.idCar + " iniciando");
		SetFuelLevel sf = new SetFuelLevel(this, 0.01); //VERIFICAR CONSUMO
		sf.start();

		try {
            socket = new Socket(this.companyServerHost, this.companyServerPort);
            entrada = new DataInputStream(socket.getInputStream());
			saida = new DataOutputStream(socket.getOutputStream());

			int numBytesMsg;
			byte[] mensagemEncriptada;

			while (!finalizado) {
				// Recebendo Rota
				// Manda "aguardando" da primeira vez
				mensagemEncriptada = Crypto.encripta(criarJSONDrivingData(drivingDataAtual));
				saida.write(Crypto.encripta(criaJSONTamanhoBytes(mensagemEncriptada.length)));
				saida.write(mensagemEncriptada);

				System.out.println(this.idCar + " aguardando rota");
				numBytesMsg = extraiTamanhoBytes(Crypto.decripta(entrada.readNBytes(Crypto.getTamNumBytes())));
                rota = extraiRota(Crypto.decripta(entrada.readNBytes(numBytesMsg)));

				if(rota.getID().equals("-1")) {
					System.out.println(this.idCar +" - Sem rotas a receber.");
					finalizado = true;
					break;
				}

				System.out.println(this.idCar + " leu " + rota.getID());

				ts = new TransportService(true, this.idCar, rota, this, this.sumo);
				ts.start();
			
				String edgeFinal = this.getEdgeFinal(); 
				this.on_off = true;
				while(!Company.stillOnSUMO(this.idCar, this.sumo)) {
					Thread.sleep(this.acquisitionRate);
				}
				String edgeAtual = (String) this.sumo.do_job_get(Vehicle.getRoadID(this.idCar));

				boolean initRoute = true;
				while (this.on_off) {
					// Thread.sleep(this.acquisitionRate);
					if (initRoute) {
						double[] coordGeo = calculaCoordGeograficas();
						latInicial = coordGeo[0];
						lonInicial = coordGeo[1];
						initRoute = false;
					}

					if(verificaRotaTerminada(edgeAtual, edgeFinal)) {
						System.out.println(this.idCar + " acabou a rota.");
						//this.ts.setOn_off(false);
						this.carStatus = "finalizado";
						mensagemEncriptada = Crypto.encripta(criarJSONDrivingData(drivingDataAtual));
						saida.write(Crypto.encripta(criaJSONTamanhoBytes(mensagemEncriptada.length)));
						saida.write(mensagemEncriptada);
						this.on_off = false;
						break;
					} 

					Thread.sleep(this.acquisitionRate);
					
					if(!verificaRotaTerminada(edgeAtual, edgeFinal)) {
						
						double[] coordGeo = calculaCoordGeograficas();
						latAtual = coordGeo[0];
						lonAtual = coordGeo[1];
						atualizaSensores();

						if (carStatus != "abastecendo") {
							this.carStatus = "rodando";
						}
						
						saida.writeUTF(criarJSONDrivingData(drivingDataAtual));
						
						if(this.carStatus.equals("finalizado")) {
							this.on_off = false;
							break;
						} else {
							edgeAtual = (String) this.sumo.do_job_get(Vehicle.getRoadID(this.idCar));
						}
					}

				}
				System.out.println(this.idCar + " off.");

				if(!finalizado) {
					this.carStatus = "aguardando";
				}

				if(finalizado) {
					this.carStatus = "encerrado";
				}
			}

			System.out.println("Encerrando: " + idCar);
			entrada.close();
			saida.close();
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
				SumoPosition2D sumoPosition2D;
				sumoPosition2D = (SumoPosition2D) sumo.do_job_get(Vehicle.getPosition(this.idCar));

				// System.out.println("CarID: " + this.getIdCar());
				// System.out.println("RoadID: " + (String) this.sumo.do_job_get(Vehicle.getRoadID(this.idCar)));
				// System.out.println("RouteID: " + (String) this.sumo.do_job_get(Vehicle.getRouteID(this.idCar)));
				// System.out.println("RouteIndex: " + this.sumo.do_job_get(Vehicle.getRouteIndex(this.idCar)));
				
				// Criacao dos dados de conducao do veiculo
				drivingDataAtual = new DrivingData(
						this.idCar, this.driverID, this.carStatus, this.latInicial, this.lonInicial,
						this.latAtual, this.lonAtual,
						
						System.currentTimeMillis(), sumoPosition2D.x, sumoPosition2D.y,
						(String) this.sumo.do_job_get(Vehicle.getRoadID(this.idCar)),
						(String) this.sumo.do_job_get(Vehicle.getRouteID(this.idCar)),
						(double) this.sumo.do_job_get(Vehicle.getSpeed(this.idCar)),
						(double) this.sumo.do_job_get(Vehicle.getDistance(this.idCar)),

						(double) this.sumo.do_job_get(Vehicle.getFuelConsumption(this.idCar)),
						// Vehicle's fuel consumption in ml/s during this time step,
						// to get the value for one step multiply with the step length; error value:
						// -2^30
						
						1/*averageFuelConsumption (calcular)*/,

						this.fuelType, this.fuelPrice,

						(double) this.sumo.do_job_get(Vehicle.getCO2Emission(this.idCar)),
						// Vehicle's CO2 emissions in mg/s during this time step,
						// to get the value for one step multiply with the step length; error value:
						// -2^30

						(double) this.sumo.do_job_get(Vehicle.getHCEmission(this.idCar)),
						// Vehicle's HC emissions in mg/s during this time step,
						// to get the value for one step multiply with the step length; error value:
						// -2^30
						
						this.personCapacity,
						// the total number of persons that can ride in this vehicle
						
						this.personNumber
						// the total number of persons which are riding in this vehicle

				);

				// Criar relat�rio auditoria / alertas
				// velocidadePermitida = (double)
				// sumo.do_job_get(Vehicle.getAllowedSpeed(this.idSumoVehicle));
				//atualizaPlanilhaCar(drivingDataAtual);
				this.drivingRepport.add(drivingDataAtual);

				//System.out.println("Data: " + this.drivingRepport.size());
				// System.out.println("idCar = " + this.drivingRepport.get(this.drivingRepport.size() - 1).getCarID());
				//System.out.println(
				//		"timestamp = " + this.drivingRepport.get(this.drivingRepport.size() - 1).getTimeStamp());
				//System.out.println("X=" + this.drivingRepport.get(this.drivingRepport.size() - 1).getX_Position() + ", "
				//		+ "Y=" + this.drivingRepport.get(this.drivingRepport.size() - 1).getY_Position());
				// System.out.println("speed = " + this.drivingRepport.get(this.drivingRepport.size() - 1).getSpeed());
				// System.out.println("odometer = " + this.drivingRepport.get(this.drivingRepport.size() - 1).getOdometer());
				// System.out.println("Fuel Consumption = "
				// 		+ this.drivingRepport.get(this.drivingRepport.size() - 1).getFuelConsumption());
				//System.out.println("Fuel Type = " + this.fuelType);
				//System.out.println("Fuel Price = " + this.fuelPrice);

				// System.out.println(
				// 		"CO2 Emission = " + this.drivingRepport.get(this.drivingRepport.size() - 1).getCo2Emission());

				//System.out.println();
				//System.out.println("************************");
				//System.out.println("testes: ");
				//System.out.println("getAngle = " + (double) sumo.do_job_get(Vehicle.getAngle(this.idCar)));
				//System.out
				//		.println("getAllowedSpeed = " + (double) sumo.do_job_get(Vehicle.getAllowedSpeed(this.idCar)));
				//System.out.println("getSpeed = " + (double) sumo.do_job_get(Vehicle.getSpeed(this.idCar)));
				//System.out.println(
				//		"getSpeedDeviation = " + (double) sumo.do_job_get(Vehicle.getSpeedDeviation(this.idCar)));
				//System.out.println("getMaxSpeedLat = " + (double) sumo.do_job_get(Vehicle.getMaxSpeedLat(this.idCar)));
				//System.out.println("getSlope = " + (double) sumo.do_job_get(Vehicle.getSlope(this.idCar))
				//		+ " the slope at the current position of the vehicle in degrees");
				//System.out.println(
				//		"getSpeedWithoutTraCI = " + (double) sumo.do_job_get(Vehicle.getSpeedWithoutTraCI(this.idCar))
				//				+ " Returns the speed that the vehicle would drive if no speed-influencing\r\n"
				//				+ "command such as setSpeed or slowDown was given.");

				//sumo.do_job_set(Vehicle.setSpeed(this.idCar, (1000 / 3.6)));
				//double auxspeed = (double) sumo.do_job_get(Vehicle.getSpeed(this.idCar));
				//System.out.println("new speed = " + (auxspeed * 3.6));
				//System.out.println(
				//		"getSpeedDeviation = " + (double) sumo.do_job_get(Vehicle.getSpeedDeviation(this.idCar)));
				
				if (carStatus != "abastecendo") {
					this.sumo.do_job_set(Vehicle.setSpeed(this.idCar, speed));
					this.sumo.do_job_set(Vehicle.setSpeedMode(this.idCar, 31));
				}
				
				// System.out.println("getPersonNumber = " + sumo.do_job_get(Vehicle.getPersonNumber(this.idCar)));
				//System.out.println("getPersonIDList = " + sumo.do_job_get(Vehicle.getPersonIDList(this.idCar)));
				
				// System.out.println("************************");

			} else {
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

	public void voltarAndar() throws Exception {
		this.sumo.do_job_set(Vehicle.setSpeed(this.idCar, setRandomSpeed()));
		//this.sumo.do_job_set(Vehicle.setSpeedMode(this.idCar, 31));
	}

	public double setRandomSpeed(){
		Random random = new Random();
        double range = 9.5 - 2.5;
        double scaled = random.nextDouble() * range;
        double generatedNumber = scaled + 2.5;
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

	private double[] calculaCoordGeograficas() throws Exception {
		SumoPosition2D sumoPosition2D = (SumoPosition2D) sumo.do_job_get(Vehicle.getPosition(this.idCar));

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

	private void criaSheet(String carID){
		String nomeDoArquivo = "carData.xlsx";

		try (Workbook workbook = new XSSFWorkbook();
            FileOutputStream outputStream = new FileOutputStream(nomeDoArquivo)) {
			org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet(carID);

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Timestamp");
            headerRow.createCell(1).setCellValue("ID Car");
            headerRow.createCell(2).setCellValue("ID Route");
            headerRow.createCell(3).setCellValue("Speed");
            headerRow.createCell(4).setCellValue("Distance");
			headerRow.createCell(5).setCellValue("FuelConsumption");
            headerRow.createCell(6).setCellValue("FuelType");
            headerRow.createCell(7).setCellValue("CO2Emission");
            headerRow.createCell(8).setCellValue("Longitude (Lon)");
            headerRow.createCell(9).setCellValue("Latitude (Lat)");

            // Salve a planilha com o cabeçalho
            workbook.write(outputStream);
		} catch (Exception e) {
			
		} 

	}

	private synchronized void atualizaPlanilhaCar(DrivingData data){
        
        String nomeDoArquivo = "carData.xlsx";

        try (FileInputStream inputStream = new FileInputStream(nomeDoArquivo);
             Workbook workbook = WorkbookFactory.create(inputStream);
             FileOutputStream outputStream = new FileOutputStream(nomeDoArquivo)) {
           
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheet(getIdCar());    
        
        int lastRowNum = sheet.getLastRowNum();
        Row newRow = sheet.createRow(lastRowNum + 1);

            // Preencha as células da nova linha com os dados da classe TransferData
        newRow.createCell(0).setCellValue(data.getTimeStamp());
        newRow.createCell(1).setCellValue(data.getCarID());
        newRow.createCell(2).setCellValue(data.getRouteIDSUMO());
        newRow.createCell(3).setCellValue(data.getSpeed());
        newRow.createCell(4).setCellValue(data.getOdometer()); 
		newRow.createCell(5).setCellValue(data.getFuelConsumption());
		newRow.createCell(6).setCellValue(data.getFuelType());
		newRow.createCell(7).setCellValue(data.getCo2Emission());
		newRow.createCell(8).setCellValue(data.getLonAtual());
		newRow.createCell(9).setCellValue(data.getLatAtual());
        
        // Salve as alterações na planilha
        workbook.write(outputStream);
        
        } catch (IOException e) {
            e.printStackTrace();
        }
            
    }

	private String criaJSONTamanhoBytes(int numBytes) {
        JSONObject my_json = new JSONObject();
        my_json.put("Num Bytes", numBytes);
        return my_json.toString();
    }

    private int extraiTamanhoBytes(String numBytesJSON) {
        JSONObject my_json = new JSONObject(numBytesJSON);
        int numBytes = my_json.getInt("Num Bytes");
        return numBytes;
    }

}