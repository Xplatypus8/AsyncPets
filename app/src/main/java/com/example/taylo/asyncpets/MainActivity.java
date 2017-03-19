package com.example.taylo.asyncpets;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private CheckConnection connectStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        checkNetworkConnection();


    }

    public void checkNetworkConnection(){
        connectStatus = new CheckConnection(this);

        Toast.makeText(this, connectStatus.checkNetworkConnection()?"Network Reachable":"No Network",Toast.LENGTH_LONG).show();

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
}
