package com.eahm.drinkwaterapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.eahm.drinkwaterapp.Adapters.WaterAdapter;
import com.eahm.drinkwaterapp.BroadcastReceiver.AlarmReceiver;
import com.facebook.login.LoginManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PrevWaterActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener, ValueEventListener {

    //region VARIABLES
    boolean appConnected = false;

    CircleImageView imageViewProfile;
    TextView textViewWelcome;
    Button buttonLogOut;

    GraphView graphView;
    RecyclerView recyclerView;
    RelativeLayout layoutViews;
    ImageView imageViewStarting;
    TextView textViewStartingMessage;
    Button buttonAdjust;
    Button buttonIDrink;

    RelativeLayout layoutWaiting;
    ImageView imageViewWaitingResults;
    ProgressBar progressBar;
    TextView textViewResultMessage;
    Button buttonRetry;

    WaterAdapter adapterSwitch;
    ValueEventListener registryValueEventListener;
    DatabaseReference registryRef;

    View layout_setting_included;
    TextView textViewDayMax;
    TextView textViewReminder;

    EditText editTextDayMax;
    Spinner spinnerDayMax;

    //endregion VARIABLES

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseDatabase.getInstance().getReference(".info/connected").addValueEventListener(this);

        setContentView(R.layout.activity_water_registry);

        //region FIND VIEWS
        imageViewProfile = findViewById(R.id.imageViewProfile);
        textViewWelcome = findViewById(R.id.textViewWelcome);
        buttonLogOut = findViewById(R.id.buttonLogOut);

        graphView = findViewById(R.id.graph);

        recyclerView = findViewById(R.id.recycler);
        layoutViews = findViewById(R.id.layoutViews);
        imageViewStarting = findViewById(R.id.imageViewStarting);
        textViewStartingMessage = findViewById(R.id.textViewStartingMessage);

        buttonAdjust = findViewById(R.id.buttonAdjust);
        buttonIDrink = findViewById(R.id.buttonIDrink);

        layoutWaiting = findViewById(R.id.layoutWaiting);
        imageViewWaitingResults = findViewById(R.id.imageViewWaitingResults);
        progressBar = findViewById(R.id.progressBar);
        textViewResultMessage = findViewById(R.id.textViewResultMessage);
        buttonRetry = findViewById(R.id.buttonRetry);

        layout_setting_included = findViewById(R.id.layout_setting_included);
        textViewDayMax = findViewById(R.id.textViewDayMax);
        textViewReminder = findViewById(R.id.textViewReminder);
        editTextDayMax = findViewById(R.id.editTextDayMax);
        spinnerDayMax = findViewById(R.id.spinnerDayMax);

        AdView adView = findViewById(R.id.adView);

        //endregion FIND VIEWS

        //region ADMOB
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }
        });
        //endregion ADMOB

        //region LISTENERS
        buttonLogOut.setOnClickListener(v->{
            buttonIDrink.setEnabled(false);
            goOut();
        });

        buttonRetry.setOnClickListener(v->{
            buttonRetry.setEnabled(false);
            getWaterRegistry();
        });

        buttonAdjust.setOnClickListener(v -> {
            setView(5);
        });

        buttonIDrink.setOnClickListener(v -> {
            DrinkingWater();
        });

        layout_setting_included.setOnClickListener(v -> {
            setView(6);
        });

        //endregion LISTENERS

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            Glide.with(this)
                    .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                    .into(imageViewProfile);
        }

        alarmIntent = new Intent(PrevWaterActivity.this, AlarmReceiver.class);

        if(existAlarm()){
            Toast.makeText(this, "EXISTE", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "NO ESTA ACTIVADA", Toast.LENGTH_SHORT).show();
        }

        //region INITIALIZATIONS
        DataPoint[] data = new DataPoint[]{
                new DataPoint(0,1),
                new DataPoint(1,5),
                new DataPoint(2,3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        };


        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(data);
        graphView.addSeries(series);

        series.setTitle("Progreso al dia");
        series.setColor(Color.GREEN);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(7);
        series.setThickness(1);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setPathEffect(new DashPathEffect(new float[]{8, 5}, 0));
        series.setCustomPaint(paint);

        // set date label formatter
        graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(PrevWaterActivity.this));
        graphView.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
        // set manual x bounds to have nice steps
       // graphView.getViewport().setMinX(d1.getTime());
        //graphView.getViewport().setMaxX(d3.getTime());
        //graphView.getViewport().setXAxisBoundsManual(true);

        // as we use dates as labels, the human rounding to nice readable numbers
        // is not necessary
        graphView.getGridLabelRenderer().setHumanRounding(false);


        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayourManager = new LinearLayoutManager(PrevWaterActivity.this);
        ((LinearLayoutManager) mLayourManager).setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mLayourManager );
        adapterSwitch = new WaterAdapter(PrevWaterActivity.this);
        recyclerView.setAdapter(adapterSwitch);

        String userName = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "";
        String welcome = "Bienvenido " + userName;
        textViewWelcome.setText(welcome);
        //endregion INITIALIZATIONS

        //restore Shared Preferences
        restorePrefs();
        setView(1);
    }

    SharedPreferences sharedPref;

    final String goal_amount = "goal_amount";
    final String goal_measure = "goal_measure";
    final String time_amount = "time_amount";
    final String time_unit = "time_unit";

    private void restorePrefs() {

        /*sharedPref = getApplicationContext().getSharedPreferences("App_Settings", Context.MODE_PRIVATE);
        //sharedPref.edit().clear().apply();

        if(sharedPref.contains(goal_amount) && sharedPref.contains(goal_measure)){
            //read
            float amount = sharedPref.getFloat(goal_amount, 1);
            editTextDayMax.setText(String.valueOf(amount));

            int position = sharedPref.getInt(goal_measure, 0);
            spinnerDayMax.setSelection(position);

            Log.i("VALORES", "GOAL: " + amount + " " + position);
        }
        else {
            editTextDayMax.setText("1");
            spinnerDayMax.setSelection(0);
        }

        if(sharedPref.contains(time_amount) && sharedPref.contains(time_unit)){
            //read
            alarmTime = sharedPref.getInt(time_amount, 40);
            editTextReminderTime.setText(String.valueOf(alarmTime));

            timePos = sharedPref.getInt(time_unit, 0);
            spinnerReminderTime.setSelection(timePos);

            Log.i("VALORES", "TIME: " + alarmTime + " " + timePos);
        }
        else {
            alarmTime = 45;
            timePos = 0;

            editTextReminderTime.setText(alarmTime);
            spinnerReminderTime.setSelection(timePos);

        }*/

    }

    Handler setApp = null;
    Runnable run = this::getWaterRegistry;
    @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        // La primera vez devuelve FALSE, si esta conectado a internet primero regresa FALSE Y despues TRUE (conectado)
        try{
            boolean connected = dataSnapshot.getValue(Boolean.class);
            if(connected) {
                Log.i("APLICATION_IS_HERE", "ESTA CONECTADO A INTERNET");
                appConnected = true;
            }
            else {
                //OFFLINE
                Log.i("APLICATION_IS_HERE", "SE DESCONECTO EL INTERNET");
                appConnected = false;
            }
        }catch (NullPointerException ignore){
            //OFFLINE
            Log.i("APLICATION_IS_HERE", "SE DESCONECTO EL INTERNET por exception");
            appConnected = false;
        }

        if(setApp != null) setApp.removeCallbacks(run);
        setApp = new Handler();
        setApp.postDelayed(run,1000);
    }

    AlarmManager alarmManager;
    Intent alarmIntent;
    PendingIntent alarmPendingIntent;
    static int ALARM_REQUEST_CODE = 777;

    private int alarmTime = 0;
    private int timePos = 0;

    public void alarmReceived(){
        Toast.makeText(this, "Recibida en water", Toast.LENGTH_SHORT).show();
    }

    private void updateAlarm() {
        //MANIFEST: <receiver android:name=".AndroidActionsReceiver" />

        if(!existAlarm()) {
            long time = SystemClock.elapsedRealtime() + (2 * 60 * 1000); //2 * 60 * 1000 ====> 2 minutos

            String timemeasure = null;

            switch (timePos) {
                case 0: //minutos
                    time = SystemClock.elapsedRealtime() + (alarmTime * 60 * 1000);
                    timemeasure = "minutos";
                    break;
                case 1: //hora
                    time = SystemClock.elapsedRealtime() + ((alarmTime * 60) * 60 * 1000);
                    timemeasure = "horas";
                    break;
            }

            time = SystemClock.elapsedRealtime() + (1 * 1000);

            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmPendingIntent = PendingIntent.getBroadcast(PrevWaterActivity.this, ALARM_REQUEST_CODE, alarmIntent, 0);

            //Wake up the device to fire the alarm in 30 minutes, and every 30 minutes after that:
            //alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, time, AlarmManager.INTERVAL_HALF_HOUR, alarmPendingIntent);

            //Wake up the device to fire a one-time (non-repeating) alarm in one minute:
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, time, alarmPendingIntent);

            Toast.makeText(this, "Te recordaremos en " + alarmTime + " " + (timemeasure != null ? timemeasure : ""), Toast.LENGTH_SHORT).show();
        }
        else {

            //Cancel
            alarmManager.cancel(alarmPendingIntent);
            alarmPendingIntent.cancel();

        }



    }


    boolean existAlarm(){
        return (PendingIntent.getBroadcast(PrevWaterActivity.this, ALARM_REQUEST_CODE, alarmIntent, PendingIntent.FLAG_NO_CREATE) != null);
    }

    //region DRINK_REGISTRY

    private void DrinkingWater() {
        if(FirebaseAuth.getInstance().getCurrentUser() == null) goOut();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("history").child("customer").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        Map<Object, Object> value = new HashMap<>();
        value.put("timestamp", ServerValue.TIMESTAMP);

        buttonIDrink.setEnabled(false);
        String userName = "Guardando! espera un momento... ";
        textViewWelcome.setText(userName);

        ref.push().setValue(value).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Done!
                buttonIDrink.setEnabled(true);

                String userName = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "";
                String welcome = "Bienvenido " + userName;
                textViewWelcome.setText(welcome);

                updateAlarm();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PrevWaterActivity.this, "No se pudo guardar, intenta otra vez.", Toast.LENGTH_SHORT).show();
                buttonIDrink.setEnabled(true);
            }
        });
    }



    private void setView(int view) {
        if(view == 1){
            // cargando...
            layoutWaiting.setVisibility(View.VISIBLE);
            imageViewWaitingResults.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            buttonRetry.setVisibility(View.GONE);
            layoutViews.setVisibility(View.GONE);
            layout_setting_included.setVisibility(View.GONE);
        }
        else if(view == 2){
            // error o mensaje como resultado
            layoutWaiting.setVisibility(View.VISIBLE);
            imageViewWaitingResults.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            buttonRetry.setVisibility(View.VISIBLE);
            layoutViews.setVisibility(View.GONE);
            layout_setting_included.setVisibility(View.GONE);
        }
        else if(view == 3){
            // tenemos el registro vacio
            graphView.setVisibility(View.GONE);
            layoutViews.setVisibility(View.VISIBLE);
            layoutWaiting.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            layout_setting_included.setVisibility(View.GONE);
        }
        else if(view == 4){
            //mostrar datos en el registro
            graphView.setVisibility(View.VISIBLE);
            layoutWaiting.setVisibility(View.GONE);
            layoutViews.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            layout_setting_included.setVisibility(View.GONE);
        }
        else if(view == 5){
            //mostrar ajustes de toma de agua
            layout_setting_included.setVisibility(View.VISIBLE);
        }
        else if(view == 6){
            //ocultar ajustes de toma de agua
            layout_setting_included.setVisibility(View.GONE);
        }

    }

    private void getWaterRegistry() {
        if(!appConnected){
            buttonRetry.setEnabled(true);
            setView(2);
            textViewResultMessage.setText("No hay conexion a internet!");
            return;
        }

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            goOut();
            return;
        }

        registryRef = FirebaseDatabase.getInstance().getReference().child("history").child("customer").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Query query = registryRef.limitToLast(20);

        //registryRef.removeEventListener(registryValueEventListener);

        registryValueEventListener = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0){

                    List<String> receivedData = new ArrayList<>();
                    for (DataSnapshot data: dataSnapshot.getChildren()) {
                        Object value = data.child("timestamp").getValue();

                        if(value != null){
                            Log.i("DATASNAPSHOT_RECEIVED", "Añadido: " + value);
                            receivedData.add(value.toString());
                        }
                    }

                    if(!receivedData.isEmpty()){
                        Collections.reverse(receivedData);
                        //adapterSwitch.overrideList(receivedData);
                        adapterSwitch.notifyDataSetChanged();
                    }
                }
                else if(!dataSnapshot.exists()){
                    adapterSwitch.removeAll();
                    adapterSwitch.notifyDataSetChanged();
                }

                if(adapterSwitch.getItemCount() > 0){
                    setView(4);
                }
                else setView(3);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                setView(2);
                textViewResultMessage.setText("Ups! Se cancelo la llamada de tu registro");
            }
        });

    }

    private void ListenNewValues(Query query){
        //registryRef.removeEventListener(registryChildEventListener);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i("DATASNAPSHOT_RECEIVED", "onChildAdded " + dataSnapshot.getKey() + " - " );
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i("DATASNAPSHOT_RECEIVED", "onChildChanged " + dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.i("DATASNAPSHOT_RECEIVED", "onChildRemoved");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i("DATASNAPSHOT_RECEIVED", "onChildMoved");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("DATASNAPSHOT_RECEIVED", "onCancelled");
            }
        });
    }
    //endregion DRINK REGISTRY


    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if(firebaseAuth.getCurrentUser() == null){
            goOut();
        }
    }

    private void goOut(){
        if(FirebaseAuth.getInstance() != null) FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        Intent intent = new Intent(PrevWaterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        ThisApplication.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ThisApplication.onPause();
    }
}
