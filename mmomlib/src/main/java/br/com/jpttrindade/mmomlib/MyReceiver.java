package br.com.jpttrindade.mmomlib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

public class MyReceiver extends BroadcastReceiver {
    private final Class activity;

    public MyReceiver(Class activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Objects.equals(intent.getAction(), IntentServiceTeste.ACTION_CALLBACK)){
            Intent it = new Intent(context, activity);
            context.startActivity(it);
        }
    }
}

