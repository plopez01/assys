package com.ac.acassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Locale;

public class basicConfig extends AppCompatActivity {

    private MessageAdapter messageAdapter;
    private ListView messagesView;

    TextToSpeech t1;
    Locale spanish = new Locale("es", "ES");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_config);
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    t1.setLanguage(spanish);
                    final MemberData user = new MemberData("Assys", "#d9d9d9");
                    final Message message = new Message("¡Hola! Bienvenido, soy Assys, me alegro mucho de verte. Me voy a encargar de ayudarte en todo lo posible, para empezar, neccesito un nombre para poder dirigirme a ti. Dime. ¿Cómo quieres que te llame?", user, false, false, null);
                    messageAdapter.add(message);
                    messagesView.setSelection(messagesView.getCount() - 1);
                    speak("¡Hola! Bienvenido, soy ,Assys, me alegro mucho de verte. Me voy a encargar de ayudarte en todo lo posible, para empezar, neccesito un nombre para poder dirigirme a ti. Dime. ¿Cómo quieres que te llame?");
                }
            }
        });
        messageAdapter = new MessageAdapter(this);
        messagesView = findViewById(R.id.messages_assistant);
        messagesView.setAdapter(messageAdapter);
    }

    // Speak function
    public void speak (String toSpeak){
        t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void okButton (View view){
        final TextView TextBox = findViewById(R.id.userInput);
        final String TextBoxText = TextBox.getText().toString();
        if(!TextBoxText.equals("")) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            final MemberData user = new MemberData("Assys", "#d9d9d9");
            final Message message = new Message("De acuerdo, " + TextBoxText, user, false, false, null);
            speak("De acuerdo, " + TextBoxText + ", vamos a empezar!");
            messageAdapter.add(message);
            messagesView.setSelection(messagesView.getCount() - 1);
            editor.putString("name", TextBoxText);
            editor.putBoolean("isFirstLogin", false);
            TextBox.setText("");
            editor.apply();
            TextBox.setHint("");
            Intent myIntent = new Intent(basicConfig.this, MainActivity.class);
            basicConfig.this.startActivity(myIntent);
        }else{
            TextBox.setHint("¡No puede estar vacio!");
        }
    }



    /*  SharedPreferences sharedPreferences = getSharedPreferences("EVENTS", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isFirstLogin", false);
        editor.apply();*/
}
