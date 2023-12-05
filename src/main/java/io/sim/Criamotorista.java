package io.sim;
import it.polito.appeal.traci.SumoTraciConnection;
import java.util.ArrayList;
import de.tudresden.sumo.objects.SumoColor;


public class Criamotorista {

    public static boolean on_off = false;
    public static int fuelType = 2;
    public static int fuelPreferential = 2;
    public static double fuelPrice = 5.89;
    public static int personCapacity = 1;
    public static int personNumber = 1;

    public static ArrayList<Driver> criaListaDrivers(int qtdDrivers, FuelStation fuelStation, long acquisitionRate, SumoTraciConnection sumo, String host, int portaCompanny, int portaAlphaBank) throws Exception {
        
        ArrayList<Driver> drivers = new ArrayList<>();
        SumoColor cor = new SumoColor(0, 255, 0, 126);

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
