package com.ferraris.firebasetest.mentor_chat;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ferraris.firebasetest.R;
import com.ferraris.firebasetest.api.MessageHelper;
import com.ferraris.firebasetest.api.UserHelper;
import com.ferraris.firebasetest.base.BaseActivity;
import com.ferraris.firebasetest.models.Message;
import com.ferraris.firebasetest.models.User;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MentorChatActivity extends BaseActivity implements MentorChatAdapter.Listener {

    // FOR DESIGN

    @BindView(R.id.activity_mentor_chat_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.activity_mentor_chat_text_view_recycler_view_empty)
    TextView textViewRecyclerViewEmpty;
    @BindView(R.id.activity_mentor_chat_message_edit_text)
    TextInputEditText editTextMessage;
    @BindView(R.id.activity_mentor_chat_image_chosen_preview)
    ImageView imageViewPreview;

    // FOR DATA

    private MentorChatAdapter mentorChatAdapter;
    @Nullable
    private User modelCurrentUser;
    private String currentChatName;
    private Uri uriImageSelected;

    // STATIC DATA FOR CHAT
    private static final String CHAT_NAME_ANDROID = "android";
    private static final String CHAT_NAME_BUG = "bug";
    private static final String CHAT_NAME_FIREBASE = "firebase";

    // STATIC DATA FOR PICTURE
    private static final String PERMS = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final int RC_IMAGE_PERMS = 100;
    private static final int RC_CHOOSE_PHOTO = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.configureRecyclerView(CHAT_NAME_ANDROID);
        this.configureToolbar();
        this.getCurrentUserFromFirestore();
    }

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_mentor_chat;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.handleResponse(requestCode, resultCode, data);
    }

    // --------------------
    // ACTIONS
    // --------------------

    @OnClick(R.id.activity_mentor_chat_send_button)
    public void onClickSendMessage() {
        if (!TextUtils.isEmpty(editTextMessage.getText()) && modelCurrentUser != null) {
            MessageHelper.createMessageForChat(editTextMessage.getText().toString(), this.currentChatName, modelCurrentUser).addOnFailureListener(this.onFailureListener());
            this.editTextMessage.setText("");
        }
    }

    @OnClick({R.id.activity_mentor_chat_android_chat_button, R.id.activity_mentor_chat_firebase_chat_button, R.id.activity_mentor_chat_bug_chat_button})
    public void onClickChatButtons(ImageButton imageButton) {

        switch (Integer.valueOf(imageButton.getTag().toString())) {
            case 10:
                this.configureRecyclerView(CHAT_NAME_ANDROID);
                break;
            case 20:
                this.configureRecyclerView(CHAT_NAME_FIREBASE);
                break;
            case 30:
                this.configureRecyclerView(CHAT_NAME_BUG);
                break;
        }
    }

    @OnClick(R.id.activity_mentor_chat_add_file_button)
    @AfterPermissionGranted(RC_IMAGE_PERMS)
    public void onClickAddFile() {
        this.chooseImageFromPhone();
    }

    // --------------------
    // REST REQUESTS
    // --------------------

    private void getCurrentUserFromFirestore() {
        UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                modelCurrentUser = documentSnapshot.toObject(User.class);
            }
        });
    }

    // --------------------
    // UI
    // --------------------

    private void configureRecyclerView(String chatName) {
        //Track current chat name
        this.currentChatName = chatName;
        //Configure Adapter & RecyclerView
        this.mentorChatAdapter = new MentorChatAdapter(generateOptionsForAdapter(MessageHelper.getAllMessageForChat(this.currentChatName)), Glide.with(this), this, this.getCurrentUser().getUid());
        mentorChatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(mentorChatAdapter.getItemCount()); // Scroll to bottom on new messages
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(this.mentorChatAdapter);
    }


    private FirestoreRecyclerOptions<Message> generateOptionsForAdapter(Query query) {
        return new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .setLifecycleOwner(this)
                .build();
    }

    // --------------------
    // CALLBACK
    // --------------------

    @Override
    public void onDataChanged() {

        textViewRecyclerViewEmpty.setVisibility(this.mentorChatAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    // --------------------
    // FILE MANAGEMENT
    // --------------------

    private void chooseImageFromPhone() {
        if (!EasyPermissions.hasPermissions(this, PERMS)) {
            EasyPermissions.requestPermissions(this, getString(R.string.popup_title_permission_files_access), RC_IMAGE_PERMS, PERMS);
            return;
        }
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RC_CHOOSE_PHOTO);
    }

    private void handleResponse(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) { //SUCCESS
                this.uriImageSelected = data.getData();
                Glide.with(this) //SHOWING PREVIEW OF IMAGE
                        .load(this.uriImageSelected)
                        .apply(RequestOptions.circleCropTransform())
                        .into(this.imageViewPreview);
            } else {
                Toast.makeText(this, getString(R.string.toast_title_no_image_chosen), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
