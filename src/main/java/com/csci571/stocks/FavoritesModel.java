package com.csci571.stocks;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class FavoritesModel {
    String ticker;
    String name;
    double marketPrice;
    double change;
    double changeP;

    public FavoritesModel(JSONObject updateData, JSONObject favoriteData) {
        try {
            this.ticker = favoriteData.getString("ticker");;
            this.name = favoriteData.getString("company");
            this.marketPrice = updateData.getJSONObject("price").getDouble("c");
            this.change = updateData.getJSONObject("price").getDouble("d");
            this.changeP = updateData.getJSONObject("price").getDouble("dp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
