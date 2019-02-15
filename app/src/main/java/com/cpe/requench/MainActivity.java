package com.cpe.requench;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private static JSONObject response_object;
    private RequestQueue requestqueue;
    private StringRequest stringrequest;
    public static String response_json;
    private String user,pass;
    private boolean response_flag=false;
    private String url = "http://requench.000webhostapp.com/Mobile_Login.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final EditText user_field, pass_field;
        Button login;
        TextView sign_up;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sign_up = (TextView) findViewById(R.id.sign_up);
        user_field = (EditText) findViewById(R.id.user_field);
        pass_field = (EditText) findViewById(R.id.pass_field);
        login = (Button) findViewById(R.id.login_button);
        requestqueue = Volley.newRequestQueue(getApplicationContext());


        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("JSON Response:",response_object.toString());
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Response:","ON CLICK TRIGGERED");
                user = user_field.getText().toString();
                pass = pass_field.getText().toString();

                if (!isEmpty(user) && !isEmpty(pass)){
                    Log.i("Response:","if entered");
                    verify(user,pass);
                }else{
                    Log.i("Response:","else entered");
                    if (isEmpty(user) && isEmpty(pass)){
                        Log.i("Response:","both empty");
                        Toast.makeText(getApplicationContext(),"Please enter a valid login details!" , Toast.LENGTH_SHORT).show();
                    }else if(isEmpty(pass)){
                        Toast.makeText(getApplicationContext(),"Please enter a Password!" , Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(),"Please enter a Username!" , Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });

    }

    private Boolean isEmpty(String text){
        String trimmed = text.trim();
        if(trimmed.matches("")){
            return true;
        }else{
            return false;
        }
    }

    private void setJSON(JSONObject jsonObject){
        this.response_object = jsonObject;
    }


    private void authorize(){

        //Put trigger to next page here
        Intent intent = new Intent(getApplicationContext(),Home_Activity.class);
        intent.putExtra("fetched",response_object.toString());
        startActivity(intent);

    }


    private void verify(String user, String pass){
        final String username = user;
        final String password = pass;

        Log.i("Response:","Request before Initialization");
        stringrequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                response_json = response;
                try {
                    Log.i("Response:",response);

                    if (isEmpty(response)){
                        Toast.makeText(getApplicationContext(),"Log In Failed" , Toast.LENGTH_SHORT).show();
                    }else{
                        response_object = new JSONObject(response_json);
                        JSONObject account_object = response_object.getJSONObject("Account_Details");

                        setJSON(response_object);
                        String json_user = account_object.getString("User_Name");
                        String json_access = account_object.getString("Access_Level");
                        Log.i("Response:","Message:" + json_user);
                        Toast.makeText(getApplicationContext(),"Welcome " + json_user , Toast.LENGTH_SHORT).show();
                        authorize();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                response_flag = true;


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"An Error Occured" + error.getMessage(),Toast.LENGTH_SHORT).show();
                response_flag = false;
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> MyData = new HashMap<String,String>();
                Log.i("Response","Parameters POSTED");

                MyData.put("User_name",username);
                MyData.put("Password",password);
                return MyData;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                Log.i("Response","Headers POSTED");
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };

        requestqueue.add(stringrequest);
        Log.i("Response","RequestQueue Added String Request");

    }


}
