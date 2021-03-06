package com.example.goodsshop;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ClickGoods extends Activity {

    ImageButton backImgBtn;
    TextView priceTextView, goodsTitleTextView, daysTextView, dibsTextView, postTextView, parcelTextView
            , directDealingTextView, stateTextView, contentTextView, categoryTextView, shopNameTextView
            , chattingBtn, goToSalesBtn;
    ImageView profileImgView;
    RatingBar evaluateRBar;
    ConstraintLayout seeOtherShopCLayout, userBtnView, nonUserBtnView, mineBtnView;
    ImageButton jjimImg;

    Intent fromSearchIntent;
    String getMyUid, sellerUid, goodsName, category, price, jjimCnt, condition, regiDate
            , explain, otherShopName, otherProfileImg, saleState, myShopName;
    int picCount;
    float sellerInitialRBar;
    ArrayList<String> imgUrlArrList = new ArrayList<>();
    boolean post, parcel, directDealing, click;
    DatabaseReference path;

    /***** ????????? ???????????? ?????? ?????? https://android-dev.tistory.com/12 *****/
    ViewPager2 goodsImgViewPager2;
    LinearLayout indicatorLinearLayout;

    ArrayList<String> images = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.click_goods);

        getMyUid = MainActivity.sharedUid();

        fromSearchIntent = getIntent();
        sellerUid = fromSearchIntent.getStringExtra("uid");
        goodsName = fromSearchIntent.getStringExtra("goodsName");
        category = fromSearchIntent.getStringExtra("category");
        price = fromSearchIntent.getStringExtra("price");
        post = fromSearchIntent.getBooleanExtra("post", false);
        parcel = fromSearchIntent.getBooleanExtra("parcel", false);
        directDealing = fromSearchIntent.getBooleanExtra("directDealing", false);
        condition = fromSearchIntent.getStringExtra("condition");
        regiDate = fromSearchIntent.getStringExtra("regiDate");
        jjimCnt = fromSearchIntent.getStringExtra("jjimCnt");
        saleState = fromSearchIntent.getStringExtra("saleState");
        explain = fromSearchIntent.getStringExtra("explain");
        picCount = fromSearchIntent.getIntExtra("picCount", 1);
        imgUrlArrList = fromSearchIntent.getStringArrayListExtra("imgUrlArrList");

        backImgBtn = findViewById(R.id.backImgBtn);
        backImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });  //???????????? ??????

        goodsImgViewPager2 = findViewById(R.id.goodsImgViewPager2);
        indicatorLinearLayout = findViewById(R.id.indicatorLinearLayout);

        for (int i=0; i<picCount; i++){
            images.add(imgUrlArrList.get(i));
        }

        goodsImgViewPager2.setOffscreenPageLimit(picCount);  //????????? ?????? ???????????? ??????
        goodsImgViewPager2.setAdapter(new ImageSlider(this, images));


        GestureDetector gestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Intent intent = new Intent(getApplicationContext(), ClickGoodsDetail.class);
                intent.putExtra("imgUrlArrList", imgUrlArrList);
                intent.putExtra("picCount", picCount);

                startActivity(intent);

                return false;
            }  //??????????????? ?????? ????????? ??????

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
        goodsImgViewPager2.getChildAt(0).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);

                return false;
            }
        });  // ?????? ????????? ?????? ???

        goodsImgViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);
            }
        });  //?????? ???????????? ???????????? ????????????

        setupIndicators(images.size());

        // ?????? ???????????? ????????? ????????? ?????? ????????? ??????
        priceTextView = findViewById(R.id.priceTextView);
        priceTextView.setText(price);

        goodsTitleTextView = findViewById(R.id.goodsTitleTextView);
        goodsTitleTextView.setText(goodsName);

        daysTextView = findViewById(R.id.daysTextView);
        daysTextView.setText(regiDate);

        dibsTextView = findViewById(R.id.dibsTextView);
        setJjimCnt();

        postTextView = findViewById(R.id.postTextView);
        if (post){
            postTextView.setVisibility(View.VISIBLE);
        }
        parcelTextView = findViewById(R.id.parcelTextView);
        if (parcel){
            parcelTextView.setVisibility(View.VISIBLE);
        }
        directDealingTextView = findViewById(R.id.directDealingTextView);
        if (directDealing){
            directDealingTextView.setVisibility(View.VISIBLE);
        }

        stateTextView = findViewById(R.id.stateTextView);
        stateTextView.setText(condition);

        contentTextView = findViewById(R.id.contentTextView);
        contentTextView.setText(explain);

        categoryTextView = findViewById(R.id.categoryTextView);
        categoryTextView.setText(category);

        shopNameTextView = findViewById(R.id.shopNameTextView);
        profileImgView = findViewById(R.id.profileImgView);
        evaluateRBar = findViewById(R.id.evaluateRBar);

        getSellerInfo(sellerUid);

        seeOtherShopCLayout = findViewById(R.id.seeOtherShopConstraintLayout);
        seeOtherShopCLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SeeOtherShop.class);
                intent.putExtra("getUid", sellerUid);
                intent.putExtra("otherShopName", otherShopName);
                intent.putExtra("profileImgUrl", otherProfileImg);
                intent.putExtra("otherRBar", sellerInitialRBar);

                startActivity(intent);
            }
        });  //????????? ????????? ?????? ???

        userBtnView = findViewById(R.id.userBtnView);
        nonUserBtnView = findViewById(R.id.nonUserBtnView);
        mineBtnView = findViewById(R.id.mineBtnView);
        checkUid();

        jjimImg = findViewById(R.id.jjimBtn);


        if (getMyUid != null){
            path = databaseReference.child("users").child(getMyUid).child("jjim")
                    .child(sellerUid).child(category).child(goodsName);

            path.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    click = true;
                    jjimImg.setImageResource(R.drawable.ic_baseline_favorite_24);
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

        jjimImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int intJjimCnt = Integer.parseInt(jjimCnt);

                if(click){
                    intJjimCnt--;
                    dibsTextView.setText(String.valueOf(intJjimCnt));
                    changeJjimCnt(intJjimCnt);

                    path.removeValue();

                    jjimImg.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    click=false;
                }else{
                    intJjimCnt++;
                    dibsTextView.setText(String.valueOf(intJjimCnt));
                    changeJjimCnt(intJjimCnt);

                    JjimGoods jjimGoods = new JjimGoods();
                    jjimGoods.setRegiImageUrl(imgUrlArrList.get(0));
                    jjimGoods.setPicCount(picCount);
                    jjimGoods.setPrice(Integer.parseInt(price));

                    path.setValue(jjimGoods);

                    jjimImg.setImageResource(R.drawable.ic_baseline_favorite_24);
                    click=true;
                }
            }
        });  //????????? ????????? ?????? ??? ???????????? ????????? ??? ????????? ??? ??????

        chattingBtn = findViewById(R.id.chattingBtn);

        if (saleState.equals("reservation")){
            chattingBtn.setText("?????????");
        }
        else if (saleState.equals("salesCompleted")) {
            chattingBtn.setText("????????????");
        }
        else{
            chattingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ChattingRoom.class);
                    intent.putExtra("getOtherUid", sellerUid);
                    intent.putExtra("myShopName", myShopName);
                    intent.putExtra("otherShopName", otherShopName);
                    intent.putExtra("otherProfileImg", otherProfileImg);
                    intent.putExtra("getGoodsName", goodsName);

                    startActivity(intent);
                }
            });  //???????????? ?????? ?????? ???
        }

        goToSalesBtn = findViewById(R.id.goToSalesBtn);
        goToSalesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Sales.class);

                startActivity(intent);
            }
        });  //??? ???????????? ?????? ?????? ?????? ???
    }

    // ????????? ??? uid ??????
    private void checkUid(){
        if(getMyUid == null) {  //uid??? ?????????(????????? ????????? ????????????) nonUserBtnView ?????????
            userBtnView.setVisibility(View.GONE);
            nonUserBtnView.setVisibility(View.VISIBLE);
            mineBtnView.setVisibility(View.GONE);
        }
        else if (getMyUid.equals(sellerUid)){  //?????? ???????????? ????????? ?????? mineBtnView ?????????
            userBtnView.setVisibility(View.GONE);
            nonUserBtnView.setVisibility(View.GONE);
            mineBtnView.setVisibility(View.VISIBLE);
            getShopName();
        }
        else {  // ????????? ??? ???????????? userBtnView ?????????
            userBtnView.setVisibility(View.VISIBLE);
            nonUserBtnView.setVisibility(View.GONE);
            mineBtnView.setVisibility(View.GONE);
            getShopName();
        }
    }

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    // ????????? ?????? ??? ??? ????????????
    private void setJjimCnt(){
        DatabaseReference path = databaseReference.child("regiGoods").child(sellerUid).child(saleState).child(category).child(goodsName).child("jjimCnt");

        path.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                jjimCnt = String.valueOf(snapshot.getValue());
                dibsTextView.setText(jjimCnt);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // ????????? ?????? ??? ????????? ??????
    private void changeJjimCnt(int intJjimCnt){
        DatabaseReference path = databaseReference.child("regiGoods").child(sellerUid).child(saleState).child(category).child(goodsName).child("jjimCnt");
        path.setValue(intJjimCnt);
    }

    // ????????? ?????? ????????????
    private void getSellerInfo(String sellerUid){
        databaseReference.child("users").child(sellerUid).child("shopName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                otherShopName = snapshot.getValue(String.class);
                shopNameTextView.setText(otherShopName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReference.child("users").child(sellerUid).child("profileImageUrl").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                otherProfileImg = snapshot.getValue(String.class);

                if (otherProfileImg != null){ // ????????? ????????? ??????????????? ?????????????????? ???????????? ?????????
                    Glide.with(getApplicationContext()).load(otherProfileImg).circleCrop()
                            .placeholder(R.drawable.ic_baseline_person_24).into(profileImgView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        databaseReference.child("users").child(sellerUid).child("initialRBar").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    sellerInitialRBar = snapshot.getValue(float.class);
                    evaluateRBar.setRating(sellerInitialRBar);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // ?????? ??? ?????? ????????????
    private void getShopName(){
        databaseReference.child("users").child(getMyUid).child("shopName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myShopName = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setupIndicators(int count) {
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        params.setMargins(16, 8, 16, 8);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(this);
            indicators[i].setImageDrawable(ContextCompat.getDrawable(this,
                    R.drawable.indicator_inactive));
            indicators[i].setLayoutParams(params);
            indicatorLinearLayout.addView(indicators[i]);
        }
        setCurrentIndicator(0);
    }  //?????? ???????????? ???????????? ?????? ...??????

    private void setCurrentIndicator(int position) {
        int childCount = indicatorLinearLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) indicatorLinearLayout.getChildAt(i);
            if (i == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        this,
                        R.drawable.indicator_active
                ));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        this,
                        R.drawable.indicator_inactive
                ));
            }
        }
    }  //?????? ????????? ???????????? ?????? ???????????? ?????? ... ?????? ??????
}
