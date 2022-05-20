package com.csci571.stocks;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class WebAppInterface {
    String allDate;

    /** Instantiate the interface and set the context */
    WebAppInterface() {
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public String getData() {
        return this.allDate;
    }
}
