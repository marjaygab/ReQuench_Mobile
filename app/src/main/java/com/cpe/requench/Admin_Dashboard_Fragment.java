package com.cpe.requench;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

public class Admin_Dashboard_Fragment extends Fragment {
    private RequestQueue requestQueue;
    private LinkedList<ReQuench_Machine> machines;
    private ArrayList<ReQuench_Notifications> notifications;
    private ViewPager viewPager;
    private ListView notifs_list;
    private MyAdapter myAdapter;
    private CustomAdapter NotifsAdapter;
    private String fragment_message,Access_Level,Account_ID;
    private JSONObject fetched_json;
    private Switch mode_switch;
    private TextView seemore_notifs,seemore_machine,mode_label;
    private RelativeLayout customer_mode;
    private LinearLayout admin_mode;
    private RequestQueue requestqueue;
    private LinkedList<Transaction_History> transactions;
    private LinkedList<Purchase_History> purchases;
    private LinkedList<Object> recent_activity, partial_view;
    private LinearLayout temp_pass_field;
    private ListView recent_list;
    private Button generate;
    private ProgressBar timer;
    private Random random;
    private Admin_Dashboard_Fragment.ActivityCustomAdapter adapter;
    private TextView balance,empty,seemore,otp_first;
    private TextView[] otp_fields;
    private int progress = 0;
    private Handler handler;
    private String currentOTP;
    private FirebaseFirestore db;

    private enum Commands{
        GET_HISTORY,GENERATE_OTP,CLEAR_OTP
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment_message = getArguments().getString("JSON_Response");
        return inflater.inflate(R.layout.admin_dashboard,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
        requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        machines = new LinkedList<>();
        viewPager = view.findViewById(R.id.view_pager);
        notifs_list = view.findViewById(R.id.recent_notifs);
        mode_label = view.findViewById(R.id.mode_label);
        seemore_machine = view.findViewById(R.id.seemore_label_machine);
        seemore_notifs = view.findViewById(R.id.seemore_label_notif);
        notifications = new ArrayList<>();
        admin_mode = view.findViewById(R.id.admin_mode);
        customer_mode = view.findViewById(R.id.customer_mode);
        mode_switch = view.findViewById(R.id.mode_switch);
        mode_switch.setChecked(true);

        myAdapter = new MyAdapter(machines,getContext());


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
        adapter = new Admin_Dashboard_Fragment.ActivityCustomAdapter();
        generate = (Button) getView().findViewById(R.id.generate);
        empty.setText("No records exists");
        handler = new Handler();
        db = FirebaseFirestore.getInstance();
        try {
            fetched_json = new JSONObject(fragment_message);
            JSONObject account_object = fetched_json.getJSONObject("Account_Details");
            Access_Level = account_object.getString("Access_Level");
            Account_ID = account_object.getString("Acc_ID");
            balance.setText(account_object.getString("Balance") + " mL");

        } catch (JSONException e) {
            e.printStackTrace();
        }


        this.getMachines();
        this.getNotifications(Account_ID);
        requestHTTP(Admin_Dashboard_Fragment.Commands.GET_HISTORY);


        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestHTTP(Admin_Dashboard_Fragment.Commands.GENERATE_OTP);
            }
        });


        mode_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    mode_label.setText("ADMIN MODE");
                    admin_mode.setVisibility(View.VISIBLE);
                    customer_mode.setVisibility(View.GONE);
                }
                else {
                    mode_label.setText("CUSTOMER MODE");
                    admin_mode.setVisibility(View.GONE);
                    customer_mode.setVisibility(View.VISIBLE);
                }
            }
        });

        seemore_machine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Admin_Activity parent = (Admin_Activity) getActivity();
                parent.goTo("MACHINE");
            }
        });

        seemore_notifs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Admin_Activity parent = (Admin_Activity) getActivity();
                parent.goTo("NOTIFICATIONS");
            }
        });

        db.collection("Machines").addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(e != null){
                    return;
                }

                Log.i("Testing", "Testing");
                for(DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()){
                    DocumentSnapshot documentSnapshot = dc.getDocument();
                    String document_name = documentSnapshot.getId();

                    ListIterator machine_iterator = machines.listIterator();

                    while(machine_iterator.hasNext()){
                        Admin_Dashboard_Fragment.ReQuench_Machine machine = (Admin_Dashboard_Fragment.ReQuench_Machine) machine_iterator.next();
                        if (machine.getMU_ID().equals(document_name)){
                            machine.setAPI_Key(documentSnapshot.getString("api_key"));
                            machine.setCurrent_Water_Level(documentSnapshot.getDouble("current_water_level"));
                            machine.setDate_of_Purchase(Date.valueOf(documentSnapshot.getString("date_of_purchase")));
                            machine.setLast_Maintenance_Date(Date.valueOf(documentSnapshot.getString("last_maintenance_date")));
                            machine.setMachine_Location(documentSnapshot.getString("location"));
                            machine.setModel_Number(documentSnapshot.getString("Model_Number"));
                            machine.setMU_ID(documentSnapshot.get("mu_id").toString());
                            machine.setSTATUS(documentSnapshot.getString("status"));
                            myAdapter.notifyDataSetChanged();
                            viewPager.setAdapter(myAdapter);
                            Log.i("Machine Status",documentSnapshot.getString("status"));
                        }
                    }



                }
            }
        });

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


    class ReQuench_Machine{
        private String MU_ID,Model_Number,API_Key,Machine_Location;
        private Double Current_Water_Level;
        private Date Date_of_Purchase,Last_Maintenance_Date;
        private String STATUS;

        public ReQuench_Machine(String MU_ID, String model_Number, String API_Key, String machine_Location, Double current_Water_Level, Date date_of_Purchase, Date last_Maintenance_Date, String STATUS) {
            this.MU_ID = MU_ID;
            Model_Number = model_Number;
            this.API_Key = API_Key;
            Machine_Location = machine_Location;
            Current_Water_Level = current_Water_Level;
            Date_of_Purchase = date_of_Purchase;
            Last_Maintenance_Date = last_Maintenance_Date;
            this.STATUS = STATUS;

        }

        public String getMU_ID() {
            return MU_ID;
        }

        public void setMU_ID(String MU_ID) {
            this.MU_ID = MU_ID;
        }

        public String getModel_Number() {
            return Model_Number;
        }

        public void setModel_Number(String model_Number) {
            Model_Number = model_Number;
        }

        public String getAPI_Key() {
            return API_Key;
        }

        public void setAPI_Key(String API_Key) {
            this.API_Key = API_Key;
        }

        public String getMachine_Location() {
            return Machine_Location;
        }

        public void setMachine_Location(String machine_Location) {
            Machine_Location = machine_Location;
        }

        public Double getCurrent_Water_Level() {
            return Current_Water_Level;
        }

        public void setCurrent_Water_Level(Double current_Water_Level) {
            Current_Water_Level = current_Water_Level;
        }

        public Date getDate_of_Purchase() {
            return Date_of_Purchase;
        }

        public void setDate_of_Purchase(Date date_of_Purchase) {
            Date_of_Purchase = date_of_Purchase;
        }

        public Date getLast_Maintenance_Date() {
            return Last_Maintenance_Date;
        }

        public void setLast_Maintenance_Date(Date last_Maintenance_Date) {
            Last_Maintenance_Date = last_Maintenance_Date;
        }

        public String getSTATUS() {
            return STATUS;
        }

        public void setSTATUS(String STATUS) {
            this.STATUS = STATUS;
        }
    }


    class CustomAdapter extends BaseAdapter {

        private List<ReQuench_Notifications> notifications;
        private String Account_ID;

        public CustomAdapter(List<ReQuench_Notifications> notifications, String account_ID) {
            this.notifications = notifications;
            Account_ID = account_ID;
        }

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

    class ActivityCustomAdapter extends BaseAdapter {

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
    class MyAdapter extends PagerAdapter{
        private List<ReQuench_Machine> machine_list;
        private LayoutInflater layoutInflater;
        private Context context;

        public MyAdapter(List<ReQuench_Machine> machine_list, Context context) {
            this.machine_list = machine_list;
            this.context = context;
        }


        public void notifyChanges(){
            this.notifyDataSetChanged();
        }

        public void setMachine_list(List<ReQuench_Machine> machine_list) {
            this.machine_list = machine_list;
        }

        @Override
        public int getCount() {
            return machine_list.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view.equals(o);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            layoutInflater = LayoutInflater.from(context);
            View view  = layoutInflater.inflate(R.layout.view_pager_item, container, false);
            TextView machine_title = view.findViewById(R.id.machine_title);
            RadioButton machine_status = view.findViewById(R.id.machine_status);
            TextView last_maintenance_date = view.findViewById(R.id.last_maintenance_date);
            TextView api_key = view.findViewById(R.id.api_key);
            TextView water_level_percentage = view.findViewById(R.id.water_level_percentage);
            ProgressBar water_level_progress = view.findViewById(R.id.water_level_progress);
            Double current_water_level = this.machine_list.get(position).getCurrent_Water_Level();
            Double percentage = 0.0;
            int percent = 0;
            String percentage_string = "";
            if(current_water_level != null){
                percentage = getPercentage(current_water_level,20000.0);
                percent = (int) Math.round(percentage);
                percentage_string = String.valueOf(percent);
            }else{
                percentage_string = "0";
            }

            machine_title.setText("Machine " + this.machine_list.get(position).MU_ID);

            if (this.machine_list.get(position).getSTATUS() != null){
                if (this.machine_list.get(position).getSTATUS().equals("ONLINE") || this.machine_list.get(position).getSTATUS().equals("online"))
                    machine_status.setChecked(true);
                else
                    machine_status.setChecked(false);
            }else
                machine_status.setChecked(false);




            last_maintenance_date.setText("Last Maintenance Date: " + this.machine_list.get(position).Last_Maintenance_Date.toString());

            if(this.machine_list.get(position).API_Key != null)
                api_key.setText("API Key: " + this.machine_list.get(position).API_Key);
            else
                api_key.setText("API Key: Not Yet Configured");
            water_level_percentage.setText(percentage_string + "%");
            water_level_progress.setProgress((int) Math.round(percentage));

            container.addView(view,0);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

    }

    private void getMachines(){
        String url = "https://requench-rest.herokuapp.com/Fetch_All_Machines.php";
        JsonObjectRequest postRequest;
        JSONObject params = new JSONObject();
        postRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                response -> {
                    // display response
                    try {
                        if (response.getBoolean("Success")){
                            JSONArray machines_array = response.getJSONArray("Machines");

                            int machine_list_limit = 5;

                            if (machines_array.length() < 5)
                                machine_list_limit=  machines_array.length();

                            for (int i=0; i<machine_list_limit;i++){
                                JSONObject machine_object = machines_array.getJSONObject(i);
                                Log.i("API_KEY: ", machine_object.getString("MU_ID") + " " + machine_object.getString("API_KEY"));
                                if (!machine_object.getString("API_KEY").equals("null")){
                                    machines.add(new Admin_Dashboard_Fragment.ReQuench_Machine(machine_object.getString("MU_ID"),machine_object.getString("Model_Number"),machine_object.getString("API_KEY"),
                                            machine_object.getString("Machine_Location"),machine_object.getDouble("Current_Water_Level"),Date.valueOf(machine_object.getString("Date_of_Purchase")),
                                            Date.valueOf(machine_object.getString("Last_Maintenance_Date")),(machine_object.getString("STATUS"))));
                                }else{
                                    machines.add(new Admin_Dashboard_Fragment.ReQuench_Machine(machine_object.getString("MU_ID"),machine_object.getString("Model_Number"),null,
                                            machine_object.getString("Machine_Location"),null,Date.valueOf(machine_object.getString("Date_of_Purchase")),
                                            Date.valueOf(machine_object.getString("Last_Maintenance_Date")),null));
                                }
                            }

                            viewPager.setAdapter(myAdapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.i("Error.Response", error.toString())
        );
        requestQueue.add(postRequest);
    }


    private void getNotifications(String Account_ID){
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
                        int recent_count = 5;

                        if (notif_array.length() < 5)
                            recent_count = notif_array.length();


                        for (int i = 0;i< recent_count;i++){
                            JSONObject notif_response = notif_array.getJSONObject(i);
                            notifications.add(new Admin_Dashboard_Fragment.ReQuench_Notifications(notif_response.getString("Notif_ID"),notif_response.getString("Notif_Title"),
                                    notif_response.getString("Notif_Desc"),Date.valueOf(notif_response.getString("Date_Posted")),Time.valueOf(notif_response.getString("Time_Posted")),
                                    notif_response.getBoolean("Seen")));
                        }

                        NotifsAdapter = new CustomAdapter(notifications,Account_ID);
                        notifs_list.setAdapter(NotifsAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.i("Error.Response", error.toString())
        );
        requestQueue.add(postRequest);
    }

    public Double getPercentage(Double value,Double overall){
        Double percentage_value = (value/overall) * 100;
        return percentage_value;
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
        requestQueue.add(postRequest);
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

    private void requestHTTP(Admin_Dashboard_Fragment.Commands comm){
        final Admin_Dashboard_Fragment.Commands command = comm;
        JsonObjectRequest postRequest;
        JSONObject params = new JSONObject();
        String url;
        switch(command){
            case GET_HISTORY:
                url = "https://requench-rest.herokuapp.com/Fetch_History.php";
                JSONArray response_array = new JSONArray();
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
                                JSONArray response_array = new JSONArray();
                                JSONArray purchase_array = new JSONArray();
                                try {

                                    if(!response.getString("Transactions").isEmpty()){
                                        response_array = response.getJSONArray("Transactions");
                                    }
                                    if(!response.getString("Purchase").isEmpty()){
                                        purchase_array = response.getJSONArray("Purchase");
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
                                if (recent_activity.size() == 0){
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
                        Log.i("Generate OTP",response.toString());
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
                                        requestHTTP(Admin_Dashboard_Fragment.Commands.CLEAR_OTP);
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
}
