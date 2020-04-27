package com.aknayak.offchat.datas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.aknayak.offchat.globaldata.AESHelper;
import com.aknayak.offchat.globaldata.respData;
import com.aknayak.offchat.messages.Message;
import com.aknayak.offchat.usersViewConcact.users.contactsUser;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.aknayak.offchat.MainActivity.temp;
import static com.aknayak.offchat.globaldata.respData.playSound;
import static com.aknayak.offchat.globaldata.respData.sound_incoming_message;
import static com.aknayak.offchat.globaldata.respData.sound_notification;


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

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table contacts " +
                        "(name text,phone text primary key, active boolean)"
        );
        db.execSQL(
                "create table messages " +
                        "(message text,messageId text primary key,messagesource text ,messagesenttime date, messageStatus number,messageRoot text,messageFor)"
        );
        db.execSQL(
                "create table userInfo " +
                        "(varName text primary key, varData text)"
        );
    }

    public boolean insertuserInfo(String varName, String varData) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("varName", varName);
        contentValues.put("varData", varData);

        Cursor res = db.rawQuery("select * from userInfo where varName = '" + varName + "';", null);
        res.moveToFirst();
        if (res.getCount() == 0) {
            db.insert("userInfo", null, contentValues);
//                Log.d("Inserted",name);
        } else {
            db.update("userInfo", contentValues, "varName = ? ", new String[]{varName});
//            Log.d("Already Inserted",name);
        }
        return true;
    }

    public String getUserInfo(String varName) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from userInfo where varName = '" + varName + "'", null);
        res.moveToFirst();
        if (res.getCount() != 0) {
            return res.getString(res.getColumnIndex("varData"));
        } else {
            return null;
        }
    }

    public ArrayList<Message> getHist() throws ParseException {
        ArrayList<Message> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from  (select * from messages order by messagesenttime asc)  group by messageRoot order by messagesenttime desc", null);
        res.moveToFirst();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        while (res.isAfterLast() == false) {
            array_list.add(new Message(res.getString(res.getColumnIndex("message")), res.getString(res.getColumnIndex("messagesource")), simpleDateFormat.parse(res.getString(res.getColumnIndex("messagesenttime"))), res.getInt(res.getColumnIndex("messageStatus")), res.getString(res.getColumnIndex("messageId")), res.getString(res.getColumnIndex("messageFor"))));
            res.moveToNext();
        }
        return array_list;
    }

    public boolean insertUser(String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("phone", phone);

        Cursor res = db.rawQuery("select * from contacts where phone = '" + phone + "';", null);
        res.moveToFirst();
        if (res.getCount() == 0) {
            db.insert("contacts", null, contentValues);
//                Log.d("Inserted",name);
        } else {
//            Log.d("Already Inserted",name);
        }
        return true;
    }


    public boolean insertContact(String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("active", false);
        Cursor res = db.rawQuery("select * from contacts where phone = '" + phone + "';", null);
        res.moveToFirst();
        if (res.getCount() == 0) {
            db.insert("contacts", null, contentValues);
//                Log.d("Inserted",name);
        } else {
//            Log.d("Already Inserted",name);
        }
        return true;
    }

    public boolean insertContact(String name, String phone, Boolean present) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("active", present);

        Cursor res = db.rawQuery("select * from contacts where phone = '" + phone + "';", null);
        res.moveToFirst();
        if (res.getCount() == 0) {
            db.insert("contacts", null, contentValues);
//                Log.d("Inserted",name);
        } else {
            db.update("contacts", contentValues, "phone = ? ", new String[]{phone});
        }
        return true;
    }


    public ArrayList<contactsUser> getData(String searchData) {
        ArrayList<contactsUser> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from contacts where name like '%" + searchData + "%' or phone like '%" + searchData + "%' order by name asc", null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            array_list.add(new contactsUser(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)), res.getString(res.getColumnIndex(CONTACTS_COLUMN_PHONE)), "",1==res.getInt(res.getColumnIndex("active"))));
            res.moveToNext();
        }
        return array_list;
    }

    public String getUserName(String searchData) {

        SQLiteDatabase db = this.getReadableDatabase();
        if (searchData.equals("+1")) {
            return "Admin";
        }
        Cursor res = db.rawQuery("select * from contacts where phone = '" + searchData + "'", null);
        res.moveToFirst();
        if (res.getCount() == 0) {
            if (this.getUserInfo(searchData)!= null){
                return this.getUserInfo(searchData);
            }else {
                return searchData;
            }
        } else {
            return res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME));
        }
    }

    public void deleteAllContact() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from contacts");
    }

    public ArrayList<contactsUser> getAllCotacts() {
        ArrayList<contactsUser> array_list = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from (select * from contacts order by name desc) order by active asc", null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            array_list.add(new contactsUser(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)), res.getString(res.getColumnIndex(CONTACTS_COLUMN_PHONE)), "",res.getInt(res.getColumnIndex("active"))==1));
//            Log.d("Retrived :",res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)));
            res.moveToNext();
        }
        return array_list;
    }

    public boolean inserthistory(String phone, String lastmessage, Date lastmessagesenttime, int sentstatus) {
//        Log.d("History :","Inserted");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("phone", phone);
        contentValues.put("lastmessage", lastmessage);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        contentValues.put("latmessagesenttime", simpleDateFormat.format(lastmessagesenttime));
        contentValues.put("sentstatus", sentstatus);

        Cursor res = db.rawQuery("select * from histories where phone = '" + phone + "';", null);
        res.moveToFirst();
        if (res.getCount() == 0) {
            db.insert("histories", null, contentValues);
//                Log.d("Inserted",phone);
        } else {
            db.update("histories", contentValues, "phone = ? ", new String[]{phone});
//            Log.d("Already Inserted",phone);
            return true;
        }
        return true;
    }

//    public void deleteAllHistories() {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.execSQL("delete from histories");
//    }


    public void deleteAllDatasOfTable() {
        SQLiteDatabase db = this.getWritableDatabase();
//        db.execSQL("delete from histories");
        db.execSQL("delete from contacts");
        db.execSQL("delete from messages");
    }


    public boolean insertMessage(String Message, String messageSource, Date messageSentTime, int messageStatus, String messageId, String messageRoot, String messageFor,int tag) {
        Log.d("Message Insert"," "+messageSource+" "+messageStatus+"  "+ AESHelper.decrypt(Message)+" " +tag);
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
        Cursor res = db.rawQuery("select * from messages where messageId = '" + messageId + "';", null);
        res.moveToFirst();
        if (res.getCount() == 0) {
            if (messageFor.equals(respData.mUsername) && messageStatus == 2){
                if (temp != null && temp.equals(messageSource)){
                    playSound(context,sound_incoming_message);
                }else {
                    playSound(context,sound_notification);
                }
            }
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
        Cursor res = db.rawQuery("select * from messages where messageroot = '" + messageRoot + "' order by messagesenttime asc", null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            array_list.add(new Message(res.getString(res.getColumnIndex("message")), res.getString(res.getColumnIndex("messagesource")), simpleDateFormat.parse(res.getString(res.getColumnIndex("messagesenttime"))), res.getInt(res.getColumnIndex("messageStatus")), res.getString(res.getColumnIndex("messageId")), res.getString(res.getColumnIndex("messageFor"))));
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

    public void deleteuserInfo() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from userInfo where 1 = 1");
    }
}