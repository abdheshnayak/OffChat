package com.aknayak.offchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aknayak.offchat.datas.DBHelper;
import com.aknayak.offchat.messages.Message;
import com.aknayak.offchat.users.connDetail;
import com.aknayak.offchat.users.userAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import static com.aknayak.offchat.AllConcacts.REQUEST_READ_CONTACTS;
import static com.aknayak.offchat.messageViewActivity.MAINVIEW_CHILD;
import static com.aknayak.offchat.messageViewActivity.MESSAGES_CHILD;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public final ArrayList<Message> messages  = new ArrayList<Message>();
    private static final String TAG = "MainActivity";
    public static final String ANONYMOUS = "anonymous";
    private static final String CHANNEL_ID = "MyNotification";

    public static final String ROOT_CHILD = "UserData";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername;
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

    public static String authUser;


    public static String senderUserName;
    public static String receiverUsername;



    @Override
    protected void onResume() {
        super.onResume();
        messages.clear();
        ArrayList<Message> arrayList= null;
        try {
            arrayList=mydb.getHist();
            messages.addAll(arrayList);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
        rvUser.scrollToPosition(arrayList.size());
    }

    String notification;
    DBHelper mydb ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mydb = new DBHelper(getApplicationContext());
        setContentView(R.layout.activity_main);


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
        mSignOutButton.setOnClickListener(this);
        mMenuButton.setOnClickListener(this);
        getmMenuButtonClose.setOnClickListener(this);
        menuLayout.setOnClickListener(this);
        mImgButton.setOnClickListener(this);
        settings.setOnClickListener(this);
        aboutButton.setOnClickListener(this);


//        Check Acess for read Contacts
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(this);
        }

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


        senderUserName = mFirebaseUser.getPhoneNumber();

        authUser = mFirebaseUser.getPhoneNumber();

        createNotificationChannel();

        rvUser = findViewById(R.id.recyclerView);

        adapter = new userAdapter(messages, this);

        // Attach the adapter to the recyclerview to populate items
        rvUser.setAdapter(adapter);
//        rvUser.scrollToPosition(users.size());

        // Set layout manager to position the items
//        rvUser.setLayoutManager(new LinearLayoutManager(this));


        ArrayList<Message> msg=null;
        int n=0;
        try {
             msg= mydb.getHist();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        n=msg.size();
        for (int i = 0;i < n ;i++) {
            Log.d("BBBB"+msg.get(i).getMessageFor(),simpleDateFormat.format(msg.get(i).getMessageSentTime()));
        }

        adapter.notifyDataSetChanged();


        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
//        linearLayoutManager.setReverseLayout(true);
        rvUser.setLayoutManager(linearLayoutManager);

        try {
            if (mydb.getHist().size() == 0) {
                FirebaseDatabase.getInstance().getReference().child("admin").child("welcome_notification").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        notification = dataSnapshot.getValue(String.class);

                        Message message = new Message(notification, "+1",Calendar.getInstance(Locale.ENGLISH).getTime(), 1, getRandString(15),senderUserName);
                        FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(getRoot("+1", senderUserName))
                                .child(message.getMessageID()).updateChildren(message.toMap());

                        DatabaseReference mFirebaseRefrence = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MAINVIEW_CHILD).child(senderUserName).child("+1");


//                        Date td = Calendar.getInstance(Locale.ENGLISH).getTime();
//                        User user = new User("+1", td, notification, "no", 0);
//                        mFirebaseRefrence.updateChildren(user.toMap());
                        mFirebaseRefrence.setValue(new connDetail(true));
//                        DBHelper mydb = new DBHelper(getApplicationContext());
//                        mydb.inserthistory("+1", notification, Calendar.getInstance(Locale.ENGLISH).getTime(), 0);

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

//        History Fetcher
//        FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MAINVIEW_CHILD).child(senderUserName).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                messages.clear();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    User user = snapshot.getValue(User.class);
//                    mydb.inserthistory(user.getUserName(), user.getLastMessage(), user.getLastMessageSentTime(), user.getSentStatus());
//                }
//                try {
//                    messages.addAll(mydb.getHist());
//                    adapter.notifyDataSetChanged();
//                    rvUser.scrollToPosition(messages.size());
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        final ValueEventListener v3 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    messages.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Message message = snapshot.getValue(Message.class);
                        if ((message != null && !message.getMessageSource().equals(senderUserName)) || message.getMessageStatus() != 1) {
                            Log.d("KKK",message.getMessage());
                            mydb.insertMessage(message.getMessage(), message.getMessageSource(), message.getMessageSentTime(), message.getMessageStatus(), snapshot.getKey(),getRoot(message.getMessageFor(),message.getMessageSource()),message.getMessageFor(),"me5");
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
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    connDetail check = snapshot.getValue(connDetail.class);
                    if (check.getConnected()){
                        String user= snapshot.getKey();
                        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(getRoot(senderUserName,user));
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

        cdt = new CountDownTimer(50000, 50000) {
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

                        if (diffSeconds > 30) {
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


//        rvUser.scrollToPosition(users.size());
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void signOut() {
        new DBHelper(getApplicationContext()).deleteAllDatasOfTable();
        mAuth.signOut();
    }

    public static void requestPermission(Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.READ_CONTACTS)) {
            // show UI part if you want here to show some rationale !!!
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.READ_CONTACTS)) {
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.READ_CONTACTS},
                    REQUEST_READ_CONTACTS);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menuButtonActivityMain:
                mTouchSensors.setVisibility(View.VISIBLE);
                menuLayout.setTranslationY(-300);
                menuLayout.setVisibility(View.VISIBLE);
                menuLayout.animate().translationY(0);
                mMenuButton.setVisibility(View.GONE);
                getmMenuButtonClose.setVisibility(View.VISIBLE);
                break;
            case R.id.menuCloseActivity_main:
            case R.id.splashImageMainActivity:
                menuLayout.animate().translationY(-300);
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
                        signOut();
                        mUsername = "ANONYMOUS";
                        startActivity(new Intent(getApplicationContext(), phone_verification.class));
                        finish();
                    }
                });
                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
                Toast.makeText(getApplicationContext(), "You don't have access now.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.profileButton:

                Intent i = new Intent(getApplicationContext(), myProfile.class);
                i.putExtra("phone", senderUserName);
                startActivity(i);
                getmMenuButtonClose.performClick();
                break;
            case R.id.aboutButton:
                Intent j = new Intent(getApplicationContext(),aboutPage.class);
                startActivity(j);
                getmMenuButtonClose.performClick();
                break;
            default:
                break;
        }
    }

    public static String getRoot(String first, String second) {
//        Log.d("kk", "" + Double.valueOf(first));
        if (Double.valueOf(first) > Double.valueOf(second)) {
            return first + second;
        } else {
            return second + first;
        }
    }

    public static String filterNumber(String number) {
        String temp = "";
        for (int i = 0; i < number.length(); i++) {
            if (number.charAt(i) == '+' || number.charAt(i) == '0' || number.charAt(i) == '1' || number.charAt(i) == '2' || number.charAt(i) == '3' || number.charAt(i) == '4' || number.charAt(i) == '5' || number.charAt(i) == '6' || number.charAt(i) == '7' || number.charAt(i) == '8' || number.charAt(i) == '9') {
                temp = temp + number.charAt(i);
            } else {
                continue;
            }
        }
        if (temp.length() == 10) {
            temp = authUser.substring(0, authUser.length() - 10) + temp;
        }
        return temp;
    }


    @Override
    protected void onPause() {
        super.onPause();
        cdt.cancel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        cdt.start();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void notifyIt(int icon, String title, String message, Context context, int notificationId) {
        String GROUP_KEY_WORK_EMAIL = "com.android.example.WORK_EMAIL";

        //use constant ID for notification used as group summary
        String msg;
        if (message.length() > 300) {
            msg = message.substring(0, 300) + "...";
        } else {
            msg = message;
        }

        int SUMMARY_ID = 0;

        PendingIntent contentIntent =
                PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);

        Notification newMessageNotification =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(icon)
                        .setContentTitle(title)
                        .setContentText(msg)
                        .setGroup(GROUP_KEY_WORK_EMAIL)
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .build();


        Notification summaryNotification =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setContentTitle(title)
                        //set content text to support devices running API level < 24
                        .setContentText("")
                        .setSmallIcon(icon)
                        //build summary info into InboxStyle template
                        .setStyle(new NotificationCompat.InboxStyle()
                                .addLine("")
                                .setBigContentTitle("")
                        )
                        //specify which group this notification belongs to
                        .setGroup(GROUP_KEY_WORK_EMAIL)
                        //set this notification as the summary for the group
                        .setGroupSummary(true)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setContentIntent(contentIntent)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, newMessageNotification);
        notificationManager.notify(SUMMARY_ID, summaryNotification);


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
}
