package com.aknayak.offchat.users;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.aknayak.offchat.MainActivity;
import com.aknayak.offchat.R;
import com.aknayak.offchat.datas.DBHelper;
import com.aknayak.offchat.messageViewActivity;
import com.aknayak.offchat.messages.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.aknayak.offchat.MainActivity.ROOT_CHILD;
import static com.aknayak.offchat.MainActivity.getRoot;
import static com.aknayak.offchat.MainActivity.receiverUsername;
import static com.aknayak.offchat.MainActivity.senderUserName;
import static com.aknayak.offchat.messageViewActivity.MAINVIEW_CHILD;
import static com.aknayak.offchat.messageViewActivity.MESSAGES_CHILD;

public class userAdapter extends RecyclerView.Adapter<userAdapter.userViewHolder> {

    String tempMessage;
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
//                Log.d("kels","lksd");
                return false;
            }
        });
        userView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RecyclerView rv = parrentActivity.findViewById(R.id.recyclerView);
                int pos = rv.getChildLayoutPosition(v);
                User user = mUser.get(pos);

                TextView userNameView = userView.findViewById(R.id.uiUserName);
//                TextView phoneNumberView = userView.findViewById(R.id.);
                String stringUserName = userNameView.getText().toString().trim();

                Intent i = new Intent(userView.getContext(), messageViewActivity.class);

//                i.putExtra("userName","hello");
//                i.putExtra("phoneNumber",stringUserName);

                i.putExtra("phoneNumber", user.getUserName());

                parrentActivity.startActivity(i);
            }
        });
        userViewHolder viewHolder = new userViewHolder(userView);
        return viewHolder;

    }

    private int unseen;

    @Override
    public void onBindViewHolder(userAdapter.userViewHolder viewHolder, int position) {
        final User user = mUser.get(position);
        // Set item views based on your views and data model
        final TextView seenStatusSingle = viewHolder.sentStatusSingle;
        final TextView seenStatusDouble = viewHolder.sentStatusSingleDouble;
        final TextView seenStatusDoubleBlue = viewHolder.sentStatusSingleDoubleBlue;
        TextView textView = viewHolder.usernameTextView;
        final DBHelper mydb = new DBHelper(parrentActivity);
        textView.setText(mydb.getUserName(user.getUserName()));
        final TextView lastMessageTextView = viewHolder.lastMessageTextView;
        try {
            if (user.getLastMessage().length() > 25) {
                lastMessageTextView.setText(user.getLastMessage().substring(0, 25) + "...");
            } else {
                lastMessageTextView.setText(user.getLastMessage());
            }
        } catch (Exception e) {
            Log.d("Info", e.getMessage());
        }

        textView = viewHolder.lastMessageTimeTextView;
        DateFormat df = new SimpleDateFormat("hh:mm aa");
        String strDate = df.format(user.getLastMessageSentTime());
        textView.setText(strDate);
        final TextView unseenTextView = viewHolder.unseencount;
        final String rootPath = getRoot(senderUserName,user.getUserName());
        unseen = mydb.getUnseenCount(getRoot(senderUserName, user.getUserName()), user.getUserName());
        uiUpdate(user,seenStatusSingle,seenStatusDouble,seenStatusDoubleBlue,lastMessageTextView,unseenTextView);


        final DatabaseReference fdbr = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MESSAGES_CHILD).child(rootPath);
        fdbr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Message message = snapshot.getValue(Message.class);
                    if (message.getMessageStatus() == 1 && message.getMessageSource().equals(user.getUserName())){
                        message.setMessageStatus(2);
                        fdbr.child(snapshot.getKey()).child("messageStatus").setValue(2);

                        //                Updating History Of Sender
                        DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(ROOT_CHILD).child(MAINVIEW_CHILD).child(user.getUserName()).child(senderUserName).child("sentStatus");
                        mFirebaseDatabaseReference.setValue(2);
                    }
                        mydb.insertMessage(message.getMessage(),message.getMessageSource(),message.getMessageSentTime(),message.getMessageStatus(),snapshot.getKey(),rootPath);
                }
                unseen = mydb.getUnseenCount(getRoot(senderUserName, user.getUserName()), user.getUserName());
                uiUpdate(user,seenStatusSingle,seenStatusDouble,seenStatusDoubleBlue,lastMessageTextView,unseenTextView);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void uiUpdate(User user, TextView seenStatusSingle, TextView seenStatusDouble, TextView seenStatusDoubleBlue, TextView lastMessageTextView, TextView unseenTextView) {
//        Toast.makeText(parrentActivity.getApplicationContext(),""+user.getSentStatus(),Toast.LENGTH_SHORT).show();
        if (unseen == 0 ) {
            if (user.getSentStatus() == 1) {
                seenStatusDoubleBlue.setVisibility(View.GONE);
                seenStatusDouble.setVisibility(View.GONE);
                seenStatusSingle.setVisibility(View.VISIBLE);
            } else if (user.getSentStatus() == 2) {
                seenStatusDoubleBlue.setVisibility(View.GONE);
                seenStatusSingle.setVisibility(View.GONE);
                seenStatusDouble.setVisibility(View.VISIBLE);
            } else if (user.getSentStatus() == 3) {
                seenStatusDoubleBlue.setVisibility(View.VISIBLE);
                seenStatusDouble.setVisibility(View.GONE);
                seenStatusSingle.setVisibility(View.GONE);
            }else if (user.getSentStatus() == 0 ){
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
        return mUser.size();
    }

    public class userViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        TextView lastMessageTextView;
        TextView lastMessageTimeTextView;
        CircleImageView messengerImageView;
        TextView sentStatusSingle;
        TextView sentStatusSingleDouble;
        TextView sentStatusSingleDoubleBlue;
        TextView unseencount;
        public userViewHolder(View v) {
            super(v);
            usernameTextView = itemView.findViewById(R.id.uiUserName);
            lastMessageTextView = itemView.findViewById(R.id.lastMessage);
            lastMessageTimeTextView = itemView.findViewById(R.id.time_detail);
            messengerImageView = itemView.findViewById(R.id.dp_user);

            sentStatusSingle= itemView.findViewById(R.id.sentStatus);
            sentStatusSingleDouble= itemView.findViewById(R.id.sentStatusDouble);
            sentStatusSingleDoubleBlue= itemView.findViewById(R.id.sentStatusDoubleBlue);

            unseencount = itemView.findViewById(R.id.unseenCount);
        }
    }

    private List<User> mUser;
    AppCompatActivity parrentActivity;
    public userAdapter(List<User> mUser, AppCompatActivity a) {
        this.mUser = mUser;
        this.parrentActivity = a;
    }
}
