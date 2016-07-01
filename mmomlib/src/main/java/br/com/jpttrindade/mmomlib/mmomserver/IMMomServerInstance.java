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
    private MMomMessage reqMessage;

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
    public void response(String response) {


        MMomMessage message = new MMomMessage();
        message.setCode(MMomMessage.CODE_RESPONSE);
        message.setDestinationId(reqMessage.requestorId);
        message.setRequestId(reqMessage.requestId);
        message.setType(MMomMessage.TEXT);
        message.setTextContent(response);

        Log.d("DEBUG", ">\n"+message.toString());
        byte[] data = MMomMessageEncoder.encode(message);
        brokerConnection.response(data);
    }

    @Override
    public void response(File response) {
        MMomMessage message = new MMomMessage();

        message.setCode(MMomMessage.CODE_RESPONSE);
        message.setDestinationId(message.requestorId);
        message.setRequestId(message.requestId);
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
        reqMessage = MMomMessageEncoder.decode(data);


        callback.onReceiveRequest(reqMessage.textContent);
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
