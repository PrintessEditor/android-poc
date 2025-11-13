package com.example.printessandroid

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts


class MainActivity : ComponentActivity() {

    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    private lateinit var currentPhotoUri: Uri

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Example endpoint for this app
        val mUrl = "https://account.printess.com/buyer-test-android.html"

        val printessView = WebView(this.baseContext)
        // Clearing Cache might be required if you update your endpoint
        printessView.clearCache(true)
        setContentView(printessView)
        printessView.loadUrl(mUrl)
//        This will register the interface in your web endpoints' context, you need to call your function from this
        printessView.addJavascriptInterface(WebAppInterface(this.baseContext), "printess_android")

        printessView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                Log.d("File Chooser", fileUploadCallback.toString())
                fileUploadCallback?.onReceiveValue(null)
                fileUploadCallback = filePathCallback

//                Choose, which app to pick image from
//                val pickIntent = Intent(Intent.ACTION_PICK)
//                pickIntent.setDataAndType(
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    "image/*"
//                )
//                pickImageFromGalleryForResult.launch(pickIntent)

//                Goes directly to camera roll
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                val chooserIntent = Intent.createChooser(intent, "Choose File")
                pickImageFromGalleryForResult.launch(chooserIntent)

                return true
            }
        }

        val webSettings: WebSettings = printessView.settings
        with(webSettings) {
            javaScriptEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            javaScriptCanOpenWindowsAutomatically = true
            mediaPlaybackRequiresUserGesture = false
            domStorageEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
        }
    }

    val pickImageFromGalleryForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val results: Array<Uri>? = when {
                intent?.data != null -> arrayOf(intent.data!!)
                result.resultCode == RESULT_OK -> arrayOf(currentPhotoUri)
                else -> null
            }
            fileUploadCallback?.onReceiveValue(results)
            fileUploadCallback = null
        }
    }

    private fun pickImageFromGallery() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.setDataAndType(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "image/*"
        )
        pickImageFromGalleryForResult.launch(pickIntent)
    }

//    Deprecated way of receiving uploads
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//
//        Log.d("File Chooser Received", requestCode.toString())
//        Log.d("File Chooser Received", fileUploadCallback.toString())
//        if (requestCode == 1337) {
//            if (fileUploadCallback == null) {
//                super.onActivityResult(requestCode, resultCode, data)
//                return
//            }
//
//            val results: Array<Uri>? = when {
//                resultCode == RESULT_OK && data?.data != null -> arrayOf(data.data!!)
//                resultCode == RESULT_OK -> arrayOf(currentPhotoUri)
//                else -> null
//            }
//            Log.d("File Chooser Received", results.toString())
//
//            fileUploadCallback?.onReceiveValue(results)
//            fileUploadCallback = null
//
//            Log.d("File Chooser Received", "Last call in method")
//        } else {
//            super.onActivityResult(requestCode, resultCode, data)
//        }
//    }
}

class WebAppInterface(private val mContext: Context) {

    /** Show a toast from the web page  */
    @JavascriptInterface
    fun backButtonCallback(token: String) {
        Toast.makeText(mContext, "Save Token: $token)", Toast.LENGTH_SHORT).show()
    }

//    Important: JavascriptInterface can only handle primitive types
    @JavascriptInterface
    fun addToBasketCallback(map: String?) {
        val values = map?.split(",")
        Toast.makeText(
            mContext,
            "Save Token: (${values?.get(0)})), , thumbnail url: (${values?.get(1)}))",
            Toast.LENGTH_LONG
        ).show()
    }
}

//Webviews don't work in the preview of Android Studio, only on the (emulated or real) device
//@SuppressLint("SetJavaScriptEnabled")
//@Preview(showBackground = true)
//@Composable
//fun PrintessPreview() {
//
//    PrintessAndroidTheme {
//        Surface(
//            modifier = Modifier.fillMaxSize(),
//            color = Color.Magenta
//        ) {
//            Column(modifier = Modifier.fillMaxWidth()) {
//                TextView(
//                    text = "Nothing to see here..."
//                )
//            }
//
//        }
//    }
//}
