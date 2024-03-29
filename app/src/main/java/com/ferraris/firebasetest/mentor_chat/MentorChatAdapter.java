package com.ferraris.firebasetest.mentor_chat;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.ferraris.firebasetest.R;
import com.ferraris.firebasetest.models.Message;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class MentorChatAdapter extends FirestoreRecyclerAdapter<Message, MessageViewHolder> {

    public interface Listener {
        void onDataChanged();
    }

    //FOR DATA
    private final RequestManager glide;
    private final String idCurrentUser;

    //FOR COMMUNICATION
    private Listener callback;

    public MentorChatAdapter(@NonNull FirestoreRecyclerOptions<Message> options, RequestManager glide, Listener callback, String idCurrentUser) {
        super(options);
        this.glide = glide;
        this.callback = callback;
        this.idCurrentUser = idCurrentUser;
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull Message model) {
        holder.updateWithMessage(model, this.idCurrentUser, this.glide);
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_mentor_chat_item, parent, false));
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        this.callback.onDataChanged();
    }
}
