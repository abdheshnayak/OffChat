package com.aknayak.offchat.globaldata;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.aknayak.offchat.usersViewConcact.users.contactsUser;

import java.util.ArrayList;
import java.util.Arrays;

import static com.aknayak.offchat.MainActivity.filterNumber;
import static com.aknayak.offchat.MainActivity.senderUserName;

public class respData {
    public static boolean selection = false;
    public static boolean delFlag = false;
    public static ArrayList<String> delItem = new ArrayList<>();
    public static ArrayList<Integer> cntCode = new ArrayList<>(Arrays.asList(886, 93, 355, 213, 376, 244, 672, 54, 374, 297, 61, 43, 994, 973, 880, 375, 32, 501, 229, 975, 591, 599, 387, 267, 47, 55, 246, 673, 359, 226, 257, 238, 855, 237, 1, 236, 235, 56, 86, 852, 853, 61, 61, 57, 269, 242, 682, 506, 385, 53, 599, 357, 420, 225, 850, 243, 45, 253, 593, 20, 503, 240, 291, 372, 268, 251, 500, 298, 679, 358, 33, 594, 689, 262, 241, 220, 995, 49, 233, 350, 30, 299, 590, 502, 44, 224, 245, 592, 509, 672, 504, 36, 354, 91, 62, 98, 964, 353, 44, 972, 39, 81, 44, 962, 7, 254, 686, 965, 996, 856, 371, 961, 266, 231, 218, 423, 370, 352, 261, 265, 60, 960, 223, 356, 692, 596, 222, 230, 262, 52, 691, 377, 976, 382, 212, 258, 95, 264, 674, 977, 31, 687, 64, 505, 227, 234, 683, 672, 47, 968, 92, 680, 507, 675, 595, 51, 63, 870, 48, 351, 1, 974, 82, 373, 40, 7, 250, 262, 590, 290, 590, 508, 685, 378, 239, 966, 221, 381, 248, 232, 65, 421, 386, 677, 252, 27, 500, 211, 34, 94, 970, 249, 597, 47, 46, 41, 963, 992, 66, 389, 670, 228, 690, 676, 216, 90, 993, 688, 256, 380, 971, 44, 255, 1, 598, 998, 678, 58, 84, 681, 212, 967, 260, 263, 358));

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
                            String contCode = phoneNo.substring(0, phoneNo.length() - 10);
                            contCode = cntCode.contains(Double.valueOf(contCode).intValue()) ? ("+" + Double.valueOf(contCode).intValue()) : null;
                            if (contCode != null) {
                                phoneNo = contCode + phoneNo.substring(phoneNo.length() - 10);
                                uj.setPhoneNumber(phoneNo);
                                if (!UserList.contains(uj)) {
                                    UserList.add(uj);
                                }
                            }
                        } else if (phoneNo.length() == 10) {
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

}
