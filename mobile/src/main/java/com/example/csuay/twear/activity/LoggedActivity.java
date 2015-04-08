package com.example.csuay.twear.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.csuay.twear.R;
import com.example.csuay.twear.model.Tuit;
import com.example.csuay.twear.singleton.TwearSingleton;
import com.example.csuay.twear.utils.TwearUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class LoggedActivity extends ActionBarActivity implements DataApi.DataListener,
        MessageApi.MessageListener, NodeApi.NodeListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    TextView loggedText;
    LinearLayout timeLineLayout;

    Button sendTweets;

    TwitterSession twitterSession;

    private String TAG = "LoggedActivity";
    private static final String START_ACTIVITY_PATH = "/start-activity-twear";
    private static final String SEND_TWEETS_PATH = "/send-tweets-twear";
    boolean tweetsAcquired;

    boolean wearableConnected;
    private GoogleApiClient mGoogleApiClient;

    private List<Tuit> mTuits;

    private void addTuit(Tuit tuit) {
        if (mTuits == null) {
            mTuits = new ArrayList<>();
        }
        mTuits.add(tuit);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged);

        //TODO REMOVE THIS
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        //TODO REMOVE THIS
        loggedText = (TextView) findViewById(R.id.logged_username);
        timeLineLayout = (LinearLayout) findViewById(R.id.timeline_layout);

        twitterSession = TwearSingleton.INSTANCE.getTwitterSession();
        loggedText.setText(twitterSession.getUserName());
        TwitterApiClient client = new TwitterApiClient(twitterSession);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        Callback<List<Tweet>> twCallback = new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> listResult) {
                Button buttonTweet;
                for (Tweet tweet : listResult.data) {
                    buttonTweet = new Button(getApplicationContext());
                    buttonTweet.setTextColor(Color.parseColor("#000000"));
                    buttonTweet.setText(tweet.user.name + "\n" + tweet.text + "\n" + TwearUtils.parseTwitterDate(tweet.createdAt).getTime() + "\n");

                   /*
                   TEST LOAD IMAGES ASYNC
                    */

                    timeLineLayout.addView(buttonTweet);

                    addTuit(parseTuit(tweet));

                }
                tweetsAcquired = true;
                enableSendTweetsButton();
                mGoogleApiClient.connect();
            }

            @Override
            public void failure(TwitterException e) {

            }
        };
        client.getStatusesService().homeTimeline(10, null, null, null, null, null, null, twCallback);

        sendTweets = (Button) findViewById(R.id.send_tweets_button);
        sendTweets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendTweetsClick(v);

            }
        });


    }

    private Tuit parseTuit(Tweet tweet) {
        Tuit tuit = new Tuit();
        tuit.setText(tweet.text);
        tuit.setUserName(tweet.user.name);
        tuit.setTimestamp(TwearUtils.parseTwitterDate(tweet.createdAt).getTime());
        Bitmap bm = null;
        Uri imageUri = null;

        Bitmap bm = null;
        String imageUrl = null;
        if (tweet.entities.media != null && tweet.entities.media.size() > 0) {
            imageUrl = tweet.entities.media.get(0).mediaUrl;
        } else {
            imageUrl = tweet.user.profileImageUrlHttps;
        }
        try {
            bm = BitmapFactory.decodeStream((InputStream) new URL(imageUrl).getContent());
        } catch (IOException e) {
            Log.e("ERROR", "URI ERROR");
            return tuit;
        }
        tuit.setAsset(TwearUtils.toAsset(bm));
        return tuit;
    }


    protected void enableSendTweetsButton() {
        if (tweetsAcquired && wearableConnected) {
            sendTweets.setEnabled(true);
        } else {
            sendTweets.setEnabled(false);
        }
    }


    /*


     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.menu_logged, menu);
         return true;
     }
 */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        wearableConnected = true;
        enableSendTweetsButton();
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        wearableConnected = false;
        enableSendTweetsButton();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        LOGD(TAG, "onMessageReceived() A message from watch was received:" + messageEvent
                .getRequestId() + " " + messageEvent.getPath());
    }

    @Override
    public void onPeerConnected(Node node) {
        LOGD(TAG, "onPeerConnected: " + node);
    }

    @Override
    public void onPeerDisconnected(Node node) {
        LOGD(TAG, "onPeerDisconnected: " + node);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        wearableConnected = false;
        enableSendTweetsButton();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        Wearable.NodeApi.removeListener(mGoogleApiClient, this);
    }

    private static void LOGD(final String tag, String message) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message);
        }
    }


    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }

        return results;
    }

    private void sendStartActivityMessage(String node) {
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, START_ACTIVITY_PATH, new byte[0]).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to send message with status code: "
                                    + sendMessageResult.getStatus().getStatusCode());
                        }
                    }
                }
        );
    }

    private class StartWearableActivityTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendStartActivityMessage(node);
            }
            return null;
        }
    }

    /**
     * Sends an RPC to start a fullscreen Activity on the wearable.
     */
    public void onStartWearableActivityClick(View view) {
        LOGD(TAG, "Generating RPC");

        // Trigger an AsyncTask that will query for a list of connected nodes and send a
        // "start-activity" message to each connected node.
        new StartWearableActivityTask().execute();
    }


    public class SendTweetsWearableActivityTask extends AsyncTask<Void, Void, Void> {

        private List<Tuit> mTuits;

        public SendTweetsWearableActivityTask(List<Tuit> tuits) {
            this.mTuits = tuits;
        }

        @Override
        public Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                for (Tuit tuit : mTuits) {
                    sendTuitsMessage(node, tuit);
                }
            }
            return null;
        }
    }

    private void sendTuitsMessage(String node, Tuit tuit) {
        DataMap data = new DataMap();
        data.putString("text", tuit.getText());
        data.putString("user", tuit.getUserName());
        data.putLong("timestamp", tuit.getTimestamp());
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, SEND_TWEETS_PATH, data.toByteArray()).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to send message with status code: "
                                    + sendMessageResult.getStatus().getStatusCode());
                        }
                    }
                }
        );
    }

    public void onSendTweetsClick(View view) {
        LOGD(TAG, "Generating RPC");

        // Trigger an AsyncTask that will query for a list of connected nodes and send a
        // "start-activity" message to each connected node.
        new SendTweetsWearableActivityTask(mTuits).execute();
    }

}
