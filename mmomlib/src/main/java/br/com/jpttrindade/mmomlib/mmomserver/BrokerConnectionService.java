package br.com.jpttrindade.mmomlib.mmomserver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;

public class BrokerConnectionService extends Service {

    private Looper looper;

    private Messenger toClientHandlerMessenger;

    private Socket socket;
    private PrintStream out;
    private InputStream in;
    private ServiceHandler mServiceHandler;
    private boolean isBinded;
    private boolean isConnected;

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
                case BrokerConnection.RESPONSE_MESSAGE:
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    //sendConnection(outputStream);
                    sendText("requestId","{\"response\":\"que orgulho meu jovem coração.\"}");
                    break;
                case BrokerConnection.CLOSE_CONNECTION:
                    closeConnection();
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
                        sendConnection();
                        notifyConnectionEstablished();

                        while(true) {
                            int i = socket.getInputStream().read();
                            Log.d("DEBUG", "socket.read = " + i);

                            if (i == -1) {
                                throw new SocketException();
                            } else {
                                Log.d("DEBUG", "i>0");
                                receiveMessage(socket.getInputStream());
                            }

                        }
                    } catch (SocketException e) {
                        //TODO retry
                        if(isConnected) {
                            //PROVAVELMENTE PERDEU A CONEXÃO COM BROKER: 1- o broker caiu 2- internet caiu
                            if(isNetworkAvailable()) {
                                //TODO talvez lancar exceptio quando o broker encerrar a conexao
                                //BROKER CAIU
                                Log.e("DEBUG", "Broker caiu.");
                            } else {
                                //INTERNET CAIU
                                Log.e("DEBUG", "Internet caiu.");
                            }
                        } else {
                            //PROVAVELMENTE ESTA TENTANTO ABRIR CONEXÃO COM O BROKER: 1- o broker esta off 2- sem internet
                            if(isNetworkAvailable()) {
                                //Provavelmente o Broker esta off
                                Log.e("DEBUG", "Broker off.");
                            } else {
                                //Sem conexao com internet para estabelecer conexao com o Broker
                                Log.e("DEBUG", "Sem acesso a internet.");
                            }
                        }

                        if(isBinded) {
                            notifyConnectionClosed(BrokerConnection.CONNECTION_CLOSED_BY_BROKER);
                        } else {
                            Log.e("DEBUG", "service not binded");
                        }

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

    private void receiveMessage(InputStream inputStream) {
        Log.d("DEBUG", "BrokerConnectionService.receiverMessage");
    }


    private void closeConnection() {
        Log.d("DEBUG", "BrokerConnectionService.closeConnection, socket = "+socket);
        try {
            if (socket != null && socket.isConnected()) {
                Log.d("DEBUG", "BrokerConnectionService.closeConnection, socket.close()");
                socket.close();
            }
            notifyConnectionClosed(BrokerConnection.CONNECTION_CLOSED_BY_APP);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void sendText(String requestId, String text) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(1); //type text

            //RequestId
            outputStream.write(requestId.getBytes().length); //RequestId size
            outputStream.write(requestId.getBytes()); //RequestId requestId

            //content
            outputStream.write(text.getBytes().length); //Content size
            outputStream.write(text.getBytes()); //Content content

            outputStream.writeTo(socket.getOutputStream());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendConnection(){
        try {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(0); //type conn
            String uuid = "uuid1";
            //content
            outputStream.write(uuid.getBytes().length); //content size
            outputStream.write(uuid.getBytes()); //content content
            outputStream.writeTo(socket.getOutputStream());
            outputStream.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notifyConnectionEstablished() {
        try {
            isConnected = true;
            Message msg = new Message();
            msg.what = BrokerConnection.CONNECTION_ESTABLISHED;
            toClientHandlerMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void notifyConnectionClosed(int close_id) {
        try {
            Log.d("DEBUG", "notifyConnectionClosed()");
            isConnected = false;
            Message msg = new Message();
            msg.what = close_id;
            toClientHandlerMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("DEBUG", "onUnbind");
        isBinded = false;
        closeConnection();
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        isBinded = true;
        return fromClientMessenger.getBinder();
    }
}
