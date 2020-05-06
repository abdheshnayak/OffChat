package com.aknayak.offchatserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.aknayak.offchatserver.datas.DBHelper;
import com.aknayak.offchatserver.messages.Message;
import com.aknayak.offchatserver.messages.MessageAdapter;
import com.aknayak.offchatserver.serverdata.OperatorData;
import com.aknayak.offchatserver.serverdata.CountryData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;


/**
 * OffChat
 * Created by Abdhesh Nayak on 3/18/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 * Message Format
 **/
//<#>
//FROM:
//+9779805953008
//TO:
//+9779880616090
//Your Multi line message.
//qCJdG2cyZgy

/********************/


public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "MainActivity";
    public static final String ANONYMOUS = "anonymous";
    public static final String INSTANCE_ID = "instance";


    public static String authUser;
    public static String senderUserName;
    public static String receiverUsername;

    public static String MESSAGES_CHILD = "messages";
    public static String MAINVIEW_CHILD = "history";


    public static int viewnumber = 5;
    public static final Queue<Message> OTF = new ArrayDeque<>();
    private final Queue<com.aknayak.offchatserver.messages.Message> messages = new ArrayDeque<>();
    private final ArrayList<com.aknayak.offchatserver.messages.Message> sentMessages = new ArrayList<>();
    private final ArrayList<Message> tempmessages = new ArrayList<>();
    private final ArrayList<Message> tempmessages2 = new ArrayList<>();
    private Map<String, CountryData> serverDatavar = new HashMap<>();

    public static final String ROOT_CHILD = "UserData";
    //    public static final String ROOT_CHILD = "Debug";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mPhotoUrl;
    private FirebaseAuth mAuth;
    private Spinner countryPicker, operatorPicker;
    private RecyclerView rvQueue, rvSent;

    private Message staticMessage;
    private CountDownTimer cdt;

    Button sentbtn, queuebtn, refreshbtn;
    private ToggleButton onOffbtn;
    public static CountDownTimer offToOn;
    TextView message;

    DatabaseReference oflinesentRef, queueDataRef;
    ValueEventListener oflineSentlistner, queDataListenr;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        SmsBroadcastReceiver.updateDatabaseMessage(getApplicationContext());

        onOffbtn = findViewById(R.id.startbutton);
        refreshbtn = findViewById(R.id.refreshButton);
        rvQueue = findViewById(R.id.recyclerViewQueue);
        rvSent = findViewById(R.id.recyclerViewSent);
        message = findViewById(R.id.textView);
        sentbtn = findViewById(R.id.sentbtn);
        queuebtn = findViewById(R.id.quebtn);
        countryPicker = findViewById(R.id.countryPicker);
        operatorPicker = findViewById(R.id.operatorPicker);

        countryPicker.setOnItemSelectedListener(this);
        operatorPicker.setOnItemSelectedListener(this);

        refreshbtn.setOnClickListener(this);
        sentbtn.setOnClickListener(this);
        queuebtn.setOnClickListener(this);
        onOffbtn.setOnClickListener(this);

        final ArrayList<String> messagesID = new ArrayList<>();
        final MessageAdapter adapter = new MessageAdapter(tempmessages2, this);
        final MessageAdapter adaptersent = new MessageAdapter(sentMessages, this);
        rvQueue.setAdapter(adapter);
        rvSent.setAdapter(adaptersent);

        final DBHelper mydb = new DBHelper(this);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        final LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getApplicationContext());
        rvQueue.setLayoutManager(linearLayoutManager);
        rvSent.setLayoutManager(linearLayoutManager2);

        receiverUsername = null;
        mAuth = FirebaseAuth.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        // Initialize Firebase Auth
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, phone_verification.class));
            finish();
            return;
        } else {
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        checkAndRequestPermissions();

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("");
        progressDialog.setMessage("Loading. Please wait...");
        progressDialog.setIndeterminate(true);

//        updateServer();
        downloadServer();

        updateView(1);
        oflineSentlistner = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sentMessages.clear();
                mydb.deleteAllMessages();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final Message msg = snapshot.getValue(Message.class);
                    if (msg != null) {
                        msg.setMessageID(snapshot.getKey());
//                        sentMessages.add(msg);
                        if (checkAvailable(serverDatavar, msg.getMessageFor(), country, operator)) {
                            mydb.insertMessage(msg.getMsgBody(), msg.getMessageSource(), msg.getMessageSentTime(), msg.getMessageStatus(), msg.getMessageID(), "2", msg.getMessageFor(), msg.getReplyId());
                        }
                    }
                }
                try {
                    sentMessages.addAll(mydb.getAllMessages("2"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                adaptersent.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        progressDialog.show();
        oflinesentRef = FirebaseDatabase.getInstance().getReference().child("offline-sent");
        oflinesentRef.addValueEventListener(oflineSentlistner);

        queDataListenr = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                mydb.deleteAllMessages();
//                tempmessages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        final Message msg = ds.getValue(Message.class);
                        msg.setMessageID(ds.getKey());
                        if (msg.getMessageStatus() == 1) {
                            DatabaseReference o_status = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child("online_status").child(msg.getMessageFor()).child("online_status");
                            o_status.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Date date = dataSnapshot.getValue(Date.class);
                                    if (date != null) {
                                        Date currentTime = Calendar.getInstance(Locale.ENGLISH).getTime();
                                        long diff = 0;
                                        diff = currentTime.getTime() - date.getTime();
                                        long diffSeconds = diff / 1000;
                                        if (diffSeconds > 20) {
                                            Log.d("checkdata", country + operator + " " + msg.getMessageFor());
                                            if (checkAvailable(serverDatavar, msg.getMessageFor(), country, operator)) {
                                                messages.add(msg);
                                                mydb.insertMessage(msg.getMsgBody(), msg.getMessageSource(), msg.getMessageSentTime(), msg.getMessageStatus(), msg.getMessageID(), "1", msg.getMessageFor(), msg.getReplyId());
                                            }
                                            tempmessages2.clear();
                                            try {
                                                tempmessages2.addAll(mydb.getAllMessages("1"));
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                            adapter.notifyDataSetChanged();
                                        }
                                    } else {
                                        Log.d("checkdata", country + operator + " " + msg.getMessageFor());
                                        if (checkAvailable(serverDatavar, msg.getMessageFor(), country, operator)) {
                                            messages.add(msg);
                                            mydb.insertMessage(msg.getMsgBody(), msg.getMessageSource(), msg.getMessageSentTime(), msg.getMessageStatus(), msg.getMessageID(), "1", msg.getMessageFor(), msg.getReplyId());
                                        }
                                        tempmessages2.clear();
                                        try {
                                            tempmessages2.addAll(mydb.getAllMessages("1"));
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        adapter.notifyDataSetChanged();
                                    }
                                    progressDialog.cancel();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });

                        }
                        adapter.notifyDataSetChanged();
                    }
                }
                tempmessages2.clear();
                try {
                    tempmessages2.addAll(mydb.getAllMessages("1"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        };

        progressDialog.show();
        queueDataRef = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD);
        queueDataRef.addValueEventListener(queDataListenr);
        senderUserName = mFirebaseUser.getPhoneNumber();

        authUser = mFirebaseUser.getPhoneNumber();

        offToOn = new CountDownTimer(60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (OTF.size() > 0) {
                    staticMessage = OTF.peek();
                    FirebaseDatabase.getInstance().getReference().child("offline-sent").child(staticMessage.getMessageID()).updateChildren(staticMessage.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "" + messages.size());
                            FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(getRoot(staticMessage.getMessageSource(), staticMessage.getMessageFor())).child(staticMessage.getMessageID()).updateChildren(staticMessage.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    if (OTF.size() > 0) {
                                        OTF.remove();
                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "failled to upload");
                        }
                    });
//                    Toast.makeText(getApplicationContext(),AESHelper.decrypt(msg.getMessage()),Toast.LENGTH_SHORT).show();
//                            Log.d(TAG, staticMessage.getMessageID() + staticMessage.getMessageFor() + staticMessage.getMessageSource());

                }

            }

            @Override
            public void onFinish() {
                offToOn.start();
            }
        };

        cdt = new
                CountDownTimer(6000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (messages.size() > 0) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child(ROOT_CHILD)
                                    .child(MESSAGES_CHILD)
                                    .child(getRoot(messages.peek().getMessageSource(), messages.peek().getMessageFor()))
                                    .child(messages.peek().getMessageID())
                                    .child("messageStatus")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            int status = dataSnapshot.getValue(int.class);
                                            if (status == 1) {
                                                messages.peek().setMessageStatus(2);
                                                FirebaseDatabase.getInstance().getReference().child("offline-sent").child(messages.peek().getMessageID()).updateChildren(messages.peek().toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "" + messages.size());
                                                        sendSMSMessage(messages.peek().getMessageFor(), AESHelper.decrypt(messages.peek().getMsgBody()));
                                                        FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(getRoot(messages.peek().getMessageSource(), messages.peek().getMessageFor())).child(messages.peek().getMessageID()).updateChildren(messages.peek().toMap());
                                                        messages.remove();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.d(TAG, "failled to upload");
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            progressDialog.cancel();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onFinish() {
                        cdt.start();
                    }
                }

        ;

        offToOn.start();

    }


    public void updateButton(final Button a, final Button b) {
        int colorFrom = Color.rgb(200, 200, 255);
        int colorTo = Color.argb(0, 255, 255, 255);

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
        colorAnimation.setDuration(100); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                a.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });

        colorAnimation.start();
        colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(300); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                b.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();

    }

    public void updateView(int view) {
        if (viewnumber != view) {
            Log.d(TAG + " viewnumber", "" + viewnumber + "" + view);
            Display display = getWindowManager().getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            if (view == 1) {
                viewnumber = 1;
                rvQueue.setVisibility(View.VISIBLE);
                rvQueue.animate().translationX(0);
                rvSent.animate().translationX(width);
//            rvSent.setVisibility(View.INVISIBLE);
                updateButton(queuebtn, sentbtn);

            } else {
                viewnumber = 2;
                rvQueue.animate().translationX(-width);
//            rvQueue.setVisibility(View.INVISIBLE);
                rvSent.setVisibility(View.VISIBLE);
                rvSent.animate().translationX(0);
                updateButton(sentbtn, queuebtn);
            }
        }
    }


    public static String getRandString(int n) {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }


    private boolean checkAndRequestPermissions() {
        int permissionSendMessage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS);
        int receiveSMS = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);
        int readSMS = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (receiveSMS != PackageManager.PERMISSION_GRANTED || readSMS != PackageManager.PERMISSION_GRANTED || permissionSendMessage != PackageManager.PERMISSION_GRANTED || !listPermissionsNeeded.isEmpty()) {
            startActivity(new Intent(getApplicationContext(), permissions_grant.class));
        }
        return true;
    }


    private void downloadServer() {
        FirebaseDatabase.getInstance().getReference().child("servers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                serverDatavar = new HashMap<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    CountryData cds = ds.getValue(CountryData.class);
                    serverDatavar.put(ds.getKey(), cds);
                }
                updatePicker();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updatePicker() {

        Set<String> keyset = serverDatavar.keySet();
        List<String> countries = new ArrayList<>(keyset);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, countries);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        countryPicker.setAdapter(dataAdapter);
    }

    private void updateServer() {

        Map<String, CountryData> sd = new HashMap<>();
        sd.put("India", new CountryData("+91", null));

        sd.put("All", new CountryData("+", null));

        Map<String, OperatorData> od;
        ArrayList<String> codes;


        od = new HashMap<>();
        codes = new ArrayList<>();
        codes.add("961");
        codes.add("962");
        codes.add("988");
        od.put("Smart", new OperatorData(codes));


        List<Integer> list = Arrays.asList(98014,
                98140, 98150, 98170, 98240, 98249, 98049, 98079, 98060, 98149, 98159, 98169, 98179, 98160, 98259, 98269, 98279
                , 98027, 98207,
                98040, 98190, 98110, 98113, 98123, 98009, 98043, 98070, 98073, 98053, 98143, 98153, 98163, 98173, 98193, 98104, 98105, 98243, 98253, 98263
                , 98015,
                98047, 98077, 98059, 98147, 98157, 98167, 98177, 98197, 98199, 98117, 98247, 98257, 98267
                , 98016,
                98048, 98076, 98078, 98148, 98158, 98168, 98178, 98198, 98196, 98176, 98120, 98121, 98008, 98096, 98248, 98258, 98268
                , 98029, 98209,
                98042, 98068, 98071, 98072, 98142, 98152, 98162, 98172, 98192, 98111, 98112, 98118, 98122, 98091, 98092, 98211, 98212, 98218, 98242, 98252
                , 98021, 98020, 98011, 98022, 98018, 98019, 98012, 98010, 98023, 98024,
                9803, 9808, 9813, 9818, 98100, 98101, 98102, 98103, 98230, 98231, 98232, 98233, 98234, 98235, 98236, 98237, 98238, 98239
                , 98028,
                98166, 98241, 98251, 98261, 98041, 98065, 98066, 98067, 98058, 98141, 98151, 98161, 98171, 98191, 98271, 98291, 98266
                , 98026,
                98129, 98007, 98214, 98215, 98219, 98044, 98069, 98074, 98075, 98054, 98144, 98154, 98164, 98174, 98194, 98175, 98114, 98115, 98119, 98244, 98254
                , 9801300,
                98061, 98051, 98052, 98213
                , 9801325,
                98062, 98095, 98097, 98098, 98128, 98108, 98109, 98228, 98229
                , 9801350,
                98063, 98093
                , 98025,
                98195, 98124, 98125, 98005, 98224, 98225, 98045, 98145, 98155, 98165, 98245, 98255, 98265
                , 98017,
                98046, 98146, 98156, 98116, 98126, 98006, 98246, 98256, 98216, 98226
                , 9801375,
                98127, 98106, 98107, 9805740, 98064, 98094);
        codes = new ArrayList<>();
        for (Integer i : list) {
            codes.add(String.valueOf(i));
        }
        od.put("Ncell", new OperatorData(codes));


        sd.put("Nepal", new CountryData("+977", od));

        FirebaseDatabase.getInstance().getReference().child("servers").setValue(sd);
    }

    public static String getRoot(String first, String second) {
        if (Double.valueOf(first) > Double.valueOf(second)) {
            return first + second;
        } else {
            return second + first;
        }
    }

    public void updateStartButton(int view, final ToggleButton a) {
        int colorFrom = Color.rgb(120, 254, 142);
        int colorTo = Color.rgb(254, 120, 142);
        ValueAnimator colorAnimation;
        if (view == 2) {
            colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
        } else {
            colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        }
        colorAnimation.setDuration(500); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                a.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startbutton:
                switch (onOffbtn.getText().toString().trim()) {
                    case "Start Server":
                        updateStartButton(1, onOffbtn);
                        findViewById(R.id.offlineStatus).setVisibility(View.VISIBLE);
                        findViewById(R.id.onlineStatus).setVisibility(View.INVISIBLE);
                        cdt.cancel();
//                        offToOn.cancel();
                        break;
                    case "Stop Server":
                        AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(this);
                        alertDialogBuilder2.setPositiveButton("I acept it.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                updateStartButton(2, onOffbtn);
                                findViewById(R.id.offlineStatus).setVisibility(View.INVISIBLE);
                                findViewById(R.id.onlineStatus).setVisibility(View.VISIBLE);
                                cdt.start();
                            }
                        });
                        alertDialogBuilder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onOffbtn.performClick();
                            }
                        });
                        alertDialogBuilder2.setCancelable(false);
                        alertDialogBuilder2.setTitle("Attention !!!");
                        alertDialogBuilder2.setMessage("You selected the options: \nCountry -> " + country + "\nOperator -> " + ((operator == null) ? "All" : operator) + "\nStandard charge will be deducted from your sim card. be careful !!!");
                        alertDialogBuilder2.show();
                        break;
                }
                break;
            case R.id.sentbtn:
                updateView(2);
                break;
            case R.id.quebtn:
                updateView(1);
                break;
            case R.id.refreshButton:
                if (viewnumber == 1) {
                    queueDataRef.addListenerForSingleValueEvent(queDataListenr);
                } else {
                    oflinesentRef.addListenerForSingleValueEvent(oflineSentlistner);
                }
                progressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                }, 2 * 1000);
                break;
        }
    }

    String country;
    String operator;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String item = parent.getItemAtPosition(position).toString();

        // Showing selected spinner item
        switch (parent.getId()) {
            case R.id.countryPicker:
                List<String> operators = new ArrayList<String>();

                CountryData cd = serverDatavar.get(item);
                if (cd.getOperaters() != null) {
                    Map<String, OperatorData> od = cd.getOperaters();
                    for (String st : od.keySet()) {
                        operators.add(st);
                    }
                } else {
                    operators.clear();
                }

                findViewById(R.id.operatorLayout).setVisibility(View.VISIBLE);
                country = item;

                // Creating adapter for spinner
                ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, operators);

                // Drop down layout style - list view with radio button
                dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                // attaching data adapter to spinner
                operatorPicker.setAdapter(dataAdapter2);
//                operator = null;
                if (operators.size() == 0) {
                    findViewById(R.id.operatorLayout).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.operatorLayout).setVisibility(View.VISIBLE);
                }
                break;
            case R.id.operatorPicker:
                operator = item;
                break;
        }
        refreshbtn.performClick();
//        Toast.makeText(parent.getContext(), "Selected: " + country + "/" + operator, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    public boolean checkAvailable(Map<String, CountryData> sd, String number, String country, String operator) {
        CountryData cd = sd.get(country);
        Log.d("checkdata", "6");
        if (cd != null && cd.getCntCode() != null) {
            Log.d("checkdata", "1");
            if (cd.getCntCode().equals(number.substring(0, cd.getCntCode().length()))) {
                Log.d("checkdata", "2");
                Map<String, OperatorData> od = cd.getOperaters();
                if (od != null) {
                    Log.d("checkdata", "3");
                    OperatorData ops = od.get(operator);
                    if (ops != null) {
                        Log.d("checkdata", "4");
                        for (String st : ops.getOpCode()) {
//                            Log.d("checkdata",);
                            if (st.equals("ALL")) return true;
                            if (st.equals(number.substring(cd.getCntCode().length()).substring(0, st.length()))) {
                                Log.d("checkdata", "5");
                                return true;
                            }
                        }
                        return false;
                    } else return false;
                } else return true;
            } else return false;
        } else return true;
    }

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;

    String sendingNumber, sendingMessage;

    public void sendSMSMessage(String phoneNo, String message) {
        sendingMessage = message;
        sendingNumber = phoneNo;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Please Give Permission", Toast.LENGTH_SHORT).show();
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.",
                    Toast.LENGTH_LONG).show();
        }

    }


}
