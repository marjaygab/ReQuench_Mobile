package com.cpe.requench;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Home_Activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private DrawerLayout drawer;
    private TextView fullname,email;
    private NavigationView navigationView;
    private String Acc_ID,Access_Level,image_string;
    private ImageView profile_image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer =findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fullname = navigationView.getHeaderView(0).findViewById(R.id.fullname);
        email = navigationView.getHeaderView(0).findViewById(R.id.email);
        profile_image = navigationView.getHeaderView(0).findViewById(R.id.profile_image);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        if(savedInstanceState == null){
            Bundle bundle = new Bundle();
            String intent_value = fetchIntentValue();

            bundle.putString("JSON_Response",intent_value);
            Log.i("Intent Value",fetchIntentValue());

            try {

                JSONObject intent_object = new JSONObject(intent_value);
                JSONObject account_details = intent_object.getJSONObject("Account_Details");
                Log.i("Fullname",account_details.getString("First_Name") + " " + account_details.getString("Last_Name"));
                Acc_ID = account_details.getString("Acc_ID");
                Access_Level = account_details.getString("Access_Level");
                fullname.setText(account_details.getString("First_Name") + " " + account_details.getString("Last_Name"));
                email.setText(account_details.getString("Email"));
                image_string = intent_object.getString("image");
                byte[] decodedString = Base64.decode(image_string, Base64.DEFAULT);
                Bitmap decodedimage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                profile_image.setImageBitmap(decodedimage);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            DashboardFragment dashboard = new DashboardFragment();
            dashboard.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,dashboard).commit();
            navigationView.setCheckedItem(R.id.nav_dashboard);
        }

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Bundle bundle = new Bundle();
        switch (menuItem.getItemId()){
            case R.id.nav_dashboard:

                bundle.putString("JSON_Response",fetchIntentValue());
                Log.i("Intent Value",fetchIntentValue());
                DashboardFragment dashboard = new DashboardFragment();
                dashboard.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,dashboard).commit();
                break;
            case R.id.nav_profile:
                bundle.putString("JSON_Response",fetchIntentValue());
//                Log.i("Intent Value",fetchIntentValue());
                Profile_Fragment prof_frag = new Profile_Fragment();
                prof_frag.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,prof_frag).commit();
                Toast.makeText(this,"Profile",Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_history:
                Toast.makeText(this,"History",Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_generate:
                Toast.makeText(this,"Generate",Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_signout:
                Toast.makeText(this,"Sign Out",Toast.LENGTH_SHORT).show();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    private String fetchIntentValue(){
        String fetched = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null){
            fetched = extras.getString("fetched");
        }
        return fetched;
    }


}
