package com.example.newsgateway;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class ArticleLoaderRunnable implements Runnable{
  private static final String TAG = "ArticleLoaderRunnable";
  private final MainActivity mainActivity;
  private final String SOURCE_URL;
  private final String API_KEY;
  private final int position;
  private final Source source;

  public ArticleLoaderRunnable(MainActivity mainActivity, Source source, int position) {
    this.mainActivity = mainActivity;
    this.position = position;
    this.source = source;
    this.API_KEY = mainActivity.getResources().getString(R.string.apiKey);
    this.SOURCE_URL = mainActivity.getResources().getString(R.string.articleUrl);
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public void run() {
    Uri.Builder dataUri = Uri.parse(SOURCE_URL).buildUpon();

    dataUri.appendQueryParameter("sources", source.getId());
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

  @RequiresApi(api = Build.VERSION_CODES.O)
  private void handleResults(final String s){
    if (s == null){
      Log.d(TAG, "handleResults: Failure in data download");
      mainActivity.runOnUiThread(mainActivity::downloadFailed);
      return;
    }
    Log.d(TAG, "handleResults: " + source.getId());
    final ArrayList<Article> articleList = parseJSON(s);
    if (articleList == null){
      mainActivity.runOnUiThread(mainActivity::downloadFailed);
      return;
    }
    Log.d(TAG, "handleResults: " + articleList);
    mainActivity.runOnUiThread(
        () -> mainActivity.getArticles(articleList, position));
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private ArrayList<Article> parseJSON(String s){
    try {
      JSONObject json = new JSONObject(s);
      JSONArray articles = json.getJSONArray("articles");

      ArrayList<Article> articleList = new ArrayList<>();

      for (int i = 0; i < articles.length(); i++) {
        JSONObject article = (JSONObject) articles.get(i);

        String author = null;
        if (article.has("author") || !(article.isNull("author")))
          author = article.getString("author");

        String title = null;
        if (article.has("title")|| !(article.isNull("title")))
          title = article.getString("title");

        String desc = null;
        if (article.has("description") || !(article.isNull("description")))
          desc = article.getString("description");

        String url = null;
        if (article.has("url") || !(article.isNull("url")))
          url = article.getString("url");

        String imgUrl = null;
        if (article.has("urlToImage") || !(article.isNull("urlToImage")))
          imgUrl = article.getString("urlToImage");

        String date = null;
        if (article.has("publishedAt") || !(article.isNull("publishedAt")))
          date = convertDate(article.getString("publishedAt"));


        Article ss = new Article(author, title, desc, url, imgUrl, date);

        articleList.add(ss);
      }
      return articleList;
    } catch (Exception e) {
      Log.d(TAG, "parseJSON: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  @SuppressLint("SimpleDateFormat")
  @RequiresApi(api = Build.VERSION_CODES.O)
  private String convertDate(String zDate) throws ParseException {
    String fFormat = "MMM dd uuuu, kk:mm";
    String inputFormat = "yyyy-MM-dd'T'HH:mm:ss";
    String temp = zDate.substring(0,19);

    Date date = new SimpleDateFormat(inputFormat).parse(temp);
    assert date != null;
    return new SimpleDateFormat(fFormat).format(date);
  }
}
