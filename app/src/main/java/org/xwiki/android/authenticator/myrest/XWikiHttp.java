package org.xwiki.android.authenticator.myrest;

import android.util.Base64;

import org.xwiki.android.authenticator.bean.SearchResult;
import org.xwiki.android.authenticator.bean.XWikiUsers;
import org.xwiki.android.authenticator.rest.XmlUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iof on 2016/4/25.
 */
public class XWikiHttp {
    //http://xwiki.org/xwiki/rest
    private final String serverRestPreUrl = "http://xwikichina.com/xwiki/rest";

    public byte[] login(String username, String password) throws IOException {
        HttpConnector httpConnector = new HttpConnector();
        String url = serverRestPreUrl;
        HttpRequest request = new HttpRequest(url);
        String basicAuth = username + ":" + password;
        basicAuth = "Basic " + new String(Base64.encodeToString(basicAuth.getBytes(), Base64.NO_WRAP));
        request.httpParams.putHeaders("Authorization", basicAuth);
        HttpResponse response = httpConnector.performRequest(request);
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        return response.getContentData();
    }

    //http://xwiki.org/xwiki/rest/wikis/query?q=wiki:xwiki%20and%20object:XWiki.XWikiUsers&number=10
    public List<XWikiUsers> getUserList(String wiki, int number) throws IOException{
        String url = serverRestPreUrl + "/wikis/query?q=wiki:"+wiki+"%20and%20object:XWiki.XWikiUsers&number="+number;
        HttpResponse response = new HttpConnector().performRequest(new HttpRequest(url));
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        List<XWikiUsers> userList = new ArrayList<>();
        List<SearchResult> searchlist = XmlUtils.getSearchResults(new ByteArrayInputStream(response.getContentData()));
        for(SearchResult item : searchlist){
            XWikiUsers user = getUserDetail(item.pageName);
            userList.add(user);
        }
        return userList;
    }

    public XWikiUsers getUserDetail(String username) throws IOException{
        String url = serverRestPreUrl + "/wikis/xwiki/spaces/XWiki/pages/"+username+"/objects/XWiki.XWikiUsers/0";
        HttpRequest request = new HttpRequest(url);
        HttpConnector httpConnector = new HttpConnector();
        HttpResponse response = httpConnector.performRequest(request);
        int statusCode = response.getResponseCode();
        if (statusCode < 200 || statusCode > 299) {
            throw new IOException("statusCode="+statusCode+",response="+response.getResponseMessage());
        }
        XWikiUsers user = XmlUtils.getXWikiUsers(new ByteArrayInputStream(response.getContentData()));
        return user;
    }

    public Boolean updateUser(XWikiUsers user) throws IOException{
        String url = serverRestPreUrl + "/wikis/xwiki/spaces/XWiki/pages/"+user.id+"/objects/XWiki.XWikiUsers/0";
        HttpRequest request = new HttpRequest(url, HttpRequest.HttpMethod.PUT, null);
        request.httpParams.putHeaders("","");

        HttpConnector httpConnector = new HttpConnector();
        HttpResponse response = httpConnector.performRequest(request);
        return true;
    }

}
