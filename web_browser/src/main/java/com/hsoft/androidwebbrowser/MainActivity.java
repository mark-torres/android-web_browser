package com.hsoft.androidwebbrowser;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
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

	private final String TAG = "ANDROID_WEB_BROWSER";

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

	private MainActivity thisActivity;

	// ACTIVITY - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		thisActivity = this;

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
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				super.onReceivedSslError(view, handler, error);
				Log.e(TAG, "onReceivedSslError: " + error.toString());
			}

			@Override
			public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
				super.onReceivedHttpError(view, request, errorResponse);
				Log.e(TAG, "onReceivedHttpError: " + errorResponse.toString());
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				//super.onReceivedError(view, errorCode, description, failingUrl);
				if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
					Log.e(TAG, "onReceivedError:" + description);
				}
			}

			@Override
			public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
				//super.onReceivedError(view, request, error);
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
					String errorDescr = error.getDescription().toString();
					Log.e(TAG, "onReceivedError:" + errorDescr);
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

			@Override
			public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
				// https://blog.maurizionapoleoni.com/opening-a-window-open-in-android-without-killing-the-content-of-the-main-webview/

				// create new web view
				WebView popup = new WebView(thisActivity);
				popup.setWebViewClient(new WebViewClient() {
					@Override
					public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
						if (Build.VERSION.SDK_INT >= 21) {
							String popUrl = request.getUrl().toString();
							Log.d(TAG, "shouldOverrideUrlLoading: " + popUrl);
							processPopupRequest(popUrl);
						}
						return true;
					}

					@Override
					public boolean shouldOverrideUrlLoading(WebView view, String url) {
						Log.d(TAG, "shouldOverrideUrlLoading: " + url);
						processPopupRequest(url);
						return true;
					}

					private void processPopupRequest(String popupUrl) {
						Intent shareIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(popupUrl));
						if (shareIntent.resolveActivity(thisActivity.getPackageManager()) != null) {
							startActivity(shareIntent);
						} else {
							showToast("Could not resolve activity for popup URL");
						}
					}
				});

				WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
				transport.setWebView(popup);
				resultMsg.sendToTarget();

				return true;
			}
		});


		// configure web view
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setSupportMultipleWindows(true);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	// UTILS - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private void showAlert(@Nullable String title, @NonNull String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		if (title != null && !title.isEmpty()) {
			builder.setTitle(title);
		}
		builder.setPositiveButton(android.R.string.ok, null);
		builder.setCancelable(false);
		builder.create().show();
	}

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
