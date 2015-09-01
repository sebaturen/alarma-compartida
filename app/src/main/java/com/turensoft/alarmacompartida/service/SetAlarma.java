package com.turensoft.alarmacompartida.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.turensoft.alarmacompartida.MainActivity;
import com.turensoft.alarmacompartida.R;
import com.turensoft.alarmacompartida.controlador.ConnectionDetect;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Seba on 26/05/2015.
 */
public class SetAlarma extends Service
{
    //Constantes
    public static final String TAG = "ServicioDeSeteo";
    public static final int UPDATE_INTERVAL = 60 * 1000;
    public static final int NOTIFICATION_EX = 1;

    //Atributos
    private Timer timer = null;
    private Context mContext = this;
    private JSONObject json;
    private RevisandoSet rs;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(timer == null)
        {
            SharedPreferences sPref = getSharedPreferences(MainActivity.PREFS_CONFIG, 0);
            MainActivity.idUser = sPref.getInt(MainActivity.PREFS_C_ID, 0);
            if(MainActivity.idUser != 0)
            {
                loadTime();
                JodaTimeAndroid.init(this);
            }
            else
            {
                Toast.makeText(mContext, getString(R.string.inicie_secion_alert), Toast.LENGTH_LONG).show();
                onDestroy();
            }
        }
        else
        {
            //force update.
            loadTime();
        }
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {

    }

    private void loadTime()
    {
        //Time:
        timer = new Timer();
        rs = new RevisandoSet();
        timer.schedule(rs, NOTIFICATION_EX, UPDATE_INTERVAL);
    }

    private class RevisandoSet extends TimerTask
    {
        @Override
        public void run()
        {
            if(MainActivity.idUser != 0)
            {
                //Checando si ahi conexion a la internete
                if(ConnectionDetect.isNetworkAvailable(mContext))
                {
                    getInfo();
                    //Un pequeño randomo para validar el userID
                    if( (int) (Math.random()*10) > 5 )
                    {
                        if(MainActivity.debug) Log.d(TAG, "Toca revisar user...");
                        validUser();
                    }
                }
                setAlarm();
            }
        }

        //Validando usuario
        private void validUser()
        {
            SharedPreferences sPref = getSharedPreferences(MainActivity.PREFS_CONFIG, 0);
            MainActivity.idUser = sPref.getInt(MainActivity.PREFS_C_ID, 0);
            String userEmail = sPref.getString(MainActivity.PREFS_C_EMAIL, "");
            String password =  sPref.getString(MainActivity.PREFS_C_PASS, "");
            String urlFinal = MainActivity.URL_INFO + "?emailAC=" + userEmail + "&passAC=" + password;
            try
            {
                //Actualizando la info:
                URL url = new URL(urlFinal);
                URLConnection uc = url.openConnection();
                uc.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                String linea = br.readLine();

                if(MainActivity.debug) Log.d(TAG, "Linea: " + linea);

                //Grabando el archivo, en caso que no exista, lo crea.
                if (!linea.equals(""))
                {
                    try {
                        JSONObject json = new JSONObject(linea);
                        int idDelUsuario = json.getInt("success");
                        if (idDelUsuario == 0)
                        {
                            MainActivity.idUser = 0;

                            //save new config...
                            SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_CONFIG, 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString(MainActivity.PREFS_C_EMAIL, "");
                            editor.putString(MainActivity.PREFS_C_PASS, "");
                            editor.putInt(MainActivity.PREFS_C_ID, 0);
                            editor.commit();
                        }
                        if(MainActivity.debug) Log.d(TAG, "User ID Encontrado..."+ idDelUsuario);
                    } catch (JSONException e) {
                        if(MainActivity.debug) Log.e(TAG, "Falla al ocnvertir en jSon los user");
                    }
                }
                br.close();
            } catch (MalformedURLException e) {
                if(MainActivity.debug) Log.e(TAG, "Error al tomar el URL: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                if(MainActivity.debug) Log.e(TAG, "Error al abrir la conexion: " + e.getMessage());
                e.printStackTrace();
            }
        }

		//Recojemos la info desde internet.
        private void getInfo()
        {
            try{
                //Actualizando la info:
                String urlUser = MainActivity.URL_INFO +"?user="+ MainActivity.idUser;
                URL url = new URL(urlUser);
                URLConnection uc = url.openConnection();
                uc.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                String linea = br.readLine();

                if(MainActivity.debug) Log.d(TAG, "Linea: "+ linea);

                //Grabando el archivo, en caso que no exista, lo crea.
                if(!linea.equals(""))
                {
                    OutputStreamWriter outSWMensaje = new OutputStreamWriter(openFileOutput(MainActivity.ARCHIVO_INFO, Context.MODE_PRIVATE));
                    outSWMensaje.write(linea);
                    outSWMensaje.close();
                }

                br.close();
            } catch (MalformedURLException e) {
                if(MainActivity.debug) Log.e(TAG, "Error al tomar el URL: " + e.getMessage());
            } catch (IOException e) {
                if(MainActivity.debug) Log.e(TAG, "al abrir la conexion: " + e.getMessage());
            } finally {
            }
        }
    
		//generamos el alarm manager.
		private void setAlarm()
		{
			//En caso que tengamos el archivo con la informion
			if(existsFile(MainActivity.ARCHIVO_INFO))
			{
                try
                {
                    //Leemos el archivo:
                    BufferedReader brArchivo = new BufferedReader(new InputStreamReader(openFileInput(MainActivity.ARCHIVO_INFO)));
                    String conf = brArchivo.readLine();
                    if(conf != null)
                    {
                        if(MainActivity.debug) Log.d(TAG, "Lect: "+ conf);
                        //Creamos un jSON con el contenido.
                        json = new JSONObject(conf);
                        //Tengo que separarlo en otra thread...
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    JSONArray tiempos = json.getJSONArray("tiempos");
                                    for(int i = 0; i < tiempos.length(); i++)
                                    {
                                        JSONObject time = tiempos.getJSONObject(i);
                                        DateTime tiempo = null;
                                        Boolean estado = false;

                                        try
                                        {
                                            estado = (time.getString("estado").equals("1"));
                                            String calend = time.getString("tiempo");
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                            tiempo = new DateTime(sdf.parse(calend).getTime());
                                        }
                                        catch (ParseException e)
                                        {
                                            if(MainActivity.debug) Log.e(TAG, "Fallo al transformar la fecha: "+ e);
                                        }
                                        catch (JSONException e)
                                        {
                                            if(MainActivity.debug) Log.e(TAG, "Falla la conversion json: "+ e);
                                        }

                                        if(estado)
                                        {
                                            if(tiempo != null)
                                            {
                                                DateTime nowTime = new DateTime();
                                                Long diff = tiempo.getMillis() - nowTime.getMillis();
                                                if(MainActivity.debug) Log.d(TAG, " - Tiempo: "+ tiempo.toString());
                                                if(MainActivity.debug) Log.d(TAG, "Tiemp current (?): "+ nowTime.toString());
                                                if(MainActivity.debug) Log.d(TAG, "Diferencia: "+ diff);
                                                if(diff > 0)
                                                {
                                                    Long sonaria =  System.currentTimeMillis() +  diff;
                                                    if ((sonaria - MainActivity.alarm.getTimeAlarm()) > 10000)
                                                    {
                                                        if(MainActivity.debug) Log.d(TAG, "Sonaria: " + sonaria + " y la actual en: " + MainActivity.alarm.getTimeAlarm());
                                                        MainActivity.alarm.setAlarm(mContext, diff);
                                                    }
                                                }
                                            }
                                        }
                                        else
                                        {
                                            if(tiempo != null)
                                            {
                                                DateTime nowTime = new DateTime();
                                                Long diff = tiempo.getMillis() - nowTime.getMillis();
                                                if(diff > 0)
                                                {
                                                    Long sonaria =  System.currentTimeMillis() +  diff;

                                                    if(MainActivity.debug) Log.d(TAG, " - Tiempo: "+ tiempo.toString());
                                                    if(MainActivity.debug) Log.d(TAG, "Tiemp current (?): "+ nowTime.toString());
                                                    if(MainActivity.debug) Log.d(TAG, "Diferencia: "+ diff);
                                                    if(MainActivity.debug) Log.d(TAG, "Sonaria... "+ sonaria +" actual - "+ MainActivity.alarm.getTimeAlarm() + " la otra wea "+ (sonaria - MainActivity.alarm.getTimeAlarm()));
                                                    if ((sonaria - MainActivity.alarm.getTimeAlarm()) < 10000 && (sonaria - MainActivity.alarm.getTimeAlarm()) > -10000 && MainActivity.alarm.getTimeAlarm() > 0)
                                                    {
                                                        if(MainActivity.debug) Log.d(TAG, "Se cancela esta alarma...");
                                                        MainActivity.alarm.cancelAlarm(mContext);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                catch (JSONException e)
                                {
                                    if(MainActivity.debug) Log.e(TAG, "Fallo el jSON: "+ e);
                                }
                            }
                        }).start();

                    }
                    brArchivo.close();
                }
                catch (IOException e)
                {
                    if(MainActivity.debug) Log.e(TAG, "Error al leer el archivo: "+ e);
                }
                catch (JSONException e)
                {
                    if(MainActivity.debug) Log.e(TAG, "Fallo el jSON: "+ e);
                }
				//Tomamos la hora de la alarma, y verificamos
				//que no se halla creado ya, obteniendo los segundos que faltan
				//y comparandolos con el objeto alarma.
				//En caso de que no este, lo setiamos.
				//Por ultimo revisar si se ha marcado como "desactivado", la alarma
				//tiene que verificar si esta activa dicha alarm y cancelarla.
			}
		}

        public boolean existsFile(String fileName)
        {
            for (String tmp : fileList())
            {
                if (tmp.equals(fileName))
                    return true;
            }
            return false;
        }

	}
}
