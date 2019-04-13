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

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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

    String url = "https://requench-rest.herokuapp.com/Fetch_History.php";
    JSONObject fetched_json,response_object;
    JSONArray response_array,purchase_array;
    private StringRequest stringrequest;
    private RequestQueue requestqueue;
    private LinkedList<Transaction_History> transactions;
    private LinkedList<Purchase_History> purchases;
    private LinkedList<Object> recent_activity, partial_view;
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
        purchases = new LinkedList<>();
        recent_activity = new LinkedList<>();
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

    private void sortList(LinkedList<Object> recent_activity_list){

        Collections.sort(recent_activity_list, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                Date date_1 = Date.valueOf("1970-01-01");
                Date date_2 = Date.valueOf("1970-01-01");
                Time time_1 = Time.valueOf("00:00:00");
                Time time_2 = Time.valueOf("00:00:00");
                if (o1.getClass().toString().equals("class com.cpe.requench.Transaction_History")){

                    Transaction_History transaction = (Transaction_History) o1;
                    date_1 = Date.valueOf(transaction.getDate_of_purchase().toString());
                    time_1 = Time.valueOf(transaction.getTime_of_purchase().toString());
                }else if(o1.getClass().toString().equals("class com.cpe.requench.Purchase_History")){

                    Purchase_History purchase = (Purchase_History) o1;
                    date_1 = Date.valueOf(purchase.getDate_of_purchase().toString());
                    time_1 = Time.valueOf(purchase.getTime_of_purchase().toString());
                }

                if (o2.getClass().toString().equals("class com.cpe.requench.Transaction_History")){

                    Transaction_History transaction = (Transaction_History) o2;
                    date_2 = Date.valueOf(transaction.getDate_of_purchase().toString());
                    time_2 = Time.valueOf(transaction.getTime_of_purchase().toString());
                }else if(o2.getClass().toString().equals("class com.cpe.requench.Purchase_History")){

                    Purchase_History purchase = (Purchase_History) o2;
                    date_2 = Date.valueOf(purchase.getDate_of_purchase().toString());
                    time_2 = Time.valueOf(purchase.getTime_of_purchase().toString());
                }
                int return_val = 0;
                if(date_1.compareTo(date_2) == -1){
                    Log.i("Date Compare",date_1.toString() +" " + date_2.toString() + " -1");
                    return_val =  -1;
                }else if(date_1.compareTo(date_2) == 1){
                    Log.i("Date Compare",date_1.toString() +" " + date_2.toString() + " 1");
                    return_val =  1;
                }else{
                    if (time_1.compareTo(time_2) == -1){
                        Log.i("Time Compare",time_1.toString() +" " + time_2.toString() + " -1");
                        return_val =  -1;
                    }else if (time_1.compareTo(time_2) == 1){
                        Log.i("Time Compare",time_1.toString() +" " + time_2.toString() + " 1");
                        return_val =  1;
                    }else{
                        Log.i("Time Compare",time_1.toString() +" " + time_2.toString() + " -1");
                        return_val =  0;
                    }
                }




                return -(return_val);
            }
        });
    }

    private int compareDateTime(String startDateTime,String endDateTime){

        return 0;
    }



    private void requestHTTP(Commands comm){
        final Commands command = comm;
        JsonObjectRequest postRequest;
        JSONObject params = new JSONObject();
        switch(command){
            case GET_HISTORY:
                url = "https://requench-rest.herokuapp.com/Fetch_History.php";
                try {
                    params.put("Acc_ID",Account_ID);
                }catch(Exception e){
                    Log.i("Error.Response", e.toString());
                }
                postRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                        new Response.Listener<JSONObject>()
                        {
                            @Override
                            public void onResponse(JSONObject response) {
                                // display response
                                try {
                                    try {
                                        response_array = response.getJSONArray("Transactions");
                                    }catch (Exception ex){
                                        response_array = new JSONArray();
                                    }

                                    try {
                                        purchase_array = response.getJSONArray("Purchase");
                                    }catch (Exception ex){
                                        purchase_array = new JSONArray();
                                    }

                                    for (int i = 0; i < response_array.length();i++){
                                        JSONObject temp_object = response_array.getJSONObject(i);
                                        try{
                                            Transaction_History trans = new Transaction_History(Date.valueOf(temp_object.getString("Date")),Time.valueOf(temp_object.getString("Time"))
                                            ,Double.valueOf(temp_object.getString("Amount")),Double.valueOf(temp_object.getString("Price_Computed")),Integer.parseInt(temp_object.getString("Transaction_ID")),
                                                    temp_object.getString("Machine_Location"),temp_object.getString("Temperature"),Double.valueOf(temp_object.getString("Remaining_Balance")));
                                            transactions.add(trans);
                                        }catch (Exception e){
                                            continue;
                                        }
//                                        Log.i("Temp",temp_object.getString("Date"));
                                    }

                                    for (int i = 0; i < purchase_array.length();i++){
                                        JSONObject temp_object = purchase_array.getJSONObject(i);
                                        try{
                                            Purchase_History purchase = new Purchase_History(Date.valueOf(temp_object.getString("Date")), Time.valueOf(temp_object.getString("Time")),
                                                    Double.valueOf(temp_object.getString("Amount")),Double.valueOf(temp_object.getString("Price_Computed")),temp_object.getInt("Purchase_ID"));
                                            purchases.add(purchase);
                                        }catch (Exception e){
                                            continue;
                                        }
//                                        Log.i("Temp",temp_object.getString("Date"));
                                    }

                                    recent_activity.addAll(transactions);
                                    recent_activity.addAll(purchases);
                                    sortList(recent_activity);

                                    for (int i = recent_activity.size()-1; i>5 ;  i--){
                                        recent_activity.remove(i);
                                    }
                                    recent_list.setAdapter(adapter);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if (transactions.size() == 0 && purchases.size() == 0){
                                    seemore.setText("No records found");
                                }
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i("Error.Response", error.toString());
                            }
                        }
                ){
                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put("Acc_ID", Account_ID);
                        return params;
                    }
                };
                requestqueue.add(postRequest);
                break;
            case GENERATE_OTP:
                url = "https://requench-rest.herokuapp.com/Generate_OTP.php";
                params = new JSONObject();
                try{
                    params.put("Acc_ID",Account_ID);
                }catch(Exception e){
                    Toast.makeText(getActivity().getApplicationContext(),"An Error Occured (GOT): " + e.getMessage(),Toast.LENGTH_SHORT).show();
                }

                postRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
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
                            JSONObject otp_response = response;
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
                        Toast.makeText(getActivity().getApplicationContext(),"An Error Occured (Volley): " + error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
                requestqueue.add(postRequest);
                break;
            case CLEAR_OTP:
                url = "https://requench-rest.herokuapp.com/Clear_OTP.php";
                params = new JSONObject();
                try{
                    params.put("Acc_ID",Account_ID);
                }catch(Exception e){
                    Toast.makeText(getActivity().getApplicationContext(),"An Error Occured" + e.getMessage(),Toast.LENGTH_SHORT).show();
                }

                postRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity().getApplicationContext(),"An Error Occured" + error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
                requestqueue.add(postRequest);
                break;
        }
    }



    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return recent_activity.size();
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
            ImageView status_image = (ImageView) convertView.findViewById(R.id.image);
            TextView description = (TextView) convertView.findViewById(R.id.description);
            TextView date_time = (TextView) convertView.findViewById(R.id.date_time);
            TextView amount = (TextView) convertView.findViewById(R.id.amount);
            if (recent_activity.get(position).getClass().toString().equals("class com.cpe.requench.Transaction_History")){
                Transaction_History transaction = (Transaction_History) recent_activity.get(position);
                Log.i("Testing","Transaction: " + transaction.getPrice());
                if(transaction.Temperature.equals("COLD")){
                    status_image.setImageResource(R.drawable.cold_drop);
                }else{
                    status_image.setImageResource(R.drawable.hot_drop);
                }
                description.setText(transaction.Machine_Loc);
                amount.setText(String.valueOf(transaction.getAmount()) + " mL");
                date_time.setText(transaction.getDate_of_purchase() + " " + transaction.getTime_of_purchase());
            }else if (recent_activity.get(position).getClass().toString().equals("class com.cpe.requench.Purchase_History")){

                Purchase_History purchase = (Purchase_History) recent_activity.get(position);
                Log.i("Testing","Purchase: " + purchase.getPrice());
                status_image.setImageResource(R.drawable.logo);
                description.setText("Added ReQuench Points");
                amount.setText("Php " + String.valueOf(purchase.getAmount()));
                date_time.setText(purchase.getDate_of_purchase() + " " + purchase.getTime_of_purchase());
            }else{
                Log.i("Testing",recent_activity.get(position).getClass().toString());
            }
            return convertView;
        }
    }
}
