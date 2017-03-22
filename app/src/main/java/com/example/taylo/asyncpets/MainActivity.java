package com.example.taylo.asyncpets;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ParseJSON";
    private static final String MYURL = "http://www.tetonsoftware.com/pets/pets.json";

    public static final int MAX_LINES = 15;
    private static final int SPACES_TO_INDENT_FOR_EACH_LEVEL_OF_NESTING = 2;

    private String tvRaw;
    private String name;
    private String file;
    JSONArray jsonArray;

    int numberentries = 0;
    int currententry = 0;

    private CheckConnection connectStatus;
    ATask myTask;
    String [] items;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        checkNetworkConnection();
        processJSON((MYURL));

        items = new String[numberentries];
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.JSON_URL_NAME, android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);


    }

    public void checkNetworkConnection(){
        connectStatus = new CheckConnection(this);

        if(connectStatus.checkNetworkConnection()) {
            Toast.makeText(this,"Network Reachable", Toast.LENGTH_LONG).show();
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

    public void checkWifi(View view){
        String res =connectStatus.isWifiReachable()?"WiFi Reachable":"No WiFi";
        Toast.makeText(this, res,Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
      getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }


    public void processJSON(String string) {
        try {
            JSONObject jsonobject = new JSONObject(string);

            //*********************************
            //makes JSON indented, easier to read
            Log.d(TAG, jsonobject.toString(SPACES_TO_INDENT_FOR_EACH_LEVEL_OF_NESTING));
            tvRaw = (jsonobject.toString(SPACES_TO_INDENT_FOR_EACH_LEVEL_OF_NESTING));

            // you must know what the data format is, a bit brittle
            jsonArray = jsonobject.getJSONArray("people");

            // how many entries
            numberentries = jsonArray.length();

            currententry = 0;
            setJSONUI(currententry); // parse out object currententry

            Log.i(TAG, "Number of entries " + numberentries);

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
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            name = jsonObject.getString("name");
            file = (jsonObject.getString("file"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void doLeft(View v) {
        if (numberentries != -1 && currententry != 0) {
            currententry--;
            setJSONUI(currententry);
        }
    }

    public void doRight(View v) {
        if (numberentries != -1 && currententry != numberentries) {
            currententry++;
            setJSONUI(currententry);
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







    private static class ATask extends AsyncTask<String, Void, View>{


        MainActivity myActivity;

        public ATask(MainActivity activity){
            assaignActivity(activity);
        }

        @Override
        protected View doInBackground(String... params) {
            return null;
        }


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
    }
}


