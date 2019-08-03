package com.loogs.alc_4_0.phase1.challenge2.travelmantics;
/**
 * keytool -exportcert -list -v -alias androiddebugkey -keystore %USERPROFILE%\.android\debug.keystore
 *
 * Certificate fingerprints:
 * MD5:  9B:55:61:43:39:BE:CB:59:87:6E:19:33:8F:2A:F7:BF
 * SHA1: 2B:01:0A:2C:8D:54:1A:F1:56:8E:E0:37:DE:B7:50:9B:49:32:24:8E
 * SHA256: 8A:42:D8:63:68:B8:B3:F1:97:8C:94:10:98:72:3A:AC:5B:3A:2E:63:73:78:6A:13:FB:EB:DE:4E:55:F9:2E:97
 *
 * Client ID
 *  249492363886-klsav9n1ap3h2ju0ns4odq5k0mqvei6j.apps.googleusercontent.com
 *
 *  Client Secret
 *   _oFXTHzpZNuCfTob2XxhVQ_F
 *
 */

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealsActivity extends AppCompatActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private static final int PICTURE_RESULT = 42;
    EditText txtTitle;
    EditText txtDescription;
    EditText txtPrice;
    TravelDeal deal;
    ImageView imageView;
    private StorageReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        //FirebaseUtil.openFBReference("traveldeals");
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtDescription = (EditText) findViewById(R.id.txtDescription);
        txtPrice = (EditText) findViewById(R.id.txtPrice);
        imageView = (ImageView) findViewById(R.id.image);
        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if (deal == null) {
            deal = new TravelDeal();
        }
        this.deal = deal;
        txtTitle.setText(deal.getTitle());
        txtDescription.setText(deal.getDescription());
        txtPrice.setText(deal.getPrice());
        showImage(deal.getImageUrl());
        Button btnImage = findViewById(R.id.btnImage);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "Insert Picture"), PICTURE_RESULT);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deal Saved!", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this, "Deal Deleted!", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    private void saveDeal() {
        deal.setTitle(txtTitle.getText().toString());
        deal.setDescription(txtDescription.getText().toString());
        deal.setPrice(txtPrice.getText().toString());
        if(deal.getId() == null) {
            mDatabaseReference.push().setValue(deal);
        }
        else {
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }
    }

    private void deleteDeal() {
        if(deal == null) {
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_LONG).show();
        }
        //else {
            mDatabaseReference.child(deal.getId()).removeValue();
        //}
        Log.d("IMAGE name", deal.getImageName());
        if (deal.getImageName() != null && !deal.getImageName().isEmpty()) {
            StorageReference picRef = FirebaseUtil.mStorage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("IMAGE delete", "Image successfully deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("IMAGE delete", "Some shit happened..." + e.getMessage());
                }
            });
        }
    }

    private void backToList() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    private void clean() {
        txtTitle.setText("");
        txtDescription.setText("");
        txtPrice.setText("");
        txtTitle.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if (FirebaseUtil.isAdmin == true) {
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enableEditTexts(true);
            findViewById(R.id.btnImage).setEnabled(true);
        }
        else {
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enableEditTexts(false);
            findViewById(R.id.btnImage).setEnabled(false);
        }
        return true;
    }
/*

    // OLD CODE FROM TUTORIAL, NOT WORKING
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            StorageReference ref = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String url = taskSnapshot.getStorage().getDownloadUrl().toString();
                    String url1 = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                    String url2 = taskSnapshot.getStorage().toString();
                    String url3 = taskSnapshot.toString();
                    Log.d("IMAGE_URL: ", url);      // com.google.android.gms.tasks.zzu@c3234a9
                    Log.d("IMAGE_URL1: ", url1);    // com.google.android.gms.tasks.zzu@bb47f5c
                    Log.d("IMAGE_URL2: ", url2);    // gs://travelmantics-cbbfd.appspot.com/deals_pictures/image%3A115783
                    Log.d("IMAGE_URL3: ", url3);    // com.google.firebase.storage.UploadTask$TaskSnapshot@7d07265
                    //Toast.makeText(this, url1, Toast.LENGTH_LONG).show();
                    deal.setImageUrl(url2);
                    showImage(url2);
                }
            });
        }
    }
*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // This code is copied from https://www.codeproject.com/Questions/1248011/What-do-I-use-instead-of-getdownloadurl-in-android

        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            ref = FirebaseUtil.mStorageRef.child("deals_pictures").child(imageUri.getLastPathSegment());

            Task<Uri> urlTask = ref.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidCredentialsException e){
                            Log.e("IMAGE_: Exception01 ", "~{[ " + e.getMessage() + " ]}~");
                            Toast.makeText(getApplicationContext() , e.getMessage(), Toast.LENGTH_LONG).show();
                        } catch (FirebaseNetworkException e){
                            Log.e("IMAGE_: Exception02 ", "~{[ " + e.getMessage() + " ]}~");
                            Toast.makeText(getApplicationContext() , e.getMessage(), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.e("IMAGE_: Exception03 ", "~{[ " + e.getMessage() + " ]}~");
                            Toast.makeText(getApplicationContext() , e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    // Continue with the task to get the download URL
                    String getPath = ref.getPath();
                    //String getBucket = ref.getBucket();
                    //String getName = ref.getName();
                    //String getStorage = ref.getStorage().toString();
                    /* BELOW NOT WORKING!!
                    String pictureName = ref.getGeneration();
                    String pictureName = ref.getMetadataGeneration();
                    long pictureName = ref.getSizeBytes();
                    long pictureName = ref.getCreationTimeMillis();
                    long pictureName = ref.getUpdatedTimeMillis();
                    String pictureName = ref.getMd5Hash();
                    String pictureName = ref.getCacheControl();
                    String pictureName = ref.getContentDisposition();
                    String pictureName = ref.getContentEncoding();
                    String pictureName = ref.getContentLanguage();
                    String pictureName = ref.getContentType();
                    String pictureName = ref.getCustomMetadata();
                    String pictureName = ref.getCustomMetadataKeys();
                    */
                    Log.d("IMAGE_: getPath() ", "~{[ " + getPath + " ]}~");
                    //Log.d("IMAGE_: getBucket() ", "~{[ " + getBucket + " ]}~");
                    //Log.d("IMAGE_: getName() ", "~{[ " + getName + " ]}~");
                    //Log.d("IMAGE_: getStorage() ", "~{[ " + getStorage + " ]}~");
                    deal.setImageName(getPath);

                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        //Uri downloadUrl = task.getResult();
                        String url = task.getResult().toString();

                        Log.d("Setting IMAGE_URL: ", "~{[ " + url + " ]}~");
                        deal.setImageUrl(url);
                        showImage(url);
                        Toast.makeText(getApplicationContext() , url, Toast.LENGTH_LONG).show();
                        //mProgress.dismiss();
                    } else {
                        // Handle failures
                        Log.d("IMAGE_URL: ", "~{[ Some shit happened... ]}~");
                        Toast.makeText(getApplicationContext(), "Image upload failed", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void enableEditTexts(boolean isEnabled) {
        txtTitle.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
        findViewById(R.id.btnImage).setEnabled(isEnabled);
    }

    private void showImage(String url) {
        if (url != null && !url.isEmpty()) {
            Log.d("DealsShowIMAGE() URL ", "~{[ " + url + " ]}~");
            int width = (int) Math.floor(Resources.getSystem().getDisplayMetrics().widthPixels * 0.98);
            Picasso.with(this)
                    .load(url)
                    .resize(width, width*2/3)
                    .centerCrop()
                    .into(imageView);
        }
    }
}
