package com.aknayak.offchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.aknayak.offchat.datas.DBHelper;
import com.aknayak.offchat.globaldata.respData;
import com.aknayak.offchat.messages.Message;
import com.aknayak.offchat.messages.MessageAdapter;
import com.aknayak.offchat.swipetoreply.ISwipeControllerActions;
import com.aknayak.offchat.swipetoreply.SwipeController;
import com.aknayak.offchat.users.connDetail;
import com.aknayak.offchat.users.typingDetails;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.aknayak.offchat.globaldata.Constants.PREF_DATA;
import static com.aknayak.offchat.globaldata.Constants.ROOT_CHILD;
import static com.aknayak.offchat.globaldata.AESHelper.decrypt;
import static com.aknayak.offchat.globaldata.AESHelper.encrypt;
import static com.aknayak.offchat.globaldata.respData.MAINVIEW_CHILD;
import static com.aknayak.offchat.globaldata.respData.MESSAGES_CHILD;
import static com.aknayak.offchat.globaldata.respData.TYPING_CHILD;
import static com.aknayak.offchat.globaldata.respData.getRandString;
import static com.aknayak.offchat.globaldata.respData.getRoot;
import static com.aknayak.offchat.globaldata.respData.isOnline;
import static com.aknayak.offchat.globaldata.respData.playSound;
import static com.aknayak.offchat.globaldata.respData.receiverUsername;
import static com.aknayak.offchat.globaldata.respData.senderUserName;
import static com.aknayak.offchat.globaldata.respData.showAds;
import static com.aknayak.offchat.globaldata.respData.sound_sent;
import static com.aknayak.offchat.globaldata.respData.sound_waiting;
import static com.aknayak.offchat.globaldata.respData.tdtls;
import static com.aknayak.offchat.globaldata.respData.userStatus;

/**
 * OffChat
 * Created by Abdhesh Nayak on 3/18/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/


public class messageViewActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean PHONE_STATUS;

    TextView messageLenShow;
    private TextView replyUserName;
    private TextView replyTextMessage;
    private Message replyMessage;
    ImageButton imageButtoncloseReply;
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
    public String rootPath;
    BroadcastReceiver networkStateReceiver;
    DBHelper mydb;
    int cnt = 0;
    boolean flg = false;
    AdView adView;

    CountDownTimer cdt;
    boolean pnt = true;
    ArrayList<Message> msg = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_view);

        replyMessage = new Message();
        adView = findViewById(R.id.adView);

        respData.delItem = new ArrayList<>();
        receiverUsername = getIntent().getStringExtra("phoneNumber");
        respData.temp = receiverUsername;
        rootPath = getRoot(senderUserName, receiverUsername);

        //        Initialize All the elements of the screen
        messageLenShow = findViewById(R.id.letterCount);
        imageButtoncloseReply = findViewById(R.id.imageButtonreplyClose);
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
        replyUserName = findViewById(R.id.replyUserName);
        replyTextMessage = findViewById(R.id.replyTextview);

        onlineStatusTextView = findViewById(R.id.onlineStatusTextView_in_MessageView);


        findViewById(R.id.userModinfo).setOnClickListener(this);
//        Add All the components into OnClickListner
        imageButtoncloseReply.setOnClickListener(this);
        findViewById(R.id.onoffinfo).setOnClickListener(this);
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


        mydb = new DBHelper(getApplicationContext());
        if (showAds) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.setVisibility(View.VISIBLE);
            adView.loadAd(adRequest);
        }

        SwipeController swipeController = new SwipeController(getApplicationContext(), new ISwipeControllerActions() {
            @Override
            public void onSwipePerformed(int position) {
                findViewById(R.id.replyLayout).animate().translationX(0);
                findViewById(R.id.replyLayout).setVisibility(View.VISIBLE);
                replyMessage = messages.get(position);
                replyUserName.setText(replyMessage.getMessageSource().equals(senderUserName) ? "You" : replyMessage.getMessageSource());
                replyTextMessage.setText(decrypt(replyMessage.getMsgBody()));
            }
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeController);
        itemTouchHelper.attachToRecyclerView(rvMessages);


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
                    long diffSeconds = diff / 1000;
                    if (diffSeconds > 15) {
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
                onlineStatusTextView.setVisibility(View.VISIBLE);
                historyRef.child("online_status").addListenerForSingleValueEvent(v1);
                Date date = dataSnapshot.getValue(Date.class);
                if (date != null) {
                    Date currentTime = Calendar.getInstance(Locale.ENGLISH).getTime();
                    long diff = 0;
                    diff = currentTime.getTime() - date.getTime();
                    long diffSeconds = diff / 1000;
                    if (diffSeconds < 20) {
//                        Toast.makeText(getApplicationContext(),""+diffSeconds,Toast.LENGTH_SHORT).show();
                        FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(TYPING_CHILD).child(senderUserName).child(receiverUsername).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                typingDetails tpDetail = dataSnapshot.getValue(typingDetails.class);
                                if (tpDetail != null && tpDetail.isTyping() && tpDetail.getTime() != null) {
                                    Date currentTime = Calendar.getInstance(Locale.ENGLISH).getTime();
                                    long diff = 0;

                                    diff = currentTime.getTime() - tpDetail.getTime().getTime();
                                    long diffSeconds2 = diff / 1000;
                                    if (diffSeconds2 < 10) {
                                        onlineStatusTextView.setText("Typing...");
                                    } else {
                                        userStatus = "online";
                                    }
                                } else {
                                    userStatus = "online";
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);

                        int dayCheck = Double.valueOf(simpleDateFormat.format(currentTime.getTime())).intValue() - Double.valueOf(simpleDateFormat.format(date.getTime())).intValue();

                        String str;
                        if (dayCheck == 0) {
                            SimpleDateFormat sf = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
                            str = sf.format(date);
                        } else if (dayCheck == 1) {
                            SimpleDateFormat sf = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
                            str = sf.format(date);
                            str = "Yesterday " + str;
                        } else {
                            SimpleDateFormat sf = new SimpleDateFormat("EEE MMM dd  hh:mm aa", Locale.ENGLISH);
                            str = sf.format(date);
                        }
                        userStatus = "last seen " + str;
                    }
                } else {
                    userStatus = "Not on OffChat";
                }
                onlineStatusTextView.setText(userStatus);
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
                        if ((message != null && message.getMessageSource().equals(receiverUsername)) || message.getMessageStatus() != 1) {
                            Log.d("UUUU", "kl");
                            mydb.insertMessage(message.getMsgBody(), message.getMessageSource(), message.getMessageSentTime(), message.getMessageStatus(), snapshot.getKey(), rootPath, message.getMessageFor(), message.getReplyId());
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


        networkStateReceiver = new BroadcastReceiver() {


            @Override
            public void onReceive(Context context, Intent intent) {
                boolean connectivity = respData.isNetworkAvailable(messageViewActivity.this);
                TextView online = findViewById(R.id.userOnline);
                TextView offline = findViewById(R.id.userOffline);
//                TextView useStatus = findViewById(R.id.userStatusTextView);
                if (connectivity) {
                    if (isOnline()) {
                        online.setVisibility(View.VISIBLE);
                        offline.setVisibility(View.INVISIBLE);
                        findViewById(R.id.aceptOffline).setVisibility(View.INVISIBLE);
                        PHONE_STATUS=true;
                        OFFLINE_SEND=false;
//                        useStatus.setText("You are in online mode.");
                    } else {
                        offline.setVisibility(View.VISIBLE);
                        online.setVisibility(View.GONE);
                        findViewById(R.id.aceptOffline).setVisibility(View.VISIBLE);
                        PHONE_STATUS=false;
//                        useStatus.setText("Your Connection may Not Working\nYou Are in Offline Mode");
                    }
                } else {
                    offline.setVisibility(View.VISIBLE);
                    online.setVisibility(View.GONE);
                    findViewById(R.id.aceptOffline).setVisibility(View.VISIBLE);
                    PHONE_STATUS=false;
//                    useStatus.setText("You Are in Offline Mode");
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
                if (mMessageBox.getLineCount()>1){
                    messageLenShow.setText("120/"+mMessageBox.getText().length());
                    messageLenShow.setVisibility(View.VISIBLE);
                }else {
                    messageLenShow.setVisibility(View.INVISIBLE);
                }
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
                    if (tdtls.isTyping() != true) {
                        tdtls = new typingDetails(true, Calendar.getInstance(Locale.ENGLISH).getTime());
                        FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(TYPING_CHILD).child(receiverUsername).child(senderUserName).updateChildren(new typingDetails(true, tdtls.getTime()).toMap());
                    } else {
                        long diff = Calendar.getInstance(Locale.ENGLISH).getTime().getTime() - tdtls.getTime().getTime();
                        long diffSeconds = diff / 1000;
                        if (diffSeconds > 5) {
                            tdtls = new typingDetails(true, Calendar.getInstance(Locale.ENGLISH).getTime());
                            FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(TYPING_CHILD).child(receiverUsername).child(senderUserName).updateChildren(new typingDetails(true, tdtls.getTime()).toMap());
                        }
                    }
                } else {
                    if (tdtls.isTyping() != false) {
                        tdtls = new typingDetails(false, Calendar.getInstance(Locale.ENGLISH).getTime());
                        FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(TYPING_CHILD).child(receiverUsername).child(senderUserName).updateChildren(new typingDetails(false, tdtls.getTime()).toMap());
                    } else {
                        long diff = Calendar.getInstance(Locale.ENGLISH).getTime().getTime() - tdtls.getTime().getTime();
                        long diffSeconds = diff / 1000;
                        if (diffSeconds > 5) {
                            tdtls = new typingDetails(false, Calendar.getInstance(Locale.ENGLISH).getTime());
                            FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(TYPING_CHILD).child(receiverUsername).child(senderUserName).updateChildren(new typingDetails(false, tdtls.getTime()).toMap());
                        }
                    }
                }
            }
        });


        // Attach the adapter to the recyclerview to populate items
        rvMessages.setAdapter(adapter);
        // Set layout manager to position the items
        rvMessages.setLayoutManager(new LinearLayoutManager(this));


        try {
            messages.clear();
            messages.addAll(mydb.getAllMessages(rootPath));
            adapter.notifyDataSetChanged();
            rvMessages.scrollToPosition(messages.size() - 1);
        } catch (ParseException e) {
            e.printStackTrace();
        }

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
                                        mydb.insertMessage(msgvar.getMsgBody(), msgvar.getMessageSource(), msgvar.getMessageSentTime(), 1, msgvar.getMessageID(), getRoot(msgvar.getMessageFor(), msgvar.getMessageSource()), msgvar.getMessageFor(), msgvar.getReplyId());
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


        CheckBox cb;
        cb = findViewById(R.id.oflineAceptCheckBox);

        SharedPreferences sharedPreferences = getSharedPreferences(PREF_DATA,Context.MODE_PRIVATE);
        if (sharedPreferences.contains("off_permission")){
            cb.setChecked(sharedPreferences.getBoolean("off_permission",false));
        }

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                OFFLINE_SEND=isChecked;
                SharedPreferences sharedPreferences = getSharedPreferences(PREF_DATA,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("off_permission",isChecked);
                editor.apply();
                Toast.makeText(getApplicationContext(),""+isChecked,Toast.LENGTH_SHORT).show();
            }
        });

        selectAllcheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    respData.delItem = new ArrayList<>();
                    for (Message msg : messages) {
                        respData.delItem.add(msg.getMessageID());
                    }
                    refreshSelectCount();
                    adapter.notifyDataSetChanged();
                }
            }
        });

    }

    private Object OFFLINE_SEND;

    //    Listen Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageButtonreplyClose:
                findViewById(R.id.replyLayout).animate().translationX(-20000);
                findViewById(R.id.replyLayout).setVisibility(View.GONE);
                replyMessage = new Message();
                break;
            case R.id.userModinfo:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(messageViewActivity.this);
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialogBuilder.setTitle("Red/Green Indicator");
                alertDialogBuilder.setMessage("The Indicator shows internet Connectivity of your phone.");
                alertDialogBuilder.show();

                break;
            case R.id.onoffinfo:
                AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(messageViewActivity.this);
                alertDialogBuilder2.setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialogBuilder2.setCancelable(false);
                alertDialogBuilder2.setTitle("Agree ??[this feature available now]");
                alertDialogBuilder2.setMessage("By tick the box you agree to send SMS message from your phone using your sim card. Standard local charge will be detucted from your main balance. SMS will be charged same as your national SMS charge.");
                alertDialogBuilder2.show();

                break;
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
                playSound(messageViewActivity.this, sound_sent);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
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
//                            adapter.notifyDataSetChanged();
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
//                            adapter.notifyDataSetChanged();
                                }
                            });
                        }
                        respData.delItem = new ArrayList<>();
                        messages.clear();
                        try {
                            messages.addAll(mydb.getAllMessages(rootPath));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }


                    }
                }).start();

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
                final Message message = new Message(encrypt(mMessageBox.getText().toString().trim()), senderUserName, Calendar.getInstance(Locale.ENGLISH).getTime(), 1, messageKey, receiverUsername, replyMessage.getMessageID());
                mMessageBox.getText().clear();
                mydb.insertMessage(message.getMsgBody(), message.getMessageSource(), message.getMessageSentTime(), 0, message.getMessageID(), getRoot(message.getMessageSource(), message.getMessageFor()), message.getMessageFor(), message.getReplyId());

                messages.clear();
                try {
                    messages.addAll(mydb.getAllMessages(rootPath));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                findViewById(R.id.replyLayout).setVisibility(View.GONE);
                replyMessage = new Message();
                adapter.notifyDataSetChanged();
                rvMessages.smoothScrollToPosition(messages.size() - 1);
                playSound(messageViewActivity.this, sound_waiting);


                FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(getRoot(message.getMessageSource(), message.getMessageFor()))
                        .child(message.getMessageID()).updateChildren(message.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mydb.insertMessage(message.getMsgBody(), message.getMessageSource(), message.getMessageSentTime(), 1, message.getMessageID(), getRoot(message.getMessageSource(), message.getMessageFor()), message.getMessageFor(), message.getReplyId());
                        playSound(messageViewActivity.this, sound_sent);
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
            respData.temp = null;
            super.onBackPressed();
        }
    }


    public void dellButton() {
        mDeleteButton.setVisibility(View.VISIBLE);
        mMenuButton.setVisibility(View.GONE);
        findViewById(R.id.userSpace).setVisibility(View.GONE);
        findViewById(R.id.selectAllBoxContainer).setVisibility(View.VISIBLE);
    }

    public void refreshSelectCount() {
        Log.d("sel", "kk");
        selectCount.setText(respData.delItem.size() + "/" + messages.size());
        if (respData.delItem.size() != messages.size()) {
            selectAllcheckBox.setChecked(false);
        } else if (respData.delItem.size() == messages.size()) {
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
                }, 3 * 1000); // wait for 5 seconds
            }
        } else {
            scrollButton.setVisibility(View.INVISIBLE);
            flg = false;
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkStateReceiver);
    }


    @Override
    protected void onPause() {
        super.onPause();
        respData.temp = null;
        cdt.cancel();
    }

    @Override
    protected void onResume() {
        messageRef.addValueEventListener(v3);
        respData.temp = receiverUsername;
        super.onResume();
    }

    public void scrollTo(String msgId) throws ParseException {
        ArrayList<String> st = mydb.getAllMessagesID(rootPath);
        int i = st.indexOf(msgId);
        rvMessages.smoothScrollToPosition(i);
        adapter.notifyDataSetChanged();
    }


    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;

    public void sendSMSMessage(String phoneNo, String message) {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(),"Please Give Permission",Toast.LENGTH_SHORT).show();
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }else {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.",
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

    }

}
