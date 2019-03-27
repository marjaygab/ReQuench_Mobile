package com.cpe.requench;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

public class Admin_Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = Admin_Activity.class.getName();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private RequestQueue requestqueue;
    private String Account_ID;
    private TextView fullname,email;
    private ImageView profile_image;
    private String Acc_ID,Access_Level,image_string;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        requestqueue = Volley.newRequestQueue(this.getApplicationContext());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_admin_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fullname = navigationView.getHeaderView(0).findViewById(R.id.fullname);
        email = navigationView.getHeaderView(0).findViewById(R.id.email);
        profile_image = navigationView.getHeaderView(0).findViewById(R.id.profile_image);


        Bundle bundle = new Bundle();
        Intent intent = getIntent();
        String fetched_string = fetchIntentValue();
        Log.i("Fetched String", fetched_string);
        try {
            JSONObject fetched_response = new JSONObject(fetched_string);

            JSONObject account_details = fetched_response.getJSONObject("Account_Details");
            Account_ID = account_details.getString("Acc_ID");
            Access_Level = account_details.getString("Access_Level");
            fullname.setText(account_details.getString("First_Name") + " " + account_details.getString("Last_Name"));
            email.setText(account_details.getString("Email"));
            image_string = fetched_response.getString("image");
            Log.i("Image String",image_string);
            byte[] decodedString = Base64.decode(image_string, Base64.DEFAULT);
            Bitmap decodedimage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            profile_image.setImageBitmap(decodedimage);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        FirebaseApp.initializeApp(this);
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Log.i("Token:",token);
                        setCurrentToken(Account_ID,token);
                        // Log and toast
//                        String msg = getString(R.string.msg_token_fmt, token);
//                        Log.d(TAG, msg);
//                        Toast.makeText(Admin_Activity.this, token, Toast.LENGTH_LONG).show();

                    }
                });

        bundle.putString("JSON_Response",fetchIntentValue());
        Admin_Dashboard_Fragment dashboard = new Admin_Dashboard_Fragment();
        dashboard.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_admin_container,dashboard).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_admin_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.admin_, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Bundle bundle = new Bundle();
        if (id == R.id.nav_admin_dashboard) {
            bundle.putString("JSON_Response",fetchIntentValue());
            Admin_Dashboard_Fragment dashboard = new Admin_Dashboard_Fragment();
            dashboard.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_admin_container,dashboard).commit();
        } else if (id == R.id.nav_admin_profile) {
            bundle.putString("JSON_Response",fetchIntentValue());
            Admin_Profile_Fragment admin_profile = new Admin_Profile_Fragment();
            admin_profile.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_admin_container,admin_profile).commit();
        } else if (id == R.id.nav_admin_machine) {
            bundle.putString("JSON_Response",fetchIntentValue());
            Machine_Fragment machine_fragment = new Machine_Fragment();
            machine_fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_admin_container,machine_fragment).commit();
        } else if (id == R.id.nav_admin_notifications) {
            bundle.putString("JSON_Response",fetchIntentValue());
            Notifications_Fragment notifications_fragment = new Notifications_Fragment();
            notifications_fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_admin_container,notifications_fragment).commit();
        } else if (id == R.id.nav_admin_activity){
            bundle.putString("JSON_Response",fetchIntentValue());
            Admin_Recent_Activity_Fragment admin_recent_activity = new Admin_Recent_Activity_Fragment();
            admin_recent_activity.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_admin_container,admin_recent_activity).commit();
        }else if (id == R.id.nav_admin_signout){
            editor.remove("Acc_ID");
            editor.commit();
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_admin_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void goTo(String id){
        Bundle bundle = new Bundle();
        if (id.equals("DASHBOARD")) {
            bundle.putString("JSON_Response",fetchIntentValue());
            Admin_Dashboard_Fragment dashboard = new Admin_Dashboard_Fragment();
            dashboard.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_admin_container,dashboard).commit();
        } else if (id.equals("PROFILE")) {
            bundle.putString("JSON_Response",fetchIntentValue());
            Admin_Profile_Fragment admin_profile = new Admin_Profile_Fragment();
            admin_profile.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_admin_container,admin_profile).commit();
        } else if (id.equals("MACHINE")) {
            bundle.putString("JSON_Response",fetchIntentValue());
            Machine_Fragment machine_fragment = new Machine_Fragment();
            machine_fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_admin_container,machine_fragment).commit();
        } else if (id.equals("NOTIFICATIONS")) {
            bundle.putString("JSON_Response",fetchIntentValue());
            Notifications_Fragment notifications_fragment = new Notifications_Fragment();
            notifications_fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_admin_container,notifications_fragment).commit();
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

    private void setCurrentToken(String Acc_ID,String token){
        String url = "https://requench-rest.herokuapp.com/Update_Token.php";
        JsonObjectRequest postRequest;
        JSONObject params = new JSONObject();
        try {
            params.put("Acc_ID",Acc_ID);
            params.put("registration_token",token);
        }catch(Exception e){
            Log.i("Error.Response", e.toString());
        }
        postRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                response -> {
                    // display response
                    try {
                        if (response.getBoolean("Success")){

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.i("Error.Response", error.toString())
        );
        requestqueue.add(postRequest);
    }
}
