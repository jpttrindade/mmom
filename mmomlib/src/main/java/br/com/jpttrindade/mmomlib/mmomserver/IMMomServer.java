package br.com.jpttrindade.mmomlib.mmomserver;

/**
 * Created by jpttrindade on 14/06/16.
 */
public interface IMMomServer {
    public void connect(BrokerEventCallback callback);
    public void response(Object data);
}
