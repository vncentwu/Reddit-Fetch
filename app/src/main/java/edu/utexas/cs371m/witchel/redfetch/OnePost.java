package edu.utexas.cs371m.witchel.redfetch;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by vncentwu on 10/20/2016.
 */

public class OnePost extends AppCompatActivity implements URLFetch.Callback{


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.one_post_layout);
        Bundle extras = getIntent().getExtras();
        String title = extras.getString("title");
        String url = extras.getString("url");
        ((TextView)findViewById(R.id.one_text)).setText(title);
        Bitmap bm = BitmapCache.getInstance().getBitmap(url);
        if(bm == null){
            try {
                URLFetch fetch = new URLFetch(this, new URL(url), true);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        else{
            ((ImageView)findViewById(R.id.one_image)).setImageBitmap(bm);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }

    @Override
    public void fetchStart() {

    }

    @Override
    public void fetchComplete(String result) {
        Bitmap bm = BitmapCache.getInstance().getBitmap(result.toString());
        if(bm != null){
            ((ImageView)findViewById(R.id.one_image)).setImageBitmap(bm);
        }

    }

    @Override
    public void fetchCancel(String url) {

    }
}
