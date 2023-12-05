package io.sim;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import io.sim.Driver;
import io.sim.Criamotorista;
import it.polito.appeal.traci.SumoTraciConnection;

public class EnvSimulator extends Thread {
    private SumoTraciConnection sumo;
    private static String host;
    private static int portaSUMO;
    private static int portaCompany;
    private static int portaAlphaBank;
    private static long acquisitionRate;
    private static int numDrivers;
    private static String rotasXML;

    public EnvSimulator() {
        String sumo_bin = "sumo-gui";
        String config_file = "map/map.sumo.cfg";

        this.sumo = new SumoTraciConnection(sumo_bin, config_file);
        host = "localhost";
        portaSUMO = 12345;
        portaCompany = 23415;
        portaAlphaBank = 54321;
        acquisitionRate = 500;
        numDrivers = 100;
        rotasXML = "map/map.rou.alt.xml";
    }

    public void run() {
        sumo.addOption("start", "1");
        sumo.addOption("quit-on-end", "1");

        try {
            sumo.runServer(portaSUMO);
            System.out.println("SUMO conect");

            Thread.sleep(5000);

            ExecutaSimulador execSimulador = new ExecutaSimulador(this.sumo, acquisitionRate);
            execSimulador.start();

            ServerSocket alphaBankServer = new ServerSocket(portaAlphaBank);
            AlphaBank alphaBank = new AlphaBank(alphaBankServer);
            alphaBank.start();

            Thread.sleep(2000);

            FuelStation fuelStation = new FuelStation(portaAlphaBank, host);
            fuelStation.start();

            ServerSocket companyServer = new ServerSocket(portaCompany);
            ArrayList<Rota> rotas = routeCreator.criaRotas(rotasXML);
            Company company = new Company(companyServer, rotas, numDrivers, portaAlphaBank, host);
            company.start();

            ArrayList<Driver> drivers = Criamotorista.criaListaDrivers(numDrivers, fuelStation, acquisitionRate, sumo, host, portaCompany, portaAlphaBank);

            createSheet(drivers);

            for (int i = 0; i < drivers.size(); i++) {
                drivers.get(i).start();
                Thread.sleep(500);
            }

            for (int i = 0; i < drivers.size(); i++) {
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

        
    }

    private void createSheet(ArrayList<Driver> drivers) {
        String nomeDoArquivo = "Dadoscarro_1.xlsx";

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
