package com.ferraris.firebasetest.api;

import com.ferraris.firebasetest.models.Message;
import com.ferraris.firebasetest.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

public class MessageHelper {

    private static final String COLLECTION_NAME = "messages";

    public static Task<DocumentReference> createMessageForChat(String textMessage, String chat, User userSender) {

        Message message = new Message(textMessage, userSender);

        return ChatHelper.getChatCollection()
                .document(chat)
                .collection(COLLECTION_NAME)
                .add(message);
    }

    // --- GET ---

    public static Query getAllMessageForChat(String chat) {
        return ChatHelper.getChatCollection()
                .document(chat)
                .collection(COLLECTION_NAME)
                .orderBy("dateCreated")
                .limit(50);
    }
}
