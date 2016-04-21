package org.xwiki.android.authenticator.rest;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lf on 2016/4/21.
 */
public class XWikiConn extends HttpConnector{

    List<XWikiUser> getAllUsers() {
        HttpURLConnection conn = null;
        try {
            String requestUrl = "http://localhost:8080/xwiki/rest/wikis/query?q=object:XWiki.XWikiUsers";
            Map<String, String> requestParams = new HashMap<String, String>();
            requestParams.put("age", "12");
            requestParams.put("name", "中国");
            StringBuilder params = new StringBuilder();
            for(Map.Entry<String, String> entry : requestParams.entrySet()){
                params.append(entry.getKey());
                params.append("=");
                params.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                params.append("&");
            }
            if (params.length() > 0) params.deleteCharAt(params.length() - 1);
            byte[] data = params.toString().getBytes();
            URL realUrl = null;
            realUrl = new URL(requestUrl);
            conn = (HttpURLConnection) realUrl.openConnection();
            conn.setDoOutput(true);//发送POST请求必须设置允许输出
            conn.setUseCaches(false);//不使用Cache
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");//维持长连接
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            DataOutputStream outStream = null;
            outStream = new DataOutputStream(conn.getOutputStream());
            outStream.write(data);
            outStream.flush();
            if( conn.getResponseCode() == 200 ){
                //String result = readAsString(conn.getInputStream(), "UTF-8");
                outStream.close();
                //System.out.println(result);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(conn != null)
                conn.disconnect();
        }
        return null;
    }

}
