package io.sim.Transport.Rotas;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class routeCreator {

     public static ArrayList<Rota> criaRotas(String xmlFilePath) {
        ArrayList<Rota> routes = new ArrayList<>();
        
        try {
            File xmlFile = new File(xmlFilePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            NodeList vehicleList = doc.getElementsByTagName("vehicle");
            
            for (int i = 0; i < vehicleList.getLength(); i++) {
                Element vehicleElement = (Element) vehicleList.item(i);
                String idRouteAux = vehicleElement.getAttribute("id");
                NodeList routeList = vehicleElement.getElementsByTagName("route");
                
                for (int j = 0; j < routeList.getLength(); j++) {
                    Element routeElement = (Element) routeList.item(j);
                    String edges = routeElement.getAttribute("edges");
                
                    // Crie um objeto Route para cada edge
                    Rota route = new Rota(idRouteAux, edges);
                    routes.add(route);
                    
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return routes;
    }

}
