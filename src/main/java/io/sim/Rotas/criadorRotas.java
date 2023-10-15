package io.sim.Rotas;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class criadorRotas {
    
    public criadorRotas(){}

    public static ArrayList<Rotas> extract(String arqXML) throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(arqXML); // arquivo das rotas no formato Documents
        NodeList nList = doc.getElementsByTagName("vehicle"); // lista de rotas com a tag vehicle
        ArrayList<Rotas> routes = new ArrayList<Rotas>();
        
        for (int i = 0; i < nList.getLength(); i++)
        {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element elem = (Element) nNode; // pausa 
                String idRouteAux = elem.getAttribute("id"); 
                Node node = elem.getElementsByTagName("route").item(0);
                Element edges = (Element) node; // extrai as edges -> FALTA ver se eh o primeiro ponto ou a edge toda
                Rotas route = new Rotas(idRouteAux, edges.getAttribute("edges"));
                routes.add(route);
            }
        }

        return routes;
    }

}
