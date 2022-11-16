package com.example.newsgateway;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class Source
    implements Comparable<Source>, Serializable{

  private String id;
  private String name;
  private String category;
  private String lang;
  private String country;
  private ArrayList<Article> articles;

  Source(String id, String name, String category, String lang, String country){
    this.id = id;
    this.name = name;
    this.category = category;
    this.lang = lang;
    this.country = country;
    this.articles = new ArrayList<>();
  }

  String getId() { return id; }
  String getName() { return name; }
  String getCategory() { return category; }
  String getLang() { return lang; }
  String getCountry() { return country; }
  ArrayList<Article> getArticles() { return articles; }

  void setArticles(ArrayList<Article> xs){
    this.articles = xs;
  }

  @Override
  public int compareTo(Source o) {
    return name.compareTo(o.name);
  }

  @Override
  public String toString() { return name; }
}
