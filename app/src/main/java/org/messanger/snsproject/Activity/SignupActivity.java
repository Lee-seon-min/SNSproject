package org.messanger.snsproject.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.messanger.snsproject.R;

public class SignupActivity extends AppCompatActivity {
    private final String TAG="failErr";
    private EditText email,password,chPassword; //이메일,패스워드,패스워드 확인 EditText 객체
    private Button signup; //회원가입 버튼
    private FirebaseAuth firebaseAuth; //파이아베이스의 인증기능을 사용하기위한 객체선언
    private RelativeLayout loader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        setID(); //ID 설정(findViewByID)
        setEvents(); //버튼 이벤트 설정
    }
    public void setID(){
        email=findViewById(R.id.emailEdittext);
        password=findViewById(R.id.passEdittext);
        chPassword=findViewById(R.id.chpassEdittext);
        signup=findViewById(R.id.signupButton);
        firebaseAuth=FirebaseAuth.getInstance();
        loader=findViewById(R.id.viewLoader);
    }
    public void setEvents(){
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(email.getText().toString().length()<=0 || password.getText().toString().length()<=0){
                    Toast.makeText(SignupActivity.this,"이메일 또는 비밀번호를 입력해주십시오.",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(password.getText().toString().equals(chPassword.getText().toString())){
                    loader.setVisibility(View.VISIBLE);
                    startSignUp();
                }
                else{
                    Toast.makeText(SignupActivity.this,"비밀번호와 확인란이 일치하지 않습니다.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void startSignUp(){
        firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loader.setVisibility(View.GONE);
                if(task.isSuccessful()){ //회원가입을 성공적으로 마쳤다면,
                    firebaseAuth.signOut();
                    Intent intent=new Intent(SignupActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent); //로그인 화면으로 이동
                    Toast.makeText(SignupActivity.this,"Creating Account is successful",Toast.LENGTH_SHORT).show();
                    finish(); //화면을 종료
                }
                else{
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(SignupActivity.this,"Error",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
