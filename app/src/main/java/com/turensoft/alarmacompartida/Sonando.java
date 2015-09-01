package com.turensoft.alarmacompartida;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Created by Seba on 28/05/2015.
 */
public class Sonando extends Activity
{
    public static final String TAG = "Sonando!!";

    //Atributos...
    private Context mContext;

    private Vibrator vibrator;
    private MediaPlayer mp;
    private AudioManager am;
    private Boolean vibrando = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarma);
        mContext = this;

        vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        mp = MediaPlayer.create(mContext, R.raw.sonido_despertador);
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //Ejecutando hasta in hold...
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        //Boton de apagado
        Button apagado = (Button) findViewById(R.id.boton_apagar);
        apagado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                MainActivity.alarm.cancelAlarm(mContext);
                finish();
            }
        });

        //Boton de 5m mas
        Button sincoMas = (Button) findViewById(R.id.buton_suspender_5);
        sincoMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                MainActivity.alarm.setAlarm(mContext, 300000);
                finish();
            }
        });


        //Vibrando!
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                while (vibrando)
                {
                    //Codigo para vibrar (?)
                    try
                    {
                        vibrator.vibrate(2000);
                        Thread.sleep(4000);
                    }
                    catch (InterruptedException e)
                    {
                        if(MainActivity.debug) Log.e(TAG, "Fallo al dormir la alarma..." + e);
                    }

                }
            }

        }).start();

        //Sonando!
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                mp.start();

                //Ajustando el sonido alto...
                am.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                        0);


            }
        }).start();

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        vibrando = false;
        if(mp.isPlaying())
        {
            mp.stop();
        }
        vibrator.cancel();
        mp.reset();
        mp.release();
    }
}
