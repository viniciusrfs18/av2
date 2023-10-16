package io.sim;

import io.sim.Car;
import it.polito.appeal.traci.SumoTraciConnection;
import io.sim.Pagamentos.Account;

import java.util.ArrayList;

import de.tudresden.sumo.objects.SumoColor;

public class criadorMotoristas {
    private static final int PORT_COMPANY = 11111;
    private static final int AQUISITION_RATE = 500;
	private static final int FUEL_TYPE = 2;
	private static final int FUEL_PREFERENTIAL = 2;
	private static final double FUEL_PRICE = 3.40;
	private static final int PERSON_CAPACITY = 1;
	private static final int PERSON_NUMBER = 1;

    public static ArrayList<Driver> create(SumoTraciConnection sumo, FuelStation fs, int NUM_DRIVERS) throws Exception {
        
        ArrayList<Driver> drivers = new ArrayList<>();

        for(int i=0;i<NUM_DRIVERS;i++)
			{	
				Account account = new Account((2+i), 0);
				SumoColor cor = new SumoColor(0, 255, 0, 126);// funcao para cria cors
				String driverID = "Driver" + (i+1);
				String carHost = "localhost";// + i+1;
				Car car = new Car(carHost,PORT_COMPANY,true, "CAR" + (i+1), cor, driverID, sumo, AQUISITION_RATE, FUEL_TYPE, FUEL_PREFERENTIAL, FUEL_PRICE,
				PERSON_CAPACITY, PERSON_NUMBER, fs);
				Driver driver = new Driver(driverID, car, AQUISITION_RATE, account, fs);
				drivers.add(driver);
		}

        return drivers;
    }
}
