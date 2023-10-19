package io.sim;

import org.json.JSONObject;

import io.sim.Pagamentos.TransferData;
import io.sim.Transport.CarDriver.DrivingData;
import io.sim.Transport.Rotas.Rota;

// Classe para tratar as comunicacoes entre classes
public class JSONConverter {

    //                                        Usadas na lógica de pagamentos
    //--------------------------------------------------------------------------------------------------------------------------

    // -> SERVIDOR
    // Chamada em AccountManipulator
    public static String[] extraiLogin(String loginJSON) {
        JSONObject loginJSONObj = new JSONObject(loginJSON);
        String[] login = new String[] { loginJSONObj.getString("ID do Pagador"), loginJSONObj.getString("Senha do Pagador") };
        return login;
    }

    // -> SERVIDOR
    // Chamada em AccountManipulator
    public static TransferData extraiTransferData(String transferDataJSON) {
        JSONObject transferDataJSONObj = new JSONObject(transferDataJSON);
		String pagador = transferDataJSONObj.getString("ID do Pagador");
        String operacao = transferDataJSONObj.getString("Operacao");
        String recebedor = transferDataJSONObj.getString("ID do Recebedor");
		double valor = transferDataJSONObj.getDouble("valor");
        TransferData tf = new TransferData(pagador, operacao, recebedor, valor);
		return tf;
	}

    // -> SERVIDOR
    // Chamada em AccountManipulator
    public static String criaRespostaTransferencia(boolean sucesso) {
        JSONObject my_json = new JSONObject();
        my_json.put("Resposta", sucesso);
        return my_json.toString();
    }

    // -> CLIENTE
    // Chamada em BotPayment
    public static String criaJSONTransferData(TransferData transferData) {
        JSONObject transferDataJSON = new JSONObject();
		transferDataJSON.put("ID do Pagador", transferData.getPagador());
        transferDataJSON.put("Operacao", transferData.getOperacao());
        transferDataJSON.put("ID do Recebedor", transferData.getRecebedor());
		transferDataJSON.put("valor", transferData.getvalor());
		return transferDataJSON.toString();
	}

    // -> CLIENTE
    // Chamada em BotPayment
    public static String criarJSONLogin(String[] login) {
        JSONObject loginJSONObj = new JSONObject();
        loginJSONObj.put("ID do Pagador", login[0]);
		loginJSONObj.put("Senha do Pagador", login[1]);
        return loginJSONObj.toString();
    }

    // -> CLIENTE
    // Chamada em BotPayment
    public static boolean extraiResposta(String respostaJSON) {
        JSONObject resposta = new JSONObject(respostaJSON);
        return resposta.getBoolean("Resposta");
    }

    //                                    Usadas na lógica de tratamento de rotas
    //--------------------------------------------------------------------------------------------------------------------------

    // CarManipulator
    public static String criaJSONRota(Rota rota) {
        JSONObject rotaJSON = new JSONObject();
        rotaJSON.put("ID da Rota", rota.getID());
        rotaJSON.put("Edges", rota.getEdges());
        return rotaJSON.toString();
    }

    // CarManipulator
    public static DrivingData extraiDrivingData(String drivingDataJSON) {
		JSONObject drivingDataJSONObj = new JSONObject(drivingDataJSON);
        String carID = drivingDataJSONObj.getString("Car ID");
        String driverID = drivingDataJSONObj.getString("Driver ID");
        String carStatus = drivingDataJSONObj.getString("Car Status");
		double latInicial = drivingDataJSONObj.getDouble("Latitude Inicial");
        double lonInicial = drivingDataJSONObj.getDouble("Longitude Inicial");
        double latAtual = drivingDataJSONObj.getDouble("Latitude Atual");
        double lonAtual = drivingDataJSONObj.getDouble("Longitude Atual");
        long timeStamp = drivingDataJSONObj.getLong("TimeStamp");
        double X_Position = drivingDataJSONObj.getDouble("X_Position");
        double Y_Position = drivingDataJSONObj.getDouble("Y_Position");
        String roadIDSUMO = drivingDataJSONObj.getString("RoadIDSUMO");
        String routeIDSUMO = drivingDataJSONObj.getString("RouteIDSUMO");
        double speed = drivingDataJSONObj.getDouble("Speed");
        double odometer = drivingDataJSONObj.getDouble("Odometer");
        double fuelConsumption = drivingDataJSONObj.getDouble("FuelConsumption");
        double averageFuelConsumption = drivingDataJSONObj.getDouble("AverageFuelConsumption");
        int fuelType = drivingDataJSONObj.getInt("FuelType");
        double fuelPrice = drivingDataJSONObj.getDouble("FuelPrice");
        double Co2Emission = drivingDataJSONObj.getDouble("Co2Emission");
        double HCEmission = drivingDataJSONObj.getDouble("HCEmission");
        int personCapacity = drivingDataJSONObj.getInt("PersonCapacity");
        int personNumber = drivingDataJSONObj.getInt("PersonNumber");

        DrivingData drivingData = new DrivingData(carID, driverID, carStatus, latInicial, lonInicial, latAtual, lonAtual, timeStamp, 
        X_Position, Y_Position, roadIDSUMO, routeIDSUMO, speed, odometer, fuelConsumption, averageFuelConsumption, 
        fuelType, fuelPrice, Co2Emission, HCEmission, personCapacity, personNumber);

		return drivingData;
	}

    // Car
    public static Rota extraiRota(String rotaJSON) {
        JSONObject rotaJSONObj = new JSONObject(rotaJSON);
		Rota rota = new Rota(rotaJSONObj.getString("ID da Rota"), rotaJSONObj.getString("Edges"));
        return rota;
	}

    // Car
	public static String criarJSONDrivingData(DrivingData drivingData) {
        JSONObject drivingDataJSON = new JSONObject();
        drivingDataJSON.put("Car ID", drivingData.getCarID());
        drivingDataJSON.put("Driver ID", drivingData.getDriverID());
        drivingDataJSON.put("Car Status", drivingData.getCarStatus());
		drivingDataJSON.put("Latitude Inicial", drivingData.getLatInicial());
        drivingDataJSON.put("Longitude Inicial", drivingData.getLonInicial());
        drivingDataJSON.put("Latitude Atual", drivingData.getLatAtual());
        drivingDataJSON.put("Longitude Atual", drivingData.getLonAtual());
		drivingDataJSON.put("TimeStamp", drivingData.getTimeStamp());
		drivingDataJSON.put("X_Position", drivingData.getX_Position());
		drivingDataJSON.put("Y_Position", drivingData.getY_Position());
		drivingDataJSON.put("RoadIDSUMO", drivingData.getRoadIDSUMO());
		drivingDataJSON.put("RouteIDSUMO", drivingData.getRouteIDSUMO());
		drivingDataJSON.put("Speed", drivingData.getSpeed());
		drivingDataJSON.put("Odometer", drivingData.getOdometer());
		drivingDataJSON.put("FuelConsumption", drivingData.getFuelConsumption());
		drivingDataJSON.put("AverageFuelConsumption", drivingData.getAverageFuelConsumption());
		drivingDataJSON.put("FuelType", drivingData.getFuelType());
		drivingDataJSON.put("FuelPrice", drivingData.getFuelPrice());
		drivingDataJSON.put("Co2Emission", drivingData.getCo2Emission());
		drivingDataJSON.put("HCEmission", drivingData.getHCEmission());
		drivingDataJSON.put("PersonCapacity", drivingData.getPersonCapacity());
		drivingDataJSON.put("PersonNumber", drivingData.getPersonNumber());
        return drivingDataJSON.toString();
	}
}
