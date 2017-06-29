package triangle.triangleapp.persistence.stream.impl;

import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.Excluder;

import org.spongycastle.openssl.PEMException;
import org.spongycastle.openssl.jcajce.JcaPEMWriter;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.security.PrivateKey;
import java.security.PublicKey;

import triangle.triangleapp.TriangleApplication;
import triangle.triangleapp.domain.KeySerializer;
import triangle.triangleapp.helpers.ConfigHelper;
import triangle.triangleapp.logic.StreamCallback;
import triangle.triangleapp.logic.StreamManager;
import triangle.triangleapp.persistence.ConnectionCallback;
import triangle.triangleapp.helpers.MultipartRequest;
import triangle.triangleapp.persistence.ViewersCallback;
import triangle.triangleapp.persistence.stream.StreamAdapter;

/**
 * Created by Kevin Ly on 6/16/2017.
 */

public class HttpStream implements StreamAdapter {
    private static final String TAG = "HttpStream";
    private static final String URL = ConfigHelper.getInstance().get(ConfigHelper.KEY_WEBAPI_DESTINATION_ADDRESS);
    private RequestQueue mRequestQueue;
    private String id;
    private Gson mGsonInstance;
    private StreamCallback mStreamCallback;

    /**
     * Initializing HttpStream
     */
    public HttpStream(StreamCallback streamCallback) {
        mRequestQueue = Volley.newRequestQueue(TriangleApplication.getAppContext());
        mStreamCallback = streamCallback;
        GsonBuilder builder = new GsonBuilder();
        mGsonInstance = builder.create();
    }

    @Override
    public void sendPublicKey(@NonNull PublicKey publicKey) {
        StringWriter stringWriter = new StringWriter();
        JcaPEMWriter writer = new JcaPEMWriter(stringWriter);
        try {
            writer.writeObject(publicKey);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        final String pubKey = stringWriter.toString();
        final String completeUrl = URL + "stream/sendKey/" + id;

        KeySerializer keySerializer = new KeySerializer(pubKey, ConfigHelper.getInstance().get(ConfigHelper.KEY_USERNAME));
        final String pubKeyJsonObj = mGsonInstance.toJson(keySerializer);

        StringRequest keyRequest = new StringRequest(Request.Method.PUT, completeUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, "Done sending public key to server.");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error sending public key to server.", error);
                mStreamCallback.onConnectError(new ConnectException("Could not send public key to server."), true);

            }
        }){
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return pubKeyJsonObj.toString().getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "Error while get bytes from public key.", e);
                    mStreamCallback.onConnectError(new PEMException("Could not get bytes from public key."), true);
                    return null;
                }
            }
        };

        mRequestQueue.add(keyRequest);
    }

    @Override
    public void connect(final ConnectionCallback callback) {
        final String completeUrl = URL + "stream/connect/";

        StringRequest idRequest = new StringRequest(Request.Method.GET, completeUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                id = response.substring(1, response.length() - 1);
                Log.i(TAG, "ID = " + id);
                callback.onConnected();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error getting ID", error);
                mStreamCallback.onConnectError(new ConnectException("Could not get ID from server."), true);
            }
        });

        mRequestQueue.add(idRequest);
    }

    @Override
    public void sendFile(@NonNull byte[] fileInBytes, @NonNull PrivateKey privateKey) {
        try {
            final String completeUrl = URL + "stream/send/" + id;

            MultipartRequest multipartRequest = new MultipartRequest(completeUrl, null, "application/octet-stream", fileInBytes, new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    Log.i(TAG, "Done sending data");

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error sending data", error);
                    mStreamCallback.onSendError(new ConnectException("Could not send stream data to server."), false);
                }
            });

            mRequestQueue.add(multipartRequest);


        } catch (Exception ex) {
            Log.e(TAG, "Error occured while send request via Volley", ex);
            mStreamCallback.onSendError(new ConnectException("Could not send stream data to server."), false);
        }
    }

    @Override
    public void sendText(@NonNull String text) {

    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void getViewers(final ViewersCallback callback) {
        final String completeUrl = URL + "stream/getViewers/" + id;

        StringRequest getViewersAmountRequest = new StringRequest(Request.Method.GET, completeUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.i(TAG, "Viewers: " + response);
                    callback.getViewersCount(Integer.parseInt(response));
                } catch (Exception ex){
                    Log.e(TAG, "Error while parse integer of viewers (response)", ex);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error getting viewers", error);
            }
        });

        mRequestQueue.add(getViewersAmountRequest);
    }
}
