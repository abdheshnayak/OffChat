package com.aknayak.offchat.globaldata;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.aknayak.offchat.usersViewConcact.users.contactsUser;

import java.util.ArrayList;

import static com.aknayak.offchat.MainActivity.filterNumber;

public class respData {
    public static boolean selection=false;
    public static boolean delFlag=false;
    public static ArrayList<String> delItem= new ArrayList<>();

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

                        if (phoneNo.length() > 10) {
                            if (phoneNo.substring(0, 4).equals("+977") && phoneNo.length() >= 14) {
                                if (!UserList.contains(uj)) {
                                    UserList.add(uj);
                                }
                            } else if (phoneNo.substring(0, 3).equals("+91") && phoneNo.length() >= 13) {
                                if (!UserList.contains(uj)) {
                                    UserList.add(uj);
                                }
                            }
                        } else if (phoneNo.length() == 10) {
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

}
