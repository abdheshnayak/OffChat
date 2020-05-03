package com.aknayak.offchat.globaldata;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.aknayak.offchat.MainActivity;
import com.aknayak.offchat.R;
import com.aknayak.offchat.datas.DBHelper;
import com.aknayak.offchat.notificationDialog;
import com.aknayak.offchat.phone_verification;
import com.aknayak.offchat.users.typingDetails;
import com.aknayak.offchat.usersViewConcact.users.contactsUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.aknayak.offchat.AllConcacts.REQUEST_READ_CONTACTS;
import static com.aknayak.offchat.MainActivity.ANONYMOUS;
import static com.aknayak.offchat.MainActivity.INSTANCE_ID;
import static com.aknayak.offchat.MainActivity.authUser;
import static com.aknayak.offchat.MainActivity.senderUserName;
import static com.aknayak.offchat.MainActivity.temp;
import static com.aknayak.offchat.MainActivity.updateLink;


/**
 * OffChat
 * Created by Abdhesh Nayak on 3/18/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/

public class respData {
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS=0;
    public static Boolean IS_PERMISSIONS_REQUEST_READ_CONTACTS = false;

    public static boolean selection = false;
    public static ArrayList<String> delItem = new ArrayList<>();
    public static String repItem = null;
    public static final String CHANNEL_ID = "MyNotification";
    public static String mUsername;
    public static typingDetails tdtls = new typingDetails(false, Calendar.getInstance(Locale.ENGLISH).getTime());

    public static Boolean appLaunched;
    public static String MESSAGES_CHILD = "messages";
    public static String MAINVIEW_CHILD = "history";
    public static String TYPING_CHILD = "typing";

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
            return temp;
        } else if (temp.length() > 10) {
            return "+" + Double.valueOf(temp.substring(0, 4)).intValue() + temp.substring(4);
        } else {
            return temp;
        }
    }

    public static void requestPermission(Activity activity) {

        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            IS_PERMISSIONS_REQUEST_READ_CONTACTS=true;
            // Permission has already been granted
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


    public static ArrayList getAllContacts(ContentResolver cr) {
        ArrayList<contactsUser> UserList = new ArrayList<>();
//        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));


                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));

                        phoneNo = filterNumber(phoneNo);

                        contactsUser uj = new contactsUser(name, phoneNo, "", true);

                        if (phoneNo.length() > 10 && isValidMobile(phoneNo)) {
                            if (!UserList.contains(uj)) {
                                UserList.add(uj);
                            }

                        } else if (phoneNo.length() == 10 && isValidMobile(phoneNo)) {
                            phoneNo = senderUserName.substring(0, phoneNo.length() - 10);
                            uj.setPhoneNumber(phoneNo);
                            if (!UserList.contains(uj)) {
                                UserList.add(uj);
                            }
                        }
                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }
        return UserList;
    }

    public static boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }


    public static void createNotificationChannel(Activity activity) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = activity.getString(R.string.channel_name);
            String description = activity.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static Boolean isOnline() {
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

    public static Boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfoListener = connectivityManager.getActiveNetworkInfo();
        return networkInfoListener != null && networkInfoListener.isConnectedOrConnecting();
    }


    public static void notifyIt(String title, String message, Context context, int notificationId) {
        String GROUP_KEY_WORK_EMAIL = "com.aknayak.offchat.notificationBroadcast";

        int icon = R.drawable.ic_launcher_empty;
        //use constant ID for notification used as group summary
        String msg;
        if (message.length() > 300) {
            msg = message.substring(0, 300) + "...";
        } else {
            msg = message;
        }

        int SUMMARY_ID = 9878;

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
//                        .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                        .setLights(Color.RED, 3000, 3000)
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
//                        .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                        .setContentIntent(contentIntent)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .build();

        Intent i = new Intent(context, notificationDialog.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("userName", title);
        i.putExtra("userMessage", msg);
        if (appLaunched == false) {
            if (notificationDialog.mNotificationApp == null) {
                context.startActivity(i);
            } else {
                notificationDialog.mNotificationApp.refreshData(title, msg);
                Toast.makeText(context.getApplicationContext(), "update", Toast.LENGTH_SHORT).show();
            }
        } else {
//            context.startActivity(i);
        }
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


    public static void signOut(Activity activity) {
        new DBHelper(activity).deleteAllDatasOfTable();
        FirebaseAuth.getInstance().signOut();
    }

    public static boolean verifyUser(final String string, final int count, final Activity activity) {
        DBHelper mydb = new DBHelper(activity);
        if (!string.equals(mydb.getUserInfo(INSTANCE_ID))) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
            alertDialogBuilder.setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mUsername = ANONYMOUS;
                    FirebaseAuth.getInstance().signOut();
                    activity.startActivity(new Intent(activity, phone_verification.class));
                    activity.finish();
                }
            });
            alertDialogBuilder.setNegativeButton("Logout", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(activity);
                    alertDialogBuilder2.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            signOut(activity);
                            mUsername = "ANONYMOUS";
                            activity.startActivity(new Intent(activity, phone_verification.class));
                        }
                    });
                    alertDialogBuilder2.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (count >= 1) {
                                Toast.makeText(activity, "Exit from OffChat.", Toast.LENGTH_LONG).show();
                                activity.finish();
                                return;
                            }
                            Toast.makeText(activity, "You will be exit.", Toast.LENGTH_SHORT).show();
                            verifyUser(string, count + 1, activity);
                        }
                    });
                    alertDialogBuilder2.setTitle("Sign Out?");
                    alertDialogBuilder2.setMessage("We Don't store your chats. So if you Sign Out Your Account then you will loose your chats.\n\nAre You Sure to Sign Out ???");
                    alertDialogBuilder2.show();
                }
            });

//            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (count >= 1) {
                        activity.finish();
                    }
                    verifyUser(string, count + 1, activity);
                }
            });
            alertDialogBuilder.setTitle("Verify Your Account");
            alertDialogBuilder.setMessage("We Found another instance started with your phone number. If it is not you then please verify again.");
            alertDialogBuilder.show();

//            Toast.makeText(activity, "Someone may Using Your Acount in Another Phone.", Toast.LENGTH_LONG).show();
        } else {
            return true;
        }
        return false;
    }


    public static boolean checkUpdate(final String varforceUpdate, final String varnormalUpdate, final int count, final Activity activity) {
        if (!varforceUpdate.equals(MainActivity.forceUpdateVersion)) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
            alertDialogBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FirebaseDatabase.getInstance().getReference().child("admin").child("update_link").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            updateLink = dataSnapshot.getValue(String.class);
                            if (updateLink != null) {
                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(updateLink));
                                activity.startActivity(i);
                                Toast.makeText(activity, updateLink, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(activity, "Link Not Available ask with about admin.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    checkUpdate(varforceUpdate, varnormalUpdate, 0, activity);
                }
            });

            alertDialogBuilder.setNegativeButton("Close app", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    activity.finish();
                }
            });

            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setTitle("Compulsory Update");
            alertDialogBuilder.setMessage("This Version of OffChat App is no longer Supported so please update it.");
            alertDialogBuilder.show();

        } else if (!varnormalUpdate.equals(MainActivity.normalupdateVersion)) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
            alertDialogBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FirebaseDatabase.getInstance().getReference().child("admin").child("update_link").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            updateLink = dataSnapshot.getValue(String.class);
                            if (updateLink != null) {
                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(updateLink));
                                activity.startActivity(i);
                                Toast.makeText(activity, updateLink, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(activity, "Link Not Available ask with about admin.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });
            alertDialogBuilder.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
            alertDialogBuilder.setTitle("Update Found !!!");
            alertDialogBuilder.setMessage("A newer Version Of OffChat app found.");
            alertDialogBuilder.show();

        }
        return false;
    }


    final public static int sound_sent = 0;
    public final static int sound_notification = 1;
    public final static int sound_incoming_message = 2;
    public final static int sound_waiting = 3;


    static MediaPlayer mp = null;

    public static void playSound(Context context, int notification) {
        if (mp == null) {
            mp = MediaPlayer.create(context, R.raw.send_message);
        }

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (audio.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            mp.stop();
            switch (notification) {
                case sound_sent:
                    mp = MediaPlayer.create(context, R.raw.send_message);
                    mp.start();
                    break;
                case sound_notification:
                    mp = MediaPlayer.create(context, R.raw.notification);
                    mp.start();
                    break;
                case sound_incoming_message:
                    mp = MediaPlayer.create(context, R.raw.incoming);
                    mp.start();
                    break;
                case sound_waiting:
                    mp = MediaPlayer.create(context, R.raw.waiting);
                    mp.start();
                    break;
            }
        }

    }


}
