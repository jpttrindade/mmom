package br.com.jpttrindade.mmomlib.mmomserver;

import android.content.Context;
import android.util.Log;

/**
 * Created by jpttrindade on 14/06/16.
 */
public class IMMomServerInstance implements IMMomServer {

    private BrokerConnection brokerConnection;

    IMMomServerInstance(Context context, String brokerHost, int brokerPort) {
        brokerConnection = new BrokerConnection(context, brokerHost, brokerPort);
    }

    @Override
    public void connect(BrokerEventCallback callback) {
        brokerConnection.connect(callback);
    }

    @Override
    public void response(Object data) {
        brokerConnection.response(data);
    }

    @Override
    public void closeConnection() {
        Log.d("DEBUG", "IMMomServerInstance.closeConnection");
        brokerConnection.closeConnection();
    }
}