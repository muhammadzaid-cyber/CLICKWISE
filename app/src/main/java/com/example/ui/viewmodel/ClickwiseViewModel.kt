package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.EducationalContentRepository
import com.example.data.PreferencesRepository
import com.example.data.UpdatePreferenceSnapshot
import com.example.domain.analyzer.AiRepository
import com.example.domain.analyzer.AnalysisStage
import com.example.domain.analyzer.FirebaseAiRepository
import com.example.domain.analyzer.UrlInspectorEngine
import com.example.domain.model.AppLanguage
import com.example.domain.model.MessageAnalysisRequest
import com.example.domain.model.MessageAnalysisResult
import com.example.domain.model.QuizQuestion
import com.example.domain.model.SampleScenario
import com.example.domain.model.SessionScanRecord
import com.example.domain.model.UrlInspectionResult
import com.example.domain.update.AndroidApkIntegrityVerifier
import com.example.domain.update.AndroidApkInstallerLauncher
import com.example.domain.update.AndroidNetworkConnectivityChecker
import com.example.domain.update.GitHubReleaseClient
import com.example.domain.update.UpdateManager
import com.example.domain.update.UpdateUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val MAX_MESSAGE_LENGTH = 4000
const val MAX_URL_LENGTH = 1000

sealed interface AnalysisUiState {
    data object Idle : AnalysisUiState
    data class Scanning(val stage: AnalysisStage, val progress: Float) : AnalysisUiState
    data class Success(val result: MessageAnalysisResult) : AnalysisUiState
    data class Error(val message: String) : AnalysisUiState
}

data class ScannerScreenState(
    val messageInput: String = "",
    val optionalUrlInput: String = "",
    val validationError: String? = null,
    val selectedSampleId: String? = null,
    val analysisState: AnalysisUiState = AnalysisUiState.Idle
)

data class UrlInspectorScreenState(
    val urlInput: String = "",
    val validationError: String? = null,
    val result: UrlInspectionResult? = null
)

data class QuizScreenState(
    val questions: List<QuizQuestion> = EducationalContentRepository.quizQuestions.shuffled(),
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val score: Int = 0,
    val isCompleted: Boolean = false
)

class ClickwiseViewModel @JvmOverloads constructor(
    application: Application,
    private val aiRepository: AiRepository = FirebaseAiRepository(),
    customUpdateManagerFactory: ((Application, PreferencesRepository, kotlinx.coroutines.CoroutineScope) -> UpdateManager)? = null
) : AndroidViewModel(application) {

    private val preferencesRepository = PreferencesRepository(application.applicationContext)

    private val updateManager: UpdateManager = customUpdateManagerFactory?.invoke(
        application,
        preferencesRepository,
        viewModelScope
    ) ?: UpdateManager(
        scope = viewModelScope,
        gateway = GitHubReleaseClient(application.applicationContext),
        connectivityChecker = AndroidNetworkConnectivityChecker(application.applicationContext),
        integrityVerifier = AndroidApkIntegrityVerifier(application.applicationContext),
        installerLauncher = AndroidApkInstallerLauncher(application.applicationContext),
        preferencesStore = preferencesRepository
    )

    val installedVersionName: String = BuildConfig.VERSION_NAME
    val installedVersionCode: Int = BuildConfig.VERSION_CODE

    val updateUiState: StateFlow<UpdateUiState> = updateManager.uiState
    val showUpdateDialog: StateFlow<Boolean> = updateManager.showUpdateDialog
    val updatePreferences: StateFlow<UpdatePreferenceSnapshot> = updateManager.prefsSnapshot

    private val _language = MutableStateFlow(AppLanguage.EN)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _reduceMotion = MutableStateFlow(false)
    val reduceMotion: StateFlow<Boolean> = _reduceMotion.asStateFlow()

    private var userModifiedLanguage = false
    private var userModifiedReduceMotion = false

    private val _showEntranceSplash = MutableStateFlow(true)
    val showEntranceSplash: StateFlow<Boolean> = _showEntranceSplash.asStateFlow()

    private val _scannerState = MutableStateFlow(ScannerScreenState())
    val scannerState: StateFlow<ScannerScreenState> = _scannerState.asStateFlow()

    private val _urlInspectorState = MutableStateFlow(UrlInspectorScreenState())
    val urlInspectorState: StateFlow<UrlInspectorScreenState> = _urlInspectorState.asStateFlow()

    // Session-only scan history — strictly in memory, never persisted to disk
    private val _sessionHistory = MutableStateFlow<List<SessionScanRecord>>(emptyList())
    val sessionHistory: StateFlow<List<SessionScanRecord>> = _sessionHistory.asStateFlow()

    // Session-only completed lesson IDs
    private val _completedLessonIds = MutableStateFlow<Set<String>>(emptySet())
    val completedLessonIds: StateFlow<Set<String>> = _completedLessonIds.asStateFlow()

    private val _quizState = MutableStateFlow(QuizScreenState())
    val quizState: StateFlow<QuizScreenState> = _quizState.asStateFlow()

    init {
        viewModelScope.launch {
            val savedLang = preferencesRepository.languageFlow.first()
            if (!userModifiedLanguage) {
                _language.value = savedLang
            }
        }
        viewModelScope.launch {
            val savedMotion = preferencesRepository.reduceMotionFlow.first()
            if (!userModifiedReduceMotion) {
                _reduceMotion.value = savedMotion
            }
        }
        viewModelScope.launch {
            delay(950)
            _showEntranceSplash.value = false
            // Non-blocking startup update check (throttled to max once per 24h)
            updateManager.runStartupAutoCheckIfDue()
        }
    }

    fun dismissEntranceSplash() {
        _showEntranceSplash.value = false
    }

    // --- Wireless Update Actions ---

    fun checkForUpdatesManually() {
        updateManager.checkForUpdatesManually()
    }

    fun openUpdateDialogIfAvailable() {
        updateManager.openUpdateDialogIfAvailable()
    }

    fun dismissUpdateAsLater() {
        updateManager.selectLater()
    }

    fun closeUpdateDialog() {
        updateManager.closeDialogWithoutDismissing()
    }

    fun startUpdateDownloadAndInstall() {
        updateManager.startDownloadAndInstall()
    }

    fun cancelUpdateDownload() {
        updateManager.cancelDownload()
    }

    fun retryUpdateFlow() {
        updateManager.retryFromError()
    }

    fun buildManageUnknownSourcesIntent(): Intent = updateManager.buildManagePermissionIntent()

    fun onInstallPermissionReturned() {
        updateManager.onInstallPermissionReturned()
    }

    fun buildSystemInstallerIntent(): Intent? = updateManager.buildSystemInstallerIntent()

    fun onSystemInstallerResult(resultCode: Int) {
        updateManager.onSystemInstallerResult(resultCode)
    }

    fun toggleLanguage() {
        val next = if (_language.value == AppLanguage.EN) AppLanguage.UR else AppLanguage.EN
        setLanguage(next)
    }

    fun setLanguage(lang: AppLanguage) {
        userModifiedLanguage = true
        _language.value = lang
        viewModelScope.launch {
            preferencesRepository.setLanguage(lang)
        }
        val selectedId = _scannerState.value.selectedSampleId
        if (selectedId != null && _scannerState.value.analysisState is AnalysisUiState.Idle) {
            val scenario = EducationalContentRepository.sampleScenarios.firstOrNull { it.id == selectedId }
            if (scenario != null) {
                val localizedText = if (lang == AppLanguage.UR) scenario.messageUr else scenario.message
                _scannerState.update { it.copy(messageInput = localizedText) }
            }
        }
    }

    fun setReduceMotion(enabled: Boolean) {
        userModifiedReduceMotion = true
        _reduceMotion.value = enabled
        viewModelScope.launch {
            preferencesRepository.setReduceMotion(enabled)
        }
    }

    // --- Scanner Actions ---

    fun onMessageChanged(newValue: String) {
        val clamped = newValue.take(MAX_MESSAGE_LENGTH)
        _scannerState.update {
            it.copy(
                messageInput = clamped,
                validationError = null,
                selectedSampleId = null
            )
        }
    }

    fun onOptionalUrlChanged(newValue: String) {
        val clamped = newValue.take(MAX_URL_LENGTH)
        _scannerState.update {
            it.copy(
                optionalUrlInput = clamped,
                validationError = null
            )
        }
    }

    fun clearScannerInputs() {
        _scannerState.value = ScannerScreenState()
    }

    fun resetToNewScan() {
        _scannerState.update {
            it.copy(
                validationError = null,
                analysisState = AnalysisUiState.Idle
            )
        }
    }

    fun selectSampleScenario(scenario: SampleScenario, lang: AppLanguage) {
        val sampleText = if (lang == AppLanguage.UR) scenario.messageUr else scenario.message
        _scannerState.update {
            it.copy(
                messageInput = sampleText,
                optionalUrlInput = scenario.sampleUrl,
                validationError = null,
                selectedSampleId = scenario.id,
                analysisState = AnalysisUiState.Idle
            )
        }
    }

    fun analyzeMessage() {
        val current = _scannerState.value
        if (current.analysisState is AnalysisUiState.Scanning) return

        val msg = current.messageInput.trim()
        val url = current.optionalUrlInput.trim()
        val currentLang = language.value

        if (msg.isEmpty() && url.isEmpty()) {
            val errMsg = if (currentLang == AppLanguage.UR) {
                "تجزیہ شروع کرنے کے لیے براہِ کرم کوئی پیغام یا لنک درج کریں۔"
            } else {
                "Please enter a message or suspicious URL before running analysis."
            }
            _scannerState.update { it.copy(validationError = errMsg) }
            return
        }

        viewModelScope.launch {
            try {
                _scannerState.update {
                    it.copy(
                        validationError = null,
                        analysisState = AnalysisUiState.Scanning(AnalysisStage.PREPARING, 0.15f)
                    )
                }

                val result = aiRepository.analyzeMessage(
                    request = MessageAnalysisRequest(
                        message = msg,
                        optionalUrl = url,
                        language = currentLang
                    ),
                    onStageChanged = { stage ->
                        val progress = when (stage) {
                            AnalysisStage.PREPARING -> 0.18f
                            AnalysisStage.INSPECTING_MESSAGE -> 0.40f
                            AnalysisStage.IDENTIFYING_WARNINGS -> 0.65f
                            AnalysisStage.GENERATING_ASSESSMENT -> 0.85f
                            AnalysisStage.FINALIZING -> 0.96f
                        }
                        _scannerState.update {
                            it.copy(analysisState = AnalysisUiState.Scanning(stage, progress))
                        }
                    }
                )

                val record = SessionScanRecord(
                    id = result.id,
                    shortLabel = result.shortLabel,
                    shortLabelUr = result.shortLabelUr,
                    riskLevel = result.riskLevel,
                    riskScore = result.riskScore,
                    timestampEpochMs = result.timestampEpochMs,
                    analysisType = result.analysisType,
                    result = result
                )
                _sessionHistory.update { listOf(record) + it }
                _scannerState.update { it.copy(analysisState = AnalysisUiState.Success(result)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _scannerState.update {
                    it.copy(
                        analysisState = AnalysisUiState.Error(
                            e.localizedMessage ?: "Analysis could not be completed. Please try again."
                        )
                    )
                }
            }
        }
    }

    fun openHistoryRecord(record: SessionScanRecord) {
        _scannerState.update {
            it.copy(
                validationError = null,
                analysisState = AnalysisUiState.Success(record.result)
            )
        }
    }

    fun clearSessionHistory() {
        _sessionHistory.value = emptyList()
    }

    // --- URL Inspector Actions ---

    fun onInspectorUrlChanged(newValue: String) {
        _urlInspectorState.update {
            it.copy(
                urlInput = newValue.take(MAX_URL_LENGTH),
                validationError = null
            )
        }
    }

    fun clearUrlInspector() {
        _urlInspectorState.value = UrlInspectorScreenState()
    }

    fun inspectUrl(sampleUrl: String? = null) {
        val raw = (sampleUrl ?: _urlInspectorState.value.urlInput).trim()
        if (raw.isEmpty()) {
            val err = if (language.value == AppLanguage.UR) {
                "معائنے کے لیے براہِ کرم کوئی لنک (URL) درج کریں۔"
            } else {
                "Please paste a URL to inspect its structure."
            }
            _urlInspectorState.update { it.copy(validationError = err) }
            return
        }
        val inspection = UrlInspectorEngine.inspect(raw)
        _urlInspectorState.update {
            it.copy(
                urlInput = raw,
                validationError = null,
                result = inspection
            )
        }
    }

    // --- Cyber Academy Actions ---

    fun markLessonCompleted(lessonId: String) {
        _completedLessonIds.update { it + lessonId }
    }

    // --- Quiz Actions ---

    fun selectQuizOption(index: Int) {
        if (_quizState.value.isAnswerSubmitted) return
        _quizState.update { it.copy(selectedOptionIndex = index) }
    }

    fun submitQuizAnswer() {
        val state = _quizState.value
        val selected = state.selectedOptionIndex ?: return
        if (state.isAnswerSubmitted) return
        val currentQ = state.questions.getOrNull(state.currentIndex) ?: return
        val isCorrect = selected == currentQ.correctIndex
        _quizState.update {
            it.copy(
                isAnswerSubmitted = true,
                score = if (isCorrect) it.score + 1 else it.score
            )
        }
    }

    fun nextQuizQuestion() {
        val state = _quizState.value
        val nextIdx = state.currentIndex + 1
        if (nextIdx >= state.questions.size) {
            _quizState.update { it.copy(isCompleted = true) }
        } else {
            _quizState.update {
                it.copy(
                    currentIndex = nextIdx,
                    selectedOptionIndex = null,
                    isAnswerSubmitted = false
                )
            }
        }
    }

    fun restartQuiz() {
        _quizState.value = QuizScreenState(
            questions = EducationalContentRepository.quizQuestions.shuffled()
        )
    }
}
