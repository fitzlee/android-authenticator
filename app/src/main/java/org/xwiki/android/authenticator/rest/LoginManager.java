package org.xwiki.android.authenticator.rest;

import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import org.xwiki.android.authenticator.bean.XWikiUsers;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

/**
 * Created by iof on 2016/4/25.
 */
public class LoginManager{
    public interface Callback{
        void onResponse(String response);
    }

    public static void Login(final String username, final String passwd, final Callback callback){
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://xwikichina.com/xwiki/rest";
                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("GET");
                    String basicAuth = username + ":" + passwd;
                    basicAuth = "Basic " + new String(Base64.encodeToString(basicAuth.getBytes(), Base64.NO_WRAP));
                    connection.setRequestProperty("Authorization", basicAuth);
                    final int code = connection.getResponseCode();
                    final String response = connection.getResponseMessage() + "";
                    if(code == 200){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onResponse("code="+code+" "+ response);
                            }
                        });
                    }
                    Log.i("tag", code + "");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
