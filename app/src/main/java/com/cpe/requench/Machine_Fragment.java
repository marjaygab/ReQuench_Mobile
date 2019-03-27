package com.cpe.requench;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class Machine_Fragment extends Fragment {
    private RequestQueue requestQueue;
    private LinkedList<ReQuench_Machine> machines;
    private ListView machines_list;
    private CustomAdapter adapter;
    private FirebaseFirestore db;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_machine,container,false);
    }




    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        machines_list = view.findViewById(R.id.machines_list);
        adapter = new CustomAdapter();
        machines = new LinkedList<>();

        getMachines();
        db = FirebaseFirestore.getInstance();
        db.collection("Machines").addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if(e != null){
                    return;
                }
                for(DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()){
                    DocumentSnapshot documentSnapshot = dc.getDocument();
                    String document_name = documentSnapshot.getId();

                    ListIterator machine_iterator = machines.listIterator();

                    while(machine_iterator.hasNext()){
                        ReQuench_Machine machine = (ReQuench_Machine) machine_iterator.next();
                        if (machine.getMU_ID().equals(document_name)){
                            machine.setAPI_Key(documentSnapshot.getString("api_key"));
                            machine.setCurrent_Water_Level(documentSnapshot.getDouble("current_water_level"));
                            machine.setDate_of_Purchase(Date.valueOf(documentSnapshot.getString("date_of_purchase")));
                            machine.setLast_Maintenance_Date(Date.valueOf(documentSnapshot.getString("last_maintenance_date")));
                            machine.setMachine_Location(documentSnapshot.getString("location"));
                            machine.setModel_Number(documentSnapshot.getString("Model_Number"));
                            machine.setMU_ID(documentSnapshot.get("mu_id").toString());
                            machine.setSTATUS(documentSnapshot.getString("status"));

                            adapter.notifyDataSetChanged();
                        }
                    }

                }
            }
        });


    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return machines.size();
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
            convertView = getLayoutInflater().inflate(R.layout.machines_custom_layout,null);
            TextView machine_title = convertView.findViewById(R.id.machine_title);
            TextView machine_location = convertView.findViewById(R.id.machine_location);
            TextView maintenance_date = convertView.findViewById(R.id.maintenance_date);
            TextView api_key = convertView.findViewById(R.id.api_key);
            ProgressBar water_level_progress = convertView.findViewById(R.id.water_level_progress);
            TextView water_level_percentage = convertView.findViewById(R.id.water_level_percentage);
            CheckBox machine_status = convertView.findViewById(R.id.machine_status);
            Double percentage = 0.0;
            int percent = 0;
            String percentage_string = "";
            Double current_water_level = machines.get(position).getCurrent_Water_Level();
            if(current_water_level != null){
                percentage = getPercentage(current_water_level,20000.0);
                percent = (int) Math.round(percentage);
                percentage_string = String.valueOf(percent);
            }else{
                percentage_string = "0";
            }

            water_level_percentage.setText(percentage_string + "%");
            machine_title.setText("Machine " + machines.get(position).getMU_ID());
            machine_location.setText("Location: " + machines.get(position).getMachine_Location());
            String main_date = (machines.get(position).getLast_Maintenance_Date() == null) ? "Not yet configured" : machines.get(position).getLast_Maintenance_Date().toString();
            maintenance_date.setText("Last Maintenance Date: " + main_date);
            api_key.setText((machines.get(position).getAPI_Key() == null) ? "API Key: Not yet configured" : "API Key: " + machines.get(position).getAPI_Key());
            water_level_progress.setMax(100);
            water_level_progress.setProgress((int) Math.round(percentage));

            if (machines.get(position).getSTATUS() != null){
                if (machines.get(position).getSTATUS().equals("ONLINE") || machines.get(position).getSTATUS().equals("online")){
                    Log.i("Checked","true");
                    machine_status.setChecked(true);
                }else{
                    Log.i("Checked","false");
                    machine_status.setChecked(false);
                }
            }else{
                machine_status.setChecked(false);
            }


            return convertView;
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

                            for (int i=0; i<machines_array.length();i++){
                                JSONObject machine_object = machines_array.getJSONObject(i);
                                Log.i("API_KEY: ", machine_object.getString("MU_ID") + " " + machine_object.getString("API_KEY"));
                                if (!machine_object.getString("API_KEY").equals("null")){
                                    machines.add(new ReQuench_Machine(machine_object.getString("MU_ID"),machine_object.getString("Model_Number"),machine_object.getString("API_KEY"),
                                            machine_object.getString("Machine_Location"),machine_object.getDouble("Current_Water_Level"),Date.valueOf(machine_object.getString("Date_of_Purchase")),
                                            Date.valueOf(machine_object.getString("Last_Maintenance_Date")),(machine_object.getString("STATUS"))));
                                }else{
                                    machines.add(new ReQuench_Machine(machine_object.getString("MU_ID"),machine_object.getString("Model_Number"),null,
                                            machine_object.getString("Machine_Location"),null,Date.valueOf(machine_object.getString("Date_of_Purchase")),
                                            Date.valueOf(machine_object.getString("Last_Maintenance_Date")),null));
                                }
                            }

                            machines_list.setAdapter(adapter);

                        }
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
}
