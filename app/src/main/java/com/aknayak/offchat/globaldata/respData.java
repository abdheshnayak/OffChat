package com.aknayak.offchat.globaldata;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.aknayak.offchat.usersViewConcact.users.contactsUser;

import java.util.ArrayList;
import java.util.Arrays;

import static com.aknayak.offchat.MainActivity.authUser;
import static com.aknayak.offchat.MainActivity.senderUserName;


/**
 * OffChat
 * Created by Abdhesh Nayak on 3/18/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/

public class respData {
    public static boolean selection = false;
    public static ArrayList<String> delItem = new ArrayList<>();

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
}
