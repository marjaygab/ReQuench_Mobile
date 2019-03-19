package com.cpe.requench;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Date;
import java.sql.Time;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
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
public class Profile_Fragment extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private StringRequest stringrequest;
    private RequestQueue requestqueue;
    private Handler handler;
    private String url;
    private JSONObject fetched_json,response_object;
    private JSONArray response_array;
    private String Account_ID,Access_Level,FN,LN,Balance,email_string,ID_Number;
    private String fragment_message,image_str,file_name;
    private TextView fullname,id_number_string,email_text;
    private TextView edit_account,edit_email,edit_phone;
    private ImageView profile_image;
    private Dictionary edit_text_components;
    private FloatingActionButton picture_button;
    private EditText firstname,lastname,id_number,username,password,email,phonenumber;
    private EditText[] edit_text_elements = {firstname,lastname,id_number,username,password,email,phonenumber};
    private int[] edit_text_id = {R.id.firstname,R.id.lastname,R.id.id_number,R.id.username,R.id.password,R.id.email,R.id.phonenumber};
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public enum Commands{
        INIT_PROFILE,EDIT_PROFILE,UPLOAD_IMAGE
    }


    public Profile_Fragment() {
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

        return inflater.inflate(R.layout.profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        requestqueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        handler = new Handler();
        edit_text_components = new Hashtable();
        picture_button = view.findViewById(R.id.picture_button);
        profile_image = view.findViewById(R.id.profile_image);
        firstname = view.findViewById(R.id.firstname);
        lastname = view.findViewById(R.id.lastname);
        id_number = view.findViewById(R.id.id_number);
        username = view.findViewById(R.id.username);
        password = view.findViewById(R.id.password);
        email = view.findViewById(R.id.email);
        phonenumber = view.findViewById(R.id.phonenumber);
        edit_account = view.findViewById(R.id.edit_account);
        edit_phone = view.findViewById(R.id.edit_phone);
        edit_email = view.findViewById(R.id.edit_email);
        firstname.setEnabled(false);
        lastname.setEnabled(false);
        id_number.setEnabled(false);
        username.setEnabled(false);
        password.setEnabled(false);
        email.setEnabled(false);
        phonenumber.setEnabled(false);


        try {
            JSONObject fragment_object = new JSONObject(fragment_message);
            JSONObject account_details = fragment_object.getJSONObject("Account_Details");
            Account_ID = account_details.getString("Acc_ID");
            Access_Level = account_details.getString("Access_Level");
            FN = account_details.getString("First_Name");
            LN = account_details.getString("Last_Name");
            firstname.setText(FN);
            lastname.setText(LN);
            Log.i("Access_Level",Access_Level);
            ID_Number = account_details.getString("ID_Number");
            username.setText(account_details.getString("User_Name"));
            password.setText(account_details.getString("Password"));
            Log.i("ID NUMBER",ID_Number);
            id_number.setText(ID_Number);
            email_string = account_details.getString("Email");
            email.setText(email_string);
            Balance = account_details.getString("Balance");
            requestHTTP(Commands.INIT_PROFILE);
            Log.i("Info Gathered",fragment_message);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        picture_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.i("Clicked","Picture Button Pressed");
                PickImageDialog.build(new PickSetup()).setOnPickResult(new IPickResult() {
                    @Override
                    public void onPickResult(PickResult pickResult) {
                        file_name = Access_Level + "_" + Account_ID + ".png";

                        Bitmap fetched_image = pickResult.getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        fetched_image.compress(Bitmap.CompressFormat.PNG, 50, stream);

                        byte [] byte_arr = stream.toByteArray();
                        image_str = Base64.encodeToString(byte_arr,Base64.DEFAULT);
                       requestHTTP(Commands.UPLOAD_IMAGE);

                    }
                }).setOnPickCancel(new IPickCancel() {
                    @Override
                    public void onCancelClick() {

                    }
                }).show(getFragmentManager());


            }



        });

        edit_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_account.getText().equals("Edit")){
                    edit_account.setText("Save");
                    firstname.setEnabled(true);
                    lastname.setEnabled(true);
                    id_number.setEnabled(true);
                    username.setEnabled(true);
                    password.setEnabled(true);

                }else{
                    edit_account.setText("Edit");
                    firstname.setEnabled(false);
                    lastname.setEnabled(false);
                    id_number.setEnabled(false);
                    username.setEnabled(false);
                    password.setEnabled(false);
                    //save here
                }
            }
        });

        edit_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_email.getText().equals("Edit")){
                    edit_email.setText("Save");
                    email.setEnabled(true);
                }else{
                    edit_email.setText("Edit");
                    email.setEnabled(false);
                    //save here
                }
            }
        });

        edit_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_phone.getText().equals("Edit")){
                    edit_phone.setText("Save");
                    phonenumber.setEnabled(true);
                }else{
                    edit_phone.setText("Edit");
                    phonenumber.setEnabled(false);
                }
            }
        });



    }

    private void requestHTTP(Commands command){
        final Commands comm = command;
        JsonObjectRequest postRequest;
        JSONObject params = new JSONObject();

        switch (comm){
            case INIT_PROFILE:
                try {
                    params.put("Acc_ID",Account_ID);
                }catch(Exception e){
                    Log.i("Error.Response", e.toString());
                }
                url = "https://requench-rest.herokuapp.com/Fetch_Image.php";
                postRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String fetched_base_64 = null;
                        try {
                            fetched_base_64 = response.getString("image");
                            byte[] decodedString = Base64.decode(fetched_base_64, Base64.DEFAULT);
                            Bitmap decodedimage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            profile_image.setImageBitmap(decodedimage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity().getApplicationContext(),"An Error Occured" + error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
                requestqueue.add(postRequest);
                break;

            case EDIT_PROFILE:
                break;
            case UPLOAD_IMAGE:
                try {
                    params.put("Acc_ID",Account_ID);
                    params.put("image_string",image_str);
                    params.put("file_name",file_name);
                }catch(Exception e){
                    Log.i("Error.Response", e.toString());
                }
                url = "https://requench-rest.herokuapp.com/Upload_Image.php";

                postRequest = new JsonObjectRequest(Request.Method.POST, url, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getContext(),"Image Uploaded",Toast.LENGTH_SHORT).show();
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.detach(getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container))
                                .attach(getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container))
                                .commit();
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
