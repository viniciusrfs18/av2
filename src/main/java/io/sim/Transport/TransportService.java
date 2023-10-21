package io.sim.Transport;

import de.tudresden.sumo.cmd.Route;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.Transport.CarDriver.Car;
import io.sim.Transport.Rotas.Rota;
import it.polito.appeal.traci.SumoTraciConnection;

/**
 * Esta classe é responsável por criar um veículo no simulador SUMO (Simulation of Urban Mobility) e definir sua rota e outras configurações.
 */
public class TransportService extends Thread {

    private String idTransportService; // ID do serviço de transporte
    private boolean on_off; // Estado do veículo (ligado/desligado)
    private SumoTraciConnection sumo; // Conexão com o SUMO
    private Car car; // O veículo correspondente
    private Rota rota; // A rota a ser seguida pelo veículo
    private SumoStringList edge; // Lista de arestas da rota
    private boolean terminado; // Indica se o serviço foi concluído
    private boolean sumoInit; // Indica se a conexão com o SUMO foi inicializada

    /**
     * Construtor da classe que recebe informações iniciais para configurar o serviço de transporte.
     *
     * @param _on_off             Estado do veículo (ligado/desligado)
     * @param _idTransportService ID do serviço de transporte
     * @param _route              A rota que o veículo seguirá
     * @param _car                O veículo correspondente
     * @param _sumo               Conexão com o SUMO
     */
    public TransportService(boolean _on_off, String _idTransportService, Rota _route, Car _car, SumoTraciConnection _sumo) {
        this.on_off = _on_off;
        this.idTransportService = _idTransportService;
        this.rota = _route;
        this.car = _car;
        this.sumo = _sumo;
        this.terminado = false;
        this.sumoInit = false;
    }

    @Override
    public void run() {
        // Início do serviço de transporte
        System.out.println("Iniciando TransportService para o Carro: " + this.car.getIdCar());

        // Inicializa as rotas do veículo
        this.initializeRoutes();

        // Imprime a última aresta da rota
        System.out.println(this.car.getIdCar() + "Rota: " + edge + " adicionada!");
        String edgeFinal = edge.get(edge.size() - 1);
        System.out.println(this.car.getIdCar() + "- Edge final: " + edgeFinal);

        try {
            // Pausa a thread de acordo com a taxa de aquisição do veículo
            sleep(this.car.getAcquisitionRate());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Encerra o serviço de transporte
        System.out.println("Encerrando TransportService.");
    }

    /**
     * Inicializa as rotas do veículo no simulador SUMO.
     */
    private void initializeRoutes() {
        // Adiciona todas as arestas da rota a uma lista de arestas
        edge = new SumoStringList();
        edge.clear();
        String aux = this.rota.getEdges();

        for (String e : aux.split(" ")) {
            edge.add(e);
        }

        // Inicializa a rota, veículo e a cor do veículo no SUMO
        try {
            sumo.do_job_set(Route.add(this.rota.getID(), edge));
            sumo.do_job_set(Vehicle.addFull(this.car.getIdCar(), this.rota.getID(), "DEFAULT_VEHTYPE", "now", "0", "0", "0", "current", "max", "current", "", "", "", this.car.getPersonCapacity(), this.car.getPersonNumber()));
            sumo.do_job_set(Vehicle.setColor(this.car.getIdCar(), this.car.getColorCar()));
            this.sumo.do_job_set(Vehicle.setSpeed(this.car.getIdCar(), 50));
            this.sumo.do_job_set(Vehicle.setSpeedMode(this.car.getIdCar(), 31));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        this.sumoInit = true;
    }

    /**
     * Obtém o estado do veículo.
     *
     * @return true se o veículo está ligado, false se estiver desligado
     */
    public boolean isOn_off() {
        return on_off;
    }

    /**
     * Define o estado do veículo.
     *
     * @param _on_off Novo estado do veículo (ligado/desligado)
     */
    public void setOn_off(boolean _on_off) {
        this.on_off = _on_off;
    }

    /**
     * Define se o serviço de transporte foi concluído.
     *
     * @param _terminado true se o serviço foi concluído, false caso contrário
     */
    public void setTerminado(boolean _terminado) {
        this.terminado = _terminado;
    }

    /**
     * Obtém o ID do serviço de transporte.
     *
     * @return ID do serviço de transporte
     */
    public String getIdTransportService() {
        return this.idTransportService;
    }

    /**
     * Obtém a conexão com o SUMO.
     *
     * @return Conexão com o SUMO
     */
    public SumoTraciConnection getSumo() {
        return this.sumo;
    }

    /**
     * Obtém o veículo correspondente.
     *
     * @return Veículo correspondente
     */
    public Car getCar() {
        return this.car;
    }

    /**
     * Obtém a rota que o veículo está seguindo.
     *
     * @return Rota que o veículo está seguindo
     */
    public Rota getRota() {
        return this.rota;
    }

    /**
     * Define a rota que o veículo deve seguir.
     *
     * @param _rota Nova rota a ser seguida
     */
    public void setRoute(Rota _rota) {
        this.rota = _rota;
    }
}
