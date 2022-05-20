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

public class historicalChart extends Fragment {
    WebView historicalChartWebview;
    JSONObject allData;
    public historicalChart() {
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
        return inflater.inflate(R.layout.fragment_historical_chart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        historicalChartWebview = (WebView) getView().findViewById(R.id.historicalChartWebview);
        historicalChartWebview.loadUrl("file:///android_asset/historical_chart.html");
        WebSettings webSettings = historicalChartWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        WebAppInterface nwqWebAppInterface = new WebAppInterface();
        nwqWebAppInterface.allDate = this.allData.toString();
        historicalChartWebview.addJavascriptInterface(nwqWebAppInterface, "Android");
        webSettings.setJavaScriptEnabled(true);
    }
}