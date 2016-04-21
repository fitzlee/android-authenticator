package org.xwiki.android.authenticator.restbackup;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xwiki.android.authenticator.restbackup.Request.HttpMethod;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * HttpUrlConnection方式实现
 *
 * @author kymjs (http://www.kymjs.com/) .
 */
public class HttpConnect{

    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private final UrlRewriter mUrlRewriter;
    private final SSLSocketFactory mSslSocketFactory;

    public interface UrlRewriter {
        /**
         * 重写用于请求的URL
         */
        public String rewriteUrl(String originalUrl);
    }

    public HttpConnect() {
        this(null);
    }

    public HttpConnect(UrlRewriter urlRewriter) {
        this(urlRewriter, null);
    }

    public HttpConnect(UrlRewriter urlRewriter,
                       SSLSocketFactory sslSocketFactory) {
        mUrlRewriter = urlRewriter;
        mSslSocketFactory = sslSocketFactory;
    }

    public HttpResponse performRequest(Request<?> request,
                                       Map<String, String> additionalHeaders) throws IOException {
        String url = request.getUrl();
        HashMap<String, String> map = new HashMap<String, String>();
        map.putAll(request.getHeaders());
        map.putAll(additionalHeaders);

        if (mUrlRewriter != null) {
            String rewritten = mUrlRewriter.rewriteUrl(url);
            if (rewritten == null) {
                throw new IOException("URL blocked by rewriter: " + url);
            }
            url = rewritten;
        }
        URL parsedUrl = new URL(url);
        HttpURLConnection connection = openConnection(parsedUrl, request);
        for (String headerName : map.keySet()) {
            connection.addRequestProperty(headerName, map.get(headerName));
        }
        setConnectionParametersForRequest(connection, request);
        HttpResponse response = responseFromConnection(connection);
        return response;
    }

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
        
        response.setContentStream(inputStream);

        response.setContentLength(connection.getContentLength());
        response.setContentEncoding(connection.getContentEncoding());
        response.setContentType(connection.getContentType());
        //header
        Map<String, String> headerMap = new HashMap<>();
        for (Entry<String, List<String>> header : connection.getHeaderFields()
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



    private HttpURLConnection openConnection(URL url, Request<?> request)
            throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        int timeoutMs = request.getTimeoutMs();
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
                //信任所有证书
                HTTPSTrustManager.allowAllSSL();
            }
        }
        return connection;
    }

    /* package */
    static void setConnectionParametersForRequest(
            HttpURLConnection connection, Request<?> request)
            throws IOException {
        switch (request.getMethod()) {
            case HttpMethod.GET:
                connection.setRequestMethod("GET");
                break;
            case HttpMethod.DELETE:
                connection.setRequestMethod("DELETE");
                break;
            case HttpMethod.POST:
                connection.setRequestMethod("POST");
                addBodyIfExists(connection, request);
                break;
            case HttpMethod.PUT:
                connection.setRequestMethod("PUT");
                addBodyIfExists(connection, request);
                break;
            case HttpMethod.HEAD:
                connection.setRequestMethod("HEAD");
                break;
            case HttpMethod.OPTIONS:
                connection.setRequestMethod("OPTIONS");
                break;
            case HttpMethod.TRACE:
                connection.setRequestMethod("TRACE");
                break;
            case HttpMethod.PATCH:
                connection.setRequestMethod("PATCH");
                addBodyIfExists(connection, request);
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    /**
     * 如果有body则添加
     */
    private static void addBodyIfExists(HttpURLConnection connection,
                                        Request<?> request) throws IOException {
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
}
