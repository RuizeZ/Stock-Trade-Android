package com.csci571.stocks;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.MyViewHolder> {
    Context context;
    List<newsModel> newsModelList;

    public NewsAdapter(Context context, List<newsModel> newsModelList) {
        this.context = context;
        this.newsModelList = newsModelList;
    }

    @NonNull
    @Override
    public NewsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recyclerviewrow, parent, false);
        return new NewsAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsAdapter.MyViewHolder holder, int position) {
        holder.newsSource.setText(newsModelList.get(position).newsSource);
        holder.newsTime.setText(newsModelList.get(position).newsTime);
        holder.newsTitle.setText(newsModelList.get(position).newsTitle);
        Glide.with(context)
                .load(newsModelList.get(position).newsImgURL)
                .into(holder.newsImg);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(newsModelList.get(holder.getAdapterPosition()).newsURL);
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.newsdialog);
                TextView newsDialog_source = dialog.findViewById(R.id.newsDialog_source);
                newsDialog_source.setText(newsModelList.get(holder.getAdapterPosition()).newsSource);
                TextView newsDialog_time = dialog.findViewById(R.id.newsDialog_time);
                newsDialog_time.setText(newsModelList.get(holder.getAdapterPosition()).newsDate);
                TextView newsDialog_tital = dialog.findViewById(R.id.newsDialog_tital);
                newsDialog_tital.setText(newsModelList.get(holder.getAdapterPosition()).newsTitle);
                TextView newsDialog_content = dialog.findViewById(R.id.newsDialog_content);
                newsDialog_content.setText(newsModelList.get(holder.getAdapterPosition()).newsSummary);
//              // open company site
                ImageButton imageButton_chrome = dialog.findViewById(R.id.imageButton_chrome);
                imageButton_chrome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        context.startActivity(intent);
                    }
                });
                // open company site
                ImageButton imageButton_twitter = dialog.findViewById(R.id.imageButton_twitter);
                imageButton_twitter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newUrl = "https://twitter.com/intent/tweet?text="+newsModelList.get(holder.getAdapterPosition()).newsTitle+" "+uri;
                        Uri uri = Uri.parse(newUrl);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        context.startActivity(intent);
                    }
                });
                // open company site
                ImageButton imageButton_facebook = dialog.findViewById(R.id.imageButton_facebook);
                imageButton_facebook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newUrl = "https://www.facebook.com/sharer/sharer.php?u="+uri+"&amp;src=sdkpreparse";
                        Uri uri = Uri.parse(newUrl);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        context.startActivity(intent);
                    }
                });
                dialog.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return this.newsModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView newsImg;
        TextView newsSource, newsTime, newsTitle;
        View itemView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            newsImg = itemView.findViewById(R.id.newsImg);
            newsSource = itemView.findViewById(R.id.newsSource);
            newsTime = itemView.findViewById(R.id.newsTime);
            newsTitle = itemView.findViewById(R.id.newsTitle);
            this.itemView = itemView;
        }
    }
}
