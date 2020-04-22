package com.aknayak.offchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.aknayak.offchat.datas.DBHelper;
import com.aknayak.offchat.usersViewConcact.users.contactsUser;
import com.aknayak.offchat.usersViewConcact.users.contactsUserAdapter;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.aknayak.offchat.MainActivity.requestPermission;
import static com.aknayak.offchat.globaldata.respData.cntCode;
import static com.aknayak.offchat.globaldata.respData.getAllContacts;


public class AllConcacts extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_READ_CONTACTS = 79;
    public ArrayList<contactsUser> contactsUsers = new ArrayList<contactsUser>();
    public ArrayList<contactsUser> mobileArray = new ArrayList<>();
    public RecyclerView rvUser;
    ProgressBar usersLoadProgressBar;
    ImageButton closeButton;
    EditText searchBox;
    contactsUserAdapter adapter;
    ImageButton mReloadButton;
    ConstraintLayout mLogoLayout;
    ImageButton mSearchButton;
    DBHelper mydb;
    Thread t;
    Thread t2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_contacts);



        mReloadButton = findViewById(R.id.reloadfloatButton);
        usersLoadProgressBar = findViewById(R.id.progressBar);
        rvUser = findViewById(R.id.contactsRecyclerView);
        searchBox = findViewById(R.id.searchBox);
        closeButton = findViewById(R.id.closeButton);
        mSearchButton = findViewById(R.id.searchButton);
        mLogoLayout = findViewById(R.id.contact_info_logo);
        usersLoadProgressBar.setVisibility(View.INVISIBLE);
        usersLoadProgressBar.getIndeterminateDrawable().setColorFilter(Color.rgb(133, 94, 238), PorterDuff.Mode.MULTIPLY);


        mydb = new DBHelper(this);
        mobileArray = mydb.getAllCotacts();

        closeButton.setOnClickListener(this);
        mSearchButton.setOnClickListener(this);
        mReloadButton.setOnClickListener(this);

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

        if (mobileArray.size() <= 1) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED) {
                mReloadButton.performClick();
            } else {
                requestPermission(this);
            }

        } else {
            loadContacts(2);
            rvUser.scrollToPosition(contactsUsers.size() - 1);
        }



    }


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
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    mobileArray = getAllContacts();
                } else {
                    // permission denied,Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    public void onClick(View v) {
        InputMethodManager imm;
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        switch (v.getId()) {
            case R.id.closeButton:
                mReloadButton.setVisibility(View.VISIBLE);
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
                mReloadButton.setVisibility(View.GONE);
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
            case R.id.reloadfloatButton:
                t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mobileArray != null) {
                            mobileArray.clear();
                        }
                        mobileArray.addAll(getAllContacts(getContentResolver()));
                        mydb.deleteAllContact();
                        for (int i = 0; i < mobileArray.size(); i++) {
                            mydb.insertContact(mobileArray.get(i).getUserName(), mobileArray.get(i).getPhoneNumber());
                        }
                    }
                });

                t2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            t.join();
                            usersLoadProgressBar.setVisibility(View.INVISIBLE);
                            Intent i = new Intent(getApplicationContext(), AllConcacts.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            AllConcacts.super.finish();
                            startActivity(i);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                t.start();
                t2.start();

                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
                mSearchButton.setEnabled(false);
                mReloadButton.startAnimation(animation);
                mReloadButton.setEnabled(false);
                rvUser.setVisibility(View.INVISIBLE);
                usersLoadProgressBar.setVisibility(View.VISIBLE);
                break;
        }
    }
}