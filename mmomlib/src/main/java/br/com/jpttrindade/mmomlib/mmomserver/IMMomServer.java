package br.com.jpttrindade.mmomlib.mmomserver;

import java.io.File;

/**
 * Created by jpttrindade on 14/06/16.
 */
public interface IMMomServer {
    public void connect(String responderId, BrokerEventCallback callback);
    public void response(String response);
    public void response(File response);
    public void closeConnection();
}
