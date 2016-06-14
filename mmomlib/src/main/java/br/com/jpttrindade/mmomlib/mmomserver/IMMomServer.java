package br.com.jpttrindade.mmomlib.mmomserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by jpttrindade on 14/06/16.
 */
public class IMMomServer {
    Socket socket;
    private final int defaultBrokerPort = 1992;
    BufferedOutputStream outputStream;

    IMMomServer(String broker) {
        try {
            socket = new Socket(broker, defaultBrokerPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setHandler() {

    }

    public void connect() {
        try {
            outputStream = new BufferedOutputStream(socket.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
