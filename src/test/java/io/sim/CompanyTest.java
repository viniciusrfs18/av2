package io.sim;

import static org.junit.Assert.*;

import java.net.ServerSocket;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import it.polito.appeal.traci.SumoTraciConnection;

public class CompanyTest {
    
    private SumoTraciConnection sumo;
    private Company company;

    @Before
    public void setUp() throws Exception {

       String sumo_bin = "sumo-gui";		
	   String config_file = "map/map.sumo.cfg";
		
		// Sumo connection
		this.sumo = new SumoTraciConnection(sumo_bin, config_file);
		
		String rotasXML = "data/dados.xml";

		sumo.runServer(11111); // porta servidor SUMO

		FuelStation fuelStation = new FuelStation(33333, "localhost");
		fuelStation.start();

		ServerSocket companyServer = new ServerSocket(12345);
		ArrayList<Rota> rotas = routeCreator.criaRotas(rotasXML);
		this.company = new Company(companyServer, rotas, 1,  33333, "localhost");

    }

     
    @Test
    public void testRotasDispVazio() {
        // Teste do método routesAvaliable()
        boolean routesAvailable = company.rotasDispVazio();
        assertTrue(routesAvailable);
    }

    @Test
    public void testGetprice() {
        // Teste do método stillOnSUMO()
        double expectedprice = 3.25; // Substitua pelo amount esperado do preço do litro de gasolina.
        double actualprice = company.getprice();

        assertEquals(expectedprice, actualprice, 0.01);
    }

}

