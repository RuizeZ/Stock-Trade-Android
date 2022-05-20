package com.csci571.stocks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity {
    private static Context currentContext;

    JSONObject autoJSON;
    SharedPreferences pref;

    int count = 0;
    int ProgressBarCounter = 0;
    static boolean startUpdate = true;
    String textInSearch = "";
    double totalMarketWealth = 0;
    static String URL = "https://csci571hw9-349315.wl.r.appspot.com/search/";
//    static String URL = "http://10.26.158.188:8080/search/";

    ArrayList<FavoritesModel> favoriteModelList;
    ArrayList<PortfolioModel> portfolioModelList;

    static RecyclerView recyclerView;
    static RecyclerView favRecyclerView;
    MainPageSection mainPageSection;
    FavoritesSection favoritesSection;
    SectionedRecyclerViewAdapter sectionAdapter = new SectionedRecyclerViewAdapter();
    SectionedRecyclerViewAdapter favSectionAdapter = new SectionedRecyclerViewAdapter();
    static SearchView searchView;
    static LinearLayout linlaHeaderProgress;
    static Toolbar toolbar;
    static TextView main_date;
    static TextView main_powerby;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentContext = this;
        setTheme(R.style.Theme_Stocks);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        linlaHeaderProgress.setVisibility(View.VISIBLE);

        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        main_date = findViewById(R.id.main_date);


        // set up powerBy
        main_powerby = findViewById(R.id.main_powerpy);
        // set up the link to company website
        String HTMLStr = "<a style=\"color:gray;\" href=https://www.finnhub.io/>Powered by Finnhub</a>";
        main_powerby.setText(Html.fromHtml(HTMLStr, Html.FROM_HTML_MODE_LEGACY));
        main_powerby.setTextColor(Color.GRAY);
        // go to company website
        main_powerby.setMovementMethod(LinkMovementMethod.getInstance());


        recyclerView = (RecyclerView) findViewById(R.id.main_RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        favRecyclerView = (RecyclerView) findViewById(R.id.favorites_recyclerView);
        favRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        ItemTouchHelper portMoveItemTouchHelper = new ItemTouchHelper(portMoveCallback);
        portMoveItemTouchHelper.attachToRecyclerView(recyclerView);
        ItemTouchHelper favMoveItemTouchHelper = new ItemTouchHelper(favMoveCallback);
        favMoveItemTouchHelper.attachToRecyclerView(favRecyclerView);

        // initialize the shared preference
        pref = getApplicationContext().getSharedPreferences("localPreference", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        if (pref.getString("favorite", null) == null) {
            editor.putFloat("total", 25000);
            editor.putString("favorite", "");
            editor.putString("portfolio", "");
            editor.putString("totalCompany", "");
            editor.apply();
        }
//        editor.putFloat("total", 25000);
//        editor.putString("favorite", "");
//        editor.putString("portfolio", "");
//        editor.putString("totalCompany", "");
//        editor.apply();

        enableSwipeToDeleteAndUndo();
        makeAllGone();
        runUpdate();
    }

    @Override
    protected void onStart() {

        ProgressBarCounter = 0;
        super.onStart();

        sectionAdapter.removeSection(mainPageSection);
        favSectionAdapter.removeSection(favoritesSection);

        if (!startUpdate) {
            setLists();
        }

        Instant currTimeInstant = Instant.now();
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        // Unix time to local time
        LocalDateTime ldt = LocalDateTime.ofInstant(currTimeInstant, ZoneId.systemDefault());
        String currentDate = String.valueOf(ldt.getDayOfMonth()) + " " + months[ldt.getMonthValue() - 1] + " " + String.valueOf(ldt.getYear());
        main_date.setText(currentDate);
    }

    @Override
    protected void onStop() {
        super.onStop();
        startUpdate = false;
    }

    protected static void makeAllVisible() {
//        toolbar.setVisibility(View.VISIBLE);
        main_date.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        favRecyclerView.setVisibility(View.VISIBLE);
        main_powerby.setVisibility(View.VISIBLE);
    }

    protected static void makeAllGone() {
//        toolbar.setVisibility(View.GONE);
        main_date.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        favRecyclerView.setVisibility(View.GONE);
        main_powerby.setVisibility(View.GONE);
    }

    private void setLists() {
        favoriteModelList = new ArrayList<>();
        portfolioModelList = new ArrayList<>();
        totalMarketWealth = 0;
        String totalCompanyStr = pref.getString("totalCompany", null);
        String portfolioContentStr = pref.getString("portfolio", null);
        String favoriteContentStr = pref.getString("favorite", null);
        if (totalCompanyStr.length() != 0 && totalCompanyStr.compareTo("{}") != 0) {
            JSONObject totalCompany = null;
            try {
                totalCompany = new JSONObject(pref.getString("totalCompany", null));
                count = 0;
                int keysNum = totalCompany.length();
                Iterator<String> keys = totalCompany.keys();
                while (keys.hasNext()) {
                    String ticker = keys.next();
//                    final JSONObject currPortfolio = portfolio.getJSONObject(ticker);
                    String url = URL + "update/" + ticker;
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        JSONObject updateData = new JSONObject(response.toString());
                                        updateData = new JSONObject(response.toString());
                                        if (portfolioContentStr.length() != 0 && portfolioContentStr.compareTo("{}") != 0) {
                                            JSONObject portfolio = new JSONObject(pref.getString("portfolio", null));
                                            if (portfolio.has(ticker)) {
                                                JSONObject currPortfolio = portfolio.getJSONObject(ticker);
                                                Log.d("in newPortfolioModel", "");
                                                PortfolioModel newPortfolioModel = new PortfolioModel(updateData, currPortfolio);
                                                totalMarketWealth += newPortfolioModel.marketPrice;
                                                portfolioModelList.add(newPortfolioModel);
                                            }
                                        }
                                        if (favoriteContentStr.length() != 0 && favoriteContentStr.compareTo("{}") != 0) {
                                            JSONObject favorite = new JSONObject(pref.getString("favorite", null));
                                            if (favorite.has(ticker)) {
                                                JSONObject currFavorite = favorite.getJSONObject(ticker);
                                                Log.d("in newFavoritesModel", "");
                                                FavoritesModel newFavoritesModel = new FavoritesModel(updateData, currFavorite);
                                                favoriteModelList.add(newFavoritesModel);
                                            }
                                        }
                                        count++;
                                        if (count == keysNum) {
                                            setPortfolio();
                                            setFavorites();
                                            startUpdate = true;
                                            linlaHeaderProgress.setVisibility(View.GONE);
                                            makeAllVisible();
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
                    VolleySingleton.getInstance(this).addToRequestQueue(request);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            setPortfolio();
            setFavorites();
            startUpdate = true;
            linlaHeaderProgress.setVisibility(View.GONE);
            makeAllVisible();
        }
    }

    private void setFavorites() {
        favoritesSection = new FavoritesSection(favoriteModelList);
        favSectionAdapter.addSection(favoritesSection);
        // Set up your RecyclerView with the SectionedRecyclerViewAdapter
        favRecyclerView.setAdapter(favSectionAdapter);
    }

    private void setPortfolio() {
        mainPageSection = new MainPageSection(portfolioModelList, totalMarketWealth, pref.getFloat("total", 0));
        sectionAdapter.addSection(mainPageSection);
        // Set up your RecyclerView with the SectionedRecyclerViewAdapter
        recyclerView.setAdapter(sectionAdapter);

    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search...");
        // Get SearchView autocomplete object.
        SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setThreshold(1);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getAllDate(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                startUpdate = false;
                RequestQueue mQueue = Volley.newRequestQueue(MainActivity.this);
                if (newText.length() != 0 && textInSearch.compareTo(newText) != 0) {
                    String url = URL + "auto/" + newText;
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    autoJSON = response;
                                    startUpdate = true;
                                    String[] dataArr = createAuto();
                                    ArrayAdapter<String> autoAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, dataArr);
                                    searchAutoComplete.setAdapter(autoAdapter);
                                    searchAutoComplete.performClick();
                                    // Listen to search view item on click event.
                                    searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                                            String queryString = (String) adapterView.getItemAtPosition(itemIndex);
                                            queryString = queryString.substring(0, queryString.indexOf(' '));
                                            Toast.makeText(MainActivity.this, "you clicked " + queryString, Toast.LENGTH_LONG).show();
//                                            stop call auto api
                                            textInSearch = queryString;
//                                            change text in searchview after change textInSearch, otherwise onQueryTextChange() will run
                                            searchAutoComplete.setText(queryString);
//                                            make API call get all data
                                            getAllDate(queryString);
                                        }
                                    });
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });
                    request.setRetryPolicy(new DefaultRetryPolicy(50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    VolleySingleton.getInstance(MainActivity.this).addToRequestQueue(request);
                } else {
                    searchAutoComplete.setAdapter(null);
                }
                startUpdate = true;
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private String[] createAuto() {
        List<String> autoList = new ArrayList<>();
        try {
            JSONArray autoJSONArr = autoJSON.getJSONArray("result");
            for (int i = 0; i < autoJSONArr.length(); i++) {
                JSONObject currCompany = autoJSONArr.getJSONObject(i);
//                                charSequence is string
                if (currCompany.getString("type").compareTo("Common Stock") == 0 && !currCompany.getString("symbol").contains(".")) {
                    String ontCompany = currCompany.getString("symbol") + " | " + currCompany.getString("description");
                    autoList.add(ontCompany);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String[] autoStrArr = new String[autoList.size()];
        return autoList.toArray(autoStrArr);
    }

    public static void getAllDate(String ticker) {
        InputMethodManager imm = (InputMethodManager) currentContext.getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        makeAllGone();
        linlaHeaderProgress.setVisibility(View.VISIBLE);
        startUpdate = false;
        RequestQueue mQueue = Volley.newRequestQueue(currentContext);
        String url = URL + "company/" + ticker;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("company").compareTo("{}") == 0) {
                                Toast.makeText(currentContext, "Please enter a valid ticker", Toast.LENGTH_LONG).show();
                            } else {
                                Intent intent = new Intent(currentContext, StockActivity.class);
                                intent.putExtra("allData", response.toString());
                                currentContext.startActivity(intent);
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
        VolleySingleton.getInstance(currentContext).addToRequestQueue(request);
    }

    private void runUpdate() {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (startUpdate) {
                    sectionAdapter.removeSection(mainPageSection);
                    favSectionAdapter.removeSection(favoritesSection);
                    setLists();
                }
                handler.postDelayed(this, 15000);
            }
        });
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                startUpdate = false;
                final int position = viewHolder.getAdapterPosition();
                String ticker = favoritesSection.removeItem(position);
                favSectionAdapter.notifyItemRemoved(position - 1);
                favRecyclerView.setAdapter(favSectionAdapter);
                JSONObject favoritesJSON = new JSONObject();
                JSONObject totalCompany = new JSONObject();
                try {
                    favoritesJSON = new JSONObject(pref.getString("favorite", null));
                    favoritesJSON.remove(ticker);
                    totalCompany = new JSONObject(pref.getString("totalCompany", null));
                    if (totalCompany.getInt(ticker) == 2) {
                        totalCompany.put(ticker, 1);
                    } else {
                        totalCompany.remove(ticker);
                    }
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("favorite", favoritesJSON.toString());
                    editor.putString("totalCompany", totalCompany.toString());
                    editor.apply();
                    Log.d("Local", pref.getAll().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startUpdate = true;
            }
        };
        ItemTouchHelper swipeToDeleteItemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        swipeToDeleteItemTouchhelper.attachToRecyclerView(favRecyclerView);
    }

    ItemTouchHelper.SimpleCallback portMoveCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView tempRecyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            startUpdate = false;
            int fromPosition = viewHolder.getAdapterPosition() - 1;
            int toPosition = target.getAdapterPosition() - 1;
            Collections.swap(portfolioModelList, fromPosition, toPosition);
            sectionAdapter.notifyItemMoved(fromPosition, toPosition);
            // Set up your RecyclerView with the SectionedRecyclerViewAdapter
            recyclerView.setAdapter(sectionAdapter);
            startUpdate = true;
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };

    ItemTouchHelper.SimpleCallback favMoveCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView tempRecyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            startUpdate = false;
            int fromPosition = viewHolder.getAdapterPosition() - 1;
            int toPosition = target.getAdapterPosition() - 1;
            Collections.swap(favoriteModelList, fromPosition, toPosition);
            favSectionAdapter.notifyItemMoved(fromPosition, toPosition);
            // Set up your RecyclerView with the SectionedRecyclerViewAdapter
            favRecyclerView.setAdapter(favSectionAdapter);
            startUpdate = true;
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };
}