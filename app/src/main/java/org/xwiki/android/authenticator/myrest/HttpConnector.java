package org.xwiki.android.authenticator.myrest;

import org.xwiki.android.authenticator.myrest.HttpRequest.HttpMethod;
import org.xwiki.android.authenticator.utils.Loger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by fitz on 2016/4/25.
 */
public class HttpConnector {
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private final SSLSocketFactory mSslSocketFactory;

    public HttpConnector() {
        mSslSocketFactory = null;
    }

    public HttpResponse performRequest(HttpRequest request) throws IOException{
        String url = request.getUrl();
        HashMap<String, String> map = new HashMap<String, String>();
        map.putAll(request.getHeaders());
        URL parsedUrl = new URL(url);
        HttpURLConnection connection = openConnection(parsedUrl, request);
        for (String headerName : map.keySet()) {
            connection.addRequestProperty(headerName, map.get(headerName));
        }
        setConnectionParametersForRequest(connection, request);
        HttpResponse response = responseFromConnection(connection);
        return response;
    }

    private HttpURLConnection openConnection(URL url, HttpRequest request)
            throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        int timeoutMs = request.getTIMEOUT();
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setUseCaches(false);
        connection.setDoInput(true);

        // use caller-provided custom SslSocketFactory, if any, for HTTPS
        if ("https".equals(url.getProtocol())) {
            if (mSslSocketFactory != null) {
                ((HttpsURLConnection) connection)
                        .setSSLSocketFactory(mSslSocketFactory);
            } else {
                //trust all ca
                HTTPSTrustManager.allowAllSSL();
            }
        }
        return connection;
    }

    private void setConnectionParametersForRequest(HttpURLConnection urlConnection, HttpRequest request) throws IOException{
        switch(request.getMethod()){
            case HttpMethod.GET:
                urlConnection.setRequestMethod("GET");
                break;
            case HttpMethod.POST:
                urlConnection.setRequestMethod("POST");
                addBodyIfExists(urlConnection, request);
                break;
            case HttpMethod.PUT:
                urlConnection.setRequestMethod("PUT");
                addBodyIfExists(urlConnection, request);
                break;
            case HttpMethod.DELETE:
                urlConnection.setRequestMethod("DELETE");
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static void addBodyIfExists(HttpURLConnection connection,
                                        HttpRequest request) throws IOException {
        byte[] body = request.getBody();
        if (body != null) {
            connection.setDoOutput(true);
            connection.addRequestProperty(HEADER_CONTENT_TYPE,
                    request.getBodyContentType());
            DataOutputStream out = new DataOutputStream(
                    connection.getOutputStream());
            out.write(body);
            out.close();
        }
    }


    //HttpResponse
    private HttpResponse responseFromConnection(HttpURLConnection connection) throws IOException {
        HttpResponse response = new HttpResponse();
        //contentStream
        InputStream inputStream;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException ioe) {
            inputStream = connection.getErrorStream();
        }
        int responseCode = connection.getResponseCode();
        if (responseCode == -1) {
            throw new IOException(
                    "Could not retrieve response code from HttpUrlConnection.");
        }
        response.setResponseCode(responseCode);
        response.setResponseMessage(connection.getResponseMessage());
//        response.setContentStream(inputStream);
        response.setContentData(getByteArrayFromInputStream(inputStream));
        response.setContentLength(connection.getContentLength());
        response.setContentEncoding(connection.getContentEncoding());
        response.setContentType(connection.getContentType());
        //header
        Map<String, String> headerMap = new HashMap<>();
        for (Map.Entry<String, List<String>> header : connection.getHeaderFields()
                .entrySet()) {
            if (header.getKey() != null) {
                String value = "";
                for (String v : header.getValue()) {
                    value += (v + "; ");
                }
                headerMap.put(header.getKey(), value);
            }
        }
        response.setHeaders(headerMap);
        return response;
    }

//    private byte[] entityToBytes(HttpResponse kjHttpResponse) throws IOException,
//            IOException {
//        PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(
//                ByteArrayPool.get(), (int) kjHttpResponse.getContentLength());
//        byte[] buffer = null;
//        try {
//            InputStream in = kjHttpResponse.getContentStream();
//            if (in == null) {
//                throw new IOException("server error");
//            }
//            buffer = ByteArrayPool.get().getBuf(1024);
//            int count;
//            while ((count = in.read(buffer)) != -1) {
//                bytes.write(buffer, 0, count);
//            }
//            return bytes.toByteArray();
//        } finally {
//            try {
////                entity.consumeContent();
//                kjHttpResponse.getContentStream().close();
//            } catch (IOException e) {
//                Loger.debug("Error occured when calling consumingContent");
//            }
//            ByteArrayPool.get().returnBuf(buffer);
//            bytes.close();
//        }
//    }

    private byte[] getByteArrayFromInputStream(InputStream is)
            throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        is.close();
        byte[] bytes = os.toByteArray();
        os.close();
        return bytes;
    }


}
