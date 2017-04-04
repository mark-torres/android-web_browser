package com.hsoft.androidwebbrowser;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

	private WebView webView;
	private EditText textUrl;
	private String homeUrl;

	// CLICK LISTENERS

	private View.OnClickListener clickGo = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			clickBtnGo();
		}
	};

	private View.OnClickListener clickBack = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			clickBtnBack();
		}
	};

	private View.OnClickListener clickForward = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			clickBtnForward();
		}
	};

	private View.OnClickListener clickHome = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			clickBtnHome();
		}
	};

	private View.OnClickListener clickRefresh = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			clickBtnRefresh();
		}
	};

	private View.OnClickListener clickStop = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			clickBtnStop();
		}
	};

	// ACTIVITY - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		homeUrl = "http://urdevel.mtorres.urstaging.com/dump.php";

		textUrl = (EditText) findViewById(R.id.text_url);
		ImageButton btnGo = (ImageButton) findViewById(R.id.btn_go);
		ImageButton btnBack = (ImageButton) findViewById(R.id.btn_back);
		ImageButton btnForward = (ImageButton) findViewById(R.id.btn_forward);
		ImageButton btnHome = (ImageButton) findViewById(R.id.btn_home);
		ImageButton btnRefresh = (ImageButton) findViewById(R.id.btn_refresh);
		ImageButton btnStop= (ImageButton) findViewById(R.id.btn_stop);
		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
		progressBar.setVisibility(View.INVISIBLE);
		progressBar.setProgress(0);

		btnGo.setOnClickListener(clickGo);
		btnBack.setOnClickListener(clickBack);
		btnForward.setOnClickListener(clickForward);
		btnHome.setOnClickListener(clickHome);
		btnRefresh.setOnClickListener(clickRefresh);
		btnStop.setOnClickListener(clickStop);

		textUrl.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
					clickBtnGo();
					return true;
				}
				return false;
			}
		});

		webView = (WebView) findViewById(R.id.web_view);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				textUrl.setText(url);
				progressBar.setProgress(0);
				progressBar.setVisibility(View.VISIBLE);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				progressBar.setVisibility(View.INVISIBLE);
			}
		});

		webView.setWebChromeClient(new WebChromeClient(){
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				progressBar.setProgress(newProgress);
			}
		});


		// configure web view
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	// UTILS - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private void clickBtnGo() {
		webView.requestFocus();
		String urlString = textUrl.getText().toString();
		if (!urlString.matches("^https?://.+")) {
			Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show();
			return;
		}
		webView.loadUrl(urlString);
	}

	private void clickBtnBack() {
		if (webView.canGoBack()) {
			webView.goBack();
		}
	}

	private void clickBtnForward() {
		if (webView.canGoForward()) {
			webView.goForward();
		}
	}

	private void clickBtnHome() {
		webView.loadUrl(homeUrl);
	}

	private void clickBtnRefresh() {
		webView.reload();
	}

	private void clickBtnStop() {
		webView.stopLoading();
	}
}
