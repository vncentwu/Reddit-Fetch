package edu.utexas.cs371m.witchel.redfetch;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements URLFetch.Callback {
    static public String AppName = "RedFetch";
    private ProgressBar progressBar;
    // Reddit json search will return up to 100 records, 25 by default
    // Only display a subset of those returned
    protected final int maxRedditRecords = 100;
    protected DynamicAdapter redditRecordAdapter = null;
    protected LinearLayoutManager rv_layout_mgr;


    @Override
    public void fetchStart() {
    }

    @Override
    public void fetchComplete(String result) {
        Log.d("wfwef", "FETCH COMPLETE");
        ArrayList<RedditRecord> records = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(result);
            JSONArray jsonArray = json.getJSONObject("data").getJSONArray("children");
            int index = 0;
            while(index < jsonArray.length()){
                json = jsonArray.getJSONObject(index);
                if(json.isNull("data")){
                    index++;
                    continue;
                }
                json = json.getJSONObject("data");
                if(json.isNull("thumbnail")
                        || (!json.getString("thumbnail").endsWith("jpg") && ! json.getString("thumbnail").endsWith("png"))
                        || json.isNull("url")
                        || (!json.getString("url").endsWith("jpg") && !json.getString("url").endsWith("png"))
                        || json.isNull("title")){
                    index++;
                    continue;
                }
                String title = json.getString("title");
                String thumbnailURL = json.getString("thumbnail");
                String imageURL = json.getString("url");
                RedditRecord record = new RedditRecord();
                record.title = title;
                try {
                    record.thumbnailURL = new URL(thumbnailURL);
                    record.imageURL = new URL(imageURL);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                records.add(record);
                index++;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        redditRecordAdapter.listings = records;
        redditRecordAdapter.notifyDataSetChanged();
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        Log.d("ewfwef", "data set changed");



    }

    @Override
    public void fetchCancel(String url) {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // XXX write me: call setContentView

        setContentView(R.layout.search_results);

        // XXX other initialization.
        progressBar = (ProgressBar) findViewById(R.id.netIndicator);
        progressBar.setIndeterminate(true);

        RecyclerView rv = (RecyclerView)findViewById(R.id.searchResults);
        rv_layout_mgr = new LinearLayoutManager(this);
        rv.setLayoutManager(rv_layout_mgr);

        redditRecordAdapter = new DynamicAdapter();
        rv.setAdapter(redditRecordAdapter);
        SwipeDetector swipeDetector = new SwipeDetector();
        rv.addOnItemTouchListener(swipeDetector);
        redditRecordAdapter.swipeDetector = swipeDetector;

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/4th of the available memory for this memory cache.
        BitmapCache.cacheSize = maxMemory / 4;
        // Get the size of the display so we properly size bitmaps
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        BitmapCache.maxW = size.x;
        BitmapCache.maxH = size.y;

        // Listen for the enter key
        EditText editText = (EditText) findViewById(R.id.searchTerm);
        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            newSearch();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
    }

    // https://www.reddit.com/dev/api#GET_search
    protected void newSearch() {

        EditText editText = (EditText) findViewById(R.id.searchTerm);
        String term = editText.getText().toString();
        if(term.contains("&"))
        {
            redditRecordAdapter.listings.clear();
            redditRecordAdapter.notifyDataSetChanged();
            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);
        String myString = "";

        myString = "/r/aww/search.json?q=" + term + "&sort=hot&limit=100";
        URL searchURL = null;
        try {
            searchURL = new URL("https", "www.reddit.com", myString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.d("new ur; stufwef", myString);
        URLFetch fetch = new URLFetch(this, searchURL, false);



        // XXX write me.
        // Hint, to search reddit, read the above URL.  You will construct a string like the following
        // one and substitute your search term for the %s.  This is pseudo-code.
        // myString = "/r/aww/search.json?q=%s&sort=hot&limit=100"
        // Then you will construct a URL from your string
        // URL searchURL = new URL("https", "www.reddit.com", myString);
    }

    // You will need to parse JSON
    // https://github.com/reddit/reddit/wiki/JSON

    // Finally, enjoy these simple functions
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_get_search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.action_settings :
                return true;
            case R.id.exit:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
