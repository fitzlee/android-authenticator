package org.xwiki.android.authenticator.rest;

import android.os.Handler;

import org.apache.http.protocol.HTTP;

/**
 * Created by fitz on 2016/4/16.
 */
public class AsynHttpUtils extends HttpConnector{

    public interface Callback{
        void onResponse(String response);
    }

    public static void get(final String url, final Callback callback){
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String response = get(url);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(response);
                    }
                });
            }
        }).start();
    }

    public static void post(final String url, final String content, final Callback callback){
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String response = HttpConnector.post(url,content);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(response);
                    }
                });
            }
        }).start();
    }

}
