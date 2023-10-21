package io.sim;

import static org.junit.Assert.*;
import org.junit.Test;
import io.sim.Transport.Rotas.Rota;
import io.sim.Transport.Rotas.routeCreator;

import java.util.ArrayList;

public class RouteCreatorTest {

    @Test
    public void testCriaRotas() {
        String xmlFilePath = "data/dados.xml"; // Substitua pelo caminho real do arquivo XML de teste.

        ArrayList<Rota> routes = routeCreator.criaRotas(xmlFilePath);

        assertNotNull(routes);
        assertFalse(routes.isEmpty());

        for (Rota route : routes) {
            assertNotNull(route);
            assertNotNull(route.getID());
            assertNotNull(route.getEdges());
        }
    }
}

