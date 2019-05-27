package com.eahm.drinkwaterapp.BroadcastReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.eahm.drinkwaterapp.Utils.Utils;

public class NotificationReceiver extends BroadcastReceiver {

    //region VARIABLES
    Context context;
    AlarmManager alarmManager;
    Intent alarmIntent;
    PendingIntent alarmPendingIntent;
    static int ALARM_REQUEST_CODE = 159;
    Utils utils = new Utils();

    String actionName = "";
    int actionCode = 0;

    //endregion VARIABLES

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        Bundle extras = intent.getExtras();

        if(extras == null){
            //Toast.makeText(context, "No se registro tu accion :(", Toast.LENGTH_SHORT).show();
            return;
        }

        actionName = intent.getAction();
        actionCode = extras.getInt("ACTION",0);

        int notificationID = extras.getInt("NOTI_ID");

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notificationID);

        int nextTime = 20;
        String code = ";";
        switch (actionCode){
            case 1:
                nextTime = 5;
                code = "SKIPPED";
                //Toast.makeText(context, "Saltar: Recordar en 5 minutos", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                nextTime = 50;
                //Toast.makeText(context, "Beber: Proximo recordatorio en 50 mins", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                nextTime = 10;
                code = "CLOSED";
                //Toast.makeText(context, "Cerrado: Intentar de nuevo en 10 mins", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

        //nextTime = 2; //Testing

        updateAlarm(context, nextTime, code);

        Toast.makeText(context, actionName + ": Proxima alarma en " + nextTime + " mins", Toast.LENGTH_LONG).show();

    }

    private void initAlarm(Context context){
        alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmPendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, alarmIntent, 0);
    }

    private void updateAlarm(Context context, int minutes, String CODE) {
        //MANIFEST: <receiver android:name=".AndroidActionsReceiver" />

        //Tomamos agua, crear otra alarma dentro de.. 45 min por ahora (hay que calcular aqui segun mi META)

        initAlarm(context);

        if(utils.existAlarm(context, ALARM_REQUEST_CODE, alarmIntent)) {
            //Cancel
            alarmManager.cancel(alarmPendingIntent);
            alarmPendingIntent.cancel();
        }

        long time = SystemClock.elapsedRealtime() + (minutes * 60 * 1000); // cada X minutos

        initAlarm(context);

        alarmIntent.setAction("ON_NOTIFICATION_CLICKED");
        //alarmIntent.putExtra("NOTI_ID", NOTIFICATION_ID);
        alarmIntent.putExtra("CODE", CODE);

        //Wake up the device to fire a one-time (non-repeating) alarm in one minute:
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, time, alarmPendingIntent);

        //Toast.makeText(context, "Proxima alarma en " + minutes + " mins", Toast.LENGTH_SHORT).show();

    }

}
