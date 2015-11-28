package lb.prayforparis;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private int REQUEST_CODE = 123;

    private LinearLayout lnlFromCamera;
    private LinearLayout lnlFromGallery;
    private LinearLayout lnlRateApp;
    private LinearLayout lnlShareApp;

    private InterstitialAd interstitial;
    protected AdRequest adRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialAdmob();

        initView();
        initData();
    }

    private void initView() {
        lnlFromCamera = (LinearLayout) findViewById(R.id.lnlFromCamera);
        lnlFromGallery = (LinearLayout) findViewById(R.id.lnlFromGallery);
        lnlRateApp = (LinearLayout) findViewById(R.id.lnlRateApp);
        lnlShareApp = (LinearLayout) findViewById(R.id.lnlShareApp);
    }

    private void initData() {
        lnlFromCamera.setOnClickListener(this);
        lnlFromGallery.setOnClickListener(this);
        lnlRateApp.setOnClickListener(this);
        lnlShareApp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lnlFromCamera:
                Intent intentCamera = new Intent(this, ImageTransActivity.class);
                intentCamera.putExtra("camera", true);
                startActivityForResult(intentCamera, REQUEST_CODE);
                break;
            case R.id.lnlFromGallery:
                Intent intentImage = new Intent(this, ImageTransActivity.class);
                startActivityForResult(intentImage, REQUEST_CODE);
                break;
            case R.id.lnlRateApp:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName().toString())));
                break;
            case R.id.lnlShareApp:
                shareTextFacebook("https://play.google.com/store/apps/details?id=" + getPackageName().toString());
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            showFullAd();
        }
    }

    // Method to share any text.
    private void shareTextFacebook(String text) {
        Intent share = new Intent(Intent.ACTION_SEND);

        share.setType("text/plain");

        share.putExtra(Intent.EXTRA_TEXT, text);

        startActivity(Intent.createChooser(share, "Share App!"));
    }

    private void initialAdmob() {
        adRequest = new AdRequest.Builder().build();

        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId("ca-app-pub-6956931160448072/7189551949");

        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // TODO Auto-generated method stub
                super.onAdLoaded();
            }

            @Override
            public void onAdClosed() {
                // TODO Auto-generated method stub
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // TODO Auto-generated method stub
                super.onAdFailedToLoad(errorCode);
            }

        });

        interstitial.loadAd(adRequest);
    }

    private void showFullAd() {
        if (interstitial != null) {
            if (interstitial.isLoaded()) {
                interstitial.show();
            }
        }
    }
}
