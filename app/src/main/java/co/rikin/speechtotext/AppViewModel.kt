package co.rikin.speechtotext

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AppViewModel(private val stt: SpeechToText) : ViewModel() {
  var state by mutableStateOf(AppState())
    private set

  init {
    viewModelScope.launch {
      with(stt) {
        text.collect { result ->
          state = state.copy(
            display = state.display + result
          )
        }
      }
    }
  }


  fun send(action: AppAction) {
    when (action) {
      AppAction.StartRecord -> {
        stt.start()
      }

      AppAction.EndRecord -> {
        stt.stop()
        viewModelScope.launch {
          delay(3000)
          state = state.copy(
            display = ""
          )
        }
      }
    }
  }
}

@Suppress("UNCHECKED_CAST")
class AppViewModelFactory(private val stt: SpeechToText) : ViewModelProvider.NewInstanceFactory() {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    return AppViewModel(stt) as T
  }
}

data class AppState(
  val display: String = ""
)

sealed class AppAction {
  object StartRecord : AppAction()
  object EndRecord : AppAction()
}
