package org.messanger.snsproject.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.messanger.snsproject.R;

public class FindPasswordActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText targetEmail;
    private Button sendButton;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_password);
        setID();
        setEvents();
    }
    public void setID(){
        targetEmail=findViewById(R.id.targetEmailEdittext);
        sendButton=findViewById(R.id.sendButton);
        firebaseAuth=FirebaseAuth.getInstance();
    }
    public void setEvents(){
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(targetEmail.getText().toString().length()<=0){
                    return;
                }
                else{
                    Email();
                }
            }
        });
    }
    public void Email(){
        firebaseAuth.sendPasswordResetEmail(targetEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() { //해당 이메일 주소로 비밀번호 재설정
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(FindPasswordActivity.this,"We e-mail password reset document.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

