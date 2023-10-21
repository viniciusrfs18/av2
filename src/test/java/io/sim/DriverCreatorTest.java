package io.sim;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import it.polito.appeal.traci.SumoTraciConnection;
import io.sim.Transport.Fuel.FuelStation;
import io.sim.Transport.CarDriver.Driver;
import io.sim.Transport.CarDriver.driverCreator;

import java.io.IOException;
import java.util.ArrayList;

public class DriverCreatorTest {

    private SumoTraciConnection sumo;
    private FuelStation fuelStation;
    private String host;
    private int portaCompanny;
    private int portaAlphaBank;

    @Before
    public void setUp() throws IOException, InterruptedException {
        String sumo_bin = "sumo-gui";		
	    String config_file = "map/map.sumo.cfg";
		
		// Sumo connection
		this.sumo = new SumoTraciConnection(sumo_bin, config_file);

		sumo.runServer(11111); // porta servidor SUMO
        
        // Inicialize objetos ou configurações necessárias para os testes, se houver.
        host = "localhost";
        portaCompanny = 12345; // Substitua pelo número da porta real, se aplicável.
        portaAlphaBank = 33333; // Substitua pelo número da porta real, se aplicável.
        this.fuelStation = new FuelStation(33333, host);
    }

    @Test
    public void testCriaListaDrivers() {
        try {
            int qtdDrivers = 5; // Defina a quantidade desejada para testar.

            ArrayList<Driver> drivers = driverCreator.criaListaDrivers(qtdDrivers, fuelStation, 1000, sumo, host, portaCompanny, portaAlphaBank);

            assertEquals(qtdDrivers, drivers.size());

            System.out.println(drivers.get(0).getId() + " ");
            
            for (int i = 0; i < qtdDrivers; i++) {
                assertNotNull(drivers.get(i));
                assertEquals(drivers.get(i).getDriverId(), "Driver" + (i + 1));
                assertNotNull(drivers.get(i).getCar());
                assertNotNull(drivers.get(i).getFuelStation());
            }

        } catch (Exception e) {
            fail("Exceção inesperada: " + e.getMessage());
        }
    }
}
