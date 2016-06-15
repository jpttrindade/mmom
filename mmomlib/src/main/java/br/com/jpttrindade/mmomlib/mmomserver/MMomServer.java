package br.com.jpttrindade.mmomlib.mmomserver;

import android.content.Context;

/**
 * Created by jpttrindade on 14/06/16.
 */
public class MMomServer {

    public static IMMomServer createMMomServer(Context context, String brokerHost, int brokerPort) {
        return new IMMomServerInstance(context, brokerHost, brokerPort);
    }
}
