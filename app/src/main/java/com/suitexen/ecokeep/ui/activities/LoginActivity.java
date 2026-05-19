package com.suitexen.ecokeep.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.suitexen.ecokeep.R;
import com.suitexen.ecokeep.viewmodels.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private Button btnLogin;
    private TextView tvSignUp, tvForgotPassword;
    private CardView cardGoogleLogin;
    private AuthViewModel authViewModel;
    private ProgressDialog progressDialog;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        initViews();
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupProgressDialog();
        setupGoogleSignIn();
        setupListeners();
        observeViewModel();
    }

    private void setupProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleGoogleSignInResult(task);
                    } else {
                        progressDialog.dismiss();
                    }
                }
        );
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                authViewModel.googleLogin(account.getIdToken());
            }
        } catch (ApiException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        cardGoogleLogin = findViewById(R.id.cardGoogleLogin);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInput(email, password)) {
                progressDialog.show();
                authViewModel.login(email, password);
            }
        });

        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show();
        });

        cardGoogleLogin.setOnClickListener(v -> {
            progressDialog.show();
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void observeViewModel() {
        authViewModel.getUserLiveData().observe(this, firebaseUser -> {
            progressDialog.dismiss();
            if (firebaseUser != null) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        });

        authViewModel.getErrorLiveData().observe(this, errorMessage -> {
            progressDialog.dismiss();
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            return false;
        }
        tilEmail.setError(null);
        tilPassword.setError(null);
        return true;
    }
}