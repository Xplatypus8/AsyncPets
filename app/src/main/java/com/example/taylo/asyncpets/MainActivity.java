package com.example.taylo.asyncpets;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Clifton Dent and Kyle Hoobler
 * This class is the main component of our application and it calls other async tasks to handle a json file, as well as builds the app envirionment
 *
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ParseJSON";

    public static final int MAX_LINES = 15;
    private static final int SPACES_TO_INDENT_FOR_EACH_LEVEL_OF_NESTING = 2;
    private  Spinner spinner;
    private SharedPreferences myPreference;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private String tvRaw;
    private DownloadTask downloadImage;
    private ImageView background;
    private JSONArray jsonArray;
    private List<Pet> petList= new ArrayList<Pet>();
    int numberentries = 0;
    private CheckConnection connectStatus;
    private ATask myTask;
    private String [] items;

    /**
     * Creates app and sets all the parts in motion
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //calls main activity.xml
        setContentView(R.layout.activity_main);

        //Pref manager
        myPreference = PreferenceManager.getDefaultSharedPreferences(this);

        //On Update prefrences
        listener = new SharedPreferences.OnSharedPreferenceChangeListener(){
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key){
                if(key.equals("listpref")){
                    runATask();

                }
            }
        };
        //updates when prefrences change
        myPreference.registerOnSharedPreferenceChangeListener(listener);


        //Sets up background
        background = (ImageView)findViewById(R.id.imageView);

        //Toolbar info
        Toolbar myToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        myToolbar.setAlpha((float).7);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        myToolbar.setTitle("");
        myToolbar.setSubtitle("");

        //Runs the network check
        checkNetworkConnection();
        //Adds items to spinner
        populateSpinner();

        //loads first image in task if it has one
        if(petList.size() > 0) {
            loadImage(petList.get(0).getLink());
        }


    }


    /**
     * Checks network status of phone
     */
    public void checkNetworkConnection(){

        //status of connection
        connectStatus = new CheckConnection(this);

        //tests if phone has network
        if(connectStatus.checkNetworkConnection()) {
            //starts async task
            runATask();
        }
        else{
            //calls notification that phone isn't properly connected
            doAlert();
        }
    }

    /**
     * Opens settings
     *
     * @param item the settings menu item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //switches between the options
        switch(item.getItemId()){
            case(R.id.settings):
                //starts new preference activity
                Intent myIntent = new Intent(this, PrefActivity.class);
                startActivity(myIntent);
                break;
            default:
                break;

        }
        return true;
    }

    /**
     * opens option menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        //inflates menu
      getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }


    /**
     * parses json and converts the recieved information into a readable format inside of a pet object
     * @param string URL
     */
    public void processJSON(String string) {
        try {
            JSONObject jsonobject = new JSONObject(string);

            //*********************************
            //makes JSON indented, easier to read
            Log.d(TAG, jsonobject.toString(SPACES_TO_INDENT_FOR_EACH_LEVEL_OF_NESTING));
            tvRaw = (jsonobject.toString(SPACES_TO_INDENT_FOR_EACH_LEVEL_OF_NESTING));

            // you must know what the data format is, a bit brittle
            jsonArray = jsonobject.getJSONArray("pets");




            // how many entries
            numberentries = jsonArray.length();

            //puts into pet arraylist
            setJSONUI(); // parse out object currententry

            //adds new values to spinner
            populateSpinner();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void setJSONUI() {
        //check if sent null
        if (jsonArray == null) {
            return;
        }

        // gotta wrap JSON in try catches cause it throws an exception if you
        // try to
        // get a value that does not exist
        try {
            if(petList.size() > 0){
                petList.clear();
            }

            //populate array list with parsed json values
            for(int j = 0; j < numberentries; j++) {
                JSONObject jsonObject = jsonArray.getJSONObject(j);
                Pet p = new Pet(jsonObject.getString("name"), "http://www.tetonsoftware.com/pets/" +jsonObject.getString("file"));
                petList.add(p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Creates a dialog describing network error/lack of json file
     */
    private void doAlert(){
        //makes dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("The Network is unavailable. Please try your request again later.");
        builder.setTitle("No Network Connection");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Populates the spinner
     */
    public void populateSpinner(){

        //converts the pet arraylist to a string array so we can populate the spinner
        items = new String[petList.size()];

        //ensure not empty
        if(numberentries > 0) {
            //populates new string array
            for (int i = 0; i < petList.size(); i++) {

                items[i] = petList.get(i).getName();
            }
        }
          //sets spinner
        spinner = (Spinner)findViewById(R.id.spinner);

        //spinner listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            /**
             * gets item selected
             * @param parent
             * @param view
             * @param position
             * @param id
             */
            @Override
            public void onItemSelected(AdapterView parent, View view, int position, long id) {
                //loads image
                loadImage(petList.get(position).getLink());
            }

            /**
             * in case no option is selected
             * @param parent
             */
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Makes spinner visible
        spinner.setVisibility(View.VISIBLE);

        //Array Adapter to hold names
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, R.layout.spinner_item,items);

        //sets dropdown list to our xml file
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);

    }

    /**
     * Loads image from a URL
     *
     * @param url
     */
    public void loadImage(String url){

        if(downloadImage != null){
            //resets the image
            downloadImage.detach();
            downloadImage= null;
        }

        //downloads new image
        downloadImage = new DownloadTask(this);
        downloadImage.execute(url);

    }

    /**
     * Runs our new Async task to get json info
     */
    public void runATask(){
        //if task is not new
        if(myTask != null){
            myTask.detach();
            myTask= null;
        }

        myTask = new ATask(this);

        //sets values to search for
        myTask.setnameValuePair("name", "file");

        //execuates async task
        myTask.execute(myPreference.getString("listpref","http://www.tetonsoftware.com/pets/pets.json"));
    }

    /**
     * if server is not found
     * @param errorCode
     */
    public void serverNotFound(String errorCode){

        //displays network connection error to user
        Toast.makeText(this, "Error trying to reach: " + myPreference.getString("listpref","http://www.tetonsoftware.com/pets/pets.json") +". Server returned " + errorCode, Toast.LENGTH_LONG).show();
        //sets spinner to be invisible
        spinner.setVisibility(View.INVISIBLE);
    }

    /**
     * sets background image
     * @param b
     */
    public void setBackground(Bitmap b){
        background.setImageBitmap(b);
    }


    /**
     * New Async task class to download info
     */
    private static class ATask extends AsyncTask<String, Void, String>{

        private static final int TIMEOUT = 1000;
        private static final int READ_THIS_AMOUNT = 8096;
        private String myQuery = "";
        private MainActivity myActivity;

        public ATask(MainActivity activity){
            assaignActivity(activity);
        }

        /**
         * Does async task and returns results
         * @param params
         * @return
         */
        @Override
        protected String doInBackground(String... params) {

            //gets URL from params list
            String myURL = params[0];
            int statusCode = 0;
            try {
                URL url = new URL(myURL + myQuery);

                // this does no network IO
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // can further configure connection before getting data
                // cannot do this after connected
                connection.setRequestMethod("GET");
                connection.setReadTimeout(TIMEOUT);
                connection.setConnectTimeout(TIMEOUT);
                connection.setRequestProperty("Accept-Charset", "UTF-8");
                // this opens a connection, then sends GET & headers

                // wrap in finally so that stream bis is sure to close
                // and we disconnect the HttpURLConnection
                BufferedReader in = null;

                try {
                    connection.connect();

                    // lets see what we got make sure its one of
                    // the 200 codes (there can be 100 of them
                    // http_status / 100 != 2 does integer div any 200 code will = 2
                    statusCode = connection.getResponseCode();
                    if (statusCode / 100 != 2) {
                        Log.e(TAG, "Error-connection.getResponseCode returned "
                                + Integer.toString(statusCode));
                        return null;
                    }

                    in = new BufferedReader(new InputStreamReader(connection.getInputStream()), READ_THIS_AMOUNT);

                    // the following buffer will grow as needed
                    String myData;
                    StringBuffer sb = new StringBuffer();

                    while ((myData = in.readLine()) != null) {
                        sb.append(myData);
                    }
                    return sb.toString();

                } finally {
                    // close resource no matter what exception occurs
                    in.close();
                    connection.disconnect();
                }
            } catch (Exception exc) {
                exc.printStackTrace();
                return ("*" +statusCode);
            }
        }

        /**
         * Sets the value pair
         *
         * @param name
         * @param value
         * @return
         */
        public ATask setnameValuePair(String name, String value) {
            try {
                if (name.length() != 0 && value.length() != 0) {

                    // if 1st pair that include ? otherwise use the joiner char &
                    if (myQuery.length() == 0)
                        myQuery += "?";
                    else
                        myQuery += "&";

                    myQuery += name + "=" + URLEncoder.encode(value, "utf-8");
                }
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return this;
        }


        /**
         * runs post async task
         * @param result
         */
        @Override
        protected void onPostExecute(String result) {

            if (myActivity != null) {

                //if result contains a * it contains an error
                if(result.substring(0,1).equals("*")){
                    //gets error code after *
                    myActivity.serverNotFound(result.substring(1, result.length()));
                }
                else{
                    //proceed as normal
                    myActivity.processJSON(result);
                }


            }
        }

        /**
         * If user cancels task
         */
        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
        //stops async task
        void detach() {
            myActivity = null;
        }
        private void assaignActivity(MainActivity activity){
            this.myActivity = activity;
        }

    }


    /**
     * Async task that downloads image
     */
    private class DownloadTask extends AsyncTask<String, Void, Bitmap>{
        private MainActivity activity;

        /**
         * Constructor
         * @param activity
         */
       public DownloadTask(MainActivity activity){
            this.activity = activity;
        }

        /**
         * Downloads the image
         * @param params
         * @return
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            //empty tmp background
            Bitmap background = null;
            try {
                //Handles URL and connection
                URL aURL = new URL(params[0]);
                URLConnection connect = aURL.openConnection();
                connect.connect();
                InputStream input = connect.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(input);
                //Gets image
                background = BitmapFactory.decodeStream(bufferedInputStream);
                //closes connection
                bufferedInputStream.close();
                input.close();

            }
            catch(IOException e){

            }
            //returns the image
            return background;
        }

        /**
         * Runs after onBackground
         * @param result
         */
        @Override
        protected void onPostExecute(Bitmap result) {
            //Sets background to downloaded iamge
            if (activity != null) {
                activity.setBackground(result);

            }
        }

        /**
         * When user cancels task
         */
        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
        void detach() {
            activity = null;
        }
    }
}
