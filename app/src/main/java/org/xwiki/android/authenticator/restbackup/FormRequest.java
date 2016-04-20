/*
 * Copyright (c) 2014, 张涛.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xwiki.android.authenticator.restbackup;

import org.xwiki.android.authenticator.utils.Loger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Form http request
 * Created by fitz on 2016/4/16.
 */
public class FormRequest extends Request<byte[]> {

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
