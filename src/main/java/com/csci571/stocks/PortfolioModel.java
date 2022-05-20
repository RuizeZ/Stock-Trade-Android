package com.csci571.stocks;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class PortfolioModel {
    String ticker;
    String share;
    double marketPrice;
    double change;
    double changeP;

    public PortfolioModel(JSONObject updateData, JSONObject portfolioData) {
        try {
            this.ticker = portfolioData.getString("ticker");
            this.share = String.valueOf(portfolioData.getInt("shares"));
            this.marketPrice = updateData.getJSONObject("price").getDouble("c") * portfolioData.getInt("shares");
            this.change = updateData.getJSONObject("price").getDouble("c") * portfolioData.getInt("shares") - portfolioData.getDouble("cost");
            if (this.change > -0.01 && this.change < 0.01) {
                this.change = 0;
            }
            this.changeP = (change / portfolioData.getDouble("cost")) * 100;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
