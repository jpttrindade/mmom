package br.com.jpttrindade.mmomlib.mmomserver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * Created by jpttrindade on 14/06/16.
 */
public class BrokerConnection implements ServiceConnection{

    public static final int CONNECTION_ESTABLISHED = 0;
    public static final int REGISTER_HANDLER = 1;
    public static final int CONNECT_TO_BROKER = 2;
    public static final int CONNECTION_CLOSED_BY_APP = 3;
    public static final int RESPONSE_MESSAGE = 4;
    public static final int CLOSE_CONNECTION = 5;
    public static final int CONNECTION_CLOSED_BY_BROKER = 6;

    private final String host;
    private final int port;
    private final Context context;

    private boolean mBound;
    private boolean isConnected;
    // Recebe os eventos do broker
    private BrokerEventHandler brokerEventHandler;

    //Encapsula o handler(brokerConnectio->service) de dados enviados para o service
    private Messenger toServiceHandlerMessenger = null;
    //Encapsula o handler(service->brokerConnection) a ser enviado ao service
    final Messenger fromServiceHandlerMessenger = new Messenger(new FromServiceHandler());

    //Handler de recebimento de chamadas/msgs/operacoes vindas do service
    class FromServiceHandler extends Handler {


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECTION_ESTABLISHED:
                    isConnected = true;
                    Message newMsg = Message.obtain(null, CONNECTION_ESTABLISHED);
                    brokerEventHandler.sendMessage(newMsg);
                    break;
                case CONNECTION_CLOSED_BY_BROKER:
                    context.unbindService(BrokerConnection.this);
                    break;
                case CONNECTION_CLOSED_BY_APP:

                    //context.unbindService(BrokerConnection.this);
                    newMsg = Message.obtain(null, CONNECTION_CLOSED_BY_APP);
                    brokerEventHandler.sendMessage(newMsg);

                    toServiceHandlerMessenger = null;
                    mBound = false;
                    isConnected = false;
                    break;
            }
        }
    }




    public BrokerConnection(Context context, String host, int port) {
        Log.d("DEBUG", "instanciando o BrokerConnection");
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public void connect(BrokerEventCallback callback) {
        this.brokerEventHandler = new BrokerEventHandler(callback);
        Log.d("DEBUG", "dando o bindService antes de conectar-se ao broker.");
        context.bindService(new Intent(context,BrokerConnectionService.class), this, Context.BIND_AUTO_CREATE);
    }

    private void realConnect() {
        Log.d("DEBUG", "iniciar conexão com o broker.");

        try {
            Message msg = new Message();
            msg.what = CONNECT_TO_BROKER;
            msg.obj = host;
            msg.arg1 = port;
            toServiceHandlerMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void closeConnection() {
        Log.d("DEBUG", "BrokerConnection.closeConnection");
        if(isConnected) {
            context.unbindService(BrokerConnection.this);
        }
    }

    public void response(Object data) {
        if(isConnected) {
            try {
                Message msg = new Message();
                msg.obj = data;
                msg.what = RESPONSE_MESSAGE;
                toServiceHandlerMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d("DEBUG", "sucesso na conexao com o service!");
        Log.d("DEBUG", "setando toServiceHandlerMessenger");
        toServiceHandlerMessenger = new Messenger(service);
        mBound = true;
        sendfromServiceHandlerMessenger();

        realConnect();


    }



    private void sendfromServiceHandlerMessenger() {
        try {
            Message msg = new Message();
            msg.what = REGISTER_HANDLER;
            msg.replyTo = fromServiceHandlerMessenger;
            toServiceHandlerMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d("DEBUG", "conexao com o service encerrada!");

        toServiceHandlerMessenger = null;
        mBound = false;
        isConnected = false;

    }


}
