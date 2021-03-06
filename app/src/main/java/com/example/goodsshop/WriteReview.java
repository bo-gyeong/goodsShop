package com.example.goodsshop;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WriteReview extends AppCompatActivity {

    RatingBar evaluateRBar;
    EditText evaluateEditText;
    ConstraintLayout addReviewImg;
    ImageView reviewImgView;
    Button evaluateBtn;

    Intent fromNotiIntent;
    String myUid, otherUid, goodsName, category, shopName, rvDate;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_review);

        getSupportActionBar().setTitle("?????? ??????");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //back ??????

        myUid = MainActivity.sharedUid();
        new Thread(new Runnable() {
            @Override
            public void run() {
                databaseReference.child("users").child(myUid).child("shopName").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        shopName = snapshot.getValue(String.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).start();  //shopName ????????????

        // Notification?????? ?????? ????????? ????????? ?????? ????????????
        fromNotiIntent = getIntent();
        otherUid = fromNotiIntent.getStringExtra("otherUid");
        goodsName = fromNotiIntent.getStringExtra("goodsName");
        category = fromNotiIntent.getStringExtra("goodsCtgry");

        evaluateEditText = findViewById(R.id.evaluateEditText);
        evaluateRBar = findViewById(R.id.evaluateRBar);

        addReviewImg = findViewById(R.id.addReviewImg);
        addReviewImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGallery();
            }
        });

        reviewImgView = findViewById(R.id.reviewImgView);

        evaluateBtn = findViewById(R.id.evaluateBtn);
        evaluateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (evaluateRBar.getRating() != 0.0 && evaluateEditText.length()!=0){  // ????????? ???????????? ??? ??? ?????????
                    if (uri != null){
                        uploadFile();
                    } else{
                        rvImageUrl = "";
                        uploadReview();
                    }
                    deleteNoti();
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(), "??? ?????? ???????????????", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private final int REQ_CODE_SELECT_IMAGE = 1000;
    Uri uri;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                uri = data.getData();

                if (uri != null){
                    try {  //?????? ???????????? ????????? ?????? ????????? ?????????
                        Glide.with(this).load(uri).centerCrop().into(reviewImgView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void getGallery(){
        Intent intent;

        if (Build.VERSION.SDK_INT >= 19){
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        else{
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            //API 18???????????? ?????? ????????? ???????????? ??????,,,,??????
        }
        intent.setType("image/*");
        startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
    }  //????????? ????????? ?????? ??????

    private String rvImageUrl;

    // ????????????&???????????? ????????? ?????? ?????????
    private void uploadFile() {
        String filename = otherUid + "_" + goodsName + "_review";

        UploadTask uploadTask = FirebaseStorage.getInstance().getReference()
                .child("reviews/" + filename).putFile(uri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                final Task<Uri> imageUrl = taskSnapshot.getStorage().getDownloadUrl();
                while (!imageUrl.isComplete());

                rvImageUrl = Objects.requireNonNull(imageUrl.getResult()).toString();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        uploadReview();
                    }
                }, 1000);
            }
        });

    } //?????? ???????????? ??????????????? ?????????

    //  ????????? ????????? ??????
    private void uploadReview(){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        rvDate = simpleDateFormat.format(date);

        ReviewData reviewData = new ReviewData();

        reviewData.setUid(myUid);
        reviewData.setShopName(shopName);
        reviewData.setrBarScore(evaluateRBar.getRating());
        reviewData.setRvContent(evaluateEditText.getText().toString());
        reviewData.setRvImgUrl(rvImageUrl);
        reviewData.setRvDate(rvDate);

        databaseReference.child("reviews").child(otherUid).child(category)
                .child(goodsName).setValue(reviewData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                changeRBarScore();

                Toast.makeText(getApplicationContext(), "????????? ?????????????????????", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    double getOtherRBarScore, setOtherRBarScore;

    // ???????????? ????????? initialRBar ??????
    private void changeRBarScore(){
        databaseReference.child("users").child(otherUid).child("initialRBar").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getOtherRBarScore = snapshot.getValue(float.class);

                reviewCnt();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (reviewCount > 2){
                            setOtherRBarScore = (evaluateRBar.getRating() + getOtherRBarScore + 3)/reviewCount;
                            setOtherRBarScore = (double) (Math.round(setOtherRBarScore * 2)/2.0);
                        } else{
                            setOtherRBarScore = (evaluateRBar.getRating() + getOtherRBarScore)/2;
                            setOtherRBarScore = (double) setOtherRBarScore * 2/2.0;
                        }

                        HashMap<String, Object> updateMap = new HashMap<>();
                        updateMap.put("initialRBar", setOtherRBarScore);
                        databaseReference.child("users").child(otherUid).updateChildren(updateMap);
                    }
                }, 1000);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    int reviewCount;

    // ?????? ?????? ?????????
    private void reviewCnt(){
        reviewCount = 1;  // ?????? ????????? ???????????? 3?????? ????????? ???

        databaseReference.child("reviews").child(otherUid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot ctgrySnapshot, @Nullable String previousChildName) {
                databaseReference.child("reviews").child(otherUid).child(ctgrySnapshot.getKey())
                        .addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot goodsNameSnapshot, @Nullable String previousChildName) {
                                reviewCount++;
                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // ????????????????????? notification??? ?????? ?????? ????????? ??????
    private void deleteNoti(){
        DatabaseReference path = databaseReference.child("notifications").child(myUid).child(category).child(goodsName);

        path.removeValue();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: //toolbar??? back??? ????????? ??? ??????
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}