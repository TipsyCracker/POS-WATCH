package com.nowfloats.smartretailer.Utils;

import android.widget.EditText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by NowFloats on 12-01-2017.
 */

public class Utils {
    public static boolean isNullOrEmpty(String str){
       return str==null || str.trim().equals("");
    }
    public static boolean isNullOrEmpty(String[] strs){
        boolean flag = false;
        for (String str:strs) {
            if(str==null || str.trim().equals("")){
                flag = true;
                break;
            }
        }
        return flag;
    }
    public static void makeEditTextEmpty(EditText[] fields){
        for(EditText field: fields){
            field.setText("");
        }
    }

    public static String getFormattedDate(Date date){
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return df.format(date);

    }

}
