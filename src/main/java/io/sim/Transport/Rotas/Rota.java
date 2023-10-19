package io.sim.Transport.Rotas;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Rota {
    private String id;
    private String edges;

    public Rota(String id, String edges) {
        this.id = id;
        this.edges = edges;
    }

    public String getID() {
        return id;
    }

    public String getEdges(){
        return edges;
    }

}
