package com.hsoft.androidwebbrowser;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

	private WebView webView;
	private EditText textUrl;
	private String homeUrl;

	private SwipeRefreshLayout swipeRefreshLayout;
	private Boolean pullRefreshSet;

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

		homeUrl = "https://duckduckgo.com";
		textUrl = findViewById(R.id.text_url);

		pullRefreshSet = false;

		swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
		swipeRefreshLayout.setEnabled(false);

		ImageButton btnGo = findViewById(R.id.btn_go);
		ImageButton btnBack = findViewById(R.id.btn_back);
		ImageButton btnForward = findViewById(R.id.btn_forward);
		ImageButton btnHome = findViewById(R.id.btn_home);
		ImageButton btnRefresh = findViewById(R.id.btn_refresh);
		ImageButton btnStop= findViewById(R.id.btn_stop);

		final TextView labelOverlay = findViewById(R.id.label_overlay);
		final ProgressBar progressBar = findViewById(R.id.progress_bar);
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

		labelOverlay.setVisibility(View.INVISIBLE);

		webView = findViewById(R.id.web_view);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				webView.requestFocus();
				textUrl.setText(url);
				labelOverlay.setVisibility(View.VISIBLE);
				progressBar.setProgress(0);
				progressBar.setVisibility(View.VISIBLE);

				if (pullRefreshSet && !swipeRefreshLayout.isRefreshing()) {
					swipeRefreshLayout.setRefreshing(true);
				}
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				progressBar.setVisibility(View.INVISIBLE);
				labelOverlay.setVisibility(View.INVISIBLE);

				if (!pullRefreshSet) {
					// configure swipe refresh
					swipeRefreshLayout.setEnabled(true);
					swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
						@Override
						public void onRefresh() {
							webView.reload();
						}
					});
					pullRefreshSet = true;
				} else {
					swipeRefreshLayout.setRefreshing(false);
				}
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				if (Build.VERSION.SDK_INT >= 21) {
					String url = request.getUrl().toString();

					if (overrideUrlLoading(url)) {
						return true;
					}
				}
				return super.shouldOverrideUrlLoading(view, request);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (overrideUrlLoading(url)) {
					return true;
				} else {
					return super.shouldOverrideUrlLoading(view, url);
				}
			}

			private boolean overrideUrlLoading(String url) {
				// check for email url
				if (MailTo.isMailTo(url)) {
					MailTo mailTo = MailTo.parse(url);
					String emailTo = mailTo.getTo() != null ? mailTo.getTo() : "";
					String emailSubject = mailTo.getSubject() != null ? mailTo.getSubject() : "";
					String emailBody = mailTo.getBody() != null ? mailTo.getBody() : "";
					//https://developer.android.com/reference/android/content/Intent.html#ACTION_SEND
					// https://developer.android.com/guide/components/intents-common.html#Email
					Intent email = new Intent(Intent.ACTION_SENDTO);
					email.setData(Uri.parse(url)); // show email clients only
					email.putExtra(Intent.EXTRA_EMAIL, new String[]{emailTo});
					email.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
					email.putExtra(Intent.EXTRA_TEXT, emailBody);
					if (email.resolveActivity(getPackageManager()) != null) {
						startActivity(email);
					} else {
						showToast("Could not resolve activity for email.");
					}
					return true;
				}

				// check for telephone
				if (url.startsWith("tel:")) {
					// https://developer.android.com/reference/android/content/Intent.html#ACTION_DIAL
					Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
					startActivity(tel);
					return true;
				}

				// check for SMS
				if (url.startsWith("sms:") || url.startsWith("smsto:")) {
					// https://developer.android.com/reference/android/content/Intent.html#ACTION_VIEW
					// https://developer.android.com/reference/android/content/Intent.html#ACTION_SENDTO
					Intent sms = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(sms);
					return true;
				}

				// if URL is not web
				if (!url.startsWith("http:") && !url.startsWith("https:")) {
					showToast("Unhandled URL scheme: " + url);
					return true;
				}

				return false;
			}
		});

		webView.setWebChromeClient(new WebChromeClient(){
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				progressBar.setProgress(newProgress);
			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
				AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
				builder.setMessage(message).setCancelable(false);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						result.confirm();
					}
				});
				builder.create().show();
				return true;
			}

			@Override
			public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
				AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
				builder.setMessage(message).setCancelable(false);
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						result.confirm();
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						result.cancel();
					}
				});
				builder.create().show();
				return true;
			}

			@Override
			public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
				return super.onJsPrompt(view, url, message, defaultValue, result);
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

	private void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	private void clickBtnGo() {
		String urlString = textUrl.getText().toString();
		if (!urlString.matches("^https?://.+")) {
			showToast("Please enter a valid URL");
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
