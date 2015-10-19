package com.cmpe277.lab2.mytube.Utility;
import com.google.android.gms.common.Scopes;
import com.google.api.services.youtube.YouTubeScopes;

/**
 * Created by knbarve on 10/13/15.
 */
public class Constatnts {

    public static final String TAG = "MyTube";

    public static final String PLAYLIST_NAME = "SJSU-CMPE-277";

    public static final String APPLICATION_NAME = "MyTube";

    public static final String YOUTUBE_KEY = "AIzaSyDtDJp32YdBH37ovd0a5Lj88BY1f2INoJ8";

    public static final long NUMBER_OF_VIDEOS_RETURNED = 30;

    public static final String SCOPE_STRING = "oauth2:" + Scopes.PROFILE + " " + YouTubeScopes.YOUTUBE + " " + YouTubeScopes.YOUTUBE_UPLOAD + " " + Scopes.EMAIL;

    public static final String INITIAL_SEARCH_WORD = "Latest Technology 2015";


}
