package com.csci571.stocks;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.json.JSONObject;

public class hourlyPriceChart extends Fragment {
    WebView hourlyPriceWebview;
    JSONObject allData;

    public hourlyPriceChart() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hourly_price_chart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hourlyPriceWebview = (WebView) getView().findViewById(R.id.hourlyPriceWebview);
        hourlyPriceWebview.loadUrl("file:///android_asset/hourly_price_chart.html");
        WebSettings webSettings = hourlyPriceWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        WebAppInterface nwqWebAppInterface = new WebAppInterface();
        nwqWebAppInterface.allDate = this.allData.toString();
        hourlyPriceWebview.addJavascriptInterface(nwqWebAppInterface, "Android");
        webSettings.setJavaScriptEnabled(true);
    }
}