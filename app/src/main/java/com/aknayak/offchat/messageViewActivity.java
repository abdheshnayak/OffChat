package com.aknayak.offchat;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aknayak.offchat.datas.DBHelper;
import com.aknayak.offchat.globaldata.AESHelper;
import com.aknayak.offchat.globaldata.respData;
import com.aknayak.offchat.messages.Message;
import com.aknayak.offchat.messages.MessageAdapter;
import com.aknayak.offchat.users.connDetail;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Delayed;

import static com.aknayak.offchat.MainActivity.INSTANCE_ID;
import static com.aknayak.offchat.MainActivity.ROOT_CHILD;
import static com.aknayak.offchat.MainActivity.getRandString;
import static com.aknayak.offchat.MainActivity.getRoot;
import static com.aknayak.offchat.MainActivity.receiverUsername;
import static com.aknayak.offchat.MainActivity.senderUserName;
import static com.aknayak.offchat.globaldata.AESHelper.encrypt;

/**
 * OffChat
 * Created by Abdhesh Nayak on 3/18/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/


public class messageViewActivity extends AppCompatActivity implements View.OnClickListener {
    EditText mMessageBox;
    ImageButton mMessageSendButton;
    ImageButton mMessageBoxCloseButton;
    ImageButton mDeleteButton;
    ImageButton mMenuButton;
    CheckBox selectAllcheckBox;
    ImageButton mMenuButtonClose;
    ConstraintLayout menuLayout;
    TextView mProfileButton;
    TextView userName;
    TextView onlineStatusTextView;
    RecyclerView rvMessages;
    ConstraintLayout constraintLayout;
    Date lastSeenTime;
    TextView selectCount;
    boolean menuButtonStatus = true;
    public ArrayList<Message> messages = new ArrayList<Message>();
    public ImageButton scrollButton;

    MessageAdapter adapter = new MessageAdapter(messages, this);

    Boolean sendButtonStatus = false;

    private DatabaseReference mFirebaseDatabaseReference;
    private DatabaseReference o_status;
    private DatabaseReference historyRef;
    private DatabaseReference messageRef;
    private ValueEventListener v1;
    private ValueEventListener v2;
    private ValueEventListener v3;
    private ValueEventListener v4;

    public static String MESSAGES_CHILD = "messages";
    public static String MAINVIEW_CHILD = "history";
    public String rootPath;
    BroadcastReceiver networkStateReceiver;
    DBHelper mydb;

    int cnt = 0;

    boolean flg = false;


    AdView adView;


    public void dellButton() {
        mDeleteButton.setVisibility(View.VISIBLE);
        mMenuButton.setVisibility(View.GONE);
        findViewById(R.id.userSpace).setVisibility(View.GONE);
        findViewById(R.id.selectAllBoxContainer).setVisibility(View.VISIBLE);
    }

    public void refreshSelectCount(){
        Log.d("sel","kk");
        selectCount.setText(respData.delItem.size()+"/"+messages.size());
        if (respData.delItem.size()!=messages.size()){
            selectAllcheckBox.setChecked(false);
        }else if (respData.delItem.size() == messages.size()){
            selectAllcheckBox.setChecked(true);
        }
    }
    public void scButton(int i) {
        if (i > 15) {
            if (!flg) {
                scrollButton.setVisibility(View.VISIBLE);
                flg = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        flg = false;
                        scrollButton.setVisibility(View.INVISIBLE);
                    }
                }, 2 * 1000); // wait for 5 seconds
            }
        } else {
            scrollButton.setVisibility(View.INVISIBLE);
            flg = false;
        }
    }

    public Boolean isOnline() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal == 0);
            return reachable;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfoListener = connectivityManager.getActiveNetworkInfo();
        return networkInfoListener != null && networkInfoListener.isConnectedOrConnecting();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkStateReceiver);
    }


    CountDownTimer cdt;
    boolean pnt = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_view);


        adView = findViewById(R.id.adView);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d("UUUU", "Initialized");
            }
        });

//        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d("UUUU", "Add Loaded");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.d("UUUU", "Add Loaded failed" + i);
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                Log.d("UUUU", "Add Imper");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.d("UUUU", "Add Opend");
            }
        });

        respData.delItem = new ArrayList<>();

        receiverUsername = getIntent().getStringExtra("phoneNumber");
        rootPath = getRoot(senderUserName, receiverUsername);

        //        Initialize All the elements of the screen
        selectAllcheckBox = findViewById(R.id.selectAllBox);
        mMessageBoxCloseButton = findViewById(R.id.messageBox_closeButton);
        mDeleteButton = findViewById(R.id.deleteButton);

        selectCount = findViewById(R.id.selectedCount);
        rvMessages = findViewById(R.id.messageView);
        mMessageSendButton = findViewById(R.id.sendButton);
        mMenuButton = findViewById(R.id.menuButton);
        mMenuButtonClose = findViewById(R.id.menuClose);
        menuLayout = findViewById(R.id.menuLayout);
        mProfileButton = findViewById(R.id.profileButton_message);
        mMessageBox = findViewById(R.id.messageBox);
        constraintLayout = findViewById(R.id.rootLayout);
        userName = findViewById(R.id.msgUserName);
        scrollButton = findViewById(R.id.scrollbutton);

        onlineStatusTextView = findViewById(R.id.onlineStatusTextView_in_MessageView);

//        Add All the components into OnClickListner
        findViewById(R.id.splashImage).setOnClickListener(this);
        scrollButton.setOnClickListener(this);
        constraintLayout.setOnClickListener(this);
        mMenuButton.setOnClickListener(this);
        mMenuButtonClose.setOnClickListener(this);
        mProfileButton.setOnClickListener(this);
        mMessageSendButton.setOnClickListener(this);
        mMessageBoxCloseButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);
        findViewById(R.id.userSpace).setOnClickListener(this);
        findViewById(R.id.messageView).setOnClickListener(this);


        if (receiverUsername.equals("+1")) {
            findViewById(R.id.messageBoxContainer).setVisibility(View.GONE);
        }


        selectAllcheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    respData.delItem = new ArrayList<>();
                    for(Message msg : messages){
                        respData.delItem.add(msg.getMessageID());
                    }
                    refreshSelectCount();
                    adapter.notifyDataSetChanged();
                }
            }
        });

        mydb = new DBHelper(getApplicationContext());


//        Refrences
        o_status = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child("online_status").child(receiverUsername).child("online_status");
        historyRef = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child("online_status").child(senderUserName);


//        Value Event Listners
        v1 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Date currentTime;
                currentTime = Calendar.getInstance(Locale.ENGLISH).getTime();
                long diff = 0;
                Date date = dataSnapshot.getValue(Date.class);
                if (date == null) {
                    historyRef.child("online_status").setValue(Calendar.getInstance(Locale.ENGLISH).getTime());
                } else {
                    lastSeenTime = dataSnapshot.getValue(Date.class);
                    diff = currentTime.getTime() - lastSeenTime.getTime();
                    long diffSeconds = diff / 1000 % 60;
                    if (diffSeconds > 30) {
                        historyRef.child("online_status").setValue(Calendar.getInstance(Locale.ENGLISH).getTime());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };


        v2 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                historyRef.child("online_status").addListenerForSingleValueEvent(v1);

                Date date = dataSnapshot.getValue(Date.class);
                if (date != null) {
                    Date currentTime = Calendar.getInstance(Locale.ENGLISH).getTime();
                    long diff = 0;

                    diff = currentTime.getTime() - date.getTime();
                    long diffMinutes = diff / (60 * 1000) % 60;
                    onlineStatusTextView.setVisibility(View.VISIBLE);
                    if (diffMinutes < 1) {
                        onlineStatusTextView.setText("online");
                    } else {

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);

                        int dayCheck = Double.valueOf(simpleDateFormat.format(currentTime.getTime())).intValue() - Double.valueOf(simpleDateFormat.format(date.getTime())).intValue();

                        String str;
                        if (dayCheck == 0) {
                            SimpleDateFormat sf = new SimpleDateFormat("hh:mm aa");
                            str = sf.format(date);
                        } else if (dayCheck == 1) {
                            SimpleDateFormat sf = new SimpleDateFormat("hh:mm aa");
                            str = sf.format(date);
                            str = "Yesterday " + str;
                        } else {
                            SimpleDateFormat sf = new SimpleDateFormat("EEEE MMM dd  hh:mm aa");
                            str = sf.format(date);
                        }
                        String str2 = str.replace("AM", "am").replace("PM", "pm");
                        str2 = "last seen " + str2;
                        onlineStatusTextView.setText(str2);
                    }
                } else {
                    onlineStatusTextView.setVisibility(View.VISIBLE);
                    onlineStatusTextView.setText("Not on OffChat");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        o_status.addValueEventListener(v2);

        v3 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    messages.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Message message = snapshot.getValue(Message.class);
                        if ((message != null && message.getMessageSource().equals(message.getMessageFor())) || message.getMessageStatus() != 1) {
                            mydb.insertMessage(message.getMessage(), message.getMessageSource(), message.getMessageSentTime(), message.getMessageStatus(), snapshot.getKey(), rootPath, message.getMessageFor());
                        }
                    }
                    messages.addAll(mydb.getAllMessages(rootPath));
                    adapter.notifyDataSetChanged();

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (messages.size() != 0) {
                    adapter.notifyDataSetChanged();
                    try {
                        if (cnt != mydb.getAllMessages(rootPath).size()) {
                            cnt = mydb.getAllMessages(rootPath).size();
                            rvMessages.scrollToPosition(messages.size() - 1);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };


//        Online Status Listner
        cdt = new CountDownTimer(5000, 5000) {
            @Override
            public void onTick(long millisUntilFinished) {
                o_status.addListenerForSingleValueEvent(v2);


                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            msg = mydb.getAllMessagesByStatus(rootPath, 1, senderUserName);


                            int n = msg.size();
                            if (n > 50) {
                                for (int i = 50; i < n; i++) {
                                    FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(rootPath).child(msg.get(i).getMessageID()).removeValue();
                                    mydb.deleteMessage(msg.get(i).getMessageID());
                                }
                                pnt = false;
                            }

                            msg = mydb.getAllMessagesByStatus(rootPath, 0, senderUserName);
                            n = msg.size();

                            for (int i = 0; i < n; i++) {
                                DatabaseReference fdbr = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(rootPath).child(msg.get(i).getMessageID());
                                final Message msgvar = msg.get(i);
                                msgvar.setMessageStatus(1);
                                fdbr.updateChildren(msgvar.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mydb.insertMessage(msgvar.getMessage(), msgvar.getMessageSource(), msgvar.getMessageSentTime(), 1, msgvar.getMessageID(), getRoot(msgvar.getMessageFor(), msgvar.getMessageSource()), msgvar.getMessageFor());
                                        messages.clear();
                                        try {
                                            messages.addAll(mydb.getAllMessages(rootPath));
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                                pnt = false;
                            }


                            msg = mydb.getAllMessagesByStatus(rootPath, 2, receiverUsername);

                            n = msg.size();

                            for (int i = 0; i < n; i++) {
                                DatabaseReference fdbr = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(rootPath).child(msg.get(i).getMessageID());
                                Message msgvar = msg.get(i);
                                msgvar.setMessageStatus(3);
                                fdbr.updateChildren(msgvar.toMap());
                                pnt = false;
                            }


                            msg = mydb.getAllMessagesByStatus(rootPath, 3, senderUserName);

                            n = msg.size();
                            Message lastMessage = null;

                            lastMessage = mydb.getlastMessages(rootPath);

                            for (int i = 0; i < n; i++) {
                                if (!lastMessage.getMessageID().equals(msg.get(i).getMessageID())) {
                                    FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(rootPath).child(msg.get(i).getMessageID()).removeValue();
                                }
                                pnt = false;
                            }
                            if (messages.size() != 0 && mydb.getlastMessages(rootPath).getMessageSource().equals(receiverUsername)) {
                                mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MAINVIEW_CHILD).child(receiverUsername).child(senderUserName);
//                                User user = new User(senderUserName, Calendar.getInstance(Locale.ENGLISH).getTime(), mydb.getlastMessages(rootPath).getMessage(), "no", 3);
                                mFirebaseDatabaseReference.setValue(new connDetail(true));
                                pnt = false;
                            }

//                            messages.addAll(mydb.getAllMessages(rootPath));

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }

                }).start();
                if (pnt) {
                    pnt = true;
                    try {
                        messages.clear();
                        messages.addAll(mydb.getAllMessages(rootPath));
//                        rvMessages.scrollToPosition(messages.size() - 1);

//                        adapter.notifyDataSetChanged();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFinish() {
                cdt.start();
            }
        };

        cdt.cancel();
        cdt.start();

        networkStateReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                boolean connectivity = isNetworkAvailable();
                TextView netWorkStatus = findViewById(R.id.networkStatus);
                if (connectivity) {
                    if (isOnline()) {
                        netWorkStatus.setText("Online");
                    } else {
                        netWorkStatus.setText("Your Connection may Not Working");
                    }
                } else {
                    netWorkStatus.setText("Turn On Network Connection");
                }

            }
        };


        IntentFilter filter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        try {
            registerReceiver(networkStateReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        messageRef = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(rootPath);

        messageRef.addValueEventListener(v3);


        userName.setText(mydb.getUserName(receiverUsername));

        mMessageBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && !mMessageBox.getText().toString().equals("")) {
                    mMessageBoxCloseButton.setVisibility(View.VISIBLE);
                } else {
                    mMessageBoxCloseButton.setVisibility(View.GONE);
                }
            }
        });

//        Sence Text Changes and Control it
        mMessageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mMessageBox.getText().toString().trim().equals("") && sendButtonStatus) {
                    sendButtonStatus = false;
                    mMessageBox.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    mMessageSendButton.setEnabled(false);
                } else if (!mMessageBox.getText().toString().trim().equals("") && !sendButtonStatus) {
                    sendButtonStatus = true;
                    mMessageBox.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    mMessageSendButton.setVisibility(View.VISIBLE);
                    mMessageBoxCloseButton.setVisibility(View.VISIBLE);
                    mMessageSendButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

//       Typing Or Not Check
        constraintLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                constraintLayout.getWindowVisibleDisplayFrame(r);
                int screenHeight = constraintLayout.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;
                if (keypadHeight > screenHeight * 0.15) {
                    Log.d("Typing", "ON");
                } else {
                    Log.d("Typing", "OFF");
                }
            }
        });


        // Attach the adapter to the recyclerview to populate items
        rvMessages.setAdapter(adapter);
        // Set layout manager to position the items
        rvMessages.setLayoutManager(new LinearLayoutManager(this));

//        adapter.notifyDataSetChanged();
//        if (messages.size() != 0) {
//            try {
//                rvMessages.scrollToPosition(messages.size() - mydb.getAllMessagesByStatus(rootPath, 2, receiverUsername).size() - 1);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//        }

        try {
            messages.clear();
            messages.addAll(mydb.getAllMessages(rootPath));
            adapter.notifyDataSetChanged();
            rvMessages.scrollToPosition(messages.size() - 1);
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }


    //    Listen Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menuButton:
                findViewById(R.id.splashImage).setVisibility(View.VISIBLE);
                menuButtonStatus = false;
                findViewById(R.id.messageBoxContainer).animate().translationY(300);
//                menuLayout.setTranslationX(300);
//                menuLayout.setTranslationY(-300);
                menuLayout.setVisibility(View.VISIBLE);
//                menuLayout.animate().translationX(0);
//                menuLayout.animate().translationY(0);
                mMenuButton.setVisibility(View.GONE);
                mMenuButtonClose.setVisibility(View.VISIBLE);
                break;
            case R.id.menuClose:
                findViewById(R.id.splashImage).setVisibility(View.INVISIBLE);
                menuButtonStatus = true;
                findViewById(R.id.messageBoxContainer).animate().translationY(0);
//                menuLayout.animate().translationX(300);
//                menuLayout.animate().translationY(-300);
                menuLayout.setVisibility(View.GONE);
                mMenuButtonClose.setVisibility(View.GONE);
                mMenuButton.setVisibility(View.VISIBLE);
                break;
            case R.id.deleteButton:
                respData.selection = false;
                for (final String st : respData.delItem) {
                    FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(getRoot(senderUserName, receiverUsername)).child(st).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mydb.deleteMessage(st);
                            messages.clear();
                            try {
                                messages.addAll(mydb.getAllMessages(rootPath));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mydb.deleteMessage(st);
                            messages.clear();
                            try {
                                messages.addAll(mydb.getAllMessages(rootPath));
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
                respData.delItem= new ArrayList<>();
                messages.clear();
                try {
                    messages.addAll(mydb.getAllMessages(rootPath));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
                mDeleteButton.setVisibility(View.GONE);
                mMenuButton.setVisibility(View.VISIBLE);
                findViewById(R.id.selectAllBoxContainer).setVisibility(View.GONE);
                findViewById(R.id.userSpace).setVisibility(View.VISIBLE);
                break;
            case R.id.sendButton:
//                Updating Message
                final String messageKey;
                messageKey = getRandString(15);
                final Message message = new Message(encrypt(mMessageBox.getText().toString().trim()), senderUserName, Calendar.getInstance(Locale.ENGLISH).getTime(), 1, messageKey, receiverUsername);
                mMessageBox.getText().clear();
                mydb.insertMessage(message.getMessage(), message.getMessageSource(), message.getMessageSentTime(), 0, message.getMessageID(), getRoot(message.getMessageSource(), message.getMessageFor()), message.getMessageFor());

                messages.clear();
                try {
                    messages.addAll(mydb.getAllMessages(rootPath));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();


                FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(getRoot(message.getMessageSource(), message.getMessageFor()))
                        .child(message.getMessageID()).updateChildren(message.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mydb.insertMessage(message.getMessage(), message.getMessageSource(), message.getMessageSentTime(), 1, message.getMessageID(), getRoot(message.getMessageSource(), message.getMessageFor()), message.getMessageFor());
                        messages.clear();
                        try {
                            messages.addAll(mydb.getAllMessages(rootPath));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        adapter.notifyDataSetChanged();
                    }
                });


//                Updating The Data base

//                Updating History Of Sender
                FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MAINVIEW_CHILD).child(message.getMessageSource()).child(message.getMessageFor()).setValue(new connDetail(true));

//                Updating History Of Reciver
                FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MAINVIEW_CHILD).child(message.getMessageFor()).child(message.getMessageSource()).setValue(new connDetail(true));

                break;
            case R.id.profileButton_message:
                findViewById(R.id.userSpace).performClick();
                mMenuButtonClose.performClick();
            case R.id.messageBox_closeButton:
                mMessageBox.getText().clear();
                break;
            case R.id.userSpace:
                Intent i = new Intent(getApplicationContext(), profileCard.class);
                i.putExtra("phone", receiverUsername);
                startActivity(i);
                break;
            case R.id.rootLayout:
            case R.id.splashImage:
                if (!menuButtonStatus) {
                    mMenuButtonClose.performClick();
                }
                break;
            case R.id.scrollbutton:
                try {
                    rvMessages.smoothScrollToPosition(mydb.getAllMessages(rootPath).size() - 1);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (respData.selection) {
            respData.selection = false;
            respData.delItem = new ArrayList<>();
            adapter.notifyDataSetChanged();
            mDeleteButton.setVisibility(View.GONE);
            mMenuButton.setVisibility(View.VISIBLE);
            findViewById(R.id.selectAllBoxContainer).setVisibility(View.GONE);
            findViewById(R.id.userSpace).setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cdt.cancel();
    }

    ArrayList<Message> msg = null;

}
