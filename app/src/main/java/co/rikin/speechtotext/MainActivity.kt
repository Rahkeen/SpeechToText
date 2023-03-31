package co.rikin.speechtotext

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import co.rikin.speechtotext.ui.theme.MaterialRed
import co.rikin.speechtotext.ui.theme.SpeechToTextTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      App()
    }
  }
}

@Composable
fun App() {
  val context = LocalContext.current
  var permission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
      ) == PackageManager.PERMISSION_GRANTED
    )
  }
  val launcher =
    rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { granted ->
      permission = granted
    }

  val viewModel = viewModel<AppViewModel>()
  val speechRecognizer = remember {
    SpeechRecognizer.createSpeechRecognizer(context).apply {
      setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(p0: Bundle?) {
          Log.d("Speech Recognizer", "onReadyForSpeech")
        }

        override fun onBeginningOfSpeech() {
          Log.d("Speech Recognizer", "onBeginningOfSpeech")
        }

        override fun onRmsChanged(p0: Float) {
        }

        override fun onBufferReceived(p0: ByteArray?) {
        }

        override fun onEndOfSpeech() {
          Log.d("Speech Recognizer", "onEndOfSpeech")
        }

        override fun onError(error: Int) {
          val message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> {
              "Audio"
            }

            SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT -> {
              "Cannot Check Support"
            }

            SpeechRecognizer.ERROR_CLIENT -> {
              "Client"
            }

            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
              "Insufficient Permissions"
            }

            SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED -> {
              "Language Not Supported"
            }

            SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> {
              "Language Unavailable"
            }

            SpeechRecognizer.ERROR_NETWORK -> {
              "Network"
            }

            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
              "Network Timeout"
            }

            SpeechRecognizer.ERROR_NO_MATCH -> {
              "No Match"
            }

            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
              "Busy"
            }

            SpeechRecognizer.ERROR_SERVER -> {
              "Server Error"
            }

            SpeechRecognizer.ERROR_SERVER_DISCONNECTED -> {
              "Server Disconnected"
            }

            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
              "Speech Timout"
            }

            SpeechRecognizer.ERROR_TOO_MANY_REQUESTS -> {
              "Too Many Requests"
            }

            else -> {
              "Unknown"
            }
          }
          Log.d("Speech Recognizer", "Error: $message")
        }

        override fun onResults(results: Bundle?) {}

        override fun onPartialResults(results: Bundle?) {
          val partial =
            results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.getOrNull(0) ?: ""
          Log.d("Speech Recognizer", "onPartialResult: $partial")

          viewModel.send(AppAction.Recording(partial))
        }

        override fun onEvent(p0: Int, p1: Bundle?) {
          Log.d("Speech Recognizer", "onEvent")
        }
      })
    }
  }
  val speechRecognizerIntent = remember {
    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(
        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
      )
      putExtra(
        RecognizerIntent.EXTRA_LANGUAGE,
        Locale.getDefault()
      )
      putExtra(
        RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
        5000
      )
    }
  }

  var pressed by remember { mutableStateOf(false) }
  val buttonScale by animateFloatAsState(
    targetValue = if (pressed) 0.8f else 1f,
    animationSpec = spring(
      stiffness = Spring.StiffnessLow,
      dampingRatio = Spring.DampingRatioMediumBouncy,
      visibilityThreshold = 0.001f
    ),
    label = "Button Scale"
  )

  SpeechToTextTheme {
    if (permission) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .background(color = MaterialTheme.colorScheme.background)
          .windowInsetsPadding(insets = WindowInsets.navigationBars)
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f), contentAlignment = Alignment.Center
        ) {
          Text(
            text = viewModel.state.display,
            modifier = Modifier
              .fillMaxWidth()
              .wrapContentHeight(),
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .scale(buttonScale)
              .size(80.dp)
              .background(
                color = MaterialRed,
                shape = CircleShape
              )
              .clip(CircleShape)
              .pointerInput(Unit) {
                detectTapGestures(
                  onPress = {
                    pressed = true
                    speechRecognizer.startListening(speechRecognizerIntent)
                    viewModel.send(AppAction.StartRecord)
                    awaitRelease()
                    pressed = false
                    speechRecognizer.stopListening()
                    viewModel.send(AppAction.EndRecord)
                  }
                )
              },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              modifier = Modifier.size(30.dp),
              painter = painterResource(id = R.drawable.ic_microphone),
              tint = MaterialTheme.colorScheme.onBackground,
              contentDescription = "Record"
            )
          }
        }
      }
    } else {
      launcher.launch(Manifest.permission.RECORD_AUDIO)
    }
  }
}

@Preview
@Composable
fun AppPreview() {
  App()
}


class AppViewModel : ViewModel() {
  var state by mutableStateOf(AppState())
    private set

  private var recordJob: Job? = null

  fun send(action: AppAction) {
    when (action) {
      AppAction.StartRecord -> {
//        recordJob = viewModelScope.launch {
//          var index = 0
//          while (isActive) {
//            val randomDelay = (100L..500L).random()
//            if (index in TEST_SPEECH.indices) {
//              delay(randomDelay)
//              state = state.copy(display = state.display + " " + TEST_SPEECH[index])
//              index++
//            } else {
//              delay(randomDelay)
//              index = 0
//              state = state.copy(display = "")
//            }
//          }
//        }
      }

      AppAction.EndRecord -> {
//        recordJob?.cancel()
        viewModelScope.launch {
          delay(3000)
          state = state.copy(
            display = ""
          )
        }
      }

      is AppAction.Recording -> {
        state = state.copy(
          display = state.display + action.text
        )
      }
    }
  }
}

data class AppState(
  val display: String = ""
)

sealed class AppAction {
  object StartRecord : AppAction()
  object EndRecord : AppAction()
  data class Recording(val text: String) : AppAction()
}

val TEST_SPEECH = listOf(
  "This",
  "is",
  "a",
  "test",
  "of",
  "the",
  "recording",
  "system",
  "This",
  "is",
  "a",
  "test",
  "of",
  "the",
  "recording",
  "system",
  "This",
  "is",
  "a",
  "test",
  "of",
  "the",
  "recording",
  "system",
  "This",
  "is",
  "a",
  "test",
  "of",
  "the",
  "recording",
  "system",
)

