package br.com.jpttrindade.mmomlib.mmomserver;

import android.content.Context;
import android.util.Log;
import java.io.File;

/**
 * Created by jpttrindade on 14/06/16.
 */
public class IMMomServerInstance implements IMMomServer, EventCallback {

    private BrokerConnection brokerConnection;
    private BrokerEventCallback callback;
    private String responderId ;

    IMMomServerInstance(Context context, String brokerHost, int brokerPort) {
        brokerConnection = new BrokerConnection(context, brokerHost, brokerPort);
    }

    @Override
    public void connect(String responderId, BrokerEventCallback callback) {
        this.responderId = responderId;
        MMomMessage connectionMessage = new MMomMessage();
        connectionMessage.setCode(0);
        connectionMessage.setDestinationId(responderId);
        byte[] connectionData = MMomMessageEncoder.encode(connectionMessage);
        brokerConnection.connect(responderId, connectionData, this);
        this.callback = callback;
    }



    @Override
    public void response(String requestorId, String requestId, String response) {


        MMomMessage message = new MMomMessage();
        message.setCode(MMomMessage.CODE_RESPONSE);
        message.setDestinationId(requestorId);
        message.setRequestId(requestId);
        message.setType(MMomMessage.TEXT);
        message.setTextContent(response);

        Log.d("DEBUG", ">\n"+message.toString());
        byte[] data = MMomMessageEncoder.encode(message);
        brokerConnection.response(data);
    }

    @Override
    public void response(String requestorId, String requestId, File response) {
        MMomMessage message = new MMomMessage();

        message.setCode(MMomMessage.CODE_RESPONSE);
        message.setDestinationId(requestorId);
        message.setRequestId(requestId);
        message.setType(MMomMessage.FILE);
        message.setFileName(response.getName());
        message.setFileContent(response);

        Log.d("DEBUG", "starting encode");
        byte[] data = MMomMessageEncoder.encode(message);
        Log.d("DEBUG", "ending encode");
        brokerConnection.response(data);
    }

    @Override
    public void closeConnection() {
        Log.d("DEBUG", "IMMomServerInstance.closeConnection");
        brokerConnection.closeConnection();
    }

    @Override
    public void onReceiveRequest(byte[] data) {
        MMomMessage message = MMomMessageEncoder.decode(data);
        callback.onReceiveRequest(message);
    }

    @Override
    public void onConnectionEstablished() {
        callback.onConnectionEstablished();
    }

    @Override
    public void onConnectionClosed() {
        callback.onConnectionClosed();
    }
}
