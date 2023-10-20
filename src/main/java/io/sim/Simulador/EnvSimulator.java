package io.sim.Simulador;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import io.sim.MobilityCompany.Company;
import io.sim.Pagamentos.AlphaBank;
import io.sim.Transport.CarDriver.Driver;
import io.sim.Transport.CarDriver.driverCreator;
import io.sim.Transport.Fuel.FuelStation;
import io.sim.Transport.Rotas.Rota;
import io.sim.Transport.Rotas.routeCreator;
import it.polito.appeal.traci.SumoTraciConnection;

/**Classe que faz a conexao com o SUMO e cria os objetos da simulacao. 
 * Acaba funcionando como uma classe principal
 */
public class EnvSimulator extends Thread {
    private SumoTraciConnection sumo;
	private static String host;
	private static int portaSUMO; // NEWF
	private static int portaCompany;
	private static int portaAlphaBank;
	private static long taxaAquisicao;
	private static int numDrivers;
	private static String rotasXML;

    public EnvSimulator() {
		/* SUMO */
		String sumo_bin = "sumo-gui";		
		String config_file = "map/map.sumo.cfg";
		
		// Sumo connection
		this.sumo = new SumoTraciConnection(sumo_bin, config_file);
		
		host = "localhost";
		portaSUMO = 12345;
		portaCompany = 23415;
		portaAlphaBank = 54321;
		taxaAquisicao = 500;
		numDrivers = 10;
		rotasXML = "map/map.rou.alt.xml";
	}

    public void run() {
		// Start e configurações inicias do SUMO
		sumo.addOption("start", "1"); // auto-run on GUI show
		sumo.addOption("quit-on-end", "1"); // auto-close on end

		try {

			sumo.runServer(portaSUMO); // porta servidor SUMO
			System.out.println("SUMO conectado.");
			Thread.sleep(5000);
			
			ExecutaSimulador execSimulador = new ExecutaSimulador(this.sumo, taxaAquisicao);
			execSimulador.start();

			ServerSocket alphaBankServer = new ServerSocket(portaAlphaBank);
			AlphaBank alphaBank = new AlphaBank(alphaBankServer);
			alphaBank.start();

			Thread.sleep(2000);

			FuelStation fuelStation = new FuelStation(portaAlphaBank, host);
			fuelStation.start();

			ServerSocket companyServer = new ServerSocket(portaCompany);
			ArrayList<Rota> rotas = routeCreator.criaRotas(rotasXML);
			Company company = new Company(companyServer, rotas, numDrivers,  portaAlphaBank, host);
			company.start();

			// Roda o metodo join em todos os Drivers, espera todos os drivers terminarem a execução
			ArrayList<Driver> drivers = driverCreator.criaListaDrivers(numDrivers, fuelStation, taxaAquisicao, sumo, host, portaCompany, portaAlphaBank);
			
			criaSheet(drivers);

			for(int i = 0; i < drivers.size(); i++) {
				
				drivers.get(i).start();
				Thread.sleep(500);
			}
			
			for(int i = 0; i < drivers.size(); i++) {
				drivers.get(i).join();
			}

			companyServer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Encerrando EnvSimulator");
    }

	private void criaSheet(ArrayList<Driver> drivers){
		String nomeDoArquivo = "carData.xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {
            for (Driver driver : drivers) {
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet(driver.getCar().getIdCar());
                
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
            }

            // Salve o arquivo Excel após criar todas as abas de planilha.
            try (FileOutputStream outputStream = new FileOutputStream(nomeDoArquivo)) {
                workbook.write(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
