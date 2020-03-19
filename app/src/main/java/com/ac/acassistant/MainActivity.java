package com.ac.acassistant;

//<----Package Import Definition--->
import android.Manifest;

import android.content.ActivityNotFoundException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Camera;


import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;


import org.json.*;

import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
//<----Package Import Definition--->

//Declare Class
public class MainActivity extends AppCompatActivity {

    //Define static vars
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private static final int MY_PERMISSIONS_REQUEST_CALL = 0;

    public static Locale spanish = new Locale("es", "ES");
    //Define Message Adapter + ListView
    private MessageAdapter messageAdapter;
    private ListView messagesView;

    //We set the Locale of the App to the device selected Locale
    Locale defaultLocale = Locale.getDefault();

    //TTS Class definition
    TextToSpeech t1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Checks if is first application start.
        welcomeMenu();


        // Ask For Permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED
                ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an expanation to the user *asynchronously* -- don't block. Work In Progress


            } else {

                //Request the permissions

                //Camera PERMISSION
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                //Call PERMISSION
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_CALL);
                //Contacts PERMISSION
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_CALL);

            }
        }



        //We construct the TTS Class
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    //Set locale to device locale
                    t1.setLanguage(defaultLocale);
                }
            }
        });
        //We define the UI Textbox
        TextView text = findViewById(R.id.TextBoxSend);

        //We contruct the MessageAdapter
        messageAdapter = new MessageAdapter(this);
        messagesView = findViewById(R.id.messages_view);

        //Set adapter messageView
        messagesView.setAdapter(messageAdapter);

        //We listen for text change in the textBox
        text.addTextChangedListener(textWatcher);




    }




    //Listen when user types in textBox
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

            //If user writed in textBox we change mic button to send button

            TextView text = findViewById(R.id.TextBoxSend);
            ImageButton MicButton = findViewById(R.id.MicButton);
            ImageButton SendButton = findViewById(R.id.SendButton);
            if(text.getText().toString().equals("")){
                MicButton.setVisibility(View.VISIBLE);
                SendButton.setVisibility(View.GONE);
            } else{
                MicButton.setVisibility(View.GONE);
                SendButton.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    //If is First Login we change open WelcomeActivity
    public void welcomeMenu(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isFirstLogin = sharedPreferences.getBoolean("isFirstLogin",true);
        if(isFirstLogin){
            Intent myIntent = new Intent(MainActivity.this, welcomeScreen.class);
            MainActivity.this.startActivity(myIntent);
        }
    }

    //Start settingsActivity
    public void settingsOpen(View v){
        Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
        MainActivity.this.startActivity(myIntent);
    }


    //Changes between input types  (MIC, KEYBOARD)
    public void inputMethod (View view){
        final LinearLayout TextInput = findViewById(R.id.layout_chatbox);
        final ConstraintLayout VocieInput = findViewById(R.id.MicLayout);
        if(TextInput.getVisibility() == View.INVISIBLE){
            VocieInput.setVisibility(View.INVISIBLE);
            TextInput.setVisibility(View.VISIBLE);
        }else{
            VocieInput.setVisibility(View.VISIBLE);
            TextInput.setVisibility(View.INVISIBLE);
        }
    }
    //Start STT Engine
    public void startVoiceInput(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, spanish
                );
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "¡Hola! ¿Como puedo ayudarte?");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            a.printStackTrace();
        }
    }

    //STT Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final TextView label = findViewById(R.id.TextBoxSend);
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    label.setText(result.get(0));
                    sendMessage(null);
                }
                break;
            }

        }
    }
    // Use TTS engine to speak the passed string argument
    public void speak (String toSpeak){
        t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void MicInput(View view){
        inputMethod(null);
        startVoiceInput(null);
    }









    //Function that finds a package by a string name
    public String getPackNameByAppName(String name) {
        PackageManager pm = this.getPackageManager();
        List<ApplicationInfo> l = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        String packName = "";
        for (ApplicationInfo ai : l) {
            String n = (String)pm.getApplicationLabel(ai);
            if (n.contains(name) || name.contains(n)){
                packName = ai.packageName;
            }
        }

        return packName;
    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        RequestQueue queue = Volley.newRequestQueue(this); //Volley request queue
        final TextView TextBox = findViewById(R.id.TextBoxSend); //Define UI textBox
        final String TextBoxText = TextBox.getText().toString(); //Get the TextBox Text
        final MemberData user = new MemberData("Me", "#0099ff"); //Define userData with MemberData Class

        //We define a new message using Message class
        final Message message = new Message(TextBoxText, user, true,false, null);

        //We add the message to the messageAdapter and then update the view
        messageAdapter.add(message);
        messagesView.setSelection(messagesView.getCount() - 1);

        //Empty the textbox
        TextBox.setText("");

    try {
        // POST REQUEST START
        JSONObject jsonBody = new JSONObject();

        //Asign language to Spanish
        jsonBody.put("lang", "es");

        //We add the string that user writed as request
        jsonBody.put("query", TextBoxText);

        //Add a session Id
        jsonBody.put("sessionId", "12345");

        //Convert the json to a string as is a StringRequest
        final String requestBody = jsonBody.toString();
        final String url = "https://api.dialogflow.com/v1/query?v=20150910"; //Set API URL
        //We define the request
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // response
                        try {
                            //We define the Asisstant user with MemberData Class
                            final MemberData assistant = new MemberData("Assys", "#d9d9d9");


                            //We create a new Json object with the API response and iterate through it.
                            JSONObject jsonObj = new JSONObject(response);

                            JSONObject sys = jsonObj.getJSONObject("result");

                            String action = sys.getString("action");

                            //Log the triggered intent in API to console
                            Log.d("action", action);

                            //We determine the type of action that the assistant has to do.

                            //Shows Actual Time
                            if (action.equals("gettime")){
                                Date d=new Date();
                                SimpleDateFormat sdf=new SimpleDateFormat("hh:mm a");
                                String currentDateTimeString = sdf.format(d);
                                JSONObject last = sys.getJSONObject("fulfillment");
                                String returnmesage = last.getString("speech");
                                String FinalText = returnmesage + " " + currentDateTimeString;
                                final Message message = new Message(FinalText, assistant, false, false, null);
                                messageAdapter.add(message);
                                messagesView.setSelection(messagesView.getCount() - 1);
                                speak(returnmesage + " " + currentDateTimeString);

                            //Shows Weather
                            }else if (action.equals("getweather")){
                                JSONObject last = sys.getJSONObject("fulfillment");
                                String returnmesage = last.getString("speech");
                                JSONObject parameters = sys.getJSONObject("parameters");
                                String CityName = parameters.getString("Cities");
                                String Date = parameters.getString("date");
                                Date d=new Date();
                                Date dt=new Date();
                                Calendar c = Calendar.getInstance();
                                c.setTime(dt);
                                c.add(Calendar.DATE, 1);
                                dt = c.getTime();
                                SimpleDateFormat date=new SimpleDateFormat("yyyy-MM-dd");
                                String day1Date = date.format(dt);
                                String currentDate= date.format(d);
                                Log.e("Day1Date", currentDate);
                                if(Date.equals("") || Date.equals(currentDate)){
                                    wheaterMessage(CityName, returnmesage, "actual");
                                }else if(Date.equals(day1Date)){
                                    wheaterMessage(CityName, returnmesage, "1day");
                                }
                            //Show Info about *parameter*
                            }else if(action.equals("wikisearch")){
                                JSONObject parameters = sys.getJSONObject("parameters");
                                String term = parameters.getString("any");
                                wikiSearch(term);
                            //Adds an alarm
                            }else if(action.equals("alarmadd")){

                                JSONObject last = sys.getJSONObject("fulfillment");
                                String returnmesage = last.getString("speech");
                                JSONObject parameters = sys.getJSONObject("parameters");
                                String term = parameters.getString("time");
                                final Message message = new Message(returnmesage, assistant, false, false, null);
                                messageAdapter.add(message);
                                messagesView.setSelection(messagesView.getCount() - 1);
                                speak(returnmesage);
                                final String[] timeArray = term.split(Pattern.quote(":"));
                                Log.e("hola", timeArray[0]);
                                Intent intentClock = new Intent(AlarmClock.ACTION_SET_ALARM);
                                int hours = Integer.parseInt(timeArray[0]);
                                int minutes = Integer.parseInt(timeArray[1]);
                                intentClock.putExtra(AlarmClock.EXTRA_HOUR, hours);
                                intentClock.putExtra(AlarmClock.EXTRA_MINUTES, minutes);
                                intentClock.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
                                startActivity(intentClock);
                            //Adds a temporizer
                            }else if(action.equals("tempadd")) {

                                JSONObject last = sys.getJSONObject("fulfillment");
                                String returnmesage = last.getString("speech");
                                JSONObject parameters = sys.getJSONObject("parameters");
                                JSONObject term = parameters.getJSONObject("duration");
                                int time = term.getInt("amount");
                                String unit = term.getString("unit");
                                final Message message = new Message(returnmesage, assistant, false, false, null);
                                messageAdapter.add(message);
                                messagesView.setSelection(messagesView.getCount() - 1);
                                speak(returnmesage);
                                Intent intentTemp = new Intent(AlarmClock.ACTION_SET_TIMER);
                                int timeConverted;
                                if(unit.equals("hora")){
                                    timeConverted = time * 3600;
                                }
                                else if(unit.equals("min")){
                                    timeConverted = time * 60;
                                }else{
                                    timeConverted = time;
                                }
                                intentTemp.putExtra(AlarmClock.EXTRA_LENGTH, timeConverted);

                                intentTemp.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
                                startActivity(intentTemp);
                            //Starts Flashlight
                            }else if(action.equals("flashlightON")){
                                if(getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
                                    Camera cam = Camera.open();
                                    Camera.Parameters p = cam.getParameters();
                                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                                    cam.setParameters(p);
                                    cam.startPreview();
                                    JSONObject last = sys.getJSONObject("fulfillment");
                                    String returnmesage = last.getString("speech");
                                    final Message message = new Message(returnmesage, assistant, false, false, null);
                                    messageAdapter.add(message);
                                    messagesView.setSelection(messagesView.getCount() - 1);
                                    speak(returnmesage);
                                }
                            }
                            //Stops Flashlight
                            else if(action.equals("flashlightOFF")) {
                                if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                                    Camera cam = Camera.open();
                                    Camera.Parameters p = cam.getParameters();
                                    cam.stopPreview();
                                    cam.release();
                                    JSONObject last = sys.getJSONObject("fulfillment");
                                    String returnmesage = last.getString("speech");
                                    final Message message = new Message(returnmesage, assistant, false, false, null);
                                    messageAdapter.add(message);
                                    messagesView.setSelection(messagesView.getCount() - 1);
                                    speak(returnmesage);
                                }
                            }
                            //Retrieve userName
                            else if(action.equals("userName")) {
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                String name = sharedPreferences.getString("name", "user");
                                JSONObject last = sys.getJSONObject("fulfillment");
                                String returnmesage = last.getString("speech");
                                final Message message = new Message(returnmesage + " " + name, assistant, false, false, null);
                                messageAdapter.add(message);
                                messagesView.setSelection(messagesView.getCount() - 1);
                                speak(returnmesage + " " + name);
                            }
                            //Edits Username
                            else if(action.equals("editName")) {
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                JSONObject parameters = sys.getJSONObject("parameters");
                                String newName = parameters.getString("any");
                                editor.putString("name", newName);
                                editor.apply();
                                JSONObject last = sys.getJSONObject("fulfillment");
                                String returnmesage = last.getString("speech");
                                final Message message = new Message(returnmesage, assistant, false, false, null);
                                messageAdapter.add(message);
                                messagesView.setSelection(messagesView.getCount() - 1);
                                speak(returnmesage);
                            }
                            //Opens another app
                            else if(action.equals("openApp")) {
                                JSONObject parameters = sys.getJSONObject("parameters");
                                String appName = parameters.getString("any");
                                String appPackage = getPackNameByAppName(appName);

                                JSONObject last = sys.getJSONObject("fulfillment");
                                String returnmesage = last.getString("speech");
                                final Message message = new Message(returnmesage, assistant, false, false, null);
                                messageAdapter.add(message);
                                messagesView.setSelection(messagesView.getCount() - 1);
                                speak(returnmesage);

                                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(appPackage);
                                if (launchIntent != null) {
                                    startActivity(launchIntent);//null pointer check in case package name was not found
                                }
                            }
                            //Calls a contact or number
                            else if(action.equals("call")) {
                                JSONObject parameters = sys.getJSONObject("parameters");
                                String callParameter = parameters.getString("persona");


                                JSONObject last = sys.getJSONObject("fulfillment");
                                String returnmesage = last.getString("speech");
                                final Message message = new Message(returnmesage, assistant, false, false, null);
                                messageAdapter.add(message);
                                messagesView.setSelection(messagesView.getCount() - 1);
                                speak(returnmesage);


                                if(callParameter.length() != 9 && !callParameter.matches("[0-9]+")){
                                    String ret = null;
                                    String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'%" + callParameter +"%'";
                                    String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
                                    Cursor c = getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                            projection, selection, null, null);
                                    if (c.moveToFirst()) {
                                        ret = c.getString(0);
                                    }
                                    c.close();
                                    if(ret==null)
                                        ret = "Unsaved";
                                    Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                                    phoneIntent.setData(Uri.parse("tel:"+ret)); // Puede ser neccesitado prefijo de país +34
                                    try {
                                        startActivity(phoneIntent);
                                    } catch (SecurityException e){
                                        Log.e("ERROR", e.toString());
                                    }

                                }else{
                                    Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                                    phoneIntent.setData(Uri.parse("tel:"+callParameter));
                                    try {
                                        startActivity(phoneIntent);
                                    } catch (SecurityException e){
                                        Log.e("ERROR", e.toString());
                                    }

                                }

                            }
                            //Add an event to the calendar
                            else if(action.equals("calendar")) {
                                JSONObject parameters = sys.getJSONObject("parameters");
                                String eventName = parameters.getString("any");
                                String date = parameters.getString("date");


                                String[] formattedDate = date.split("-");

                                int year = Integer.parseInt(formattedDate[0]);
                                int month = Integer.parseInt(formattedDate[1]);
                                int day = Integer.parseInt(formattedDate[2]);


                                Intent intent = new Intent(Intent.ACTION_EDIT);

                                intent.setType("vnd.android.cursor.item/event");

                                Calendar beginTime = Calendar.getInstance();
                                beginTime.set(year, month, day);


                                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis());
                                intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
                                intent.putExtra(CalendarContract.Events.TITLE, eventName);

                                //intent.putExtra(CalendarContract.Events.DESCRIPTION, "This is a sample description");
                                //intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "My Guest House");
                                //intent.putExtra(CalendarContract.Events.RRULE, "FREQ=YEARLY");

                                startActivity(intent);

                                JSONObject last = sys.getJSONObject("fulfillment");
                                String returnmesage = last.getString("speech");
                                final Message message = new Message(returnmesage + day + " del " + month + " del " + year, assistant, false, false, null);
                                messageAdapter.add(message);
                                messagesView.setSelection(messagesView.getCount() - 1);
                                speak(returnmesage + day + " del " + month + " del " + year);
                            }
                            // If no action is triggered we only show a text-voice response
                            else {
                                JSONObject last = sys.getJSONObject("fulfillment");
                                String returnmesage = last.getString("speech");
                                final Message message = new Message(returnmesage, assistant, false, false, null);
                                messageAdapter.add(message);
                                messagesView.setSelection(messagesView.getCount() - 1);
                                speak(returnmesage);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders(){
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("Authorization", "Bearer 7be5d9e9e7e0475b8c9e56b9b53d14f3");
                return params;
            }


            @Override
            public byte[] getBody(){
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }
        };
        //We send the request to the server
        queue.add(postRequest);
        // POST REQUEST FINISH
    }catch (JSONException e) {
        e.printStackTrace();
    }
    }

    //Handle a request to the Wikipedia API
    private void wikiSearch (final String term){
        //Define Assistant MemberData
        final MemberData assistant = new MemberData("Assys", "#d9d9d9");
        final RequestQueue queue = Volley.newRequestQueue(this); //Volley request queue
        final String url = "https://es.wikipedia.org/api/rest_v1/page/summary/" + term; // Set url and parameters

        //Define the request
        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // response
                        try {
                            //We create a json with the response and iterate trough it
                            JSONObject jsonOBJ = new JSONObject(response);
                            String desc = jsonOBJ.getString("extract");
                            //We pick until the first dot.
                            String[] fn = desc.split("\\.");
                            Log.e("var1", fn[0]);
                            //Speaks and show message with response
                            speak(fn[0]);
                            final Message message = new Message(fn[0] + ".", assistant, false, false, null);
                            messageAdapter.add(message);
                            messagesView.setSelection(messagesView.getCount() - 1);
                        } catch(JSONException e){
                            e.printStackTrace();
                        }



                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders(){
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                return params;
            }


        };
        queue.add(postRequest);
        // POST REQUEST FINISH
    }
    //Handle a weather request
    private void wheaterMessage (final String city, final String fulfillment, final String dayPrevision){
        final MemberData assistant = new MemberData("Assys", "#d9d9d9");
        final RequestQueue queue = Volley.newRequestQueue(this); //Volley request queue

        if (dayPrevision.equals("actual")) {
            StringRequest weatherRequest = new StringRequest(Request.Method.GET, "http://api.openweathermap.org/data/2.5/weather?q=" + city +"&APPID=3bbbff0b84e34d64cf6897ea04168fa8&units=metric&lang=es",
                    new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    // response
                    try {
                        JSONObject jsonArr = new JSONObject(response);
                        JSONArray weather = jsonArr.getJSONArray("weather");
                        JSONObject arrayObject = weather.getJSONObject(0);
                        Double ID = arrayObject.getDouble("id");
                        JSONObject wind = jsonArr.getJSONObject("wind");
                        Double windSpeedFinal = wind.getDouble("speed");
                        Double localizedDirection = wind.getDouble("deg");
                        String description = arrayObject.getString("description");
                        String formattedDesc = description.substring(0, 1).toUpperCase() + description.substring(1);
                        JSONObject Temperature = jsonArr.getJSONObject("main");
                        Double Temp = Temperature.getDouble("temp");
                        Double maxTemp = Temperature.getDouble("temp_max");
                        Double minTemp = Temperature.getDouble("temp_min");
                        String cloud = null;
                        String sun = null;
                        Calendar rightNow = Calendar.getInstance();
                        int currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY);
                        if(currentHourIn24Format > 20 || currentHourIn24Format > 0 && currentHourIn24Format < 7)
                        {
                            sun = "false";
                        }
                        else{
                            sun = "true";
                        }
                            if(ID >= 200 && ID <= 700 || ID >= 802 && ID <= 804){
                                cloud = "true";
                            }else{
                                cloud = "false";
                            }

                            //Convert to integrer

                        Log.e("degrees", localizedDirection.toString());
                            // 90º to N, O, E, S
                        String NOEsDirection = null;
                            if(localizedDirection >= 330 || localizedDirection < 30){
                                NOEsDirection = "N";
                            }
                            if(localizedDirection >= 30 && localizedDirection < 60){
                                NOEsDirection = "NE";
                            }
                        if(localizedDirection >= 60 && localizedDirection < 120){
                            NOEsDirection = "E";
                        }
                        if(localizedDirection >= 120 && localizedDirection < 150){
                            NOEsDirection = "SE";
                        }
                        if(localizedDirection >= 150 && localizedDirection < 210){
                            NOEsDirection = "S";
                        }
                        if(localizedDirection >= 210 && localizedDirection < 240){
                            NOEsDirection = "SO";
                        }
                        if(localizedDirection >= 240 && localizedDirection < 300){
                            NOEsDirection = "O";
                        }
                        if(localizedDirection >= 300 && localizedDirection < 330){
                            NOEsDirection = "NO";
                        }

                            final String nose = String.format("%.0f", Temp);
                            final String formattedSpeed = String.format("%.0f", windSpeedFinal * 3.6);
                            final String FinalText = "Ahora mismo hace una temperatura de " + nose + " grados y hay " + description.toLowerCase() + ".";
                            final String[] temps = {nose, maxTemp.toString(), minTemp.toString(), cloud, sun, NOEsDirection, formattedSpeed + " " + "km/h"};
                            final Message message = new Message(formattedDesc, assistant, false, true, temps);
                            speak(FinalText);
                            messageAdapter.add(message);
                            messagesView.setSelection(messagesView.getCount() - 1);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("1", "YES");
                                // As of f605da3 the following should work

                                }

                        }
                        ) {
                            @Override
                            public Map<String, String> getHeaders() {
                                Map<String, String> params = new HashMap<>();
                                params.put("Content-Type", "application/json; charset=UTF-8");
                                return params;
                            }


                        };
                        queue.add(weatherRequest);
                        }else if (dayPrevision.equals("1day")) {
                            final Message message = new Message("Está función no está disponible, pero estamos trabajando para que lo esté", assistant, false, false, null);
                            speak("Está función no está disponible, pero estamos trabajando para que lo esté");
                            messageAdapter.add(message);
                            messagesView.setSelection(messagesView.getCount() - 1);
                            /*554
                            StringRequest weatherRequest = new StringRequest(Request.Method.GET, "http://api.openweathermap.org/data/2.5/forecast?q="+ city +"&APPID=3bbbff0b84e34d64cf6897ea04168fa8&units=metric&lang=es",
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {


                                            // response
                                            try {

                                                JSONObject jsonObj = new JSONObject(response);


                                                JSONArray list = jsonObj.getJSONArray("list");


                                                JSONObject zroObject = list.getJSONObject(0);
                                                String actualDate = zroObject.getString("dt_txt");
                                                String[] split1 = actualDate.split(" ");
                                                String finalDate = split1[1].split(":")[0];
                                                int numHour = Integer.parseInt(finalDate);
                                                int arrayNum = 0;

                                                for(int num = 1; num >= 8; ++num) {
                                                    int sumNum = 3;
                                                    if (numHour - 24 == sumNum) {
                                                        arrayNum = num;
                                                    }
                                                    sumNum = sumNum + 3;
                                                }

                                                for(int x = 0; x >= 6; ++x){

                                                }


                                                JSONArray tempArr = jsonObj.getJSONArray("DailyForecasts");
                                                JSONObject oBJ = tempArr.getJSONObject(0);
                                                JSONObject wind = oBJ.getJSONObject("Wind");
                                                JSONObject windSpeed = wind.getJSONObject("Speed");
                                                JSONObject windSpeedMetric = windSpeed.getJSONObject("Metric");
                                                String windSpeedFinal = windSpeedMetric.getString("Value");
                                                String windUnit = windSpeedMetric.getString("Unit");
                                                JSONObject windDirection = wind.getJSONObject("Direction");
                                                String localizedDirection = windDirection.getString("Localized");
                                                JSONObject day = oBJ.getJSONObject("Day");
                                                String description = day.getString("IconPhrase");
                                                String icon = day.getString("Icon");
                                                JSONObject temperature = oBJ.getJSONObject("Temperature");
                                                JSONObject Maximum = temperature.getJSONObject("Maximum");
                                                JSONObject Minimum = temperature.getJSONObject("Minimum");
                                                String MinTemp = Minimum.getString("Value");
                                                String MaxTemp = Maximum.getString("Value");
                                                //Convert to integrer
                                                int iconId = Integer.parseInt(icon);
                                                String cloud = null;
                                                if(iconId == 3 || iconId == 6 || iconId == 4 || iconId == 5 || iconId == 14 || iconId == 17 || iconId == 21 || iconId == 35 || iconId == 36 || iconId == 37|| iconId == 38|| iconId == 39 || iconId == 41){
                                                    cloud = "true";
                                                }else{
                                                    cloud = "false";
                                                }
                                                final String[] temps = {MaxTemp, MaxTemp, MinTemp, cloud, "true", localizedDirection, windSpeedFinal + " " + windUnit};
                                                final String FinalText = fulfillment + ". Mañana habrá una temperatura máxima de " + MaxTemp + " grados y una mínima de " + MinTemp + ". Estará " + description.toLowerCase() + ".";
                                                final Message message = new Message(description, assistant, false, true, temps);
                                                speak(FinalText);
                                                messageAdapter.add(message);
                                                messagesView.setSelection(messagesView.getCount() - 1);

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }


                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            // error
                                            Log.d("Error.Response", error.toString());
                                            Log.d("2", "YES");
                                        }
                                    }
                            ) {
                                @Override
                                public Map<String, String> getHeaders() {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("Content-Type", "application/json; charset=UTF-8");
                                    return params;
                                }


                            };
                            queue.add(weatherRequest);
                            */
                        }




                }

    }

