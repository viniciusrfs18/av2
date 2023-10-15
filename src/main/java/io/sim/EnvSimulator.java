package io.sim;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import org.apache.logging.log4j.core.jmx.Server;
import org.python.modules.thread.thread;

import io.sim.MobilityCompany.Company;
import io.sim.Pagamentos.Account;
import io.sim.Pagamentos.AlphaBank;
import de.tudresden.sumo.objects.SumoColor;
import io.sim.Rotas.Rotas;
import io.sim.Rotas.criadorRotas;
import it.polito.appeal.traci.SumoTraciConnection;

/**Classe que faz a conexao com o SUMO e cria os objetos da simulacao. 
 * Acaba funcionando como uma classe principal
 */
public class EnvSimulator extends Thread
{
    private SumoTraciConnection sumo;
	private static final int PORT_SUMO = 12345;
	private static final int PORT_COMPANY = 11111;
	private static final int PORT_BANK = 33333;
	private static final String ROTAS_XML = "map/map.rou.alt.xml";
	//"data/dados2.xml"
	private static final int NUM_DRIVERS = 2; 
	

    public EnvSimulator(){}

    public void run()
	{
		/* SUMO */
		String sumo_bin = "sumo-gui";		
		String config_file = "map/map.sumo.cfg";
		
		// Sumo connection
		this.sumo = new SumoTraciConnection(sumo_bin, config_file);
		sumo.addOption("start", "1"); // auto-run on GUI show
		//sumo.addOption("quit-on-end", "1"); // auto-close on end

		try
		{
			sumo.runServer(PORT_SUMO); // porta servidor SUMO
			System.out.println("SUMO conectado.");
			Thread.sleep(5000);

			// Itinerary i1 = new Itinerary(ROTAS_XML, "0");
			
			ArrayList<Account> accounts = new ArrayList<Account>();
			FuelStation fs = new FuelStation();
			accounts.add(fs.getAccount());

			ArrayList<Rotas> routes = criadorRotas.extract(ROTAS_XML);
			
			//System.out.println("ES - " + routes.size() + " rotas disponiveis.");
			
			ServerSocket alphaBankServer = new ServerSocket(PORT_BANK);
			AlphaBank alphaBank = new AlphaBank(alphaBankServer);

			Thread.sleep(500);

			ServerSocket companyServer = new ServerSocket(PORT_COMPANY);
			Company company = new Company(companyServer, routes, NUM_DRIVERS);
			accounts.add(company.getAccount());

			ArrayList<Driver> driversList = new ArrayList<Driver>();
			driversList = criadorMotoristas.create(sumo, fs, NUM_DRIVERS);

			for(int i=0;i<NUM_DRIVERS;i++){
				accounts.add(driversList.get(i).getAccount());
			}

			alphaBank.setAccount(accounts);
			
			company.setDrivers(driversList);

			alphaBank.start();
			
			company.start();
			
			//alphaBank.join();
			//company.join();

			//Loop responsável por criar os Motoristas e os Carros
			/** 
			for(int i=0;i<NUM_DRIVERS;i++)
			{
				SumoColor cor = new SumoColor(0, 255, 0, 126);// funcao para cria cors
				String driverID = "Driver" + (i+1);
				String carHost = "localhost";// + i+1;
				Car car = new Car(carHost,PORT_COMPANY,true, "CAR" + (i+1), cor, driverID, sumo, AQUISITION_RATE, FUEL_TYPE, FUEL_PREFERENTIAL, FUEL_PRICE,
				PERSON_CAPACITY, PERSON_NUMBER, fs);
				Driver driver = new Driver(driverID, car, AQUISITION_RATE);
				drivers.add(driver);
			}
			*/

			aguardaDrivers(driversList); 
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

	/**Roda o metodo join em todos os Drivers.
     * @param lista
     * @throws InterruptedException
     */
    private static void aguardaDrivers(ArrayList<Driver> _lista) throws InterruptedException
    {
        for(int i=0;i<_lista.size();i++)
        {
            _lista.get(i).join(); //O método JOIN simplesmente faz com que a thread que o chama aguarde até que a thread na qual ele foi chamado termine.
        }
    }

}
