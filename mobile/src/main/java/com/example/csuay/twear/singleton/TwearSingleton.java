package com.example.csuay.twear.singleton;

import com.twitter.sdk.android.core.TwitterSession;

/**
 * Created by csuay on 01/04/15.
 */
public enum TwearSingleton {

    INSTANCE;

    private TwitterSession twitterSession;

    public TwitterSession getTwitterSession() {
        return this.twitterSession;
    }

    public void setTwitterSession(TwitterSession twitterSession) {
        this.twitterSession = twitterSession;
    }

}
