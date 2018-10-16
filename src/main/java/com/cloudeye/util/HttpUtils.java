package com.cloudeye.util;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lafangyuan on 2017/12/13.
 */
public class HttpUtils {

    public static synchronized String post(String url, String content) {
        CloseableHttpClient httpClient = null;
        StringBuilder sb = new StringBuilder();
        try {
            httpClient = getClinet();
            HttpPost postRequest = new HttpPost(url);// "http://localhost:8080/RESTfulExample/json/product/post"
            postRequest.addHeader("charset", "utf-8");

            //"{\"id\":\""+id+"\"}"
            StringEntity input = new StringEntity(content, ContentType.APPLICATION_JSON);
            postRequest.setEntity(input);

            //httpClient.execute(postRequest);
            HttpResponse response = httpClient.execute(postRequest);

            if (response.getStatusLine().getStatusCode() == 200) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = null;
                while ((line = rd.readLine()) != null) {
                    //System.out.println(line);
                    sb.append(line);
                }

            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
//            log.error("error:", e.getCause());
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        }
        return sb.toString();
//        System.out.println(url);
//        return null;
    }
    public static synchronized String httpPost(String url,Map<String,String> params){
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        for(String key:params.keySet()){
            formParams.add(new BasicNameValuePair(key, params.get(key)));
        }
        try {
            UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(formParams,"UTF-8");
            httpPost.setEntity(uefEntity);
            CloseableHttpResponse response = getClinet().execute(httpPost);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static synchronized String httpGet(String url) {
        HttpGet httpGet = new HttpGet(url);
        try {
            CloseableHttpResponse response = getClinet().execute(httpGet);
            HttpEntity entity = response.getEntity();
            return EntityUtils.toString(entity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private synchronized  static CloseableHttpClient getClinet(){
        HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {

            public boolean retryRequest(
                    IOException exception,
                    int executionCount,
                    HttpContext context) {
                if (executionCount >= 4) {
                    // Do not retry if over max retry count
                    return false;
                }
                if (exception instanceof InterruptedIOException) {
                    // Timeout
                    return false;
                }
                if (exception instanceof UnknownHostException) {
                    // Unknown host
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {
                    // Connection refused
                    return false;
                }
                if (exception instanceof SSLException) {
                    // SSL handshake exception
                    return false;
                }
                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
                if (idempotent) {
                    // Retry if the request is considered idempotent
                    return true;
                }
                return false;
            }

        };
        CloseableHttpClient httpclient = HttpClients.custom()
                .setRetryHandler(myRetryHandler)
                .build();
        return httpclient;
    }
    
    
    
    
    


}
