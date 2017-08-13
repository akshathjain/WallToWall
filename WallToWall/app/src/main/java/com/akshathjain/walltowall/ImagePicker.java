package com.akshathjain.walltowall;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ImagePicker extends AppCompatActivity {
    private RecyclerView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        imageView = (RecyclerView) findViewById(R.id.recylcer_view_image_view);
        imageView.setLayoutManager(new LinearLayoutManager(this));

        JSONRetriever retriever = new JSONRetriever();
        retriever.addAsyncFinishedListener(new AsyncFinished<JSONObject>() {
            @Override
            public void onAsyncFinished(JSONObject o) {
                System.out.println(o);
                imageView.setAdapter(new ImageViewAdapter(ImagePicker.this, o));
            }
        });
        retriever.execute("http://akshathjain.com/WallToWall/json/directory.json");
    }

    //this class will retrieve the json on a background thread
    class JSONRetriever extends AsyncTask<String, Void, JSONObject> {
        private ProgressDialog loader;
        private AsyncFinished reference;

        //adds an interface that is called when task is finished executing
        public void addAsyncFinishedListener(AsyncFinished reference) {
            this.reference = reference;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //set up a loader dialog
            loader = new ProgressDialog(ImagePicker.this);
            loader.setMessage("Retrieving wallpaper data...");
            loader.setCancelable(false);
            loader.show();
        }

        @Override
        protected JSONObject doInBackground(String... jsonUrl) {
            String result = null;

            try {
                //set up a HTTP request
                URL url = new URL(jsonUrl[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                Scanner scanner = new Scanner(connection.getInputStream());

                result = new String();
                while (scanner.hasNext())
                    result += scanner.nextLine();

                return new JSONObject(result);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject data) {
            super.onPostExecute(data);
            loader.dismiss(); //dismiss the loader dialog
            reference.onAsyncFinished(data); //uses the AsyncJSONRetrieved interface to call the json received method in the main class
        }
    }

}

class ImageViewAdapter extends RecyclerView.Adapter<ImageViewAdapter.ViewHolder>{
    private Context context;
    private String rootURL;
    private JSONArray imageData;

    public ImageViewAdapter(Context context, JSONObject data){
        this.context = context;

        try {
            this.rootURL = data.getString("rootPath");
            this.imageData = data.getJSONArray("data");
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageOne;

        public ViewHolder(View itemView) {
            super(itemView);
            imageOne = (ImageView) itemView.findViewById(R.id.image_view_image1);
        }
    }

    @Override
    public ImageViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_image_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ImageViewAdapter.ViewHolder holder, int position) {
       try{
           String fullURL = rootURL + imageData.getJSONObject(position).getString("name");
           Glide.with(context).load(fullURL).into(holder.imageOne);
       }catch (JSONException e){
           e.printStackTrace();
       }
    }

    @Override
    public int getItemCount() {
        return imageData.length();
    }
}



