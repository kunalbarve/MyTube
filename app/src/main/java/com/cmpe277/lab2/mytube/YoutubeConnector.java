package com.cmpe277.lab2.mytube;

import android.content.Context;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Joiner;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by vaide_000 on 10/12/2015.
 */
public class YoutubeConnector {
    private YouTube youtube;
    private YouTube.Search.List query;

    // Your developer key goes here
    public static final String KEY
            = "AIzaSyASKkpE1A0_MO7N6Yh2zuMyTu0tmqajOPk";
    private static final long NUMBER_OF_VIDEOS_RETURNED = 30;

    public YoutubeConnector(Context context) {
        youtube = new YouTube.Builder(new NetHttpTransport(),
                new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {}
        }).setApplicationName(context.getString(R.string.app_name)).build();

        try{
            query = youtube.search().list("id,snippet");
            query.setKey(KEY);
            query.setType("video");
            query.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
            query.setFields("items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url,snippet/publishedAt)");
            //query.setFields("items(id/videoId)");
        }catch(IOException e){
            Log.d("YC", "Could not initialize: " + e);
        }
    }

    public List<VideoItem> search(String keywords){
        Log.d("MYTUBE", "Query"+keywords);
        query.setQ(keywords);
        try{
            SearchListResponse response = query.execute();
            List<SearchResult> searchResultList = response.getItems();

            List<VideoItem> items = new ArrayList<VideoItem>();

            List<String> videoIds = new ArrayList<String>();

            if(searchResultList != null){
                Log.d("MYTUBE","My Search Size"+searchResultList.size());
                return  createItemUsingSearch(searchResultList.iterator());
                /*for (SearchResult searchResult : searchResultList) {
                    videoIds.add(searchResult.getId().getVideoId());
                }
                Joiner stringJoiner = Joiner.on(',');
                String videoId = stringJoiner.join(videoIds);

                Log.d("MYTUBE","Video Ids"+videoId);

                YouTube.Videos.List listVideosRequest = youtube.videos().list("snippet, statistics, id").setId(videoId);
                VideoListResponse listResponse = listVideosRequest.execute();

                List<Video> videoList = listResponse.getItems();

                Log.d("MYTUBE","My Videos Size"+videoList.size());

                if (videoList != null) {
                    return createItems(videoList.iterator());
                }*/
            }

            return items;
        }catch(Exception e){
            Log.e("MYTUBE", "Could not search: ", e);
            return new ArrayList<VideoItem>();
        }
    }

    public List<VideoItem> createItems(Iterator<Video> iteratorVideoResults){
        List<VideoItem> items = new ArrayList<>();

        try{
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
                Log.d("MYTUBE","Video Detail>>>>"+item.toString());
                items.add(item);
            }
        }catch (Exception e){
            Log.e("MYTUBE", "error in video iteration ",e);
            return items;
        }

        return items;
    }

    public List<VideoItem> createItemUsingSearch(Iterator<SearchResult> searchIterator){
        List<VideoItem> items = new ArrayList<>();

        try{
            while (searchIterator.hasNext()) {
                SearchResult searchResult = searchIterator.next();

                VideoItem item = new VideoItem();
                item.setTitle(searchResult.getSnippet().getTitle());
                item.setDescription(searchResult.getSnippet().getDescription());
                item.setThumbnailURL(searchResult.getSnippet().getThumbnails().getDefault().getUrl());
                item.setId(searchResult.getId().getVideoId());
                item.setPublishDate(searchResult.getSnippet().getPublishedAt().toString());
                Log.d("MYTUBE","Search Item Detail>>>>"+item.toString());
                items.add(item);
            }
        }catch (Exception e){
            Log.e("MYTUBE", "error in search iteration ",e);
            return items;
        }

        return items;
    }
}
