package com.aknayak.offchatserver.datas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.aknayak.offchatserver.AESHelper;
import com.aknayak.offchatserver.messages.MSG;
import com.aknayak.offchatserver.messages.Message;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.aknayak.offchatserver.MainActivity.ROOT_CHILD;


/**
 * OffChat
 * Created by Abdhesh Nayak on 3/18/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/

public class DBHelper extends SQLiteOpenHelper implements Serializable {

    public static final String DATABASE_NAME = "MyDBName.db";
    public static final String CONTACTS_TABLE_NAME = "contacts";
    public static final String CONTACTS_COLUMN_NAME = "name";
    public static final String CONTACTS_COLUMN_PHONE = "phone";

    private Context context;
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS contacts");
        onCreate(db);
    }

    public static String MESSAGE = "message";
    public static String SENDER_PHONE = "senderPhone";
    public static String RECEIVER_PHONE = "receierPhone";
    public static String TIMESTAMP = "timestamp";
    public static String STATUS = "status";
    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table msg " +
                        "("+MESSAGE+" NUMBER,"+SENDER_PHONE+" NUMBER,"+RECEIVER_PHONE+" TEXT,"+TIMESTAMP+" NUMBER primary key,"+STATUS+" NUMBER)"
        );
        db.execSQL(
                "create table messages " +
                        "(message text,messageId text primary key,messagesource text ,messagesenttime date, messageStatus number,messageRoot text,messageFor, replyId text)"
        );
    }

    public boolean insertMSG(String message, String senderPhone, String receiverPhone, int timeStamp, int messageStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MESSAGE,message);
        contentValues.put(SENDER_PHONE,senderPhone);
        contentValues.put(RECEIVER_PHONE,receiverPhone);
        contentValues.put(TIMESTAMP,timeStamp);
        contentValues.put(STATUS, messageStatus);
        Cursor res = db.rawQuery("select * from msg where  "+TIMESTAMP+" = '" + timeStamp +"' and "+SENDER_PHONE+" = '"+senderPhone+"';", null);
        res.moveToFirst();
        if (res.getCount() == 0) {
            db.insert("msg", null, contentValues);
        } else {
            db.update("msg", contentValues,TIMESTAMP+" = ? and "+SENDER_PHONE+" = ?",new String[]{String.valueOf(timeStamp),senderPhone});
        }
        return true;
    }

    public MSG getLastMSG(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from msg where "+STATUS+" = '1' order by "+TIMESTAMP+" asc", null);
        res.moveToLast();

        if (res.getCount() != 0) {
            return new MSG(res.getString(res.getColumnIndex(MESSAGE)),res.getString(res.getColumnIndex(SENDER_PHONE)),res.getString(res.getColumnIndex(RECEIVER_PHONE)),res.getInt(res.getColumnIndex(TIMESTAMP)),res.getInt(res.getColumnIndex(STATUS)));
        } else {
            return null;
        }
    }

    public boolean insertOTFMessage(String Message, String messageSource, Date messageSentTime, int messageStatus, String messageId, String messageRoot, String messageFor,String replyId) {
        Log.d("Message Insert"," "+messageSource+" "+messageStatus+"  "+ AESHelper.decrypt(Message)+" ");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("message", Message);
        contentValues.put("messageId", messageId);
        contentValues.put("messagesource", messageSource);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        contentValues.put("messagesenttime", simpleDateFormat.format(messageSentTime));
        contentValues.put("messageStatus", messageStatus);
        contentValues.put("messageRoot", messageRoot);
        contentValues.put("messageFor", messageFor);
        contentValues.put("replyId", replyId);
        Cursor res = db.rawQuery("select * from messages where (messageRoot = '3' or messageRoot = '4') and messageSource = '" + messageSource + "' and Message = '"+Message+"' and messagesenttime = '"+simpleDateFormat.format(messageSentTime)+"';", null);
        res.moveToFirst();
        if (res.getCount() == 0) {
            db.insert("messages", null, contentValues);
        } else {
            db.update("messages", contentValues, "messageId = ? ", new String[]{messageId});
        }

        return true;
    }

    public boolean updateOTFMessage( String messageId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("messageId", messageId);
        contentValues.put("messageRoot","4");
        db.update("messages", contentValues, "messageId = ? ", new String[]{messageId});
        return true;
    }

    public boolean insertMessage(String Message, String messageSource, Date messageSentTime, int messageStatus, String messageId, String messageRoot, String messageFor,String replyId) {
        Log.d("Message Insert"," "+messageSource+" "+messageStatus+"  "+ AESHelper.decrypt(Message)+" ");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("message", Message);
        contentValues.put("messageId", messageId);
        contentValues.put("messagesource", messageSource);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        contentValues.put("messagesenttime", simpleDateFormat.format(messageSentTime));
        contentValues.put("messageStatus", messageStatus);
        contentValues.put("messageRoot", messageRoot);
        contentValues.put("messageFor", messageFor);
        contentValues.put("replyId", replyId);
        Cursor res = db.rawQuery("select * from messages where messageId = '" + messageId + "';", null);
        res.moveToFirst();
        if (res.getCount() == 0) {
            db.insert("messages", null, contentValues);
        } else {
            db.update("messages", contentValues, "messageId = ? ", new String[]{messageId});
        }

        return true;
    }

    public ArrayList<Message> getAllMessages(String messageRoot) throws ParseException {
        ArrayList<Message> array_list = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from messages where messageroot = '" + messageRoot + "' order by messagesenttime desc", null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            array_list.add(new Message(res.getString(res.getColumnIndex("message")), res.getString(res.getColumnIndex("messagesource")), simpleDateFormat.parse(res.getString(res.getColumnIndex("messagesenttime"))), res.getInt(res.getColumnIndex("messageStatus")), res.getString(res.getColumnIndex("messageId")), res.getString(res.getColumnIndex("messageFor")), res.getString(res.getColumnIndex("replyId"))));
            res.moveToNext();
        }
        return array_list;
    }


    public ArrayList<String> getAllMessagesID(String messageRoot) throws ParseException {
        ArrayList<String> array_list = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from messages where messageroot = '" + messageRoot + "' order by messagesenttime asc", null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            array_list.add(res.getString(res.getColumnIndex("messageId")));
            res.moveToNext();
        }
        return array_list;
    }


    public void deleteAllMessages() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from messages");
    }

    public void deleteAllMessagesOfUser(String messageSource) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from messages where messagesource ='" + messageSource + "'");
    }

    public int getUnseenCount(String messageRoot, String source) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from messages where messageRoot = '" + messageRoot + "' and messagesource = '" + source + "' and ( messageStatus ='2' or messageStatus = '1') ;", null);
        res.moveToFirst();
        return res.getCount();
    }

    public String getMessageId(String messageRoot, String source, Date sentTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Cursor res = db.rawQuery("select * from messages;", null);
        res.moveToLast();

        res = db.rawQuery("select * from messages where messageRoot = '" + messageRoot + "' and messagesource = '" + source + "' and messagesenttime = '" + simpleDateFormat.format(sentTime) + "' ;", null);
        res.moveToFirst();
        if (res.getCount() != 0) {
            return res.getString(res.getColumnIndex("messageId"));
        } else {
            return null;
        }
    }

    public Message getMessage(String messageID) throws ParseException {

        SQLiteDatabase db = this.getWritableDatabase();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        Cursor res = db.rawQuery("select * from messages where messageId = '" + messageID + "';", null);
        res.moveToFirst();
        if (res.getCount() != 0) {
            return new Message(res.getString(res.getColumnIndex("message")), res.getString(res.getColumnIndex("messagesource")), simpleDateFormat.parse(res.getString(res.getColumnIndex("messagesenttime"))), res.getInt(res.getColumnIndex("messageStatus")), res.getString(res.getColumnIndex("messageId")), res.getString(res.getColumnIndex("messageFor")));
        } else {
            return null;
        }
    }


    public Message getlastMessages(String messageRoot) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from messages where messageroot = '" + messageRoot + "' order by messagesenttime asc", null);
        res.moveToLast();

        if (res.getCount() != 0) {
            return new Message(res.getString(res.getColumnIndex("message")), res.getString(res.getColumnIndex("messagesource")), simpleDateFormat.parse(res.getString(res.getColumnIndex("messagesenttime"))), res.getInt(res.getColumnIndex("messageStatus")), res.getString(res.getColumnIndex("messageId")), res.getString(res.getColumnIndex("messageFor")));
        } else {
            return null;
        }
    }

    public ArrayList<Message> getAllMessagesByStatus(String messageRoot, int status, String Source) throws ParseException {
        ArrayList<Message> array_list = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from messages where messageroot = '" + messageRoot + "' and messagesource = '" + Source + "' and messageStatus = '" + status + "'  order by messagesenttime asc", null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            array_list.add(new Message(res.getString(res.getColumnIndex("message")), res.getString(res.getColumnIndex("messagesource")), simpleDateFormat.parse(res.getString(res.getColumnIndex("messagesenttime"))), res.getInt(res.getColumnIndex("messageStatus")), res.getString(res.getColumnIndex("messageId")), res.getString(res.getColumnIndex("messageFor"))));
            res.moveToNext();
        }
        return array_list;
    }

    public void deleteMessage(String messageId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from messages where messageId ='" + messageId + "'");
    }

}