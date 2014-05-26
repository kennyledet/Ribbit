package com.kennyken.ribbit.app;

import android.app.Application;

import com.parse.Parse;


// Application class is first thing loaded
// Good place to do initial setup required by entire app
public class RibbitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "L4rNUjDeODRdFXA3n3pmCBv0HoPwn2xi7ZNbWb3h",
                "w2VHWMnwG6QbtgXtsloAsmcdsJqKR5LEBbUI6dlS");

    }
}
