package br.com.jpttrindade.mmomlib;

import android.app.IntentService;
import android.content.Intent;

public class IntentServiceTeste extends IntentService {

    private static final String NAME = "testeiService";
    public static final String ACTION_CALLBACK = "myaction.callback";

    public IntentServiceTeste() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Intent it = new Intent();
            it.setAction(ACTION_CALLBACK);
            sendBroadcast(it);
        }




    }

}
