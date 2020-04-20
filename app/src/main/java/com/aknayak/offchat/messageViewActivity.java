package com.aknayak.offchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.aknayak.offchat.datas.DBHelper;
import com.aknayak.offchat.messages.Message;
import com.aknayak.offchat.messages.MessageAdapter;
import com.aknayak.offchat.users.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Console;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.aknayak.offchat.MainActivity.ROOT_CHILD;
import static com.aknayak.offchat.MainActivity.getRandString;
import static com.aknayak.offchat.MainActivity.getRoot;
import static com.aknayak.offchat.MainActivity.receiverUsername;
import static com.aknayak.offchat.MainActivity.senderUserName;

public class messageViewActivity extends AppCompatActivity implements View.OnClickListener{
    EditText mMessageBox;
    ImageButton mMessageSendButton;
    ImageButton mMessageBoxCloseButton;
    ImageButton mMenuButton;
    ImageButton mMenuButtonClose;
    ConstraintLayout menuLayout;
    TextView mProfileButton;
    TextView userName;
    TextView onlineStatusTextView;
    RecyclerView rvMessages;
    ConstraintLayout constraintLayout;
    Date lastSeenTime;
    boolean menuButtonStatus=true;
    public ArrayList<Message> messages = new ArrayList<Message>();
    final MessageAdapter adapter = new MessageAdapter(messages,this);

    Boolean sendButtonStatus=false;

    private DatabaseReference mFirebaseDatabaseReference;
    private DatabaseReference o_status;
    private DatabaseReference historyRef;
    private DatabaseReference messageRef;
    private ValueEventListener v1;
    private ValueEventListener v2;
    private ValueEventListener v3;
    private ValueEventListener v4;

    public static String MESSAGES_CHILD = "messages";
    public static String MAINVIEW_CHILD ="history";
    public String rootPath;
    BroadcastReceiver networkStateReceiver;
    DBHelper mydb;

    int cnt=0;
    public Boolean isOnline(){
        try {
            Process p1= java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal==0);
            return reachable;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfoListener= connectivityManager.getActiveNetworkInfo();
        return networkInfoListener != null && networkInfoListener.isConnectedOrConnecting();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkStateReceiver);
    }


    CountDownTimer cdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_view);

        receiverUsername = getIntent().getStringExtra("phoneNumber");
        rootPath = getRoot(senderUserName,receiverUsername);

        //        Initialize All the elements of the screen
        mMessageBoxCloseButton = findViewById(R.id.messageBox_closeButton);
        rvMessages = findViewById(R.id.messageView);
        mMessageSendButton = findViewById(R.id.sendButton);
        mMenuButton=findViewById(R.id.menuButton);
        mMenuButtonClose =findViewById(R.id.menuClose);
        menuLayout=findViewById(R.id.menuLayout);
        mProfileButton=findViewById(R.id.profileButton_message);
        mMessageBox = findViewById(R.id.messageBox);
        constraintLayout = findViewById(R.id.rootLayout);
        userName=findViewById(R.id.msgUserName);
        onlineStatusTextView =findViewById(R.id.onlineStatusTextView_in_MessageView);

//        Add All the components into OnClickListner
        findViewById(R.id.splashImage).setOnClickListener(this);
        constraintLayout.setOnClickListener(this);
        mMenuButton.setOnClickListener(this);
        mMenuButtonClose.setOnClickListener(this);
        mProfileButton.setOnClickListener(this);
        mMessageSendButton.setOnClickListener(this);
        mMessageBoxCloseButton.setOnClickListener(this);
        findViewById(R.id.userSpace).setOnClickListener(this);
        findViewById(R.id.messageView).setOnClickListener(this);


        if(receiverUsername.equals("+1")){
            findViewById(R.id.messageBoxContainer).setVisibility(View.GONE);
        }


//        Refrences
        o_status = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child("online_status").child(receiverUsername).child("online_status");
        historyRef = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child("online_status").child(senderUserName);



//        Value Event Listners
        v1 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Date currentTime;
                currentTime= Calendar.getInstance(Locale.ENGLISH).getTime();
                long diff = 0;
                Date date = dataSnapshot.getValue(Date.class);
                if (date == null){
                    historyRef.child("online_status").setValue(Calendar.getInstance(Locale.ENGLISH).getTime());
                }else {
                    lastSeenTime = dataSnapshot.getValue(Date.class);
                    diff =currentTime.getTime() - lastSeenTime.getTime();
                    long diffSeconds = diff / 1000 % 60;
                    if(diffSeconds>30){
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
                if (date != null){
                    Date currentTime = Calendar.getInstance(Locale.ENGLISH).getTime();
                    long diff = 0;

                    diff = currentTime.getTime() - date.getTime();
                    long diffSeconds = diff / 1000 % 60;
                    long diffMinutes = diff / (60 * 1000) % 60;
//                    long diffHours = diff / (60 * 60 * 1000);
//                    System.out.println("Time in seconds: " + diffSeconds + " seconds.");
//                    System.out.println("Time in minutes: " + diffMinutes + " minutes.");
//                    System.out.println("Time in hours: " + diffHours + " hours.");
                    onlineStatusTextView.setVisibility(View.VISIBLE);
                    if(diffMinutes<1){
                        onlineStatusTextView.setText("online");
                    }else {
                        long dayCheck =date.getDay()-currentTime.getDay();
                        String str;
                        if (dayCheck==0){
                            SimpleDateFormat sf= new SimpleDateFormat("hh:mm aa");
                            str=sf.format(date);
                            str="today "+str;
                        }else if(dayCheck==1) {
                            SimpleDateFormat sf= new SimpleDateFormat("hh:mm aa");
                            str=sf.format(date);
                            str ="yesterday "+str;
                        }else {
                            SimpleDateFormat sf= new SimpleDateFormat("EEEE MMM dd  hh:mm aa");
                            str=sf.format(date);
                        }
//                                Toast.makeText(getApplicationContext(),""+dayCheck,Toast.LENGTH_SHORT).show();
                        String str2 = str.replace("AM", "am").replace("PM","pm");
                        str2="last seen "+str2;
                        onlineStatusTextView.setText(str2);
                    }
                }else {
                    onlineStatusTextView.setVisibility(View.VISIBLE);
                    onlineStatusTextView.setText("Never Used");
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
//                    if (dataSnapshot.getChildrenCount()!=mydb.getAllMessages(rootPath).size()){
                        Log.d("Abdhesh","bb");
                        messages.clear();
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            Message message = snapshot.getValue(Message.class);
//                    Log.d("MMM",message.getMessage());
                            mydb.insertMessage(message.getMessage(),message.getMessageSource(),message.getMessageSentTime(),message.getMessageStatus(),snapshot.getKey(),rootPath);
                        }
                        messages.addAll(mydb.getAllMessages(rootPath));
                        adapter.notifyDataSetChanged();
//                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (messages.size()!=0) {
                    try {
                        if (cnt!=mydb.getAllMessages(rootPath).size()) {
                            cnt = mydb.getAllMessages(rootPath).size();
                            rvMessages.scrollToPosition(messages.size()-1);
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
        cdt = new CountDownTimer(60000, 60000) {
            @Override
            public void onTick(long millisUntilFinished) {
                o_status.addListenerForSingleValueEvent(v2);

                try {
                    msg= mydb.getAllMessagesByStatus(rootPath,2,receiverUsername);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int n = msg.size();

                        for (int i=0;i<n;i++){
                            DatabaseReference fdbr = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(rootPath).child(msg.get(i).getMessageID());
                            Message msgvar=msg.get(i);
                            msgvar.setMessageStatus(3);
                            fdbr.updateChildren(msgvar.toMap());
//                            FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(rootPath).child(msg.get(i).getMessageID()).removeValue();
                        }

                        try {
                            msg=mydb.getAllMessagesByStatus(rootPath,3,senderUserName);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        n = msg.size();
                        Message lastMessage = null;
                        try {
                            lastMessage= mydb.getlastMessages(rootPath);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        Log.d("KKKK"," "+n);
                        for (int i =0;i<n;i++){
                            if (!lastMessage.getMessageID().equals(msg.get(i).getMessageID())){
                                FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(rootPath).child(msg.get(i).getMessageID()).removeValue();
                            }
                        }
                        try {
                            if (messages.size()!=0 && mydb.getlastMessages(rootPath).getMessageSource().equals(receiverUsername)){
                                Log.d("MMM",mydb.getlastMessages(rootPath).getMessage());
                                mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MAINVIEW_CHILD).child(receiverUsername).child(senderUserName);
                                User user = new User(senderUserName,Calendar.getInstance(Locale.ENGLISH).getTime(),mydb.getlastMessages(rootPath).getMessage(),"no",3);
                                mFirebaseDatabaseReference.updateChildren(user.toMap());
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
//                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFinish() {
//                Toast.makeText(getApplicationContext(),"finished",Toast.LENGTH_SHORT).show();
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
                    if (isOnline()){
                        netWorkStatus.setText("Online");
                    }else {
                        netWorkStatus.setText("Your Connection may Not Working");
                    }
                } else {
                    netWorkStatus.setText("Turn On Network Connection");
                }

            }
        };

        mydb = new DBHelper(getApplicationContext());

        try {
//            Log.d("kk",rootPath);
            messages.addAll(mydb.getAllMessages(rootPath));
            if (messages.size()>0){
                rvMessages.scrollToPosition(messages.size()-1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        IntentFilter filter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        try {
            registerReceiver(networkStateReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        messageRef = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(rootPath);
        new Thread(new Runnable() {
            @Override
            public void run() {
                messageRef.addValueEventListener(v3);
            }
        }).start();


        userName.setText(mydb.getUserName(receiverUsername));

        mMessageBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && !mMessageBox.getText().toString().equals("")) {
                    mMessageBoxCloseButton.setVisibility(View.VISIBLE);
                }
                else {
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
                if(mMessageBox.getText().toString().trim().equals("") && sendButtonStatus){
                    sendButtonStatus=false;
                    mMessageBox.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//                    mMessageSendButton.animate().translationY(-100);
                    mMessageSendButton.setEnabled(false);
                }else if(!mMessageBox.getText().toString().trim().equals("") && !sendButtonStatus){
                    sendButtonStatus=true;
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
                    Log.d("Typing","ON");
                } else {
                    Log.d("Typing","OFF");
                }
            }
        });


        // Attach the adapter to the recyclerview to populate items
        rvMessages.setAdapter(adapter);
        // Set layout manager to position the items
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        // That's all!
        adapter.notifyDataSetChanged();
        if (messages.size()!=0) {
            try {
                rvMessages.scrollToPosition(messages.size() - mydb.getAllMessagesByStatus(rootPath,2,receiverUsername).size()-1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }


//    Listen Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.menuButton:
                findViewById(R.id.splashImage).setVisibility(View.VISIBLE);
                menuButtonStatus=false;
//                findViewById(R.id.messageBoxContainer).setVisibility(View.GONE);
                findViewById(R.id.messageBoxContainer).animate().translationY(300);
                menuLayout.setTranslationY(-300);
                menuLayout.setVisibility(View.VISIBLE);
                menuLayout.animate().translationY(0);
                mMenuButton.setVisibility(View.GONE);
                mMenuButtonClose.setVisibility(View.VISIBLE);
                break;
            case R.id.menuClose:
                findViewById(R.id.splashImage).setVisibility(View.INVISIBLE);
                menuButtonStatus=true;
                findViewById(R.id.messageBoxContainer).animate().translationY(0);
                menuLayout.animate().translationY(-300);
                menuLayout.setVisibility(View.GONE);
                mMenuButtonClose.setVisibility(View.GONE);
                mMenuButton.setVisibility(View.VISIBLE);
                break;
            case R.id.sendButton:
//                Updating Message
                String messageKey;
                messageKey=getRandString(15);
                final Message message = new Message(mMessageBox.getText().toString().trim(), senderUserName,1,messageKey);
                mMessageBox.getText().clear();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(getRoot(senderUserName,receiverUsername))
                                .child(message.getMessageID()).updateChildren(message.toMap(), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if (databaseError!=null){
                                    Log.d("MMM","Failed");
                                    mydb.insertMessage(message.getMessage(),message.getMessageSource(),message.getMessageSentTime(),0,message.getMessageID(),getRoot(senderUserName,receiverUsername));
                                }else {
                                    mydb.insertMessage(message.getMessage(),message.getMessageSource(),message.getMessageSentTime(),1,message.getMessageID(),getRoot(senderUserName,receiverUsername));
                                }
                            }
                        });

                        messages.add(message);

//                Updating The Data base

//                Updating History Of Sender
                        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MAINVIEW_CHILD).child(senderUserName).child(receiverUsername);

                        User user = new User(receiverUsername,Calendar.getInstance(Locale.ENGLISH).getTime(),message.getMessage(),"no",1);

                        mFirebaseDatabaseReference.updateChildren(user.toMap());

//                Updating History Of Reciver
                        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MAINVIEW_CHILD).child(receiverUsername).child(senderUserName);
                        user = new User(senderUserName,Calendar.getInstance(Locale.ENGLISH).getTime(),message.getMessage(),"no",0);

                        mFirebaseDatabaseReference.updateChildren(user.toMap());

                    }
                }).start();

//                Clearing MessageBox And moving the scroll positiion to he last message

                mydb.insertMessage(message.getMessage(),message.getMessageSource(),message.getMessageSentTime(),0,message.getMessageID(),getRoot(senderUserName,receiverUsername));

                rvMessages.scrollToPosition(messages.size());

                break;
            case R.id.profileButton_message:
                findViewById(R.id.userSpace).performClick();
                mMenuButtonClose.performClick();
            case R.id.messageBox_closeButton:
                mMessageBox.getText().clear();
                break;
            case R.id.userSpace:
                Intent i = new Intent(getApplicationContext(),profileCard.class);
                i.putExtra("phone",receiverUsername);
                startActivity(i);
//                Toast.makeText(getApplicationContext(),"UserName : "+mydb.getUserName(receiverUsername)+"\nPhone Number : "+ receiverUsername,Toast.LENGTH_LONG).show();
                break;
            case R.id.rootLayout:
            case R.id.splashImage:
                if(!menuButtonStatus){
                    mMenuButtonClose.performClick();
                }
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        messageRef.removeEventListener(v3);
        super.onPause();
        cdt.cancel();
    }


    ArrayList<Message> msg=null;

}
