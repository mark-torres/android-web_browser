package com.hsoft.androidwebbrowser;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

	private WebView webView;
	private EditText textUrl;
	private Button btnGo;

	// CLICK LISTENERS

	private View.OnClickListener clickGo = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			clickBtnGo();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		textUrl = (EditText) findViewById(R.id.text_url);
		btnGo = (Button) findViewById(R.id.btn_go);

		btnGo.setOnClickListener(clickGo);

		webView = (WebView) findViewById(R.id.web_view);
		webView.setWebViewClient(new WebViewClient());

		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	// UTILS - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

	private void clickBtnGo() {
		String urlString = textUrl.getText().toString();
		if (!urlString.matches("^https?://.+")) {
			Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show();
			return;
		}
		webView.loadUrl(urlString);
	}
}
