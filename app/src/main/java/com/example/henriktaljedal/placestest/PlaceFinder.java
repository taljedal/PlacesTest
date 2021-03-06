package com.example.henriktaljedal.placestest;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by henriktaljedal on 2015-05-04.
 */
public class PlaceFinder {
    ArrayList<WPlace> foundPlaces;


    public DownloadWebpage getPlaces(){
        String s = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=57.6932533,11.9758226" +
                "&radius=5000&types=bar&sensor=true&key=AIzaSyDtYpMpKbapO5YkwHO5h265jccWsiYUx58";

        DownloadWebpage dwt = new DownloadWebpage();
        dwt.execute(s);
        return dwt;
    }



    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    public class DownloadWebpage extends AsyncTask<String, Void, String> {
        public AsyncResponse delegate=null;
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            JSONObject jObject;
            WPlace place;

            try{
                jObject = new JSONObject(result);

                /** Getting the parsed data as a List construct */
                place = parse(jObject);

                delegate.processFinish(place);

            }catch(Exception e){
                Log.d("Exception", e.toString());
            }
        }
    }
    // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a string.
    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;


        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("HTTP_Example", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        StringBuilder jsonResults = new StringBuilder();
        int readSize;
        reader = new InputStreamReader(stream, "UTF-8");

        char[] buff = new char[1024];
        while ((readSize = reader.read(buff)) != -1) {
            jsonResults.append(buff, 0, readSize);
        }


        return jsonResults.toString();
    }
    /** Receives a JSONObject and returns an instance of a WPlace object */
    public WPlace parse(JSONObject jObject){

        ArrayList<WPlace> placesList = new ArrayList<WPlace>();
        WPlace place, chosenPlace;

        JSONArray jPlaces = null;
        try {
            /** Retrieves all the elements in the 'places' array */
            jPlaces = jObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int placesCount = jPlaces.length();

        /** Taking each place, parses and adds to list object */
        for(int i=0; i<placesCount;i++){
            try {
                /** Call getPlace with place JSON object to parse the place */
                place = getPlace((JSONObject)jPlaces.get(i));
                placesList.add(place);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Random rand = new Random();
        int  i = rand.nextInt(placesList.size()-1);
        chosenPlace = placesList.get(i);
        return chosenPlace;
    }

    /** Parsing the WPlace JSON object */
    private WPlace getPlace(JSONObject jPlace){

        WPlace place = new WPlace();

        try {
            // Extracting WPlace Reference, if available
            if(!jPlace.isNull("reference")){
                place.reference = jPlace.getString("reference");
            }
            // Extracting WPlace name, if available
            if(!jPlace.isNull("name")){
                place.name = jPlace.getString("name");
            }

            // Extracting WPlace Vicinity, if available
            if(!jPlace.isNull("vicinity")){
                place.address = jPlace.getString("vicinity");
            }

            // Extracting WPlace Rating, if available
            if(!jPlace.isNull("rating")){
                place.rating = jPlace.getString("rating");
            }

            // Extracting WPlace Phone, if available
            if(!jPlace.isNull("phone")){
                place.phone = jPlace.getString("phone");
            }

            // Extracting WPlace isOpen, if available
            if(!jPlace.isNull("open_now")){
                place.isOpen = jPlace.getBoolean("open_now");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }
}
