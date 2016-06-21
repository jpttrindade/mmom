package br.com.jpttrindade.mmomlib.mmomserver;

import java.io.File;

/**
 * Created by jpttrindade on 14/06/16.
 */
public interface IMMomServer {
    public void connect(String responderId, BrokerEventCallback callback);
    public void response(String requestorId, String requestId, String response);
    public void response(String requestorId, String requestId, File response);
    public void closeConnection();
}
