package br.com.jpttrindade.mmomlib.mmomserver;

import android.os.Handler;
import android.os.Message;

/**
 * Created by jpttrindade on 14/06/16.
 */
public class BrokerEventHandler extends Handler{
    private BrokerEventCallback brokerEventCallback;

    public BrokerEventHandler() {}
    public BrokerEventHandler(BrokerEventCallback callback){
        this.brokerEventCallback = callback;
    }

    public void setBrokerEventCallback(BrokerEventCallback eventCallback) {
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
            }
        }

    }
}
