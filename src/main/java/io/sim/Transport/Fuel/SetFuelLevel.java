package io.sim.Transport.Fuel;

import io.sim.Transport.CarDriver.Car;

// Classe responsável por realizar o gasto de combustível dos carros a cada 1 segundo
public class SetFuelLevel extends Thread {
    Car car;        // O carro ao qual esse gasto de combustível está associado
    double litros;  // A quantidade de combustível gasta a cada intervalo

    public SetFuelLevel(Car _car, double _litros) {
        this.car = _car;     // Inicializa o carro associado
        this.litros = _litros; // Inicializa a quantidade de combustível gasta a cada intervalo
    }

    @Override
    public void run() {
        try {
            boolean toStart = true; // Variável para controlar o início do loop
            while (!car.getFinalizado()) { // Continua até que o carro seja finalizado
                
                if (toStart) {
                    Thread.sleep(200); // Aguarda 200 milissegundos antes de continuar
                    toStart = false;
                }

                while (car.isOn_off()) { // Enquanto o carro estiver ligado
                    if (car.getSpeed() > 0) { // Apenas gasta combustível se o carro não estiver parado.
                        car.gastaCombustivel(litros); // Chama o método para gastar combustível no carro
                    }
                    Thread.sleep(1000); // Aguarda 1 segundo antes de gastar mais combustível
                }

                if (!car.isOn_off()) {
                    toStart = true; // Quando o carro é desligado, a variável toStart é redefinida para true
                }
            }

            System.out.println("Finalizando SetFuelLevel"); // Mensagem indicando o encerramento da thread

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
