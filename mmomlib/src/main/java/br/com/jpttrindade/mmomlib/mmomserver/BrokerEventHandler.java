package br.com.jpttrindade.mmomlib.mmomserver;

import android.os.Handler;
import android.os.Message;

/**
 * Created by jpttrindade on 14/06/16.
 */
public class BrokerEventHandler extends Handler{
    private EventCallback brokerEventCallback;

    public BrokerEventHandler() {}
    public BrokerEventHandler(EventCallback callback){
        this.brokerEventCallback = callback;
    }

    public void setBrokerEventCallback(EventCallback eventCallback) {
        this.brokerEventCallback = eventCallback;
    }
    public void removeEventCallcack() {
        this.brokerEventCallback = null;
    }

    @Override
    public void handleMessage(Message msg) {
        if(brokerEventCallback != null) {
            switch (msg.what) {
                case BrokerConnection.CONNECTION_ESTABLISHED:
                    brokerEventCallback.onConnectionEstablished();
                    break;
                case BrokerConnection.CONNECTION_CLOSED_BY_APP:
                    brokerEventCallback.onConnectionClosed();
                    break;
                case BrokerConnection.RECEIVE_REQUEST_MESSAGE:
                    byte[] data = (byte[]) msg.obj;
                    brokerEventCallback.onReceiveRequest(data);
            }
        }

    }
}
