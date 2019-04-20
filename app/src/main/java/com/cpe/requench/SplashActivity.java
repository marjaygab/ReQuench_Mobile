package com.cpe.requench;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity{
    private static final String TAG = SplashActivity.class.getName();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private RequestQueue requestQueue;
    private JsonObjectRequest jsonObjectRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        requestQueue = Volley.newRequestQueue(this.getApplicationContext());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        String account_id = sharedPreferences.getString("Acc_ID",null);
        if(account_id == null || account_id.isEmpty()){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }else{

            fetchProfile(account_id);
        }
    }

    public void fetchProfile(String Acc_ID){
        String url = "https://requench-rest.herokuapp.com/Fetch_Profile.php";
        JSONObject params = new JSONObject();


        try{
            params.put("Acc_ID",Acc_ID);
        }catch(Exception e){
            e.printStackTrace();
        }
        jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params, response -> {
            try {
                if (response.getBoolean("Success")){
                    authorize(response);
                }else{
                    Log.i("Splash Response",response.toString());
                    AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this)
                    .setTitle("An Error Occured.")
                    .setMessage("Please login and try again.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    editor.remove("Acc_ID");
                                    editor.commit();
                                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            error.printStackTrace();
        });

        requestQueue.add(jsonObjectRequest);
    }

    private void authorize(JSONObject response_object){
        //Put trigger to next page here
        try {
            JSONObject account_details = response_object.getJSONObject("Account_Details");
            if (account_details.getString("Access_Level").equals("USER")){
                Intent intent = new Intent(getApplicationContext(),Home_Activity.class);
                intent.putExtra("fetched",response_object.toString());
                startActivity(intent);
            }else{
                Intent intent = new Intent(getApplicationContext(),Admin_Activity.class);
                intent.putExtra("fetched",response_object.toString());
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
