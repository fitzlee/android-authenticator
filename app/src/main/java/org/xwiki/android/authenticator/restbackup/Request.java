package org.xwiki.android.authenticator.restbackup;

import android.net.TrafficStats;
import android.net.Uri;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

public abstract class Request implements Comparable{

    /**
     * 默认编码 {@link #getParamsEncoding()}.
     */
    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

    /**
     * Http请求超时时间
     **/
    public static int TIMEOUT = 5000;

    /**
     * 支持的请求方式
     */
    public interface HttpMethod {
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTIONS = 5;
        int TRACE = 6;
        int PATCH = 7;
    }

    private static final long SLOW_REQUEST_THRESHOLD_MS = 3000; // 请求超时时间

    private final String mUrl;
    private final int mDefaultTrafficStatsTag; // 默认tag {@link TrafficStats}
    private Integer mSequence; // 本次请求的优先级

    private final int mMethod; // 请求方式
    private final long mRequestBirthTime = 0;// 用于转储慢的请求。

    private boolean mShouldCache = true; // 是否缓存本次请求
    private boolean mCanceled = false; // 是否取消本次请求
    private boolean mResponseDelivered = false; // 是否再次分发本次响应

    protected final HttpCallBack mCallback;

    public Request(int method, String url, HttpCallBack callback) {
        mMethod = method;
        mUrl = url;
        mCallback = callback;

        mDefaultTrafficStatsTag = findDefaultTrafficStatsTag(url);
    }

    public HttpCallBack getCallback() {
        return mCallback;
    }

    public int getMethod() {
        return mMethod;
    }


    /**
     * @return A tag for use with {@link TrafficStats#setThreadStatsTag(int)}
     */
    public int getTrafficStatsTag() {
        return mDefaultTrafficStatsTag;
    }

    /**
     * @return The hashcode of the URL's host component, or 0 if there is none.
     */
    private static int findDefaultTrafficStatsTag(String url) {
        if (!TextUtils.isEmpty(url)) {
            Uri uri = Uri.parse(url);
            if (uri != null) {
                String host = uri.getHost();
                if (host != null) {
                    return host.hashCode();
                }
            }
        }
        return 0;
    }

    public final Request setSequence(int sequence) {
        mSequence = sequence;
        return this;
    }

    public final int getSequence() {
        if (mSequence == null) {
            throw new IllegalStateException(
                    "getSequence called before setSequence");
        }
        return mSequence;
    }

    public String getUrl() {
        return mUrl;
    }

    public abstract String getCacheKey();

    public void cancel() {
        mCanceled = true;
    }

    public void resume() {
        mCanceled = false;
    }

    public boolean isCanceled() {
        return mCanceled;
    }

    public Map<String, String> getParams() {
        return null;
    }

    public Map<String, String> getHeaders() {
        return Collections.emptyMap();
    }

    protected String getParamsEncoding() {
        return DEFAULT_PARAMS_ENCODING;
    }

    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset="
                + getParamsEncoding();
    }

    /**
     * 返回Http请求的body
     */
    public byte[] getBody() {
        Map<String, String> params = getParams();
        if (params != null && params.size() > 0) {
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }

    /**
     * 对中文参数做URL转码
     */
    private byte[] encodeParameters(Map<String, String> params,
                                    String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(),
                        paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(),
                        paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: "
                    + paramsEncoding, uee);
        }
    }

    public final Request setShouldCache(boolean shouldCache) {
        mShouldCache = shouldCache;
        return this;
    }

    public boolean shouldCache() {
        return mShouldCache;
    }

    /**
     * 本次请求的优先级，四种
     */
    public enum Priority {
        LOW, NORMAL, HIGH, IMMEDIATE
    }

    public Priority getPriority() {
        return Priority.NORMAL;
    }

    public final int getTimeoutMs() {
        return TIMEOUT;
    }

    /**
     * 标记为已经分发过的
     */
    public void markDelivered() {
        mResponseDelivered = true;
    }

    /**
     * 是否已经被分发过
     */
    public boolean hasHadResponseDelivered() {
        return mResponseDelivered;
    }

    /**
     * Http请求成功后，在异步调用本方法，本方法执行完成才会继续调用onSuccess()
     *
     * @param t 请求成功后的数据
     */
    protected void onAsyncSuccess(byte[] t) {
        if (mCallback != null) {
            mCallback.onSuccessInAsync(t);
        }
    }

    /**
     * Http请求完成(不论成功失败)
     */
    public void requestFinish() {
        mCallback.onFinish();
    }



    @Override
    public String toString() {
        String trafficStatsTag = "0x"
                + Integer.toHexString(getTrafficStatsTag());
        return (mCanceled ? "[X] " : "[ ] ") + getUrl() + " " + trafficStatsTag
                + " " + getPriority() + " " + mSequence;
    }
}
