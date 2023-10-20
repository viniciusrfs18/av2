package io.sim;

import io.sim.Simulador.EnvSimulator;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {

        //Crypto c = new Crypto();
        EnvSimulator ev = new EnvSimulator();
        ev.start();

    }
}
