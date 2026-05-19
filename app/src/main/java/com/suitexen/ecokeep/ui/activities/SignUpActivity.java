package com.suitexen.ecokeep.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.suitexen.ecokeep.R;
import com.suitexen.ecokeep.viewmodels.AuthViewModel;

public class SignUpActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirmPassword;
    private Button btnSignUp;
    private CheckBox cbTerms;
    private TextView tvLogin;
    private ImageView ivBack;
    private AuthViewModel authViewModel;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        initViews();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupProgressDialog();
        setupListeners();
        observeViewModel();
    }

    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating account...");
        progressDialog.setCancelable(false);
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        cbTerms = findViewById(R.id.cbTerms);
        tvLogin = findViewById(R.id.tvLogin);
        ivBack = findViewById(R.id.ivBack);
    }

    private void setupListeners() {
        btnSignUp.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (validateInput(name, email, password, confirmPassword)) {
                progressDialog.show();
                authViewModel.signUp(name, email, password);
            }
        });

        tvLogin.setOnClickListener(v -> finish());

        ivBack.setOnClickListener(v -> onBackPressed());
    }

    private void observeViewModel() {
        authViewModel.getSignUpSuccessLiveData().observe(this, success -> {
            progressDialog.dismiss();
            if (success != null && success) {
                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                finishAffinity();
            }
        });

        authViewModel.getErrorLiveData().observe(this, errorMessage -> {
            progressDialog.dismiss();
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInput(String name, String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(name)) {
            tilName.setError("Name is required");
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            return false;
        }
        if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            return false;
        }
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please agree to the Terms & Privacy", Toast.LENGTH_SHORT).show();
            return false;
        }

        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        return true;
    }
}