package com.turensoft.alarmacompartida.controlador;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;

import com.turensoft.alarmacompartida.MainActivity;
import com.turensoft.alarmacompartida.R;

import org.joda.time.DateTime;

/**
 * Created by Seba on 26/05/2015.
 */
public class Alarm extends BroadcastReceiver
{
    //
    public static final String TAG = "Alarma";
    public static String fechaTextAlarma = "";

    //Atributios ()?
    private long timeAlarm = 0;
    @Override
    public void onReceive(Context context, Intent intent)
    {
        SharedPreferences sPref = context.getSharedPreferences(MainActivity.PREFS_CONFIG, 0);
        Boolean noMolestar = sPref.getBoolean(MainActivity.PREFS_C_NO_MOLESTAR, true);
        if(MainActivity.debug) Log.d(TAG, "recivido!, noMolestar es: "+ noMolestar);

        if(!noMolestar)
        {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
            wl.acquire();

            Intent i = new Intent();
            i.setClassName("com.turensoft.alarmacompartida", "com.turensoft.alarmacompartida.Sonando");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

            wl.release();
        }
        else
        {
            cancelAlarm(context);
        }

    }

    /**
     * Time in MilliSecond!
     * @param context
     * @param milliSecondTime
     */
    public void setAlarm(Context context, long milliSecondTime)
    {
        AlarmManager am =( AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, Alarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        if(MainActivity.debug) Log.d(TAG, "Servicio alarma iniciado");
        this.timeAlarm = System.currentTimeMillis() +  milliSecondTime;
        am.set(AlarmManager.RTC_WAKEUP, timeAlarm, pi);

        DateTime temp = new DateTime(this.timeAlarm);
        int mn = temp.getMinuteOfHour();
        String minute = ((mn > 10)? (mn+""):("0"+mn));
        String textFecha = temp.getHourOfDay() +":"+ minute +"\n"+ temp.getDayOfMonth() +"/"+ temp.getMonthOfYear() +"/"+ temp.getYear();
        fechaTextAlarma = textFecha;
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);

        this.timeAlarm = 0;
        fechaTextAlarma = context.getString(R.string.no_prox_alarm);
    }

    public long getTimeAlarm()
    {
        return this.timeAlarm;
    }

}
