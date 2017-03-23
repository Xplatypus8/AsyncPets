package com.example.taylo.asyncpets;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.util.prefs.PreferenceChangeListener;

import static com.example.taylo.asyncpets.R.id.imageView;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ParseJSON";
    private String MYURL;

    public static final int MAX_LINES = 15;
    private static final int SPACES_TO_INDENT_FOR_EACH_LEVEL_OF_NESTING = 2;
    private  Spinner spinner;
    private SharedPreferences myPreference;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private String tvRaw;
    private String name;

    private Bitmap image;

    private String file;
    private ImageView background;
    JSONArray jsonArray;
    JSONArray array2;
    List<Pet> petList= new ArrayList<Pet>();

    int numberentries = 0;
    int currententry = 0;

    private CheckConnection connectStatus;
    ATask myTask;
    String [] items;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myPreference = PreferenceManager.getDefaultSharedPreferences(this);


        listener = new SharedPreferences.OnSharedPreferenceChangeListener(){
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key){
                if(key.equals("listpref")){
                    runATask();

                }
            }
        };
        myPreference.registerOnSharedPreferenceChangeListener(listener);

        background = (ImageView)findViewById(R.id.imageView);
        Toolbar myToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        myToolbar.setAlpha((float).7);
        setSupportActionBar(myToolbar);


        checkNetworkConnection();

       // Toast.makeText(this, petList.get(0).getName(), Toast.LENGTH_LONG).show();
        populateSpinner();


    }


    public void checkNetworkConnection(){
        connectStatus = new CheckConnection(this);

        if(connectStatus.checkNetworkConnection()) {
           // Toast.makeText(this,"Network Reachable", Toast.LENGTH_LONG).show();


            runATask();

        }
        else{
            doAlert();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case(R.id.settings):
                Intent myIntent = new Intent(this, PrefActivity.class);
                startActivity(myIntent);
                break;
            default:
                break;

        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
      getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }


    public void processJSON(String string) {
        //Toast.makeText(this, string ,Toast.LENGTH_LONG).show();
        try {
            if(string == null) {
                serverNotFound();
                return;
            }
            JSONObject jsonobject = new JSONObject(string);

            //*********************************
            //makes JSON indented, easier to read
            Log.d(TAG, jsonobject.toString(SPACES_TO_INDENT_FOR_EACH_LEVEL_OF_NESTING));
            tvRaw = (jsonobject.toString(SPACES_TO_INDENT_FOR_EACH_LEVEL_OF_NESTING));

            // you must know what the data format is, a bit brittle
            jsonArray = jsonobject.getJSONArray("pets");




            // how many entries
            numberentries = jsonArray.length();

            currententry = 0;
            setJSONUI(currententry); // parse out object currententry

            Log.i(TAG, "Number of entries " + numberentries);

            populateSpinner();
            //Toast.makeText(this,petList.get(0).getLink(),Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param i
     *            find the object i in the member var jsonArray get the
     *            firstname and lastname and set the appropriate UI elements
     */
    private void setJSONUI(int i) {
        if (jsonArray == null) {
            Log.e(TAG, "tried to dereference null jsonArray");
            return;
        }

        // gotta wrap JSON in try catches cause it throws an exception if you
        // try to
        // get a value that does not exist
        try {
            if(petList.size() > 0){
                petList.clear();
            }

            for(int j = 0; j < numberentries; j++) {
                JSONObject jsonObject = jsonArray.getJSONObject(j);
                Pet p = new Pet(jsonObject.getString("name"), jsonObject.getString("file"));
                petList.add(p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    private void doAlert(){
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

    public void populateSpinner(){


        items = new String[petList.size()];
        if(numberentries > 0) {
            for (int i = 0; i < petList.size(); i++) {

                items[i] = petList.get(i).getName();
            }
        }
          // Toast.makeText(this,items[1], Toast.LENGTH_LONG).show();
        spinner = (Spinner)findViewById(R.id.spinner);

        spinner.setVisibility(View.VISIBLE);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
                android.R.layout.simple_spinner_item,items);// ArrayAdapter.createFromResource(this, R.array.JSON_URL_NAME, android.R.layout.simple_spinner_dropdown_item);


        // adapter.add("j");
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);



    }

    public void runATask(){
        if(myTask != null){
            myTask.detach();
            myTask= null;
        }

        myTask = new ATask(this);

        myTask.setnameValuePair("name", "file");
        //Toast.makeText(this,myPreference.getString("listpref","http://www.tetonsoftware.com/pets/pets.json"),Toast.LENGTH_SHORT).show();

        myTask.execute(myPreference.getString("listpref","http://www.tetonsoftware.com/pets/pets.json"));
    }

    public void serverNotFound(){
        Toast.makeText(this, "Error trying to reach: " + myPreference.getString("listpref","http://www.tetonsoftware.com/pets/pets.json"), Toast.LENGTH_LONG).show();
        spinner.setVisibility(View.INVISIBLE);
    }

    public void setBackground(Bitmap b){

    }




    private static class ATask extends AsyncTask<String, Void, String>{

        private static final int TIMEOUT = 1000;
        private static final int READ_THIS_AMOUNT = 8096;
        private String myQuery = "";
        MainActivity myActivity;

        public ATask(MainActivity activity){
            assaignActivity(activity);
        }

        @Override
        protected String doInBackground(String... params) {

            String myURL = params[0];

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
                    int statusCode = connection.getResponseCode();
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
                return null;
            }
        }

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

        private Bitmap downloadImage(String string){
            Bitmap background = null;
            try {
                URL aURL = new URL(string);
                URLConnection connect = aURL.openConnection();
                connect.connect();
                InputStream input = connect.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(input);
                background = BitmapFactory.decodeStream(bufferedInputStream);
                bufferedInputStream.close();
                input.close();

            }
            catch(IOException e){

            }
            return background;
        }

        @Override
        protected void onPostExecute(String result) {

            if (myActivity != null) {
                myActivity.processJSON(result);

            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
        void detach() {
            myActivity = null;
        }

        private void assaignActivity(MainActivity activity){
            this.myActivity = activity;
        }


        public void loadJSON(URL url){



        }
    }


    private class downloadTask extends AsyncTask<ImageView, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(ImageView... params) {
            return null;
        }
    }
}


