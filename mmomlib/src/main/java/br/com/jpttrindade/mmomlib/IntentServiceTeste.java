package br.com.jpttrindade.mmomlib;

import android.app.IntentService;
import android.content.Intent;

public class IntentServiceTeste extends IntentService {

    private static final String NAME = "testeiService";

    public IntentServiceTeste() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

        }
    }

}
