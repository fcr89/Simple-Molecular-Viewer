package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.ProteinStructure
import com.example.model.RenderStyle
import com.example.model.MolecularColorScheme
import com.example.model.StructureSource

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MolstarWebViewer(
    structure: ProteinStructure,
    renderStyle: RenderStyle,
    colorScheme: MolecularColorScheme,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }

    val styleScript = when (renderStyle) {
        RenderStyle.CARTOON -> "{cartoon: {color: 'spectrum'}}"
        RenderStyle.BACKBONE -> "{line: {color: 'spectrum'}}"
        RenderStyle.BALL_AND_STICK -> "{stick: {}, sphere: {radius: 0.4}}"
        RenderStyle.SPACEFILL -> "{sphere: {}}"
    }

    val sanitizedPdb = structure.rawPdbContent
        .replace("\\", "\\\\")
        .replace("`", "\\`")
        .replace("\$", "\\$")
        .replace("\r", "")

    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <script src="https://cdnjs.cloudflare.com/ajax/libs/3Dmol/2.4.2/3Dmol-min.js"></script>
            <style>
                body, html { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; background-color: #090D16; }
                #viewport { width: 100%; height: 100%; position: absolute; }
                #info-tag { position: absolute; top: 12px; left: 12px; color: #00E5FF; font-family: monospace; font-size: 11px; z-index: 10; background: rgba(9, 13, 22, 0.7); padding: 4px 8px; border-radius: 4px; border: 1px solid rgba(0, 229, 255, 0.3); }
            </style>
        </head>
        <body>
            <div id="info-tag">WebGL 3Dmol Renderer: ${structure.id}</div>
            <div id="viewport"></div>
            <script>
                try {
                    let viewer = ${'$'}3Dmol.createViewer("viewport", {
                        defaultcolors: ${'$'}3Dmol.rasmolElementColors,
                        backgroundColor: '#090D16'
                    });
                    
                    const pdbData = `$sanitizedPdb`;
                    viewer.addModel(pdbData, "pdb");
                    viewer.setStyle({}, $styleScript);
                    viewer.zoomTo();
                    viewer.render();
                    viewer.spin("y", 0.8);
                } catch(e) {
                    console.error("Mol* 3Dmol Error: " + e);
                }
            </script>
        </body>
        </html>
    """.trimIndent()

    Box(modifier = modifier.testTag("molstar_webgl_viewport"), contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        allowFileAccess = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                    }
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }
                    }
                    loadDataWithBaseURL("https://cdnjs.cloudflare.com", htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL("https://cdnjs.cloudflare.com", htmlContent, "text/html", "UTF-8", null)
            }
        )

        if (isLoading) {
            CircularProgressIndicator(color = com.example.ui.theme.CyanPrimary)
        }
    }
}
