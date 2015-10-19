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
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class YoutubeConnector {
    private static YouTube youtube;
    private YouTube.Search.List query;
    private SessionManager session;
    private static String accessToken;

    public YoutubeConnector(Context context) {
        session = new SessionManager(context);
        accessToken = session.getToken();
        Log.d(Constatnts.TAG, "Token:" + accessToken);
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName(Constatnts.APPLICATION_NAME).build();
    }

    //m-GK8hgfWKc, rhWFs0QVIgU, 7xevFud9lzg
    public List<VideoItem> search(String keywords) {
        try {
            query = youtube.search().list("id,snippet");
            query.setKey(Constatnts.YOUTUBE_KEY);
            query.setType("video");
            query.setMaxResults(Constatnts.NUMBER_OF_VIDEOS_RETURNED);
            query.setFields("items(id/videoId)");
            query.setQ(keywords);
            SearchListResponse response = query.execute();

            List<SearchResult> searchResultList = response.getItems();

            List<VideoItem> items = new ArrayList<VideoItem>();

            List<String> videoIds = new ArrayList<String>();

            if (searchResultList != null) {

                for (SearchResult searchResult : searchResultList) {
                    videoIds.add(searchResult.getId().getVideoId());
                }
                Joiner stringJoiner = Joiner.on(',');
                String videoId = stringJoiner.join(videoIds);

                YouTube.Videos.List listVideosRequest = youtube.videos().list("snippet, statistics, id").setId(videoId);
                VideoListResponse listResponse = listVideosRequest.execute();
                List<Video> videoList = listResponse.getItems();

                if (videoList != null) {
                    items = getVideoDetailsForSearch(videoList.iterator());
                }
            }

            return items;
        } catch (Exception e) {
            Log.e(Constatnts.TAG, "Error in Video Search: ", e);
            return new ArrayList<VideoItem>();
        }
    }

    public List<VideoItem> getVideoDetailsForSearch(Iterator<Video> iteratorVideoResults) throws Exception {
        Log.d(Constatnts.TAG, "Came for video making");
        List<VideoItem> items = new ArrayList<>();
        List<String> favoriteIds = getFavoriteIds();
        while (iteratorVideoResults.hasNext()) {
            Video singleVideo = iteratorVideoResults.next();

            VideoItem item = new VideoItem();
            item.setTitle(singleVideo.getSnippet().getTitle());
            item.setDescription("");
            item.setThumbnailURL(singleVideo.getSnippet().getThumbnails().getDefault().getUrl());
            item.setId(singleVideo.getId());
            item.setPublishDate(singleVideo.getSnippet().getPublishedAt().toString());
            if (singleVideo.getStatistics() != null && singleVideo.getStatistics().getViewCount() != null) {
                item.setViewCount(singleVideo.getStatistics().getViewCount().intValue());
                //item.setLikeCount(singleVideo.getStatistics().getLikeCount().intValue());
                //item.setDislikeCount(singleVideo.getStatistics().getDislikeCount().intValue());
            } else {
                item.setViewCount(200);
            }


            if (favoriteIds.contains(item.getId())) {
                item.setIsFavorite(true);
            } else {
                item.setIsFavorite(false);
            }


            items.add(item);
        }
        return items;
    }

    public List<VideoItem> getVideoDetailsForFavorite(Iterator<Video> iteratorVideoResults) throws Exception {
        Log.d(Constatnts.TAG, "Came for video making");
        List<VideoItem> items = new ArrayList<>();

        while (iteratorVideoResults.hasNext()) {
            Video singleVideo = iteratorVideoResults.next();

            VideoItem item = new VideoItem();
            item.setTitle(singleVideo.getSnippet().getTitle());
            item.setDescription("");
            item.setThumbnailURL(singleVideo.getSnippet().getThumbnails().getDefault().getUrl());
            item.setId(singleVideo.getId());
            item.setPublishDate(singleVideo.getSnippet().getPublishedAt().toString());
            if (singleVideo.getStatistics() != null && singleVideo.getStatistics().getViewCount() != null) {
                item.setViewCount(singleVideo.getStatistics().getViewCount().intValue());
                //item.setLikeCount(singleVideo.getStatistics().getLikeCount().intValue());
                //item.setDislikeCount(singleVideo.getStatistics().getDislikeCount().intValue());
            } else {
                item.setViewCount(200);
            }

            item.setIsFavorite(true);

            items.add(item);
        }
        return items;
    }

    public List<String> getFavoriteIds() throws Exception {
        List<PlaylistItem> favoriteVideos = getPlayListItems();
        List<String> ids = new ArrayList<>();

        for (PlaylistItem item : favoriteVideos) {
            ids.add(item.getContentDetails().getVideoId());
        }
        Log.d(Constatnts.TAG, "Came for fetching favorite list" + favoriteVideos.size());
        return ids;
    }

    public String getPlayListDetail() throws Exception {
        String playListId = "";
        Log.d(Constatnts.TAG, "Came for fetching channels");
        YouTube.Playlists.List channels = youtube.playlists().list("snippet").setMine(true);
        PlaylistListResponse channelDetail = channels.execute();

        if (channelDetail.getItems().size() > 0) {
            for (Playlist playList : channelDetail.getItems()) {
                if (playList.getSnippet().getTitle().equalsIgnoreCase(Constatnts.PLAYLIST_NAME)) {
                    playListId = playList.getId();
                    break;
                }
            }
        }

        Log.d(Constatnts.TAG, "Channel size" + channelDetail.getItems().size());

        if (playListId.equalsIgnoreCase("")) {
            playListId = createPlayList();
        }

        return playListId;
    }

    public String createPlayList() throws Exception {
        String createdId = "";
        PlaylistSnippet playListObj = new PlaylistSnippet();
        playListObj.setTitle(Constatnts.PLAYLIST_NAME);
        playListObj.setDescription("Playlist created for using MyTube for CMPE 277 class.");

        PlaylistStatus status = new PlaylistStatus();
        status.setPrivacyStatus("private");

        Playlist playList = new Playlist();
        playList.setSnippet(playListObj);
        playList.setStatus(status);

        YouTube.Playlists.Insert insert = youtube.playlists().insert("snippet,status", playList);
        Playlist insertedPlayList = insert.execute();
        createdId = insertedPlayList.getId();
        Log.d(Constatnts.TAG, "Id:" + insertedPlayList.getId());

        return createdId;
    }

    public void insertVideo(String videoId) throws Exception {
        String playListId = getPlayListDetail();

        ResourceId resource = new ResourceId();
        resource.setKind("youtube#video");
        resource.setVideoId(videoId);

        PlaylistItemSnippet snippet = new PlaylistItemSnippet();
        snippet.setTitle("");
        snippet.setPlaylistId(playListId);
        snippet.setResourceId(resource);

        PlaylistItem item = new PlaylistItem();
        item.setSnippet(snippet);

        YouTube.PlaylistItems.Insert insertVideo = youtube.playlistItems().insert("snippet,contentDetails", item);
        insertVideo.execute();
    }

    public void removeVideos(List<String> videoIds) throws Exception {
        List<PlaylistItem> playListItems = getPlayListItems();
        for (PlaylistItem item : playListItems) {
            if (videoIds.contains(item.getContentDetails().getVideoId())) {
                YouTube.PlaylistItems.Delete deleteVideo = youtube.playlistItems().delete(item.getId());
                deleteVideo.execute();
            }
        }
    }

    public List<PlaylistItem> getPlayListItems() throws Exception {
        Log.d(Constatnts.TAG, "Came for fetching playlist items");
        String playListId = getPlayListDetail();

        List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();

        YouTube.PlaylistItems.List playlistItemRequest =
                youtube.playlistItems().list("id,contentDetails,snippet");
        playlistItemRequest.setPlaylistId(playListId);
        playlistItemRequest.setFields("items(id,contentDetails/videoId,snippet/title,snippet/publishedAt),nextPageToken,pageInfo");

        String nextToken = "";

        do {
            playlistItemRequest.setPageToken(nextToken);
            PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();
            playlistItemList.addAll(playlistItemResult.getItems());
            nextToken = playlistItemResult.getNextPageToken();
        } while (nextToken != null);

        Log.d(Constatnts.TAG, "For fetching the playlist against Id:" + playlistItemList.size());

        return playlistItemList;
    }

    public List<VideoItem> getFavoriteVideos() throws Exception {
        Log.d(Constatnts.TAG, "Fetch Favorites");
        List<PlaylistItem> videoItems = getPlayListItems();
        List<VideoItem> items = new ArrayList<VideoItem>();
        List<String> videoIds = new ArrayList<String>();

        for (PlaylistItem playListItem : videoItems) {
            videoIds.add(playListItem.getContentDetails().getVideoId());
        }
        Joiner stringJoiner = Joiner.on(',');
        String videoIdString = stringJoiner.join(videoIds);

        YouTube.Videos.List listVideosRequest = youtube.videos().list("snippet, statistics, id").setId(videoIdString);
        VideoListResponse listResponse = listVideosRequest.execute();
        List<Video> videoList = listResponse.getItems();

        Log.d(Constatnts.TAG, "Go For Video Making" + videoList.size());
        if (videoList != null) {
            items = getVideoDetailsForFavorite(videoList.iterator());
        }

        return items;
    }

}
