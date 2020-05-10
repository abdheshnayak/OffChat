package com.aknayak.offchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aknayak.offchat.datas.DBHelper;
import com.aknayak.offchat.globaldata.AESHelper;
import com.aknayak.offchat.globaldata.respData;
import com.aknayak.offchat.messages.Message;
import com.aknayak.offchat.users.connDetail;
import com.aknayak.offchat.users.userAdapter;
import com.aknayak.offchat.versionInfo.appVersion;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.aknayak.offchat.globaldata.Constants.ANONYMOUS;
import static com.aknayak.offchat.globaldata.Constants.INSTANCE_ID;
import static com.aknayak.offchat.globaldata.Constants.PREF_DATA;
import static com.aknayak.offchat.globaldata.Constants.ROOT_CHILD;
import static com.aknayak.offchat.globaldata.respData.*;
import static com.aknayak.offchat.globaldata.respData.getRandString;
import static com.aknayak.offchat.globaldata.respData.verifyUser;

/**
 * OffChat
 * Created by Abdhesh Nayak on 3/18/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public final ArrayList<Message> messages = new ArrayList<Message>();
    private static final String TAG = "MainActivity";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mPhotoUrl;
    private FirebaseAuth mAuth;
    private TextView aboutButton;

    ImageButton mImgButton;
    ImageButton mMenuButton;
    ImageButton getmMenuButtonClose;
    ConstraintLayout menuLayout;
    TextView mSignOutButton;
    ImageView mTouchSensors;
    RecyclerView rvUser;
    userAdapter adapter;
    TextView settings;
    TextView checkAcess;
    CountDownTimer cdt;


    @Override
    protected void onResume() {
        super.onResume();
        appLaunched = true;
        messages.clear();
        ArrayList<Message> arrayList = null;
        try {
            arrayList = mydb.getHist();
            messages.addAll(arrayList);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
        rvUser.scrollToPosition(arrayList.size());
    }

    DBHelper mydb;

    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        startActivity(new Intent(getApplicationContext(), splashScreen.class));

        mydb = new DBHelper(getApplicationContext());

        receiverUsername = null;


        mAuth = FirebaseAuth.getInstance();
        mUsername = ANONYMOUS;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mSignOutButton = findViewById(R.id.signOut);
        aboutButton = findViewById(R.id.aboutButton);
        mMenuButton = findViewById(R.id.menuButtonActivityMain);
        getmMenuButtonClose = findViewById(R.id.menuCloseActivity_main);
        menuLayout = findViewById(R.id.menuLayout);
        mTouchSensors = findViewById(R.id.splashImageMainActivity);
        mImgButton = findViewById(R.id.floatButton);
        settings = findViewById(R.id.settings);
        checkAcess = findViewById(R.id.profileButton);

        checkAcess.setOnClickListener(this);
        mTouchSensors.setOnClickListener(this);
        mMenuButton.setOnClickListener(this);
        getmMenuButtonClose.setOnClickListener(this);
        menuLayout.setOnClickListener(this);
        mImgButton.setOnClickListener(this);
        settings.setOnClickListener(this);
        aboutButton.setOnClickListener(this);


        // Initialize Firebase Auth
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, phone_verification.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getPhoneNumber();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        checkAndRequestPermissions();


        senderUserName = mFirebaseUser.getPhoneNumber();

        authUser = mFirebaseUser.getPhoneNumber();

        createNotificationChannel(this);

        rvUser = findViewById(R.id.recyclerView);

        adapter = new userAdapter(messages, this);

        adView = findViewById(R.id.adView);
        MobileAds.initialize(MainActivity.this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d("UUUU", "Initialized");
            }
        });

        FirebaseDatabase.getInstance().getReference().child("admin").child("adview").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean b = dataSnapshot.getValue(Boolean.class);
                if (b != null && b) {
                    showAds = true;
                    Toast.makeText(getApplicationContext(), String.valueOf(showAds), Toast.LENGTH_SHORT).show();
                    AdRequest adRequest = new AdRequest.Builder().build();
                    adView.setVisibility(View.VISIBLE);
                    adView.loadAd(adRequest);
                } else {
                    showAds = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Attach the adapter to the recyclerview to populate items
        rvUser.setAdapter(adapter);
//        rvUser.scrollToPosition(users.size());

        // Set layout manager to position the items
//        rvUser.setLayoutManager(new LinearLayoutManager(this));


        ArrayList<Message> msg = null;
        int n = 0;
        try {
            msg = mydb.getHist();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        n = msg.size();
        for (int i = 0; i < n; i++) {
            final String name = msg.get(i).getMessageFor().equals(senderUserName) ? msg.get(i).getMessageSource() : msg.get(i).getMessageFor();
            if (mydb.getUserName(name).equals(name)) {
                FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child("online_status").child(name).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue(String.class) != null) {
                            mydb.insertuserInfo(name, "[ " + dataSnapshot.getValue(String.class) + " ]");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
        adapter.notifyDataSetChanged();


        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
//        linearLayoutManager.setReverseLayout(true);
        rvUser.setLayoutManager(linearLayoutManager);

        mSignOutButton.setOnClickListener(this);


//        FirebaseDatabase.getInstance().getReference().child("admin").child("offchat_version_control").updateChildren(new appVersion("1","1").toMap());


        FirebaseDatabase.getInstance().getReference().child("admin").child("offchat_version_control").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                appVersion appVer = dataSnapshot.getValue(appVersion.class);
                if (appVer != null) {
                    checkUpdate(appVer.getForceupdateVersion(), appVer.getNormalUpdateVersion(), 0, MainActivity.this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (mydb.getUserInfo(INSTANCE_ID) != null) {
            FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child("online_status").child(senderUserName).child("InstanceVar").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String string = dataSnapshot.getValue(String.class);
                    if (string != null) {
                        rvUser.setVisibility(View.GONE);
                        boolean check = verifyUser(string, 0, MainActivity.this);
                        if (check) {
                            rvUser.setVisibility(View.VISIBLE);
                        }
                    } else {
                        final String inst = getRandString(10);
                        FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child("online_status").child(senderUserName).child("InstanceVar").setValue(inst).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mydb.insertuserInfo(INSTANCE_ID, inst);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            final String inst = getRandString(10);
            FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child("online_status").child(senderUserName).child("InstanceVar").setValue(inst).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mydb.insertuserInfo(INSTANCE_ID, inst);
                }
            });

        }


        try {
            if (mydb.getHist().size() == 0) {
                Log.d("UUUU", "add");
                FirebaseDatabase.getInstance().getReference().child("admin").child("welcome_notification").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String notification = dataSnapshot.getValue(String.class);
                        Log.d("UUUU", notification);

                        Message message = new Message(AESHelper.encrypt(notification), "+1", Calendar.getInstance(Locale.ENGLISH).getTime(), 1, getRandString(15), senderUserName);
                        FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(getRoot("+1", senderUserName))
                                .child(message.getMessageID()).updateChildren(message.toMap());

                        DatabaseReference mFirebaseRefrence = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MAINVIEW_CHILD).child(senderUserName).child("+1");

                        messages.clear();

                        mFirebaseRefrence.setValue(new connDetail(true));

                        DatabaseReference o_status = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child("online_status").child("+1").child("online_status");
                        o_status.setValue(Calendar.getInstance(Locale.ENGLISH).getTime());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (messages.size() != 0) {
            messages.clear();
        }
        try {
            messages.clear();
            messages.addAll(mydb.getHist());
            adapter.notifyDataSetChanged();
            rvUser.scrollToPosition(messages.size());
        } catch (ParseException e) {
            Log.d("lskdf", e.getMessage());
        }


        final ValueEventListener v3 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("KKK", "ll");
                try {
                    messages.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Message message = snapshot.getValue(Message.class);
                        if (message != null)
                            if (!message.getMessageSource().equals(senderUserName) || message.getMessageStatus() != 1) {
                                mydb.insertMessage(message.getMsgBody(), message.getMessageSource(), message.getMessageSentTime(), message.getMessageStatus(), snapshot.getKey(), getRoot(message.getMessageFor(), message.getMessageSource()), message.getMessageFor(), message.getReplyId());
                            }
                    }
                    messages.addAll(mydb.getHist());
                    adapter.notifyDataSetChanged();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (messages.size() != 0) {
//                    try {
//                        if (cnt != mydb.getAllMessages(rootPath).size()) {
//                            cnt = mydb.getAllMessages(rootPath).size();
//                            rvMessages.scrollToPosition(messages.size() - 1);
//                        }
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };


        ValueEventListener v1 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("kkkk", "ll");
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    connDetail check = snapshot.getValue(connDetail.class);
                    if (check != null && check.getCnDetails()) {
                        String user = snapshot.getKey();
                        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(getRoot(senderUserName, user));
                        messageRef.addValueEventListener(v3);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MAINVIEW_CHILD).child(senderUserName);
        historyRef.addValueEventListener(v1);

//        FirebaseApp.initializeApp(this);

        Intent i = new Intent(com.aknayak.offchat.services.mainService.class.getName());
        i.setPackage(this.getPackageName());
        startService(i);

        cdt = new CountDownTimer(25000, 25000) {
            @Override
            public void onTick(long millisUntilFinished) {
                final DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child("online_status").child(senderUserName);
                historyRef.child("online_status").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Date currentTime;
                        currentTime = Calendar.getInstance(Locale.ENGLISH).getTime();
                        long diff = 0;

                        Date dt = dataSnapshot.getValue(Date.class);
                        if (dt != null) {
                            diff = currentTime.getTime() - dt.getTime();
                        } else {
                            historyRef.child("online_status").setValue(Calendar.getInstance(Locale.ENGLISH).getTime());
                            diff = 5;
                        }
                        long diffSeconds = diff / 1000 % 60;

                        if (diffSeconds > 10) {
                            historyRef.child("online_status").setValue(Calendar.getInstance(Locale.ENGLISH).getTime());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onFinish() {
                cdt.start();
            }
        };

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                try {
                    messages.clear();
                    messages.addAll(mydb.getHist());
                    adapter.notifyDataSetChanged();
                    swipeContainer.setRefreshing(false);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

//        rvUser.scrollToPosition(users.size());
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private SwipeRefreshLayout swipeContainer;


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menuButtonActivityMain:
                mTouchSensors.setVisibility(View.VISIBLE);
//                menuLayout.setTranslationY(-300);
//                menuLayout.setTranslationX(300);
                menuLayout.setVisibility(View.VISIBLE);
//                menuLayout.animate().translationX(0);
//                menuLayout.animate().translationY(0);
                mMenuButton.setVisibility(View.GONE);
                getmMenuButtonClose.setVisibility(View.VISIBLE);
                break;
            case R.id.menuCloseActivity_main:
            case R.id.splashImageMainActivity:
//                menuLayout.animate().translationX(300);
//                menuLayout.animate().translationY(-300);
                mTouchSensors.setVisibility(View.INVISIBLE);
                menuLayout.setVisibility(View.GONE);
                getmMenuButtonClose.setVisibility(View.GONE);
                mMenuButton.setVisibility(View.VISIBLE);
                break;
            case R.id.rootLayout:
                break;
            case R.id.signOut:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        signOut(MainActivity.this);
                        mUsername = "ANONYMOUS";
                        startActivity(new Intent(getApplicationContext(), phone_verification.class));
                        finish();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialogBuilder.setTitle("Sign Out?");
                alertDialogBuilder.setMessage("We Don't store your chats. So if you Sign Out Your Account then you will loose your chats.\n\nAre You Sure to Sign Out ???");
                alertDialogBuilder.show();
                getmMenuButtonClose.performClick();
                break;
            case R.id.floatButton:
                startActivity(new Intent(getApplicationContext(), AllConcacts.class));
                break;
            case R.id.settings:
                respData.playSound(MainActivity.this, sound_incoming_message);
                Toast.makeText(getApplicationContext(), "You don't have access now.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.profileButton:

                Intent i = new Intent(getApplicationContext(), myProfile.class);
                i.putExtra("phone", senderUserName);
                startActivity(i);
                getmMenuButtonClose.performClick();
                break;
            case R.id.aboutButton:
                Intent j = new Intent(getApplicationContext(), aboutPage.class);
                startActivity(j);
                getmMenuButtonClose.performClick();
                break;
            default:
                break;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        appLaunched = false;
        cdt.cancel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(9878);
        appLaunched = true;
        cdt.start();

    }


    @Override
    protected void onStop() {
        super.onStop();
        appLaunched = false;
    }

    private boolean checkAndRequestPermissions() {
        int permissionSendMessage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS);
        int receiveSMS = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);
        int readSMS = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (receiveSMS != PackageManager.PERMISSION_GRANTED || readSMS != PackageManager.PERMISSION_GRANTED || permissionSendMessage != PackageManager.PERMISSION_GRANTED || !listPermissionsNeeded.isEmpty()) {
            startActivity(new Intent(getApplicationContext(), permissions_grant.class));
            finish();
        } else {
            if (mydb.getAllCotacts().size() <= 1) {
                SharedPreferences sharedPreferences = getSharedPreferences(PREF_DATA, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("allcontacts", 0);
                editor.apply();
                mImgButton.performClick();
            }
        }
        return true;
    }


}
