package com.aknayak.offchat.globaldata;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.aknayak.offchat.MainActivity;
import com.aknayak.offchat.R;
import com.aknayak.offchat.datas.DBHelper;
import com.aknayak.offchat.phone_verification;
import com.aknayak.offchat.usersViewConcact.users.contactsUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.aknayak.offchat.MainActivity.ANONYMOUS;
import static com.aknayak.offchat.MainActivity.INSTANCE_ID;
import static com.aknayak.offchat.MainActivity.authUser;
import static com.aknayak.offchat.MainActivity.senderUserName;
import static com.aknayak.offchat.MainActivity.updateLink;


/**
 * OffChat
 * Created by Abdhesh Nayak on 3/18/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/

public class respData {
    public static boolean selection = false;
    public static ArrayList<String> delItem = new ArrayList<>();
    private static final String CHANNEL_ID = "MyNotification";
    public static String mUsername;

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

                        contactsUser uj = new contactsUser(name, phoneNo, "");

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
                            verifyUser(string, count + 1,activity);
                        }
                    });
                    alertDialogBuilder2.setTitle("Sign Out?");
                    alertDialogBuilder2.setMessage("We Don't store your chats. So if you Sign Out Your Account then you will loose your chats.\n\nAre You Sure to Sign Out ???");
                    alertDialogBuilder2.show();
                }
            });

            alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (count >= 1) {
                        activity.finish();
                    }
                    verifyUser(string, count + 1,activity);
                }
            });
            alertDialogBuilder.setTitle("Verify Your Account");
            alertDialogBuilder.setMessage("We Found another instance started with your phone number. If it is not yout then please verify else signout.");
            alertDialogBuilder.show();

            Toast.makeText(activity, "Someone may Using Your Acount in Another Phone.", Toast.LENGTH_LONG).show();
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
                    checkUpdate(varforceUpdate, varnormalUpdate, 0,activity);
                }
            });

            alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (count >= 1) {
                        Toast.makeText(activity, "Exit from OffChat.", Toast.LENGTH_LONG).show();
                        activity.finish();
                        return;
                    }
                    Toast.makeText(activity, "You will be exit.", Toast.LENGTH_SHORT).show();
                    checkUpdate(varforceUpdate, varnormalUpdate, count + 1,activity);
                }
            });
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

}
