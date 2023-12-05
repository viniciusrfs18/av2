package io.sim;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import it.polito.appeal.traci.SumoTraciConnection;
import io.sim.simulator.bank.AlphaBank;
import io.sim.simulator.company.Company;
import io.sim.simulator.company.Rota;
import io.sim.simulator.driver.Car;
import io.sim.simulator.driver.Driver;
import io.sim.simulator.fuelStation.FuelStation;
import io.sim.simulator.simulation.DriverANDCarCreator;
import io.sim.simulator.simulation.ExecutaSimulador;

public class DriverANDCarCreatorTest {
    // Configuração inicial do ambiente e conexão com o SUMO
    private SumoTraciConnection sumo;
    private FuelStation fuelStation;
    private String sumo_bin = "sumo-gui";        
    private String config_file = "map/map.sumo.cfg";
    
    // Configurações gerais para simulação
    private String host = "localhost";
    private int portaSUMO = 12345;
    private int portaCompany = 23415;
    private int portaAlphaBank = 54321;
    private int taxaAquisicao = 300;
    private int numDrivers = 1;
    private boolean considerarConsumoComb = false;
    private String rotasXML = "data/dados.xml";
        

    @Before
    public void setUp() {
        sumo = new SumoTraciConnection(sumo_bin, config_file);

        // Configuração inicial do SUMO para automação
        sumo.addOption("start", "1"); // Inicia a simulação automaticamente na GUI
        sumo.addOption("quit-on-end", "1"); // Fecha o SUMO automaticamente ao final

        try {
            sumo.runServer(portaSUMO); // Inicia o servidor SUMO na porta especificada
            System.out.println("SUMO conectado.");
            Thread.sleep(5000);

            // Inicia a execução do simulador
            ExecutaSimulador execSimulador = new ExecutaSimulador(this.sumo, taxaAquisicao);
            execSimulador.start();

            // Inicia um servidor AlphaBank na porta especificada
            ServerSocket alphaBankServer = new ServerSocket(portaAlphaBank);
            AlphaBank alphaBank = new AlphaBank(numDrivers + 2,alphaBankServer);
            alphaBank.start();
            Thread.sleep(2000);

            // Inicia uma estação de combustível (FuelStation)
            FuelStation fuelStation = new FuelStation(portaAlphaBank, host);
            fuelStation.start();

            // Inicia um servidor Company na porta especificada
            ArrayList<Rota> rotasDisp = Rota.criaRotasXML(rotasXML);
            ServerSocket companyServer = new ServerSocket(portaCompany);
            Company company = new Company(companyServer, rotasDisp, numDrivers, portaAlphaBank, host);
            company.start();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Teste para verificar se um carro é criado corretamente
    @Test
    public void testCreateCar() {
        String driverID = "Driver1";
        long taxaAquisicao = 1000;
        Car car = DriverANDCarCreator.createCar("Car1", driverID, taxaAquisicao, sumo, host, portaCompany, considerarConsumoComb);
        assertNotNull(car);
        assertEquals(car.getIdCar(), "Car1");
        assertEquals(car.getDriverID(), "Driver1");
    }

    // Teste para verificar se uma lista de drivers e carros é criada corretamente
    @Test
    public void testCriaListaDrivers() {
        int qtdDrivers = 5;
        ArrayList<Driver> drivers = DriverANDCarCreator.criaListaDrivers(qtdDrivers, fuelStation, 1000, sumo, host, portaCompany, portaAlphaBank, considerarConsumoComb);
        assertNotNull(drivers);
        assertEquals(qtdDrivers, drivers.size());
    }
}

