package com.cpe.requench;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.sql.Date;
import java.sql.Time;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;


///**
// * A simple {@link Fragment} subclass.
// * Activities that contain this fragment must implement the
// * {@link Profile_Fragment.OnFragmentInteractionListener} interface
// * to handle interaction events.
// * Use the {@link Profile_Fragment#newInstance} factory method to
// * create an instance of this fragment.
// */
public class Recent_Activity_Fragment extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    JSONObject fetched_json,response_object;
    JSONArray response_array,purchase_array;
    private StringRequest stringrequest;
    private RequestQueue requestqueue;
    private LinkedList<Transaction_History> transactions;
    private LinkedList<Purchase_History> purchases;
    private LinkedList<Object> recent_activity, backup_list;
    private String Account_ID,Access_Level;
    private ListView recent_activity_list;
    private String fragment_message;
    private Recent_Activity_Fragment.CustomAdapter adapter;
    private CheckBox transactions_checkbox,purchase_checkbox;
    private Spinner category_spinner,order_spinner;
    private Handler handler;
    private String selected_category = "Date",selected_order = "Descending";
    private String[] category_items = {"Date","Time","Amount"};
    private String[] order_items = {"Ascending","Descending"};

    public Recent_Activity_Fragment() {
        // Required empty public constructor
    }

//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment Profile_Fragment.
//     */
//    // TODO: Rename and change types and number of parameters

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragment_message = getArguments().getString("JSON_Response");

        return inflater.inflate(R.layout.recent_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recent_activity_list = view.findViewById(R.id.recent_activity_list);
        transactions_checkbox = view.findViewById(R.id.transactions_checkbox);
        purchase_checkbox = view.findViewById(R.id.purchase_checkbox);
        requestqueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        recent_activity = new LinkedList<>();
        backup_list = new LinkedList<>();
        transactions = new LinkedList<>();
        purchases = new LinkedList<>();
        adapter = new CustomAdapter();
        category_spinner = view.findViewById(R.id.category_spinner);
        order_spinner = view.findViewById(R.id.order_spinner);

        final ArrayAdapter<String> category_adapter = new ArrayAdapter<>(getActivity(),R.layout.support_simple_spinner_dropdown_item,category_items);

        final ArrayAdapter<String> order_adapter = new ArrayAdapter<>(getActivity(),R.layout.support_simple_spinner_dropdown_item,order_items);

        category_spinner.setAdapter(category_adapter);
        order_spinner.setAdapter(order_adapter);
        category_spinner.setSelection(category_adapter.getPosition("Date"));
        order_spinner.setSelection(category_adapter.getPosition("Descending"));


        try {
            fetched_json = new JSONObject(fragment_message);
            JSONObject account_object = fetched_json.getJSONObject("Account_Details");
            Access_Level = account_object.getString("Access_Level");
            Account_ID = account_object.getString("Acc_ID");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getActivities();

        category_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_category = category_adapter.getItem(position);
                if (selected_category.equals("Date")){
                    sortListDate(recent_activity,selected_order);
                }else if(selected_category.equals("Time")){
                    sortListTime(recent_activity,selected_order);
                }else{
                    sortListAmount(recent_activity,selected_order);
                }

                recent_activity_list.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        order_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_order = order_adapter.getItem(position);
                if (selected_category.equals("Date")){
                    sortListDate(recent_activity,selected_order);
                }else if(selected_category.equals("Time")){
                    sortListTime(recent_activity,selected_order);
                }else{
                    sortListAmount(recent_activity,selected_order);
                }

                recent_activity_list.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        transactions_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ListIterator recent_list_iterator = recent_activity.listIterator();
                if (isChecked){
                    recent_activity.addAll(transactions);
                    if (selected_category.equals("Date")){
                        sortListDate(recent_activity,selected_order);
                    }else if(selected_category.equals("Time")){
                        sortListTime(recent_activity,selected_order);
                    }else{
                        sortListAmount(recent_activity,selected_order);
                    }
                    adapter.notifyDataSetChanged();
                }else{
                    while(recent_list_iterator.hasNext()){
                        Object element = recent_list_iterator.next();
                        if (element.getClass().toString().equals("class com.cpe.requench.Transaction_History")){
                            recent_list_iterator.remove();
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });


        purchase_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ListIterator recent_list_iterator = recent_activity.listIterator();
                if(isChecked){
                    recent_activity.addAll(purchases);
                    if (selected_category.equals("Date")){
                        sortListDate(recent_activity,selected_order);
                    }else if(selected_category.equals("Time")){
                        sortListTime(recent_activity,selected_order);
                    }else{
                        sortListAmount(recent_activity,selected_order);
                    }
                    adapter.notifyDataSetChanged();
                }else{
                    while(recent_list_iterator.hasNext()){
                        Object element = recent_list_iterator.next();
                        if (element.getClass().toString().equals("class com.cpe.requench.Purchase_History")){
                            recent_list_iterator.remove();
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }


    private void sortListDate(LinkedList<Object> recent_activity_list, final String order){
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


                if (order == "Descending"){
                    return -(return_val);
                }else{
                    return return_val;
                }

            }
        });
    }

    private void sortListTime(LinkedList<Object> recent_activity_list, final String order){
        Collections.sort(recent_activity_list, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                Time time_1 = Time.valueOf("00:00:00");
                Time time_2 = Time.valueOf("00:00:00");
                if (o1.getClass().toString().equals("class com.cpe.requench.Transaction_History")){
                    Transaction_History transaction = (Transaction_History) o1;
                    time_1 = Time.valueOf(transaction.getTime_of_purchase().toString());
                }else if(o1.getClass().toString().equals("class com.cpe.requench.Purchase_History")){

                    Purchase_History purchase = (Purchase_History) o1;
                    time_1 = Time.valueOf(purchase.getTime_of_purchase().toString());
                }

                if (o2.getClass().toString().equals("class com.cpe.requench.Transaction_History")){

                    Transaction_History transaction = (Transaction_History) o2;
                    time_2 = Time.valueOf(transaction.getTime_of_purchase().toString());
                }else if(o2.getClass().toString().equals("class com.cpe.requench.Purchase_History")){

                    Purchase_History purchase = (Purchase_History) o2;
                    time_2 = Time.valueOf(purchase.getTime_of_purchase().toString());
                }
                int return_val = 0;

                    if (time_1.compareTo(time_2) == -1){
                        return_val =  -1;
                    }else if (time_1.compareTo(time_2) == 1){
                        return_val =  1;
                    }else{
                        return_val =  0;
                    }

                if (order == "Descending"){
                    return -(return_val);
                }else{
                    return return_val;
                }
            }
        });
    }

    private void sortListAmount(LinkedList<Object> recent_activity_list, final String order){
        Collections.sort(recent_activity_list, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                Double amt1 = 0.0;
                Double amt2 = 0.0;
                if (o1.getClass().toString().equals("class com.cpe.requench.Transaction_History")){
                    Transaction_History transaction = (Transaction_History) o1;
                    amt1 = transaction.getAmount();
                }else if(o1.getClass().toString().equals("class com.cpe.requench.Purchase_History")){
                    Purchase_History purchase = (Purchase_History) o1;
                    amt1 = purchase.getAmount();
                }

                if (o2.getClass().toString().equals("class com.cpe.requench.Transaction_History")){
                    Transaction_History transaction = (Transaction_History) o2;
                    amt2 = transaction.getAmount();
                }else if(o2.getClass().toString().equals("class com.cpe.requench.Purchase_History")){
                    Purchase_History purchase = (Purchase_History) o2;
                    amt2 = purchase.getAmount();
                }
                int return_val = 0;

                if ((amt1-amt2) < 0){
                    return_val =  -1;
                }else if ((amt1-amt2) > 0){
                    return_val =  1;
                }else{
                    return_val =  0;
                }

                if (order == "Descending"){
                    return -(return_val);
                }else{
                    return return_val;
                }
            }
        });
    }




    private void getActivities(){
        String url = "https://requench-rest.herokuapp.com/Fetch_History.php";
        JsonObjectRequest postRequest;
        JSONObject params = new JSONObject();
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
                            response_array = response.getJSONArray("Transactions");
                            purchase_array = response.getJSONArray("Purchase");
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
                            backup_list.addAll(recent_activity);
                            sortListDate(recent_activity,"Descending");


                            recent_activity_list.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
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
            TextView amount = (TextView) convertView.findViewById(R.id.amount);
            TextView date_time = (TextView) convertView.findViewById(R.id.date_time);
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

