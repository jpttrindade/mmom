package br.com.jpttrindade.mmomlib.mmomserver;

/**
 * Created by jpttrindade on 14/06/16.
 */
public class MMomServer {

    public static IMMomServer createMMomServer(String broker) {
        IMMomServer imMomServer = new IMMomServer(broker);

        return imMomServer;
    }
}
