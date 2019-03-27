package com.cpe.requench;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Notifications_Fragment extends Fragment {

    private RequestQueue requestqueue;
    ListView notifs_list;
    LinkedList<ReQuench_Notifications> notifications;
    String fragment_message;
    JSONObject fetched_json;
    String Access_Level,Account_ID;
    CustomAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment_message = getArguments().getString("JSON_Response");
        return inflater.inflate(R.layout.fragment_notifications,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        notifs_list = view.findViewById(R.id.notifs_list);
        notifications = new LinkedList<>();
        requestqueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        adapter = new CustomAdapter();

        try {
            fetched_json = new JSONObject(fragment_message);
            JSONObject account_object = fetched_json.getJSONObject("Account_Details");
            Access_Level = account_object.getString("Access_Level");
            Account_ID = account_object.getString("Acc_ID");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getNotifications();
    }

    private void getNotifications(){
        String url = "https://requench-rest.herokuapp.com/Fetch_Notifs.php";
        JsonObjectRequest postRequest;
        JSONObject params = new JSONObject();
        try {
            params.put("Acc_ID",Account_ID);
        }catch(Exception e){
            Log.i("Error.Response", e.toString());
        }
        postRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                response -> {
                    // display response
                    try {
                        JSONArray notif_array = response.getJSONArray("Notifications");
                        for (int i = 0;i< notif_array.length();i++){
                            JSONObject notif_response = notif_array.getJSONObject(i);
                            notifications.add(new ReQuench_Notifications(notif_response.getString("Notif_ID"),notif_response.getString("Notif_Title"),
                                    notif_response.getString("Notif_Desc"),Date.valueOf(notif_response.getString("Date_Posted")),Time.valueOf(notif_response.getString("Time_Posted")),
                                    notif_response.getBoolean("Seen")));
                        }
                        notifs_list.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.i("Error.Response", error.toString())
        );
        requestqueue.add(postRequest);
    }

    private void updateSeen(String Notif_ID,String Acc_ID,Boolean seen){
        String url = "https://requench-rest.herokuapp.com/Update_Seen.php";
        JsonObjectRequest postRequest;
        JSONObject params = new JSONObject();
        try {
            params.put("Acc_ID",Acc_ID);
            params.put("Notif_ID",Notif_ID);
            params.put("Seen",seen);
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





    class CustomAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return notifications.size();
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
            convertView = getLayoutInflater().inflate(R.layout.notifs_custom_layout,null);
            TextView notif_title = convertView.findViewById(R.id.notif_title);
            TextView notif_desc = convertView.findViewById(R.id.notif_desc);
            TextView notif_date_time = convertView.findViewById(R.id.notif_date_time);
            ImageView image = convertView.findViewById(R.id.notif_image);
            CheckBox seen = convertView.findViewById(R.id.seen_notif);

            notif_title.setText(notifications.get(position).title);
            notif_desc.setText(notifications.get(position).body);
            notif_date_time.setText(notifications.get(position).notif_date.toString() + " " +notifications.get(position).notif_time.toString());
            image.setImageResource(R.drawable.logo);

            if(notifications.get(position).seen){
                seen.setChecked(true);
            }else{
                seen.setChecked(false);

            }

            seen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        updateSeen(notifications.get(position).notif_id,Account_ID,true);
                    }else{
                        updateSeen(notifications.get(position).notif_id,Account_ID,false);
                    }
                }
            });

            return convertView;
        }
    }

    class ReQuench_Notifications{
        private String notif_id;
        private String title;
        private String body;
        private Date notif_date;
        private Time notif_time;
        private Boolean seen;

        public ReQuench_Notifications(String notif_id, String title, String body, Date notif_date, Time notif_time, Boolean seen) {
            this.notif_id = notif_id;
            this.title = title;
            this.body = body;
            this.notif_date = notif_date;
            this.notif_time = notif_time;
            this.seen = seen;
        }

        public String getTitle() {
            return title;
        }

        public String getBody() {
            return body;
        }

        public Date getNotif_date() {
            return notif_date;
        }

        public Time getNotif_time() {
            return notif_time;
        }

        public Boolean getSeen() {
            return seen;
        }
    }

}
