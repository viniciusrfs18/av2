package io.sim.Transport.Fuel;

import io.sim.Transport.CarDriver.Car;

// Classe responsável por realizar o gasto de combustível dos carros a cada 1segundo
public class SetFuelLevel extends Thread {
    Car car;
    double litros;

    public SetFuelLevel(Car _car, double _litros) {
        this.car = _car;
        this.litros = _litros;
    }

    @Override
    public void run() {
        try {
            boolean toStart = true;
            while (!car.getFinalizado()) {
                
                if (toStart) {
                    Thread.sleep(200);
                    toStart = false;
                }

                while (car.isOn_off()) {
                    if (car.getSpeed() > 0) { // Apenas gasta combustível se o carro não estiver parado.
                        car.gastaCombustivel(litros);
                    }
                    Thread.sleep(1000);
                }

                if (!car.isOn_off()) {
                    toStart = true;
                }

            }

            System.out.println("Finalizando SetFuelLevel");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
