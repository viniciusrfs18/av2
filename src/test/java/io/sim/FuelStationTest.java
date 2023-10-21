package io.sim;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import io.sim.Transport.Fuel.FuelStation;

public class FuelStationTest {

    private FuelStation fuelStation;

    @Before
    public void setUp() {
        // Configure as informações necessárias, como o host e a porta do AlphaBank, antes de criar a instância da FuelStation.
        fuelStation = new FuelStation(12345, "localhost");
        fuelStation.start();
    }

    @Test
    public void testGetPreco() {
        double expectedPreco = 5.87; // Substitua pelo amount esperado do preço do litro de gasolina.
        double actualPreco = fuelStation.getPreco();

        assertEquals(expectedPreco, actualPreco, 0.01); // Use uma margem de erro de 0.01 para lidar com a precisão de ponto flutuante.
    }

    // Implemente testes adicionais, incluindo testes para o método fuelCar, conforme necessário.
}

