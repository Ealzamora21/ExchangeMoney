package emilio.alzamora.exchangemoney.scene.history.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HistoryViewModel : ViewModel() {

    // 1. Variable privada que almacenará
    // las conversiones guardadas.
    private val _history =
        MutableLiveData<List<String>>()

    // 2. La View puede observar el historial,
    // pero no modificarlo directamente.
    val history: LiveData<List<String>> =
        _history

    // 3. Recibimos las conversiones recuperadas
    // desde SharedPreferences.
    fun loadHistory(
        savedHistory: List<String>
    ) {

        // 4. Enviamos la lista a la View.
        _history.value = savedHistory
    }

    // 5. Limpiamos visualmente el historial.
    fun clearHistory() {

        _history.value = emptyList()
    }
}