package br.com.jpttrindade.mmomlib.mmomserver;

/**
 * Created by jpttrindade on 15/06/16.
 */
public interface BrokerEventCallback {
    public void onReceiveRequest(String request);

    public void onConnectionEstablished();

    public void onConnectionClosed();

}
