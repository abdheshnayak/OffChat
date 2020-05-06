package com.aknayak.offchatserver.datas;

import static com.aknayak.offchatserver.MainActivity.authUser;

/**
 * OffChat
 * Created by Abdhesh Nayak on 5/3/20
 * abdheshnayak@gmail.com
 **/
public class gloabalData
{
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
            return null;
        }
    }

    public static  boolean checkPhoneNumber(String PhoneNumber){
        if (PhoneNumber!=null){
            return isValidMobile(PhoneNumber);
        }else return false;
    }

    public static boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

}
