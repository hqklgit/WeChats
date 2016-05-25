package shiwenping.com.utils;

import android.util.Log;

/**
 * Created by bilinshengshi on 16/5/24.
 */
/*
 * HaiChecker
 * 16/5/24
 *
 */

public class DLog {
    public static void d(String TAG,String str){
        StringBuffer buffer = new StringBuffer();
        buffer.append("╔════════════════════════════════════════════════════════════════════════════════════════\n");
        buffer.append("║                                      "+TAG+"\n");
        buffer.append("╟────────────────────────────────────────────────────────────────────────────────────────\n");
        buffer.append("║\n");
        if (str.contains("\n")) {
            for (String tmp :
                    str.split("\n")) {
                buffer.append("║    " + tmp + "\n");
            }
        }else{
            buffer.append("║"+str+"\n");
        }
        buffer.append("╚════════════════════════════════════════════════════════════════════════════════════════");


        Log.d(TAG,buffer.toString());


    }
    public static void e(String TAG,String str){
        StringBuffer buffer = new StringBuffer();
        buffer.append("╔════════════════════════════════════════════════════════════════════════════════════════\n");
        buffer.append("║                                      "+TAG+"\n");
        buffer.append("╟────────────────────────────────────────────────────────────────────────────────────────\n");
        buffer.append("║\n");
        if (str.contains("\n")) {
            for (String tmp :
                    str.split("\n")) {
                buffer.append("║    " + tmp + "\n");
            }
        }else{
            buffer.append("║"+str+"\n");
        }
        buffer.append("╚════════════════════════════════════════════════════════════════════════════════════════");


        Log.e(TAG,buffer.toString());


    }
}
