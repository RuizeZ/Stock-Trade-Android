package com.csci571.stocks;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class newsModel {
    String newsTitle;
    String newsSource;
    String newsTime;
    String newsDate;
    String newsSummary;
    String newsImgURL;
    String newsURL;

    public newsModel(String newsTitle, String newsSource, long newsTime, String newsImgURL, String newsSummary, String url) {
        this.newsTitle = newsTitle;
        this.newsSource = newsSource;
        // news time UTC
        Instant newsTimeInstant = Instant.ofEpochSecond(newsTime);
        // current time UTC
        Instant currTimeInstant = Instant.now();
        // duration from newsTime to currentTime
        long timeElapsedD = Duration.between(newsTimeInstant, currTimeInstant).toDays();
        long timeElapsedH = Duration.between(newsTimeInstant, currTimeInstant).toHours();
        long timeElapsedM = Duration.between(newsTimeInstant, currTimeInstant).toMinutes();
        long timeElapsedS = Duration.between(newsTimeInstant, currTimeInstant).toMillis();
        if (timeElapsedD >= 1) {
            this.newsTime = String.valueOf(timeElapsedD) + " days ago";
        } else if (timeElapsedH >= 1) {
            this.newsTime = String.valueOf(timeElapsedH) + " hours ago";
        } else if (timeElapsedM >= 1) {
            this.newsTime = String.valueOf(timeElapsedM) + " minutes ago";
        } else {
            this.newsTime = String.valueOf(timeElapsedS) + " seconds ago";
        }
//        System.out.println(timeElapsedH);
//        System.out.println(timeElapsedM);
//        System.out.println(timeElapsedS);
//        System.out.println(this.newsTime);
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        // Unix time to local time
        LocalDateTime ldt = LocalDateTime.ofInstant(newsTimeInstant, ZoneId.systemDefault());
        this.newsDate = months[ldt.getMonthValue() - 1] + " " + String.valueOf(ldt.getDayOfMonth()) + ", " + String.valueOf(ldt.getYear());
        this.newsImgURL = newsImgURL;
        this.newsURL = url;
        this.newsSummary = newsSummary;
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public String getNewsSource() {
        return newsSource;
    }

    public String getNewsTime() {
        return newsTime;
    }

    public String getNewsImgURL() {
        return newsImgURL;
    }
}
