package com.eahm.drinkwaterapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.eahm.drinkwaterapp.Class.SettingsClass;
import com.eahm.drinkwaterapp.Interfaces.Callbacks;
import com.eahm.drinkwaterapp.Models.News;
import com.eahm.drinkwaterapp.Models.NewsMedia;
import com.eahm.drinkwaterapp.Models.UserModel;
import com.eahm.drinkwaterapp.Utils.Constants;
import com.eahm.drinkwaterapp.Utils.UserManagement;
import com.eahm.drinkwaterapp.Utils.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SplashScreen extends AppCompatActivity implements Callbacks, ValueEventListener {

    //region VARIABLES
    private final static String TAG_CONNECTED = "TAG_CONNECTED";
    private final static String TAG_NEWS = "TAG_NEWS";

    String language = "Español";
    boolean worksOffline = true; // Esta app funciona sin internet?
    boolean appConnected = true; // conectividad a internet

    private ImageView imageViewMarkerDesign;
    private TextView textViewExtraInfo;
    private TextView textViewLoadingApp;
    private VideoView videoViewSplashVideo;

    Utils utils = new Utils();

    private boolean stopFadeAnimation = false;

    boolean appEnabled = false;
    String appOnDisableMessage = "";

    boolean updateForce = true;
    String updateCurrentVersion = "NONE";
    String updateOnNewVersionMessage = "";

    UserManagement userManager = new UserManagement();
    private UserModel cUser = new UserModel("","","","","","","","", null);
    private FirebaseAuth auth;


    boolean appInfo = false;
    boolean appNews = false;
    boolean appUser = false;
    boolean videoSplashScreenDone = false;
    //endregion

    CardView includeLayoutPrivacyPolicy;
    TextView textViewPPContent;
    TextView textViewPPInformation;
    CheckBox checkBoxPP;
    Button buttonPPContinue;

    boolean closeApp = false;

    @Override
    public void onBackPressed() {
        if(includeLayoutPrivacyPolicy.getVisibility() == View.VISIBLE){
            if(!closeApp){
                closeApp = true;
                Toast.makeText(this, "Presiona otra vez para salir", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(()->{
                    closeApp = false;
                }, 2500);
            }
            else super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //((AnimationDrawable) getWindow().getDecorView().getBackground()).start();
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        //Listener para verificar si tenemos internet (siempre inicia con desconectado, aun con internet)
        DatabaseReference conection = FirebaseDatabase.getInstance().getReference(".info/connected");
        conection.addValueEventListener(this);

        language = Utils.convertFirstLetterToUpperCase(Locale.getDefault().getDisplayLanguage());

        // 0: Without(Testing) 1: Normal  2: Video
        switch (Constants.SPLASH_SCREEN_MODE){
            case "0":
                //region WITHOUT
                getAppInformation();
                getAppNewsCheckAvailability();
                getAppCurrentUser();
                //endregion WITHOUT
                break;
            case "1":
                //region NORMAL
                setContentView(R.layout.activity_splash_screen);
                
                imageViewMarkerDesign = findViewById(R.id.imageViewLoadingDesign);
                textViewExtraInfo = findViewById(R.id.textViewExtraInfo);
                textViewLoadingApp = findViewById(R.id.textViewLoadingApp);

                //initialize and wait a response since the splash screen is in progress...
                textViewLoadingApp.setText(getString(R.string.ss_getting_content));

                //Get data from server
                //setAppInformation(); // this method is called when the .info/connected listener is called!

                //setAnimation while we wait...
                imageViewMarkerDesign.setAlpha(1f);
                fadeOut(imageViewMarkerDesign, 1000);

                //endregion NORMAL
                break;
            case "2":
                //region VIDEO
                setContentView(R.layout.activity_splash_screen);
                imageViewMarkerDesign = findViewById(R.id.imageViewLoadingDesign);
                textViewLoadingApp = findViewById(R.id.textViewLoadingApp);
                videoViewSplashVideo = findViewById(R.id.videoViewSplashVideo);

                imageViewMarkerDesign.setVisibility(View.GONE);
                textViewLoadingApp.setVisibility(View.GONE);
                videoViewSplashVideo.setVisibility(View.VISIBLE);

                try {
                    Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash_screen_video);
                    videoViewSplashVideo.setVideoURI(video);

                    videoViewSplashVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        public void onCompletion(MediaPlayer mp) {
                            if (isFinishing()) return;

                            textViewLoadingApp.setVisibility(View.VISIBLE);
                            //textViewLoadingApp.setTextColor(getResources().getColor(R.color.white));
                            videoSplashScreenDone = true;
                            setAppInformation();
                        }
                    });

                    getAppInformation();
                    getAppNewsCheckAvailability();
                    getAppCurrentUser();

                    videoViewSplashVideo.start();

                } catch (Exception ex) {
                    if (isFinishing()) return;
                    startMainApp();
                }
                //endregion VIDEO
                break;
            case "3":
                break;
        }

        includeLayoutPrivacyPolicy = findViewById(R.id.includeLayoutPrivacyPolicy);
        textViewPPContent = findViewById(R.id.textViewPPContent);
        textViewPPInformation = findViewById(R.id.textViewPPInformation);
        checkBoxPP = findViewById(R.id.checkBoxPP);
        buttonPPContinue = findViewById(R.id.buttonPPContinue);

        checkBoxPP.setOnCheckedChangeListener((buttonView, isChecked) -> textViewPPInformation.setVisibility(View.INVISIBLE));

        buttonPPContinue.setOnClickListener(v -> {
            if(checkBoxPP.isChecked()){
                settings.setPrivacyPolicyStatus(true);
                textViewPPInformation.setVisibility(View.INVISIBLE);
                includeLayoutPrivacyPolicy.setVisibility(View.GONE);
                setAppInformation();
            }
            else {
                textViewPPInformation.setVisibility(View.VISIBLE);
            }
        });

        textViewPPInformation.setVisibility(View.INVISIBLE);
    }

    //region ANIMATION
    public void fadeOut(View view, long startDelay){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.animate().alpha(0).setDuration(2500).setStartDelay(startDelay).withEndAction(()-> {
                if (!stopFadeAnimation) fadeIn(view,0);
            });
        }
    }

    public void fadeIn(View view, long startDelay){
        view.setAlpha(0f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.animate().alpha(1).setDuration(2500).setStartDelay(startDelay).withEndAction(()-> {
                if (!stopFadeAnimation) fadeOut(view,0);
            });
        }
    }
    //endregion

    //region GET APP CONFIGURATION
    DatabaseReference databaseRefAppInfo;
    ValueEventListener vEventListenerAppInfo;

    private void getAppInformation(){
        databaseRefAppInfo = FirebaseDatabase.getInstance().getReference("app");
        vEventListenerAppInfo = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    appEnabled = false;
                    appOnDisableMessage = "";

                    if (dataSnapshot.hasChild("application")){
                        if (dataSnapshot.child("application").hasChild("enabled"))
                            appEnabled = dataSnapshot.child("application").child("enabled").getValue(true).toString().equalsIgnoreCase("true");

                        if (dataSnapshot.child("application").hasChild("onDisableMessage")){

                            if(dataSnapshot.child("application").child("onDisableMessage").hasChild(language)){
                                appOnDisableMessage = dataSnapshot.child("application").child("onDisableMessage").child(language).getValue().toString();
                            }
                            else appOnDisableMessage = "We currently disabled the application, try again later...";
                        }

                        if (dataSnapshot.child("application").hasChild("privacyPolicyUrl")){
                            privacyPolicyURL = dataSnapshot.child("application").child("privacyPolicyUrl").getValue().toString();
                        }

                    }

                    updateForce = true;
                    updateCurrentVersion = "NONE";
                    updateOnNewVersionMessage = "";

                    if (dataSnapshot.hasChild("updates")){
                        if (dataSnapshot.child("updates").hasChild("enabledVersion"))
                            updateCurrentVersion  = dataSnapshot.child("updates").child("enabledVersion").getValue(true).toString();

                        if (dataSnapshot.child("updates").hasChild("forceUpdate"))
                            updateForce = dataSnapshot.child("updates").child("forceUpdate").getValue(true).toString() == "true";

                        if (dataSnapshot.child("updates").hasChild("onNewUpdateMessage")) {

                            if(dataSnapshot.child("updates").child("onNewUpdateMessage").hasChild(language)){
                                updateOnNewVersionMessage = dataSnapshot.child("updates").child("onNewUpdateMessage").child(language).getValue().toString();
                            }
                            else updateOnNewVersionMessage = "A new version is available. Download it now!";
                        }
                    }

                    appInfo = true;
                    setAppInformation();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SplashScreen.this, "App Information Error: " + databaseError.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        databaseRefAppInfo.addListenerForSingleValueEvent(vEventListenerAppInfo);
    }

    DatabaseReference drNewsCheck;
    ValueEventListener velNewsCheck;

    String privacyPolicy = "";
    String privacyPolicyURL = "";

    private void getAppNewsCheckAvailability() {
        final String country = Utils.getUserCountry(SplashScreen.this);
        if(country != null) {

            drNewsCheck =  FirebaseDatabase.getInstance().getReference().child("communication").child(language);
            velNewsCheck = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!appConnected) return;


                    if (dataSnapshot.exists() && dataSnapshot.hasChild("countries")) {

                        boolean countryAvailable = false;

                        for (DataSnapshot countries : dataSnapshot.child("countries").getChildren()) {
                            String key = countries.getKey();
                            Log.i("LANGUAGUE", key + " == " + country);
                            if (key.equalsIgnoreCase(country)) {
                                countryAvailable = true;
                            }
                        }

                        if(dataSnapshot.hasChild("privacyPolicy") ){
                            privacyPolicy = dataSnapshot.child("privacyPolicy").getValue().toString();
                        }

                        if(countryAvailable) {
                            getAppNewsByCountry();
                            return;
                        }
                    }

                    getAppNewsDone();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    //Toast.makeText(SplashScreen.this, "1. Error: " + databaseError.toString(), Toast.LENGTH_SHORT).show();
                    getAppNewsDone();
                }
            };
            drNewsCheck.addListenerForSingleValueEvent(velNewsCheck);

        }
        else getAppNewsDone();

    }

    Query queryNewsByCountry;
    ValueEventListener velNewsByCountry;

    private void getAppNewsByCountry(){
        int limitMaxOfNewsTo = 4;

        queryNewsByCountry = FirebaseDatabase.getInstance().getReference("communication").child(language).child("news")
                .orderByChild("enabled").equalTo(true)
                .limitToLast(limitMaxOfNewsTo);

        Log.i(TAG_NEWS, "GET NEWS");
        velNewsByCountry = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    if(!((ThisApplication) getApplication()).getNewsList().isEmpty()){
                        ((ThisApplication) getApplication()).clearNewsList();
                    }

                    for (DataSnapshot item : dataSnapshot.getChildren()) {

                        News newsModelItem = new News();

                        if (item.hasChild("enabled")) newsModelItem.setEnabled(item.child("enabled").getValue(true).toString().equalsIgnoreCase("true"));
                        if (item.hasChild("publishedTimestamp")) newsModelItem.setPublishedTimestamp(item.child("publishedTimestamp").getValue(true).toString());


                        if (item.hasChild("title")) newsModelItem.setTitle(item.child("title").getValue(true).toString());
                        if (item.hasChild("content")) newsModelItem.setContent(item.child("content").getValue(true).toString());
                        if (item.hasChild("contentUrl")) newsModelItem.setContentUrl(item.child("contentUrl").getValue(true).toString());

                        if (item.hasChild("media")){
                            List<NewsMedia> newsMediaList = new ArrayList<>();

                            for (DataSnapshot itemMedia: item.child("media").getChildren()) {

                                NewsMedia newsMediaModelItem = new NewsMedia();
                                if (itemMedia.hasChild("enabled")) newsMediaModelItem.setEnabled(itemMedia.child("enabled").getValue(true).toString().equalsIgnoreCase("true"));
                                if (itemMedia.hasChild("isVideo")) newsMediaModelItem.setVideo(itemMedia.child("isVideo").getValue(true).toString().equalsIgnoreCase("true"));

                                if (itemMedia.hasChild("coverImageUrl")) newsMediaModelItem.setCoverImageUrl(itemMedia.child("coverImageUrl").getValue(true).toString());
                                if (itemMedia.hasChild("coverImageScaleType")) newsMediaModelItem.setCoverImageScaleType(itemMedia.child("coverImageScaleType").getValue(true).toString());
                                if (itemMedia.hasChild("mediaUrl")) newsMediaModelItem.setMediaUrl(itemMedia.child("mediaUrl").getValue(true).toString());
                                if (itemMedia.hasChild("mediaOnClickUrl")) newsMediaModelItem.setMediaOnClickUrl(itemMedia.child("mediaOnClickUrl").getValue(true).toString());

                                newsMediaList.add(newsMediaModelItem);
                            }

                            newsModelItem.setMedia(newsMediaList);
                        }
                        else newsModelItem.setMedia(null);

                        if (item.hasChild("titleTextSize")) newsModelItem.setTitleTextSize(Integer.parseInt(item.child("titleTextSize").getValue(true).toString()));
                        if (item.hasChild("colorPrimaryRGB")) newsModelItem.setColorPrimaryRGB(item.child("colorPrimaryRGB").getValue(true).toString());
                        if (item.hasChild("colorSecondaryRGB")) newsModelItem.setColorSecondaryRGB(item.child("colorSecondaryRGB").getValue(true).toString());
                        if (item.hasChild("colorTextRGB")) newsModelItem.setColorTextRGB(item.child("colorTextRGB").getValue(true).toString());

                        ((ThisApplication) getApplication()).getNewsList().add(newsModelItem);
                    }
                }

                Log.i(TAG_NEWS, "Cantidad de noticias: " +   ((ThisApplication) getApplication()).getNewsList().size());

                getAppNewsDone();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SplashScreen.this, "2. Error: " + databaseError.toString(), Toast.LENGTH_SHORT).show();
                getAppNewsDone();
            }
        };
        queryNewsByCountry.addListenerForSingleValueEvent(velNewsByCountry);

    }
    private void getAppNewsDone(){
        appNews = true;
        setAppInformation();
    }

    Task<SignInMethodQueryResult> fetch;
    DatabaseReference drCurrentUserDBVerification;
    ValueEventListener velCurrentUserDBVerification;

    private void getAppCurrentUser() {

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null){

            fetch = auth.fetchSignInMethodsForEmail(auth.getCurrentUser().getEmail() == null ? "" : auth.getCurrentUser().getEmail());
            fetch.addOnSuccessListener(new OnSuccessListener<SignInMethodQueryResult>() {
                @Override
                public void onSuccess(SignInMethodQueryResult signInMethodQueryResult) {

                    if(signInMethodQueryResult.getSignInMethods().isEmpty()){
                        // theres no Email, is deleted or invalid, user do not exist
                        auth.signOut();
                        appUser = true;
                        setAppInformation();
                    }
                    else {
                        Log.i("VALIDATION", "USUARIO EXISTE");
                        //USUARIO EXISTE signInMethodQueryResult.getSignInMethods() is not empty (user list)
                        String reference = "";

                        cUser = userManager.getCustomerData();
                        reference = cUser.getType();

                        drCurrentUserDBVerification = FirebaseDatabase.getInstance().getReference().child("users").child("customer").child(auth.getCurrentUser().getUid());
                        velCurrentUserDBVerification = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                                    String name = "", secondName = "", lastName = "", secondLastName = "", phone = "", type = "", profileImageUrl = "", currentDevice = "";

                                    if (dataSnapshot.hasChild("name"))
                                        name = dataSnapshot.child("name").getValue(true).toString();
                                    if (dataSnapshot.hasChild("secondName"))
                                        secondName = dataSnapshot.child("secondName").getValue(true).toString();
                                    if (dataSnapshot.hasChild("lastName"))
                                        lastName = dataSnapshot.child("lastName").getValue(true).toString();
                                    if (dataSnapshot.hasChild("secondLastName"))
                                        secondLastName = dataSnapshot.child("secondLastName").getValue(true).toString();
                                    if (dataSnapshot.hasChild("phone"))
                                        phone = dataSnapshot.child("phone").getValue(true).toString();
                                    if (dataSnapshot.hasChild("type"))
                                        type = dataSnapshot.child("type").getValue(true).toString();
                                    if (dataSnapshot.hasChild("profileImageUrl"))
                                        profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(true).toString();
                                    if (dataSnapshot.hasChild("currentDevice"))
                                        currentDevice = dataSnapshot.child("currentDevice").getValue(true).toString();

                                    userManager.setCustomerData(new UserModel(name, secondName, lastName, secondLastName, phone, type, profileImageUrl, currentDevice, null));

                                    //getAppCurrentUser has done correctly!
                                    appUser = true;
                                    setAppInformation();
                                    //done!
                                }
                                else {
                                    //DATASNAP NO EXISTE, DEBEMOS SALIR!
                                    userManager.removeCustomerData();
                                    cUser = userManager.getCustomerData();
                                    auth.signOut();
                                    appUser = true;
                                    setAppInformation();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(SplashScreen.this, "Get App User: Fail: " + databaseError.toString(), Toast.LENGTH_SHORT).show();
                                userManager.removeCustomerData();
                                cUser = userManager.getCustomerData();
                                auth.signOut();
                                appUser = true;
                                setAppInformation();
                            }
                        };
                        drCurrentUserDBVerification.addListenerForSingleValueEvent(velCurrentUserDBVerification);

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("VALIDATION","fetchSignInMethodsForEmail: Error: " + e.toString());
                    if(worksOffline){

                    }
                    else {
                        // Usuario no existe!
                        auth.signOut();
                        appUser = true;
                        setAppInformation();
                    }
                }
            });

        }
        else {
            // no hay usuario guardado, enviar a login
            auth.signOut();
            appUser = true;
            setAppInformation();
        }
    }


    SettingsClass settings = new SettingsClass();
    private void setAppInformation(){

        cancelAllEventListeners();

        if(appConnected) {
            //region GET APPLICATION INFORMATION
            if (!appInfo) {
                if (Constants.SPLASH_SCREEN_MODE.equalsIgnoreCase("1") || Constants.SPLASH_SCREEN_MODE.equalsIgnoreCase("2"))
                    textViewLoadingApp.setText(getString(R.string.ss_getting_info));

                getAppInformation();
                return;
            }

            if (appEnabled) {
                if (updateForce) {
                    try {
                        PackageInfo pinfo = null;
                        pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);

                        int versionNumber = pinfo.versionCode;
                        String versionName = pinfo.versionName;

                        if (!versionName.equalsIgnoreCase(updateCurrentVersion) && !updateCurrentVersion.equalsIgnoreCase("NONE")) {

                            stopFadeAnimation = true;
                            utils.displayAlertDialogYesNo(
                                    this,
                                    this,
                                    "UPDATE_APP",
                                    updateOnNewVersionMessage,
                                    "DESCARGAR",
                                    "Cerrar",
                                    false);

                            if (Constants.SPLASH_SCREEN_MODE.equalsIgnoreCase("1") || Constants.SPLASH_SCREEN_MODE.equalsIgnoreCase("2"))
                                textViewLoadingApp.setText("Hurrah!");
                            return;
                        }
                    }
                    catch (Exception e) {
                        //PackageManager.NameNotFoundException
                        e.printStackTrace();
                        if (Constants.SPLASH_SCREEN_MODE.equalsIgnoreCase("1") || Constants.SPLASH_SCREEN_MODE.equalsIgnoreCase("2"))
                            textViewLoadingApp.setText("Something fail\nRestart the App :(");
                        return;
                    }
                }
            }
            else {
                if (Constants.SPLASH_SCREEN_MODE.equalsIgnoreCase("1") || Constants.SPLASH_SCREEN_MODE.equalsIgnoreCase("2"))
                    textViewLoadingApp.setText("Ouch!");

                stopFadeAnimation = true;
                utils.displayAlertDialogOneOption(
                        this,
                        this,
                        "DISABLED_APP",
                        appOnDisableMessage,
                        "Aceptar",
                        false);
                return;
            }
            //endregion GET APPLICATION INFORMATION

            //region GET APP NEWS
            if (!appNews) {
                if (Constants.SPLASH_SCREEN_MODE.equalsIgnoreCase("1") || Constants.SPLASH_SCREEN_MODE.equalsIgnoreCase("2"))
                    textViewLoadingApp.setText(getString(R.string.ss_checking_news));

                getAppNewsCheckAvailability();
                return;
            }
            //endregion GET APP NEWS

            //region GET CURRENT USER
            if (!appUser) {
                if (Constants.SPLASH_SCREEN_MODE.equalsIgnoreCase("1") || Constants.SPLASH_SCREEN_MODE.equalsIgnoreCase("2"))
                    textViewLoadingApp.setText(getString(R.string.ss_verifying_user));

                getAppCurrentUser();
                return;
            }
            //endregion GET CURRENT USER
        }
        else startDelay = 400;

        if(!worksOffline && !appConnected) {
            textViewLoadingApp.setText(getString(R.string.ss_not_connected));
            return;
        }

        if (Constants.SPLASH_SCREEN_MODE.equalsIgnoreCase("2")) {
            if (!videoSplashScreenDone) return;
        }

        if (Constants.SPLASH_SCREEN_MODE.equalsIgnoreCase("1") || Constants.SPLASH_SCREEN_MODE.equalsIgnoreCase("2"))
            textViewLoadingApp.setText(getString(R.string.ss_loading_content));

        stopFadeAnimation = true;

        //Revisamos las politicas de privacidad
        //region PRIVACY POLICY
        if(!settings.getPrivacyPolicyStatus()){

            if(privacyPolicy.isEmpty()){
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    //getResources()
                    textViewPPContent.setText(Html.fromHtml(getResources().getString(R.string.privacy_policy_local_content), Html.FROM_HTML_MODE_LEGACY));
                } else {
                    textViewPPContent.setText(Html.fromHtml(getResources().getString(R.string.privacy_policy_local_content)));
                }
            }
            else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    textViewPPContent.setText(Html.fromHtml(privacyPolicy, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    textViewPPContent.setText(Html.fromHtml(privacyPolicy));
                }
            }

            includeLayoutPrivacyPolicy.setVisibility(View.VISIBLE);
            return;
        }
        else if(includeLayoutPrivacyPolicy.getVisibility() == View.VISIBLE) return;
        //endregion PRIVACY POLICY

        //Primera vez que verificamos los permisos!
        //region APP PERMISSIONS
        settings.setPermissionStatus(false); // establecemos falsos, vamos a revisar si alguno no esta aprobado aun
        getInitialAppPermissions();
        //endregion APP PERMISSIONS
    }


    private void cancelAllEventListeners() {
        if(databaseRefAppInfo != null && vEventListenerAppInfo != null)databaseRefAppInfo.removeEventListener(vEventListenerAppInfo); //getAppInformation();
        if(drNewsCheck != null && velNewsCheck != null) drNewsCheck.removeEventListener(velNewsCheck); //getAppNewsCheckAvailability();
        if(queryNewsByCountry != null && velNewsByCountry != null) queryNewsByCountry.removeEventListener(velNewsByCountry); //getAppNewsByCountry();

       //fetch Sign In Methods, we should find a way to cancel this in getAppCurrentUser();
        if(fetch != null) fetch = null;
        if(drCurrentUserDBVerification != null && velCurrentUserDBVerification != null) drCurrentUserDBVerification.removeEventListener(velCurrentUserDBVerification); //getAppCurrentUser();

    }

    //endregion

    //region REQUEST PERMISSIONS

    long startDelay = 0;
    AlertDialog builder;

    private void showPermissionDialog(){

        textViewLoadingApp.setText(getString(R.string.ss_confirm_permissions_1));

        if(builder != null){
            builder.dismiss();
            builder = null;
        }

        builder = new AlertDialog.Builder(this).create();
        builder.setTitle("Permisos");

        if(privacyPolicyURL.isEmpty()){
            builder.setMessage(getString(R.string.permission_request_01));
        }
        else {
            builder.setMessage(getString(R.string.permission_request_02));

            builder.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.privacy_policy_title), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyURL));
                    startActivity(browserIntent);
                    privacyPolicyURL = "";
                    getInitialAppPermissions();
                }
            });
        }

        builder.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.text_continue), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Prefs.putString(device, "DONE");
                settings.setPermissionStatus(true);
                getInitialAppPermissions();
            }
        });

        builder.setCancelable(false);
        builder.show();

        //endregion
    }

    private void getInitialAppPermissions() {

        //region SYSTEM ALERT WINDOW
/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED) {
            if(!settings.getPermissionStatus()){
                showPermissionDialog();
                return;
            }

            startDelay = 1500;

            requestPermissionSystemAlertWindow();
            return;
        }
*/
        //endregion SYSTEM ALERT WINDOW

        //region READ_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if(!settings.getPermissionStatus()){
                showPermissionDialog();
                return;
            }


            startDelay = 1500;
            // Check in manifest:  <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
            requestPermissionReadExternalStorage();
            return;
        }
        //endregion READ_EXTERNAL_STORAGE

        //region WRITE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if(!settings.getPermissionStatus()){
                showPermissionDialog();
                return;
            }


            startDelay = 1500;
            // Check in manifest:  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
            requestPermissionWriteExternalStorage();
            return;
        }
        //endregion WRITE_EXTERNAL_STORAGE

        //Terminamos la verificacion de permisos.. t0d0 en orden! dejamos la variable en true (si habian permisos) la proxima vez la volvemos a poner falso.

        if (startDelay > 0){
            textViewLoadingApp.setText(getString(R.string.ss_starting));
            textViewExtraInfo.setText("");
        }

        new Handler().postDelayed(this::startMainApp,startDelay);

    }

    final int CODE_SYSTEM_ALERT_WINDOW = 123;
    final int CODE_READ_EXTERNAL_STORAGE = 321;
    final int CODE_WRITE_EXTERNAL_STORAGE = 357;

    boolean STATUS_SYSTEM_ALERT_WINDOW = true;
    boolean STATUS_READ_EXTERNAL_STORAGE = true;
    boolean STATUS_WRITE_EXTERNAL_STORAGE = true;

    //region SYSTEM_ALERT_WINDOW

    private void requestPermissionSystemAlertWindow() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SYSTEM_ALERT_WINDOW)){

            new AlertDialog.Builder(this).setTitle(getString(R.string.permission_system_alert_windows)).setMessage(getString(R.string.permission_system_alert_windows_explanation))
                    .setPositiveButton(getString(R.string.permission_grant), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(SplashScreen.this, new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW}, CODE_SYSTEM_ALERT_WINDOW);
                        }
                    })
                    .create()
                    .show();

        }
        else {
            ActivityCompat.requestPermissions(SplashScreen.this, new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW}, CODE_SYSTEM_ALERT_WINDOW);
        }


    }
    //endregion SYSTEM_ALERT_WINDOW

    // region READ_EXTERNAL_STORAGE

    private void requestPermissionReadExternalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    new AlertDialog.Builder(this).setTitle(R.string.permission_read_external_title).setMessage(getString(R.string.permission_read_external_explanation))
                            .setPositiveButton(getString(R.string.permission_grant), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(SplashScreen.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CODE_READ_EXTERNAL_STORAGE);
                                }
                            })
                            .create()
                            .show();

                } else {
                    ActivityCompat.requestPermissions(SplashScreen.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CODE_READ_EXTERNAL_STORAGE);
                }

            }
        }
        else getInitialAppPermissions();

    }
    //endregion READ_EXTERNAL_STORAGE

    // region WRITE_EXTERNAL_STORAGE

    private void requestPermissionWriteExternalStorage() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){

                new AlertDialog.Builder(this).setTitle(getString(R.string.permission_write_external_title)).setMessage(getString(R.string.permission_write_external_explanation))
                        .setPositiveButton(getString(R.string.permission_grant), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(SplashScreen.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_WRITE_EXTERNAL_STORAGE);
                            }
                        })
                        .create()
                        .show();

            }
            else {
                ActivityCompat.requestPermissions(SplashScreen.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_WRITE_EXTERNAL_STORAGE);
            }
        }
    }
    //endregion WRITE_EXTERNAL_STORAGE

    @SuppressLint("SetTextI18n")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CODE_SYSTEM_ALERT_WINDOW:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED){
                        //WE NOW HAVE THE PERSMISSIONS TO SHOW LAYOUTS IN THE PHONE SCREEN
                    }
                    textViewExtraInfo.setText("");
                    textViewLoadingApp.setText(getString(R.string.ss_confirm_permissions_2));
                }
                else if(STATUS_SYSTEM_ALERT_WINDOW) {
                    textViewExtraInfo.setText(getString(R.string.permission_system_alert_window));
                    textViewLoadingApp.setText(getString(R.string.ss_confirm_permissions_3));
                    STATUS_SYSTEM_ALERT_WINDOW = false;
                }
                getInitialAppPermissions();
                break;
            }
            case CODE_READ_EXTERNAL_STORAGE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        //WE NOW HAVE THE PERSMISSIONS TO USE MAP LOCATION!
                    }
                    textViewExtraInfo.setText("");
                    textViewLoadingApp.setText(getString(R.string.ss_confirm_permissions_2));
                }
                else if (STATUS_READ_EXTERNAL_STORAGE) {
                    textViewExtraInfo.setText(getString(R.string.permission_read_external));
                    textViewLoadingApp.setText(getString(R.string.ss_confirm_permissions_3));
                    STATUS_READ_EXTERNAL_STORAGE = false;
                }
                getInitialAppPermissions();
                break;
            }
            case CODE_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        //WE NOW HAVE THE PERSMISSIONS TO USE MAP LOCATION!
                    }
                    textViewExtraInfo.setText("");
                    textViewLoadingApp.setText(getString(R.string.ss_confirm_permissions_2));
                } else if (STATUS_WRITE_EXTERNAL_STORAGE) {
                    textViewExtraInfo.setText(getString(R.string.permission_write_external));
                    textViewLoadingApp.setText(getString(R.string.ss_confirm_permissions_3));
                    STATUS_WRITE_EXTERNAL_STORAGE = false;
                }
                getInitialAppPermissions();
                break;
            }
        }
    }

    //endregion

    boolean started = false;

    private void startMainApp () {
        // Single call to this method
        if(started) return;
        started = true;

        Intent intent = new Intent(SplashScreen.this, WelcomeActivity.class); //send to login;
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

        if(auth == null){
            //worksOffline
           // if(worksOffline) intent = new Intent(SplashScreen.this, PrevWaterActivity.class);
            if(worksOffline) intent = new Intent(SplashScreen.this, WelcomeActivity.class);
        }
        else if (auth.getCurrentUser() != null){

            cUser = userManager.getCustomerData();

            if (cUser.getCurrentDevice().equalsIgnoreCase(utils.getDeviceName(SplashScreen.this))) {
                //AUTENTICADO
                //intent = new Intent(SplashScreen.this, PrevWaterActivity.class);
                intent = new Intent(SplashScreen.this, WelcomeActivity.class);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
            else if (!cUser.getCurrentDevice().equalsIgnoreCase(utils.getDeviceName(SplashScreen.this))){
                Toast.makeText(this, "Sesion activa en otro dispositivo!", Toast.LENGTH_SHORT).show();
                auth.signOut();
                userManager.removeCustomerData();
            }
        }
        else {
            // Without user
            auth.signOut();
            userManager.removeCustomerData();
        }

        startActivity(intent);

        Log.i("APPLICATION", "SPLASH SCREEN ACTIVITY DONE!");
        finish();
    }

    //region TODO: CUSTOM CALLBACK

    @Override
    public void onReturnCallback(boolean status, String message, int code) {}

    @Override
    public void onYesResponse(String code, String message) {
        if (code.equalsIgnoreCase("UPDATE_APP")){
            //go to playstore
            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
            System.exit(0);
        }
        else if (code.equalsIgnoreCase("DISABLED_APP")){
            //close app
            System.exit(0);
        }
    }

    @Override
    public void onNoResponse(String code, String message) {
        if (code.equalsIgnoreCase("UPDATE_APP")){
            //close app
            System.exit(0);
        }
    }
 
    //endregion

    //region FIREBASE CONNECTION STATUS

    Handler setApp = null;
    Runnable run = this::setAppInformation;

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        // La primera vez devuelve FALSE. Si esta conectado a internet primero regresa FALSE Y despues TRUE (conectado)
        try{
            boolean connected = dataSnapshot.getValue(Boolean.class);
            if(connected) {
                Log.i(TAG_CONNECTED, "ESTA CONECTADO A INTERNET");
                appConnected = true;
            }
            else {
                //OFFLINE
                Log.i(TAG_CONNECTED, "SE DESCONECTO EL INTERNET");
                appConnected = false;
            }
        }
        catch (Exception ignore){
            //OFFLINE
            Log.i(TAG_CONNECTED, "SE DESCONECTO EL INTERNET por exception");
            appConnected = false;
        }

        if(setApp != null) setApp.removeCallbacks(run);
        setApp = new Handler();
        setApp.postDelayed(run,1000);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        // do nothing here
    }
    //endregion FIREBASE CONNECTION STATUS

}
