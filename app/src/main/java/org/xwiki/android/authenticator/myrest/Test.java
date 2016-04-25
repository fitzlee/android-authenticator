package org.xwiki.android.authenticator.myrest;


import android.os.Handler;
import android.widget.TextView;

import org.xwiki.android.authenticator.bean.XWikiUsers;
import org.xwiki.android.authenticator.rest.XWikiUser;
import org.xwiki.android.authenticator.utils.Loger;

import java.io.IOException;
import java.util.List;

/**
 * Created by iof on 2016/4/25.
 */
public class Test {

    public static void testLogin(final HttpCallback callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] result = new XWikiHttp().login("fitz", "fitz2xwiki");
                    callback.postSuccess(result);
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.postFailure(e.getMessage());
                }
            }
        }).start();
    }

    public static void testGetAllUser(final HttpCallback callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<XWikiUsers> userList = new XWikiHttp().getUserList("xwiki",10);
                    callback.postSuccess(userList);
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.postFailure(e.getMessage());
                }
            }
        }).start();
    }



}
