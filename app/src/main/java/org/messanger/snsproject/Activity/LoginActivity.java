package org.messanger.snsproject.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.messanger.snsproject.R;

public class LoginActivity extends AppCompatActivity {
    private EditText myEmail,myPassword;
    private Button loginButton,trySignUpButton,findPasswordButton;
    private FirebaseAuth firebaseAuth;
    private RelativeLayout loader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setID();
        setEvents();
    }
    public void setID(){
        myEmail=findViewById(R.id.myEmailEdittext);
        myPassword=findViewById(R.id.myPassEdittext);
        loginButton=findViewById(R.id.loginButton);
        trySignUpButton=findViewById(R.id.trySignupButton);
        findPasswordButton=findViewById(R.id.findPasswordButton);
        firebaseAuth=FirebaseAuth.getInstance();
        loader=findViewById(R.id.viewLoader);
    }
    public void setEvents(){
        loginButton.setOnClickListener(new View.OnClickListener() { //로그인을 시도함
            @Override
            public void onClick(View v) { //로그인 시도
                if(myEmail.getText().toString().length()<=0 || myPassword.getText().toString().length()<=0){ //입력하지 않은 경우,
                    Toast.makeText(LoginActivity.this,"이메일과 비밀번호를 입력해주새요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                loader.setVisibility(View.VISIBLE);
                startLogin();
            }
        });
        trySignUpButton.setOnClickListener(new View.OnClickListener() { //회원가입 화면으로 이동
            @Override
            public void onClick(View v) { //회원가입 화면으로 이동
                Intent intent=new Intent(LoginActivity.this, SignupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
        findPasswordButton.setOnClickListener(new View.OnClickListener() { //비밀번호 찾기 화면 이동
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(LoginActivity.this, FindPasswordActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
    }
    public void startLogin(){ //로그인 시작
        firebaseAuth.signInWithEmailAndPassword(myEmail.getText().toString(), myPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loader.setVisibility(View.GONE);
                        if (task.isSuccessful()) { //로그인 성공시,
                            Intent intent=new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
