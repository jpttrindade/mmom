package br.com.jpttrindade.mmomlib.mmomserver;

/**
 * Created by jpttrindade on 19/06/16.
 */
public interface EventCallback {
    public void onReceiveRequest(byte[] data);

    public void onConnectionEstablished();

    public void onConnectionClosed();
}
