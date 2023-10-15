package io.sim.Rotas;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Rotas implements Serializable
{
    private String edges; // Representa os pontos de uma Rota
    private String routeID; // Id de uma determinada Rota

    public Rotas(String routeID, String edges) // Construtor da Classe Rotas
    {
        this.edges = edges;
        this.routeID = routeID;
    }

    public String getRouteID(){ // Pega a ID de uma Rota
        return this.routeID;
    }

    public String getEdges(){ // Pega as Edges de uma Rota
        return this.edges;
    }
}
