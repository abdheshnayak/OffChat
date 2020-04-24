package com.aknayak.offchat.users;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.aknayak.offchat.R;
import com.aknayak.offchat.datas.DBHelper;
import com.aknayak.offchat.messageViewActivity;
import com.aknayak.offchat.messages.Message;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.aknayak.offchat.MainActivity.ROOT_CHILD;
import static com.aknayak.offchat.MainActivity.getRoot;
import static com.aknayak.offchat.MainActivity.senderUserName;
import static com.aknayak.offchat.globaldata.AESHelper.decrypt;
import static com.aknayak.offchat.globaldata.respData.MESSAGES_CHILD;

/**
 * OffChat
 * Created by Abdhesh Nayak on 3/18/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/

public class userAdapter extends RecyclerView.Adapter<userAdapter.userViewHolder> {

    @NonNull
    @Override
    public userViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        final View userView;
        userView = inflater.inflate(R.layout.user_item, parent, false);
        // Return a new holder instance
        userView.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                return false;
            }
        });
        userView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RecyclerView rv = parrentActivity.findViewById(R.id.recyclerView);
                int pos = rv.getChildLayoutPosition(v);
                Message message = mMessages.get(pos);


                Intent i = new Intent(userView.getContext(), messageViewActivity.class);

                i.putExtra("phoneNumber", message.getMessageSource().equals(senderUserName) ? message.getMessageFor() : message.getMessageSource());

                parrentActivity.startActivity(i);
            }
        });
        userViewHolder viewHolder = new userViewHolder(userView);
        return viewHolder;

    }

    private int unseen;

    @Override
    public void onBindViewHolder(userAdapter.userViewHolder viewHolder, int position) {
        final Message message = mMessages.get(position);
        // Set item views based on your views and data model
        final TextView seenStatusWaiting = viewHolder.sentStatusWaiting;
        final TextView seenStatusSingle = viewHolder.sentStatusSingle;
        final TextView seenStatusDouble = viewHolder.sentStatusSingleDouble;
        final TextView seenStatusDoubleBlue = viewHolder.sentStatusSingleDoubleBlue;
        TextView textView = viewHolder.usernameTextView;
        final DBHelper mydb = new DBHelper(parrentActivity);
        textView.setText(mydb.getUserName(message.getMessageSource().equals(senderUserName) ? message.getMessageFor() : message.getMessageSource()));
        final TextView lastMessageTextView = viewHolder.lastMessageTextView;
        String messagedec = decrypt(message.getMessage());
        try {
            if (messagedec.length() > 25) {
                lastMessageTextView.setText(messagedec.substring(0, 25) + "...");
            } else {
                lastMessageTextView.setText(messagedec);
            }
        } catch (Exception e) {
//            Log.d("Info", e.getMessage());
        }

        textView = viewHolder.lastMessageTimeTextView;
        DateFormat df = new SimpleDateFormat("hh:mm aa");
        String strDate = df.format(message.getMessageSentTime());
        textView.setText(strDate);
        final TextView unseenTextView = viewHolder.unseencount;

        final String rootPath = getRoot(senderUserName, message.getMessageSource().equals(senderUserName) ? message.getMessageFor() : message.getMessageSource());
        unseen = mydb.getUnseenCount(getRoot(senderUserName, message.getMessageSource().equals(senderUserName) ? message.getMessageFor() : message.getMessageSource()), message.getMessageSource().equals(senderUserName) ? message.getMessageFor() : message.getMessageSource());

//        Log.d("BBBB","kk"+unseen);
        uiUpdate(message, seenStatusSingle, seenStatusDouble, seenStatusDoubleBlue, lastMessageTextView, unseenTextView, seenStatusWaiting);

        final DatabaseReference fdbr = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(rootPath);


    }

    public void uiUpdate(Message message, TextView seenStatusSingle, TextView seenStatusDouble, TextView seenStatusDoubleBlue, TextView lastMessageTextView, TextView unseenTextView, TextView seenStatusWaiting) {
        if (unseen == 0) {
            if (message.getMessageSource().equals(senderUserName)) {
                if (message.getMessageStatus() == 0) {
//                    Log.d("kkk"+message.getMessage(),""+message.getMessageStatus());
                    seenStatusWaiting.setVisibility(View.VISIBLE);
                    seenStatusDoubleBlue.setVisibility(View.GONE);
                    seenStatusDouble.setVisibility(View.GONE);
                    seenStatusSingle.setVisibility(View.GONE);
                } else if (message.getMessageStatus() == 1) {
                    seenStatusWaiting.setVisibility(View.GONE);
                    seenStatusDoubleBlue.setVisibility(View.GONE);
                    seenStatusDouble.setVisibility(View.GONE);
                    seenStatusSingle.setVisibility(View.VISIBLE);
                } else if (message.getMessageStatus() == 2) {
                    seenStatusWaiting.setVisibility(View.GONE);
                    seenStatusDoubleBlue.setVisibility(View.GONE);
                    seenStatusSingle.setVisibility(View.GONE);
                    seenStatusDouble.setVisibility(View.VISIBLE);
                } else if (message.getMessageStatus() == 3) {
                    seenStatusWaiting.setVisibility(View.GONE);
                    seenStatusDoubleBlue.setVisibility(View.VISIBLE);
                    seenStatusDouble.setVisibility(View.GONE);
                    seenStatusSingle.setVisibility(View.GONE);
                }
            } else {
                lastMessageTextView.setVisibility(View.VISIBLE);
                lastMessageTextView.setTypeface(lastMessageTextView.getTypeface(), Typeface.NORMAL);
                seenStatusDoubleBlue.setVisibility(View.GONE);
                seenStatusDouble.setVisibility(View.GONE);
                seenStatusSingle.setVisibility(View.GONE);
            }
            lastMessageTextView.setTypeface(lastMessageTextView.getTypeface(), Typeface.NORMAL);
            unseenTextView.setVisibility(View.GONE);
        } else {
            seenStatusSingle.setVisibility(View.GONE);
            seenStatusDouble.setVisibility(View.GONE);
            seenStatusDoubleBlue.setVisibility(View.GONE);
            unseenTextView.setVisibility(View.VISIBLE);
            lastMessageTextView.setTypeface(lastMessageTextView.getTypeface(), Typeface.BOLD);
            unseenTextView.setText(String.valueOf(unseen));
        }
    }


    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public class userViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        TextView lastMessageTextView;
        TextView lastMessageTimeTextView;
        CircleImageView messengerImageView;
        TextView sentStatusSingle;
        TextView sentStatusSingleDouble;
        TextView sentStatusSingleDoubleBlue;
        TextView sentStatusWaiting;
        TextView unseencount;


        public userViewHolder(View v) {
            super(v);
            usernameTextView = itemView.findViewById(R.id.uiUserName);
            lastMessageTextView = itemView.findViewById(R.id.lastMessage);
            lastMessageTimeTextView = itemView.findViewById(R.id.time_detail);
            messengerImageView = itemView.findViewById(R.id.dp_user);

            sentStatusSingle = itemView.findViewById(R.id.sentStatus);
            sentStatusSingleDouble = itemView.findViewById(R.id.sentStatusDouble);
            sentStatusSingleDoubleBlue = itemView.findViewById(R.id.sentStatusDoubleBlue);

            sentStatusWaiting = itemView.findViewById(R.id.sentStatusWaiting);
            unseencount = itemView.findViewById(R.id.unseenCount);
        }
    }

    private List<Message> mMessages;
    AppCompatActivity parrentActivity;

    public userAdapter(List<Message> mMessages, AppCompatActivity a) {
        this.mMessages = mMessages;
        this.parrentActivity = a;
    }
}
