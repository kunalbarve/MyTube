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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cmpe277.lab2.mytube.Utility.Constatnts;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FavoriteFragment extends Fragment {

    private ListView favoriteVideos;
    private List<VideoItem> favoriteResults;
    private Handler handler;
    private Context context = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_favorite, container, false);
        context = getActivity().getApplicationContext();

        favoriteVideos = (ListView)v.findViewById(R.id.favorite_found);
        new GetFavoriteVideos().execute();

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

        return  v;
    }

    private void updateVideosFound(){
        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(context, R.layout.favorite_video_item, favoriteResults){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.favorite_video_item, parent, false);
                }
                ImageView thumbnail = (ImageView)convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView)convertView.findViewById(R.id.video_title);
                TextView viewCounts = (TextView)convertView.findViewById(R.id.view_count);
                TextView publishDate = (TextView)convertView.findViewById(R.id.publish_date);

                VideoItem item = favoriteResults.get(position);
                Picasso.with(context).load(item.getThumbnailURL()).into(thumbnail);
                title.setText(item.getTitle());
                viewCounts.setText("Views: "+item.getViewCount());
                publishDate.setText(item.getPublishDate());
                return convertView;
            }
        };

        favoriteVideos.setAdapter(adapter);
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

}
