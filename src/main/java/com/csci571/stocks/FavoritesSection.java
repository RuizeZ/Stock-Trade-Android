package com.csci571.stocks;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

import java.util.*;

public class FavoritesSection extends Section {
    private final List<FavoritesModel> list;


    FavoritesSection(List<FavoritesModel> list) {

        super(SectionParameters.builder()
                .itemResourceId(R.layout.favorites_item)
                .headerResourceId(R.layout.favorites_header)
                .build());
        this.list = list;
    }

    @Override
    public int getContentItemsTotal() {
        return list.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(final View view) {
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final ItemViewHolder itemHolder = (ItemViewHolder) holder;
        FavoritesModel currFavoritesModel = list.get(position);
        itemHolder.favorites_item_searchimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getAllDate(currFavoritesModel.ticker);
            }
        });
        itemHolder.favorites_item_ticker.setText(currFavoritesModel.ticker);
        itemHolder.favorites_item_name.setText(currFavoritesModel.name);
        itemHolder.favorites_item_MarketValue.setText(String.format("$%.2f", currFavoritesModel.marketPrice));
        if (String.format("%.2f", currFavoritesModel.change).compareTo("0.00") == 0 || String.format("%.2f", currFavoritesModel.change).compareTo("-0.00") == 0) {
            itemHolder.favorites_item_change.setTextColor(Color.BLACK);
            itemHolder.favorites_item_changeImg.setImageResource(0);
        } else if (currFavoritesModel.change > 0) {
            itemHolder.favorites_item_change.setTextColor(Color.GREEN);
            itemHolder.favorites_item_changeImg.setImageResource(R.drawable.ic_baseline_trending_up_24);
        } else {
            itemHolder.favorites_item_change.setTextColor(Color.RED);
            itemHolder.favorites_item_changeImg.setImageResource(R.drawable.ic_baseline_trending_down_24);
        }
        itemHolder.favorites_item_change.setText(String.format("$%.2f(%.2f%%)", currFavoritesModel.change, currFavoritesModel.changeP));
    }

    public String removeItem(int position) {
        String ticker = list.get(position - 1).ticker;
        list.remove(position - 1);
        return ticker;
    }


    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(final View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(final RecyclerView.ViewHolder holder) {
        final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        ImageView favorites_item_searchimg, favorites_item_changeImg;
        TextView favorites_item_ticker, favorites_item_MarketValue, favorites_item_change, favorites_item_name;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            favorites_item_ticker = itemView.findViewById(R.id.favorites_item_ticker);
            favorites_item_name = itemView.findViewById(R.id.favorites_item_name);
            favorites_item_MarketValue = itemView.findViewById(R.id.favorites_item_MarketValue);
            favorites_item_change = itemView.findViewById(R.id.favorites_item_change);
            favorites_item_searchimg = itemView.findViewById(R.id.favorites_item_searchimg);
            favorites_item_changeImg = itemView.findViewById(R.id.favorites_item_changeImg);
            this.itemView = itemView;
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
