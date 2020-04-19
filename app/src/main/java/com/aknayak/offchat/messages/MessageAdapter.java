package com.aknayak.offchat.messages;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aknayak.offchat.MainActivity;
import com.aknayak.offchat.R;
import com.aknayak.offchat.datas.DBHelper;
import com.aknayak.offchat.users.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.aknayak.offchat.MainActivity.ROOT_CHILD;
import static com.aknayak.offchat.MainActivity.getRoot;
import static com.aknayak.offchat.MainActivity.receiverUsername;
import static com.aknayak.offchat.MainActivity.senderUserName;
import static com.aknayak.offchat.messageViewActivity.MAINVIEW_CHILD;
import static com.aknayak.offchat.messageViewActivity.MESSAGES_CHILD;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    @Override
    public int getItemViewType(int position) {
        if (mMessage.get(position).getMessageSource().equals(MainActivity.senderUserName)) {
            return 1;
        } else {
            return 2;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View messageView;
        if(viewType == 1) {
            // Inflate the custom layout
            messageView = inflater.inflate(R.layout.sender_messagebox, parent, false);
        }else {
            messageView = inflater.inflate(R.layout.receiver_messagebox, parent, false);
        }

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(messageView);
        return viewHolder;


    }

    @Override
    public void onBindViewHolder(MessageAdapter.ViewHolder viewHolder, int position) {
        final Message message = mMessage.get(position);

        // Set item views based on your views and data model
        TextView textView = viewHolder.Message;
        textView.setText(message.getMessage());
        textView = viewHolder.messageSentTime;
        DateFormat df= new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
        String strDate = df.format(message.getMessageSentTime());
        textView.setText(strDate);
        final String rootPath = getRoot(senderUserName,receiverUsername);

        final TextView waitingForSent = viewHolder.watitingForSent;
        final TextView seenStatusSingle = viewHolder.sentStatusSingle;
        final TextView seenStatusDouble = viewHolder.sentStatusSingleDouble;
        final TextView seenStatusDoubleBlue = viewHolder.sentStatusSingleDoubleBlue;


        final DBHelper mydb = new DBHelper(parentActivity);

        if (message.getMessageStatus()==3 && message.getMessageSource().equals(senderUserName) ) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
                    try {
                        if (!message.getMessageID().equals(mydb.getlastMessages(rootPath).getMessageID())){
                            FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(rootPath).child(message.getMessageID()).removeValue();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
//                    }
                }
//            }).start();
        }
        if (message.getMessageStatus()<3 && message.getMessageSource().equals(receiverUsername) ) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
                    DatabaseReference fdbr = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(rootPath).child(message.getMessageID());
                    message.setMessageStatus(3);
                    fdbr.updateChildren(message.toMap());
//                        fdbr.setValue(3);
//                        try {
//                            if (!mydb.getlastMessages(rootPath).getMessageID().equals(message.getMessageID())){
////                                FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(rootPath).child(message.getMessageID()).removeValue();
//                            }
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                        FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(rootPath).child(message.getMessageID()).removeValue();

                    DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MAINVIEW_CHILD).child(receiverUsername).child(senderUserName);
                    User user = null;
                    try {
                        user = new User(senderUserName, Calendar.getInstance(Locale.ENGLISH).getTime(), mydb.getlastMessages(rootPath).getMessage(), "no", 3);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    mFirebaseDatabaseReference.updateChildren(user.toMap());
                }
//            }).start();
//        }

        if (message.getMessageSource().equals(receiverUsername)){
            if (message.getMessageStatus()==1) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
                        message.setMessageStatus(2);
                        DatabaseReference fdbr = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(rootPath).child(message.getMessageID());
                        fdbr.updateChildren(message.toMap());
                    }
//                }).start();
//            }
        }
        if (message.getMessageSource().equals(senderUserName)){
            uiUpdate(message,seenStatusSingle,seenStatusDouble,seenStatusDoubleBlue,waitingForSent);
        }
    }

    @Override
    public int getItemCount() {
        return mMessage.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView Message;
        public TextView messageSentTime;

        TextView watitingForSent;
        TextView sentStatusSingle;
        TextView sentStatusSingleDouble;
        TextView sentStatusSingleDoubleBlue;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            Message = itemView.findViewById(R.id.textView);
            messageSentTime = itemView.findViewById(R.id.sentTime);

            watitingForSent = itemView.findViewById(R.id.waitingForSent);
            sentStatusSingle= itemView.findViewById(R.id.sentStatus_message);
            sentStatusSingleDouble= itemView.findViewById(R.id.sentStatusDouble_message);
            sentStatusSingleDoubleBlue= itemView.findViewById(R.id.sentStatusDoubleBlue_message);

        }
    }

    private List<Message> mMessage;
    private Activity parentActivity;

    public MessageAdapter(List<Message> mMessage, Activity a) {
        this.parentActivity = a;
        this.mMessage = mMessage;
    }

    public void uiUpdate(Message message, TextView seenStatusSingle, TextView seenStatusDouble, TextView seenStatusDoubleBlue, TextView waitingForSent) {
        if (message.getMessageStatus() == 1) {
            seenStatusDoubleBlue.setVisibility(View.GONE);
            seenStatusDouble.setVisibility(View.GONE);
            seenStatusSingle.setVisibility(View.VISIBLE);
            waitingForSent.setVisibility(View.GONE);
        } else if (message.getMessageStatus() == 2) {
            seenStatusDoubleBlue.setVisibility(View.GONE);
            seenStatusSingle.setVisibility(View.GONE);
            seenStatusDouble.setVisibility(View.VISIBLE);
            waitingForSent.setVisibility(View.GONE);
        } else if (message.getMessageStatus() == 3) {
            seenStatusDoubleBlue.setVisibility(View.VISIBLE);
            seenStatusDouble.setVisibility(View.GONE);
            seenStatusSingle.setVisibility(View.GONE);
            waitingForSent.setVisibility(View.GONE);
        }else if (message.getMessageStatus() == 0 ){
            waitingForSent.setVisibility(View.VISIBLE);
            seenStatusDoubleBlue.setVisibility(View.GONE);
            seenStatusDouble.setVisibility(View.GONE);
            seenStatusSingle.setVisibility(View.GONE);
        }
    }
}