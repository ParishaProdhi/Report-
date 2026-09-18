package com.taxreport.calculator;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.OutputStream;

/**
 * Hosts the tax-report calculator (assets/index.html) in a WebView and supplies the
 * three things a plain WebView cannot do on its own:
 *  - pick Excel / Word / saved-data files (the page's file inputs),
 *  - save the Word report and data files (through Android's "Save as" screen),
 *  - print the report, which offers "Save as PDF".
 */
public class MainActivity extends Activity {

    private static final int REQ_OPEN_FILES = 1;
    private static final int REQ_SAVE_FILE = 2;

    private WebView web;
    private ValueCallback<Uri[]> fileCallback;
    private byte[] pendingBytes;
    private String pendingName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);      // inputs are autosaved in localStorage
        s.setTextZoom(100);                // keep the report layout independent of the phone's font size

        web.addJavascriptInterface(new Bridge(), "AndroidBridge");

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("file".equals(uri.getScheme())) return false;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));   // e-mail and web links open outside the app
                } catch (Exception ignored) {
                }
                return true;
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");   // the page checks the file itself; phones often mislabel .xlsx/.docx types
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE);
                try {
                    startActivityForResult(Intent.createChooser(intent, "ফাইল বেছে নিন"), REQ_OPEN_FILES);
                    return true;
                } catch (Exception e) {
                    fileCallback = null;
                    toast("ফাইল খোলার কোনো অ্যাপ পাওয়া যায়নি");
                    return false;
                }
            }
        });

        web.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_OPEN_FILES) {
            Uri[] result = null;
            if (resultCode == RESULT_OK && data != null) {
                ClipData clip = data.getClipData();
                if (clip != null) {
                    result = new Uri[clip.getItemCount()];
                    for (int i = 0; i < clip.getItemCount(); i++) result[i] = clip.getItemAt(i).getUri();
                } else if (data.getData() != null) {
                    result = new Uri[]{data.getData()};
                }
            }
            if (fileCallback != null) fileCallback.onReceiveValue(result);
            fileCallback = null;
        } else if (requestCode == REQ_SAVE_FILE) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null && pendingBytes != null) {
                try (OutputStream out = getContentResolver().openOutputStream(data.getData())) {
                    if (out == null) throw new Exception("no output stream");
                    out.write(pendingBytes);
                    toast("সংরক্ষণ হয়েছে: " + pendingName);
                } catch (Exception e) {
                    toast("সংরক্ষণ করা যায়নি: " + e.getMessage());
                }
            }
            pendingBytes = null;
            pendingName = null;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    /** Methods callable from the page as window.AndroidBridge.* */
    private class Bridge {

        @JavascriptInterface
        public void saveFile(final String name, final String mime, final String base64) {
            final byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            runOnUiThread(() -> {
                pendingBytes = bytes;
                pendingName = name;
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(mime == null || mime.isEmpty() ? "application/octet-stream" : mime);
                intent.putExtra(Intent.EXTRA_TITLE, name);
                try {
                    startActivityForResult(intent, REQ_SAVE_FILE);
                } catch (Exception e) {
                    pendingBytes = null;
                    toast("সংরক্ষণের জায়গা খোলা যায়নি");
                }
            });
        }

        @JavascriptInterface
        public void printReport(final String title) {
            runOnUiThread(() -> {
                PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
                PrintDocumentAdapter adapter = web.createPrintDocumentAdapter(title);
                PrintAttributes attrs = new PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .build();
                pm.print(title, adapter, attrs);
            });
        }
    }
}
