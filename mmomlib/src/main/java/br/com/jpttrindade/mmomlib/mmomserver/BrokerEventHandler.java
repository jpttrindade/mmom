package br.com.jpttrindade.mmomlib.mmomserver;

import android.os.Handler;
import android.os.Message;

/**
 * Created by jpttrindade on 14/06/16.
 */
public class BrokerEventHandler extends Handler{
    BrokerEventCallback brokerEventCallback;

    public BrokerEventHandler(BrokerEventCallback callback){
        this.brokerEventCallback = callback;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case BrokerConnection.CONNECTION_ESTABLISHED:
                if(brokerEventCallback != null) {
                    brokerEventCallback.onConnectionEstablished();
                }
        }

    }
}
