package com.aknayak.offchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aknayak.offchat.datas.DBHelper;
import com.aknayak.offchat.globaldata.respData;
import com.aknayak.offchat.services.loadCont;
import com.aknayak.offchat.usersViewConcact.users.contactsUser;
import com.aknayak.offchat.usersViewConcact.users.contactsUserAdapter;

import java.text.ParseException;
import java.util.ArrayList;

import static com.aknayak.offchat.globaldata.Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS;
import static com.aknayak.offchat.globaldata.Constants.PREF_DATA;
import static com.aknayak.offchat.globaldata.respData.IS_PERMISSIONS_REQUEST_READ_CONTACTS;
import static com.aknayak.offchat.globaldata.respData.getAllContacts;
import static com.aknayak.offchat.globaldata.respData.requestPermission;

/**
 * OffChat
 * Created by Abdhesh Nayak on 3/18/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/


public class AllConcacts extends AppCompatActivity implements View.OnClickListener {

    public ArrayList<contactsUser> contactsUsers = new ArrayList<contactsUser>();
    public ArrayList<contactsUser> mobileArray = new ArrayList<>();
    public RecyclerView rvUser;
    ProgressBar usersLoadProgressBar;
    ImageButton closeButton;
    EditText searchBox;
    contactsUserAdapter adapter;
    ConstraintLayout mLogoLayout;
    ImageButton mSearchButton;
    DBHelper mydb;
    Thread t2;
    Thread t1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_contacts);


        rvUser = findViewById(R.id.contactsRecyclerView);
        searchBox = findViewById(R.id.searchBox);
        closeButton = findViewById(R.id.closeButton);
        mSearchButton = findViewById(R.id.searchButton);
        mLogoLayout = findViewById(R.id.contact_info_logo);


        mydb = new DBHelper(this);
        mobileArray = mydb.getAllCotacts();

        closeButton.setOnClickListener(this);
        mSearchButton.setOnClickListener(this);

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateListData(searchBox.getText().toString().trim());
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.msgContainer).setVisibility(View.VISIBLE);
            }
        },1000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.msgContainer).setVisibility(View.GONE);
            }
        },3000);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                requestPermission(AllConcacts.this);
                if (IS_PERMISSIONS_REQUEST_READ_CONTACTS) {
                    t1 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            loadCont.loadCont(getApplicationContext());
//                            mSearchButton.setEnabled(true);
//                            mReloadButton.setEnabled(true);
                        }
                    });

                    t2 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                t1.join();
//                            adapter.notifyDataSetChanged();
//                                usersLoadProgressBar.setVisibility(View.INVISIBLE);
                                loadCont.loadCont(getApplicationContext());
                                swipeContainer.setRefreshing(false);
                                Intent i = new Intent(getApplicationContext(), AllConcacts.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                AllConcacts.super.finish();
                                startActivity(i);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    t1.start();
                    t2.start();

//                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
//                    mSearchButton.setEnabled(false);
//                    mReloadButton.startAnimation(animation);
//                    mReloadButton.setEnabled(false);
                }
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        if (mobileArray.size() == 0) {
//            Toast.makeText(getApplicationContext(),"hello",Toast.LENGTH_SHORT).show();
            SharedPreferences sharedPreferences = getSharedPreferences(PREF_DATA, Context.MODE_PRIVATE);
            if (sharedPreferences.contains("allcontacts")) {
                int k = sharedPreferences.getInt("allcontacts", 0);
                if (k < 2) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("allcontacts", k + 1);
                    editor.apply();
                }
            }
        } else {
            loadContacts(2);
            rvUser.scrollToPosition(contactsUsers.size() - 1);
        }


    }

    private SwipeRefreshLayout swipeContainer;

    private void updateListData(String trim) {

        mobileArray.clear();
        mobileArray.addAll(mydb.getData(trim));

        // Start long running operation in a background thread

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(false);
        rvUser.setLayoutManager(linearLayoutManager);
        contactsUsers.addAll(mobileArray);


        adapter.notifyDataSetChanged();

    }

    public void loadContacts(int x) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            if (x == 1) {
                mobileArray.clear();
                mobileArray.addAll(getAllContacts(getContentResolver()));
            }
        } else {
            requestPermission(this);
        }

        adapter = new contactsUserAdapter(mobileArray, this);
        // Attach the adapter to the recyclerview to populate items
        rvUser.setAdapter(adapter);
        // Set layout manager to position the items
        rvUser.setLayoutManager(new LinearLayoutManager(this));

        // Start long running operation in a background thread

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        rvUser.setLayoutManager(linearLayoutManager);

        contactsUsers.addAll(mobileArray);
        adapter.notifyDataSetChanged();

    }


    @Override
    public void onClick(View v) {
        InputMethodManager imm;
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        switch (v.getId()) {
            case R.id.closeButton:
                searchBox.getText().clear();
                imm.hideSoftInputFromWindow(searchBox.getWindowToken(),
                        InputMethodManager.RESULT_UNCHANGED_SHOWN);
                closeButton.setVisibility(View.GONE);
                closeButton.animate().translationX(200);
                searchBox.clearFocus();
                searchBox.setVisibility(View.GONE);
                searchBox.animate().translationX(900);
                mLogoLayout.animate().translationX(0);
                mLogoLayout.setVisibility(View.VISIBLE);
                mSearchButton.setVisibility(View.VISIBLE);
                mSearchButton.animate().translationX(0);
                break;
            case R.id.searchButton:
                mLogoLayout.setVisibility(View.GONE);
                mLogoLayout.animate().translationX(-200);
                mSearchButton.animate().translationX(-200);
                mSearchButton.setVisibility(View.GONE);
                searchBox.setTranslationX(900);
                searchBox.setVisibility(View.VISIBLE);
                searchBox.animate().translationX(0);
                closeButton.setVisibility(View.VISIBLE);
                closeButton.animate().translationX(0);
                searchBox.requestFocus();
                imm.showSoftInput(searchBox,
                        InputMethodManager.RESULT_UNCHANGED_SHOWN);
                break;
//            case R.id.reloadfloatButton:
//
//                requestPermission(AllConcacts.this);
//                if (IS_PERMISSIONS_REQUEST_READ_CONTACTS) {
//                    t1 = new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            loadCont.loadCont(getApplicationContext());
//                        }
//                    });
//
//                    t2 = new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                t1.join();
////                            adapter.notifyDataSetChanged();
////                                usersLoadProgressBar.setVisibility(View.INVISIBLE);
//                                Intent i = new Intent(getApplicationContext(), AllConcacts.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                                AllConcacts.super.finish();
//                                startActivity(i);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                    t1.start();
//                    t2.start();
//
//                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
//                    mSearchButton.setEnabled(false);
//                    mReloadButton.startAnimation(animation);
//                    mReloadButton.setEnabled(false);
//                    rvUser.setVisibility(View.INVISIBLE);
//                    usersLoadProgressBar.setVisibility(View.VISIBLE);
//
//                } else {
//                    AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(AllConcacts.this);
//                    alertDialogBuilder2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                        }
//                    });
//                    alertDialogBuilder2.setTitle("Contacts Permision");
//                    alertDialogBuilder2.setMessage("we don't have your contacts permissions. please Give Contact Permission to the app.");
//                    alertDialogBuilder2.show();
//                }
//
//                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    respData.IS_PERMISSIONS_REQUEST_READ_CONTACTS = true;
                } else {
                    // permission denied, boo! Disable the
                    respData.IS_PERMISSIONS_REQUEST_READ_CONTACTS = false;
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

}