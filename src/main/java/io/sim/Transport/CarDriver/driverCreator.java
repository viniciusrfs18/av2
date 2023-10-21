package io.sim.Transport.CarDriver;

import it.polito.appeal.traci.SumoTraciConnection;
import java.util.ArrayList;
import de.tudresden.sumo.objects.SumoColor;
import io.sim.Transport.Fuel.FuelStation;

public class driverCreator {

    // Variáveis estáticas para configuração dos drivers
    public static boolean on_off = false;               // Indica se o veículo está ligado ou desligado
    public static int fuelType = 2;                     // Tipo de combustível (2 - Gasolina)
    public static int fuelPreferential = 2;             // Tipo de combustível preferencial (2 - Gasolina)
    public static double fuelPrice = 5.89;              // Preço do combustível por litro
    public static int personCapacity = 1;               // Capacidade máxima de passageiros no veículo
    public static int personNumber = 1;                 // Número atual de passageiros no veículo

    // Método para criar uma lista de drivers
    public static ArrayList<Driver> criaListaDrivers(int qtdDrivers, FuelStation fuelStation, long acquisitionRate, SumoTraciConnection sumo, String host, int portaCompanny, int portaAlphaBank) throws Exception {
        
        ArrayList<Driver> drivers = new ArrayList<>(); // Cria e inicializa um ArrayList de Drivers
        SumoColor cor = new SumoColor(0, 255, 0, 126); // Cria e inicializa a cor Verde

        for (int i = 0; i < qtdDrivers; i++) {
            
            String driverID = "Driver" + (i + 1);
            String carID = "Car" + (i + 1);

            // Cria um carro com as configurações especificadas
            Car car = new Car(on_off, carID, cor, driverID, sumo, acquisitionRate, fuelType, fuelPreferential, fuelPrice, personCapacity, personNumber, host, portaCompanny);

            // Cria um driver associado ao carro
            Driver driver = new Driver(driverID, car, acquisitionRate, fuelStation, portaAlphaBank, host);

            // Adiciona o driver à lista de drivers
            drivers.add(driver);
            
        }

        return drivers;
    }

}
