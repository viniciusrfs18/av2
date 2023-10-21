package io.sim.Transport.Rotas;

public class Rota {
    // Atributos da Classe
    private String id;    // Identificador único da rota.
    private String edges; // String que descreve as arestas que compõem a rota.

    // Construtor
    public Rota(String id, String edges) {
        this.id = id;
        this.edges = edges;
    }

    // Métodos
    // Obtém o identificador da rota.
    public String getID() {
        return id;
    }

    // Obtém a descrição das arestas que compõem a rota.
    public String getEdges() {
        return edges;
    }
}
