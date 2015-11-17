package lb.prayforparis;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by USER on 11/16/2015.
 */
public class ImageActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imvMain;
    private LinearLayout lnlSave;
    private LinearLayout lnlShare;

    private Bitmap bmResult;
    private String filename;
    private Toast toast;
    private ProgressDialog progress_dialog;

    private boolean isShowAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        isShowAd = false;

        initView();
        initData();
    }

    private void initView() {
        imvMain = (ImageView) findViewById(R.id.imvMain);
        lnlSave = (LinearLayout) findViewById(R.id.lnlSave);
        lnlShare = (LinearLayout) findViewById(R.id.lnlShare);

        lnlSave.setOnClickListener(this);
        lnlShare.setOnClickListener(this);
    }

    private void initData() {
        bmResult = null;
        filename = null;

        openImagePicker();
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
//        if (isShowAd) {
            setResult(Activity.RESULT_OK, returnIntent);
//        } else {
//            setResult(Activity.RESULT_CANCELED, returnIntent);
//        }
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lnlSave:
                if (filename == null) {
                    showProgressDialog();
                    filename = saveBitmapToSDCard(bmResult);
                    hideProgressDialog();
                    showToast("Save at:" + filename);
                    isShowAd = true;
                } else {
                    showToast("Saved");
                }
                break;
            case R.id.lnlShare:
                showProgressDialog();
                if (filename == null) {
                    filename = saveBitmapToSDCard(bmResult);
                }
                shareImage(filename);
                hideProgressDialog();
                isShowAd = true;
                break;
        }
    }

    private void openImagePicker() {
        Crop.pickImage(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(data.getData());
        } else if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            handleCrop(resultCode, data);
        } else {
            finish();
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);

    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            Uri selectedImageUri = Crop.getOutput(result);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

                HandleCropAsync handleCropAsync = new HandleCropAsync();
                handleCropAsync.execute(bitmap);
            } catch (Exception e) {

            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            showToast(Crop.getError(result).getMessage());
        }
    }

    private class HandleCropAsync extends AsyncTask<Bitmap, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            Bitmap bitmap = params[0];
            int screenWidth = getScreenWidth();
            if (bitmap.getWidth() > screenWidth) {
                bitmap = Bitmap.createScaledBitmap(bitmap, screenWidth, screenWidth, false);
            }
            bmResult = doColorFilterFrance(bitmap);
//            bmResult = doColorFilterFrance(params[0]);
            return bmResult;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imvMain.setImageBitmap(bmResult);
            hideProgressDialog();
        }
    }

    private Bitmap doColorFilterFrance(Bitmap src) {

        float GSRed = 0.30f;
        float GSGreen = 0.59f;
        float GSBlue = 0.11f;

        int pixelColor;

        int A1, R1, B1, G1;


        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;

        int eachWidth = width / 3;
        int eachWidthx2 = eachWidth * 2;
        int eachWidthx3 = eachWidth * 3;


        // scan through all pixels
        for (int x = 0; x < eachWidth; ++x) {
            for (int y = 0; y < height; ++y) {

                pixelColor = src.getPixel(x, y);

                A1 = (int) (Color.alpha(pixelColor) * 0.9);
                R1 = Color.red(pixelColor);
                G1 = Color.green(pixelColor);
                B1 = Color.blue(pixelColor);

                R1 = G1 = B1 = (int) (R1 * GSRed + G1 * GSGreen + B1 * GSBlue);


                // apply filtering on each channel R, G, B
                A = (int) (A1 * 1);
                R = (int) (R1 * 0.1);
                G = (int) (G1 * 0.3);
                B = (int) (B1 * 1);

                // set new color pixel to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));




//                // get pixel color
//                pixel = src.getPixel(x, y);
//                // apply filtering on each channel R, G, B
//                A = (int) (Color.alpha(pixel) * 0.8);
//                R = (int) (Color.red(pixel) * 0.1);
//                G = (int) (Color.green(pixel) * 0.3);
//                B = (int) (Color.blue(pixel) * 1);
//
//                // set new color pixel to output bitmap
//                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        for (int x = eachWidth; x < eachWidthx2; ++x) {
            for (int y = 0; y < height; ++y) {

                pixelColor = src.getPixel(x, y);

                A1 = (int) (Color.alpha(pixelColor) * 0.9);
                R1 = Color.red(pixelColor);
                G1 = Color.green(pixelColor);
                B1 = Color.blue(pixelColor);

                R1 = G1 = B1 = (int) (R1 * GSRed + G1 * GSGreen + B1 * GSBlue);



//                A = (int) (A1 * 0.6);
//                R = R1;
//                G = G1;
//                B = B1;
//
//
//                R = G = B = (int) (R * GSRed + G * GSGreen + B * GSBlue);

                bmOut.setPixel(x, y, Color.argb(A1, R1, G1, B1));



            }
        }


        for (int x = eachWidthx2; x < eachWidthx3; ++x) {
            for (int y = 0; y < height; ++y) {


                pixelColor = src.getPixel(x, y);

                A1 = (int) (Color.alpha(pixelColor) * 0.9);
                R1 = Color.red(pixelColor);
                G1 = Color.green(pixelColor);
                B1 = Color.blue(pixelColor);

                R1 = G1 = B1 = (int) (R1 * GSRed + G1 * GSGreen + B1 * GSBlue);



                A = (int) (A1 * 1);
                R = (int) (R1 * 1);
                G = (int) (G1 * 0.1);
                B = (int) (B1 * 0.3);
                // set new color pixel to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    private Bitmap grayScale(Bitmap scrBitmap) {

        float GSRed = 0.30f;
        float GSGreen = 0.59f;
        float GSBlue = 0.11f;

        Bitmap finalBitmap = Bitmap.createBitmap(scrBitmap.getWidth(), scrBitmap.getHeight(), scrBitmap.getConfig());

        int pixelColor;

        int A, R, B, G;

        int height = scrBitmap.getHeight();
        int width = scrBitmap.getWidth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelColor = scrBitmap.getPixel(x, y);

                A = (int) (Color.alpha(pixelColor) * 0.8);
                R = Color.red(pixelColor);
                G = Color.green(pixelColor);
                B = Color.blue(pixelColor);


                R = G = B = (int) (R * GSRed + G * GSGreen + B * GSBlue);

                finalBitmap.setPixel(x, y, Color.argb(A, R, G, B));

            }
        }

        return finalBitmap;
    }

    private String saveBitmapToSDCard(Bitmap bitmap) {
        String imageName = "PrayForParis_" + System.currentTimeMillis();
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/PrayForParis");

        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        String fname = imageName + ".png";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return file.getPath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {

        }
    }

    private int getScreenWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    // Method to share any image.
    private void shareImage(String path) {
        Intent share = new Intent(Intent.ACTION_SEND);

        // If you want to share a png image only, you can do:
        // setType("image/png"); OR for jpeg: setType("image/jpeg");
        share.setType("image/*");

        // Make sure you put example png image named myImage.png in your
        // directory
        String imagePath = Environment.getExternalStorageDirectory()
                + "/loading.png";

        File imageFileToShare = new File(path);

        Uri uri = Uri.fromFile(imageFileToShare);
        share.putExtra(Intent.EXTRA_STREAM, uri);

        startActivity(Intent.createChooser(share, "Share Image!"));
    }

    private void showToast(String msg) {
        if (toast == null) {
            toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        }
        toast.setText(msg);
        toast.show();
    }

    public void showProgressDialog() {
        if (progress_dialog == null) {
            progress_dialog = new ProgressDialog(this);
        }

        if (!progress_dialog.isShowing()) {
            progress_dialog.setMessage("Progressing ... ");
            progress_dialog.setCancelable(false);
            progress_dialog.show();
        }
    }

    public void hideProgressDialog() {
        if (progress_dialog != null && progress_dialog.isShowing()) {
            progress_dialog.dismiss();
        }
    }
}
