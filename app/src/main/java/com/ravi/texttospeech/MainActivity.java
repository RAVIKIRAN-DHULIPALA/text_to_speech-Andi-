package com.ravi.texttospeech;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static android.content.Context.*;

public class MainActivity extends AppCompatActivity {
    TextToSpeech t1;
    SpeechRecognizer sr;
    ImageView iv;
    TextView tv;
    String num = "";
    ArrayList<String> re=new ArrayList<String>();
    ArrayList<String> rep=new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = findViewById(R.id.iv);
        tv = findViewById(R.id.textView);
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);
        if (isFirstRun) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE}, 1);
            }

            getapps();
            SharedPreferences pref=MainActivity.this.getSharedPreferences("apps", MODE_PRIVATE);
            SharedPreferences.Editor edit=pref.edit();
            Set<String> set = new HashSet<String>();
            set.addAll(re);
            edit.putStringSet("yourKey", set);
            edit.commit();}
            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                    .putBoolean("isFirstRun", false).commit();

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listen();
            }
        });
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (t1.getEngines().size() == 0) {
                    Toast.makeText(MainActivity.this, "No Available engines on Device", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    t1.setLanguage(Locale.US);
                    speak("Hello\n I'm Andi \nYour personal assistant\npress mic to instruct me");
                    tv.setText("Hello\n I'm Andi \nYour personal assistant\npress mic to instruct me");
                }
            }
        });

        initspeechrecog();

    }

    private void initspeechrecog() {
        if (SpeechRecognizer.isRecognitionAvailable(MainActivity.this)) {
            sr = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
            sr.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int error) {

                }

                @Override
                public void onResults(Bundle results) {
                    List<String> res = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    processresults(res.get(0));
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
        }
    }
    @SuppressLint("MissingPermission")
    private void processresults(String s) {
        try{
        s = s.toLowerCase();
        tv.setText(s);
        if (s.indexOf("what is your name") != -1) {
            speak("Hi \ni'm Andi\nyour personal assistant");
        } else if (s.indexOf("open") != -1) {
            SharedPreferences prefs=MainActivity.this.getSharedPreferences("apps", MODE_PRIVATE);
            Set<String> sets = prefs.getStringSet("yourKey", null);
            rep=new ArrayList<String>(sets);
            if (s.indexOf("facebook")!=-1){
                speak("opening facebook");
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.facebook.katana");
                startActivity(launchIntent);
            }
            if (s.indexOf("tez")!=-1||s.indexOf("googlepay")!=-1){
                speak("opening Google pay");
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.nbu.paisa.user");
                startActivity(launchIntent);
            }
            else{
                Log.i("info",rep.get(1));
            for (int i=0;i<rep.size();i++) {
                if(s.replaceAll("\\s+","").contains(rep.get(i).toLowerCase().replaceAll("\\s+",""))){
                    String pack = getPackageName(rep.get(i));
                    speak("opening " + rep.get(i).toLowerCase());
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(pack);
                    startActivity(launchIntent);
                }
            }
            }
        } else if (s.indexOf("call") != -1) {
            String a[] = s.split("\\s");
                    if (a.length==3){
                        num = getPhoneNumber(a[1].toUpperCase().charAt(0) + a[1].substring(1, a[1].length()) + " " + a[2], MainActivity.this);
                    }
                    if (a.length == 2) {
                        num = getPhoneNumber(a[1].toUpperCase().charAt(0) + a[1].substring(1, a[1].length()), MainActivity.this);
                    }
            Intent i = new Intent(Intent.ACTION_CALL);
            i.setData(Uri.parse("tel:" + num));
            startActivity(i);

        } else if (s.indexOf("time") != -1) {
            Date now = new Date();
            String time = DateUtils.formatDateTime(MainActivity.this, now.getTime(), DateUtils.FORMAT_SHOW_TIME);
            speak("the time is " + time);
        } else if (s.indexOf("byebye") != -1) {
            speak("bye!!");
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
        else{
            speak("command is out of my data set");
        }
        }
        catch (Exception e){
            speak("command is out of my data set");
        }
    }

    private void speak(String msg) {
        if (Build.VERSION.SDK_INT > 21) {
            t1.speak(msg, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            t1.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public String getPhoneNumber(String name, Context context) {
        String ret = null;
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like'%" + name + "%'";
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null);
        if (c.moveToFirst()) {
            ret = c.getString(0);
        }
        c.close();
        if (ret == null)
            ret = "Unsaved";
        return ret;
    }
public String getPackageName(String name){
        PackageManager pm= getApplicationContext().getPackageManager();
        List<ApplicationInfo>l=pm.getInstalledApplications(PackageManager.GET_META_DATA);
        String packname="";
        for (ApplicationInfo ai:l){
            String n=(String)pm.getApplicationLabel(ai);
            if (n.contains(name)||name.contains(n))
            {
                packname=ai.packageName;
            }
        }
        return packname;
}
public void getapps(){
    PackageManager pm= getApplicationContext().getPackageManager();
    List<ApplicationInfo>l=pm.getInstalledApplications(PackageManager.GET_META_DATA);
    for (ApplicationInfo ai:l){
        String n=(String)pm.getApplicationLabel(ai);
        re.add(n);
    Log.i("app",n);}
    }
    public void listen(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        if (intent.resolveActivity(getPackageManager()) != null) {

            sr.startListening(intent);
        } else {
            Toast.makeText(MainActivity.this, "not supported", Toast.LENGTH_LONG).show();
        }
    }
}
