package com.example.frontend;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ServerCommAsync {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    public Call post(String url, String json, Callback callback) {
        RequestBody body = RequestBody.create(json,JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }
    public Call postWithAuthentication(String url, String json,String userToken, Callback callback) {
        RequestBody body = RequestBody.create(json,JSON);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization","Bearer "+ userToken)
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }
    public Call putWithAuthentication(String url, String json,String userToken, Callback callback) {
        RequestBody body = RequestBody.create(json,JSON);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization","Bearer "+ userToken)
                .put(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public Call getWithAuthentication(String url,String userToken, Callback callback) {

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization","Bearer "+ userToken)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }
}
