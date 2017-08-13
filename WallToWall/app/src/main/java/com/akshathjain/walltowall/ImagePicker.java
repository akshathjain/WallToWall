package com.akshathjain.walltowall;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class ImagePicker extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        JSONRetriever retriever = new JSONRetriever(new AsyncJSONRetrieved() {
            @Override
            public void jsonRetrieved(JSONArray jsonArray) {
                System.out.println(jsonArray);
            }
        });
        retriever.execute("http://akshathjain.com/WallToWall/json/directory.json");
    }

    //this class will retrieve the json on a background thread
    class JSONRetriever extends AsyncTask<String, Void, JSONArray>{
        private ProgressDialog loader;
        private AsyncJSONRetrieved reference;

        public JSONRetriever(AsyncJSONRetrieved reference){
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
        protected JSONArray doInBackground(String... jsonUrl) {
            String result = null;

            try{
                //set up a HTTP request
                URL url = new URL(jsonUrl[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                Scanner scanner = new Scanner(connection.getInputStream());

                result = new String();while(scanner.hasNext())
                    result += scanner.nextLine();

                return new JSONArray(result);
            }catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            super.onPostExecute(jsonArray);
            loader.dismiss(); //dismiss the loader dialog
            reference.jsonRetrieved(jsonArray); //uses the AsyncJSONRetrieved interface to call the json received method in the main class
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

    }
}



