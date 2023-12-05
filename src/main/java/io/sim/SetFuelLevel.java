
package io.sim;


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
                    if (car.getSpeed() > 0) {
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
