package com.example.newsgateway;

public class Article {

  private String author;
  private String title;
  private String desc;
  private String url;
  private String imgUrl;
  private String date;

  Article(String author, String title, String desc, String url, String imgUrl, String date){
    this.author = author;
    this.title = title;
    this.desc = desc;
    this.url = url;
    this.imgUrl = imgUrl;
    this.date = date;
  }

  String getAuthor() { return author; }
  String getTitle() { return title; }
  String getDesc() { return desc; }
  String getUrl() { return url; }
  String getImgUrl() { return imgUrl; }
  String getDate() {return date; }
}
