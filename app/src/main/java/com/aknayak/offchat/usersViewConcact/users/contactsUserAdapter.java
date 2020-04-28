package com.aknayak.offchat.usersViewConcact.users;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.aknayak.offchat.R;
import com.aknayak.offchat.messageViewActivity;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * OffChat
 * Created by Abdhesh Nayak on 3/18/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/


public class contactsUserAdapter extends RecyclerView.Adapter<contactsUserAdapter.userViewHolder> {

    private static final String USERRELATION_CHILD = "userRelations";
    private DatabaseReference mFirebaseDatabaseReference;

    @NonNull
    @Override
    public userViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        final View userView;
        userView = inflater.inflate(R.layout.contacts_item_user, parent, false);
        // Return a new holder instance
        userView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView userNameView = userView.findViewById(R.id.contactsUserName);
                TextView phoneNumberView = userView.findViewById(R.id.cvuiPhoneNumber);

                String stringUserName = userNameView.getText().toString().trim();
                String stringPhoneNumber = phoneNumberView.getText().toString().trim();

                Intent i = new Intent(userView.getContext(), messageViewActivity.class);

                i.putExtra("userName",stringUserName);
                i.putExtra("phoneNumber",stringPhoneNumber);


                parrentActivity.startActivity(i);
                parrentActivity.finish();
            }
        });
        userViewHolder viewHolder = new userViewHolder(userView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(contactsUserAdapter.userViewHolder viewHolder, int position) {
        contactsUser contactsUser = mContactsUser.get(position);

        // Set item views based on your views and data model
        TextView textView = viewHolder.usernameTextView;
        textView.setText(contactsUser.getUserName());

        TextView textView1 = viewHolder.phoneNumberTextView;
        textView1.setText(contactsUser.getPhoneNumber());
        ImageView imgv = viewHolder.ispresent;
        if (contactsUser.getPresent()){
            imgv.setVisibility(View.VISIBLE);
        }else {
            imgv.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mContactsUser.size();
    }

    public class userViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        TextView phoneNumberTextView;
        CircleImageView messengerImageView;
        ImageView ispresent;

        public userViewHolder(View v) {
            super(v);
            ispresent = itemView.findViewById(R.id.active);
            usernameTextView = itemView.findViewById(R.id.contactsUserName);
            phoneNumberTextView = itemView.findViewById(R.id.cvuiPhoneNumber);
            messengerImageView = itemView.findViewById(R.id.dp_user);
        }
    }

    private List<contactsUser> mContactsUser;
    AppCompatActivity parrentActivity;
    public contactsUserAdapter(List<contactsUser> mContactsUser, AppCompatActivity a) {
        this.mContactsUser = mContactsUser;
        this.parrentActivity = a;
    }
}
