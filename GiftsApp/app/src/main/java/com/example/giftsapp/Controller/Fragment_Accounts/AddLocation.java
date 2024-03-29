package com.example.giftsapp.Controller.Fragment_Accounts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.example.giftsapp.Controller.DistrictForm;
import com.example.giftsapp.Controller.LoginForm;
import com.example.giftsapp.Controller.ProvinceForm;
import com.example.giftsapp.Controller.VillageForm;
import com.example.giftsapp.Model.Helper;
import com.example.giftsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddLocation extends AppCompatActivity {

    EditText            edtName, edtPhone, edtDetailAddress;
    TextView            txtProvince, txtDistrict, txtVillage;
    Switch              switchDefaultAddress;
    Button              btnSave;
    FirebaseAuth        fAuth;
    FirebaseFirestore   fStore;
    FirebaseUser        user;
    String              userID, name = "", phone = "", province = "", district = "", village = "", detailAddress = "", addressID = "";
    Integer             provinceID, districtID;
    Boolean             isDefault = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Thêm địa chỉ");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2C4CC3")));

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), LoginForm.class));
            finish();
        }

        Init();

        txtVillage.setEnabled(false);
        txtDistrict.setEnabled(false);

        txtProvince.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProvinceForm.class);
                startActivityForResult(intent, 1);
            }
        });

        txtDistrict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DistrictForm.class);
                intent.putExtra("EXTRA_DOCUMENT_PROVINCE_ID", provinceID);
                startActivityForResult(intent, 2);
            }
        });

        txtVillage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), VillageForm.class);
                intent.putExtra("EXTRA_DOCUMENT_DISTRICT_ID", districtID);
                startActivityForResult(intent, 3);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetDataFromUI();
                if (CheckRequired()) {
                    if (isDefault) {
                        FindDefaultAddress();
                    } else {
                        AddNewAddress();
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    province = data.getStringExtra("EXTRA_DOCUMENT_PROVINCE");
                    provinceID = data.getIntExtra("EXTRA_DOCUMENT_PROVINCE_ID", 0);
                    txtProvince.setText(province);
                    txtProvince.setError(null);
                    txtDistrict.setText(null);
                    txtVillage.setText(null);
                    txtDistrict.setEnabled(true);
                    districtID = 0;
                    break;
                case 2:
                    district = data.getStringExtra("EXTRA_DOCUMENT_DISTRICT");
                    districtID = data.getIntExtra("EXTRA_DOCUMENT_DISTRICT_ID", 0);
                    txtDistrict.setText(district);
                    txtDistrict.setError(null);
                    txtVillage.setText(null);
                    txtVillage.setEnabled(true);
                    break;
                case 3:
                    village = data.getStringExtra("EXTRA_DOCUMENT_VILLAGE");
                    txtVillage.setText(village);
                    txtVillage.setError(null);
                    break;
                default:
                    break;
            }
        }
    }

    private void Init() {
        edtName                 = findViewById(R.id.edtName);
        edtPhone                = findViewById(R.id.edtPhone);
        txtProvince             = findViewById(R.id.txtProvince);
        txtDistrict             = findViewById(R.id.txtDistrict);
        txtVillage              = findViewById(R.id.txtVillage);
        edtDetailAddress        = findViewById(R.id.edtDetailAddress);
        switchDefaultAddress    = findViewById(R.id.switchDefaultAddress);
        btnSave                 = findViewById(R.id.btnSave);
        fAuth                   = FirebaseAuth.getInstance();
        fStore                  = FirebaseFirestore.getInstance();
        user                    = fAuth.getCurrentUser();
        userID                  = user.getUid();
    }

    private void AddNewAddress() {
        fStore.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    addressID = Helper.RandomID();
                    HashMap<String, Object> newAddress = new HashMap<String, Object>();
                    newAddress.put("ID", addressID);
                    newAddress.put("isDefault", isDefault);
                    newAddress.put("name", name);
                    newAddress.put("phone", phone);
                    newAddress.put("province", province);
                    newAddress.put("district", district);
                    newAddress.put("village", village);
                    newAddress.put("detailAddress", detailAddress);

                    fStore.collection("Users").document(userID).update("address", FieldValue.arrayUnion(newAddress)).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            finish();
                        }
                    });
                } else {
                    Log.d("TAG", "DocumentSnapshot Fail" + task.getException());
                }
            }
        });
    }

    private void GetDataFromUI() {
        isDefault = switchDefaultAddress.isChecked();
        name = edtName.getText().toString().trim();
        phone = edtPhone.getText().toString().trim();
        province = txtProvince.getText().toString().trim();
        district = txtDistrict.getText().toString().trim();
        village = txtVillage.getText().toString().trim();
        detailAddress = edtDetailAddress.getText().toString().trim();
    }

    private void SetNotDefault(String addressID, String name, String phone, String province, String district, String village, String detailAddress) {
        HashMap<String, Object> disableDefault = new HashMap<String, Object>();
        disableDefault.put("ID", addressID);
        disableDefault.put("isDefault", false);
        disableDefault.put("name", name);
        disableDefault.put("phone", phone);
        disableDefault.put("province", province);
        disableDefault.put("district", district);
        disableDefault.put("village", village);
        disableDefault.put("detailAddress", detailAddress);

        fStore.collection("Users").document(userID).update("address", FieldValue.arrayUnion(disableDefault)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                AddNewAddress();
            }
        });
    }

    private void FindDefaultAddress() {
        fStore.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.getData().get("address") != null) {
                        ArrayList<Map<String, Object>> addressArray = (ArrayList<Map<String, Object>>) document.getData().get("address");
                        if (addressArray.size() > 0) {
                            for (int i = 0; i < addressArray.size(); i++) {
                                if (addressArray.get(i).get("isDefault").toString().equals("true")) {
                                    HashMap<String, Object> defaultAddress = new HashMap<String, Object>();
                                    String addressID = addressArray.get(i).get("ID").toString();
                                    String name = addressArray.get(i).get("name").toString().trim();
                                    String phone = addressArray.get(i).get("phone").toString().trim();
                                    String province = addressArray.get(i).get("province").toString().trim();
                                    String district = addressArray.get(i).get("district").toString().trim();
                                    String village = addressArray.get(i).get("village").toString().trim();
                                    String detailAddress = addressArray.get(i).get("detailAddress").toString().trim();
                                    defaultAddress.put("ID", addressID);
                                    defaultAddress.put("isDefault", true);
                                    defaultAddress.put("name", name);
                                    defaultAddress.put("phone", phone);
                                    defaultAddress.put("province", province);
                                    defaultAddress.put("district", district);
                                    defaultAddress.put("village", village);
                                    defaultAddress.put("detailAddress", detailAddress);

                                    fStore.collection("Users").document(userID).update("address", FieldValue.arrayRemove(defaultAddress)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            SetNotDefault(addressID, name, phone, province, district, village, detailAddress);
                                        }
                                    });
                                    break;
                                }
                            }
                        } else {
                            AddNewAddress();
                        }

                    }
                }else {
                    Log.d("TAG", "DocumentSnapshot Fail" + task.getException());
                }
            }
        });
    }

    private boolean CheckRequired() {
        if (name.trim().equals("")) {
            edtName.setError("Bạn chưa nhập tên");
            return false;
        }
        edtName.setError(null);

        if (phone.trim().equals("")) {
            edtPhone.setError("Bạn chưa nhập số điện thoại");
            return false;
        }
        edtPhone.setError(null);

        if (province.trim().equals("")) {
            txtProvince.setError("Bạn chưa chọn tỉnh");
            return false;
        }
        txtProvince.setError(null);

        if (district.trim().equals("")) {
            txtDistrict.setError("Bạn chưa chọn huyện");
            return false;
        }
        txtDistrict.setError(null);

        if (village.trim().equals("")) {
            txtVillage.setError("Bạn chưa chọn xã");
            return false;
        }
        txtVillage.setError(null);

        if (detailAddress.trim().equals("")) {
            edtDetailAddress.setError("Bạn chưa nhập địa chỉ cụ thể");
            return false;
        }
        edtDetailAddress.setError(null);

        return true;
    }
}