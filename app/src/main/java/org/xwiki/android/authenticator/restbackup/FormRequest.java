package org.xwiki.android.authenticator.restbackup;

import org.xwiki.android.authenticator.utils.Loger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Form http request
 * Created by fitz on 2016/4/16.
 */
public class FormRequest extends Request{

    private final HttpParams mParams;

    public FormRequest(String url, HttpCallBack callback) {
        this(HttpMethod.GET, url, null, callback);
    }

    public FormRequest(int httpMethod, String url, HttpParams params,
                       HttpCallBack callback) {
        super(httpMethod, url, callback);
        if (params == null) {
            params = new HttpParams();
        }
        this.mParams = params;
    }

    @Override
    public String getCacheKey() {
        if (getMethod() == HttpMethod.POST) {
            return getUrl() + mParams.getUrlParams();
        } else {
            return getUrl();
        }
    }

    @Override
    public String getBodyContentType() {
        if (mParams.getContentType() != null) {
            return mParams.getContentType();
        } else {
            return super.getBodyContentType();
        }
    }

    @Override
    public Map<String, String> getHeaders() {
        return mParams.getHeaders();
    }

    @Override
    public byte[] getBody() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            mParams.writeTo(bos);
        } catch (IOException e) {
            Loger.debug("FormRequest75--->IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

}
