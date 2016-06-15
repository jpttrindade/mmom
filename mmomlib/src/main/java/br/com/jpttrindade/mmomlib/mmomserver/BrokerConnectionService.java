package br.com.jpttrindade.mmomlib.mmomserver;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class BrokerConnectionService extends Service {

    private Looper looper;

    private Messenger toClientHandlerMessenger;

    private Socket socket;
    private PrintStream out;
    private InputStream in;
    private ServiceHandler mServiceHandler;

    private final class ServiceHandler extends Handler{
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BrokerConnection.CONNECT_TO_BROKER: {
                    Log.d("DEBUG", "ServiceHandler - CONNECT_TO_BROKER");

                    String host = (String) msg.obj;
                    int port = msg.arg1;
                    connectToBroker(host, port);
                }
            }
        }
    }

    final Messenger fromClientMessenger = new Messenger(new FromClientHandler());
    class FromClientHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case BrokerConnection.REGISTER_HANDLER:
                    Log.d("DEBUG", "setando toClientHandlerMessenger");
                    toClientHandlerMessenger = msg.replyTo;
                    break;
                case BrokerConnection.CONNECT_TO_BROKER:
                    String host = (String) msg.obj;
                    int port = msg.arg1;
                    connectToBroker(host, port);
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread thread = new HandlerThread("BrokerConnectionService", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceHandler = new ServiceHandler(thread.getLooper());
    }

    private void connectToBroker(final String host, final int port) {
        Log.d("DEBUG", "conectando-se ao Broker");
        if(socket == null || !socket.isConnected()) {
            mServiceHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket = new Socket(host, port);
                        out = new PrintStream(socket.getOutputStream());

                        in = new DataInputStream(socket.getInputStream());
                        notifyConnectionEstablished();
                        //TODO verificar logica de escuta do broker auqi

                        int size = in.read();

                        byte[] data = new byte[3];
                        in.read(data,0,3);
                        System.out.println(new String(data));

                        int fileSize = in.read();

                        System.out.println(""+fileSize);

                        data = new byte[fileSize];
                        in.read(data,0,size);

                        System.out.println(new String(data));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }else{
            Log.d("DEBUG", "socket == "+socket);
            if(socket != null)
                Log.d("DEBUG", "socket == "+socket.isConnected());

            try {
                int size = in.read();

                byte[] data = new byte[size];

                in.read(data, 0, size);


                Log.d("DEBUG", new String(data));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyConnectionEstablished() {
        try {
            Message msg = new Message();
            msg.what = BrokerConnection.CONNECTION_ESTABLISHED;
            toClientHandlerMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return fromClientMessenger.getBinder();
    }
}
