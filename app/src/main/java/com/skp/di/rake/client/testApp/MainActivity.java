package com.skp.di.rake.client.testApp;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.skp.di.rake.client.api.Rake;
import com.skp.di.rake.client.api.RakeFactory;
import com.skplanet.pdp.sentinel.shuttle.AppCrashLoggerSentinelShuttle;
import com.skplanet.pdp.sentinel.shuttle.AppSampleSentinelShuttle;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends ActionBarActivity {

    private Rake rake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initailize();
    }

    public void initailize() {
        rake = RakeFactory.getLogger(new RakeAppConfig(), getApplicationContext());

        setBtnTrack();
        setBtnFlush();
        setBtnRegisterSuperProps();
        setBtnClearSuperProps();
    }

    public void setBtnFlush() {
        Button btnFlush = (Button) findViewById(R.id.btnFlush);

        btnFlush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rake.flush();
            }
        });
    }

    public void setBtnRegisterSuperProps() {
        Button btnRegisterSuperProps = (Button) findViewById(R.id.btnRegisterSuperProperty);

        btnRegisterSuperProps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    JSONObject superProps = new JSONObject();
                    superProps.put("header1", "header1 super props");
                    superProps.put("field2", "field2 super props");
                    rake.registerSuperProperties(superProps);

                } catch (JSONException e) {
                    Log.e("RAKE", e.toString());
                }
            }
        });
    }

    public void setBtnClearSuperProps() {
        Button btnClearSuperProps = (Button) findViewById(R.id.btnClearSuperProperty);

        btnClearSuperProps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rake.clearSuperProperties();
            }
        });
    }

    public void setBtnTrack() {
        Button btnTrack = (Button) findViewById(R.id.btnTrack);
        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppSampleSentinelShuttle shuttle = new AppSampleSentinelShuttle();
                shuttle.header1("header1 value");
                shuttle.field4("field4 value");
                rake.track(shuttle.toJSONObject());

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
