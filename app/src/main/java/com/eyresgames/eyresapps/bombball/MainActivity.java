package com.eyresgames.eyresapps.bombball;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.example.games.basegameutils.BaseGameUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RewardedVideoAdListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private RewardedVideoAd mAd;

    //image button
    private CardView buttonPlay;
    private CardView extraLives;
    private TextView livesTV;
    private TextView highScore;
    private TextView worldHighScore;

    Lives lives;
    AlertDialog alertDialog;



    //--------------------------------GOOGLE PLAY SERVICES
    private static int RC_SIGN_IN = 9001;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;
    private boolean mSignInClicked = false;

    boolean mExplicitSignOut = false;
    boolean mInSignInFlow = false; // set to true when you're in the middle of the
    // sign in flow, to know you should not attempt
    // to connect in onStart()
    GoogleApiClient mGoogleApiClient;  // initialized in onCreate

    Button signOutButton;
    SignInButton signInButton;
    CardView leaderboard;
    CardView showAchievements;
    long one;

    SharedPreferences sharedPreferences;
    SharedPreferences currentGoogleScoreSharedPreferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        worldHighScore =(TextView)findViewById(R.id.worldHighScore);

        //--------------------------GOOGLE PLAY LEADERBOARD STUFF

        // Create the Google Api Client with access to the Play Games services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API)
                .addScope(Games.SCOPE_GAMES)
                // add other APIs and scopes here as needed
                .build();

        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);
        signOutButton = (Button)findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(this);
        leaderboard = (CardView) findViewById(R.id.showLeaderboards);
        leaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mGoogleApiClient.isConnected()) {
                    startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                            getString(R.string.leaderboard_id)), 0);
                }

            }
        });
        showAchievements = (CardView)findViewById(R.id.showAchievements);
        showAchievements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mGoogleApiClient.isConnected()) {
                    startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                            0);

                }
            }
        });

        /*if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            SharedPreferences sharedPreferences = this.getSharedPreferences("highScore", Context.MODE_PRIVATE);
            String one = "";
            if(!sharedPreferences.getString("1", one).equals("")){
                one = sharedPreferences.getString("1", one);
                Games.Leaderboards.submitScore(mGoogleApiClient, LEADERBOARD_ID, Long.parseLong(one));

            }
        }*/
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
        } else {
            signOutButton.setVisibility(View.GONE);
            signInButton.setVisibility(View.VISIBLE);
        }

        //-------------------------------------------------


        lives = Lives.getInstance();
        lives.initializeLives(this);

        //setting the orientation to landscape
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //getting the button
        buttonPlay = (CardView) findViewById(R.id.start);

        //adding a click listener
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lives.getLives() > 0) {
                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(MainActivity.this, "You need to wait x minutes to refresh your lives or watch a video now to replenish your lives",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mAd = MobileAds.getRewardedVideoAdInstance(this);
        mAd.setRewardedVideoAdListener(this);

        extraLives = (CardView)findViewById(R.id.extraLives);
        extraLives.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadRewardedVideoAd();
                alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setMessage("Loading Video...");
                alertDialog.show();
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (mAd.isLoaded()) {
                            alertDialog.dismiss();
                            mAd.show();
                        }else{
                            handler.postDelayed(this,100);
                        }
                    }
                };
                handler.postDelayed(runnable,100);

            }
        });

        livesTV = (TextView)findViewById(R.id.lives);

        highScore = (TextView)findViewById(R.id.highScore);

    }

    private void loadRewardedVideoAd() {
        mAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());
    }

    // Required to reward the user.
    @Override
    public void onRewarded(RewardItem reward) {
        //Toast.makeText(this, "onRewarded! currency: " + reward.getType() + "  amount: " +
        //        reward.getAmount(), Toast.LENGTH_SHORT).show();
        int currentLives = reward.getAmount();
        if(currentLives != 5){
            currentLives = 5;
        }
        lives.setLives(currentLives, this);
        livesTV.setText("CURRENT \nLIVES \n"+ lives.getLives());
        sharedPreferences = getSharedPreferences("timeSinceLastLifeRegen", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putLong("timeSinceLastLifeRegen", System.currentTimeMillis());
        editor.commit();
        // Reward the user.
    }

    // The following listener methods are optional.
    @Override
    public void onRewardedVideoAdLeftApplication() {
        //Toast.makeText(this, "onRewardedVideoAdLeftApplication",
        //        Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdClosed() {
        //Toast.makeText(this, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        Toast.makeText(this, "Error try again", Toast.LENGTH_SHORT).show();
        alertDialog.dismiss();
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        //Toast.makeText(this, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdOpened() {
        //Toast.makeText(this, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoStarted() {
        //Toast.makeText(this, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
    }

    //-------------------------GOOGLE PLAY SERVICES LEADERBOARD STUFF
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // Already resolving
            return;
        }

        // If the sign in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInflow) {
            mAutoStartSignInflow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, "signin_other_error")) {
                mResolvingConnectionFailure = false;
            }
        }

        // Put code here to display the sign-in button
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!mInSignInFlow && !mExplicitSignOut) {
            // auto sign in
            mGoogleApiClient.connect();

        }
        livesTV.setText("CURRENT\nLIVES\n"+ lives.getLives());
        if(lives.getLives() < 5) {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastLifeRegen = 0;
            sharedPreferences = getSharedPreferences("timeSinceLastLifeRegen", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            timeSinceLastLifeRegen = sharedPreferences.getLong("timeSinceLastLifeRegen", timeSinceLastLifeRegen);

            long halfHoursPassed = (((currentTime - timeSinceLastLifeRegen) / 1000) / 60) / 30;
            long minutesLeftOver = (((currentTime - timeSinceLastLifeRegen) / 1000) / 60) % 30;
            long timeLeftOver = currentTime - timeSinceLastLifeRegen;

            if (timeSinceLastLifeRegen == 0) {
                editor.putLong("timeSinceLastLifeRegen", currentTime);
                editor.commit();

            } else if (timeSinceLastLifeRegen != 0 && halfHoursPassed > 0) {
                long currentLivesPlusEarnedLives = lives.getLives() + halfHoursPassed;
                if (lives.getLives() < 5) {
                    if (currentLivesPlusEarnedLives > 5) {
                        lives.setLives(5, this);
                    } else {
                        lives.setLives((int) currentLivesPlusEarnedLives, this);
                    }
                    if (lives.getLives() < 5) {
                        timeSinceLastLifeRegen = currentTime + timeLeftOver;
                        editor.putLong("timeSinceLastLifeRegen", timeSinceLastLifeRegen);
                        editor.commit();
                    }
                }
            }
            //timeSinceLastLifeRegen = currentTime + (minutesLeftOver * 1000 * 60);
            livesTV.setText("CURRENT\nLIVES\n"+ lives.getLives());
            livesTV.setText(livesTV.getText() + "\nFREE LIFE IN\n" + (30 - minutesLeftOver) + " MINUTES");
        }
        refreshScore();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }


    @Override
    public void onClick (View view) {
        if (view.getId() == R.id.sign_out_button) {
            // user explicitly signed out, so turn off auto sign in
            mExplicitSignOut = true;
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                Games.signOut(mGoogleApiClient);
                mGoogleApiClient.disconnect();
                findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            }
        }

        if (view.getId() == R.id.sign_in_button) {
            // start the asynchronous sign in flow
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }
    }




    @Override
    public void onConnected(Bundle connectionHint) {
        // show sign-out button, hide the sign-in button
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);

        // (your code here: update UI, enable functionality that depends on sign in, etc)
        currentGoogleScoreSharedPreferences = this.getSharedPreferences("currentScoreOnGooglePlay", Context.MODE_PRIVATE);
        editor = currentGoogleScoreSharedPreferences.edit();
        long lastUpdate = 0;
        lastUpdate = currentGoogleScoreSharedPreferences.getLong("lastUpdate", lastUpdate);
        sharedPreferences = this.getSharedPreferences("highScore", Context.MODE_PRIVATE);
        one = 0;
        one = sharedPreferences.getLong("1", one);
        if(lastUpdate == 0 && one != 0) {
            updateLeaderboards(mGoogleApiClient, getString(R.string.leaderboard_id), Long.toString(one));
            editor.putLong("lastUpdate",one);
        }else if(lastUpdate != 0 && lastUpdate != one ) {
            updateLeaderboards(mGoogleApiClient, getString(R.string.leaderboard_id), Long.toString(one));
            editor.putLong("lastUpdate",one);
        }
        editor.commit();
        checkIfAchievmentEarned();
        loadScoreOfLeaderBoard();

        System.out.println("connected");
    }


    @Override
    public void onConnectionSuspended(int i) {
// Attempt to reconnect
        mGoogleApiClient.connect();
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // Bring up an error dialog to alert the user that sign-in
                // failed. The R.string.signin_failure should reference an error
                // string in your strings.xml file that tells the user they
                // could not be signed in, such as "Unable to sign in."
                BaseGameUtils.showActivityResultError(this,
                        requestCode, resultCode, R.string.signin_failure);
            }
        }
    }

    private void loadScoreOfLeaderBoard() {
        Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient, getString(R.string.leaderboard_id), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
            @Override
            public void onResult(final Leaderboards.LoadPlayerScoreResult scoreResult) {
                if (isScoreResultValid(scoreResult)) {
                    // here you can get the score like this
                    long points = scoreResult.getScore().getRawScore();
                    long position = scoreResult.getScore().getRank();
                    worldHighScore.setText("GOOGLE PLAY\nRank\n" + position + "\nScore\n" + points);
                }
            }
        });
    }

    private boolean isScoreResultValid(final Leaderboards.LoadPlayerScoreResult scoreResult) {
        return scoreResult != null && GamesStatusCodes.STATUS_OK == scoreResult.getStatus().getStatusCode() && scoreResult.getScore() != null;
    }

    private void updateLeaderboards(final GoogleApiClient googleApiClient, final String leaderboardId, final String newScore) {
        Games.Leaderboards.loadCurrentPlayerLeaderboardScore(
                googleApiClient,
                leaderboardId,
                LeaderboardVariant.TIME_SPAN_ALL_TIME,
                LeaderboardVariant.COLLECTION_PUBLIC
        ).setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {

            @Override
            public void onResult(Leaderboards.LoadPlayerScoreResult loadPlayerScoreResult) {
                if (loadPlayerScoreResult != null) {
                    if (GamesStatusCodes.STATUS_OK == loadPlayerScoreResult.getStatus().getStatusCode()) {
                        long scoreChecker = Long.parseLong(newScore);
                        long score = 0;
                        if (loadPlayerScoreResult.getScore() != null) {
                            score = loadPlayerScoreResult.getScore().getRawScore();
                        }
                        if (score == 0 || score < scoreChecker) {
                            Games.Leaderboards.submitScore(googleApiClient, leaderboardId, scoreChecker);
                            loadScoreOfLeaderBoard();
                        }

                    }

                }
            }

        });
    }

    public void checkIfAchievmentEarned(){
        long lastScore = 0;
        sharedPreferences = this.getSharedPreferences("highScore", Context.MODE_PRIVATE);
        lastScore = sharedPreferences.getLong("lastScore", lastScore);
        System.out.println("score = " + lastScore);
        if(mGoogleApiClient.isConnected()) {
            if (lastScore == 0) {
                Games.Achievements.unlock(mGoogleApiClient, getBaseContext().getString(R.string.achievement_0).toString());
            } else if (lastScore > 9000 && lastScore <= 9020) {
                Games.Achievements.reveal(mGoogleApiClient,getBaseContext().getString(R.string.achievement_9000).toString());
                Games.Achievements.unlock(mGoogleApiClient, getBaseContext().getString(R.string.achievement_9000).toString());
            } else if (lastScore > 1000 && lastScore < 15000) {
                Games.Achievements.unlock(mGoogleApiClient, getBaseContext().getString(R.string.achievement_noob).toString());
            } else if (lastScore > 15000 && lastScore < 15000) {
                Games.Achievements.unlock(mGoogleApiClient, getBaseContext().getString(R.string.achievement_noob).toString());
            }
        }
    }
    public void refreshScore(){
        SharedPreferences sharedPreferences = getSharedPreferences("highScore",Context.MODE_PRIVATE);
        one = 0;
        one = sharedPreferences.getLong("1", one);
        long two = 0;
        two = sharedPreferences.getLong("2", two);
        long three = 0;
        three = sharedPreferences.getLong("3", three);

        if(one != 0 && two != 0 && three != 0){
            highScore.setText("HIGH SCORE\n"+one+"\n"+two+"\n"+three);
        }else if(one != 0 && two != 0){
            highScore.setText("HIGH SCORE\nONE: "+one+"\nTWO: "+two);
        }else if(one != 0){
            highScore.setText("HIGH SCORE\nONE: "+one);
        }else {
            highScore.setText("HIGH SCORE");
        }

    }
}