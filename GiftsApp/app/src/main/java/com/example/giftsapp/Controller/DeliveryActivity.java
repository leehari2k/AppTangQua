package com.example.giftsapp.Controller;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.giftsapp.Adapter.CartAdapter;
import com.example.giftsapp.Controller.Fragment_Accounts.Location;
import com.example.giftsapp.Model.Address;
import com.example.giftsapp.Model.Bill;
import com.example.giftsapp.Model.CartItemModel;
import com.example.giftsapp.Model.Products;
import com.example.giftsapp.Model.StatusBill;
import com.example.giftsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.example.giftsapp.Controller.LoginForm.currentUser;

public class DeliveryActivity extends AppCompatActivity {

    private RecyclerView deliveryRecyclerView;
    private Button changORAddNewAddressBtn, EnterMessBtn;
    private TextView totalPrice_Vat;
    private Button stylePaymentBtn, PaymentBtn;
    private EditText Mess_edt; // nhập lời chúc
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    FirebaseUser user;
    String userID;
    Address addressSelected;

    // Shipping_details_layout
    private TextView tv_name,tv_address,tv_pinCode;
    private String getMess = " "; // lấy lời chúc
    private String typePayment=" ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Delivery");

        Init();

        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            addressSelected = (Address) data.getParcelableExtra("PARCEL_ADDRESS");
                            tv_address.setText("Vận chuyển đến: " +addressSelected.getDetailAddress().toString());
                            Log.d("ADD", "result=>"+addressSelected.getProvince());
                        }
                    }
                });

        AddressDetail();
        stylePaymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog chosePaymentDialog = new Dialog(DeliveryActivity.this);
                chosePaymentDialog.setContentView(R.layout.chose_payment);
                chosePaymentDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                chosePaymentDialog.setCancelable(false);
                RadioGroup radioGroup = chosePaymentDialog.findViewById(R.id.radioGroup);
                RadioButton radio_Btn_COD = chosePaymentDialog.findViewById(R.id.radioButton_COD);
                RadioButton radio_Btn_Onlice = chosePaymentDialog.findViewById(R.id.radioButton_Online);
                Button cancelBtn = chosePaymentDialog.findViewById(R.id.cancel_btn);
                Button okBtn = chosePaymentDialog.findViewById(R.id.ok_btn);

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chosePaymentDialog.dismiss();
                    }
                });

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        // checkedId trả về id của radio Button
                        switch (checkedId)
                        {
                            case R.id.radioButton_COD:
                                Toast.makeText(DeliveryActivity.this, "Bạn chọn thanh toán COD", Toast.LENGTH_SHORT).show();
                                okBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        typePayment = "COD";
                                        chosePaymentDialog.dismiss();
                                        Toast.makeText(DeliveryActivity.this, "Tạo hóa đơn thành công", Toast.LENGTH_SHORT).show();
                                        Log.d("ADD", "pay=>"+addressSelected.getProvince());
                                        Log.d("typePay", "pay=>"+typePayment);
                                        createBill(addressSelected.getID(),typePayment);
                                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                                break;
                            case  R.id.radioButton_Online:
                                Toast.makeText(DeliveryActivity.this, "Bạn chọn thanh toán Online", Toast.LENGTH_SHORT).show();
                                break;

                        }
                    }
                });

                chosePaymentDialog.show();
            }
        });

       /* PaymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DeliveryActivity.this, "Tạo hóa đơn thành công", Toast.LENGTH_SHORT).show();
                Log.d("ADD", "pay=>"+addressSelected.getProvince());
                Log.d("typePay", "pay=>"+typePayment);
                createBill(addressSelected.getID());
            }
        });*/

        if (user == null) {
            startActivity(new Intent(getApplicationContext(), LoginForm.class));
            finish();
        }


        changORAddNewAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                someActivityResultLauncher.launch(new Intent(getApplicationContext(), SelectLocationForm.class));
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void Init() {
        //shipping_details_layout = findViewById(R.id.include);
        fAuth = FirebaseAuth.getInstance();
        fStore= FirebaseFirestore.getInstance();
        user = fAuth.getCurrentUser();
        userID = user.getUid();
        deliveryRecyclerView = findViewById(R.id.delivery_recyclerView);
        changORAddNewAddressBtn = findViewById(R.id.change_or_add_address_btn);
        totalPrice_Vat = findViewById(R.id.total_priceD);
        stylePaymentBtn= findViewById(R.id.Style_payment_btn);
        PaymentBtn = findViewById(R.id.cart_continue_btn);
        Mess_edt = findViewById(R.id.edt_messenge);
        // Shipping_details_layout
        tv_name = findViewById(R.id.fullName);
        tv_address = findViewById(R.id.address);
        tv_pinCode = findViewById(R.id.pincode);
        // Shipping_details_layout

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(linearLayoutManager.VERTICAL);
        deliveryRecyclerView.setLayoutManager(linearLayoutManager);

        List<CartItemModel> cartItemModelList = new ArrayList<>();
        /*cartItemModelList.add(new CartItemModel(0,R.drawable.teddy,"Gấu bông",1,"200000.VND","220000.VND",1,111));
        cartItemModelList.add(new CartItemModel(0,R.drawable.teddy,"Gấu bông",0,"200000.VND","220000.VND",1,111));
        cartItemModelList.add(new CartItemModel(0,R.drawable.teddy,"Gấu bông",1,"200000.VND","220000.VND",1,111));
        cartItemModelList.add(new CartItemModel(0,R.drawable.teddy,"Gấu bông",0,"200000.VND","220000.VND",1,111));*/
        //cartItemModelList.add(new CartItemModel(1,4,"800000.VND","10000.VND","80000.VND","814000.VND"));

        CartAdapter cartAdapter = new CartAdapter(cartItemModelList);
        deliveryRecyclerView.setAdapter(cartAdapter);
        try {

            fStore.collection("Carts").document(currentUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if(documentSnapshot.exists()) {
                            int S =0;
                            final double[] totalPrice = {0};
                            final int[] finalS = {0};
                            final ArrayList<Map<String, Object>> productArray = (ArrayList<Map<String, Object>>) documentSnapshot.getData().get("ListProducts");
                            int l = productArray.size();
                            int countPro = 0;
                            for(Map<String,Object> obj: productArray)
                            {
                                String ProID = obj.get("ProductID").toString();
                                int quantity = Integer.parseInt(obj.get("Quantity").toString());
                                S += quantity;

                                int finalCountPro = countPro ;
                                Log.i("KQQ", finalS[0] +"");
                                fStore.collection("Products").document(ProID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> taskPro) {
                                        if (taskPro.isSuccessful()) {

                                            DocumentSnapshot documentSnapshotPro = taskPro.getResult();
                                            Long price = Long.parseLong(documentSnapshotPro.get("price").toString());
                                            Long Cuttedprice = price + 20000;

                                            cartItemModelList.add(new CartItemModel(0
                                                    ,documentSnapshotPro.get("imageUrl").toString()
                                                    , documentSnapshotPro.get("name").toString(), 1
                                                    , documentSnapshotPro.get("price").toString(), Cuttedprice.toString(), quantity, 111,ProID));

                                            cartAdapter.notifyDataSetChanged();
                                            totalPrice[0] = totalPrice[0] + price*quantity*1.0;
                                            finalS[0] +=quantity;
                                            totalPrice_Vat.setText((totalPrice[0]*(0.01) + totalPrice[0] +20000)+""+".VND");

                                            Log.i("KQQ111", finalS[0] +"");
                                            if( cartItemModelList.size() >= l )
                                            {
                                                double Cost_S; // tiền tổng cộng
                                                double SaveTotal = finalS[0] *20000;
                                                Cost_S = totalPrice[0]*(0.01) + totalPrice[0] +20000;
                                                cartItemModelList.add(new CartItemModel(1, finalS[0],totalPrice[0]+""+".VND"
                                                        ,"20000.VND","Bạn tiết kiệm "+SaveTotal+""+".VND cho đơn hàng này"
                                                        ,Cost_S+""+".VND"));
                                                cartAdapter.notifyDataSetChanged();
                                            }

                                        } else {
                                            String error = taskPro.getException().getMessage();
                                            Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                countPro++;
                            }
                        }
                        else{
                            Toast.makeText(DeliveryActivity.this, "Không có giỏ hàng", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(DeliveryActivity.this, "Không có giỏ hàng", Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }catch (Exception e)
        {
            Toast.makeText(DeliveryActivity.this, "Chưa có giỏ hàng", Toast.LENGTH_SHORT).show();
        }


        changORAddNewAddressBtn.setVisibility(View.VISIBLE);

    }
    private  void createBill(String AddressID,String typePay)
    {

        List<Products> productsListInBill = new ArrayList<>();
        try {

            fStore.collection("Carts").document(currentUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if(documentSnapshot.exists()) {
                            int S =0;
                            final long[] totalPrice = {0};
                            final int[] finalS = {0};
                            final ArrayList<Map<String, Object>> productArray = (ArrayList<Map<String, Object>>) documentSnapshot.getData().get("ListProducts");
                            int l = productArray.size();
                            for(Map<String,Object> obj: productArray)
                            {
                                String ProID = obj.get("ProductID").toString();
                                int quantity = Integer.parseInt(obj.get("Quantity").toString());
                                S += quantity;

                                fStore.collection("Products").document(ProID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> taskPro) {
                                        if (taskPro.isSuccessful()) {
                                            DocumentSnapshot documentSnapshotPro = taskPro.getResult();
                                            Long price = Long.parseLong(documentSnapshotPro.get("price").toString());
                                            productsListInBill.add(new Products(ProID,
                                                    documentSnapshotPro.get("name").toString(),
                                                    documentSnapshotPro.get("price").toString(),
                                                    documentSnapshotPro.get("imageUrl").toString(),
                                                    quantity));
                                            totalPrice[0] = (long) (totalPrice[0] + price*quantity);
                                            finalS[0] +=quantity;


                                            if( productsListInBill.size() >= l )
                                            {
                                                long Cost_S; // tiền tổng cộng
                                                long SaveTotal = finalS[0] *20000;
                                                Cost_S = (long) (totalPrice[0]/100 + totalPrice[0] +20000);
                                                Log.i("Cost",Cost_S+"");
                                                String StringChar = "abcdefgtre";
                                                Random random = new Random();
                                                long ID = random.nextLong();
                                                int r = random.nextInt(10);
                                                String a = StringChar.substring(0,r);
                                                String Bill_ID = ID+"" + a;

                                                Date date = java.util.Calendar.getInstance().getTime();
                                                List<StatusBill> statusBillList = new ArrayList<>();
                                                statusBillList.add(new StatusBill(true,"Chờ xác nhận",date));
                                                Map<String, Object> bill = new HashMap<>();
                                                Map<String,Object> listPro=new HashMap<>();
                                                Map<String,Object> statusbill = new HashMap<>();
                                                getMess = Mess_edt.getText().toString();
                                                bill.put("addressID",AddressID);
                                                bill.put("feeShip","20000");
                                                bill.put("paymentType",typePay);
                                                bill.put("createAt",date);
                                                bill.put("userID",currentUser);
                                                bill.put("totalPrice",Cost_S+"");
                                                bill.put("quantityProduct",finalS[0]);
                                                bill.put("messenge",getMess);

                                                fStore.collection("Bill").document(Bill_ID).set(bill)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(DeliveryActivity.this, "Lưu bill thành công", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(DeliveryActivity.this, "Lưu bill thất bại", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                                for(int i=0;i<productsListInBill.size();i++)
                                                {
                                                    listPro.put("imageUrl",productsListInBill.get(i).getImageUrl().toString());
                                                    listPro.put("name",productsListInBill.get(i).getName().toString());
                                                    listPro.put("price",productsListInBill.get(i).getPrice().toString());
                                                    listPro.put("productID",productsListInBill.get(i).getId().toString());
                                                    listPro.put("quantity",productsListInBill.get(i).getQuantity());
                                                    Log.i("DATASP",productsListInBill.get(i).getId().toString());
                                                    fStore.collection("Bill").document(Bill_ID).update("products", FieldValue.arrayUnion(listPro));
                                                }

                                                for(int x=0;x<statusBillList.size();x++)
                                                {
                                                    statusbill.put("isPresent",true);
                                                    statusbill.put("name","Chờ xác nhận");
                                                    statusbill.put("createAt",date);
                                                    fStore.collection("Bill").document(Bill_ID).update("status",FieldValue.arrayUnion(statusbill));
                                                }
                                                deleteCart();
                                            }
                                        } else {
                                            String error = taskPro.getException().getMessage();
                                            Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                        else{
                            Toast.makeText(DeliveryActivity.this, "Không có giỏ hàng", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(DeliveryActivity.this, "Không có giỏ hàng", Toast.LENGTH_SHORT).show();
                    }
                }
            });


        }catch (Exception e)
        {
            Toast.makeText(DeliveryActivity.this, "Chưa có giỏ hàng", Toast.LENGTH_SHORT).show();
        }

    }

    private void deleteCart()
    {
        //Tạo bill xong thì xóa giỏ hàng
        fStore.collection("Carts").document(currentUser).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("result","xóa giỏ hàng thành công");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("result","xóa giỏ hàng thất bại");
            }
        });
    }
    private void AddressDetail()
    {
        DocumentReference docRef = fStore.collection("Users").document(currentUser);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot.exists())
                    {
                        String name = documentSnapshot.getData().get("fullName").toString();
                        tv_name.setText("Khách hàng: "+ name);
                        if(addressSelected==null)
                        {
                            tv_address.setText("Vận chuyển đến");
                        }
                        else
                        {
                            tv_address.setText("Vận chuyển đến: "+ addressSelected.getDetailAddress().toString());
                        }
                        tv_pinCode.setText("PinCode: "+currentUser);
                    }
                }else
                {
                    Toast.makeText(DeliveryActivity.this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}