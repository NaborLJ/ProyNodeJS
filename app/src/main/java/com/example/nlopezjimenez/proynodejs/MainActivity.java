package com.example.nlopezjimenez.proynodejs;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.json.JSONException;

import static com.example.nlopezjimenez.proynodejs.R.id.mensaje;
import static com.example.nlopezjimenez.proynodejs.R.id.textmensaje;

public class MainActivity extends AppCompatActivity

        implements NavigationView.OnNavigationItemSelectedListener {
    JSONObject json,clienteRX;

    private WebSocketClient mWebSocketClient;
    public static String nickname;
    private static final int MY_PERMISSIONS_REQUEST_INTERNET=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instrucciones();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);




        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.usuario) {
            AlertDialog.Builder alert= new AlertDialog.Builder(this);
            final EditText user=new EditText(this);
            user.setSingleLine();
            user.setPadding(50,0,50,0);
            alert.setTitle("NickName");
            alert.setMessage("Introducir NickName");
            alert.setView(user);
            alert.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    nickname=user.getText().toString();
                    if(nickname!=null){
                        connectWebSocket();
                    }
                }
            });
            alert.setNegativeButton("Cancelar",null);
            alert.create();
            alert.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void connectWebSocket() {

        URI uri;
        try {
            uri = new URI("ws://chat2-naborlj.c9users.io:8081");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        Map<String, String> headers = new HashMap<>();

        mWebSocketClient = new WebSocketClient(uri, new Draft_17(), headers, 0) {

            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("{\"id\":\"" + nickname+ "\"}");
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = (TextView)findViewById(R.id.textmensaje);
                        String nick;
                        String msg;
                        String dest;
                        Boolean prv;
                       try {
                            clienteRX = new JSONObject(message);
                            nick= clienteRX.getString("id");
                            msg = clienteRX.getString("mensaje");
                            dest= clienteRX.getString("destino");
                            prv= clienteRX.getBoolean("Privado");
                           if(prv.equals(Boolean.TRUE)){
                               if(dest.equals(nickname)){
                                   textView.setText(textView.getText() + "\n" + nick+ "\n" + msg);
                               }
                           }else
                               textView.setText("Mensaje Privado");

                       }
                       catch(JSONException e){

                           textView.setText(textView.getText() + "\n" + message);
                       }
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };

        mWebSocketClient.connect();

    }
    public void sendMessage() {

        EditText msg = (EditText)findViewById(R.id.mensaje);
        EditText destin = (EditText)findViewById(R.id.destino);
        CheckBox box = (CheckBox)findViewById(R.id.priv);
        String d,m;
        Boolean bl;
        d = destin.getText().toString();
        m = msg.getText().toString();
        if(box.isChecked()) {
            bl = Boolean.TRUE ;
        }else{
            bl = Boolean.FALSE;
        }
         json = new JSONObject();
        try {
            json.put("id",nickname);
            json.put("mensaje",m);
            json.put("destino",d);
            json.put("Privado",bl);
            msg.setText("");
            destin.setText("");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        mWebSocketClient.send(json.toString());


    }
    public void instrucciones() {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setTitle("Conexión");
        build.setMessage(instruccioes);
        build.setPositiveButton("Aceptar", null);
        build.create();
        build.show();
    }
    public String instruccioes = "Para poder establecer la conexión es necesario introducir un Nick.\n" +
            "-Puedes introducir tu NickName en el menu lateral,en el icono del margen superior izquierdo.\n" +
            "- Una vez introducido se conectará automaticamente.";
}

