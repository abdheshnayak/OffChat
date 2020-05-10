package com.aknayak.offchat.smsFetcher;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.aknayak.offchat.datas.DBHelper;
import com.aknayak.offchat.globaldata.AESHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.aknayak.offchat.globaldata.respData.checkPhoneNumber;
import static com.aknayak.offchat.globaldata.respData.filterNumber;
import static com.aknayak.offchat.globaldata.respData.getRandString;
import static com.aknayak.offchat.globaldata.respData.getRoot;
import static com.aknayak.offchat.globaldata.respData.mUsername;
import static com.aknayak.offchat.globaldata.respData.notifyIt;


public class SmsBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";

    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsBody = "";
            String senderAddress="";
            for (int i = 0; i < sms.length; ++i) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

                smsBody = smsMessage.getMessageBody().toString();
                senderAddress = smsMessage.getOriginatingAddress();
                senderAddress=filterNumber(senderAddress);
            }
            if (verifySender(senderAddress)) {
                incoming_message i = filterMessage(smsBody);
                if (i != null) {
                    DBHelper dbHelper = new DBHelper(context);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hhmmss", Locale.ENGLISH);
                    dbHelper.insertMessage(AESHelper.encrypt(i.message), filterNumber(i.number), Calendar.getInstance(Locale.ENGLISH).getTime(), 1, i.getMsgId(), getRoot(i.getNumber(), mUsername), mUsername, null);
                    notifyIt("" + dbHelper.getUserName(i.number), i.message, context, Double.valueOf(i.number).intValue() + Double.valueOf(simpleDateFormat.format(Calendar.getInstance(Locale.ENGLISH).getTime())).intValue());
//                OTF.add(new Message(AESHelper.encrypt(i.message), gloabalData.filterNumber(senderAddress), Calendar.getInstance(Locale.ENGLISH).getTime(), 1, getRandString(15),i.getNumber()));
                }
            }
        }
    }

    private boolean verifySender(String sender) {
        if (sender.equals(filterNumber("+9779805953007"))){
            return true;
        }else {
            return false;
        }
    }

    private incoming_message filterMessage(String smsBody){
        String receiverAddress="";
        String msgId;
        String[] lines = smsBody.split("\n");
        String mainMessage="";
        if (checkPhoneNumber(filterNumber(lines[1]))){
            receiverAddress = filterNumber(lines[1]);
            if (lines[2].trim().length()==15) {
                msgId = lines[2].trim();
                for (int i = 3; i < lines.length; i++) {
                    mainMessage = mainMessage + lines[i];
                }
                return new incoming_message(receiverAddress, mainMessage, msgId);
            }else return null;
        }else return null;
    }

    private void updateDatabaseMessage(Context context){
        DBHelper dbHelper = new DBHelper(context);
        try {
            Uri uriSms = Uri.parse("content://sms/inbox");
            Cursor c = context.getContentResolver().query(
                    uriSms,
                    new String[] { "_id", "thread_id", "address", "person",
                            "date", "body" }, "read=0", null, null);

            if (c != null && c.moveToFirst()) {
                do {
                    long id = c.getLong(0);
//                    long threadId = c.getLong(1);
                    String address = c.getString(2);
                    String body = c.getString(5);
                    Date date =new Date( c.getLong(4));

                    Log.e("log>>>",
                            "0--->" + c.getString(0) + "\n1---->" + c.getString(1)
                                    + "\n2---->" + c.getString(2) + "\n3--->"
                                    + c.getString(3) + "4----->" + c.getString(4)
                                    + "\n5---->" + c.getString(5));
                    Log.e("log>>>", "date" + c.getString(0));

                    ContentValues values = new ContentValues();
                    values.put("read", true);
                    context.getContentResolver().update(Uri.parse("content://sms/"),
                            values, "_id=" + id, null);

                    incoming_message i = filterMessage(body);
                    if (i!=null){
//                        dbHelper.insertMSG(i.message,address,i.number,c.getInt(4),1);
                    }
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e("log>>>", e.toString());
        }
    }
}