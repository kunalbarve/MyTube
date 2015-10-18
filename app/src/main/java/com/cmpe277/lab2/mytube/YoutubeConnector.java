package com.cmpe277.lab2.mytube;

import android.content.Context;
import android.util.Log;

import com.cmpe277.lab2.mytube.Utility.Constatnts;
import com.cmpe277.lab2.mytube.Utility.SessionManager;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Joiner;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class YoutubeConnector {
    private YouTube youTube;
    private static YouTube youtube;
    private static YouTube.Search.List query;
    private static SessionManager session;
    private static String accessToken;

    private static void setUpYouTube() {
        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {
            }
        }).setApplicationName(Constatnts.APPLICATION_NAME).build();
    }

    private static void resetYoutube(Context context) {
        session = new SessionManager(context);
        accessToken = "ya29.DwKATt7FUU-YjRc9jFpqNH-7eBJ1qwwCZAyn9VvWOX_EDl5_hYHlPJrrC7R67hwp6hsi";
        Log.d(Constatnts.TAG, "Token:" + accessToken);
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName(Constatnts.APPLICATION_NAME).build();
    }


    public YoutubeConnector(Context context) {

      /*  session = new SessionManager(context);
        accessToken = session.getToken();
        Log.d(Constatnts.TAG, "Token:"+accessToken);
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName(Constatnts.APPLICATION_NAME).build();*/
    }

    public static List<VideoItem> search(String keywords, Context context) {
        Log.d(Constatnts.TAG, "Query" + keywords);

        try {
            setUpYouTube();
            query = youtube.search().list("id,snippet");
            query.setKey(Constatnts.YOUTUBE_KEY);
            query.setType("video");
            query.setMaxResults(Constatnts.NUMBER_OF_VIDEOS_RETURNED);
            query.setFields("items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url,snippet/publishedAt)");
            //query.setFields("items(id/videoId)");
            query.setQ(keywords);
            //SearchListResponse response = query.setKey(Constatnts.YOUTUBE_KEY).setOauthToken(accessToken).execute();
            SearchListResponse response = query.execute();

            List<SearchResult> searchResultList = response.getItems();

            List<VideoItem> items = new ArrayList<VideoItem>();

            List<String> videoIds = new ArrayList<String>();

            if (searchResultList != null) {
                Log.d(Constatnts.TAG, "My Search Size" + searchResultList.size());
                items = createItemUsingSearch(searchResultList.iterator());
                //getVideos(searchResultList, context);
                createPlayList(context);
                /*for (SearchResult searchResult : searchResultList) {
                    videoIds.add(searchResult.getId().getVideoId());
                }
                Joiner stringJoiner = Joiner.on(',');
                String videoId = stringJoiner.join(videoIds);

                Log.d(Constatnts.TAG, "Video Ids" + videoId);

                YouTube.Videos.List listVideosRequest = youtube.videos().list("snippet, statistics, id").setId(videoId);
                VideoListResponse listResponse = listVideosRequest.setKey(Constatnts.YOUTUBE_KEY).setOauthToken(accessToken).execute();
                List<Video> videoList = listResponse.getItems();

                Log.d(Constatnts.TAG, "My Videos Size" + videoList.size());

                if (videoList != null) {
                    return createItems(videoList.iterator());
                }*/
            }

            return items;
        } catch (Exception e) {
            Log.e(Constatnts.TAG, "Error in Video Search: ", e);
            return new ArrayList<VideoItem>();
        }
    }

    public static void getVideos(List<SearchResult> searchResultList, Context context) {
        resetYoutube(context);
        List<String> videoIds = new ArrayList<String>();
        try {
            for (SearchResult searchResult : searchResultList) {
                videoIds.add(searchResult.getId().getVideoId());
            }
            Joiner stringJoiner = Joiner.on(',');
            String videoId = stringJoiner.join(videoIds);

            Log.d(Constatnts.TAG, "Video Ids" + videoId);

            YouTube.Videos.List listVideosRequest = youtube.videos().list("snippet, statistics, id").setId(videoId);
            VideoListResponse listResponse = listVideosRequest.setKey(Constatnts.YOUTUBE_KEY).setOauthToken(accessToken).execute();
            List<Video> videoList = listResponse.getItems();

            Log.d(Constatnts.TAG, "My Videos Size" + videoList.size());


        } catch (Exception e) {
            Log.e(Constatnts.TAG, "Error in Video getting: ", e);
        }
    }

    public List<VideoItem> createItems(Iterator<Video> iteratorVideoResults) {
        List<VideoItem> items = new ArrayList<>();

        try {
            while (iteratorVideoResults.hasNext()) {
                Video singleVideo = iteratorVideoResults.next();

                VideoItem item = new VideoItem();
                item.setTitle(singleVideo.getSnippet().getTitle());
                item.setDescription(singleVideo.getSnippet().getDescription());
                item.setThumbnailURL(singleVideo.getSnippet().getThumbnails().getDefault().getUrl());
                item.setId(singleVideo.getId());
                item.setPublishDate(singleVideo.getSnippet().getPublishedAt().toString());
                item.setViewCount(singleVideo.getStatistics().getViewCount().intValue());
                item.setLikeCount(singleVideo.getStatistics().getLikeCount().intValue());
                item.setDislikeCount(singleVideo.getStatistics().getDislikeCount().intValue());
                Log.d(Constatnts.TAG, "Video Detail>>>>" + item.toString());
                items.add(item);
            }
        } catch (Exception e) {
            Log.e(Constatnts.TAG, "error in video iteration ", e);
            return items;
        }

        return items;
    }

    public static List<VideoItem> createItemUsingSearch(Iterator<SearchResult> searchIterator) {
        List<VideoItem> items = new ArrayList<>();

        try {
            while (searchIterator.hasNext()) {
                SearchResult searchResult = searchIterator.next();

                VideoItem item = new VideoItem();
                item.setTitle(searchResult.getSnippet().getTitle());
                item.setDescription(searchResult.getSnippet().getDescription());
                item.setThumbnailURL(searchResult.getSnippet().getThumbnails().getDefault().getUrl());
                item.setId(searchResult.getId().getVideoId());
                item.setPublishDate(searchResult.getSnippet().getPublishedAt().toString());
                //Log.d("MYTUBE","Search Item Detail>>>>"+item.toString());
                items.add(item);
            }
        } catch (Exception e) {
            Log.e(Constatnts.TAG, "error in search iteration ", e);
            return items;
        }

        return items;
    }

    public static void createPlayList(Context context) {
        try {
            resetYoutube(context);
            PlaylistSnippet playListObj = new PlaylistSnippet();
            playListObj.setTitle("SJSU");
            playListObj.setDefaultLanguage("Test");

            PlaylistStatus status = new PlaylistStatus();
            status.setPrivacyStatus("private");

            Playlist playList = new Playlist();
            playList.setSnippet(playListObj);
            playList.setStatus(status);

            YouTube.Playlists.Insert insert = youtube.playlists().insert("snippet,status", playList);
            Playlist insertedPlayList = insert.setKey(Constatnts.YOUTUBE_KEY).setOauthToken(accessToken).execute();
            Log.d(Constatnts.TAG, "Id:"+insertedPlayList.getId());
        } catch (Exception e) {
            Log.e(Constatnts.TAG, "error in list creation ", e);
        }


    }
}
