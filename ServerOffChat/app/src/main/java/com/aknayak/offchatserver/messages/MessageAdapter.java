package com.aknayak.offchatserver.messages;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aknayak.offchatserver.MainActivity;
import com.aknayak.offchatserver.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


/**
 * OffChat
 * Created by Abdhesh Nayak on 3/18/20
 * abdheshnayak@gmail.com
 * Copyright (c) 2020 OffChat All rights reserved.
 **/

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        final View messageView;
        messageView = inflater.inflate(R.layout.message_view, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(messageView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MessageAdapter.ViewHolder viewHolder, int position) {
        final Message message = mMessage.get(position);

        // Set item views based on your views and data model

        TextView s = viewHolder.Sender;
        s.setText(message.getMessageSource());
        s = viewHolder.Receiver;
        s.setText(message.getMessageFor());
        s = viewHolder.stime;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy\nhh:mm:ss aa",Locale.ENGLISH);
        s.setText(simpleDateFormat.format(message.getMessageSentTime()));
        s= viewHolder.msgId;
        s.setText("\uD83C\uDD94 "+message.getMessageID());
        s= viewHolder.snLabel;
        s.setText((mMessage.size()-position)+".");
    }

    @Override
    public int getItemCount() {
        return mMessage.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        TextView Sender;
        TextView Receiver;
        TextView stime;
        TextView msgId;
        TextView snLabel;
        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            snLabel = itemView.findViewById(R.id.sn);
            Sender = itemView.findViewById(R.id.sender);
            Receiver = itemView.findViewById(R.id.receiver);
            stime = itemView.findViewById(R.id.stime);
            msgId = itemView.findViewById(R.id.messageId);
        }
    }

    private List<Message> mMessage;

    private MainActivity activity;

    public MessageAdapter(List<Message> mMessage, MainActivity a) {
        this.mMessage = mMessage;
        this.activity = a;
    }


}