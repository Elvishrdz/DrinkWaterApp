package com.eahm.drinkwaterapp;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.eahm.drinkwaterapp.Adapters.SelectGlassesAdapter;
import com.eahm.drinkwaterapp.Adapters.WaterAdapter;
import com.eahm.drinkwaterapp.BroadcastReceiver.AndroidActionsReceiver;
import com.eahm.drinkwaterapp.Class.AwesomeTextsClass;
import com.eahm.drinkwaterapp.Class.NotificationClass;
import com.eahm.drinkwaterapp.Class.SettingsClass;
import com.eahm.drinkwaterapp.DB.RecordsContract;
import com.eahm.drinkwaterapp.DB.RecordsDbHelper;
import com.eahm.drinkwaterapp.DB.RecordsContract.RecordsEntry;
import com.eahm.drinkwaterapp.Fragments.NewsFragment;
import com.eahm.drinkwaterapp.Interfaces.Callbacks;
import com.eahm.drinkwaterapp.Models.News;
import com.eahm.drinkwaterapp.Models.RecordModel;
import com.eahm.drinkwaterapp.Services.FloatWidgetService;
import com.eahm.drinkwaterapp.Singleton.AlarmApp;
import com.eahm.drinkwaterapp.Singleton.AlarmFirst;
import com.eahm.drinkwaterapp.Singleton.AlarmLast;
import com.eahm.drinkwaterapp.Utils.Constants;
import com.eahm.drinkwaterapp.Utils.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;

import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class WaterDrinkActivity extends AppCompatActivity implements Callbacks {

    //region CREDITS
    /*
    Credits:
    <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from <a href="https://www.flaticon.com/" 			    title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" 			    title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
    <div>Icons made by <a href="https://www.freepik.com/" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" 			    title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" 			    title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
    <div>Icons made by <a href="https://www.flaticon.com/authors/vectors-market" title="Vectors Market">Vectors Market</a> from <a href="https://www.flaticon.com/" 			    title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" 			    title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
    <div>Icons made by <a href="https://www.flaticon.com/authors/popcorns-arts" title="Icon Pond">Icon Pond</a> from <a href="https://www.flaticon.com/" 			    title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" 			    title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
    <div>Icons made by <a href="https://www.flaticon.com/authors/darius-dan" title="Darius Dan">Darius Dan</a> from <a href="https://www.flaticon.com/" 			    title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" 			    title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
    <div>Icons made by <a href="https://www.flaticon.com/authors/darius-dan" title="Darius Dan">Darius Dan</a> from <a href="https://www.flaticon.com/" 			    title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" 			    title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
    <div>Icons made by <a href="https://www.freepik.com/" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" 			    title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" 			    title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
    <div>Icons made by <a href="https://www.freepik.com/?__hstc=57440181.bdd6304c040eeae6a353f69e3b28a5ee.1557337823332.1557553785535.1558075767886.6&__hssc=57440181.7.1558075767886&__hsfp=3487993566" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" 			    title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" 			    title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
    <div>Icons made by <a href="https://www.freepik.com/" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" 			    title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" 			    title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>

//trophys in this order!
01      <div>Icons made by <a href="https://www.flaticon.com/authors/iconixar" title="iconixar">iconixar</a> from <a href="https://www.flaticon.com/" 			    title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" 			    title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
02      <div>Icons made by <a href="https://www.freepik.com/?__hstc=57440181.bdd6304c040eeae6a353f69e3b28a5ee.1557337823332.1558769490382.1558778521392.10&__hssc=57440181.8.1558778521392&__hsfp=20414623" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" 			    title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" 			    title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
03      <div>Icons made by <a href="https://www.flaticon.com/authors/roundicons" title="Roundicons">Roundicons</a> from <a href="https://www.flaticon.com/" 			    title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" 			    title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
04      <div>Icons made by <a href="https://www.freepik.com/?__hstc=57440181.bdd6304c040eeae6a353f69e3b28a5ee.1557337823332.1558769490382.1558778521392.10&__hssc=57440181.10.1558778521392&__hsfp=20414623" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" 			    title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" 			    title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
05      <div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from <a href="https://www.flaticon.com/" 			    title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" 			    title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
06      <div>Icons made by <a href="https://www.freepik.com/?__hstc=57440181.bdd6304c040eeae6a353f69e3b28a5ee.1557337823332.1558769490382.1558778521392.10&__hssc=57440181.12.1558778521392&__hsfp=20414623" title="Freepik">Freepik</a> from <a href="https://www.flaticon.com/" 			    title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" 			    title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
     */
    //endregion CREDITS

    //region VARIABLES
    private final static String LOG_WATER_DRINK_ACT = "LOG_WATER_DRINK_ACT";
    private final static String LOG_BUTTON_DRINK = "LOG_BUTTON_DRINK";
    private final static String LOG_SETTING_HOURS = "LOG_SETTING_HOURS";
    private final static String LOG_SETTING_GOAL = "LOG_SETTING_GOAL";
    private final static String LOG_RESTORE_CHRONO = "LOG_RESTORE_CHRONO";
    private static final String LOG_RECORD = "LOG_RECORD";
    private static final String TAG_NEWS = "TAG_NEWS";
    private static final String CODE_SET_ALARM = "SET_ALARM";

    RelativeLayout root;
    TextView textViewProgress, textViewStats, textViewNextDrink, textViewTips;
    CircleImageView circleImageViewProfilePic;
    PulsatorLayout pulsatorLayout;
    Button buttonDrink;
    Button buttonAlarmStatus;
    AdView adView;

    ImageButton imageButtonRecords;
    ImageButton imageButtonSetNotification;
    ImageButton imageButtonSetDrinks;
    ImageButton imageButtonSetClocks;

    RelativeLayout layoutFragmentNews;

    //region RECORDS
    CardView includeLayoutRecords;
    ImageButton buttonRecordClose;
    TextView textViewRecordTitle;
    RecyclerView recyclerRecords;
    ProgressBar progressBarRecord;
    TextView textViewRecordMessage;

    RecyclerView.LayoutManager mLayourManager;
    WaterAdapter adapterSwitch;
    //endregion RECORDS

    //region SELECT GLASS
    LinearLayout includeLayoutSelectGlasses;
    GridView gridViewSG;
    Button buttonSGCancel;

    SelectGlassesAdapter selectGlassesAdapter;
    //endregion SELECT GLASS

    //region NOTIFICATION SETTINGS
    CardView includeLayoutNotification;
    Spinner spinnerNotiSkipTimes;
    Spinner spinnerNotiDrinkAmount;
    //endregion NOTIFICATION SETTINGS

    //region SET GOAL
    CardView includeLayoutSetGoal;
    CardView includeLayoutSetReminder;

    TextView textViewDayMax;
    EditText editTextDayMax;
    Spinner spinnerDayMax;

    TextView textViewReminder;
    TimePicker timePickerInterval;
    Button buttonTimePickerInterval;

    boolean convertNotSave = false;

    //endregion SET GOAL

    //region LAYOUT SELECT TIME
    CardView includeCardViewSetTime;

    TextView textViewMyStart;
    ImageButton buttonSetStart;

    TextView textViewMyEnd;
    ImageButton buttonSetEnd;

    //endregion LAYOUT SELECT TIME

    Utils utils = new Utils();
    NotificationClass app = new NotificationClass();
    SettingsClass settings = new SettingsClass();
    AwesomeTextsClass awesomeTexts = new AwesomeTextsClass();

    CountDownTimerDrink countDownTimer;

    RecordsDbHelper dbHelper;
    SQLiteDatabase dbWrite;
    SQLiteDatabase dbRead;

    boolean startedFromNotification = false;
    boolean closeApp = false;
    boolean isDrinkTimeFromNotification = false;

    //endregion VARIABLES

    private static final int APP_PERMISSION_REQUEST = 102;

    @Override
    public void onBackPressed() {

        if (layoutFragmentNews.getVisibility() == View.VISIBLE     ) {
            //Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_NEWS);
            //fragment instanceof NewsFragment
            layoutFragmentNews.setVisibility(View.GONE);
            super.onBackPressed();
        }
        else if(includeLayoutRecords.getVisibility() == View.VISIBLE){
            includeLayoutRecords.setVisibility(View.GONE);
        }
        else if(includeLayoutSelectGlasses.getVisibility() == View.VISIBLE){
            includeLayoutSelectGlasses.setVisibility(View.GONE);
        }
        else if(includeLayoutNotification.getVisibility() == View.VISIBLE){
            includeLayoutNotification.setVisibility(View.GONE);
        }
        else if(includeLayoutSetGoal.getVisibility() == View.VISIBLE){
            includeLayoutSetGoal.setVisibility(View.GONE);
            includeLayoutSetReminder.setVisibility(View.GONE);
        }
        else if(includeCardViewSetTime.getVisibility() == View.VISIBLE){
            includeCardViewSetTime.setVisibility(View.GONE);
        }
        else if(!settings.isAlarmWorking()){
            utils.displayAlertDialogYesNo(this, this, CODE_SET_ALARM,
                    "Ya pasamos tu hora de finalizacion\n¿Deseas seguir recibiendo alarmas?",
                    "No, salir", "Si, extender tiempo", true);
        }
        else if(!closeApp && settings.isAlarmWorking()){
            closeApp = true;
            Toast.makeText(this, "Presiona otra vez para salir", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(()->{
                closeApp = false;
            }, 2500);
        }
        else{

            if(countDownTimer == null && settings.isAlarmWorking()){
                // Si es hora de tomar agua (alarma nula) y el usuario abandona la app, vamos a poner una alarma en 5min,
                // cuando se active la alarma (si aun esta la app en horas activas) entonces lanzamos notificacion.
                if(!isDrinkTimeFromNotification){
                    Log.i(LOG_RECORD, "Debes tomar agua pero salista intencionalmente aun asi... registrar 6");
                    recordDrink(0, 6);
                }
            }

            super.onBackPressed();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
       String receivedFrom = "";

        if(intent != null && intent.getExtras() != null){
            receivedFrom = intent.getExtras().getString("FROM", "");
        }

        switch (receivedFrom){
            case "CLOSED":
                Log.i(LOG_WATER_DRINK_ACT, "onNewIntent: CLOSED recibido");
                break;
            case "SKIP":
                Log.i(LOG_WATER_DRINK_ACT, "onNewIntent: SKIP recibido");
                break;
            case "DRINK":
                Log.i(LOG_WATER_DRINK_ACT, "onNewIntent: DRINK recibido");
                break;
            case "MORNING":
                Log.i(LOG_WATER_DRINK_ACT, "onNewIntent: MORNING recibido");
                break;
            case "LAST":
                Log.i(LOG_WATER_DRINK_ACT, "onNewIntent: LAST recibido");
                isDrinkTimeFromNotification = false;
                //Ajustar app a modo fin de dia!
                setAppStyle();
                if(countDownTimer != null) countDownTimer.onFinish();
                String text = "Periodo de bebida terminado!\n" + textViewNextDrink.getText();
                textViewNextDrink.setText(text);
                break;
            case Intent.ACTION_SCREEN_OFF:
                Log.i(LOG_WATER_DRINK_ACT, "Pantalla apagada");

                //inicializar en onCreate si deseas usar este listener
                /* INITIALIZE RECEIVER
                IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
                filter.addAction(Intent.ACTION_SCREEN_OFF);
                BroadcastReceiver mReceiver = new AndroidActionsReceiver();
                registerReceiver(mReceiver, filter);
                */
                break;
        }

        Log.i(LOG_WATER_DRINK_ACT, "onNewIntent Invocado! " + receivedFrom);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_drink);
        logFabricUser();
        //Prefs.clear();
        //settings.deleteTimeData();

        AlarmApp.getInstance().initialize(getBaseContext());

        if(getIntent() != null && getIntent().getExtras() != null){
            startedFromNotification = getIntent().getExtras().getBoolean("IS_FROM_NOTIFICATION");
            if(startedFromNotification){
                isDrinkTimeFromNotification = true;
            }
        }

        //region FIND VIEWS
        root = findViewById(R.id.root);
        textViewProgress = findViewById(R.id.textViewProgress);
        textViewStats = findViewById(R.id.textViewStats);
        textViewNextDrink = findViewById(R.id.textViewNextDrink);
        textViewTips = findViewById(R.id.textViewTips);
        circleImageViewProfilePic = findViewById(R.id.circleImageViewProfilePic);
        pulsatorLayout = findViewById(R.id.pulsatorLayout);
        buttonDrink = findViewById(R.id.buttonDrink);
        buttonAlarmStatus = findViewById(R.id.buttonAlarmStatus);
        imageButtonRecords = findViewById(R.id.imageButtonRecords);
        imageButtonSetNotification = findViewById(R.id.imageButtonSetNotification);
        imageButtonSetDrinks = findViewById(R.id.imageButtonSetDrinks);
        imageButtonSetClocks = findViewById(R.id.imageButtonSetClocks);

        layoutFragmentNews = findViewById(R.id.layoutFragmentNews);

        //region RECORDS
        includeLayoutRecords = findViewById(R.id.includeLayoutRecords);
        buttonRecordClose = findViewById(R.id.buttonRecordClose);
        textViewRecordTitle = findViewById(R.id.textViewRecordTitle);
        recyclerRecords = findViewById(R.id.recyclerRecords);
        progressBarRecord = findViewById(R.id.progressBarRecord);
        textViewRecordMessage = findViewById(R.id.textViewRecordMessage);
        //endregion RECORDS

        //region SELECT GLASS
        includeLayoutSelectGlasses = findViewById(R.id.includeLayoutSelectGlasses);
        gridViewSG = findViewById(R.id.gridViewSG);
        buttonSGCancel = findViewById(R.id.buttonSGCancel);

        //endregion SELECT GLASS

        //region NOTIFICATION SETTINGS
        includeLayoutNotification = findViewById(R.id.includeLayoutNotification);
        spinnerNotiSkipTimes = findViewById(R.id.spinnerNotiSkipTimes);
        spinnerNotiDrinkAmount = findViewById(R.id.spinnerNotiDrinkAmount);

        //endregion NOTIFICATION SETTINGS

        //region SET GOAL
        includeLayoutSetGoal = findViewById(R.id.includeLayoutSetGoal);
        includeLayoutSetReminder = findViewById(R.id.includeLayoutSetReminder);

        textViewDayMax = findViewById(R.id.textViewDayMax);
        textViewReminder = findViewById(R.id.textViewReminder);
        editTextDayMax = findViewById(R.id.editTextDayMax);
        spinnerDayMax = findViewById(R.id.spinnerDayMax);

        textViewReminder = findViewById(R.id.textViewReminder);
        timePickerInterval = findViewById(R.id.timePickerInterval);
        buttonTimePickerInterval = findViewById(R.id.buttonTimePickerInterval);

        //endregion SET GOAL

        //region LAYOUT SELECT TIME
        includeCardViewSetTime = findViewById(R.id.includeSetTime);

        textViewMyStart = findViewById(R.id.textViewMyStart);
        buttonSetStart = findViewById(R.id.buttonSetStart);
        textViewMyEnd = findViewById(R.id.textViewMyEnd);
        buttonSetEnd = findViewById(R.id.buttonSetEnd);

        //endregion LAYOUT SELECT TIME

        adView = findViewById(R.id.adView);

        //endregion FIND VIEWS

        //region ADMOB
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        adView.loadAd(adRequest);
        ViewCompat.setElevation(adView, Utils.convertDpToPx(7, this));

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

        //region BUTTONS
        imageButtonRecords.setOnClickListener(v -> {
            //Toast.makeText(this, "Preparar reporte eh!", Toast.LENGTH_SHORT).show();
            if(includeLayoutRecords.getVisibility() == View.VISIBLE){
                includeLayoutRecords.setVisibility(View.GONE);
            }
            else{
                includeLayoutRecords.setVisibility(View.VISIBLE);
                ViewCompat.setElevation(includeLayoutRecords, Utils.convertDpToPx(7, WaterDrinkActivity.this));
                initializeReport();
            }
            //Crashlytics.log("Mostrando reporte!");
            //Crashlytics.setString("FABRIC_REPORT", "mostrado");

            //throw new RuntimeException("This is a crash, hi FABRIC!");

        });

        imageButtonSetNotification.setOnClickListener(v -> {
            if(includeLayoutNotification.getVisibility() == View.VISIBLE){
                includeLayoutNotification.setVisibility(View.GONE);
            }
            else{
                includeLayoutNotification.setVisibility(View.VISIBLE);
            }
        });

        imageButtonSetDrinks.setOnClickListener(v -> {
            if(includeLayoutSetGoal.getVisibility() == View.VISIBLE){
                includeLayoutSetGoal.setVisibility(View.GONE);
                includeLayoutSetReminder.setVisibility(View.GONE);
            }
            else{
                includeLayoutSetGoal.setVisibility(View.VISIBLE);
                includeLayoutSetReminder.setVisibility(View.VISIBLE);
            }
        });

        imageButtonSetClocks.setOnClickListener(v -> {
            if(includeCardViewSetTime.getVisibility() == View.VISIBLE){
                includeCardViewSetTime.setVisibility(View.GONE);
            }
            else{
                Log.i(LOG_SETTING_HOURS, settings.getStartHour() + ":" + settings.getStartMin());
                Log.i(LOG_SETTING_HOURS, settings.getEndHour() + ":" + settings.getEndMin());

                setSettingsValues(settings.getStartHour(), settings.getStartMin(), true);
                setSettingsValues(settings.getEndHour(), settings.getEndMin(), false);

                includeCardViewSetTime.setVisibility(View.VISIBLE);
            }
        });

        //endregion BUTTONS

        //region RECORDS
        buttonRecordClose.setOnClickListener(v -> {
            includeLayoutRecords.setVisibility(View.GONE);
        });
        //endregion RECORDS

        //region NOTIFICATION SETTINGS
        spinnerNotiSkipTimes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int skipSeconds = 0;
                switch (position){
                    case 0: //5min * 60
                        skipSeconds = 300;
                        break;
                    case 1: //10
                        skipSeconds = 600;
                        break;
                    case 2: //15
                        skipSeconds = 900;
                        break;
                    case 3: //20
                        skipSeconds = 1200;
                        break;
                }
                if(skipSeconds == 0) return;
                settings.setNotiSkipTime(skipSeconds);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerNotiDrinkAmount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                float drinkAmount = 0;
                switch (position){
                    case 0: //10ml
                        drinkAmount = 10;
                        break;
                    case 1: //50
                        drinkAmount = 50;
                        break;
                    case 2: //100ml
                        drinkAmount = 100;
                        break;
                    case 3: //150
                        drinkAmount = 150;
                        break;
                }
                if(drinkAmount == 0) return;
                settings.setNotiDrinkAmount(drinkAmount);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //endregion NOTIFICATION SETTINGS

        //region SET GOAL
        editTextDayMax.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //Log.i(LOG_SETTING_GOAL, "before text changed " + s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Log.i(LOG_SETTING_GOAL, "on text changed " + s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Log.i(LOG_SETTING_GOAL, "after text changed " + s.toString());
                String newAmount = s.toString();

                if(!convertNotSave && !newAmount.isEmpty() && !newAmount.equalsIgnoreCase("0.0")){

                    String goal = "";

                    switch (spinnerDayMax.getSelectedItemPosition()){
                        case 0: // mililiters
                            goal = "mililitros";
                            break;
                        case 1: // liters 1-1000
                            goal = "litros";
                            float convertion = Float.parseFloat(newAmount) * 1000;
                            newAmount = String.valueOf(convertion); // Convert to mililiters
                            break;
                    }
                    Log.i(LOG_SETTING_GOAL, "Saving goal: " + s.toString());
                    settings.setGoalValue(Float.valueOf(newAmount));

                    if(Float.parseFloat(s.toString()) == 1) goal = goal.substring(0, goal.length()-1);
                    goal = "Tu meta de hoy es de " + s.toString() + " " + goal + " de agua";
                    textViewStats.setText(goal);
                    updateProgress();
                    setAppStyle();
                }
                else if(convertNotSave) convertNotSave = false;

                if(s.toString().isEmpty() || s.toString().equalsIgnoreCase("0")){
                    switch (spinnerDayMax.getSelectedItemPosition()){
                        case 0: // mililiters
                            editTextDayMax.setText("1000");
                            break;
                        case 1: // liters 1-1000
                            editTextDayMax.setText("1");
                            break;
                    }

                }

            }
        });

        spinnerDayMax.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(LOG_SETTING_GOAL, "on item selected " + position);

                settings.setGoalMetricVolume(position);
                setGoalMetricVolumeConverted(position);
                updateProgress();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i(LOG_SETTING_GOAL, "on nothing selected");
            }
        });

        timePickerInterval.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                Log.i(LOG_SETTING_GOAL, "Del selector obtuvimos: " + hourOfDay + ":" + minute);

                String textReminder = "Recordar cada:\n" + utils.getTimeString(hourOfDay, minute, 0);

                textViewReminder.setText(textReminder);

                int newTime = utils.getSeconds(hourOfDay, minute);
                Log.i(LOG_SETTING_GOAL, "Interval value SAVED: " + newTime);
                settings.setIntervalValue(newTime);

            }
        });

        buttonTimePickerInterval.setOnClickListener(v -> {

            int allSeconds = settings.getIntervalValue();
            String result = utils.convertSecondTo_HHmmss_String(allSeconds);

            String[] parts = result.split(":");
            int hours = Integer.parseInt( parts[0] );
            int minutes = Integer.parseInt( parts[1] );
            int secs = Integer.parseInt( parts[2] );


            TimePickerDialog.OnTimeSetListener timeSet = (view, hourOfDay, minute) -> {
                Log.i(LOG_SETTING_GOAL, "Del selector obtuvimos: " + hourOfDay + ":" + minute);

                String textReminder = "Recordar cada:\n" + utils.getTimeString(hourOfDay, minute, 0);

                textViewReminder.setText(textReminder);

                int newTime = utils.getSeconds(hourOfDay, minute);
                Log.i(LOG_SETTING_GOAL, "Interval value SAVED: " + newTime);
                settings.setIntervalValue(newTime);
            };

            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    AlertDialog.THEME_HOLO_LIGHT, timeSet, hours, minutes, true);

            // Set the newly created TextView as a custom tile of DatePickerDialog
            timePickerDialog.setCustomTitle(utils.createCustomTitle(this,
                    "Cada cuanto deseas recibir notificaciones?",
                    getResources().getColor(R.color.com_facebook_blue),
                    getResources().getColor(R.color.white),
                    20));

            timePickerDialog.show();
        });

        //endregion SET GOAL

        //region LAYOUT SELECT TIME
        buttonSetStart.setOnClickListener(v -> {
            getHourTime(settings.getStartHour(), settings.getStartMin(),true);
        });

        buttonSetEnd.setOnClickListener(v -> {
            getHourTime(settings.getEndHour(),settings.getEndMin(),false);
        });

        //endregion LAYOUT SELECT TIME

        //region SELECT GLASS
        buttonSGCancel.setOnClickListener(v -> {
            buttonDrink.callOnClick();
        });
        //endregion SELECT GLASS

        buttonDrink.setOnClickListener(v -> {
            //Toast.makeText(this, "Bien Hecho! te aviso para tomar agua mas tarde... ", Toast.LENGTH_LONG).show();
            if(includeLayoutSelectGlasses.getVisibility() == View.VISIBLE){
                includeLayoutSelectGlasses.setVisibility(View.GONE);
            }
            else{
                includeLayoutSelectGlasses.setVisibility(View.VISIBLE);
                ViewCompat.setElevation(includeLayoutSelectGlasses, Utils.convertDpToPx(7, this));
            }
        });

        buttonAlarmStatus.setOnClickListener(v -> {
            /*boolean alarmStatus = !settings.isAlarmWorking();
            settings.setAlarmWorking(alarmStatus);
            if(alarmStatus){
                buttonAlarmStatus.setTextColor(getResources().getColor(R.color.red_indio));
                buttonAlarmStatus.setText("Desactivar Alarma");
            }
            else {
                buttonAlarmStatus.setTextColor(getResources().getColor(R.color.green));
                buttonAlarmStatus.setText("Activar Alarma");
            }*/
        });

        //endregion LISTENERS

        //region RESTORE ALARMS
        Log.i(Constants.LOG_MORNING_ALARM, "Administrar la alarma mañanera...");
        AlarmFirst.getInstance().adminFirstAlarm(getBaseContext());

        Log.i(Constants.LOG_LAST_ALARM, "Administrar ultima alarma del dia...");
        AlarmLast.getInstance().adminLastAlarm(getBaseContext());

        activateAlarmOnRestartPhone(true);
        //endregion RESTORE ALARMS

        //region RESTORE CHRONO

        String timestamp = settings.getDrinkNextTime();

        if(timestamp.isEmpty()){
            Log.i(LOG_RESTORE_CHRONO, "No hay tiempo siguiente guardado.");

            if(settings.isAlarmWorking()){
                textViewNextDrink.setText("Empecemos...\n\nToma un vaso de agua ahora, presiona el boton y luego selecciona la cantidad que ingeriste. \nDespues continua con tus actividades que nosotros te avisaremos cuando debas beber otravez");
            }
            else {
                textViewNextDrink.setText("La alarma esta desactivada...\n\nToma un vaso de agua ahora, presiona el boton y luego selecciona la cantidad que ingeriste. \nSi aun deseas recibir alarmas puedes extender tu hora de finalizacion en los ajustes");
            }

            // LOG_BUTTON_DRINK
        }
        else if(timestamp.equalsIgnoreCase("OVER")){
            Log.i(LOG_RESTORE_CHRONO, "No hay tiempo siguiente guardado.");
            textViewNextDrink.setText("Horas de descanso\n\nSi deseas seguir recibiendo notificaciones puedes ampliar mas la hora de finalizacion en los ajustes");
            // LOG_BUTTON_DRINK

        }
        else {
            Log.i(LOG_RESTORE_CHRONO, "Tiempo siguiente guardado: " + timestamp);

            long timeInMillis = utils.getMillis(timestamp);
            Log.i(LOG_RESTORE_CHRONO, "Timestamp nextDrink en millis: " + timeInMillis);

            long currentTimestamp = System.currentTimeMillis()/1000;
            Log.i(LOG_RESTORE_CHRONO, "Timestamp actual en millis: " + timeInMillis);

            timeInMillis = Long.parseLong(timestamp);

            if(timeInMillis <= currentTimestamp){
                Log.i(LOG_RESTORE_CHRONO, "Beber agua, (ya se paso la hora definida)");
                textViewTips.setText(awesomeTexts.getCoutdownTextDone());
                setPulsatorLayout(true);
            }
            else {
                Log.i(LOG_RESTORE_CHRONO, "Siguiente bebida en: " + timestamp);

                // update the time in the chronometer hehe... by elvis :v
                int secondsRemaining = (int) (timeInMillis - currentTimestamp);
                Log.i(LOG_RESTORE_CHRONO, "Segundos restantes: " + secondsRemaining);

                timeInMillis = secondsRemaining * 1000; // convert in millis
                Log.i(LOG_RESTORE_CHRONO, "Restantes en millis: " + timeInMillis);

                if(countDownTimer != null){
                    long remainingTimeInMillis = countDownTimer.getTimeInMillis();
                    Log.i(LOG_RESTORE_CHRONO, "Obtener tiempo restante de la anterior alarma. Restante en millis: " + remainingTimeInMillis);

                    int remainingHours = utils.getHours(remainingTimeInMillis);
                    int remainingMinutes = utils.getMinutes(remainingTimeInMillis);
                    int remainingSeconds = utils.getSeconds(remainingTimeInMillis);

                    String stats;

                    if(remainingHours > 0){ // Mas de 60 Minutos
                        stats = "Te has adelantado por " + remainingHours + " hora " + remainingMinutes + " minutos " + remainingSeconds + " segundos a tu tiempo de agua";
                    }
                    else if(remainingMinutes > 0){ // Mas de 60 segundos
                        stats = "Te has adelantado por " + remainingMinutes + " minutos " + remainingSeconds + " segundos a tu tiempo de agua";
                    }
                    else stats = "Te has adelantado por " + remainingSeconds + " segundos a tu tiempo de agua";

                    textViewStats.setText(stats);
                    countDownTimer.cancel();
                }

                Log.i(LOG_RESTORE_CHRONO, "Cronometro con: " + timeInMillis + " millisegundos");
                countDownTimer = new CountDownTimerDrink(timeInMillis,1000);
                countDownTimer.start();
                setPulsatorLayout(false);
                buttonDrink.setText("Tomar agua ahora");

            }
        }
        //endregion RESTORE CHRONO

        //region INITIALIZE
        textViewTips.setText("");
        buttonAlarmStatus.setVisibility(View.GONE); // En progreso
        setAppStyle();

        //algunos telefonos muestran el selector muy pequeño... usar boton
        timePickerInterval.setVisibility(View.GONE);
        buttonTimePickerInterval.setVisibility(View.VISIBLE);

        dbHelper = new RecordsDbHelper(this);
        Log.i(LOG_RECORD, "base de datos inicializada");

        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            circleImageViewProfilePic.setVisibility(View.GONE);
            //imageButtonRecords.setVisibility(View.GONE);
        }

        restoreNotificationValues();
        restoreGoalValues();

        updateProgress();

        setAppStyle();

        //Cerrar la notificacion en caso de estar activa
        Log.i(LOG_WATER_DRINK_ACT, "onCreate: Cerrar notificacion en caso que exista");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(WaterDrinkActivity.this);
        notificationManager.cancel(Constants.NOTIFICATION_ID_APP);

        if(startedFromNotification){
            // Entramos a la app presionando la notificacion
            startedFromNotification = false;
            Log.i(LOG_WATER_DRINK_ACT, "Activity iniciada desde la notificacion");
            setPulsatorLayout(true);

            if(getIntent() != null && getIntent().getExtras() != null &&
                    getIntent().getExtras().getBoolean("SHOW_REPORT")){
                Log.i(LOG_WATER_DRINK_ACT, "Mostramos la notificacion final y entramos a ver el registro de hoy");
                imageButtonRecords.callOnClick();
                Toast.makeText(this, "Este es el registro de tú dia hasta ahora", Toast.LENGTH_LONG).show();
            }
            else if(getIntent() != null && getIntent().getExtras() != null &&
                    getIntent().getExtras().getBoolean("IS_LAST_NOTIFICATION")){
                isDrinkTimeFromNotification = false;
            }

        }
        else {
            //normal start
            Log.i(LOG_WATER_DRINK_ACT, "Activity onCreate() Entrada normal");
        }


        selectGlassesAdapter = new SelectGlassesAdapter(this);
        gridViewSG.setAdapter(selectGlassesAdapter);
        gridViewSG.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String text = parent.getItemAtPosition(position).toString();

                float value = Float.parseFloat(text);
                /*
                String[] parts = text.split("\\s+"); //Split by space " "
                float value = Float.parseFloat(parts[0]);
                String metricVolume = parts[1];

                switch (metricVolume){
                    case "mL":
                        // Nothing now
                        break;
                    case "L":
                        value = value * 1000;
                        break;
                }*/

                float goal = settings.getGoalValue();

                float currentProgress = settings.getTodayProgress();
                currentProgress = currentProgress + value;

                settings.setTodayProgress(currentProgress); // progress always in mililiters
                String currentProgressString = String.valueOf(currentProgress);

                if(currentProgress % 1 == 0){
                    long integer = (long) currentProgress;
                    currentProgressString = String.valueOf(integer);
                }

                if(currentProgress>= goal && !settings.getContratulationStatus()){
                    settings.setContratulationStatus(true);
                    textViewTips.setText("FELICIDADES!\nYa has alcanzado tu meta de hoy!");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(WaterDrinkActivity.this)) {
                        //No tenemos activado el permiso de mostrar VIEWS encima de otras apps
                    } else {
                        startService(new Intent(WaterDrinkActivity.this, FloatWidgetService.class));
                    }

                    Toast.makeText(WaterDrinkActivity.this, "Bien Hecho! has alcanzado tu meta de hoy!", Toast.LENGTH_LONG).show();

                }
                else Toast.makeText(WaterDrinkActivity.this, "Bravo! +" + currentProgressString, Toast.LENGTH_SHORT).show();


                DecimalFormat formatDecimal = new DecimalFormat("#.##");
                String progress = "Progreso:\nLlevas " + currentProgressString + " mililitros, tu meta es " + formatDecimal.format(goal) + " mililitros ";

                textViewStats.setText(progress);
                setAppStyle();
                updateProgress();
                recordDrink(value, 1);
                isDrinkTimeFromNotification = false;

                buttonDrink();
                buttonDrink.callOnClick();

                //Toast.makeText(WaterDrinkActivity.this, "Seleccionado: " + text, Toast.LENGTH_SHORT).show();
            }
        });

        //endregion INITIALIZE

        //region VALIDATE NOTIFICATION ON THIS PHONES
        if("huawei".equalsIgnoreCase(android.os.Build.MANUFACTURER) && settings.getProtectedStatus() != 2 ) {
            AlertDialog.Builder builder  = new AlertDialog.Builder(this);
            builder.setTitle("HUAWEI").setMessage("Se requiere añadir la app a la lista de apps protegidas para recibir notificaciones")
                    .setPositiveButton("Proceder", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
                            startActivity(intent);
                            settings.setProtectedStatus(1);
                        }
                    });

            if(settings.getProtectedStatus() == 1){
                builder.setNegativeButton("Ya esta hecho", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        settings.setProtectedStatus(2);
                    }
                });
            }

            builder.create().show();
        }
        //endregion VALIDATE NOTIFICATION ON THIS PHONES


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(WaterDrinkActivity.this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, APP_PERMISSION_REQUEST);
        }

        showNews();


    }

    final String FRAGMENT_TAG_NEWS = "NEWS_FRAGMENT";

    private void showNews(){

        if(((ThisApplication) getApplication()).getNewsList() != null &&
                ((ThisApplication) getApplication()).getNewsList().size() > 0){

            Log.i(TAG_NEWS, "Tenemos noticias... mostrar.");

            //Bundle bundle = new Bundle();
            //bundle.putString("key","value");

            NewsFragment newsFragment = new NewsFragment();
            //newsFragment.setArguments(bundle);

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.layoutFragmentNews, newsFragment,FRAGMENT_TAG_NEWS).addToBackStack("");
            fragmentTransaction.commit();

            RelativeLayout layoutFragmentNews = findViewById(R.id.layoutFragmentNews);
            layoutFragmentNews.setVisibility(View.VISIBLE);
            Log.i(TAG_NEWS, "MOSTRAR layout");


            /*
            try{
                FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
                fm.remove(getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_NEWS));
                fm.commit();
            }
            catch (Exception e){
                //ignore
            }

            */

            //Mostrar noticias

        }
    }

    private void initializeReport() {
        DateTime datetime = Utils.getDateTimeByTimestamp(utils.getCurrentTimestamp());

        String stringTodayDate = Utils.getDayName(datetime.getDayOfWeek(),"es") + " " +
                                 datetime.getDayOfMonth() + " de " +
                                 Utils.getMonthName(datetime.getMonthOfYear(), "es") + " " +
                                 datetime.getYear();

        textViewRecordTitle.setText("Registros de\n" + stringTodayDate);

        recyclerRecords.setVisibility(View.GONE);
        progressBarRecord.setVisibility(View.VISIBLE);
        textViewRecordMessage.setText("Cargando registros...");

        if(recyclerRecords == null || adapterSwitch == null){
            recyclerRecords.setHasFixedSize(true);
            mLayourManager = new LinearLayoutManager(this);
            ((LinearLayoutManager) mLayourManager).setOrientation(LinearLayoutManager.VERTICAL);
            recyclerRecords.setLayoutManager(mLayourManager);
            adapterSwitch = new WaterAdapter(this);
            recyclerRecords.setAdapter(adapterSwitch);
        }

        //read from db
        dbRead = dbHelper.getReadableDatabase();

        //region QUERY EXAMPLES
        // Define a projection that specifies which columns from the database you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                RecordsEntry.COLUMN_NAME_TIMESTAMP,
                RecordsEntry.COLUMN_NAME_DRINK_AMOUNT,
                RecordsEntry.COLUMN_NAME_STATUS_CODE
        };

        // Filter results WHERE "drinkAmount" = '100'
        String selection = RecordsEntry.COLUMN_NAME_DRINK_AMOUNT + " = ?";
        String[] selectionArgs = { "100" };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = RecordsEntry.COLUMN_NAME_TIMESTAMP + " DESC";

        Cursor cursor = dbRead.query(
                RecordsEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        List itemIds = new ArrayList<>();

        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(RecordsEntry._ID));
            itemIds.add(itemId);
        }
        cursor.close();

        //endregion QUERY EXAMPLES

        Cursor records = dbHelper.getAllRecords();

        if(records.moveToFirst()){
            Log.i(LOG_RECORD, "Registros encontrados! ");

            List<RecordModel> receivedData = new ArrayList<>();

            do {
                Log.i(LOG_RECORD, "entramos al while..!");
                RecordModel newRecord = new RecordModel();
                newRecord.setId(Integer.parseInt(records.getString(0)));
                newRecord.setTimestamp(records.getString(1));
                newRecord.setDrinkAmount(Float.parseFloat(records.getString(2)));
                newRecord.setStatusCode(Integer.parseInt(records.getString(3)));

                Log.i(LOG_RECORD, "Añadido: " + newRecord.getTimestamp() + " " + newRecord.getDrinkAmount() + " " + newRecord.getStatusCode());
                receivedData.add(newRecord);
            }
            while (records.moveToNext());

            Log.i(LOG_RECORD, "SALIMOS DEL while..! " + receivedData.size() + " " + records.getCount());
            /*
            // Registros cargados
            List<RecordModel> receivedData = new ArrayList<>();

            receivedData.add(new RecordModel(0, "1557480446", 100));
            receivedData.add(new RecordModel(0, "1557480461",30));
            receivedData.add(new RecordModel(0, "1557481044",0));
            receivedData.add(new RecordModel(0, "1557481061",70));

            //fron notification:
            receivedData.add(new RecordModel(0, "1557501828",true, true));
            receivedData.add(new RecordModel(0, "1557501843",95, true));
            receivedData.add(new RecordModel(0, "1557505213",true, true));
            */

            Collections.reverse(receivedData);
            adapterSwitch.overrideList(receivedData);
            adapterSwitch.notifyDataSetChanged();

            textViewRecordMessage.setText("");
            recyclerRecords.setVisibility(View.VISIBLE);
            progressBarRecord.setVisibility(View.GONE);

        }
        else {
            // Sin registros
            textViewRecordMessage.setText("No tienes registros todavia!");
            recyclerRecords.setVisibility(View.GONE);
            progressBarRecord.setVisibility(View.GONE);
        }
        records.close();

    }
  
    private void setAppStyle() {

        Calendar rightNow = Calendar.getInstance();
        int currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY);
        int currentMinute= rightNow.get(Calendar.MINUTE);

        int currentSeconds = ((currentHourIn24Format * 60) + currentMinute) * 60;
        int startSeconds = ((settings.getStartHour() * 60) + settings.getStartMin()) * 60;
        int endSeconds = ((settings.getEndHour() * 60) + settings.getEndMin()) * 60;

        if ((currentSeconds  >= startSeconds) && (currentSeconds <= endSeconds)){
            Log.i(LOG_WATER_DRINK_ACT, "La alarma esta activa porque estamos en horas de trabajo");

            settings.setAlarmWorking(true);
            buttonAlarmStatus.setTextColor(getResources().getColor(R.color.red_indio));
            buttonAlarmStatus.setText("Desactivar Alarma");
            textViewNextDrink.setText("Listos para tomar agua en cualquier momento!");
        }
        else {
            Log.i(LOG_WATER_DRINK_ACT, "La alarma esta fuera de horas laborales");
            settings.setAlarmWorking(false);
            settings.setDrinkNextTime("OVER");

            buttonAlarmStatus.setTextColor(getResources().getColor(R.color.green));
            buttonAlarmStatus.setText("Activar Alarma");
            textViewNextDrink.setText("Horas de descanso!\nLas notificaciones estan desactivadas");
        }

        if(!settings.isAlarmWorking()){
            root.setBackgroundColor(getResources().getColor(R.color.black));

            if(settings.isGoalReached()){
                //Estilo de la app cuando hemos alcanzado la meta y la alarma esta NO activa
                buttonDrink.setTextColor(getResources().getColor(R.color.purple));
                buttonDrink.setBackground(getResources().getDrawable(R.drawable.button_background_02_selector));

                pulsatorColor = R.color.white;
                pulsatorInterpolator = PulsatorLayout.INTERP_DECELERATE;
                pulsatorDuration = 8000;
                pulsatorCount = 2;
            }
            else {
                //Estilo de la app cuando no hay alarmas, la hora de finalizar esta activa
                buttonDrink.setTextColor(getResources().getColor(R.color.white));
                buttonDrink.setBackground(getResources().getDrawable(R.drawable.button_background_04_selector));

                pulsatorColor = R.color.purple;
                pulsatorInterpolator = PulsatorLayout.INTERP_DECELERATE;
                pulsatorDuration = 8000;
                pulsatorCount = 2;
            }
            setPulsatorLayout(true);
        }
        else{
            root.setBackgroundColor(getResources().getColor(R.color.white));

            if(settings.isGoalReached()){
                //Estilo de la app cuando hemos alcanzado la meta y la alarma esta activa
                buttonDrink.setTextColor(getResources().getColor(R.color.black));
                buttonDrink.setBackground(getResources().getDrawable(R.drawable.button_background_02_selector));

                pulsatorColor = R.color.yellow;
                pulsatorInterpolator = PulsatorLayout.INTERP_ACCELERATE_DECELERATE;
                pulsatorDuration = 7000;
                pulsatorCount = 3;
            }
            else {
                //Estilo por defecto de la aplicacion
                buttonDrink.setTextColor(getResources().getColor(R.color.white));
                buttonDrink.setBackground(getResources().getDrawable(R.drawable.button_background_01_selector));

                pulsatorColor = R.color.blue_water;
                pulsatorInterpolator = PulsatorLayout.INTERP_ACCELERATE_DECELERATE;
                pulsatorDuration = 5000;
                pulsatorCount = 4;
            }

            setPulsatorLayout(true);
        }
    }

    private void updateProgress(){

        final String simbolMililiters = "mL";
        final String simbolLiters = "L";

        float todayProgress = settings.getTodayProgress();
        String currentAmount = String.valueOf(todayProgress) + simbolMililiters;
        if(todayProgress % 1 == 0){
            long integer = (long) todayProgress;
            currentAmount = String.valueOf(integer) + simbolMililiters;
        }

        int metricVolume = settings.getGoalMetricVolume();
        float goalValue = settings.getGoalValue();
        String goalAmount = String.valueOf( goalValue );
        switch (metricVolume){
            case 0:
                if(goalValue % 1 == 0){
                    long integer = (long) goalValue;
                    goalAmount = String.valueOf(integer) + simbolMililiters;
                }
                else{
                    DecimalFormat formatDecimal = new DecimalFormat("#.##");
                    goalAmount = formatDecimal.format(goalValue) + simbolMililiters;
                }
                break;
            case 1:
                goalValue = goalValue/1000;
                if(goalValue % 1 == 0){
                    long integer = (long) goalValue;
                    goalAmount = String.valueOf(integer) + simbolLiters;
                }
                else{
                    DecimalFormat formatDecimal = new DecimalFormat("#.##");
                    goalAmount = formatDecimal.format(goalValue) + simbolLiters;
                }
                break;
        }

        String progress =  "Llevas " + currentAmount + " de " + goalAmount;

        textViewProgress.setText(progress);

    }

    private void logFabricUser() {
        // TODO: Use the current user's information
        // You can call any combination of these three methods
        Crashlytics.setUserIdentifier(utils.getDeviceName(WaterDrinkActivity.this));
        Crashlytics.setUserEmail("Testing_Email");
        Crashlytics.setUserName("Testing_Name");

        //Custom Keys
        Crashlytics.setInt("current_level", 3);
        Crashlytics.setString("last_UI_action", "logged_in");

        //Logging
        Crashlytics.log("Higgs-Boson detected! Bailing out...");

    }

    private void buttonDrink(){
        if(countDownTimer != null){
            long remainingTimeInMillis = countDownTimer.getTimeInMillis();
            Log.i(LOG_BUTTON_DRINK, "Obtener tiempo restante de la anterior alarma. Restante en millis: " + remainingTimeInMillis);

            int remainingHours = utils.getHours(remainingTimeInMillis);
            int remainingMinutes = utils.getMinutes(remainingTimeInMillis);
            int remainingSeconds = utils.getSeconds(remainingTimeInMillis);

            String stats;

            if(remainingHours > 0){ // Mas de 60 Minutos
                stats = "Te has adelantado por " + remainingHours + " hora " + remainingMinutes + " minutos " + remainingSeconds + " segundos a tu tiempo de agua";
            }
            else if(remainingMinutes > 0){ // Mas de 60 segundos
                stats = "Te has adelantado por " + remainingMinutes + " minutos " + remainingSeconds + " segundos a tu tiempo de agua";
            }
            else stats = "Te has adelantado por " + remainingSeconds + " segundos a tu tiempo de agua";

            textViewStats.setText(stats);
            countDownTimer.cancel();
        }

        if(settings.isAlarmWorking()){
            long timeInMillis = utils.getMillis(settings.getIntervalValue());
            long alarmNextTime = SystemClock.elapsedRealtime() + timeInMillis; // cada X minutos

            AlarmApp.getInstance().setNextDrinkingValue();
            AlarmApp.getInstance().updateAlarm(alarmNextTime);

            setPulsatorLayout(false);
            countDownTimer = new CountDownTimerDrink(timeInMillis, 1000);
            countDownTimer.start();
        }
        else{
            setPulsatorLayout(true);
            textViewNextDrink.setText("");
            textViewTips.setText(awesomeTexts.getCoutdownTextDone());
        }

        buttonDrink.setText("Bien Hecho!");
    }

    private void restoreNotificationValues(){
        int position = 0;
        int skipSeconds =  settings.getNotiSkipTime();
        switch (skipSeconds){
            case 300: //5min * 60
                position = 0;
                break;
            case 600: //10
                position = 1;
                break;
            case 900: //15
                position = 2;
                break;
            case 1200: //20
                position = 3;
                break;
        }
        spinnerNotiSkipTimes.setSelection(position);

        position = 0;
        float drinkAmount = settings.getNotiDrinkAmount();
        if (drinkAmount == 10f) {
            position = 0;
        } else if (drinkAmount == 50f) {
            position = 1;
        } else if (drinkAmount == 100f) {
            position = 2;
        } else if (drinkAmount == 150f) {
            position = 3;
        }
        spinnerNotiDrinkAmount.setSelection(position);

    }

    private void restoreGoalValues() {
        //settings.deleteGoalData();

        int selection = settings.getGoalMetricVolume();
        Log.i(LOG_SETTING_GOAL, "Goal Metric: " + selection);

        spinnerDayMax.setSelection(selection);

        setGoalMetricVolumeConverted(selection);

        int allSeconds = settings.getIntervalValue();
        Log.i(LOG_SETTING_GOAL, "Interval value: " + allSeconds);

        String result = utils.convertSecondTo_HHmmss_String(allSeconds);

        String[] parts = result.split(":");
        int hours = Integer.parseInt( parts[0] );
        int minutes = Integer.parseInt( parts[1] );
        int secs = Integer.parseInt( parts[2] );

        // set the value for current hours
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePickerInterval.setHour(hours); // from api level 23
            timePickerInterval.setMinute(minutes);
        }
        else {
            timePickerInterval.setCurrentHour(hours); // before api level 23
            timePickerInterval.setCurrentMinute(minutes);
        }

        timePickerInterval.setIs24HourView(true);
        timePickerInterval.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS); // Disable edit, enable just spinner mode

        String textReminder = "Recordar cada:\n" + utils.getTimeString(hours, minutes, 0);
        textViewReminder.setText(textReminder);

    }

    private void setGoalMetricVolumeConverted(int position){
        float mililiters = settings.getGoalValue();

        Log.i(LOG_SETTING_GOAL,"Goal Value: " + mililiters);

        String goal = "";
        switch (position){
            case 0:
                goal = "mililitros";
                break;
            case 1:
                goal = "litros";
                mililiters = (mililiters/1000);
                break;
        }

        Log.i(LOG_SETTING_GOAL, "Goal Value After Switch: " + mililiters);

        convertNotSave = true;
        String amount = String.valueOf(mililiters);

        if(mililiters % 1 == 0){
            long integer = (long) mililiters;
            amount = String.valueOf(integer);
        }
        else{
            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
            otherSymbols.setDecimalSeparator('.');
            otherSymbols.setGroupingSeparator('.');

            DecimalFormat formatDecimal = new DecimalFormat("#.##", otherSymbols);
            amount = formatDecimal.format(mililiters);
        }

        editTextDayMax.setText(amount);

        goal = "Tu meta de hoy es de " + amount + " " + goal + " de agua";
        textViewStats.setText(goal);
    }

    private void getHourTime(int defaultHour, int defaultMinute, boolean isStartTime){

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            Log.i(LOG_SETTING_HOURS, "Del selector obtuvimos: " + hourOfDay + ":" + minute + " es hora inicial? " + isStartTime);

            int selectedTimeSecs = ((hourOfDay * 60) + minute) * 60;

            if(isStartTime){
                //entre 01:00 a 12:00
                int rangeStartHourStartInSecs = ((1 * 60) + 0) * 60;
                int rangeStartHourEndInSecs = ((12 * 60) + 0) * 60;

                if(selectedTimeSecs >= rangeStartHourStartInSecs  && selectedTimeSecs <= rangeStartHourEndInSecs){
                    settings.setStartHour(hourOfDay);
                    settings.setStartMin(minute);
                    setSettingsValues(hourOfDay, minute, true);
                    Log.i(LOG_SETTING_HOURS, "Horas iniciales establecidas");
                }
                else {
                    Log.i(LOG_SETTING_HOURS, "Hora inicial no esta dentro del rango valido");
                    Toast.makeText(this, "La hora seleccionada no esta en el rango", Toast.LENGTH_LONG).show();
                }
            }
            else{
                //entre las 18:00 a 23:59
                int rangeEndHourStartInSecs= ((18 * 60) + 0) * 60;
                int rangeEndHourEndInSecs = ((23 * 60) + 59) * 60;

                if(selectedTimeSecs >= rangeEndHourStartInSecs  && selectedTimeSecs <= rangeEndHourEndInSecs){
                    settings.setEndHour(hourOfDay);
                    settings.setEndMin(minute);
                    setAppStyle();
                    setSettingsValues(hourOfDay, minute, false);
                    Log.i(LOG_SETTING_HOURS, "Hora final establecida");
                }
                else {
                    Log.i(LOG_SETTING_HOURS, "Hora final no esta dentro del rango valido");
                    Toast.makeText(this, "La hora seleccionada no esta en el rango", Toast.LENGTH_LONG).show();
                }
            }

        }, defaultHour, defaultMinute, false);


        if(isStartTime){
            timePickerDialog.setCustomTitle(utils.createCustomTitle(this,
                    "Selecciona la hora de inicio\n(entre 01:00am a 12:00pm)",
                    getResources().getColor(R.color.com_facebook_blue),
                    getResources().getColor(R.color.white),
                    18));
        }
        else {
            timePickerDialog.setCustomTitle(utils.createCustomTitle(this,
                    "Selecciona la hora de finalizar\n(entre las 06:00pm a 11:59pm)",
                    getResources().getColor(R.color.com_facebook_blue),
                    getResources().getColor(R.color.white),
                    18));
        }

        timePickerDialog.show();
    }

    private void setSettingsValues(int hourOfDay, int minute, boolean isStartTime){
        Log.i(LOG_SETTING_HOURS, "Establecer: " + hourOfDay + ":" + minute + " - es inicio? " + isStartTime);
        final String ZERO = "0";

        //Formateo el hora obtenido: antepone el 0 si son menores de 10
        String formattedHour =  (hourOfDay < 10)? String.valueOf(ZERO + hourOfDay) : String.valueOf(hourOfDay);

        //Formateo el minuto obtenido: antepone el 0 si son menores de 10
        String formattedMinute = (minute < 10)? String.valueOf(ZERO + minute):String.valueOf(minute);

        //Obtengo el valor a.m. o p.m., dependiendo de la selección del usuario
        String AM_PM;
        if(hourOfDay < 12) {
            if(hourOfDay == 0) formattedHour = "12";
            AM_PM = "a.m.";
        } else {
            formattedHour = String.valueOf( hourOfDay - 12 );
            AM_PM = "p.m.";
        }
        //Muestro la hora con el formato deseado
        String pickedHour = formattedHour + ":" + formattedMinute + " " + AM_PM;

        if(isStartTime){
            String startText = "Iniciamos a las "+ pickedHour +" de la mañana";
            textViewMyStart.setText(startText);
            AlarmFirst.getInstance().adminFirstAlarm(WaterDrinkActivity.this);
        }
        else{
            String endText = "Concluimos cerca de las " + pickedHour +" de la noche ";
            textViewMyEnd.setText(endText);
            AlarmLast.getInstance().adminLastAlarm(WaterDrinkActivity.this);
        }
    }

    private void activateAlarmOnRestartPhone(boolean enabled) {
        int state = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

        if(enabled){
            state = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
        }

        ComponentName receiver = new ComponentName(WaterDrinkActivity.this, AndroidActionsReceiver.class);
        PackageManager pm = WaterDrinkActivity.this.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                state,
                PackageManager.DONT_KILL_APP);

    }

    //region PULSATOR
    int pulsatorColor = R.color.blue_water;
    int pulsatorInterpolator = PulsatorLayout.INTERP_DECELERATE;
    int pulsatorDuration = 900;
    int pulsatorCount = 3;

    private void setPulsatorLayout(boolean activate){
        if(activate){
            pulsatorLayout.setColor(getResources().getColor(pulsatorColor));
            pulsatorLayout.setInterpolator(pulsatorInterpolator);
            pulsatorLayout.setDuration(pulsatorDuration);
            pulsatorLayout.setCount(pulsatorCount);

            if(!pulsatorLayout.isStarted())
                pulsatorLayout.start();
        }
        else{
            pulsatorLayout.stop();
        }
    }
    //endregion PULSATOR

    //region ACTIVITY CALLBACKS

    @Override
    protected void onStop() {
        Log.i(LOG_WATER_DRINK_ACT, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(LOG_WATER_DRINK_ACT, "onDestroy");
        if(dbWrite != null) dbWrite.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        Log.i(LOG_WATER_DRINK_ACT, "onResume");
        super.onResume();
        ThisApplication.onResume();
        updateProgress();

        //Cerrar la notificacion en caso de estar activa
        Log.i(LOG_WATER_DRINK_ACT, "Cerrar notificacion en caso que exista");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(WaterDrinkActivity.this);
        notificationManager.cancel(Constants.NOTIFICATION_ID_APP);
    }

    @Override
    protected void onPause() {
        Log.i(LOG_WATER_DRINK_ACT, "onPause");
        super.onPause();
        ThisApplication.onPause();

        if(countDownTimer == null && settings.isAlarmWorking()){

            // Si es hora de tomar agua (alarma nula) y el usuario abandona la app, vamos a poner una alarma en 5min,
            // cuando se active la alarma (si aun esta la app en horas activas) entonces lanzamos notificacion.

            Log.i(LOG_WATER_DRINK_ACT, "La alarma esta funcionando: Salio de la app, alarma en: 5 mins");
            long nextReminder = SystemClock.elapsedRealtime() + ((5 * 60) * 1000); // cada 5 minutos
            AlarmApp.getInstance().updateAlarm(nextReminder);
        }

        if(isDrinkTimeFromNotification){
            Log.i(LOG_RECORD, "Entraste a la app desde la notificacion y no tomaste agua. Registrar 3");
            recordDrink(0,3);
            isDrinkTimeFromNotification = false;
        }
    }





    //endregion ACTIVITY CALLBACKS

    //region COUNTDOWN
    private class CountDownTimerDrink extends CountDownTimer{

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */

        private long remainingTimeInMillis;

        public CountDownTimerDrink(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            remainingTimeInMillis = millisUntilFinished;

            int hours = utils.getHours(remainingTimeInMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;
            String text = "";

            if(hours > 0 ){
                text = String.format(Locale.getDefault(), "Siguiente en %2d horas %2d minutos %2d segundos",
                        hours,
                        minutes,
                        seconds);
            }
            else if(minutes > 0){
                text = String.format(Locale.getDefault(), "Siguiente en %2d minutos %2d segundos",
                        minutes,
                        seconds);
            }
            else {
                text = String.format(Locale.getDefault(), "Siguiente en %2d segundos",
                        seconds);
            }

            textViewNextDrink.setText(text);
        }

        @Override
        public void onFinish() {
            textViewNextDrink.setText("");
            textViewTips.setText(awesomeTexts.getCoutdownTextDone());
            //textViewStats.setText("");
            buttonDrink.setText("Ya tome agua!");

            setAppStyle();
            setPulsatorLayout(true);
            if(ThisApplication.isActivityVisible())
                AlarmApp.getInstance().cancelAlarm(); //Cancelar alarma si estamos en la app

        }

        public long getTimeInMillis(){
            return remainingTimeInMillis;
        }


    }
    //endregion COUNTDOWN

    //region CALLBACKS
    @Override
    public void onReturnCallback(boolean status, String message, int code) {

    }

    @Override
    public void onYesResponse(String code, String message) {
        if(code.equalsIgnoreCase(CODE_SET_ALARM)){
            finish();
        }
    }

    @Override
    public void onNoResponse(String code, String message) {
        if(code.equalsIgnoreCase(CODE_SET_ALARM)){
            buttonSetEnd.callOnClick();
        }
    }
    //endregion CALLBACKS

    private void getNextTimeCalculation(){

        //1 dia = 1440 minutos
        //01 litro = 1000ml

        float userGoal = 1500; // 1500 mililitros = 1.5 litros
        float currentDrink = 200; //200 mililitros
        float userReminderInterval = 45; //45 minutos

        //Hora de inicio segun usuario
        int startHourOfDay = 6; //6am
        int startMinute = 0; //
        int startTimeInSeconds = utils.getSeconds(startHourOfDay, startMinute);


        //Hora de finalizar segun usuario
        int endHourOfDay = 20; //8pm
        int endMinute = 0; //
        int endTimeInSeconds = utils.getSeconds(endHourOfDay, endMinute);


        //Hora de finalizar maxima
        int endMaxHourOfDay = 23; //Medianoche
        int endMaxMinute = 59; //
        int endMaxTimeInSeconds = utils.getSeconds(endMaxHourOfDay, endMaxMinute);


        //hora actual
        Calendar calendar = Calendar.getInstance();
        int currentHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        int currentTimeInSeconds = utils.getSeconds(currentHourOfDay, currentMinute);

        //1 dia - 24hras

        //calcular t0do normal
        /*
             Hora que tomas agua y presionas el boton = 6am

        20 - 6 = 14horas
               = (1500 mililitros - currentDrink) en 14 horas
               = (1500 - 200)/14 (14 horas en segundos = 50400)
               =  1300 / 14
               1300 mililitros de agua a tomar en las proximas 14 horas...

               -- digamos que yo puedo tomar maximo 100ml de agua en cada intervalo...

                100 / 14


         */

        //TODO: CALCULEMOS PUES!

        // 0:45 min para tomar agua
        float nextTimeInSeconds = 2700; // 45 minutos
        nextTimeInSeconds = userGoal/(startTimeInSeconds - endTimeInSeconds);


        /*
         1500 - 200 = 1300 ml --- 6am





          */


    }

    private void recordDrink(float drinkAmount, int statusCode) {
        // Gets the data repository in write mode
        dbWrite = dbHelper.getWritableDatabase();

        RecordModel newRecord = new RecordModel(utils.getCurrentTimestamp(), drinkAmount, statusCode);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = dbWrite.insert(RecordsContract.RecordsEntry.TABLE_NAME,
                null,
                newRecord.toContentValues()
        );

        dbWrite.close();
        Log.i(LOG_RECORD, "Registramos con estado: " + statusCode + ". Resultado: " + (newRowId == -1 ? "FALLO al guardar" : "Registrado!"));

    }

    private void deletePreviousRecord() {

        // Gets the data repository in write mode
        dbWrite = dbHelper.getWritableDatabase();

        // Eliminar ultimo registro si es status = 6 (abandono sin beber) ;
        // Eliminar ultimo registro si es status = 3 (no tomo agua entro a la notificacion);
        try{
            dbWrite.execSQL("DELETE FROM "
                    + RecordsEntry.TABLE_NAME +
                    " WHERE "
                    + RecordsEntry._ID +
                    " = (SELECT MAX("
                    + RecordsEntry._ID +
                    ") FROM "
                    + RecordsEntry.TABLE_NAME +
                    " ) AND ("
                    + RecordsEntry.COLUMN_NAME_STATUS_CODE +
                    " = 3 OR "
                    + RecordsEntry.COLUMN_NAME_STATUS_CODE +
                    " = 6)");
        }
        catch (Exception e){
            //Toast.makeText(this, "Error en base de datos", Toast.LENGTH_SHORT).show();
            Log.i(LOG_RECORD, "Error:\n\n" + e.toString() );
        }

        dbWrite.close();
        Log.i(LOG_RECORD, "Borrado del ultimo registro terminado...");

    }

}
