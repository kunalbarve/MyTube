package com.cmpe277.lab2.mytube;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cmpe277.lab2.mytube.Utility.Constatnts;
import com.cmpe277.lab2.mytube.Utility.SessionManager;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by vaide_000 on 10/12/2015.
 */
public class SearchActivity extends AppCompatActivity {
    private EditText searchInput;
    private ListView videosFound;
    private List<VideoItem> searchResults;
    private SessionManager session;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        session = new SessionManager(getApplicationContext());

        searchInput = (EditText)findViewById(R.id.search_input);
        videosFound = (ListView)findViewById(R.id.videos_found);

        handler = new Handler();

        searchOnYoutube(Constatnts.INITIAL_SEARCH_WORD);


        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    searchOnYoutube(v.getText().toString());
                    return false;
                }
                return true;
            }
        });

        videosFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {
                Log.d("SEARCH", "Item clicked " + id);
                Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                intent.putExtra("VIDEO_ID", searchResults.get(pos).getId());
                startActivity(intent);
            }

        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout) {
            Log.d("MYTUBE", "Before logout"+session.isLoggedIn());
            session.logoutUser();
            finish();
            Log.d("MYTUBE", "After logout" + session.isLoggedIn());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void searchOnYoutube(final String keywords){
        new Thread(){
            public void run(){
                YoutubeConnector yc = new YoutubeConnector(SearchActivity.this);
                searchResults = yc.search(keywords);
                handler.post(new Runnable(){
                    public void run(){
                        updateVideosFound();
                    }
                });
            }
        }.start();
    }

    private void updateVideosFound(){
        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getApplicationContext(), R.layout.video_item, searchResults){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.video_item, parent, false);
                }
                ImageView thumbnail = (ImageView)convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView)convertView.findViewById(R.id.video_title);
                TextView viewCounts = (TextView)convertView.findViewById(R.id.view_count);
                TextView publishDate = (TextView)convertView.findViewById(R.id.publish_date);
                final ImageButton favoriteBtn = (ImageButton)convertView.findViewById(R.id.favorite);
                favoriteBtn.setTag(position);


                favoriteBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        int placePosition = (Integer)favoriteBtn.getTag();
                        Log.d("MYTUBE","Place Position"+placePosition);
                        if(!searchResults.get(placePosition).isFavorite()){
                            favoriteBtn.setImageResource(android.R.drawable.btn_star_big_on);
                            searchResults.get(placePosition).setIsFavorite(true);
                            Toast.makeText(getApplicationContext(),
                                    "Item added to favorites", Toast.LENGTH_SHORT).show();
                        }else{
                            favoriteBtn.setImageResource(android.R.drawable.btn_star_big_off);
                            searchResults.get(placePosition).setIsFavorite(false);
                            Toast.makeText(getApplicationContext(),
                                    "Item removed from favorites", Toast.LENGTH_SHORT).show();
                        }

                    }

                });

                VideoItem item = searchResults.get(position);
                item.setIsFavorite(false);

                Picasso.with(getApplicationContext()).load(item.getThumbnailURL()).into(thumbnail);
                title.setText(item.getTitle());
                viewCounts.setText("Views: 207");
                publishDate.setText(item.getPublishDate());
                //description.setText(searchResult.getDescription());
                return convertView;
            }
        };

        videosFound.setAdapter(adapter);
    }


}
