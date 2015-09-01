package com.turensoft.alarmacompartida;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.turensoft.alarmacompartida.controlador.Alarm;
import com.turensoft.alarmacompartida.controlador.ConnectionDetect;
import com.turensoft.alarmacompartida.service.SetAlarma;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends ActionBarActivity {

    //Constantes:
    public static final String URL_INFO = "http://turensoft.com/B1D4/alarm/get_time.php";
    public static final String ARCHIVO_INFO = "infoAlarma.txt";
    public static final String PREFS_CONFIG = "CONFIG_USER";
    public static final String PREFS_C_EMAIL = "CONFIG_EMAIL";
    public static final String PREFS_C_PASS = "CONFIG_PASS";
    public static final String PREFS_C_ID = "CONFIG_ID";
    public static final String PREFS_C_NOMBRE = "CONFIG_NOMBRE";
    public static final String PREFS_C_AUTENT = "CONFIG_AUTENTIFICADOR";
    public static final String PREFS_C_NO_MOLESTAR = "CONFIG_NO_MOLESTAR";
    public static final String TAG = "Actividad Principal";

    public static final Boolean debug = false;

    public static final int UPDATE_INTERVAL = 1 * 1000;
    public static final int NOTIFICATION_EX = 1;

    //Atributos
    private Context mContext;
    public static Alarm alarm = new Alarm();
    public static int idUser = 0;
    private ProgressDialog pDialog;
    private Timer timeUpdateAlarma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        //Cargando usuario:
        //Restore info
        SharedPreferences sPref = getSharedPreferences(PREFS_CONFIG, 0);
        idUser = sPref.getInt(PREFS_C_ID, 0);
        if(MainActivity.debug) Log.d(TAG, "UsuarioID: " + idUser);
        if(idUser == 0)
        {
            setUser(false);
        }
        else
        {
            ((TextView) findViewById(R.id.bienvenido_tv)).setText(getString(R.string.bienvenido) + " " + sPref.getString(PREFS_C_NOMBRE, ""));
            ((TextView) findViewById(R.id.tv_autentifi)).setText(getString(R.string.autentificador) +" "+ sPref.getString(PREFS_C_AUTENT, ""));
        }

        //Click con el boton de cambio de secion
        (findViewById(R.id.bt_cambio_user)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUser(true);
            }
        });

        //Click en el boton hacerca de..
        (findViewById(R.id.bt_acerca_de)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acercaDe();
            }
        });

        //Click en no-molestar
        ( (Switch) findViewById(R.id.sw_no_molestar)).setChecked(sPref.getBoolean(PREFS_C_NO_MOLESTAR, false));
        ( (Switch) findViewById(R.id.sw_no_molestar)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences settings = getSharedPreferences(PREFS_CONFIG, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(PREFS_C_NO_MOLESTAR, b);
                editor.commit();
            }
        });

        //Generando la alarma y el servicio:
        //Mantiene al dia las alarmas...
        startService(new Intent(mContext, SetAlarma.class));
        //Alarmando (?)
        //alarm.SetAlarm(mContext, 10000);

        //Mostrando tiempo prox alarma...
        if(!Alarm.fechaTextAlarma.equals(""))
        {
            ((TextView) findViewById(R.id.text_alarma_set)).setText(Alarm.fechaTextAlarma);
        }
        timeUpdateAlarma = new Timer();
        timeUpdateAlarma.schedule(new TimerTask() {
            @Override
            public void run()
            {
                if(!Alarm.fechaTextAlarma.equals(""))
                {
                    if(MainActivity.debug) Log.d(TAG, "Actualizando texto...");
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) findViewById(R.id.text_alarma_set)).setText(Alarm.fechaTextAlarma);
                        }
                    });
                }
            }
        }, NOTIFICATION_EX, UPDATE_INTERVAL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_update)
        {
            Toast.makeText(mContext, "Actualizando...", Toast.LENGTH_SHORT).show();
            startService(new Intent(mContext, SetAlarma.class));
        }

        return super.onOptionsItemSelected(item);
    }

    private void acercaDe()
    {
        LayoutInflater factory = LayoutInflater.from(mContext);
        final View textEntryView = factory.inflate(R.layout.acerca_de, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert
                .setTitle(getString(R.string.acerca_de))
                .setView(textEntryView)
                .setCancelable(true)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        alert.show();
    }

    /**
     * El cancelable determina si la ventana se puede "cancelar", para poner si el usuyario quiere cambiar de secion
     * y se arrepiente, o si esta obligado...
     * @param isCancelable
     */
    private void setUser(Boolean isCancelable)
    {
        LayoutInflater factory = LayoutInflater.from(mContext);
        final View textEntryView = factory.inflate(R.layout.user_login, null);

        final EditText user_email = (EditText) textEntryView.findViewById(R.id.text_file_email);
        final EditText user_pass = (EditText) textEntryView.findViewById(R.id.text_file_password);

        final AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert
            .setTitle(getString(R.string.inicio_secion))
            .setView(textEntryView)
            .setCancelable(isCancelable)
            .setPositiveButton(getString(R.string.inicie_secion_bot), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    if(user_email.getText().toString().equals("") || user_email.getText().toString().equals(""))
                    {
                        Toast.makeText(mContext, getString(R.string.user_or_pass_fail), Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                        setUser(false);
                    }
                    else
                    {
                        ValidUser vUser = new ValidUser(user_email.getText().toString(), user_pass.getText().toString());
                        vUser.execute();

                        pDialog = ProgressDialog.show(mContext, getString(R.string.valid_account), getString(R.string.conectando));
                        pDialog.show();

                        dialogInterface.dismiss();
                    }
                }
            });
        alert.show();
    }

    private class ValidUser extends AsyncTask<Void, Integer, Boolean>
    {
        private String urlFinal;
        private String userEmail;
        private String password;
        private String autentificador;
        private int idDelUsuario = 0;
        private String nombreCompleto = "";

        public ValidUser(String userEmail, String userPassword)
        {
            this.userEmail = userEmail;
            this.password = userPassword;
            this.urlFinal = MainActivity.URL_INFO + "?emailAC=" + this.userEmail + "&passAC=" + this.password;
        }

        @Override
        protected void onPreExecute()
        {
        }

        @Override
        protected Boolean doInBackground(Void... voids)
        {
            Boolean estado = false;
            //Revisando si la conexion es valida..
            if (ConnectionDetect.isNetworkAvailable(mContext))
            {
                if(MainActivity.debug) Log.d(TAG, "Tenemos internet...");
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
                            Boolean surcJson = (json.getInt("success") == 1);
                            if (surcJson)
                            {
                                estado = true;
                                JSONArray jArray = json.getJSONArray("user");
                                idDelUsuario = (jArray.getJSONObject(0)).getInt("id");
                                nombreCompleto = (jArray.getJSONObject(0)).getString("name");
                                autentificador = (jArray.getJSONObject(0)).getString("autentification");
                                if(MainActivity.debug) Log.d(TAG, "Tenemos usuario id: "+ idDelUsuario);
                                if(MainActivity.debug) Log.d(TAG, "Tenemos usuario name: "+ nombreCompleto);
                            }
                        } catch (JSONException e) {
                            if(MainActivity.debug) Log.e(TAG, "Falla al ocnvertir en jSon los user");
                        }
                    }
                    br.close();
                } catch (MalformedURLException e) {
                    if(MainActivity.debug) Log.e(TAG, "Error al tomar el URL: " + e.getMessage());
                } catch (IOException e) {
                    if(MainActivity.debug) Log.e(TAG, "Error al abrir la conexion: " + e.getMessage());
                }
            }
            return estado;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean)
        {
            if(aBoolean)
            {
                if(idDelUsuario != 0)
                {
                    //Guardamos los datos en el cache del telefono (?)
                    SharedPreferences settings = getSharedPreferences(PREFS_CONFIG, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(PREFS_C_EMAIL, this.userEmail);
                    editor.putString(PREFS_C_PASS, this.password);
                    editor.putInt(PREFS_C_ID, this.idDelUsuario);
                    editor.putString(PREFS_C_NOMBRE, this.nombreCompleto);
                    editor.putString(PREFS_C_AUTENT, this.autentificador);
                    editor.commit();
                    if(MainActivity.debug) Log.d(TAG, "Usuario guardado.. "+ this.idDelUsuario);

                    //Ejecutamos el servicio de alarma:
                    startService(new Intent(mContext, SetAlarma.class));

                    //shao alerta!
                    pDialog.dismiss();

                    //agregando nombre kawaii
                    ((TextView) findViewById(R.id.bienvenido_tv)).setText(getString(R.string.bienvenido) + " " + this.nombreCompleto);
                    //asignando key autentificador
                    ((TextView) findViewById(R.id.tv_autentifi)).setText(getString(R.string.autentificador) +" "+ this.autentificador);
                }
            }
            else
            {
                pDialog.dismiss();
                setUser(false);
                Toast.makeText(mContext, getString(R.string.user_or_pass_fail), Toast.LENGTH_SHORT).show();
            }
            if(MainActivity.debug) Log.d(TAG, "Terminamos de validar el usuario...");
        }
    }

}
