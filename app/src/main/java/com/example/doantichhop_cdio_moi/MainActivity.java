package com.example.doantichhop_cdio_moi;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.doantichhop_cdio_moi.Utils.BitmapUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    private static final int CHOOSE_IMAGE = 133;
    final int RQS_IMAGE1 = 1;
    private TextView viewGallery;
    private ImageView imgPreview;
    private EditText imgDescription;
    private EditText id_timkiem;
    Matrix matrix;
    private ProgressBar uploadProgress;
    private DrawingView drawView;
    private float smallBrush, mediumBrush, largeBrush;
    private ImageButton currPaint, drawBtn, eraseBtn;
    private ImageButton bt_timkiem, img_camera;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;
    private Bitmap originalImage, finalImage, bmp, alteredBitmap;
    Uri source,imageFileUri;
    FirebaseAuth myau;
    public static final int SELECT_GALLERY_IMAGE = 101;
    static final int REQUEST_IMAGE_CAPTURE = 33, REQUEST_IMAGE_FIREBASE = 1000;
    static final int FIREBASE_IMAGE = 2;

    Paint paint;
    float downx = 0;
    float downy = 0;
    float upx = 0;
    float upy = 0;
    Canvas canvas;
    Bitmap bitmapMaster;
    int prvX, prvY;
    Paint paintDraw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myau = FirebaseAuth.getInstance();
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);
        uploadProgress = findViewById(R.id.uploadProgress);
        viewGallery = findViewById(R.id.viewGallery);
        imgDescription = findViewById(R.id.imgDescription);
        imgPreview = findViewById(R.id.imgPreview);
        drawView = findViewById(R.id.drawing);
        img_camera = findViewById(R.id.img_camera);
        img_camera.setOnClickListener(this);
//        drawBtn.setOnClickListener(this);
        bt_timkiem = findViewById(R.id.bt_timkiem);
        bt_timkiem.setOnClickListener(this);
//        eraseBtn.setOnClickListener(this);
        id_timkiem = findViewById(R.id.id_timkiem);
        LinearLayout paintLayout = findViewById(R.id.paint_colors);
        paintDraw = new Paint();
        paintDraw.setStyle(Paint.Style.FILL);
        paintDraw.setStrokeWidth(40);


        currPaint = (ImageButton) paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String name_storage = firebaseUser.getUid();
        mStorageRef = FirebaseStorage.getInstance().getReference(name_storage);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(name_storage);
//        drawView.setBackgroundResource(R.drawable.add);
        viewGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ViewImageActivity.class);
                startActivity(intent);
            }
        });

        loadImage_FireBase();
        loadimage_userchoose();

//

        id_timkiem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TimKiem.class);
                startActivity(intent);
            }
        });
//        imgPreview.setOnTouchListener(this);
//        imgPreview.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//                int action = event.getAction();
//                int x = (int) event.getX();
//                int y = (int) event.getY();
//                switch (action) {
//                    case MotionEvent.ACTION_DOWN:
//                        prvX = x;
//                        prvY = y;
//                        drawOnProjectedBitMap((ImageView) v, bitmapMaster, prvX, prvY, x, y);
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        drawOnProjectedBitMap((ImageView) v, bitmapMaster, prvX, prvY, x, y);
//                        prvX = x;
//                        prvY = y;
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        drawOnProjectedBitMap((ImageView) v, bitmapMaster, prvX, prvY, x, y);
//                        break;
//                }
//
//                return true;
//            }
//        });
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String anh = edt_timkiem.getText().toString();
//                String url_image =listItems.get(position) +".jpg";
//                originalImage = BitmapUtils.getBitmapFromAssets(MainActivity.this, url_image, 0, 0);
//                finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
//                imgPreview.setImageBitmap(originalImage);
//                edt_timkiem.setText(null);
//            }
//        });
    }
    private void showFileChoose() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, CHOOSE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap tempBitmap;
//        if (resultCode == RESULT_OK && requestCode == SELECT_GALLERY_IMAGE) {
//            source = data.getData();
//            try {
//                tempBitmap = BitmapFactory.decodeStream(
//                        getContentResolver().openInputStream(source));
//
//                canvas = new Canvas(bitmapMaster);
//                canvas.drawBitmap(tempBitmap, 0, 0, null);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this, data.getData(), 800, 800);
//            originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//            finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
//
// //           imgPreview.setImageBitmap(originalImage);
// //           drawView.setBackground(getResources().getDrawable(R.drawable.add));
//
//            imgPreview.setImageBitmap(originalImage);
//
//            Log.d("Tag : ", "url" + imgUrl);
//            bitmap.recycle();
//        }
////        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
////            Bundle extras = data.getExtras();
////            Bitmap imageBitmap = (Bitmap) extras.get("data");
//// //           drawView.getCanvasBitmap(imageBitmap);
////            imgPreview.setImageBitmap(imageBitmap);
////            drawView.startNew();
////        }
//        else
            if ( requestCode == RQS_IMAGE1 ) {
//                    source = data.getData();
//                    try {
//                        //tempBitmap is Immutable bitmap,
//                        //cannot be passed to Canvas constructor
//                        tempBitmap = BitmapFactory.decodeStream(
//                                getContentResolver().openInputStream(source));
//
//                        Bitmap.Config config;
//                        if (tempBitmap.getConfig() != null) {
//                            config = tempBitmap.getConfig();
//                        } else {
//                            config = Bitmap.Config.ARGB_8888;
//                        }
//
//                        //bitmapMaster is Mutable bitmap
//                        bitmapMaster = Bitmap.createBitmap(
//                                tempBitmap.getWidth(),
//                                tempBitmap.getHeight(),
//                                config);
//
//                        canvas = new Canvas(bitmapMaster);
//                        canvas.drawBitmap(tempBitmap, 0, 0, null);
//
//                        imgPreview.setImageBitmap(bitmapMaster);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }


                if (resultCode == RESULT_OK) {
                    imageFileUri = data.getData();
                    try {
                        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                        bmpFactoryOptions.inJustDecodeBounds = true;
                        bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(
                                imageFileUri), null, bmpFactoryOptions);

                        bmpFactoryOptions.inJustDecodeBounds = false;
                        bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(
                                imageFileUri), null, bmpFactoryOptions);

                        alteredBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp
                                .getHeight(), bmp.getConfig());
                        canvas = new Canvas(alteredBitmap);
                        paint = new Paint();
                        paint.setColor(Color.GREEN);
                        paint.setStrokeWidth(30);
                        matrix = new Matrix();
                        canvas.drawBitmap(bmp, matrix, paint);

                        imgPreview.setImageBitmap(alteredBitmap);
                        imgPreview.setOnTouchListener(this);
                    } catch (Exception e) {
                        Log.v("ERROR", e.toString());
                    }

                }
        }else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                final Bitmap imageBitmap = (Bitmap) extras.get("data");
                imgPreview.setImageBitmap(imageBitmap);
        }
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK) {
//            Uri imageFileUri = data.getData();
//            try {
//                Bitmap bitmap = BitmapUtils.getBitmapFromGallery(this, data.getData(), 800, 800);
////                BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
////                bmpFactoryOptions.inJustDecodeBounds = true;
////                bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(
////                        imageFileUri), null, bmpFactoryOptions);
////                bmpFactoryOptions.inJustDecodeBounds = false;
////                bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(
////                        imageFileUri), null, bmpFactoryOptions);
////
////                alteredBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp
////                        .getHeight(), bmp.getConfig());
//                canvas = new Canvas(alteredBitmap);
//                paint = new Paint();
//                paint.setColor(Color.GREEN);
//                paint.setStrokeWidth(5);
//                matrix = new Matrix();
//
//                bmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//                alteredBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//                finalImage =bmp.copy(Bitmap.Config.ARGB_8888, true);
//                canvas.drawBitmap(bmp, matrix, paint);
//
//
//
//
//                finalImage = alteredBitmap.copy(Bitmap.Config.ARGB_8888, true);
//                imgPreview.setImageBitmap(bmp);
//                imgPreview.setOnTouchListener(MainActivity.this);
//            } catch (Exception e) {
//                Log.v("ERROR", e.toString());
//            }
//        }


    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        if (imageFileUri != null) {
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageFileUri));
            mUploadTask = fileReference.putFile(imageFileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    uploadProgress.setProgress(0);
                                }
                            }, 500);
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Upload upload = new Upload(imgDescription.getText().toString().trim(), uri.toString());
                                    String uploadID = mDatabaseRef.push().getKey();
                                    mDatabaseRef.child(uploadID).setValue(upload);
                                    Toast.makeText(MainActivity.this, "Upload successfully", Toast.LENGTH_LONG).show();
                                    //      imgPreview.setImageResource(R.drawable.imagepreview);
                                    imgDescription.setText("");
                                }
                            });


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            uploadProgress.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(MainActivity.this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadImage_FireBase() {
        Intent intent = getIntent();
        String URL_Image = intent.getStringExtra("url");
        if(URL_Image != null)
        {
            Picasso.with(this).load(URL_Image).into(imgPreview);
            try {
                BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
                bmpFactoryOptions.inJustDecodeBounds = true;
                bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(
                        Uri.parse(URL_Image)), null, bmpFactoryOptions);

                bmpFactoryOptions.inJustDecodeBounds = false;
                bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(
                        Uri.parse(URL_Image)), null, bmpFactoryOptions);

                alteredBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp
                        .getHeight(), bmp.getConfig());
                canvas = new Canvas(alteredBitmap);
                paint = new Paint();
                paint.setColor(Color.GREEN);
                paint.setStrokeWidth(30);
                matrix = new Matrix();
                canvas.drawBitmap(bmp, matrix, paint);
                imgPreview.setOnTouchListener(this);
            } catch (Exception e) {
                Log.v("ERROR", e.toString());
            }
        }
    }
//    public void paintClicked(View view) {
//        drawView.setErase(false);
//        if (view != currPaint) {
//            ImageButton imgView = (ImageButton) view;
//            String color = view.getTag().toString();
//            drawView.setColor(color);
//            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
//            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
//            currPaint = (ImageButton) view;
//        }
//        drawView.setBrushSize(drawView.getLastBrushSize());
//    }


    @Override
    public void onClick(View v) {
//        drawView.setErase(false);
//        if (v.getId() == R.id.draw_btn) {
//            final Dialog brushDialog = new Dialog(this);
//            brushDialog.setTitle("Brush size:");
//            brushDialog.setContentView(R.layout.brush_chooser);
//            ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
//            smallBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    drawView.setBrushSize(smallBrush);
//                    drawView.setLastBrushSize(smallBrush);
//                    brushDialog.dismiss();
//                }
//            });
//            ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
//            mediumBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    drawView.setBrushSize(mediumBrush);
//                    drawView.setLastBrushSize(mediumBrush);
//                    brushDialog.dismiss();
//                }
//            });
//
//            ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
//            largeBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    drawView.setBrushSize(largeBrush);
//                    drawView.setLastBrushSize(largeBrush);
//                    brushDialog.dismiss();
//                }
//            });
//            brushDialog.show();
//        } else if (v.getId() == R.id.erase_btn) {
//            final Dialog brushDialog = new Dialog(this);
//            brushDialog.setTitle("Eraser size:");
//            brushDialog.setContentView(R.layout.brush_chooser);
//            ImageButton smallBtn = brushDialog.findViewById(R.id.small_brush);
//            smallBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    drawView.setErase(true);
//                    drawView.setBrushSize(smallBrush);
////                    drawView.setColor("#FFFFFF");
//                    currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
//                    //                   drawView.setBrushSize(drawView.getLastBrushSize());
//                    brushDialog.dismiss();
//                }
//            });
//            drawView.setErase(false);
//            ImageButton mediumBtn = brushDialog.findViewById(R.id.medium_brush);
//            mediumBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    drawView.setErase(true);
//                    drawView.setBrushSize(mediumBrush);
////                    drawView.setColor("#FFFFFF");
//                    currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
//                    //                   drawView.setBrushSize(drawView.getLastBrushSize());
//                    brushDialog.dismiss();
//
//                }
//            });
//            drawView.setErase(false);
//            ImageButton largeBtn = brushDialog.findViewById(R.id.large_brush);
//            largeBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    drawView.setErase(true);
//                    drawView.setBrushSize(largeBrush);
////                    drawView.setColor("#FFFFFF");
//                    currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
//                    //                   drawView.setBrushSize(drawView.getLastBrushSize());
//                    brushDialog.dismiss();
//                }
//            });
//            drawView.setErase(false);
//            brushDialog.show();
//        } else if (v.getId() == R.id.bt_timkiem) {
////            if (!edt_timkiem.getText().toString().equals("")) {
////
////                String anh = edt_timkiem.getText().toString();
////                originalImage = BitmapUtils.getBitmapFromAssets(this, anh + ".jpg", 0, 0);
////                finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);
////                imgPreview.setImageBitmap(originalImage);
////                edt_timkiem.setText(null);
////                drawView.startNew();
////            } else {
////                Toast.makeText(MainActivity.this, "Bạn không được để trống", Toast.LENGTH_SHORT).show();
////            }
//
//        } else

            if (v.getId() == R.id.img_camera) {
            dispatchTakePictureIntent();
        }
    }


    private void saveImageToGallery() {
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            final String path = BitmapUtils.insertImage(getContentResolver(), finalImage, System.currentTimeMillis() + "_profile.jpg", null);
                            if (!TextUtils.isEmpty(path)) {

                            } else {

                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Permissions are not granted!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    private void openImage(String path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path), "image/*");
        startActivity(intent);
    }

    private void openImageFromGallery() {
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, SELECT_GALLERY_IMAGE);
                        } else {
                            Toast.makeText(getApplicationContext(), "Permissions are not granted!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
//        Intent choosePictureIntent = new Intent(
//                Intent.ACTION_PICK,
//                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(choosePictureIntent, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_open:
 //               openImageFromGallery();
                Load_Image();
                return true;
            case R.id.action_save:
     //           saveImageToGallery();
                Save_Image();
                uploadImage();
                return true;
            case R.id.action_dx:

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, DangNhap.class);
                startActivity(intent);
                break;
            case R.id.action_qmk:
                QuenMk();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        int action = event.getAction();
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                downx = event.getX();
//                downy = event.getY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                upx = event.getX();
//                upy = event.getY();
//                canvas.drawLine(downx, downy, upx, upy, paint);
//                imgPreview.invalidate();
//                downx = upx;
//                downy = upy;
//                break;
//            case MotionEvent.ACTION_UP:
//                upx = event.getX();
//                upy = event.getY();
//                canvas.drawLine(downx, downy, upx, upy, paint);
//                imgPreview.invalidate();
//                break;
//            case MotionEvent.ACTION_CANCEL:
//                break;
//            default:
//                break;
//        }
//        return true;
//    }

    private void drawOnProjectedBitMap(ImageView iv, Bitmap bm,
                                       float x0, float y0, float x, float y) {
        if (x < 0 || y < 0 || x > iv.getWidth() || y > iv.getHeight()) {
            //outside ImageView
            return;
        } else {

//            float ratioWidth = (float) bm.getWidth() / (float) iv.getWidth();
//            float ratioHeight = (float) bm.getHeight() / (float) iv.getHeight();

            canvas.drawLine(downx, downy, upx, upy, paintDraw);
            imgPreview.invalidate();
        }
    }

    private void Load_Image()
    {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RQS_IMAGE1);
    }
    public void paintClicked(View view) {
        ImageButton imgView = (ImageButton) view;
        String color = view.getTag().toString();
        paintDraw.setColor(Color.parseColor(color));
        paint.setColor(Color.parseColor(color));
        imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
    }
    private void Save_Image()
    {
//        if (bitmapMaster != null) {
//            ContentValues contentValues = new ContentValues(3);
//            contentValues.put(MediaStore.Audio.Media.DISPLAY_NAME, "Draw On Me");
//
//            Uri imageFileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
//            try {
//                OutputStream imageFileOS = getContentResolver().openOutputStream(imageFileUri);
//                bitmapMaster.compress(Bitmap.CompressFormat.JPEG, 90, imageFileOS);
//                Toast t = Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT);
//                t.show();
//
//            } catch (Exception e) {
//                Log.v("EXCEPTION", e.getMessage());
//            }
//        }
        if (alteredBitmap != null) {
            ContentValues contentValues = new ContentValues(3);
            contentValues.put(MediaStore.Audio.Media.DISPLAY_NAME, "Draw On Me");

             imageFileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            try {
                OutputStream imageFileOS = getContentResolver().openOutputStream(imageFileUri);
                alteredBitmap.compress(Bitmap.CompressFormat.JPEG, 90, imageFileOS);
                Toast t = Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT);
                t.show();

            } catch (Exception e) {
                Log.v("EXCEPTION", e.getMessage());
            }
        }
    }
    private void loadimage_userchoose()
    {
        Intent intent = getIntent();
        String url_image = intent.getStringExtra("name_image");
        if(url_image != null) {
            originalImage = BitmapUtils.getBitmapFromAssets(this, url_image + ".jpg", 0, 0);
            finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true);

            imgPreview.setImageBitmap(originalImage);
        }

    }

    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downx = event.getX();
                downy = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                upx = event.getX();
                upy = event.getY();
                canvas.drawLine(downx, downy, upx, upy, paint);
                imgPreview.invalidate();
                downx = upx;
                downy = upy;
                break;
            case MotionEvent.ACTION_UP:
                upx = event.getX();
                upy = event.getY();
                canvas.drawLine(downx, downy, upx, upy, paint);
                imgPreview.invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
    }

    private void QuenMk()
    {
        FirebaseUser tk = FirebaseAuth.getInstance().getCurrentUser();
        String emaimk = tk.getEmail().toString();
        myau.sendPasswordResetEmail(emaimk).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Đã gửi email đặt lại mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
