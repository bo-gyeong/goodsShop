package com.example.goodsshop;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;

public class AfterSearch extends Activity {

    ProgressBar searchProgressBar;
    EditText searchEditText;
    TextView noGoodsTextView, goodsCountTextView;
    Button postBtn, parcelBtn, directDealingBtn, highBtn, midBtn, lowBtn, initialBtn, applyBtn;
    ImageButton backImgBtn, searchImgBtn, noticeImgBtn, filterImgBtn;
    Spinner sortSpinner;
    RecyclerView showGoodsRecycler;
    FloatingActionButton fab;

    Intent getHomeIntent, getCtgryIntent;
    String searchKeyword;
    boolean postBtnClick, parcelBtnClick, directDealingBtnClick, highBtnClick, midBtnClick, lowBtnClick;

    @Override
    protected void onResume() {  //????????? ?????? ???
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!exist){
                    goodsCountTextView.setText("0");
                    noGoodsTextView.setVisibility(View.VISIBLE);
                    searchProgressBar.setVisibility(View.GONE);
                }
            }
        }, 1500);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_search);

        getHomeIntent = getIntent();
        getCtgryIntent = getIntent();

        searchProgressBar = findViewById(R.id.searchProgressBar);
        noGoodsTextView = findViewById(R.id.noGoodsTextView);
        goodsCountTextView = findViewById(R.id.goodsCountTextView);
        showGoodsRecycler = findViewById(R.id.showGoodsRecyclerView);

        backImgBtn = findViewById(R.id.backImgBtn);
        backImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });  //???????????? ??????

        searchKeyword = getHomeIntent.getStringExtra("searchKeyword");
        searchEditText = findViewById(R.id.searchEditText);

        if (searchKeyword == null){
            findInCategory();
        } else{
            searchKeyword();
            searchEditText.setText(searchKeyword);
        }

        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == IME_ACTION_SEARCH) {  //????????? search ?????? ?????? ???
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);  //????????? ?????????

                    searchKeyword = searchEditText.getText().toString();
                    searchKeyword();

                    searchEditText.setText(searchKeyword);
                }

                return false;
            }
        });  //??????EditText

        searchImgBtn = findViewById(R.id.searchImgBtn);
        searchImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

                searchKeyword = searchEditText.getText().toString();
                searchKeyword();

                searchEditText.setText(searchKeyword);
            }
        });  //?????? ?????? ?????? ???

        noticeImgBtn = findViewById(R.id.noticeImgBtn);
        noticeImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Notification.class);
                startActivity(intent);
            }
        }); //?????? ?????? ?????? ???

        sortSpinner = (Spinner) findViewById(R.id.sortSpinner);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.spinnerArray, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (sortSpinner.getSelectedItemPosition()==0){
                    /******  https://kdinner.tistory.com/81  ?????? ???????????? '??????'??? ?????? ????????? + ??????  *******/
                } //?????? ????????? ?????? ??? ????????? ??? ??????
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /******* bottomsheet ?????? ?????? https://jwsoft91.tistory.com/45 *******/
        /******* sql????????? ?????? ?????? https://cionman.tistory.com/72 *******/
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.filter, null, false);
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(view);

        filterImgBtn = (ImageButton) findViewById(R.id.filterImgBtn);
        filterImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.show();
            }
        });

        postBtn = view.findViewById(R.id.postBtn);
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postBtnClick = changeBtnColor(postBtn, postBtnClick);
            }
        });

        parcelBtn = view.findViewById(R.id.parcelBtn);
        parcelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parcelBtnClick = changeBtnColor(parcelBtn, parcelBtnClick);
            }
        });

        directDealingBtn = view.findViewById(R.id.directDealingBtn);
        directDealingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                directDealingBtnClick = changeBtnColor(directDealingBtn, directDealingBtnClick);
            }
        });

        highBtn = view.findViewById(R.id.highBtn);
        highBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                highBtnClick = changeBtnColor(highBtn, highBtnClick);
            }
        });

        midBtn = view.findViewById(R.id.midBtn);
        midBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                midBtnClick = changeBtnColor(midBtn, midBtnClick);
            }
        });

        lowBtn = view.findViewById(R.id.lowBtn);
        lowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lowBtnClick = changeBtnColor(lowBtn, lowBtnClick);
            }
        });

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new FabClickListener());
    }

    String goodsName, price, imgUrl, picCount;
    ArrayList<String> getUid, getGoodsName, getPrice, getImgUrl, getPost, getParcel, getDirectDealing
            , getCondition, getExplain, getRegiDate, getJjimCnt, getPicCount, getCtgryArr;
    ArrayList<ArrayList<String>> getImgUrlArrList;

    ArrayList<SearchGoodsData> goodsArrList;

    // ????????????????????? ?????? ??????
    void makeRecycler(){
        SearchGoodsData searchGoodsData = new SearchGoodsData();

        searchGoodsData.setGoodsImgView(Uri.parse(imgUrl));
        searchGoodsData.setGoodsTitleTextView(goodsName);
        searchGoodsData.setPriceTextView(price);

        goodsArrList.add(searchGoodsData);

        searchProgressBar.setVisibility(View.GONE);

        ShowSearchGoods showSearchGoods;

        GridLayoutManager manager = new GridLayoutManager(this, 2);
        showGoodsRecycler.setLayoutManager(manager);
        showGoodsRecycler.setHasFixedSize(false);
        showSearchGoods = new ShowSearchGoods(goodsArrList, getApplicationContext());
        showGoodsRecycler.setAdapter(showSearchGoods);

        if (imgUrl.equals("")){
            showGoodsRecycler.setVisibility(View.GONE);
        } else{
            showGoodsRecycler.setVisibility(View.VISIBLE);
            showSearchGoods.setOnRItemClickListener(new ShowSearchGoods.OnRItemClickListener() {
                @Override
                public void onRItemClickListener(View v, int position) {  //????????? ?????? ??? clickGoods??? ?????? ?????? ??????
                    Intent intent = new Intent(getApplicationContext(), ClickGoods.class);

                    intent.putExtra("uid", getUid.get(position));
                    intent.putExtra("goodsName", getGoodsName.get(position));
                    intent.putExtra("category", getCtgryArr.get(position));
                    intent.putExtra("imgUrlArrList", getImgUrlArrList.get(position));
                    intent.putExtra("picCount", Integer.parseInt(getPicCount.get(position)));
                    intent.putExtra("price", getPrice.get(position));
                    intent.putExtra("post", Boolean.valueOf(getPost.get(position)));
                    intent.putExtra("parcel", Boolean.valueOf(getParcel.get(position)));
                    intent.putExtra("directDealing", Boolean.valueOf(getDirectDealing.get(position)));
                    intent.putExtra("condition", getCondition.get(position));
                    intent.putExtra("explain", getExplain.get(position));
                    intent.putExtra("regiDate", getRegiDate.get(position));
                    intent.putExtra("jjimCnt", getJjimCnt.get(position));
                    intent.putExtra("saleState", "onSale");

                    startActivity(intent);
                }
            });
        }
    }

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    int goodsCnt = 0;
    String getCtgry;
    boolean exist = false;

    // ?????? ?????????????????? ?????? ????????????
    private void findInCategory(){

        getUid = new ArrayList<>(); getGoodsName = new ArrayList<>(); getPrice = new ArrayList<>();
        getImgUrl = new ArrayList<>(); getPost = new ArrayList<>(); getParcel = new ArrayList<>();
        getDirectDealing = new ArrayList<>(); getCondition = new ArrayList<>(); getExplain = new ArrayList<>();
        getRegiDate = new ArrayList<>(); getJjimCnt = new ArrayList<>(); getPicCount = new ArrayList<>();
        getCtgryArr = new ArrayList<>(); getImgUrlArrList = new ArrayList<>();

        goodsArrList = new ArrayList<>();

        if (getCtgryIntent != null){
            getCtgry = getCtgryIntent.getStringExtra("category");
        }  //???????????? ????????? ????????? ?????? ??????

        databaseReference.child("regiGoods").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dtSnapshot, @Nullable String previousChildName) {
                goodsCnt = 0;

                databaseReference.child("regiGoods").child(dtSnapshot.getKey()).child("onSale")
                        .child(getCtgry).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        exist = true;
                        getUid.add(dtSnapshot.getKey());

                        goodsName = snapshot.getKey();
                        getGoodsName.add(snapshot.getKey());

                        getCtgryArr.add(getCtgry);

                        for(DataSnapshot dSnapshot : snapshot.getChildren())
                        {
                            if (dSnapshot.getKey().equals("price")){
                                price = String.valueOf(dSnapshot.getValue());
                                getPrice.add(String.valueOf(dSnapshot.getValue()));
                                goodsCnt++;
                            }
                            else if (dSnapshot.getKey().equals("regiImageUrl")){
                                imgUrl = String.valueOf(dSnapshot.child("0").getValue());
                                ArrayList<String> arr;
                                for (int i=0; i<Integer.parseInt(picCount); i++){
                                    getImgUrl.add(String.valueOf(dSnapshot.child(String.valueOf(i)).getValue()));
                                }
                                arr = new ArrayList<>(getImgUrl);
                                getImgUrlArrList.add(arr);
                                getImgUrl.clear();
                            }
                            else if (dSnapshot.getKey().equals("post")){
                                getPost.add(String.valueOf(dSnapshot.getValue()));
                            }
                            else if (dSnapshot.getKey().equals("parcel")){
                                getParcel.add(String.valueOf(dSnapshot.getValue()));
                            }
                            else if (dSnapshot.getKey().equals("directDealing")){
                                getDirectDealing.add(String.valueOf(dSnapshot.getValue()));
                            }
                            else if (dSnapshot.getKey().equals("condition")){
                                getCondition.add(String.valueOf(dSnapshot.getValue()));
                            }
                            else if (dSnapshot.getKey().equals("explain")){
                                getExplain.add(String.valueOf(dSnapshot.getValue()));
                            }
                            else if (dSnapshot.getKey().equals("regiDate")){
                                getRegiDate.add(String.valueOf(dSnapshot.getValue()));
                            }
                            else if (dSnapshot.getKey().equals("jjimCnt")){
                                getJjimCnt.add(String.valueOf(dSnapshot.getValue()));
                            }
                            else if (dSnapshot.getKey().equals("picCount")){
                                picCount = String.valueOf(dSnapshot.getValue());
                                getPicCount.add(String.valueOf(dSnapshot.getValue()));
                            }
                        }

                        goodsCountTextView.setText(String.valueOf(goodsCnt));
                        makeRecycler();
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

    // ?????? ?????? ????????????
    private void searchKeyword(){

        getUid = new ArrayList<>(); getGoodsName = new ArrayList<>(); getPrice = new ArrayList<>();
        getImgUrl = new ArrayList<>(); getPost = new ArrayList<>(); getParcel = new ArrayList<>();
        getDirectDealing = new ArrayList<>(); getCondition = new ArrayList<>(); getExplain = new ArrayList<>();
        getRegiDate = new ArrayList<>(); getJjimCnt = new ArrayList<>(); getPicCount = new ArrayList<>();
        getCtgryArr = new ArrayList<>(); getImgUrlArrList = new ArrayList<>();

        goodsArrList = new ArrayList<>();

        exist = false;

        databaseReference.child("regiGoods").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot uidSnapshot, @Nullable String previousChildName) {
                goodsCnt = 0;

                databaseReference.child("regiGoods").child(uidSnapshot.getKey()).child("onSale")
                        .addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot ctgrySnapshot, @Nullable String previousChildName) {

                                databaseReference.child("regiGoods").child(uidSnapshot.getKey()).child("onSale")
                                        .child(ctgrySnapshot.getKey()).addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                        if (Objects.requireNonNull(snapshot.getKey()).contains(searchKeyword)){  //?????? ????????? ?????????
                                            noGoodsTextView.setVisibility(View.GONE);
                                            exist = true;

                                            getUid.add(uidSnapshot.getKey());

                                            goodsName = snapshot.getKey();
                                            getGoodsName.add(snapshot.getKey());

                                            getCtgryArr.add(ctgrySnapshot.getKey());

                                            for(DataSnapshot dSnapshot : snapshot.getChildren())
                                            {
                                                if (dSnapshot.getKey().equals("price")){
                                                    price = String.valueOf(dSnapshot.getValue());
                                                    getPrice.add(String.valueOf(dSnapshot.getValue()));
                                                    goodsCnt++;
                                                }
                                                else if (dSnapshot.getKey().equals("regiImageUrl")){
                                                    imgUrl = String.valueOf(dSnapshot.child("0").getValue());
                                                    ArrayList<String> arr;
                                                    for (int i=0; i<Integer.parseInt(picCount); i++){
                                                        getImgUrl.add(String.valueOf(dSnapshot.child(String.valueOf(i)).getValue()));
                                                    }
                                                    arr = new ArrayList<>(getImgUrl);
                                                    getImgUrlArrList.add(arr);
                                                    getImgUrl.clear();
                                                }
                                                else if (dSnapshot.getKey().equals("post")){
                                                    getPost.add(String.valueOf(dSnapshot.getValue()));
                                                }
                                                else if (dSnapshot.getKey().equals("parcel")){
                                                    getParcel.add(String.valueOf(dSnapshot.getValue()));
                                                }
                                                else if (dSnapshot.getKey().equals("directDealing")){
                                                    getDirectDealing.add(String.valueOf(dSnapshot.getValue()));
                                                }
                                                else if (dSnapshot.getKey().equals("condition")){
                                                    getCondition.add(String.valueOf(dSnapshot.getValue()));
                                                }
                                                else if (dSnapshot.getKey().equals("explain")){
                                                    getExplain.add(String.valueOf(dSnapshot.getValue()));
                                                }
                                                else if (dSnapshot.getKey().equals("regiDate")){
                                                    getRegiDate.add(String.valueOf(dSnapshot.getValue()));
                                                }
                                                else if (dSnapshot.getKey().equals("jjimCnt")){
                                                    getJjimCnt.add(String.valueOf(dSnapshot.getValue()));
                                                }
                                                else if (dSnapshot.getKey().equals("picCount")){
                                                    picCount = String.valueOf(dSnapshot.getValue());
                                                    getPicCount.add(String.valueOf(dSnapshot.getValue()));
                                                }
                                            }

                                            goodsCountTextView.setText(String.valueOf(goodsCnt));
                                            makeRecycler();
                                        }else {
                                            searchProgressBar.bringToFront();
                                            searchProgressBar.setVisibility(View.VISIBLE);
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    searchProgressBar.setVisibility(View.GONE);
                                                    if (!exist){  //?????? ????????? ?????????
                                                        imgUrl = "";
                                                        goodsName = "";
                                                        price = "";

                                                        makeRecycler();
                                                        goodsCountTextView.setText("0");
                                                        noGoodsTextView.setVisibility(View.VISIBLE);
                                                    }
                                                }
                                            }, 1100);

                                        }
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

    boolean changeBtnColor(Button btn, boolean click){
        if(click){
            btn.setBackgroundResource(R.drawable.non_click_round_btn);
            click=false;
        }else{
            btn.setBackgroundResource(R.drawable.filter_click_round_btn);
            click=true;
        }

        return click;
    } //?????? ?????? ?????? ??? ?????? ??????
}
