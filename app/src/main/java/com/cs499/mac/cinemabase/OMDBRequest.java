package com.cs499.mac.cinemabase;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

public class OMDBRequest {

    private final String TAG = "MyTag";

    private Context context;
    private final String SERVER_URL = "http://www.omdbapi.com/?";
    private final String PLOT_LENGTH = "full";//for short plot replace with "short"
    private final String RESPONSE_TYPE = "json";
    private String movieTitle;
    private RequestQueue queue;
    private JsonObjectRequest jsObjRequest;
    private StringRequest moviePrevs;

    /**
     * Constructor for ombd request.
     * @param context from main activity
     */
    public OMDBRequest(Context context){
        this.context  = context;
        queue = Volley.newRequestQueue(context);
        movieTitle = "";
    }

    /**
     * @param title of movie being searched
     */
    public void requestMovie(String title, final boolean moviePreviews){
        movieTitle = title;
        String url = constructURL();
        final JSONObject jsObj = new JSONObject();

        final ProgressDialog mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Searching for movie...");
        mProgressDialog.show();

        jsObjRequest = new JsonObjectRequest(Request.Method.GET,
                url, jsObj,
                new Response.Listener<JSONObject>(){

                    @Override
                    public void onResponse(final JSONObject response){
                        Log.d(TAG, "json obj " + jsObj.toString());
                        Log.d(TAG, "RESPONSE: " + response);

                        if(moviePreviews){
                            String xmlHardURL = "http://www.androidbegin.com/tutorial/AndroidCommercial.3gp";
                            moviePrevs = new StringRequest(Request.Method.GET, xmlHardURL,
                                    new Response.Listener<String>()
                                    {
                                        @Override
                                        public void onResponse(String xmlResp) {
                                            Log.d(TAG,"xml " + xmlResp);
                                            parseObject(response, xmlResp);
                                            mProgressDialog.dismiss();
                                        }
                                    },
                                    new Response.ErrorListener()
                                    {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.d(TAG,"error " + error.getLocalizedMessage());
                                            mProgressDialog.dismiss();
                                        }
                                    }
                            );
                            queue.add(moviePrevs);
                        } else {
                            parseObject(response, null);
                            mProgressDialog.dismiss();
                        }
                    }
                },
        new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Log.d(TAG, "Error " + error);
                Log.d(TAG, "Network Response: " + error.networkResponse.statusCode);
                Log.d(TAG, "Localized Message: " + error.networkResponse.data.toString());
                mProgressDialog.dismiss();
            }
        });

        queue.add(jsObjRequest);

    }

    private void parseObject(JSONObject jsObj, String xml){

        //create movie and attempt to parse
        Movie movie = new Movie(jsObj);
        boolean validMovie = movie.parse();

        //if json returned false display message and return
        if(!validMovie){
            Log.d(TAG, "Invalid movie");
            Toast.makeText(context,"Movie was not found",Toast.LENGTH_SHORT).show();
            return;
        }

        if(xml != null){
            movie.parseXML(xml);
        }

        Log.d(TAG,"preview url " + movie.getTrailerURL());

        Log.d(TAG, "Initializing movie activity");
        Intent intent = new Intent(context,RequestedMovie.class);
        intent.putExtra("movie",movie);
        context.startActivity(intent);

    }

    private String constructURL(){
        movieTitle = movieTitle.replace(" ", "+");
        String url =  SERVER_URL + "t=" + movieTitle +
                "&y=&plot=" + PLOT_LENGTH + "&r=" + RESPONSE_TYPE;
        Log.d(TAG, "Constructed request url: " + url);
        return url;
    }

}