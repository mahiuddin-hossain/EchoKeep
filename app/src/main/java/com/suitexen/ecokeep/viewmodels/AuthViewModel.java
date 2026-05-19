package com.suitexen.ecokeep.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseUser;
import com.suitexen.ecokeep.repositories.AuthRepository;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final LiveData<FirebaseUser> userLiveData;
    private final LiveData<Boolean> loggedOutLiveData;
    private final LiveData<String> errorLiveData;
    private final LiveData<Boolean> signUpSuccessLiveData;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository();
        userLiveData = authRepository.getUserLiveData();
        loggedOutLiveData = authRepository.getLoggedOutLiveData();
        errorLiveData = authRepository.getErrorLiveData();
        signUpSuccessLiveData = authRepository.getSignUpSuccessLiveData();
    }

    public void login(String email, String password) {
        authRepository.login(email, password);
    }

    public void signUp(String name, String email, String password) {
        authRepository.signUp(name, email, password);
    }

    public void googleLogin(String idToken) {
        authRepository.firebaseAuthWithGoogle(idToken);
    }

    public void logout() {
        authRepository.logout();
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<Boolean> getLoggedOutLiveData() {
        return loggedOutLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getSignUpSuccessLiveData() {
        return signUpSuccessLiveData;
    }
}