package io.sim;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import io.sim.simulator.fuelStation.FuelStation;

public class FuelStationTest {
    private FuelStation fuelStation;

    // Configuração inicial para os testes
    @Before
    public void setUp() {
        // Iniciando uma instância da FuelStation
        fuelStation = new FuelStation(5678, "localhost");
    }

    // Teste para verificar se a quantidade de litros é decidida corretamente quando há saldo suficiente
    @Test
    public void testDecideQtdLitrosSufficientFunds() {
        double litros = 10;
        double saldoDisp = 100;
        double[] result = fuelStation.decideQtdLitros(litros, saldoDisp);
        assertEquals(58.7, result[0], 0.01); // Preço total
        assertEquals(10000.0, result[1], 0.01); // Quantidade de litros
    }

    // Teste para verificar se a quantidade de litros é decidida corretamente quando o saldo é insuficiente
    @Test
    public void testDecideQtdLitrosInsufficientFunds() {
        double litros = 10;
        double saldoDisp = 50;
        double[] result = fuelStation.decideQtdLitros(litros, saldoDisp);
        assertEquals(46.95, result[0], 0.01); // Preço total
        assertEquals(8000.0, result[1], 0.01); // Quantidade de litros ajustada
    }
}

