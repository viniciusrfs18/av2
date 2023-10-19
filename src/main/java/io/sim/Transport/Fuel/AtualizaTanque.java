package io.sim.Transport.Fuel;

import io.sim.Transport.CarDriver.Car;

public class AtualizaTanque extends Thread {
    Car car;
    double litros;

    public AtualizaTanque(Car _car, double _litros) {
        this.car = _car;
        this.litros = _litros;
    }

    @Override
    public void run() {
        try {
            boolean soNoInicio = true;
            while (!car.getFinalizado()) {
                // System.out.println("TRAVOU AQUI");
                if (soNoInicio) {
                    Thread.sleep(200);
                    soNoInicio = false;
                }

                while (car.isOn_off()) {
                    System.out.println("Velocidade do carro: " + car.getSpeed());
                    if (car.getSpeed() != 0) {
                        car.gastaCombustivel(litros);
                    }
                    Thread.sleep(1000);
                }

                if (!car.isOn_off()) {
                    soNoInicio = true;
                }
                //System.out.println("Não executou o While");
            }
            System.out.println("Finalizando Atualiza Fuel Tank");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
