package com.csci571.stocks;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.highsoft.highcharts.common.hichartsclasses.*;
import com.highsoft.highcharts.core.*;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StockActivity extends AppCompatActivity {
    RequestQueue mQueue;
    static String URL = "https://csci571hw9-349315.wl.r.appspot.com/search/";
//    String URL = "http://10.26.158.188:8080/search/";
    JSONObject allData = null;
    SharedPreferences pref; // 0 - for private mode
    SharedPreferences.Editor editor;
    String[] favContentList;
    boolean inFav = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Stocks);
        super.onCreate(savedInstanceState);
        pref = getSharedPreferences("localPreference", 0);
        editor = pref.edit();
        mQueue = Volley.newRequestQueue(this);
        setContentView(R.layout.activity_stock);
        // my_child_toolbar is defined in the layout file
        Toolbar stockToolbar = findViewById(R.id.Stock_topAppBar);
        setSupportActionBar(stockToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        // get intent data
        Intent intent = getIntent();
        JSONObject companyJSON = null;
        try {
            allData = new JSONObject(intent.getStringExtra("allData"));
            companyJSON = allData.getJSONObject("company");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            stockToolbar.setTitle(companyJSON.getString("ticker"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setHeader();
        setChart();
        setPortfolio();
        setStats();
        setAbout();
        setInsights();
        setCharts();
        setNews();
    }


    private void setNews() {
        ArrayList<newsModel> newsModelList = setNewsModelList();
        // set the top news
        CardView news_topcard = findViewById(R.id.news_topcard);
        ImageView news_topImg = findViewById(R.id.news_topImg);
        TextView news_topSource = findViewById(R.id.news_topSource);
        TextView news_topTime = findViewById(R.id.news_topTime);
        TextView news_topTitle = findViewById(R.id.news_topTitle);
        news_topSource.setText(newsModelList.get(0).newsSource);
        news_topTime.setText(newsModelList.get(0).newsTime);
        news_topTitle.setText(newsModelList.get(0).newsTitle);
        Glide.with(this)
                .load(newsModelList.get(0).newsImgURL)
                .into(news_topImg);
        news_topcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(newsModelList.get(0).newsURL);
                Dialog dialog = new Dialog(StockActivity.this);
                dialog.setContentView(R.layout.newsdialog);
                TextView newsDialog_source = dialog.findViewById(R.id.newsDialog_source);
                newsDialog_source.setText(newsModelList.get(0).newsSource);
                TextView newsDialog_time = dialog.findViewById(R.id.newsDialog_time);
                newsDialog_time.setText(newsModelList.get(0).newsDate);
                TextView newsDialog_tital = dialog.findViewById(R.id.newsDialog_tital);
                newsDialog_tital.setText(newsModelList.get(0).newsTitle);
                TextView newsDialog_content = dialog.findViewById(R.id.newsDialog_content);
                newsDialog_content.setText(newsModelList.get(0).newsSummary);
                // open company site
                ImageButton imageButton_chrome = dialog.findViewById(R.id.imageButton_chrome);
                imageButton_chrome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        StockActivity.this.startActivity(intent);
                    }
                });
                // open company site
                ImageButton imageButton_twitter = dialog.findViewById(R.id.imageButton_twitter);
                imageButton_twitter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newUrl = "https://twitter.com/intent/tweet?text=" + newsModelList.get(0).newsTitle + " " + uri;
                        Uri uri = Uri.parse(newUrl);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        StockActivity.this.startActivity(intent);
                    }
                });
                // open company site
                ImageButton imageButton_facebook = dialog.findViewById(R.id.imageButton_facebook);
                imageButton_facebook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newUrl = "https://www.facebook.com/sharer/sharer.php?u=" + uri + "&amp;src=sdkpreparse";
                        Uri uri = Uri.parse(newUrl);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        StockActivity.this.startActivity(intent);
                    }
                });
                dialog.show();
            }
        });
        // set rest of the news
        RecyclerView recyclerView = findViewById(R.id.newsRecyclerView);
        NewsAdapter newsAdapter = new NewsAdapter(this, newsModelList.subList(1, newsModelList.size()));
        recyclerView.setAdapter(newsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private ArrayList<newsModel> setNewsModelList() {
        ArrayList<newsModel> newsModelList = new ArrayList<>();
        JSONArray newsArr = null;
        try {
            newsArr = allData.getJSONArray("news");
            String[] checkArr = new String[]{"image", "url", "headline"};
            int count = 0, index = 0;
            boolean nextNews = false;
            while (index < newsArr.length() && count < 20) {
                nextNews = false;
                JSONObject currNews = (JSONObject) newsArr.get(index);
                for (String check : checkArr) {
                    if (!currNews.has(check) || currNews.get(check).toString().length() == 0) {
                        nextNews = true;
                        break;
                    }
                }
                if (!nextNews) {
                    newsModelList.add(new newsModel(currNews.getString("headline"), currNews.getString("source"), currNews.getLong("datetime"), currNews.getString("image"), currNews.getString("summary"), currNews.getString("url")));
                    count++;
                }
                index++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newsModelList;
    }

    private void setCharts() {
        JSONArray recommendationArr = null;
        ArrayList<Integer> buyList = new ArrayList<>();
        ArrayList<Integer> holdArr = new ArrayList<>();
        ArrayList<Integer> sellArr = new ArrayList<>();
        ArrayList<Integer> strongBuyArr = new ArrayList<>();
        ArrayList<Integer> strongSellArr = new ArrayList<>();
        ArrayList<Integer> totalList = new ArrayList<>();
        ArrayList<String> xArrR = new ArrayList<>();
        List<ArrayList<Integer>> dataList = new ArrayList<>();
        try {
            recommendationArr = allData.getJSONArray("recommendation");
            for (int i = 0; i < recommendationArr.length(); i++) {
                buyList.add(((JSONObject) recommendationArr.get(i)).getInt("buy"));
                holdArr.add(((JSONObject) recommendationArr.get(i)).getInt("hold"));
                sellArr.add(((JSONObject) recommendationArr.get(i)).getInt("sell"));
                strongBuyArr.add(((JSONObject) recommendationArr.get(i)).getInt("strongBuy"));
                strongSellArr.add(((JSONObject) recommendationArr.get(i)).getInt("strongSell"));
                totalList.add(buyList.get(i) + holdArr.get(i) + sellArr.get(i) + strongBuyArr.get(i) + strongSellArr.get(i));
                xArrR.add(((JSONObject) recommendationArr.get(i)).getString("period").substring(0, 7));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HIChartView chartView = (HIChartView) findViewById(R.id.tc);
        HIOptions options = new HIOptions();
        HIChart chart = new HIChart();
        chart.setType("column");
        options.setChart(chart);
        HITitle title = new HITitle();
        title.setText("Recommendation Trends");
        options.setTitle(title);
        HIXAxis xAxis = new HIXAxis();
        xAxis.setCategories(xArrR);
        options.setXAxis(new ArrayList<HIXAxis>(Collections.singletonList(xAxis)));
        HIPlotOptions plotOptions = new HIPlotOptions();
        HIColumn hiColumn = new HIColumn();
        hiColumn.setStacking("normal");
        hiColumn.setDataLabels(totalList);
        plotOptions.setColumn(hiColumn);
        options.setPlotOptions(plotOptions);
        title = new HITitle();
        title.setText("#Analysis");
        HIYAxis yAxis = new HIYAxis();
        yAxis.setTitle(title);
        options.setYAxis(new ArrayList<HIYAxis>(Collections.singletonList(yAxis)));
        HIExporting exporting = new HIExporting();
        exporting.setEnabled(false);
        options.setExporting(exporting);
        ArrayList<HISeries> seriesList = new ArrayList<>();
        String[] nameArr = new String[]{"Buy", "Hold", "Sell", "Strong Buy", "Strong Sell"};
        dataList.add(buyList);
        dataList.add(holdArr);
        dataList.add(sellArr);
        dataList.add(strongBuyArr);
        dataList.add(strongSellArr);
        for (int i = 0; i < 5; i++) {
            HIColumn series = new HIColumn();
            series.setType("column");
            series.setName(nameArr[i]);
            series.setData(dataList.get(i));
            seriesList.add(series);
        }
        options.setSeries(seriesList);
        chartView.setOptions(options);
//      end trends chart

//        EPS Chart
        ArrayList<Double> actualArr = new ArrayList<>();
        ArrayList<Double> estimateArr = new ArrayList<>();
        ArrayList<String> xArr = new ArrayList<>();
        JSONArray earningsJSONArray = null;
        try {
            earningsJSONArray = allData.getJSONArray("earnings");
            for (int i = 0; i < earningsJSONArray.length(); i++) {
                if ((((JSONObject) earningsJSONArray.get(i)).isNull("actual"))) {
                    actualArr.add(0.0);
                } else {
                    actualArr.add(((JSONObject) earningsJSONArray.get(i)).getDouble("actual"));
                }
                String date = ((JSONObject) earningsJSONArray.get(i)).getString("period");
                date = date.substring(0, date.indexOf("T"));
                if ((((JSONObject) earningsJSONArray.get(i)).isNull("estimate"))) {
                    estimateArr.add(0.0);
                    xArr.add(date + "\nSurprise: 0");
                } else {
                    estimateArr.add(((JSONObject) earningsJSONArray.get(i)).getDouble("estimate"));
                    xArr.add(date + "\nSurprise: " + String.valueOf(((JSONObject) earningsJSONArray.get(i)).getDouble("surprise")));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HIChartView EPSchartView = (HIChartView) findViewById(R.id.EPS);
        options = new HIOptions();
        title = new HITitle();
        title.setText("Historical EPS Surprises");
        options.setTitle(title);
        xAxis = new HIXAxis();
        xAxis.setCategories(xArr);
        options.setXAxis(new ArrayList<HIXAxis>(Collections.singletonList(xAxis)));
        title = new HITitle();
        title.setText("Quarterly EPS");
        yAxis = new HIYAxis();
        yAxis.setTitle(title);
        options.setYAxis(new ArrayList<HIYAxis>(Collections.singletonList(yAxis)));
        HITooltip tooltip = new HITooltip();
        tooltip.setShared(true);
        options.setTooltip(tooltip);
        exporting = new HIExporting();
        exporting.setEnabled(false);
        options.setExporting(exporting);
        seriesList = new ArrayList<>();
        HIColumn series = new HIColumn();
        series.setType("line");
        series.setName("Actual");
        series.setData(actualArr);
        seriesList.add(series);
        series = new HIColumn();
        series.setType("line");
        series.setName("Estimate");
        series.setData(estimateArr);
        seriesList.add(series);
        options.setSeries(seriesList);
        EPSchartView.setOptions(options);
    }

    private void setInsights() {
        JSONObject companyJSON = null;
        JSONObject sentimentJSON = null;
        TextView insight_textView_name = findViewById(R.id.insight_textView_name);
        TextView insight_textView_TR = findViewById(R.id.insight_textView_TR);
        TextView insight_textView_TT = findViewById(R.id.insight_textView_TT);
        TextView insight_textView_PR = findViewById(R.id.insight_textView_PR);
        TextView insight_textView_PT = findViewById(R.id.insight_textView_PT);
        TextView insight_textView_NR = findViewById(R.id.insight_textView_NR);
        TextView insight_textView_NT = findViewById(R.id.insight_textView_NT);
        try {
            companyJSON = allData.getJSONObject("company");
            sentimentJSON = allData.getJSONObject("sentiment");
            JSONArray redditArr = sentimentJSON.getJSONArray("reddit");
            JSONArray twitterArr = sentimentJSON.getJSONArray("twitter");
            int redditTotalMentions = 0, redditPositive = 0, redditNegative = 0, twitterTotalMentions = 0, twitterPositive = 0, twitterNegative = 0;
            for (int i = 0; i < redditArr.length(); i++) {
                redditTotalMentions += ((JSONObject) redditArr.get(i)).getInt("mention");
                redditPositive += ((JSONObject) redditArr.get(i)).getInt("positiveMention");
                redditNegative += ((JSONObject) redditArr.get(i)).getInt("negativeMention");
            }
            for (int i = 0; i < twitterArr.length(); i++) {
                twitterTotalMentions += ((JSONObject) twitterArr.get(i)).getInt("mention");
                twitterPositive += ((JSONObject) twitterArr.get(i)).getInt("positiveMention");
                twitterNegative += ((JSONObject) twitterArr.get(i)).getInt("negativeMention");
            }
            insight_textView_name.setText(companyJSON.getString("name"));
            insight_textView_TR.setText(String.valueOf(redditTotalMentions));
            insight_textView_TT.setText(String.valueOf(twitterTotalMentions));
            insight_textView_PR.setText(String.valueOf(redditPositive));
            insight_textView_PT.setText(String.valueOf(twitterPositive));
            insight_textView_NR.setText(String.valueOf(redditNegative));
            insight_textView_NT.setText(String.valueOf(twitterNegative));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setAbout() {
        JSONObject companyJSON = null;
        TextView Start_DateTextView = findViewById(R.id.Start_Date);
        TextView IndustryTextView = findViewById(R.id.Industry);
        TextView WebpageTextView = findViewById(R.id.Webpage);
        try {
            companyJSON = allData.getJSONObject("company");
            String Start_Date = companyJSON.getString("ipo");
            Start_Date = Start_Date.substring(0, Start_Date.indexOf('T'));

            Start_DateTextView.setText(Start_Date);
            IndustryTextView.setText(companyJSON.getString("finnhubIndustry"));
            String HTMLStr = "<a href=" + companyJSON.getString("weburl") + ">" + companyJSON.getString("weburl") + "</a>";
            WebpageTextView.setText(Html.fromHtml(HTMLStr, Html.FROM_HTML_MODE_LEGACY));
//            set up the link to company website
            WebpageTextView.setMovementMethod(LinkMovementMethod.getInstance());

//            add peers dynamically
            LinearLayout peersViewParent = findViewById(R.id.LinearLayout);
            LinearLayout.LayoutParams parameter = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            JSONArray peerArr = allData.getJSONArray("peers");
            for (int i = 0; i < peerArr.length(); i++) {
                TextView newTextView = new TextView(this);
                newTextView.setLayoutParams(parameter);
                if (i < peerArr.length() - 1) {
                    newTextView.setText(peerArr.get(i).toString() + ", ");
                } else {
                    newTextView.setText(peerArr.get(i).toString());
                }

                newTextView.setTextColor(Color.BLUE);
//                add click action
                newTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String clickedTicker = ((TextView) view).getText().toString();
                        int index = clickedTicker.indexOf(",");
                        if (index != -1) {
                            clickedTicker = clickedTicker.substring(0, index);
                        }
//                        check if only contains characters
                        if (clickedTicker.matches("[a-zA-Z]+")) {
                            getAllDate(clickedTicker);
                        }
                    }
                });
                peersViewParent.addView(newTextView);
                peersViewParent.invalidate();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setStats() {
        JSONObject priceJSON = null;
        TextView Open_PriceTextView = findViewById(R.id.Open_Price);
        TextView Low_PriceTextView = findViewById(R.id.Low_Price);
        TextView High_PriceTextView = findViewById(R.id.High_Price);
        TextView Prev_CloseTextView = findViewById(R.id.Prev_Close);
        try {
            priceJSON = allData.getJSONObject("price");
            double Open_Price = priceJSON.getDouble("o");
            double Low_Price = priceJSON.getDouble("l");
            double High_Price = priceJSON.getDouble("h");
            double Prev_Close = priceJSON.getDouble("pc");
            Open_PriceTextView.setText("$" + String.format(Locale.US, "%.2f", Open_Price));
            Low_PriceTextView.setText("$" + String.format(Locale.US, "%.2f", Low_Price));
            High_PriceTextView.setText("$" + String.format(Locale.US, "%.2f", High_Price));
            Prev_CloseTextView.setText("$" + String.format(Locale.US, "%.2f", Prev_Close));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setPortfolio() {
        // fill in portfolio part
        JSONObject companyJSON = null;
        JSONObject priceJSON = null;
        TextView portfolio_share = findViewById(R.id.portfolio_share);
        TextView portfolio_acs = findViewById(R.id.portfolio_acs);
        TextView portfolio_cost = findViewById(R.id.portfolio_cost);
        TextView portfolio_change = findViewById(R.id.portfolio_change);
        TextView portfolio_value = findViewById(R.id.portfolio_value);
        try {
            companyJSON = allData.getJSONObject("company");
            priceJSON = allData.getJSONObject("price");
            String ticker = companyJSON.getString("ticker");// empty portfolio
            String portfolioContentStr = pref.getString("portfolio", null);
            if (portfolioContentStr.length() == 0) {
                portfolio_share.setText("0");
                portfolio_acs.setText("$0.00");
                portfolio_cost.setText("$0.00");
                portfolio_change.setText("$0.00");
                portfolio_value.setText("$0.00");
            } else {
                JSONObject portfolio = new JSONObject();
                portfolio = new JSONObject(pref.getString("portfolio", null));
                // this company is not in the portfolio
                if (!portfolio.has(ticker)) {
                    portfolio_share.setText("0");
                    portfolio_acs.setText("$0.00");
                    portfolio_cost.setText("$0.00");
                    portfolio_change.setText("$0.00");
                    portfolio_value.setText("$0.00");
                }
                // the company is in the portfolio
                else {
                    JSONObject currPortfolio = new JSONObject();
                    currPortfolio = portfolio.getJSONObject(ticker);
                    portfolio_share.setText(String.valueOf(currPortfolio.getInt("shares")));
                    double ACS = currPortfolio.getDouble("cost") / currPortfolio.getInt("shares");
                    portfolio_acs.setText(String.format("$%.2f", ACS));
                    portfolio_cost.setText(String.format("$%.2f", currPortfolio.getDouble("cost")));
                    double price = priceJSON.getDouble("c");
                    double change = price - ACS;
                    if ((int) change == 0) {
                        change = 0;
                    }
                    portfolio_change.setText(String.format("$%.2f", change));
                    portfolio_value.setText(String.format("$%.2f", price * currPortfolio.getInt("shares")));
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setChart() {
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_chart_line);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_baseline_access_time_filled_24);
        ViewPager2 viewPager2 = findViewById(R.id.chartViewPager);
        VPAdapter vpAdapter = new VPAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager2.setAdapter(vpAdapter);
        hourlyPriceChart hChart = new hourlyPriceChart();
        hChart.allData = allData;
        historicalChart histChart = new historicalChart();
        histChart.allData = allData;
        vpAdapter.addFragmentList(hChart, "hourlyPrice");
        vpAdapter.addFragmentList(histChart, "historical");
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {//Configure tabs..
            if (position == 0) {
                tab.setIcon(R.drawable.ic_chart_line);
            } else {
                tab.setIcon(R.drawable.ic_baseline_access_time_filled_24);
            }
        }).attach();
    }

    private void setHeader() {
        // fill in header part
        JSONObject companyJSON = null;
        JSONObject priceJSON = null;
        TextView tickerTextView = findViewById(R.id.ticker);
        TextView nameTextView = findViewById(R.id.name);
        TextView priceTextView = findViewById(R.id.price);
        TextView changeTextView = findViewById(R.id.change);
        ImageView companyImg = findViewById(R.id.companyImg);
        try {
            companyJSON = allData.getJSONObject("company");
            priceJSON = allData.getJSONObject("price");
            Picasso.get().load(companyJSON.getString("logo")).into(companyImg);

            tickerTextView.setText(companyJSON.getString("ticker"));
            nameTextView.setText(companyJSON.getString("name"));

            double price = priceJSON.getDouble("c");
            priceTextView.setText("$" + String.format(Locale.US, "%.2f", price));

            double change = priceJSON.getDouble("d");
            double changeP = priceJSON.getDouble("dp");
            if (String.format("%.2f", change).compareTo("0.00") == 0) {
                changeTextView.setTextColor(Color.BLACK);
            } else if (change < 0) {
                changeTextView.setTextColor(Color.RED);
            } else if (change > 0) {
                changeTextView.setTextColor(Color.GREEN);
            }

            String changeStr = "$" + String.format(Locale.US, "%.2f", change) + "(" + String.format(Locale.US, "%.2f%%", changeP) + ")";
            changeTextView.setText(changeStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //show app bar menu item
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stock_menu, menu);
        MenuItem favItem = menu.findItem(R.id.action_favorite);
        JSONObject companyJSON = null;
        // set fav icon for current company by check the set
        try {
            companyJSON = allData.getJSONObject("company");
            JSONObject favoritesJSON = new JSONObject();
            String favoritesContentStr = pref.getString("favorite", null);
            JSONObject currFavorite = new JSONObject();
            // if portfolio is empty, create a new JSONObject for the new company
            if (favoritesContentStr.length() == 0) {
                favItem.setIcon(R.drawable.ic_baseline_star_border_24);
            } else {
                favoritesJSON = new JSONObject(pref.getString("favorite", null));
                // check if current company is in the portfolio
                if (favoritesJSON.has(companyJSON.getString("ticker"))) {
                    inFav = true;
                    favItem.setIcon(R.drawable.ic_baseline_star_24);
                } else {
                    favItem.setIcon(R.drawable.ic_baseline_star_border_24);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }

    // when click on the up key, back to prev activity not parent activity
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_favorite:
                JSONObject companyJSON = null;
                try {
                    companyJSON = allData.getJSONObject("company");
                    String favContent = pref.getString("favorite", null);
                    String totalCompanyJSONStr = pref.getString("totalCompany", null);
                    JSONObject favoritesJSON = new JSONObject();
                    JSONObject totalCompany = new JSONObject();
                    JSONObject currFavorite = new JSONObject();
                    // in fav, remove it
                    if (inFav) {
                        favoritesJSON = new JSONObject(pref.getString("favorite", null));
                        favoritesJSON.remove(companyJSON.getString("ticker"));
                        totalCompany = new JSONObject(pref.getString("totalCompany", null));
                        if(totalCompany.getInt(companyJSON.getString("ticker")) == 2){
                            totalCompany.put(companyJSON.getString("ticker") ,1);
                        }else{
                            totalCompany.remove(companyJSON.getString("ticker"));
                        }
                        inFav = false;
                        item.setIcon(R.drawable.ic_baseline_star_border_24);
                    } else {
                        if (favContent.length() == 0) {
                            // add current company
                            currFavorite.put("ticker", companyJSON.getString("ticker"));
                            currFavorite.put("company", companyJSON.getString("name"));
                        } else {
                            favoritesJSON = new JSONObject(pref.getString("favorite", null));
                            // add current company
                            currFavorite.put("ticker", companyJSON.getString("ticker"));
                            currFavorite.put("company", companyJSON.getString("name"));
                        }
                        favoritesJSON.put(companyJSON.getString("ticker"), currFavorite);
                        inFav = true;
                        item.setIcon(R.drawable.ic_baseline_star_24);
                        if (totalCompanyJSONStr.length() == 0 || totalCompanyJSONStr.compareTo("{}") == 0) {
                            totalCompany.put(companyJSON.getString("ticker"), 1);
                        } else {
                            totalCompany = new JSONObject(pref.getString("totalCompany", null));
                            if (!totalCompany.has(companyJSON.getString("ticker"))) {
                                totalCompany.put(companyJSON.getString("ticker"), 1);
                            } else {
                                if (totalCompany.getInt(companyJSON.getString("ticker")) == 1) {
                                    totalCompany.put(companyJSON.getString("ticker"), 2);
                                }
                            }
                        }
                    }
                    editor.putString("totalCompany", totalCompany.toString());
                    editor.putString("favorite", favoritesJSON.toString());
                    editor.apply();
                    Log.d("local", pref.getAll().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // get data for pears company
    private void getAllDate(String ticker) {
        String url = URL + "company/" + ticker;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("company").compareTo("{}") == 0) {
                                Toast.makeText(StockActivity.this, "Please enter a valid ticker", Toast.LENGTH_LONG).show();
                            } else {
                                Intent intent = new Intent(StockActivity.this, StockActivity.class);
                                intent.putExtra("allData", response.toString());
                                startActivity(intent);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mQueue.add(request);
    }

    // trade button
    public void onClickTrade(View view) {
        JSONObject priceJSON = null;
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.trade_dialog);
        Dialog secondTradeDialog = new Dialog(StockActivity.this);
        secondTradeDialog.setContentView(R.layout.second_trade_dialog);
        TextView second_trade_dialog_text = secondTradeDialog.findViewById(R.id.second_trade_dialog_text);
        Button DONEButton = secondTradeDialog.findViewById(R.id.second_trade_dialog_button);
        DONEButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                secondTradeDialog.dismiss();
            }
        });
        TextView trade_title = dialog.findViewById(R.id.trade_title);
        TextView trade_cal = dialog.findViewById(R.id.trade_cal);
        TextView trade_total = dialog.findViewById(R.id.trade_total);
        EditText trade_share = dialog.findViewById((R.id.trade_share));
        try {
            final JSONObject companyJSON = allData.getJSONObject("company");
            priceJSON = allData.getJSONObject("price");
            double price = priceJSON.getDouble("c");
            trade_title.setText("Trade" + companyJSON.getString("name") + "shares");
            trade_cal.setText(" 0 *" + String.valueOf(price) + "/share = 0");
            double totalInMemory = pref.getFloat("total", 0);
            trade_total.setText("$" + String.format("%.2f", totalInMemory) + " to buy " + companyJSON.getString("ticker"));
            trade_share.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.toString().length() == 0) {
                        trade_cal.setText(" 0 *" + String.valueOf(price) + "/share = 0");
                    } else if (!charSequence.toString().contains(".")) {
                        int currShareInt = Integer.parseInt(charSequence.toString());
                        double total = currShareInt * price;
                        trade_cal.setText(charSequence.toString() + "*" + String.valueOf(price) + "/share = " + String.format("%.2f", total));
                    }
                }

            });
            // click buy
            Button trade_buy = dialog.findViewById(R.id.trade_buy);
            trade_buy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String currShare = trade_share.getText().toString();
                    // no share enter or when share is not int
                    if (currShare.length() == 0) {
                        Toast.makeText(StockActivity.this, "Please enter a valid amount", Toast.LENGTH_LONG).show();
                    } else if (Integer.parseInt(currShare) == 0) {
                        Toast.makeText(StockActivity.this, "Cannot buy non-positive shares", Toast.LENGTH_LONG).show();
                    } else {
                        int currShareInt = Integer.parseInt(currShare);
                        double total = currShareInt * price;
                        // check totalInM < total
                        if (totalInMemory < total) {
                            Toast.makeText(StockActivity.this, "Not enough money to buy", Toast.LENGTH_LONG).show();
                        } else {
                            // modify local memory
                            JSONObject portfolio = new JSONObject();
                            try {
                                String portfolioContentStr = pref.getString("portfolio", null);
                                JSONObject currPortfolio = new JSONObject();
                                // if portfolio is empty, create a new JSONObject for the new company
                                if (portfolioContentStr.length() == 0) {
                                    currPortfolio.put("ticker", companyJSON.getString("ticker"));
                                    currPortfolio.put("shares", currShareInt);
                                    currPortfolio.put("cost", total);
                                } else {
                                    portfolio = new JSONObject(pref.getString("portfolio", null));
                                    // check if current company is in the portfolio
                                    if (portfolio.has(companyJSON.getString("ticker"))) {
                                        currPortfolio = portfolio.getJSONObject(companyJSON.getString("ticker"));
                                        currPortfolio.put("shares", currShareInt + currPortfolio.getInt("shares"));
                                        currPortfolio.put("cost", total + currPortfolio.getDouble("cost"));
                                    } else {
                                        currPortfolio.put("ticker", companyJSON.getString("ticker"));
                                        currPortfolio.put("shares", currShareInt);
                                        currPortfolio.put("cost", total);
                                    }
                                }
                                portfolio.put(companyJSON.getString("ticker"), currPortfolio);

                                String totalCompanyJSONStr = pref.getString("totalCompany", null);
                                JSONObject totalCompany = new JSONObject();
                                if (totalCompanyJSONStr.length() == 0 || totalCompanyJSONStr.compareTo("{}") == 0) {
                                    totalCompany.put(companyJSON.getString("ticker"), 1);
                                } else {
                                    totalCompany = new JSONObject(pref.getString("totalCompany", null));
                                    if (!totalCompany.has(companyJSON.getString("ticker"))) {
                                        totalCompany.put(companyJSON.getString("ticker"), "");
                                    } else {
                                        if (totalCompany.getInt(companyJSON.getString("ticker")) == 1) {
                                            totalCompany.put(companyJSON.getString("ticker"), 2);
                                        }
                                    }
                                }
                                editor.putFloat("total", (float) (totalInMemory - total));
                                editor.putString("portfolio", portfolio.toString());
                                editor.putString("totalCompany", totalCompany.toString());
                                editor.apply();
                                Log.d("Local", pref.getAll().toString());
                                setPortfolio();
                                second_trade_dialog_text.setText(String.format("You have successfully bought %d shares of %s", currShareInt, companyJSON.getString("ticker")));
                                dialog.dismiss();
                                secondTradeDialog.show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            });
            // click sell
            Button trade_sell = dialog.findViewById(R.id.trade_sell);
            trade_sell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String currShare = trade_share.getText().toString();
                    // no share enter or when share is not int
                    // User enters invalid input like text or punctuations
                    if (currShare.length() == 0) {
                        Toast.makeText(StockActivity.this, "Please enter a valid amount", Toast.LENGTH_LONG).show();
                    }
                    // User tries to sell zero
                    else if (Integer.parseInt(currShare) == 0) {
                        Toast.makeText(StockActivity.this, "Cannot sell non-positive shares", Toast.LENGTH_LONG).show();
                    } else {
                        int currShareInt = Integer.parseInt(currShare);
                        // User tries to sell more shares than they own
                        // empty portfolio
                        String portfolioContentStr = pref.getString("portfolio", null);
                        if (portfolioContentStr.length() == 0) {
                            Toast.makeText(StockActivity.this, "Not enough shares to sell", Toast.LENGTH_LONG).show();
                        } else {
                            JSONObject portfolio = new JSONObject();
                            try {
                                portfolio = new JSONObject(pref.getString("portfolio", null));
                                // this company is not in the portfolio
                                if (!portfolio.has(companyJSON.getString("ticker"))) {
                                    Toast.makeText(StockActivity.this, "Not enough shares to sell", Toast.LENGTH_LONG).show();
                                } else {
                                    JSONObject currPortfolio = new JSONObject();
                                    currPortfolio = portfolio.getJSONObject(companyJSON.getString("ticker"));
                                    // not enough share to sell for this company
                                    if (currPortfolio.getInt("shares") < currShareInt) {
                                        Toast.makeText(StockActivity.this, "Not enough shares to sell", Toast.LENGTH_LONG).show();
                                    } else {
                                        if (currPortfolio.getInt("shares") == currShareInt) {

                                            portfolio.remove(companyJSON.getString("ticker"));
                                            JSONObject totalCompany = new JSONObject(pref.getString("totalCompany", null));
                                            if(totalCompany.getInt(companyJSON.getString("ticker")) == 2){
                                                totalCompany.put(companyJSON.getString("ticker") ,1);
                                            }else{
                                                totalCompany.remove(companyJSON.getString("ticker"));
                                            }
                                            editor.putString("totalCompany", totalCompany.toString());
                                        } else {
                                            double ACS = currPortfolio.getDouble("cost") / currPortfolio.getInt("shares");
                                            double cost = currPortfolio.getDouble("cost") - (ACS * currShareInt);
                                            currPortfolio.put("shares", currPortfolio.getInt("shares") - currShareInt);
                                            currPortfolio.put("cost", cost);
                                            portfolio.put(companyJSON.getString("ticker"), currPortfolio);
                                        }
                                        editor.putString("portfolio", portfolio.toString());
                                        editor.putFloat("total", (float) (totalInMemory + (currShareInt * price)));
                                        editor.apply();
                                        Log.d("local", pref.getAll().toString());
                                        setPortfolio();
                                        second_trade_dialog_text.setText(String.format("You have successfully sold %d shares of %s", currShareInt, companyJSON.getString("ticker")));
                                        dialog.dismiss();
                                        secondTradeDialog.show();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            dialog.show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
