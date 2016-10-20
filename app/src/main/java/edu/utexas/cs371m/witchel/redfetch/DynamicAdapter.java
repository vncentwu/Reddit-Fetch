package edu.utexas.cs371m.witchel.redfetch;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by vncentwu on 10/19/2016.
 */

public class DynamicAdapter extends RecyclerView.Adapter<DynamicAdapter.DynamicViewHolder> implements URLFetch.Callback{


    public ArrayList<RedditRecord> listings = new ArrayList<>();
    public SwipeDetector swipeDetector;

    @Override
    public DynamicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.pic_text_row, parent, false);
        DynamicViewHolder vh = new DynamicViewHolder(v);
        return vh;

    }

    @Override
    public void onBindViewHolder(DynamicViewHolder holder, int position) {
        RedditRecord record = listings.get(position);
        holder.titleView.setText(record.title);
        holder.v.setTag(position);
        holder.v.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(swipeDetector.swipeDetected())
                {
                    Log.d("swiper no", "swiping");
                    listings.remove((int)v.getTag());
                    notifyDataSetChanged();
                }
                else{
                    Intent intent = new Intent(v.getContext(), OnePost.class);
                    Bundle extras = new Bundle();
                    extras.putString("title", listings.get((int)v.getTag()).title);
                    extras.putString("url", listings.get((int)v.getTag()).imageURL.toString());
                    intent.putExtras(extras);
                    v.getContext().startActivity(intent);
                }


            }
        });
        Bitmap bm =  BitmapCache.getInstance().getBitmap(record.thumbnailURL.toString());
        if(bm == null){
            URLFetch fetch = new URLFetch(this, record.thumbnailURL, false);
        }
        else{
            holder.picView.setImageBitmap(bm);
        }
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    @Override
    public void fetchStart() {

    }

    @Override
    public void fetchComplete(String result) {
        notifyDataSetChanged();
    }

    @Override
    public void fetchCancel(String url) {

    }

    public class DynamicViewHolder extends RecyclerView.ViewHolder {

        TextView titleView;
       ImageView picView;
        View v;

        public DynamicViewHolder(View itemView) {
            super(itemView);
            v = itemView;
            titleView = (TextView) itemView.findViewById(R.id.picTextRowText);
            picView = (ImageView) itemView.findViewById(R.id.picTextRowPic);
        }
    }
}
