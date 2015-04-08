package com.example.csuay.twear.activity;

import android.app.Notification;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.csuay.twear.R;
import com.example.csuay.twear.singleton.TwearSingleton;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.List;

public class LoggedActivity extends ActionBarActivity {
    TextView loggedText;
    LinearLayout timeLineLayout;

    TwitterSession twitterSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged);

        loggedText = (TextView) findViewById(R.id.logged_username);
        timeLineLayout = (LinearLayout) findViewById(R.id.timeline_layout);

        twitterSession = TwearSingleton.INSTANCE.getTwitterSession();
        loggedText.setText(twitterSession.getUserName());
        TwitterApiClient client = new TwitterApiClient(twitterSession);

        Callback<List<Tweet>> twCallback = new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> listResult) {
                Button tweet;
                for (Tweet tuit : listResult.data) {
                    tweet = new Button(getApplicationContext());
                    tweet.setTextColor(Color.parseColor("#000000"));
                    tweet.setText(tuit.text + "\n");
                    tweet.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Notification notification = new NotificationCompat.Builder(getApplication())
                                    .setContentTitle("Hello World")
                                    .setContentText("My first Android Wear notification")
                                    .extend(
                                            new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                                    .build();

                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplication());

                            int notificationId = 1;
                            notificationManager.notify(notificationId, notification);
                        }
                    });
                    timeLineLayout.addView(tweet);
                }
            }

            @Override
            public void failure(TwitterException e) {

            }
        };


        client.getStatusesService().homeTimeline(200, null, null, null, null, null, null, twCallback);
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
}
