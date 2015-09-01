package com.turensoft.alarmacompartida.controlador;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.turensoft.alarmacompartida.MainActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Seba on 26/05/26
 * Creditos a:
 * http://stackoverflow.com/questions/6493517/detect-if-android-device-has-internet-connection
 */
public class ConnectionDetect
{

    public static final String TAG = "Prueba Conexion";

    public static boolean hasActiveInternetConnection(Context c)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public static boolean isNetworkAvailable(Context context)
    {
        if (hasActiveInternetConnection(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                if(MainActivity.debug) Log.e(TAG, "IOEx: " + e);
            } catch (Exception e)
            {
                if(MainActivity.debug) Log.e(TAG, "Se ha genrado un error en la conexion: "+ e +"\n\tOJO, desde android 3 no se puede correr en el hilo principal!");
            }
        } else {
            if(MainActivity.debug) Log.e(TAG, "No tienes internet...");
        }
        return false;
    }
}