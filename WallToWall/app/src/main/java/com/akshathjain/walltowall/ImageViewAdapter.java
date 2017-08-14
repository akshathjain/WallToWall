package com.akshathjain.walltowall;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImageViewAdapter extends RecyclerView.Adapter<ImageViewAdapter.ViewHolder> {
    private Context context;
    private String rootURL;
    private JSONArray imageData;
    private RecyclerViewItemClick viewClickReference;

    public ImageViewAdapter(Context context, JSONObject data) {
        this.context = context;

        try {
            this.rootURL = data.getString("rootPath");
            this.imageData = data.getJSONArray("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageOne;

        public ViewHolder(View itemView) {
            super(itemView);
            imageOne = (ImageView) itemView.findViewById(R.id.image_view_image1);

            //add an onclick listener for view
            itemView.setOnClickListener(this);
        }

        @Override
        //this onclick listener will trigger the reference interface that the implementing class will have to implement
        public void onClick(View v) {
            if (viewClickReference != null)
                viewClickReference.onClick(getAdapterPosition());
        }
    }

    @Override
    public ImageViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_image_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ImageViewAdapter.ViewHolder holder, int position) {
        try {
            String fullURL = rootURL + imageData.getJSONObject(position).getString("name");
            Glide.with(context).load(fullURL).into(holder.imageOne);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return imageData.length();
    }

    public void addRecyclerViewItemClickListener(RecyclerViewItemClick reference) {
        this.viewClickReference = reference;
    }

    public interface RecyclerViewItemClick {
        public void onClick(int position);
    }

}