package br.com.jpttrindade.mmomlib.mmomserver;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;

public class BrokerConnectionService extends Service {

    static final String RESPONDER_ID = "ResponderID";
    static final String CONNECTION_DATA = "coNNectionDATA";

    private Messenger toClientHandlerMessenger;
    private Socket socket;

    private ServiceHandler mServiceHandler;
    private ServiceHandler mServiceHandlerResponse;

    private boolean isBinded;
    private boolean isConnected;
    private String responderId;
    private byte[] connectionData;


    private final class ServiceHandler extends Handler{
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BrokerConnection.CONNECT_TO_BROKER:
                    Log.d("DEBUG", "ServiceHandler - CONNECT_TO_BROKER");

                    String host = (String) msg.obj;
                    int port = msg.arg1;
                    connectToBroker(host, port, connectionData);
                break;
                case BrokerConnection.RESPONSE_MESSAGE:
                    Log.d("DEBUG", "ServiceHandler - RESPONSE_MESSAGE");
                    byte[] response = (byte[])msg.obj;
                    sendResponse(response);
                    break;
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
                    Bundle b = msg.getData();
                    byte[] connectionData = b.getByteArray(BrokerConnectionService.CONNECTION_DATA);
                    connectToBroker(host, port, connectionData);
                    break;
                case BrokerConnection.RESPONSE_MESSAGE:
                    Log.d("DEBUG", "service recenbendo o dataToSend");
                    final byte[] response = (byte[]) msg.obj;
                    Message threadMessage = mServiceHandlerResponse.obtainMessage(BrokerConnection.RESPONSE_MESSAGE);
                    threadMessage.obj = response;
                    mServiceHandlerResponse.sendMessage(threadMessage);
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

        HandlerThread thread2 = new HandlerThread("BrokerConnectionService", Process.THREAD_PRIORITY_BACKGROUND);
        thread2.start();

        mServiceHandler = new ServiceHandler(thread.getLooper());
        mServiceHandlerResponse = new ServiceHandler(thread2.getLooper());
    }

    private void connectToBroker(final String host, final int port, final byte[] connectionData) {
        this.connectionData = connectionData;
        Log.d("DEBUG", "conectando-se ao Broker");
        if(socket == null || !socket.isConnected()) {
            mServiceHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket = new Socket(host, port);
                        sendResponse(connectionData);
                        notifyConnectionEstablished();
                        receiveRequest();

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
                                if(isBinded) {
                                    Log.e("DEBUG", "Broker off.");
                                }else {
                                    Log.e("DEBUG", "AppClient foi desligado.");
                                }
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
        }
    }

    private void receiveRequest() throws IOException {

        ByteArrayOutputStream byteArrayOutputStream;
        while(true) {
            byteArrayOutputStream = new ByteArrayOutputStream();
            int size = 1024;
            byte[] buffer = new byte[size];
            int len;
            while ((len = socket.getInputStream().read(buffer, 0, size)) != -1){
                byteArrayOutputStream.write(buffer, 0, len);
                getMessage(byteArrayOutputStream.toByteArray());
                byteArrayOutputStream.flush();
            }
            if (len == -1) {
                byteArrayOutputStream.close();
                throw new SocketException();
            }
        }
    }

    private void getMessage(byte[] buffer) {
        Log.d("DEBUG", "BrokerConnectionService.receiverMessage");
        try {
            Message msg = new Message();
            msg.what = BrokerConnection.RECEIVE_REQUEST_MESSAGE;
            msg.obj = buffer;
            toClientHandlerMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void closeConnection() {
        Log.d("DEBUG", "BrokerConnectionService.closeConnection, socket = "+socket);
        try {
            if (socket != null && isConnected) {
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

    private void sendResponse(byte[] response) {
        try {
            Log.d("DEBUG", "response.length: "+response.length);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(response);
            OutputStream outputStream = socket.getOutputStream();
            byte[] buffer = new byte[2*1024];
            int count;
            while ((count = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, count);
            }
            inputStream.close();
            outputStream.flush();

        } catch (IOException e) {
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

        this.responderId = intent.getStringExtra(RESPONDER_ID);
        isBinded = true;
        return fromClientMessenger.getBinder();
    }
}
