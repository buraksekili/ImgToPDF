package com.example.imgtopdf;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;                // image that will be converted into .pdf
    private static final int CODE = 1;  // CODE for onActivityResult
    EditText fileName;                  // user input for filename
    PdfDocument pdfDocument;            // to create .pdf file


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.ImageView);
        fileName = findViewById(R.id.fileName);

        // granting permission
        String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, galleryPermissions)) {
            Log.i("local-dev", "permissions are granted");
        } else {
            EasyPermissions.requestPermissions(this, "Access for storage",
                    101, galleryPermissions);
        }

        // TextWatcher is going to track fileName which is user input for pdf file name.
        // User will be informed always while EditText (filename) is changing.
        fileName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFileName();
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkFileName();
            }
        });
    }

    public boolean checkFileName() {
        TextView validity = findViewById(R.id.validity);
        Button saveButton = findViewById(R.id.saveButton);
        if (fileName.getText().toString().endsWith(".pdf")) {
            validity.setTextColor(Color.GREEN);
            validity.setText("Valid");
            saveButton.setVisibility(View.VISIBLE);
            return true;
        } else {
            validity.setTextColor(Color.RED);
            validity.setText("INVALID!");
            saveButton.setVisibility(View.INVISIBLE);
            return false;
        }
    }

    public void selectImage(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageURI = data.getData();

            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImageURI, filePath, null, null, null);
            if (cursor == null) {
                Toast.makeText(this, "Cursor Error", Toast.LENGTH_SHORT);
                return;
            }
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePath[0]);
            String path = cursor.getString(columnIndex);
            Bitmap bitmap = BitmapFactory.decodeFile(path);

            Toast.makeText(this, path.toString(), Toast.LENGTH_SHORT).show();
            imageView.setImageBitmap(bitmap);
            makePDF(bitmap);
        }

        if (requestCode == 123 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void makePDF(Bitmap bitmap) {
        pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#FFFFFF"));
        canvas.drawBitmap(bitmap, 0, 0, null);
        pdfDocument.finishPage(page);

        if (fileName.getText().toString().isEmpty()) {
            Toast.makeText(this, "You need to enter file name as follow\nyour_fileName.pdf", Toast.LENGTH_SHORT).show();
        }
    }


    public void saveFile(View v) {
        if (pdfDocument == null) {
            Log.i("local-dev", "pdfDocument in 'saveFile' function is null");
            return;
        }
        File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ImgToPDF");
        boolean isDirectoryCreated = root.exists();
        if (!isDirectoryCreated) {
            isDirectoryCreated = root.mkdir();
        }
        if (checkFileName()) {
            String userInputFileName = fileName.getText().toString();
            File file = new File(root, userInputFileName);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                pdfDocument.writeTo(fileOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            pdfDocument.close();
        }
        Log.i("local-dev", "'saveFile' function successfully done");
        Toast.makeText(this, "Successful! PATH:\n" + "Internal Storage/" + Environment.DIRECTORY_DOWNLOADS, Toast.LENGTH_SHORT).show();
    }

    public void showPDF(View view) {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 123);
    }
}
