package com.suitexen.echokeep.repositories;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.suitexen.echokeep.models.User;

public class AuthRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private final MutableLiveData<FirebaseUser> userLiveData;
    private final MutableLiveData<Boolean> loggedOutLiveData;
    private final MutableLiveData<String> errorLiveData;
    private final MutableLiveData<Boolean> signUpSuccessLiveData;

    public AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.userLiveData = new MutableLiveData<>();
        this.loggedOutLiveData = new MutableLiveData<>();
        this.errorLiveData = new MutableLiveData<>();
        this.signUpSuccessLiveData = new MutableLiveData<>();

        if (firebaseAuth.getCurrentUser() != null) {
            userLiveData.postValue(firebaseAuth.getCurrentUser());
            loggedOutLiveData.postValue(false);
        }
    }

    public void login(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userLiveData.postValue(firebaseAuth.getCurrentUser());
                    } else {
                        errorLiveData.postValue(task.getException() != null ? task.getException().getMessage() : "Login Failed");
                    }
                });
    }

    public void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Save user to Firestore if it's a new user
                            User userInfo = new User(user.getUid(), user.getDisplayName(), user.getEmail());
                            saveUserToFirestore(userInfo);
                            userLiveData.postValue(user);
                        }
                    } else {
                        errorLiveData.postValue(task.getException() != null ? task.getException().getMessage() : "Google Sign-In Failed");
                    }
                });
    }

    public void signUp(String name, String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            User user = new User(firebaseUser.getUid(), name, email);
                            saveUserToFirestore(user);
                        }
                    } else {
                        errorLiveData.postValue(task.getException() != null ? task.getException().getMessage() : "Sign Up Failed");
                    }
                });
    }

    private void saveUserToFirestore(User user) {
        firestore.collection("users").document(user.getUid())
                .set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        signUpSuccessLiveData.postValue(true);
                    } else {
                        errorLiveData.postValue(task.getException() != null ? task.getException().getMessage() : "Firestore Error");
                    }
                });
    }

    public void logout() {
        firebaseAuth.signOut();
        loggedOutLiveData.postValue(true);
        userLiveData.postValue(null);
    }

    public MutableLiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public MutableLiveData<Boolean> getLoggedOutLiveData() {
        return loggedOutLiveData;
    }

    public MutableLiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public MutableLiveData<Boolean> getSignUpSuccessLiveData() {
        return signUpSuccessLiveData;
    }
}