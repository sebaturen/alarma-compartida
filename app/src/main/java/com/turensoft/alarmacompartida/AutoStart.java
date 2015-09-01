package com.turensoft.alarmacompartida;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.turensoft.alarmacompartida.service.SetAlarma;

public class AutoStart extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            //Mantiene al dia las alarmas...
            context.startService(new Intent(context, SetAlarma.class));
        }
    }
}