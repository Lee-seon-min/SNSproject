package org.messanger.snsproject.Activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;
import androidx.transition.Visibility;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.messanger.snsproject.Data.MemberData;
import org.messanger.snsproject.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MyPageActivity extends AppCompatActivity {
    private EditText myPageName,myPageAddress;
    private EditText myPagePhone, myPageBirth;
    private String uid=null,profilePath=null;
    private ImageView profImg;
    private Button changeProfImg,updateProf;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private RelativeLayout loader;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        uid=getIntent().getStringExtra("userID");

        setID();
        setEvents();
        getInfo();
    }
    public void setID(){
        myPageName=findViewById(R.id.myPageName);
        myPageAddress=findViewById(R.id.myPageAddress);
        myPagePhone=findViewById(R.id.myPagePhone);
        myPageBirth=findViewById(R.id.myPageBirth);
        changeProfImg=findViewById(R.id.changeProfileImageButton);
        updateProf=findViewById(R.id.updateProfileButton);
        profImg=findViewById(R.id.myPageProfileImage);
        loader=findViewById(R.id.viewLoader);
    }
    public void setEvents(){
        changeProfImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent,InitInfoActivity.GALLERY_LOAD_CODE);
            }
        });
        updateProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference reference = db.collection("users").document(uid);
                reference
                        .update("myname",myPageName.getText().toString(),
                                "mybirth",myPageBirth.getText().toString(),
                                "myaddress",myPageAddress.getText().toString(),
                                "myphone",myPagePhone.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MyPageActivity.this,"회원정보를 성공적으로 변경했습니다.",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MyPageActivity.this,"회원정보 변경을 실패하였습니다.",Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
    public void getInfo(){
        //정보불러와서 EditText에 넣기
        DocumentReference documentReference=db.collection("users").document(uid);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                myPageName.setText(documentSnapshot.getData().get("myname").toString());
                myPageAddress.setText(documentSnapshot.getData().get("myaddress").toString());
                myPageBirth.setText(documentSnapshot.getData().get("mybirth").toString());
                myPagePhone.setText(documentSnapshot.getData().get("myphone").toString());
                Glide.with(MyPageActivity.this).load(documentSnapshot.getData().get("prof_path").toString()).centerCrop().override(500).into(profImg);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
          if(requestCode==InitInfoActivity.GALLERY_LOAD_CODE){
            if(resultCode== Activity.RESULT_OK){
                loader.setVisibility(View.VISIBLE);
                profilePath=getPath(data.getData());
                Glide.with(this).load(profilePath).centerCrop().override(500).into(profImg);

                FirebaseStorage storage=FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                final StorageReference profileImagesRef = storageRef.child("users/"+uid+"/profileImage.jpg"); //해당 경로로 이미지를 저장
                UploadTask uploadTask = null;

                InputStream stream = null;
                try {
                    stream = new FileInputStream(new File(profilePath)); //해당 경로로의 스트림 형성
                    uploadTask = profileImagesRef.putStream(stream); //기기내부의 로컬경로로 부터 저장
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() { //저장하면서,
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        // Continue with the task to get the download URL
                        return profileImagesRef.getDownloadUrl();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        DocumentReference reference = db.collection("users").document(uid);
                        reference
                                .update("prof_path",uri.toString())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        loader.setVisibility(View.GONE);
                                        Toast.makeText(MyPageActivity.this,"프로필 사진을 성공적으로 변경했습니다.",Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        loader.setVisibility(View.GONE);
                                        Toast.makeText(MyPageActivity.this,"프로필 사진변경을 실패하였습니다.",Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

            }
        }
    }

    public String getPath(Uri uri){ //적절한 path로 바꿔주는 코드
        String[] proj={MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader=new CursorLoader(this,uri,proj,null,null,null);
        Cursor cursor=cursorLoader.loadInBackground();
        int index=cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(index);
    }
}
