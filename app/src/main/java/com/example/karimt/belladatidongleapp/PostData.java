package com.example.karimt.belladatidongleapp;


import android.util.Log;

import com.belladati.httpclientandroidlib.HttpEntity;
import com.belladati.httpclientandroidlib.client.config.RequestConfig;
import com.belladati.httpclientandroidlib.client.methods.CloseableHttpResponse;
import com.belladati.httpclientandroidlib.client.methods.HttpPost;
import com.belladati.httpclientandroidlib.config.RegistryBuilder;
import com.belladati.httpclientandroidlib.conn.socket.ConnectionSocketFactory;
import com.belladati.httpclientandroidlib.conn.socket.PlainConnectionSocketFactory;
import com.belladati.httpclientandroidlib.conn.ssl.SSLConnectionSocketFactory;
import com.belladati.httpclientandroidlib.conn.ssl.SSLContexts;
import com.belladati.httpclientandroidlib.conn.ssl.TrustSelfSignedStrategy;
import com.belladati.httpclientandroidlib.entity.ByteArrayEntity;
import com.belladati.httpclientandroidlib.impl.client.CloseableHttpClient;
import com.belladati.httpclientandroidlib.impl.client.cache.CacheConfig;
import com.belladati.httpclientandroidlib.impl.client.cache.CachingHttpClientBuilder;
import com.belladati.httpclientandroidlib.impl.conn.PoolingHttpClientConnectionManager;
import com.belladati.sdk.exception.ConnectionException;
import com.belladati.sdk.exception.server.UnexpectedResponseException;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.codec.binary.Base64;


/**
 * Created by KarimT on 19.09.2016.
 * class for send data to IoT Data Collector
 */
public class PostData {


    private CloseableHttpClient client;

    public PostData() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        this.client = buildClient(true);
    }

    public void postToCollector(String url, ObjectNode objectNode) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        HttpPost post = new HttpPost(url);
        byte[] bytes = objectNode.toString().getBytes("UTF-8");
        ByteArrayEntity entity = new ByteArrayEntity(bytes);
        entity.setContentType("application/octet-stream");
        byte[] encodedBytes=Base64.encodeBase64("sensor:BellaDati01".getBytes());
        post.setEntity(entity);
        post.addHeader("Authorization","Basic "+ new String(encodedBytes));
        CloseableHttpResponse response = null;

        try {
            response = client.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity httpEntity = response.getEntity();
            switch (statusCode) {
                case 200:
                    return;
                default:
                    byte[] content = httpEntity != null ? readBytes(httpEntity.getContent()) : new byte[0];
                    throw new UnexpectedResponseException(statusCode, new String(content));
            }
        }
        catch (Exception e)
        {
            Log.e("error",e.getMessage());
        }
        finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                throw new ConnectionException("Failed to connect to BellaDati", e);
            }
            post.releaseConnection();
        }

    }
    private static byte[] readBytes(InputStream in) throws IOException {
        int len;
        byte[] buffer = new byte[128];
        ByteArrayOutputStream buf = new ByteArrayOutputStream(8192);
        while ((len = in.read(buffer, 0, buffer.length)) != -1) {
            buf.write(buffer, 0, len);
        }
        buf.flush();
        return buf.toByteArray();
    }

    private CloseableHttpClient buildClient(boolean trustSelfSigned) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        // set timeouts for the HTTP client

        SSLContext sslContext = !trustSelfSigned ? SSLContexts.createSystemDefault() : SSLContexts.custom()
                .loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
        int globalTimeout = 10000;
        int connectTimeout = 10000;
        int connectionRequestTimeout = 10000;
        int socketTimeout = 10000;
        RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT).setConnectTimeout(connectTimeout)
                .setSocketTimeout(socketTimeout).setConnectionRequestTimeout(connectionRequestTimeout).build();

        // configure caching
        CacheConfig cacheConfig = CacheConfig.copy(CacheConfig.DEFAULT).setSharedCache(false).setMaxCacheEntries(1000)
                .setMaxObjectSize(2 * 1024 * 1024).build();

        // configure connection pooling
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(RegistryBuilder
                .<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https",new SSLConnectionSocketFactory(sslContext,SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)).build());
        int connectionLimit = 40;
        // there's only one server to connect to, so max per route matters
        connManager.setMaxTotal(connectionLimit);
        connManager.setDefaultMaxPerRoute(connectionLimit);

        // create the HTTP client
        return CachingHttpClientBuilder.create().setCacheConfig(cacheConfig).setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connManager).build();

    }

}
