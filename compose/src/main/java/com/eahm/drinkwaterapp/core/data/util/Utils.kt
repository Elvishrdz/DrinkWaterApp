package com.eahm.drinkwaterapp.core.data.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.VideoView
import com.eahm.drinkwaterapp.R
import kotlinx.datetime.toLocalDateTime
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Random
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class Utils {

    fun loadData(context: Context, inFile: String): String {
        var tContents = ""

        try {
            val stream = context.getAssets().open(inFile)

            val size = stream.available()
            val buffer = ByteArray(size)
            stream.read(buffer)
            stream.close()
            tContents = String(buffer)
        } catch (e: IOException) {
            // Handle exceptions here
            tContents = "Fail: " + e.toString()
        }

        return tContents
    }

    fun getDeviceName(context: Context): String {
        //https://developer.android.com/training/articles/user-data-ids.html
        //String uniqueID = UUID.randomUUID().toString();
        @SuppressLint("HardwareIds") var android_id =
            Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
        if (android_id == null) android_id = "117"
        return (Build.MANUFACTURER
                + "_" + Build.MODEL + "_" + Build.VERSION.RELEASE
                + "_" + Build.VERSION_CODES::class.java.getFields()[Build.VERSION.SDK_INT].getName()
                + "_" + android_id)
    }

    fun validateEmail(email: String, context: Context): String {
        if (email.isEmpty()) {
            return context.getString(R.string.validate_email_empty)
        } else if (!email.contains("@") || !email.contains(".")) {
            return context.getString(R.string.validate_email_invalid)
        }

        return "TRUE"
    }

    fun validatePassword(password: String, context: Context): String {
        if (password.isEmpty()) {
            return context.getString(R.string.validate_password_empty)
        } else if (password.length < 6) {
            return context.getString(R.string.validate_password_too_short)
        }

        return "TRUE"
    }

    fun validatePhoneNumber(phoneNumber: String, context: Context): String {
        if (phoneNumber.isEmpty()) {
            return context.getString(R.string.validate_phone_empty)
        } else if (phoneNumber.length < 8) {
            return context.getString(R.string.validate_phone_incomplete)
        } else if (phoneNumber.length > 15) {
            return context.getString(R.string.validate_phone_too_long)
        } else if (!phoneNumber.matches("[0-9]+".toRegex())) {
            return context.getString(R.string.validate_phone_only_numbers)
        }

        return "TRUE"
    }

    fun setDimensionToFitScreen(activity: Activity, videoView: VideoView) {
        // Adjust the size of the video
        // so it fits on the screen
        val videoProportion = this.videoProportion
        val screenWidth = activity.getResources().getDisplayMetrics().widthPixels
        val screenHeight = activity.getResources().getDisplayMetrics().heightPixels
        val screenProportion = screenHeight.toFloat() / screenWidth.toFloat()
        val lp: ViewGroup.LayoutParams = videoView.getLayoutParams()

        if (videoProportion < screenProportion) {
            lp.height = screenHeight
            lp.width = (screenHeight.toFloat() / videoProportion).toInt()
        } else {
            lp.width = screenWidth
            lp.height = (screenWidth.toFloat() * videoProportion).toInt()
        }
        videoView.setLayoutParams(lp)
    }

    private val videoProportion: Float
        // This method gets the proportion of the video that you want to display.
        get() = 1.5f

    fun getRandomNumber(min: Int, max: Int): Int {
        //This gives a random integer between max (inclusive) and min (exclusive).
        return (Random()).nextInt((max - min) + 1) + min
    }

    fun getMillis(hour: Int, minute: Int): Long {
        val hoursInMillis = (hour * 60 * 60 * 1000).toLong()
        val secondsInMillis = (minute * 60 * 1000).toLong()

        return hoursInMillis + secondsInMillis
    }

    fun getMillis(allseconds: Int): Long {
        return (allseconds * 1000).toLong()
    }


    fun getMillis(timestamp: String): Long {
        return timestamp.toLong() * 1000
    }

    fun getSeconds(hour: Int, minute: Int): Int {
        return (hour * 3600) + (minute * 60)
    }

    fun getSeconds(timeInMillis: Long): Int {
        return ((timeInMillis / 1000).toInt() % 60)
    }

    fun getMinutes(timeInMillis: Long): Int {
        return ((timeInMillis / (1000 * 60)) % 60).toInt()
    }

    fun getHours(timeInMillis: Long): Int {
        return ((timeInMillis / (1000 * 60 * 60)) % 24).toInt()
    }

    val currentTimestamp: String
        get() = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
            .toString()

    fun convertSecondTo_HHmmss_String(totalAmountOfSeconds: Int): String {
        val tz = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat("HH:mm:ss")
        df.setTimeZone(tz)
        return df.format(Date(totalAmountOfSeconds * 1000L))
    }

    fun getTimeString(hourOfDay: Int, minute: Int, seconds: Int, context: Context): String {
        var a = ""

        var hora = context.getResources().getString(R.string.text_hours)
        var minutos = context.getResources().getString(R.string.text_minutes)
        var segundos = context.getResources().getString(R.string.text_seconds)

        if (hourOfDay == 1) hora = context.getResources().getString(R.string.text_hour)
        if (minute == 1) minutos = context.getResources().getString(R.string.text_minute)
        if (seconds == 1) segundos = context.getResources().getString(R.string.text_second)

        if (hourOfDay > 0) {
            a = a + hourOfDay + " " + hora + " "
        }

        if (minute > 0) {
            a = a + minute + " " + minutos + " "
        }

        if (seconds != 0) {
            a = a + seconds + " " + segundos + " "
        }

        return a
    }


    fun getTimeByPos(allSeconds: Int, position: Int): Int {
        val result = convertSecondTo_HHmmss_String(allSeconds)

        val parts: Array<String?> =
            result.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (position == 0) return parts[0]!!.toInt()
        if (position == 1) return parts[1]!!.toInt()
        if (position == 2) return parts[2]!!.toInt()

        return -7
    }

    fun getFormattedTimeAMPM(
        hourOfDay: Int,
        minute: Int,
        seconds: Int,
        AMPM: Boolean,
        useSeconds: Boolean
    ): String {
        /*long allSeconds = Long.parseLong( dataset.get(position));
        String stringTime = utils.getHours(allSeconds) + ":" + utils.getMinutes(allSeconds) + "." + utils.getSeconds(allSeconds);*/

        /* long millis = Long.parseLong(timestamp) * 1000L;

        int hourOfDay = getHours(millis);
        int minute = getMinutes(millis);
        int seconds = getSeconds(millis);*/

        val ZERO = "0"

        //Formateo el hora obtenido: antepone el 0 si son menores de 10
        var formattedHour =
            if (hourOfDay < 10) (ZERO + hourOfDay).toString() else hourOfDay.toString()

        //Formateo el minuto obtenido: antepone el 0 si son menores de 10
        val formattedMinute = if (minute < 10) (ZERO + minute).toString() else minute.toString()

        //Obtengo el valor a.m. o p.m., dependiendo de la selección del usuario
        val AM_PM: String?
        if (hourOfDay < 12) {
            if (hourOfDay == 0) formattedHour = "12"
            AM_PM = "a.m."
        } else {
            formattedHour = (hourOfDay - 12).toString()
            AM_PM = "p.m."
        }
        //Muestro la hora con el formato deseado
        var formattedTime = formattedHour + ":" + formattedMinute + " " + AM_PM

        if (useSeconds) {
            formattedTime = formattedHour + ":" + formattedMinute + " " + seconds + " " + AM_PM
        }

        return formattedTime
    }

    fun createCustomTitle(
        context: Context?,
        title: String?,
        textColor: Int,
        backgroundColor: Int,
        textSizeDP: Int
    ): View {
        // Create a TextView programmatically
        val tv = TextView(context)
        val lp: ViewGroup.LayoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,  // Width of TextView
            ViewGroup.LayoutParams.WRAP_CONTENT
        ) // Height of TextView
        tv.setLayoutParams(lp)
        tv.setPadding(10, 10, 10, 10)
        tv.setGravity(Gravity.CENTER)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeDP.toFloat())
        tv.setText(title)
        tv.setTextColor(textColor)
        tv.setBackgroundColor(backgroundColor)

        return tv
    }

    @OptIn(ExperimentalTime::class)
    fun getTodayString(context: Context): String {
        val now = Clock.System.now()
        val timeZone = kotlinx.datetime.TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(timeZone)

        val dayName = getDayName(today.dayOfWeek.ordinal, context)
        val dayOfMonth = today.day
        val ofText = context.resources.getString(R.string.text_of)

        val monthName = getMonthName(today.month.ordinal, context)
        val year = today.year

        return "$dayName $dayOfMonth $ofText $monthName $year"
    }

    fun getFinalProgressString(useAbbreviations: Boolean, context: Context): String {
        // TODO: val myProgressMl: Float = SettingsClass().getTodayProgress()
        val myProgressMl: Float = 0f
        val inLiters = myProgressMl / 1000
        val myFinalProgress: String

        var metricMililiters = context.getResources().getString(R.string.text_mililiters)
        var metricLiters = context.getResources().getString(R.string.text_liters)

        if (useAbbreviations) {
            metricMililiters = context.getResources().getString(R.string.text_vol_unit_mililiters)
            metricLiters = context.getResources().getString(R.string.text_vol_unit_liters)
        }

        if (inLiters > 0.9) {
            if (!useAbbreviations) metricLiters = " " + metricLiters

            if (inLiters % 1 == 0f) {
                val integer = inLiters.toLong()
                myFinalProgress = integer.toString() + metricLiters
            } else {
                myFinalProgress = inLiters.toString() + metricLiters
            }
        } else {
            if (!useAbbreviations) metricMililiters = " " + metricMililiters

            if (myProgressMl % 1 == 0f) {
                val integer = myProgressMl.toLong()
                myFinalProgress = integer.toString() + metricMililiters
            } else {
                myFinalProgress = myProgressMl.toString() + metricMililiters
            }
        }

        return myFinalProgress
    }

    fun getGoalString(useAbbreviations: Boolean, context: Context): String {
        // TODO: val myGoalMl: Float = SettingsClass().getGoalValue()
        val myGoalMl: Float = 1000f
        val inLiters = myGoalMl / 1000
        val myFinalGoal: String

        var metricMililiters = context.getResources().getString(R.string.text_mililiters)
        var metricLiters = context.getResources().getString(R.string.text_liters)

        if (useAbbreviations) {
            metricMililiters = context.getResources().getString(R.string.text_vol_unit_mililiters)
            metricLiters = context.getResources().getString(R.string.text_vol_unit_liters)
        }

        if (inLiters > 0.9) {
            if (!useAbbreviations) metricLiters = " " + metricLiters

            if (inLiters % 1 == 0f) {
                val integer = inLiters.toLong()
                myFinalGoal = integer.toString() + metricLiters
            } else {
                myFinalGoal = inLiters.toString() + metricLiters
            }
        } else {
            if (!useAbbreviations) metricMililiters = " " + metricMililiters

            if (myGoalMl % 1 == 0f) {
                val integer = myGoalMl.toLong()
                myFinalGoal = integer.toString() + metricMililiters
            } else {
                myFinalGoal = myGoalMl.toString() + metricMililiters
            }
        }

        return myFinalGoal
    }

    companion object {
        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.getCurrentFocus()
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
        }

        fun getDayName(day: Int, context: Context): String {
            return when (day) {
                1 -> context.getString(R.string.monday)
                2 -> context.getString(R.string.tuesday)
                3 -> context.getString(R.string.wednesday)
                4 -> context.getString(R.string.thursday)
                5 -> context.getString(R.string.friday)
                6 -> context.getString(R.string.saturday)
                7 -> context.getString(R.string.sunday)
                else -> "DAY_NOT_FOUNDED"
            }
        }

        fun getMonthName(month: Int, context: Context): String {
            when (month) {
                1 -> return context.getString(R.string.january)
                2 -> return context.getString(R.string.fabruary)
                3 -> return context.getString(R.string.march)
                4 -> return context.getString(R.string.april)
                5 -> return context.getString(R.string.may)
                6 -> return context.getString(R.string.june)
                7 -> return context.getString(R.string.july)
                8 -> return context.getString(R.string.august)
                9 -> return context.getString(R.string.september)
                10 -> return context.getString(R.string.october)
                11 -> return context.getString(R.string.november)
                12 -> return context.getString(R.string.december)
                else -> return "DAY_NOT_FOUNDED"
            }
        }

        fun getUserCountry(context: Context): String? {
            try {
                val tm: TelephonyManager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val simCountry: String? = tm.getSimCountryIso()
                if (simCountry != null && simCountry.length == 2) { // SIM country code is available
                    return simCountry.lowercase()
                } else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                    val networkCountry: String? = tm.getNetworkCountryIso()
                    if (networkCountry != null && networkCountry.length == 2) { // network country code is available
                        return networkCountry.lowercase()
                    }
                }
            } catch (e: Exception) {
            }
            return null
        }

        fun convertFirstLetterToUpperCase(str: String): String {
            // Create a char array of given String

            val ch: CharArray? = str.toCharArray()
            for (i in 0..<str.length) {
                // If first character of a word is found

                if (i == 0 && ch!![i] != ' ' ||
                    ch!![i] != ' ' && ch[i - 1] == ' '
                ) {
                    // If it is in lower-case

                    if (ch[i] >= 'a' && ch[i] <= 'z') {
                        // Convert into Upper-case

                        ch[i] = (ch[i].code - 'a'.code + 'A'.code).toChar()
                    }
                } else if (ch[i] >= 'A' && ch[i] <= 'Z')  // Convert into Lower-Case
                    ch[i] = (ch[i].code + 'a'.code - 'A'.code).toChar()
            }

            // Convert the char array to equivalent String
            val st = kotlin.text.String(ch!!)
            return st
        }


        fun convertDpToPx(dp: Float, context: Context): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
            ).toInt()
        }

        fun convertSpToPx(sp: Float, context: Context): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                context.getResources().getDisplayMetrics()
            ).toInt()
        }

        fun getDeviceHeight(context: Context): Int {
            val wm: WindowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val mDisplayMetrics: DisplayMetrics = DisplayMetrics()
            wm.getDefaultDisplay().getMetrics(mDisplayMetrics)
            return mDisplayMetrics.heightPixels
        }

        fun getDeviceWidth(context: Context): Int {
            val wm: WindowManager =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val mDisplayMetrics: DisplayMetrics = DisplayMetrics()
            wm.getDefaultDisplay().getMetrics(mDisplayMetrics)
            return mDisplayMetrics.widthPixels
        }

        fun isDateToday(milliSeconds: Long): Boolean {
            val calendar = Calendar.getInstance()
            calendar.setTimeInMillis(milliSeconds)

            val getDate = calendar.getTime()

            calendar.setTimeInMillis(System.currentTimeMillis())
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            val today = calendar.getTime()


            /*
        int result = getDate.compareTo(today);
        if (getDate.compareTo(today) > 0) {
            Log.i("TAG_NOTI", "Date1 is after Date2");
        } else if (getDate.compareTo(today) < 0) {
            Log.i("TAG_NOTI", "Date1 is before Date2");
        } else if (getDate.compareTo(today) == 0) {
            Log.i("TAG_NOTI", "Date1 is equal to Date2");
        }

        Log.i("TAG_NOTI", "-- " + milliSeconds + " / Recibido: " + getDate.toString() + " - Hoy: " + today.toString() + " -- result: " + result);
*/
            return getDate.compareTo(today) > 0
        }
    }
}
