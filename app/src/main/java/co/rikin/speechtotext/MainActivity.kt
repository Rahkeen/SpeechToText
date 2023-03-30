package co.rikin.speechtotext

import android.os.Bundle
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import co.rikin.speechtotext.ui.theme.MaterialRed
import co.rikin.speechtotext.ui.theme.SpeechToTextTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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
  val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
  val viewModel = viewModel<AppViewModel>()
  val state = viewModel.state

  SpeechToTextTheme {
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
          text = state.display,
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
            .size(80.dp)
            .background(
              color = MaterialRed,
              shape = CircleShape
            )
            .clip(CircleShape)
            .pointerInput(Unit) {
              detectTapGestures(
                onPress = {
                  viewModel.send(AppAction.StartRecord)
                  awaitRelease()
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
        recordJob = viewModelScope.launch {
          var index = 0
          while (isActive) {
            val randomDelay = (100L..500L).random()
            if (index in TEST_SPEECH.indices) {
              delay(randomDelay)
              state = state.copy(display = state.display + " " + TEST_SPEECH[index])
              index++
            } else {
              delay(randomDelay)
              index = 0
              state = state.copy(display = "")
            }            }
          }
      }

      AppAction.EndRecord -> {
        recordJob?.cancel()
        state = state.copy(
          display = ""
        )
      }
    }
  }
}

data class AppState(
  val display: String = ""
)

enum class AppAction {
  StartRecord,
  EndRecord
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

