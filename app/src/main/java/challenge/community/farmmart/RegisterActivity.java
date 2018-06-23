package challenge.community.farmmart;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText reg_email, reg_password, reg_confirm_password;
    private TextView new_reg_btn, reg_signin_btn;
    private ProgressBar regProgress;

    private FirebaseAuth mAuth;

    private Toolbar registerToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        reg_email = findViewById(R.id.reg_email);
        reg_password = findViewById(R.id.reg_password);
        reg_confirm_password = findViewById(R.id.reg_passwordConfirm);

        new_reg_btn = findViewById(R.id.new_reg_btn);

        registerToolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(registerToolbar);
        getSupportActionBar().setTitle("Join FarmMart");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*reg_signin_btn = findViewById(R.id.reg_sign_in_btn);
        reg_signin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(RegisterActivity.this, Login.class);
                startActivity(loginIntent);
                finish();
            }
        });*/

        regProgress = findViewById(R.id.reg_progressBar);

        mAuth = FirebaseAuth.getInstance();

        regProgress.setVisibility(View.GONE);

        new_reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = reg_email.getText().toString();
                String password = reg_password.getText().toString();
                String confirm_password = reg_confirm_password.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(confirm_password)) {

                    if (password.equals(confirm_password)) {

                        regProgress.setVisibility(View.VISIBLE);

                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    //sendToMain();
                                    accountSetup();
                                } else {

                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                                regProgress.setVisibility(View.INVISIBLE);
                            }
                        });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Password Don't Match", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            sendToMain();
        }
    }

    private void sendToMain() {

        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    public void accountSetup() {

        Intent setupIntent = new Intent(RegisterActivity.this, AccountSetup.class);
        startActivity(setupIntent);
        finish();
    }

    public void LoginRegPage(View view) {

        startActivity(new Intent(RegisterActivity.this, Login.class));
        finish();
    }
}
