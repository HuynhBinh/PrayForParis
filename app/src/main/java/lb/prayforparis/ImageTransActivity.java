package lb.prayforparis;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by user on 11/18/2015.
 */
public class ImageTransActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView imvMain;
    private LinearLayout lnlSave;
    private LinearLayout lnlShare;

    private String filename;
    private Toast toast;
    private ProgressDialog progress_dialog;

    private boolean isShowAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_trans);

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
                SaveImageAsync saveImageAsync = new SaveImageAsync();
                saveImageAsync.execute();
                break;
            case R.id.lnlShare:
                ShareImageAsync shareImageAsync = new ShareImageAsync();
                shareImageAsync.execute();
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
                int screenWidth = getScreenWidth();
                if (bitmap.getWidth() > screenWidth) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, screenWidth, screenWidth, false);
                }
                imvMain.setImageBitmap(bitmap);
            } catch (Exception e) {

            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            showToast(Crop.getError(result).getMessage());
        }
    }

    private class SaveImageAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (filename == null) {
                Bitmap bitmap = convertViewToBitmap();
                filename = saveBitmapToSDCard(bitmap);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            hideProgressDialog();
            showToast("Saved");
            isShowAd = true;
        }
    }

    private class ShareImageAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (filename == null) {
                Bitmap bitmap = convertViewToBitmap();
                filename = saveBitmapToSDCard(bitmap);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            hideProgressDialog();
            shareImage(filename);
            isShowAd = true;
        }
    }

    private Bitmap convertViewToBitmap() {
        RelativeLayout rltImage = (RelativeLayout) findViewById(R.id.rltImage);
        rltImage.setDrawingCacheEnabled(true);
        rltImage.buildDrawingCache();
        return rltImage.getDrawingCache();
    }
    private String saveBitmapToSDCard(Bitmap bitmap) {
        String imageName = "christ" + System.currentTimeMillis();
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/christ");

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
            ContentValues image = new ContentValues();
            image.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, image);
            return file.getAbsolutePath();
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
