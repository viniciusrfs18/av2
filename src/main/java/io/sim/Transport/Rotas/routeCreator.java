package io.sim.Transport.Rotas;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class routeCreator {
    // Método para criar rotas a partir de um arquivo XML
    public static ArrayList<Rota> criaRotas(String xmlFilePath) {
        // Inicializa uma lista de rotas vazia
        ArrayList<Rota> routes = new ArrayList<>();
        
        try {
            // Lê o arquivo XML especificado
            File xmlFile = new File(xmlFilePath);
            
            // Configuração para criar um parser XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            // Faz o parsing do arquivo XML
            Document doc = builder.parse(xmlFile);
            
            // Obtém a lista de elementos "vehicle" no XML
            NodeList vehicleList = doc.getElementsByTagName("vehicle");
            
            // Itera sobre os elementos "vehicle" no XML
            for (int i = 0; i < vehicleList.getLength(); i++) {
                Element vehicleElement = (Element) vehicleList.item(i);
                
                // Obtém o atributo "id" do elemento "vehicle"
                String idRouteAux = vehicleElement.getAttribute("id");
                
                // Obtém a lista de elementos "route" no XML
                NodeList routeList = vehicleElement.getElementsByTagName("route");
                
                // Itera sobre os elementos "route" no XML
                for (int j = 0; j < routeList.getLength(); j++) {
                    Element routeElement = (Element) routeList.item(j);
                    
                    // Obtém o atributo "edges" do elemento "route"
                    String edges = routeElement.getAttribute("edges");
                    
                    // Cria um objeto Rota com o identificador da rota e as arestas
                    Rota route = new Rota(idRouteAux, edges);
                    
                    // Adiciona a rota à lista de rotas
                    routes.add(route);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Retorna a lista de rotas criadas a partir do arquivo XML
        return routes;
    }
}
