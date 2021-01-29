package org.messanger.snsproject.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.messanger.snsproject.Camera2API.CameraActivity;
import org.messanger.snsproject.Data.MemberData;
import org.messanger.snsproject.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class InitInfoActivity extends AppCompatActivity{
    private final String TAG="failErr";
    public static final int CAMERA_REQ_CODE=103;
    public static final int FIN_CODE=102;
    public static final int GALLERY_LOAD_CODE=104;
    private EditText name,birth,phone,address;
    private Button save,loadGallery;
    private ImageView profileImage;
    private String profilePath=null;
    private RelativeLayout loader;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initinfo);
        setID();
        setEvents();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        }
    }
    public void setID(){
        name=findViewById(R.id.nameEdittext);
        birth=findViewById(R.id.birthEdittext);
        phone=findViewById(R.id.phoneEdittext);
        address=findViewById(R.id.adressEdittext);
        save=findViewById(R.id.saveinfoButton);
        profileImage=findViewById(R.id.profileImageView);
        loadGallery=findViewById(R.id.loadGalleryButton);
        loader=findViewById(R.id.viewLoader);
    }
    public void setEvents(){
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.setVisibility(View.VISIBLE);
                updateInfo();
            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(InitInfoActivity.this, CameraActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(intent,CAMERA_REQ_CODE);
            }
        });
        loadGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent,GALLERY_LOAD_CODE);
            }
        });
    }
    public void updateInfo(){
        final String myname=name.getText().toString();
        final String mybirth=birth.getText().toString();
        final String myphone=phone.getText().toString();
        final String myaddress=address.getText().toString();

        if(myname.length()>0 && myphone.length()>9 && mybirth.length()>5 && myaddress.length()>0 && profilePath!=null) {
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //현재 로그인한 유저
            FirebaseStorage storage=FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            final StorageReference profileImagesRef = storageRef.child("users/"+user.getUid()+"/profileImage.jpg"); //해당 경로로 이미지를 저장
            UploadTask uploadTask = null;

            InputStream stream = null;
            try {
                Toast.makeText(InitInfoActivity.this,profilePath,Toast.LENGTH_SHORT).show();
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
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) { //프로필 사진을 파이어베이스에 업로드하고, 성공적으로 끝났다면,
                        Uri downloadUri = task.getResult(); //파이어베이스 스토리지 URI
                        FirebaseFirestore db=FirebaseFirestore.getInstance(); //파이어스토어(db) 객체
                        MemberData data = new MemberData(myname,myphone,mybirth,myaddress,downloadUri.toString()); //객체화한 유저의 정보
                        db.collection("users").document(user.getUid()).set(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {//users 아래 특정 uid의 정보를 저장
                                    @Override
                                    public void onSuccess(Void aVoid) { //저장이 성공했다면,
                                        loader.setVisibility(View.GONE);
                                        Toast.makeText(InitInfoActivity.this, "회원정보를 성공적으로 저장하였습니다.", Toast.LENGTH_SHORT).show();
                                        setResult(FIN_CODE);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        loader.setVisibility(View.GONE);
                                        Toast.makeText(InitInfoActivity.this, "정보 저장을 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    else {
                        // Handle failures
                        // ...
                    }
                }
            });

        }
        else{
            loader.setVisibility(View.GONE);
            Toast.makeText(InitInfoActivity.this, "전화번호는 10자 이상, 생일은 5자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CAMERA_REQ_CODE){
            if(resultCode== Activity.RESULT_OK){ //수정요함
                profilePath=data.getStringExtra("profilePath");
                Glide.with(this).load(profilePath).centerCrop().override(500).into(profileImage);
            }
        }
        else if(requestCode==GALLERY_LOAD_CODE){
            if(resultCode==Activity.RESULT_OK){
                profilePath=getPath(data.getData());
                Glide.with(this).load(profilePath).centerCrop().override(500).into(profileImage);
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
