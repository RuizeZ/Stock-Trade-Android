package com.csci571.stocks;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

import java.util.*;

public class MainPageSection extends Section {
    private final List<PortfolioModel> list;
    private final double totalWealth;
    private final double totalMoneyLeft;


    MainPageSection(List<PortfolioModel> list, double totalMarketWealth, double totalMoneyLeft) {

        super(SectionParameters.builder()
                .itemResourceId(R.layout.portfolio_item)
                .headerResourceId(R.layout.portfolio_header)
                .build());
        this.list = list;
        this.totalMoneyLeft = totalMoneyLeft;
        this.totalWealth = totalMarketWealth + totalMoneyLeft;
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
        PortfolioModel currPortfolioModel = list.get(position);
        itemHolder.portfolio_item_searchimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.getAllDate(currPortfolioModel.ticker);

            }
        });
        itemHolder.portfolio_item_ticker.setText(currPortfolioModel.ticker);
        itemHolder.portfolio_item_MarketValue.setText(String.format("$%.2f", currPortfolioModel.marketPrice));
        itemHolder.portfolio_item_share.setText(currPortfolioModel.share + " shares");
        if (String.format("%.2f", currPortfolioModel.change).compareTo("0.00") == 0 || String.format("%.2f", currPortfolioModel.change).compareTo("-0.00") == 0) {
            itemHolder.portfolio_item_change.setTextColor(Color.BLACK);
            itemHolder.portfolio_item_changeImg.setImageResource(0);
        } else if (currPortfolioModel.change > 0) {
            itemHolder.portfolio_item_change.setTextColor(Color.GREEN);
            itemHolder.portfolio_item_changeImg.setImageResource(R.drawable.ic_baseline_trending_up_24);
        } else {
            itemHolder.portfolio_item_change.setTextColor(Color.RED);
            itemHolder.portfolio_item_changeImg.setImageResource(R.drawable.ic_baseline_trending_down_24);
        }
        itemHolder.portfolio_item_change.setText(String.format("$%.2f(%.2f%%)", currPortfolioModel.change, currPortfolioModel.changeP));
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(final View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(final RecyclerView.ViewHolder holder) {
        final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
        headerHolder.main_total.setText(String.format("$%.2f", totalWealth));
        headerHolder.main_moneyLeft.setText(String.format("$%.2f", totalMoneyLeft));
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        ImageView portfolio_item_searchimg, portfolio_item_changeImg;
        TextView portfolio_item_ticker, portfolio_item_MarketValue, portfolio_item_share, portfolio_item_change;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            portfolio_item_ticker = itemView.findViewById(R.id.portfolio_item_ticker);
            portfolio_item_MarketValue = itemView.findViewById(R.id.portfolio_item_MarketValue);
            portfolio_item_share = itemView.findViewById(R.id.portfolio_item_share);
            portfolio_item_change = itemView.findViewById(R.id.portfolio_item_change);
            portfolio_item_searchimg = itemView.findViewById(R.id.portfolio_item_searchimg);
            portfolio_item_changeImg = itemView.findViewById(R.id.portfolio_item_changeImg);
            this.itemView = itemView;
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView main_total, main_moneyLeft;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            main_total = itemView.findViewById(R.id.main_total);
            main_moneyLeft = itemView.findViewById(R.id.main_moneyLeft);
        }
    }
}
