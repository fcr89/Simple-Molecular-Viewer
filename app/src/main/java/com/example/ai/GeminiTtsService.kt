package com.example.ai

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class TtsProvider {
    GEMINI_3_8_FLASH_TTS,
    ANDROID_TTS_FALLBACK
}

data class TtsPlaybackState(
    val isGenerating: Boolean = false,
    val isPlaying: Boolean = false,
    val currentProvider: TtsProvider? = null,
    val voiceName: String = "Kore",
    val statusMessage: String = "",
    val error: String? = null
)

class GeminiTtsService(private val context: Context) {

    private val _playbackState = MutableStateFlow(TtsPlaybackState())
    val playbackState: StateFlow<TtsPlaybackState> = _playbackState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var androidTts: TextToSpeech? = null
    private var isAndroidTtsReady = false

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    init {
        initAndroidTts()
    }

    private fun initAndroidTts() {
        androidTts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                androidTts?.language = Locale.US
                androidTts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _playbackState.value = _playbackState.value.copy(isPlaying = true)
                    }

                    override fun onDone(utteranceId: String?) {
                        _playbackState.value = _playbackState.value.copy(isPlaying = false, statusMessage = "Narration finished")
                    }

                    override fun onError(utteranceId: String?) {
                        _playbackState.value = _playbackState.value.copy(isPlaying = false, error = "Android TTS playback error")
                    }
                })
                isAndroidTtsReady = true
            }
        }
    }

    fun speak(text: String, voiceName: String = "Kore", scope: CoroutineScope) {
        stop()

        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasGeminiKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

        if (hasGeminiKey) {
            scope.launch {
                speakWithGeminiTts(text, voiceName, apiKey)
            }
        } else {
            // Inform user and run Android TTS fallback immediately
            speakWithAndroidTts(text, note = "Using Android TTS (add GEMINI_API_KEY to AI Studio Secrets for Gemini-3.8-Flash-TTS)")
        }
    }

    private suspend fun speakWithGeminiTts(text: String, voiceName: String, apiKey: String) = withContext(Dispatchers.IO) {
        _playbackState.value = TtsPlaybackState(
            isGenerating = true,
            isPlaying = false,
            voiceName = voiceName,
            statusMessage = "Synthesizing with gemini-3.8-flash-tts..."
        )

        try {
            // Model: gemini-3.8-flash-tts as specified by user mandate
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash-tts:generateContent?key=$apiKey"

            // Construct payload with AUDIO response modality and voice config
            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Read this scientific protein summary in a clear, engaging, professional voice: $text")
                            })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)

                val generationConfig = JSONObject().apply {
                    val modalities = JSONArray().apply {
                        put("AUDIO")
                    }
                    put("responseModalities", modalities)

                    val speechConfig = JSONObject().apply {
                        val voiceConfig = JSONObject().apply {
                            val prebuiltVoiceConfig = JSONObject().apply {
                                put("voiceName", voiceName)
                            }
                            put("prebuiltVoiceConfig", prebuiltVoiceConfig)
                        }
                        put("voiceConfig", voiceConfig)
                    }
                    put("speechConfig", speechConfig)
                }
                put("generationConfig", generationConfig)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                Log.w("GeminiTtsService", "Gemini TTS returned error HTTP ${response.code}: $errBody")
                // Fallback to Android TTS
                withContext(Dispatchers.Main) {
                    speakWithAndroidTts(text, note = "Gemini TTS unavailable (HTTP ${response.code}). Fallen back to Android TTS.")
                }
                return@withContext
            }

            val responseStr = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseStr)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            var audioBase64: String? = null
            var mimeType = "audio/wav"

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    val inlineData = part.optJSONObject("inlineData")
                    if (inlineData != null) {
                        audioBase64 = inlineData.optString("data")
                        mimeType = inlineData.optString("mimeType", "audio/wav")
                        break
                    }
                }
            }

            if (audioBase64.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    speakWithAndroidTts(text, note = "No audio payload in Gemini response. Fallen back to Android TTS.")
                }
                return@withContext
            }

            val audioBytes = Base64.decode(audioBase64, Base64.DEFAULT)
            val finalBytes = if (mimeType.contains("pcm", ignoreCase = true) || !isWavOrMp3(audioBytes)) {
                pcmToWav(audioBytes, sampleRate = 24000, channels = 1, bitsPerSample = 16)
            } else {
                audioBytes
            }

            val audioFile = File(context.cacheDir, "gemini_3_8_tts_narration.wav")
            FileOutputStream(audioFile).use { it.write(finalBytes) }

            withContext(Dispatchers.Main) {
                playAudioFile(audioFile, voiceName)
            }
        } catch (e: Exception) {
            Log.e("GeminiTtsService", "Error during Gemini TTS request", e)
            withContext(Dispatchers.Main) {
                speakWithAndroidTts(text, note = "Gemini network error: ${e.message}. Using Android TTS.")
            }
        }
    }

    private fun playAudioFile(file: File, voiceName: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    _playbackState.value = _playbackState.value.copy(
                        isPlaying = false,
                        statusMessage = "Narration finished"
                    )
                }
                start()
            }
            _playbackState.value = TtsPlaybackState(
                isGenerating = false,
                isPlaying = true,
                currentProvider = TtsProvider.GEMINI_3_8_FLASH_TTS,
                voiceName = voiceName,
                statusMessage = "Playing Gemini-3.8-Flash-TTS audio ($voiceName)"
            )
        } catch (e: Exception) {
            _playbackState.value = _playbackState.value.copy(
                isGenerating = false,
                isPlaying = false,
                error = "Audio player error: ${e.message}"
            )
        }
    }

    private fun speakWithAndroidTts(text: String, note: String = "") {
        _playbackState.value = TtsPlaybackState(
            isGenerating = false,
            isPlaying = true,
            currentProvider = TtsProvider.ANDROID_TTS_FALLBACK,
            statusMessage = note.ifEmpty { "Playing via local Android TTS" }
        )

        if (isAndroidTtsReady && androidTts != null) {
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "fold3d_narration")
            }
            androidTts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "fold3d_narration")
        } else {
            _playbackState.value = _playbackState.value.copy(
                isPlaying = false,
                error = "Android Text-to-Speech is initializing"
            )
        }
    }

    fun stop() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {}

        try {
            if (androidTts?.isSpeaking == true) {
                androidTts?.stop()
            }
        } catch (_: Exception) {}

        _playbackState.value = _playbackState.value.copy(
            isGenerating = false,
            isPlaying = false,
            statusMessage = "Stopped"
        )
    }

    fun release() {
        stop()
        androidTts?.shutdown()
        androidTts = null
    }

    private fun isWavOrMp3(bytes: ByteArray): Boolean {
        if (bytes.size < 4) return false
        val riff = bytes[0] == 'R'.code.toByte() && bytes[1] == 'I'.code.toByte() &&
                bytes[2] == 'F'.code.toByte() && bytes[3] == 'F'.code.toByte()
        val id3 = bytes[0] == 'I'.code.toByte() && bytes[1] == 'D'.code.toByte() && bytes[2] == '3'.code.toByte()
        return riff || id3
    }

    private fun pcmToWav(pcmData: ByteArray, sampleRate: Int, channels: Int, bitsPerSample: Int): ByteArray {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        val dataLen = pcmData.size
        val totalLen = dataLen + 36
        val header = ByteArray(44)

        // RIFF chunk
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalLen and 0xff).toByte()
        header[5] = ((totalLen shr 8) and 0xff).toByte()
        header[6] = ((totalLen shr 16) and 0xff).toByte()
        header[7] = ((totalLen shr 24) and 0xff).toByte()

        // WAVE
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // fmt subchunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0 // Subchunk1Size (16 for PCM)
        header[20] = 1
        header[21] = 0 // AudioFormat 1 = PCM
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = blockAlign.toByte()
        header[33] = 0
        header[34] = bitsPerSample.toByte()
        header[35] = 0

        // data subchunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (dataLen and 0xff).toByte()
        header[41] = ((dataLen shr 8) and 0xff).toByte()
        header[42] = ((dataLen shr 16) and 0xff).toByte()
        header[43] = ((dataLen shr 24) and 0xff).toByte()

        return header + pcmData
    }
}
