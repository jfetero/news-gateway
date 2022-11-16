package com.example.newsgateway;

import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ArticleViewHolder extends RecyclerView.ViewHolder {
  TextView title;
  TextView date;
  TextView author;
  ImageView img;
  TextView desc;
  TextView pageNum;
  ProgressBar pBar;

  public ArticleViewHolder(@NonNull View itemView) {
    super(itemView);
    title = itemView.findViewById(R.id.articleTitle);
    date = itemView.findViewById(R.id.articleDate);
    author = itemView.findViewById(R.id.articleAuthor);
    img = itemView.findViewById(R.id.articleImage);
    desc = itemView.findViewById(R.id.articleDescription);
    pageNum = itemView.findViewById(R.id.pageNumber);

    desc.setMovementMethod(new ScrollingMovementMethod());
  }
}
