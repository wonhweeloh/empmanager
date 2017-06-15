package com.wonhwee.empmgr.client;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by wloh on 6/14/17.
 */

public class HttpClient {
  public static final String module = HttpClient.class.getSimpleName();

  private static StringBuilder sb = new StringBuilder();

  public static String getResponse(String url){
    HttpURLConnection connection = null;
    InputStream inputStream = null;
    String line;
    StringBuilder sb = new StringBuilder();

    try {
      URL endPointUrl;
      endPointUrl = new URL(url);
      connection = (HttpURLConnection) endPointUrl.openConnection();
      connection.setConnectTimeout(30000);
      connection.setReadTimeout(30000);
      connection.setRequestMethod("GET");
      connection.connect();

      inputStream = connection.getInputStream();

      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
    } catch (MalformedURLException mfex){
      Log.e(module, "HTTP failed to fetch data:" + mfex);
      return null;
    } catch (IOException ioex){
      Log.e(module, "HTTP failed to fetch data:" + ioex);
      return null;
    } finally {
      try {
        if(inputStream != null) {
          inputStream.close();
        }

        if(connection != null) {
          connection.disconnect();
        }
      }catch(Exception e){
        Log.e(module, "Failed to release inputstream or disconnect:" + e);
      }
    }

    Log.d(module, sb.toString());

    return sb.toString();
  }
}
