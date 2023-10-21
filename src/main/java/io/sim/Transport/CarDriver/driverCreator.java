package io.sim.Transport.CarDriver;

import it.polito.appeal.traci.SumoTraciConnection;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.tudresden.sumo.objects.SumoColor;
import io.sim.Transport.Fuel.FuelStation;

public class driverCreator {

    public static boolean on_off = false;
    public static int fuelType = 2; // Gasolina
    public static int fuelPreferential = 2; // Gasolina
    public static double fuelPrice = 5.89;
    public static int personCapacity = 1;
    public static int personNumber = 1;

    public static ArrayList<Driver> criaListaDrivers(int qtdDrivers, FuelStation fuelStation, long acquisitionRate, SumoTraciConnection sumo, String host, int portaCompanny, int portaAlphaBank) throws Exception {
        
        ArrayList<Driver> drivers = new ArrayList<>(); // Cria e inicializa um ArrayList de Drivers
        SumoColor cor = new SumoColor(0, 255, 0, 126); // Cria e Inicializa a cor Verde

        for (int i = 0; i < qtdDrivers; i++) {
            
            String driverID = "Driver" + (i + 1);
            String carID = "Car" + (i + 1);

            Car car = new Car(on_off, carID, cor, driverID, sumo, acquisitionRate, fuelType, fuelPreferential, fuelPrice, personCapacity, personNumber, host, portaCompanny);

            Driver driver = new Driver(driverID, car, acquisitionRate, fuelStation, portaAlphaBank, host);
            drivers.add(driver);
            
        }

        return drivers;
    }

    

}