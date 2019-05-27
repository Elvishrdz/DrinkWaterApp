package com.eahm.drinkwaterapp;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    //region VARIABLES
    TextView textViewUserName;
    TextView textViewInfo;
    ImageView imageViewProfile;
    Button buttonLogOut;


    private static final String LOG_SPEECH_TEXT = "LOG_SPEECH_TEXT";
    private static String LOG_RECOGNITION_LISTENER = "LOG_RECOGNITION_LISTENER";

    private FirebaseAuth mAuth;

    private static final int CODE_SPEECH_INPUT = 13;
    private Spinner spinnerLanguage;
    private Switch switchSpeechView;
    private TextView textViewSpeechText;
    private Button buttonSpeech;
    //endregion

    //region SPEECH VAR
    String speechLanguage = Locale.getDefault().getLanguage();
    boolean isChecked = false;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;

    Locale speechLocaleLanguage = Locale.getDefault();

    private TextToSpeech textToSpeech;
    private boolean isInitialized = false;
    Locale currentLocale = null;

    //endregion SPEECH VAR

    //region ANDROID NATIVE METHODS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        mAuth = FirebaseAuth.getInstance();

        textViewUserName = findViewById(R.id.textViewUserName);
        textViewInfo = findViewById(R.id.textViewInfo);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        imageViewProfile = findViewById(R.id.imageViewProfile);
        buttonLogOut = findViewById(R.id.buttonLogOut);



        AdView adView = findViewById(R.id.adView);

        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        switchSpeechView = findViewById(R.id.switchSpeechView);
        textViewSpeechText = findViewById(R.id.textViewSpeechText);
        buttonSpeech = findViewById(R.id.buttonSpeech);

        if(mAuth.getCurrentUser() != null){
            textViewUserName.setText(mAuth.getCurrentUser().getDisplayName());
            Glide.with(this)
                    .load(mAuth.getCurrentUser().getPhotoUrl())
                    .into(imageViewProfile);

            buttonLogOut.setOnClickListener(v->{
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();

            });
        }
        else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

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

        //region SPEECH

        if (SpeechRecognizer.isRecognitionAvailable(this)){

            ArrayAdapter<String> languages = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item , new String[] {"Default", "Español (Estados Unidos)","English (United States)", "Deutsch"});
            languages.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerLanguage.setAdapter(languages);
            spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch (position){
                        case 0:
                            speechLanguage = Locale.getDefault().getLanguage();
                            speechLocaleLanguage = Locale.getDefault();
                            break;
                        case 1:
                            speechLanguage = "es_US";
                            speechLocaleLanguage = new Locale("es_US");
                            break;
                        case 2:
                            speechLanguage = "en";
                            speechLocaleLanguage = Locale.ENGLISH;
                            break;
                        case 3:
                            speechLanguage = "de_DE";
                            speechLocaleLanguage = Locale.GERMAN;
                            break;

                        default:
                            speechLanguage = Locale.getDefault().getLanguage();
                            speechLocaleLanguage = Locale.getDefault();
                            break;
                    }
                    Log.i(LOG_RECOGNITION_LISTENER, "Selected Speech Language: " + speechLanguage);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Log.i(LOG_RECOGNITION_LISTENER, "Speech Language: Nothing Selected");
                }
            });

            buttonSpeech.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    initTextToSpeech(speechLocaleLanguage);

                    if(switchSpeechView.isChecked()){
                        startGoogleSpeechRecognition();
                    }
                    else {
                        isChecked = !isChecked;
                        if (isChecked) {
                            buttonSpeech.setText("STOP");
                            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                                initSpeechRecognition();
                            }
                            else ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, CODE_SPEECH_INPUT);
                        } else {
                            //Toast.makeText(MainActivity.this, "Detenido", Toast.LENGTH_SHORT).show();
                            speech.stopListening();
                            buttonSpeech.setText("Talk To Me");
                        }
                    }
                }
            });
        }
        else {
            Toast.makeText(this, "Reconocimiento NO esta disponible!", Toast.LENGTH_SHORT).show();
        }

        //endregion SPEECH

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (speech != null)  speech.destroy();
        if (textToSpeech != null) textToSpeech.shutdown();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speech != null)  speech.destroy();
        if (textToSpeech != null) textToSpeech.shutdown();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CODE_SPEECH_INPUT){

            if(resultCode == RESULT_OK && data != null){
                ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                handleSpeechResults(matches);
            }
            else Toast.makeText(this, "Sin resultados", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CODE_SPEECH_INPUT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(this, "Hablame ahora", Toast.LENGTH_SHORT).show();
                    initSpeechRecognition();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    //endregion ANDROID NATIVE METHODS

    // region RECOGNITION LISTENER
    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.i(LOG_RECOGNITION_LISTENER, "On Ready For Speech");
        Toast.makeText(this, "Ready, Talk Now!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_RECOGNITION_LISTENER, "On Beginning Of Speech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        ///Log.i(LOG_RECOGNITION_LISTENER, "on Rms Changed"); // CALLED MULTIPLE TIMES
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_RECOGNITION_LISTENER, "On Buffer Received");
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_RECOGNITION_LISTENER, "On End Of Speech");
    }

    @Override
    public void onError(int error) {
        Toast.makeText(this, "Intenta de nuevo " + error, Toast.LENGTH_SHORT).show();
        switch (error){
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                Log.i(LOG_RECOGNITION_LISTENER, "On Error: Network Timeout");
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                Log.i(LOG_RECOGNITION_LISTENER, "On Error: Network");
                break;
            case SpeechRecognizer.ERROR_AUDIO:
                Log.i(LOG_RECOGNITION_LISTENER, "On Error: Audio");
                break;
            case SpeechRecognizer.ERROR_SERVER:
                Log.i(LOG_RECOGNITION_LISTENER, "On Error: Server");
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                Log.i(LOG_RECOGNITION_LISTENER, "On Error: Client");
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                Log.i(LOG_RECOGNITION_LISTENER, "On Error: Speech Timeout");
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                Log.i(LOG_RECOGNITION_LISTENER, "On Error: No Match");
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                Log.i(LOG_RECOGNITION_LISTENER, "On Error: Recognizer Busy");
                break;
            default:
                    Log.i(LOG_RECOGNITION_LISTENER, "On Error: " + error );
                    break;
        }
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_RECOGNITION_LISTENER, "On Results");

        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if(matches != null) handleSpeechResults(matches);
        else Toast.makeText(this, "Sin resultados", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.i(LOG_RECOGNITION_LISTENER, "On Ready For Speech");
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.i(LOG_RECOGNITION_LISTENER, "On Ready For Speech");
    }
    //endregion


    private void initSpeechRecognition(){

        if(speech == null){
            speech = SpeechRecognizer.createSpeechRecognizer(this);
            speech.setRecognitionListener(this);
        }

        if(recognizerIntent == null) recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLanguage);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, speechLanguage);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        speech.startListening(recognizerIntent);
    }

    private void initTextToSpeech(final Locale currentLanguage){

        if (isInitialized && (currentLanguage.equals(currentLocale))) return;

        if (textToSpeech != null) textToSpeech.shutdown();

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    isInitialized = true;
                    int voiceLanguage = textToSpeech.setLanguage(currentLanguage);

                    if(voiceLanguage == TextToSpeech.LANG_MISSING_DATA || voiceLanguage == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.i(LOG_SPEECH_TEXT, "Language Missing or not supported: " + voiceLanguage);
                    }
                    else currentLocale = currentLanguage;

                }
                else{
                    isInitialized = false;
                    Toast.makeText(MainActivity.this, "Text To Speech Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void speechTextInitQueue(String text){
        if(textToSpeech != null){
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
        else  Log.i(LOG_SPEECH_TEXT, "Init Queue: Text To Speech is Null");
    }

    private void speechTextAddQueue(String text){
        if(textToSpeech != null){
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
        else  Log.i(LOG_SPEECH_TEXT, "Add Queue: Text To Speech is Null");
    }

    private void handleSpeechResults(ArrayList<String> result) {

        if (result == null){
            Toast.makeText(this, "Empty List", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = "";
        for (String item: result){
            text += item + "\n";
        }

        //textViewSpeechText.setText(text);
        textViewSpeechText.setText(result.get(0));
        speechTextAddQueue(result.get(0));
    }

    private void startGoogleSpeechRecognition() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLanguage);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, speechLanguage);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        //intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi! Talk now, come on, dont be shy :D");

        try{
            startActivityForResult(intent, CODE_SPEECH_INPUT);
        }
        catch (ActivityNotFoundException e){
            Toast.makeText(this, "No se pudo iniciar la funcion de reconocimiento ", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(this, "No se pudo iniciar el reconocimiento " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }




}

