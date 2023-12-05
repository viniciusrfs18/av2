package io.sim.simulator.simulation;

import io.sim.simulator.driver.Car;
import io.sim.simulator.driver.Driver;
import io.sim.simulator.fuelStation.FuelStation;
import it.polito.appeal.traci.SumoTraciConnection;

import java.util.ArrayList;
import java.util.Random;

import de.tudresden.sumo.objects.SumoColor;

// A classe DriverANDCarCreator é responsável por criar os componentes principais da simulação, ou seja, os motoristas e os carros que eles dirigirão
public class DriverANDCarCreator {
    // Método para criar uma lista de drivers e carros
    public static ArrayList<Driver> criaListaDrivers(int qtdDrivers, FuelStation fuelStation, long taxaAquisicao, SumoTraciConnection sumo, String host, int portaCompanny, int portaAlphaBank, boolean considerarConsumoComb) {
        ArrayList<Driver> drivers = new ArrayList<>();

        for (int i = 0; i < qtdDrivers; i++) {
            String driverID = "Driver " + (i + 1);
            String carID = "Car " + (i + 1);
            Car car = createCar(carID, driverID, taxaAquisicao, sumo, host, portaCompanny, considerarConsumoComb);

            Driver driver = new Driver(driverID, car, taxaAquisicao, fuelStation, portaAlphaBank, host);
            drivers.add(driver);
        }

        return drivers;
    }

    // Método estático para criar um objeto Car com cores aleatórias
    public static Car createCar(String idCar, String driverID, long taxaAquisicao, SumoTraciConnection sumo, String host, int companyServerPort, boolean considerarConsumoComb) {
        try {
            // Define as características comuns para os novos objetos Car
            boolean on_off = false;
            int fuelType = 2; // Gasolina
            int fuelPreferential = 2; // Gasolina
            double fuelPrice = 3.40;
            int personCapacity = 1;
            int personNumber = 1;

            Random random = new Random();

            // Gera uma cor aleatória para o carro
            SumoColor randomColor = new SumoColor(
                    random.nextInt(256), // Valor de vermelho entre 0 e 255
                    random.nextInt(256), // Valor de verde entre 0 e 255
                    random.nextInt(256), // Valor de azul entre 0 e 255
                    126 // Valor de alfa (transparência) fixo em 126
            );
        
            // Cria um novo objeto Car com características comuns e cor aleatória
            Car car = new Car(on_off, idCar, randomColor, driverID, sumo, taxaAquisicao, fuelType, fuelPreferential, fuelPrice, considerarConsumoComb, personCapacity, personNumber, host, companyServerPort);
            return car;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
