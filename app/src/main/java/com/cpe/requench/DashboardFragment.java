package com.cpe.requench;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Random;
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
import org.w3c.dom.Text;

import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class DashboardFragment extends Fragment {

    String url = "https://requench.000webhostapp.com/Fetch_History.php";
    JSONObject fetched_json,response_object;
    JSONArray response_array;
    private StringRequest stringrequest;
    private RequestQueue requestqueue;
    private LinkedList<Transaction_History> transactions;
    private LinearLayout temp_pass_field;
    private String Account_ID,Access_Level;
    private ListView recent_list;
    private Button generate;
    private ProgressBar timer;
    private String fragment_message;
    private Random random;
    private CustomAdapter adapter;
    private TextView balance,empty,seemore,otp_first;
    private TextView[] otp_fields;
    private int progress = 0;
    private Handler handler;
    private String currentOTP;

    private enum Commands{
        GET_HISTORY,GENERATE_OTP,CLEAR_OTP
    }

//    private String generateOTP(){
//        int index;
//        String temp = "";
//        for (int i=0;i<6;i++){
//            index = random.nextInt(charactercollection.length-1);
//            temp += charactercollection[index];
//        }
//        return temp;
//    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        fragment_message = getArguments().getString("JSON_Response");

        return inflater.inflate(R.layout.dashboard,container,false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        temp_pass_field = (LinearLayout) getView().findViewById(R.id.temp_pass_field);
        otp_fields = new TextView[]{getView().findViewById(R.id.temp_pass_first),
                getView().findViewById(R.id.temp_pass_second),getView().findViewById(R.id.temp_pass_third),
                getView().findViewById(R.id.temp_pass_fourth),getView().findViewById(R.id.temp_pass_fifth),
                getView().findViewById(R.id.temp_pass_sixth)};
        balance = (TextView) getView().findViewById(R.id.balance);
        empty = (TextView) getView().findViewById(R.id.empty);
        seemore = (TextView) getView().findViewById(R.id.seemore_label);
        timer = (ProgressBar) getView().findViewById(R.id.timer_bar);
        transactions = new LinkedList<>();
        random = new Random();
        requestqueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        recent_list = (ListView) getView().findViewById(R.id.recent_list);
        adapter = new CustomAdapter();
        generate = (Button) getView().findViewById(R.id.generate);
        empty.setText("No records exists");
        handler = new Handler();


        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestHTTP(Commands.GENERATE_OTP);



            }
        });

        try {
            fetched_json = new JSONObject(fragment_message);
            JSONObject account_object = fetched_json.getJSONObject("Account_Details");
            Access_Level = account_object.getString("Access_Level");
            Account_ID = account_object.getString("Acc_ID");
            balance.setText(account_object.getString("Balance") + " mL");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestHTTP(Commands.GET_HISTORY);




    }

    private void requestHTTP(Commands comm){
        final Commands command = comm;

        switch(command){
            case GET_HISTORY:
                url = "https://requench.000webhostapp.com/Fetch_History.php";
                stringrequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject temp_object;
                        try {
                            response_array = new JSONArray(response);
                            for (int i = 0; i < response_array.length();i++){
                                temp_object = response_array.getJSONObject(i);

                                Transaction_History trans = new Transaction_History(Integer.parseInt(temp_object.getString("Transaction_ID")),
                                        temp_object.getString("Machine_Location"),Date.valueOf(temp_object.getString("Date")),
                                        Time.valueOf(temp_object.getString("Time")),temp_object.getString("Temperature"),
                                        Double.valueOf(temp_object.getString("Amount_Dispensed")),Double.valueOf(temp_object.getString("Price_Computed")),
                                        Double.valueOf(temp_object.getString("Remaining_Balance")));
                                transactions.add(trans);
                                Log.i("Temp",temp_object.getString("Temperature"));
                            }
                            recent_list.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (transactions.size() == 0){
                            seemore.setText("No records found");
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity().getApplicationContext(),"An Error Occured" + error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> MyData = new HashMap<String,String>();
                        MyData.put("Acc_ID",Account_ID);
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
                break;
            case GENERATE_OTP:
                url = "https://requench.000webhostapp.com/Generate_OTP.php";
                stringrequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        generate.setEnabled(false);
                                    }
                                });
                                while(progress < 100){
                                    progress++;
                                    android.os.SystemClock.sleep(500);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            timer.setProgress(progress);
                                        }
                                    });
                                }
                                progress = 0;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int i  = 0; i<otp_fields.length;i++){
                                            otp_fields[i].setText("-");
                                        }
                                        generate.setEnabled(true);
                                        timer.setProgress(0);
                                        requestHTTP(Commands.CLEAR_OTP);
                                    }
                                });

                            }
                        }).start();


                        try {
                            JSONObject otp_response = new JSONObject(response);
                            JSONArray otp_characters = otp_response.getJSONArray("OTP");
                            for (int i  = 0; i<otp_characters.length();i++){
                                otp_fields[i].setText(otp_characters.getString(i));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity().getApplicationContext(),"An Error Occured" + error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> MyData = new HashMap<String,String>();
                        MyData.put("Acc_ID",Account_ID);
                        return MyData;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("Content-Type","application/x-www-form-urlencoded");
                        return params;
                    }
                };
                break;
            case CLEAR_OTP:
                url = "https://requench.000webhostapp.com/Clear_OTP.php";
                stringrequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity().getApplicationContext(),"An Error Occured" + error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }){
                    @Override
                    protected Map<String,String> getParams(){
                        Map<String,String> MyData = new HashMap<String,String>();
                        MyData.put("Acc_ID",Account_ID);
                        return MyData;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("Content-Type","application/x-www-form-urlencoded");
                        return params;
                    }
                };
                break;
        }

        requestqueue.add(stringrequest);
    }



    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return transactions.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.custom_layout,null);
            ImageView status_image = (ImageView) convertView.findViewById(R.id.image_temp);
            TextView machine_loc = (TextView) convertView.findViewById(R.id.machine_location);
            TextView amount_dispensed = (TextView) convertView.findViewById(R.id.amount_dispensed);


            if(transactions.get(position).Temperature.equals("COLD")){
                status_image.setImageResource(R.drawable.cold_drop);
            }else{
                status_image.setImageResource(R.drawable.hot_drop);
            }

            machine_loc.setText(transactions.get(position).Machine_Loc);
            amount_dispensed.setText(String.valueOf(transactions.get(position).amount) + " mL");


            return convertView;
        }
    }
}
