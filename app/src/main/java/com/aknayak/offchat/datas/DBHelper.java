package com.aknayak.offchat.datas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.aknayak.offchat.messages.Message;
import com.aknayak.offchat.users.User;
import com.aknayak.offchat.usersViewConcact.users.contactsUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Console;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyDBName.db";
    public static final String CONTACTS_TABLE_NAME = "contacts";
    public static final String CONTACTS_COLUMN_ID = "id";
    public static final String CONTACTS_COLUMN_NAME = "name";
    public static final String CONTACTS_COLUMN_EMAIL = "email";
    public static final String CONTACTS_COLUMN_STREET = "street";
    public static final String CONTACTS_COLUMN_CITY = "place";
    public static final String CONTACTS_COLUMN_PHONE = "phone";
    private HashMap hp;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, CONTACTS_TABLE_NAME);
        return numRows;
    }

    public boolean updateContact (Integer id, String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        db.update("contacts", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteContact (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("contacts",
                "id = ? ",
                new String[] { Integer.toString(id) });
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
                        "(name text,phone text primary key)"
        );
        db.execSQL(
                "create table messages " +
                        "(message text,messageId text primary key,messagesource text ,messagesenttime date, messageStatus number,messageRoot text)"
        );

        db.execSQL(
                "create table histories " +
                        "(phone text primary key,lastmessage text,latmessagesenttime date,sentstatus number)"
        );
    }


    public boolean insertContact (String name, String phone){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);

        Cursor res =  db.rawQuery( "select * from contacts where phone = '"+phone+"';", null );
        res.moveToFirst();
        if(res.getCount()==0){
                db.insert("contacts", null, contentValues);
//                Log.d("Inserted",name);
        }else {
//            Log.d("Already Inserted",name);
        }
        return true;
    }


    public ArrayList<contactsUser> getData(String searchData) {
        ArrayList<contactsUser> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from contacts where name like '%"+searchData+"%' or phone like '%"+searchData+"%' order by name asc", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(new contactsUser(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)),res.getString(res.getColumnIndex(CONTACTS_COLUMN_PHONE)),""));

//            Log.d("Retrived :",res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)));
            res.moveToNext();
        }
        return array_list;
    }

    public String getUserName(String searchData) {
        ArrayList<contactsUser> array_list = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from contacts where phone = '" + searchData + "'", null);
        res.moveToFirst();
        if (res.getCount() == 0) {
            return searchData;
        } else{
            return res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME));
        }
    }

    public void deleteAllContact () {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from contacts");
        insertContact("Admin","+1");
    }
    public ArrayList<contactsUser> getAllCotacts() {
        ArrayList<contactsUser> array_list = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from contacts order by name desc", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(new contactsUser(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)),res.getString(res.getColumnIndex(CONTACTS_COLUMN_PHONE)),""));
//            Log.d("Retrived :",res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)));
            res.moveToNext();
        }
        return array_list;
    }

    public ArrayList<User> getAllHistories() throws ParseException {
        ArrayList<User> array_list = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from histories order by latmessagesenttime asc", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

            array_list.add(new User(res.getString(res.getColumnIndex("phone")), simpleDateFormat.parse(res.getString(res.getColumnIndex("latmessagesenttime"))),res.getString(res.getColumnIndex("lastmessage"))," ",res.getInt(res.getColumnIndex("sentstatus"))));
//            Log.d("Retrived :",res.getString(res.getColumnIndex("phone")));
            res.moveToNext();
        }
        return array_list;
    }

    public boolean inserthistory ( String phone, String lastmessage, Date lastmessagesenttime, int sentstatus){
//        Log.d("History :","Inserted");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("phone", phone);
        contentValues.put("lastmessage",lastmessage);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        contentValues.put("latmessagesenttime",simpleDateFormat.format(lastmessagesenttime));
        contentValues.put("sentstatus",sentstatus);

        Cursor res =  db.rawQuery( "select * from histories where phone = '"+phone+"';", null );
        res.moveToFirst();
        if(res.getCount()==0){
            db.insert("histories", null, contentValues);
//                Log.d("Inserted",phone);
        }else {
            db.update("histories", contentValues, "phone = ? ", new String[] { phone } );
//            Log.d("Already Inserted",phone);
            return true;
        }
        return true;
    }

    public void deleteAllHistories () {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from histories");
    }


    public void deleteAllDatasOfTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from histories");
        db.execSQL("delete from contacts");
        db.execSQL("delete from messages");
    }
    public void CountUnseen(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from messages where messageStatus = '1'", null);
        res.moveToFirst();
        res.getCount();
    }














    public boolean insertMessage (String Message, String messageSource,Date messageSentTime, int messageStatus,String messageId,String messageRoot){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("message", Message);
        contentValues.put("messageId",messageId);
        contentValues.put("messagesource", messageSource);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        contentValues.put("messagesenttime", simpleDateFormat.format(messageSentTime));
        contentValues.put("messageStatus", messageStatus);
        contentValues.put("messageRoot", messageRoot);

        Cursor res =  db.rawQuery( "select * from messages where messageId = '"+messageId+"';", null );
        res.moveToFirst();
        if(res.getCount()==0){
            db.insert("messages", null, contentValues);
//                Log.d("Inserted",name);
        }else {
            db.update("messages", contentValues, "messageId = ? ", new String[] { messageId } );
//            Log.d("Already Inserted",name);
        }
        return true;
    }




    public ArrayList<Message> getAllMessages(String messageRoot) throws ParseException {
        ArrayList<Message> array_list = new ArrayList<>();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from messages where messageroot = '"+messageRoot+"' order by messagesenttime asc", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(new Message(res.getString(res.getColumnIndex("message")),res.getString(res.getColumnIndex("messagesource")), simpleDateFormat.parse(res.getString(res.getColumnIndex("messagesenttime"))),res.getInt(res.getColumnIndex("messageStatus")),res.getString(res.getColumnIndex("messageId"))));
//            Log.d("Retrived :",res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)));

            res.moveToNext();
        }
        return array_list;
    }


    public void deleteAllMessages () {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from messages");
    }
    public void deleteAllMessagesOfUser(String messageSource){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from messages where messagesource ='"+messageSource+"'");
    }

    public int getUnseenCount(String messageRoot,String source){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res =  db.rawQuery( "select * from messages where messageRoot = '"+messageRoot+"' and messagesource = '"+source+"' and ( messageStatus ='2' or messageStatus = '1') ;", null );
        res.moveToFirst();
        return res.getCount();
    }

    public String getMessageId(String messageRoot, String source , Date sentTime){

        SQLiteDatabase db = this.getWritableDatabase();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
//        Cursor res =  db.rawQuery( "select * from messages where messageRoot = '"+messageRoot+"' and messagesource = '"+source+"' and messagesenttime = '"+simpleDateFormat.format(sentTime)+"' ;", null );
        Cursor res = db.rawQuery("select * from messages;", null );
        res.moveToLast();

//         Log.d("LLL",simpleDateFormat.format(sentTime)+" "+res.getString(res.getColumnIndex("messagesenttime")));

        res =  db.rawQuery( "select * from messages where messageRoot = '"+messageRoot+"' and messagesource = '"+source+"' and messagesenttime = '"+simpleDateFormat.format(sentTime)+"' ;", null );
        res.moveToFirst();
        if (res.getCount()!=0){
            return res.getString(res.getColumnIndex("messageId"));
        }else {
            return null;
        }
    }

    public Message getMessage(String messageID) throws ParseException {

        SQLiteDatabase db = this.getWritableDatabase();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
//        Cursor res =  db.rawQuery( "select * from messages where messageRoot = '"+messageRoot+"' and messagesource = '"+source+"' and messagesenttime = '"+simpleDateFormat.format(sentTime)+"' ;", null );


        Cursor res =  db.rawQuery( "select * from messages where messageId = '"+messageID+"';", null );
        res.moveToFirst();
        if (res.getCount()!=0){
            return new Message(res.getString(res.getColumnIndex("message")),res.getString(res.getColumnIndex("messagesource")), simpleDateFormat.parse(res.getString(res.getColumnIndex("messagesenttime"))),res.getInt(res.getColumnIndex("messageStatus")),res.getString(res.getColumnIndex("messageId")));
        }else {
            return null;
        }
    }


    public Message getlastMessages(String messageRoot) throws ParseException {
        ArrayList<Message> array_list = new ArrayList<>();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from messages where messageroot = '"+messageRoot+"' order by messagesenttime asc", null );
        res.moveToLast();

        if (res.getCount()!=0){
            return new Message(res.getString(res.getColumnIndex("message")),res.getString(res.getColumnIndex("messagesource")), simpleDateFormat.parse(res.getString(res.getColumnIndex("messagesenttime"))),res.getInt(res.getColumnIndex("messageStatus")),res.getString(res.getColumnIndex("messageId")));
        }
        else {
            return null;
        }
    }



}