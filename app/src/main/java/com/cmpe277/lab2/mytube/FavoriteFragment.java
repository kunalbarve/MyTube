package com.cmpe277.lab2.mytube;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cmpe277.lab2.mytube.Utility.Constatnts;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment {

    private ListView favoriteVideos;
    private List<VideoItem> favoriteResults;
    private Handler handler;
    private Context context = null;
    List<String> removalIds = new ArrayList<>();
    private Button removeButton;
    private  Button refreshButton;
    ArrayAdapter<VideoItem> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favorite, container, false);
        context = getActivity().getApplicationContext();

        favoriteVideos = (ListView) v.findViewById(R.id.favorite_found);
        removeButton = (Button) v.findViewById(R.id.removeButton);
        refreshButton = (Button) v.findViewById(R.id.refresh_button);

        loadFavorites();

        handler = new Handler();

        favoriteVideos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("VIDEO_ID", favoriteResults.get(pos).getId());
                startActivity(intent);
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d(Constatnts.TAG, "Removal Ids :"+removalIds.size());
                new RemoveVideos().execute();
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFavorites();
            }
        });

        return v;
    }

    private void updateVideosFound() {
         adapter = new ArrayAdapter<VideoItem>(context, R.layout.favorite_video_item, favoriteResults) {
            ViewHolderItem viewHolder;

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                VideoItem item = favoriteResults.get(position);
                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.favorite_video_item, parent, false);

                    viewHolder = new ViewHolderItem();
                    viewHolder.thumbnail = (ImageView) convertView.findViewById(R.id.video_thumbnail);
                    viewHolder.title = (TextView) convertView.findViewById(R.id.video_title);
                    viewHolder.viewCounts = (TextView) convertView.findViewById(R.id.view_count);
                    viewHolder.publishDate = (TextView) convertView.findViewById(R.id.publish_date);
                    viewHolder.favoriteBox = (CheckBox) convertView.findViewById(R.id.remove_favorite);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolderItem) convertView.getTag();
                }

                viewHolder.favoriteBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        VideoItem updatedItem = (VideoItem) arg0.getTag();
                        if(updatedItem.isFavorite()){
                            removalIds.add(updatedItem.getId());
                        }else{
                            removalIds.remove(updatedItem.getId());
                        }
                        updatedItem.setIsFavorite(!updatedItem.isFavorite());
                        notifyDataSetChanged();

                    }

                });

                Picasso.with(context).load(item.getThumbnailURL()).into(viewHolder.thumbnail);
                viewHolder.title.setText(item.getTitle());
                viewHolder.viewCounts.setText("Views: " + item.getViewCount());
                viewHolder.publishDate.setText(item.getPublishDate());
                viewHolder.favoriteBox.setTag(item);
                viewHolder.favoriteBox.setChecked(!item.isFavorite());
                return convertView;
            }
        };

        favoriteVideos.setAdapter(adapter);
    }

    public void loadFavorites() {
        new GetFavoriteVideos().execute();
    }

    private class GetFavoriteVideos extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                YoutubeConnector yc = new YoutubeConnector(context);
                favoriteResults = yc.getFavoriteVideos();

            } catch (Exception e) {
                Log.e(Constatnts.TAG, "Favorite Video Find Error", e);
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            updateVideosFound();
        }
    }

    private class RemoveVideos extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String isSucceed = "NO";
            try {
                YoutubeConnector yc = new YoutubeConnector(context);
                yc.removeVideos(removalIds);
                isSucceed = "YES";
            } catch (Exception e) {
                Log.e(Constatnts.TAG, "Favorite Video Remove Exception", e);
            }
            return isSucceed;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equalsIgnoreCase("NO")) {
                Toast.makeText(context, "Error occurred, try again!", Toast.LENGTH_SHORT).show();
            }else{
                List<VideoItem> itemsToRemove = new ArrayList<>();
                for(VideoItem item: favoriteResults){
                    if(removalIds.contains(item.getId())){
                        itemsToRemove.add(item);
                    }
                }
                favoriteResults.removeAll(itemsToRemove);
                adapter.notifyDataSetChanged();
            }
        }
    }

}
