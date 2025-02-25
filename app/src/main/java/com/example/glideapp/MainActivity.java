package com.example.glideapp;


import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 1;
    private ImageDatabaseHelper dbHelper;
    private ArrayList<String> imagePaths;
    private ArrayAdapter<String> adapter;
    private String currentImagePath;

    private Button captureImageButton;
    private ListView imageListView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        captureImageButton = findViewById(R.id.captureImageButton);
        imageListView = findViewById(R.id.imageListView);
        imageView = findViewById(R.id.selectedImageView);


        dbHelper = new ImageDatabaseHelper(this);
        imagePaths = dbHelper.getAllImagePaths();


        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, imagePaths);
        imageListView.setAdapter(adapter);

        captureImageButton.setOnClickListener(v -> captureImage());


        imageListView.setOnItemClickListener((parent, view, position, id) -> {
            String imagePath = imagePaths.get(position);
            Glide.with(this)
                    .load(imagePath)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(imageView);
        });
    }

    private void captureImage() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show();
                return;
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.imageapp.fileprovider",
                        photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        currentImagePath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            if (currentImagePath != null) {

                Glide.with(this)
                        .load(currentImagePath)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(imageView);


                if (dbHelper.insertImagePath(currentImagePath)) {
                    imagePaths.add(currentImagePath);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Image path is null", Toast.LENGTH_SHORT).show();
            }
        }
    }
}