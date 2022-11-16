package com.example.newsgateway;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleViewHolder> {
  private final MainActivity mainAct;
  private final ArrayList<Article> articleList;
  private Picasso picasso;

  public ArticleAdapter(MainActivity mainAct, ArrayList<Article> articleList){
    this.mainAct = mainAct;
    this.articleList = articleList;
    this.picasso = Picasso.get();
  }

  @NonNull
  @Override
  public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new ArticleViewHolder(
        LayoutInflater.from(parent.getContext()).
            inflate(R.layout.article_entry, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
    Article a = articleList.get(position);

    if(a.getTitle() == null)
      holder.title.setVisibility(View.GONE);
    else
      holder.title.setText(a.getTitle());

    if (a.getDate() == null)
      holder.date.setVisibility(View.GONE);
    else
      holder.date.setText(a.getDate());

    if (a.getAuthor() == null)
      holder.author.setVisibility(View.GONE);
    else
      holder.author.setText(a.getAuthor());

    if (a.getDesc() == null)
      holder.desc.setVisibility(View.GONE);
    else
      holder.desc.setText(a.getDesc());

    holder.pageNum.setText(String.format(
        Locale.getDefault(), "%d of %d", (position + 1), articleList.size()));

    if (a.getImgUrl() == null)
      holder.img.setImageResource(R.drawable.noimage);
    else
      picasso.load(a.getImgUrl())
        .error(R.drawable.brokenimage)
        .placeholder(R.drawable.loading)
        .into(holder.img);

    holder.img.setOnClickListener(v -> onClick(a.getUrl()));
    holder.title.setOnClickListener(v -> onClick(a.getUrl()));
    holder.desc.setOnClickListener(v -> onClick(a.getUrl()));
  }

  @Override
  public int getItemCount() {
    return articleList.size();
  }

  private void onClick(String url){
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    mainAct.startActivity(intent);
  }
}
