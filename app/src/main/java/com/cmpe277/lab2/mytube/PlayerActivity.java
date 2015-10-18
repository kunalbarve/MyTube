package com.cmpe277.lab2.mytube;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.cmpe277.lab2.mytube.Utility.Constatnts;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

/**
 * Created by vaide_000 on 10/12/2015.
 */
public class PlayerActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private YouTubePlayerView playerView;

    @Override
    protected void onCreate(Bundle bundle) {

        super.onCreate(bundle);
        Log.d("SEARCH", "nside player activity");
        setContentView(R.layout.activity_player);

        playerView = (YouTubePlayerView)findViewById(R.id.player_view);
        playerView.initialize(Constatnts.YOUTUBE_KEY, this);
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult result) {
        Log.d("SEARCH", "initialize fail");
        Toast.makeText(this, getString(R.string.failed), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean restored) {
        if(!restored){
            Log.d("SEARCH", "initialize success");
            player.cueVideo(getIntent().getStringExtra("VIDEO_ID"));
        }
    }
}