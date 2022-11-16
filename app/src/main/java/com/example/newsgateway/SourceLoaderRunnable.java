package com.example.newsgateway;

import android.net.Uri;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class SourceLoaderRunnable implements Runnable{

  private static final String TAG = "SourceLoaderRunnable";
  private final MainActivity mainActivity;
  private final String SOURCE_URL;
  private final String API_KEY;

  public SourceLoaderRunnable(MainActivity mainActivity) {
    this.mainActivity = mainActivity;
    this.SOURCE_URL = mainActivity.getResources().getString(R.string.sourceUrl);
    this.API_KEY = mainActivity.getResources().getString(R.string.apiKey);
  }

  @Override
  public void run() {
    Uri.Builder dataUri = Uri.parse(SOURCE_URL).buildUpon();

    dataUri.appendQueryParameter("apiKey", API_KEY);
    String urlToUse = dataUri.build().toString();
    Log.d(TAG, "run: creating url to use " + urlToUse);

    StringBuilder sb = new StringBuilder();

    try{
      URL url = new URL(urlToUse);
      HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      conn.setRequestProperty("Accept", "application/json");
      conn.addRequestProperty("User-Agent", "");
      conn.connect();

      if (conn.getResponseCode() != HttpURLConnection.HTTP_OK){
        Log.d(TAG, "run: HTTP ResponseCode NOT OK " + conn.getResponseCode());
        handleResults(null);
        return;
      }

      InputStream is = conn.getInputStream();
      BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

      String line;
      while ((line = reader.readLine()) != null){
        sb.append(line).append("\n");
      }

    } catch (Exception e){
      Log.e(TAG, "run catch block: " + e);
      handleResults(null);
    }
    handleResults(sb.toString());
  }

  private void handleResults(final String s){
    if (s == null){
      Log.d(TAG, "handleResults: Failure in data download");
      mainActivity.runOnUiThread(mainActivity::downloadFailed);
      return;
    }

    final ArrayList<Source> sourcesList = parseJSON(s);
    if (sourcesList == null){
      mainActivity.runOnUiThread(mainActivity::downloadFailed);
      return;
    }

    mainActivity.runOnUiThread(
        () -> mainActivity.updateData(sourcesList));
  }

  private ArrayList<Source> parseJSON(String s) {
    try {
      InputStream cs =
          mainActivity.getResources().openRawResource(R.raw.country_codes);
      BufferedReader cReader = new BufferedReader(new InputStreamReader(cs));

      StringBuilder countryResult = new StringBuilder();
      for (String line; (line = cReader.readLine()) != null; ) {
        countryResult.append(line);
      }

      InputStream ls =
          mainActivity.getResources().openRawResource(R.raw.language_codes);
      BufferedReader lReader = new BufferedReader(new InputStreamReader(ls));

      StringBuilder languageResult = new StringBuilder();
      for (String line; (line = lReader.readLine()) != null; ) {
        languageResult.append(line);
      }

      JSONObject json = new JSONObject(s);
      JSONArray sources = json.getJSONArray("sources");

      ArrayList<Source> sourceList = new ArrayList<>();

      for (int i = 0; i < sources.length(); i++) {
        JSONObject source = (JSONObject) sources.get(i);

        String id = source.getString("id");

        String name = source.getString("name");

        String category = source.getString("category");

        String lang = convertLanguageCode(source.getString("language"), languageResult.toString());

        String country = convertCountryCodes(source.getString("country"), countryResult.toString());

        Source ss = new Source(id, name, category, lang, country);

        sourceList.add(ss);
      }
      return sourceList;
    } catch (Exception e) {
      Log.d(TAG, "parseJSON: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  private String convertCountryCodes(String country, String result){
    String code = country.toUpperCase();
    try{
      JSONObject json = new JSONObject(result);
      JSONArray countries = json.getJSONArray("countries");

      for (int i = 0; i < countries.length(); i++){
        JSONObject c = (JSONObject) countries.get(i);
        if (c.getString("code").equals(code))
          return c.getString("name");
      }
    } catch (Exception e){
      e.printStackTrace();
    }
    return "";
  }

  private String convertLanguageCode(String lang, String result){
    String code = lang.toUpperCase();
    try{
      JSONObject json = new JSONObject(result);
      JSONArray languages = json.getJSONArray("languages");

      for (int i = 0; i < languages.length(); i++){
        JSONObject l = (JSONObject) languages.get(i);
        if (l.getString("code").equals(code))
          return l.getString("name");
      }
    } catch (Exception e){
      e.printStackTrace();
    }
    return "";
  }
}
