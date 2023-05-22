package com.spiderpocket.spider_android;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private WebView wv;

    private AdView adView;

    // Prd Ad
    public static final String AD_FULL_SCREEN_UNIT_ID = "ca-app-pub-9271167210149481/7872631214";
    public static final String AD_FULL_WIDTH_UNIT_ID = "ca-app-pub-9271167210149481/9354994031";

    // Test Ad
    //public static final String AD_FULL_SCREEN_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";
    //public static final String AD_FULL_WIDTH_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
    public static InterstitialAd interstitialAd;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wv = new WebView(getApplicationContext());

        Window window = getWindow();
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏状态栏
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);

        WebSettings webSettings = wv.getSettings();

        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDefaultTextEncodingName("utf-8");

        wv.getSettings().setSupportZoom(true);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        wv.requestFocus();
        //点击超链接的时候重新在原来的进程上加载URL
        wv.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        //wv.loadUrl("https://www.spiderpocket.net");
        wv.loadUrl("file:///android_asset/index.html");
        webSettings.setAllowFileAccess(true);

        //解决js加载本地文件的跨域问题
        try {
            Class<?> clazz = wv.getSettings().getClass();
            Method method = clazz.getMethod(
                    "setAllowUniversalAccessFromFileURLs", boolean.class);
            method.invoke(wv.getSettings(), true);
        } catch (IllegalArgumentException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        //setFullWidthAdview(wv);

        setFullScreenAdview(wv);

        //setContentView(wv);
    }

    //生命周期结束时销毁AdView实例，以避免内存泄漏
    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }

        super.onDestroy();
    }

    /**
     * 插入横幅广告
     *
     * @param wv
     */
    private void setFullWidthAdview(WebView wv) {
        // Google AdMob
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        adView = new AdView(this);
        adView.setAdSize(new AdSize(AdSize.FULL_WIDTH, 50));
        adView.setAdUnitId(AD_FULL_WIDTH_UNIT_ID);
        // 将视图添加到WebView的最底部
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        // 创建一个 RelativeLayout 对象
        RelativeLayout relativeLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams wvLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        relativeLayout.addView(wv, wvLayoutParams);
        relativeLayout.addView(adView, layoutParams);

        setContentView(relativeLayout);
    }

    /**
     * 插入全屏广告
     *
     * @param wv
     */
    private void setFullScreenAdview(WebView wv) {
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        loadAd();

        // 创建一个 RelativeLayout 对象
        RelativeLayout relativeLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams wvLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        relativeLayout.addView(wv, wvLayoutParams);

        wv.addJavascriptInterface(new JSInterface(), "JSInterface");
        setContentView(relativeLayout);
    }

    public void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(
                this,
                AD_FULL_SCREEN_UNIT_ID,
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        MainActivity.this.interstitialAd = interstitialAd;
                        //Log.i(TAG, "onAdLoaded");
                        //Toast.makeText(MainActivity.this, "onAdLoaded()", Toast.LENGTH_SHORT).show();
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        // Called when fullscreen content is dismissed.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        MainActivity.this.interstitialAd = null;
                                        Log.d("TAG", "The ad was dismissed.");
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        // Called when fullscreen content failed to show.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        MainActivity.this.interstitialAd = null;
                                        Log.d("TAG", "The ad failed to show.");
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        // Called when fullscreen content is shown.
                                        Log.d("TAG", "The ad was shown.");
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        interstitialAd = null;

                        String error =
                                String.format(
                                        "domain: %s, code: %d, message: %s",
                                        loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
                        //Toast.makeText(MainActivity.this, "onAdFailedToLoad() with error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (interstitialAd != null) {
            interstitialAd.show(this);
        } else {
            //Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
            startGame();
        }
    }

    private void startGame() {
        // Request a new ad if one isn't already loaded, hide the button, and kick off the timer.
        if (interstitialAd == null) {
            loadAd();
        }
    }

    private final class JSInterface {
        @JavascriptInterface
        public void showInterstitial() {
            wv.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.showInterstitial();
                }
            });
        }

        @JavascriptInterface
        public void recreate() {
            wv.post(new Runnable() {
                @Override
                public void run() {
                    wv.reload();
                }
            });
        }
    }

}


