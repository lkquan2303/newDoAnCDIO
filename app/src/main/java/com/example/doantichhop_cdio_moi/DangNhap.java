package com.example.doantichhop_cdio_moi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DangNhap extends AppCompatActivity implements  View.OnClickListener, FirebaseAuth.AuthStateListener, GoogleApiClient.OnConnectionFailedListener {

    Button btdngoogle,btfb1,btndangnhap;
    TextView tvfaildn,tvqmk,tvdkm;
    EditText taikhoandangnhap, matkhaudangnhap;
    ProgressDialog progressDialog;
    GoogleApiClient apiClient;
    FirebaseFirestore db;
    List<String> permissionFacebook = Arrays.asList("email","public_profile");
    public static int codegoogle = 1000;
    // Check xem dn bang Google hay  FB
    public static int kiemtradangnhap = 0;
    FirebaseAuth firebaseAuth;
    LoginManager loginManager;
    CallbackManager mCallbackFacebook;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        LoginManager.getInstance().logOut();
//       FirebaseAuth.getInstance().signOut();
        mCallbackFacebook = CallbackManager.Factory.create();
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_dang_nhap);
        db = FirebaseFirestore.getInstance();
        btndangnhap = findViewById(R.id.btndangnhap);
        progressDialog = new ProgressDialog(this);
        taikhoandangnhap = findViewById(R.id.edemail);
        matkhaudangnhap = findViewById(R.id.edmk);
        btndangnhap.setOnClickListener(this);
        btfb1 = findViewById(R.id.btdnfb1);
        loginManager = LoginManager.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        btdngoogle = findViewById(R.id.btdngoogle);
        btdngoogle.setOnClickListener(this);
        tvqmk = findViewById(R.id.tvqmk);
        tvqmk.setOnClickListener(this);
        tvdkm = findViewById(R.id.tvdkm);
        tvdkm.setOnClickListener(this);
        btfb1.setOnClickListener(this);
        getinfor();
//        hashkey();

    }
    // Hàm lấy thông tin từ GOOGLE
    private  void getinfor()
    {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder()
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Gọi API
        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, (GoogleApiClient.OnConnectionFailedListener) this)
                // Them API vao
                .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
                .build();
    }
    private void LoginWithEmailPass()
    {
        String email = taikhoandangnhap.getText().toString();
        String matkhau = matkhaudangnhap.getText().toString();
        firebaseAuth.signInWithEmailAndPassword(email, matkhau).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    progressDialog.dismiss();
                }
                else
                {
                    tvfaildn.setText(" Email Hoặc Mật Khẩu Không Hợp Lệ");
                    progressDialog.dismiss();
                }
            }
        });
    }
    @Override
    public void onClick(View view) {
        int id = view.getId();
        String userid = FirebaseAuth.getInstance().getUid();
        switch (id){
            case R.id.btdngoogle:
                progressDialog.setMessage("Đang xử lý, chờ xíu");
                progressDialog.show();
                logingoogle(apiClient);
                break;
            case R.id.btndangnhap:
                String mailinput = taikhoandangnhap.getText().toString();
                String pass = matkhaudangnhap.getText().toString();
                boolean resultcheck = checkmail(mailinput);
                if(mailinput.equals("") == true ||pass.equals("") == true )
                {
                    tvfaildn.setText("Email Hoặc Mật Khẩu Không Hợp Lệ");
                }
                else if(resultcheck)
                {
                    progressDialog.setMessage("Đang xử lý, chờ xíu");
                    progressDialog.show();
                    LoginWithEmailPass();
                }else
                {
                    tvfaildn.setText("Email Hoặc Mật Khẩu Không Hợp Lệ");
                }
                break;
            case R.id.tvqmk:
                quenmatkhau();
                break;
            case  R.id.btdnfb1:
                progressDialog.setMessage("Đang xử lý, chờ xíu");
                progressDialog.show();
                DangNhapFacebook();
                break;
            case R.id.tvdkm:
                Intent intent = new Intent(DangNhap.this, DangKy.class);
                startActivity(intent);
                finish();
                break;
        }
    }
    //Mo giao dien dang nhap google
    private void logingoogle(GoogleApiClient apiClient)
    {
        kiemtradangnhap = 1;
        Intent intent  = Auth.GoogleSignInApi.getSignInIntent(apiClient);
        startActivityForResult(intent, codegoogle);
    }
    private void quenmatkhau()
    {
//        hashkey();
        Intent intent = new Intent(DangNhap.this, QuenMatKhau.class);
        startActivity(intent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == codegoogle)
        {
            if(resultCode == RESULT_OK)
            {
                // Lấy Thông Tin Đăng Nhập (Lay Token google)
                GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                GoogleSignInAccount account = googleSignInResult.getSignInAccount();
                String tokenid = account.getIdToken();
                KiemTraDangNhap(tokenid);
            }
        }else
        {
            mCallbackFacebook.onActivityResult(requestCode, resultCode,data);
        }
    }
    private void KiemTraDangNhap(String tokenid)
    {
        if(kiemtradangnhap == 1)
        {
            AuthCredential credential = GoogleAuthProvider.getCredential(tokenid, null);
            // Kiem Tra Dang Nhap Thanh Cong ??
            firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {

                    }
                }
            });
        }else if(kiemtradangnhap == 2)
        {
            AuthCredential credential = FacebookAuthProvider.getCredential(tokenid);
            firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                }
            });
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(this);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null)
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private boolean checkmail(String inputmail)
    {
        return Patterns.EMAIL_ADDRESS.matcher(inputmail).matches();
    }

    private void DangNhapFacebook(){
        loginManager.logInWithReadPermissions(this,permissionFacebook);
        loginManager.registerCallback( mCallbackFacebook, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                kiemtradangnhap = 2;
                String tokenID = loginResult.getAccessToken().getToken();
                KiemTraDangNhap(tokenID);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    private void hashkey()
    {
        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo("com.example.doantichhop_cdio_moi", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                //String something = new String(Base64.encodeBytes(md.digest()));
                Log.e("hash key", something);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
    }
}
