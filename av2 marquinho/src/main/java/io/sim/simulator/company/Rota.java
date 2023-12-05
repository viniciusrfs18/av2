package io.sim.simulator.company;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *      A classe Rota representa informações de rota, com um identificador e detalhes sobre as bordas da rota. 
 * Além disso, o método estático "criaRotasXML" é responsável por analisar um arquivo XML, extrair informações sobre as 
 * rotas dos veículos e criar objetos Rota correspondentes, que são armazenados em uma lista e retornados.
 */
public class Rota {
    private String id;      // Identificador da rota
    private String edges;   // Informações sobre as bordas da rota

    public Rota(String id, String edges) {
        this.id = id;
        this.edges = edges;
    }

    // Retorna o identificador da rota
    public String getID() {
        return id;
    }

     // Retorna as informações sobre as bordas da rota
    public String getEdges() {
        return edges;
    }

    // Método estático que lê informações de rota de um arquivo XML e retorna uma lista de objetos Rota
    public static ArrayList<Rota> criaRotasXML(String xmlFilePath) {
        ArrayList<Rota> routes = new ArrayList<>();
        
        try {
            // Configurando as classes necessárias para a análise do documento XML
            File xmlFile = new File(xmlFilePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            
            // Obtendo a lista de elementos com a tag "vehicle" do documento
            NodeList vehicleList = doc.getElementsByTagName("vehicle");
            
            // Iterando sobre os elementos "vehicle"
            for (int i = 0; i < vehicleList.getLength(); i++) {
                Element vehicleElement = (Element) vehicleList.item(i);
                String idRouteAux = vehicleElement.getAttribute("id");
                NodeList routeList = vehicleElement.getElementsByTagName("route");
                
                // Iterando sobre os elementos "route"
                for (int j = 0; j < routeList.getLength(); j++) {
                    Element routeElement = (Element) routeList.item(j);
                    String edges = routeElement.getAttribute("edges");
                    
                    // Criando um objeto Rota com o identificador e informações sobre as bordas
                    Rota route = new Rota(idRouteAux, edges);
                    
                    // Adicionando o objeto Rota à lista de rotas
                    routes.add(route);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return routes;
    }

    public static ArrayList<Rota> criaArrayRotaAV2(String xmlFilePath, int replicacoes) {
        ArrayList<Rota> routes = new ArrayList<>();
    
        try {
            // Configurando as classes necessárias para a análise do documento XML
            File xmlFile = new File(xmlFilePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
    
            // Obtendo a lista de elementos com a tag "vehicle" do documento
            NodeList vehicleList = doc.getElementsByTagName("vehicle");
    
            // Verifica se há pelo menos um veículo
            if (vehicleList.getLength() > 0) {
                Element firstVehicleElement = (Element) vehicleList.item(0);
                String idRouteAux = firstVehicleElement.getAttribute("id");
                NodeList routeList = firstVehicleElement.getElementsByTagName("route");
    
                // Verifica se há pelo menos uma rota
                if (routeList.getLength() > 0) {
                    Element firstRouteElement = (Element) routeList.item(0);
                    String edges = firstRouteElement.getAttribute("edges");
    
                    // Replicar a rota n vezes
                    for (int i = 0; i < replicacoes; i++) {
                        Rota route = new Rota(idRouteAux + "_" + i, edges);
                        routes.add(route);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        return routes;
    }

    public static ArrayList<String> criaListaEdges(Rota rota) {
        String grandeString = rota.getEdges();
        ArrayList<String> edges = new ArrayList<String>();
        for(String e : grandeString.split(" ")) {
			edges.add(e);
		}
        return edges;
    }
    
}
