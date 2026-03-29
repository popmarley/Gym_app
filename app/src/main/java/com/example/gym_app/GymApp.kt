package com.example.gym_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import androidx.core.content.ContextCompat
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SCREEN_TODAY = "today"
private const val SCREEN_PLAN = "plan"
private const val SCREEN_WORKOUT = "workout"
private const val SCREEN_LIBRARY = "library"
private const val SCREEN_PROGRESS = "progress"
private const val SCREEN_SETTINGS = "settings"
private const val SCREEN_RELEASE_NOTES = "release_notes"
private const val SCREEN_PROGRAM_EDITOR = "program_editor"
private const val SCREEN_ACHIEVEMENTS = "achievements"

private data class InAppNotice(
    val title: String,
    val detail: String,
    val assetPath: String? = null,
    val token: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymFlowApp() {
    val context = LocalContext.current
    val store = remember { GymStore(context) }
    val initialState = remember {
        store.loadState().applyDailyOpen().withAchievementUnlocks().also { store.saveState(it) }
    }
    var persistedState by remember { mutableStateOf(initialState) }
    var currentScreen by rememberSaveable { mutableStateOf(SCREEN_TODAY) }
    var settingsReturnScreen by rememberSaveable { mutableStateOf(SCREEN_TODAY) }
    var selectedWorkoutId by rememberSaveable { mutableStateOf(recommendedWorkout(initialState).id) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var customExerciseQuery by rememberSaveable { mutableStateOf("") }
    var selectedCustomDayId by rememberSaveable { mutableStateOf(customProgramTemplate(initialState).first().id) }
    var showingProfileSetup by rememberSaveable { mutableStateOf(!initialState.profileCompleted) }
    var pendingTimerDayId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingTimerDayTitle by rememberSaveable { mutableStateOf("") }
    var inAppNotice by remember { mutableStateOf<InAppNotice?>(null) }
    val expandedCards = remember { mutableStateMapOf<String, Boolean>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val timerSnapshot by WorkoutTimerController.state.collectAsState()

    LaunchedEffect(Unit) {
        WorkoutTimerController.sync(context)
    }

    LaunchedEffect(inAppNotice?.token) {
        val currentNotice = inAppNotice ?: return@LaunchedEffect
        delay(5_000)
        if (inAppNotice?.token == currentNotice.token) {
            inAppNotice = null
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val pendingDayId = pendingTimerDayId
        val pendingDayTitle = pendingTimerDayTitle
        pendingTimerDayId = null
        pendingTimerDayTitle = ""
        if (granted && pendingDayId != null) {
            WorkoutTimerController.start(context, pendingDayId, pendingDayTitle)
        } else if (!granted) {
            scope.launch {
                snackbarHostState.showSnackbar("Bildirim izni olmadan arka plan sayacı başlatılamaz.")
            }
        }
    }

    fun persist(update: (PersistedState) -> PersistedState): PersistedState {
        val oldState = persistedState
        val newState = update(oldState).normalizeForCurrentWeek().withAchievementUnlocks()
        persistedState = newState
        val currentPlanIds = workoutPlan(newState).map { it.id }.toSet()
        if (selectedWorkoutId !in currentPlanIds) {
            selectedWorkoutId = recommendedWorkout(newState).id
        }
        val customPlanIds = customProgramTemplate(newState).map { it.id }.toSet()
        if (selectedCustomDayId !in customPlanIds) {
            selectedCustomDayId = customProgramTemplate(newState).first().id
        }
        store.saveState(newState)
        inAppNotice = buildNoticeForStateChange(oldState, newState)
        return newState
    }

    fun requestWorkoutTimerStart(day: WorkoutDay) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            WorkoutTimerController.start(context, day.id, day.title)
        } else {
            pendingTimerDayId = day.id
            pendingTimerDayTitle = day.title
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun openMain(screen: String) {
        currentScreen = screen
        if (screen == SCREEN_WORKOUT) selectedWorkoutId = recommendedWorkout(persistedState).id
    }

    fun openWorkout(dayId: String) {
        selectedWorkoutId = dayId
        currentScreen = SCREEN_WORKOUT
    }

    fun openSettingsFromCurrent() {
        settingsReturnScreen = currentScreen
        currentScreen = SCREEN_SETTINGS
    }

    val normalizedTodayState = persistedState.normalizeForCurrentWeek()
    val activeWorkout = workoutById(selectedWorkoutId, normalizedTodayState)
    val todayRecommended = recommendedWorkout(normalizedTodayState)

    if (showingProfileSetup) {
        BackHandler(enabled = persistedState.profileCompleted) {
            showingProfileSetup = false
        }
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            ProfileSetupScreen(
                state = persistedState,
                onBack = if (persistedState.profileCompleted) ({ showingProfileSetup = false }) else null,
                onSaveProfile = { age, gender, goal, currentWeight, targetWeight, measurements, style, level ->
                    val updated = persist {
                        it.completeProfile(
                            age = age,
                            genderLabel = gender,
                            goalLabel = goal,
                            currentWeightKg = currentWeight,
                            targetWeightKg = targetWeight,
                            measurements = measurements,
                            style = style,
                            level = level
                        )
                    }
                    selectedWorkoutId = recommendedWorkout(updated).id
                    showingProfileSetup = false
                    WorkoutTimerController.reset(context)
                }
            )
        }
        return
    }

    BackHandler(enabled = currentScreen != SCREEN_TODAY) {
        when (currentScreen) {
            SCREEN_SETTINGS -> currentScreen = settingsReturnScreen
            SCREEN_RELEASE_NOTES, SCREEN_PROGRAM_EDITOR -> currentScreen = SCREEN_SETTINGS
            SCREEN_ACHIEVEMENTS -> currentScreen = SCREEN_PROGRESS
            else -> openMain(SCREEN_TODAY)
        }
    }

    val title = screenTitle(currentScreen, activeWorkout.title)
    val showBack = currentScreen != SCREEN_TODAY

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = {
                            when (currentScreen) {
                                SCREEN_SETTINGS -> currentScreen = settingsReturnScreen
                                SCREEN_RELEASE_NOTES, SCREEN_PROGRAM_EDITOR -> currentScreen = SCREEN_SETTINGS
                                SCREEN_ACHIEVEMENTS -> currentScreen = SCREEN_PROGRESS
                                else -> openMain(SCREEN_TODAY)
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                        }
                    }
                },
                actions = {
                    if (currentScreen != SCREEN_SETTINGS) {
                        IconButton(onClick = { openSettingsFromCurrent() }) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Ayarlar")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (currentScreen != SCREEN_SETTINGS && currentScreen != SCREEN_RELEASE_NOTES && currentScreen != SCREEN_PROGRAM_EDITOR && currentScreen != SCREEN_ACHIEVEMENTS) {
                NavigationBar {
                    navItems().forEach { item ->
                        NavigationBarItem(
                            selected = currentScreen == item.screen,
                            onClick = { openMain(item.screen) },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                SCREEN_TODAY -> TodayScreen(paddingValues, normalizedTodayState, todayRecommended, ::openWorkout)
                SCREEN_PLAN -> ProgramScreen(paddingValues, normalizedTodayState, ::openWorkout)
                SCREEN_WORKOUT -> WorkoutScreen(
                    paddingValues, activeWorkout, normalizedTodayState, expandedCards,
                    onSetDone = { exercise -> persist { it.completeSet(activeWorkout, exercise.id) } },
                    onUndoSet = { exercise -> persist { it.undoSet(activeWorkout, exercise.id) } },
                    onResetDay = { persist { it.resetDay(activeWorkout) } },
                    onCompleteWorkout = {
                        persist { it.completeWorkout(activeWorkout) }
                        scope.launch { snackbarHostState.showSnackbar("Harika. ${activeWorkout.title} tamamlandı.") }
                    },
                    onSelectDay = ::openWorkout,
                    timerSnapshot = timerSnapshot,
                    onStartWorkoutTimer = { requestWorkoutTimerStart(activeWorkout) },
                    onPauseWorkoutTimer = { WorkoutTimerController.pause(context) },
                    onResetWorkoutTimer = { WorkoutTimerController.reset(context) }
                )
                SCREEN_LIBRARY -> LibraryScreen(paddingValues, searchQuery, { searchQuery = it }, expandedCards, normalizedTodayState.lowMediaMode)
                SCREEN_PROGRESS -> ProgressScreen(
                    paddingValues = paddingValues,
                    state = normalizedTodayState,
                    onOpenAchievements = { currentScreen = SCREEN_ACHIEVEMENTS }
                )
                SCREEN_SETTINGS -> SettingsScreen(
                    paddingValues, normalizedTodayState,
                    onSelectProgramStyle = { style ->
                        persist { it.selectProgramStyle(style) }
                        selectedWorkoutId = recommendedWorkout(persistedState).id
                    },
                    onSelectProgramLevel = { level ->
                        persist { it.selectProgramLevel(level) }
                        selectedWorkoutId = recommendedWorkout(persistedState).id
                    },
                    onToggleMediaMode = { persist { it.toggleLowMediaMode() } },
                    onOpenReleaseNotes = { currentScreen = SCREEN_RELEASE_NOTES },
                    onOpenProfileSetup = { showingProfileSetup = true },
                    onUseRecommendedProgram = { persist { it.useRecommendedProgram() } },
                    onUseCustomProgram = {
                        val updated = persist { it.enableCustomProgram() }
                        selectedCustomDayId = customProgramTemplate(updated).first().id
                    },
                    onResetCustomProgram = {
                        val updated = persist { it.resetCustomProgram() }
                        selectedCustomDayId = customProgramTemplate(updated).first().id
                    },
                    onOpenProgramEditor = {
                        val updated = persist { it.enableCustomProgram() }
                        selectedCustomDayId = customProgramTemplate(updated).first().id
                        currentScreen = SCREEN_PROGRAM_EDITOR
                    }
                )
                SCREEN_RELEASE_NOTES -> ReleaseNotesScreen(paddingValues)
                SCREEN_PROGRAM_EDITOR -> ProgramEditorScreen(
                    paddingValues = paddingValues,
                    state = normalizedTodayState,
                    selectedDayId = selectedCustomDayId,
                    query = customExerciseQuery,
                    onSelectDay = { selectedCustomDayId = it },
                    onQueryChange = { customExerciseQuery = it },
                    onUpdateTitle = { dayId, title -> persist { it.updateCustomDayTitle(dayId, title) } },
                    onClearDay = { dayId -> persist { it.clearCustomDay(dayId) } },
                    onAddExercise = { dayId, exercise -> persist { it.addExerciseToCustomDay(dayId, exercise) } },
                    onRemoveExercise = { dayId, exerciseId -> persist { it.removeExerciseFromCustomDay(dayId, exerciseId) } },
                    onResetFromRecommended = {
                        val updated = persist { it.resetCustomProgram() }
                        selectedCustomDayId = customProgramTemplate(updated).first().id
                    }
                )
                SCREEN_ACHIEVEMENTS -> AchievementsScreen(
                    paddingValues = paddingValues,
                    state = normalizedTodayState
                )
            }
            NoticeOverlay(notice = inAppNotice, modifier = Modifier.align(Alignment.TopEnd).padding(top = paddingValues.calculateTopPadding() + 10.dp, end = 16.dp))
        }
    }
}

private data class NavItem(val screen: String, val label: String, val icon: ImageVector)

private fun navItems(): List<NavItem> = listOf(
    NavItem(SCREEN_TODAY, "Bugün", Icons.Outlined.TrackChanges),
    NavItem(SCREEN_PLAN, "Programım", Icons.Outlined.CalendarMonth),
    NavItem(SCREEN_WORKOUT, "Antrenman", Icons.Outlined.FitnessCenter),
    NavItem(SCREEN_LIBRARY, "Kütüphane", Icons.AutoMirrored.Outlined.MenuBook),
    NavItem(SCREEN_PROGRESS, "İlerleme", Icons.Outlined.AutoGraph)
)

private fun screenTitle(screen: String, workoutTitle: String): String {
    return when (screen) {
        SCREEN_TODAY -> "Bugün"
        SCREEN_PLAN -> "Programım"
        SCREEN_WORKOUT -> workoutTitle
        SCREEN_LIBRARY -> "Hareket Kütüphanesi"
        SCREEN_PROGRESS -> "İlerlemem"
        SCREEN_SETTINGS -> "Ayarlar"
        SCREEN_RELEASE_NOTES -> "Sürüm Notları"
        SCREEN_PROGRAM_EDITOR -> "Kişisel Program"
        SCREEN_ACHIEVEMENTS -> "Başarımlar"
        else -> "GymFlow"
    }
}

@Composable
private fun ProfileSetupScreen(
    state: PersistedState,
    onBack: (() -> Unit)?,
    onSaveProfile: (Int, String, String, Double, Double, BodyMeasurements, ProgramStyle, ProgramLevel) -> Unit
) {
    var age by rememberSaveable { mutableIntStateOf(state.age.coerceIn(13, 90)) }
    var currentWeight by rememberSaveable { mutableStateOf(state.currentWeightKg.coerceAtLeast(40.0)) }
    var targetWeight by rememberSaveable { mutableStateOf(state.targetWeightKg.coerceAtLeast(currentWeight)) }
    var selectedGender by rememberSaveable { mutableStateOf(state.genderLabel.ifBlank { "Erkek" }) }
    var selectedGoal by rememberSaveable { mutableStateOf(state.goalLabel.ifBlank { "Kas / kütle artışı" }) }
    var selectedStyleName by rememberSaveable { mutableStateOf(state.programStyle().name) }
    var selectedLevelName by rememberSaveable { mutableStateOf(state.programLevel().name) }
    var chestInput by rememberSaveable { mutableStateOf(decimalInputValue(state.currentMeasurements.chestCm)) }
    var waistInput by rememberSaveable { mutableStateOf(decimalInputValue(state.currentMeasurements.waistCm)) }
    var hipInput by rememberSaveable { mutableStateOf(decimalInputValue(state.currentMeasurements.hipCm)) }
    var armInput by rememberSaveable { mutableStateOf(decimalInputValue(state.currentMeasurements.armCm)) }
    var thighInput by rememberSaveable { mutableStateOf(decimalInputValue(state.currentMeasurements.thighCm)) }
    var shoulderInput by rememberSaveable { mutableStateOf(decimalInputValue(state.currentMeasurements.shoulderCm)) }

    val selectedStyle = ProgramStyle.valueOf(selectedStyleName)
    val selectedLevel = ProgramLevel.valueOf(selectedLevelName)
    val measurements = BodyMeasurements(
        chestCm = decimalFromInput(chestInput),
        waistCm = decimalFromInput(waistInput),
        hipCm = decimalFromInput(hipInput),
        armCm = decimalFromInput(armInput),
        thighCm = decimalFromInput(thighInput),
        shoulderCm = decimalFromInput(shoulderInput)
    )

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionCard("Kişisel kurulum") {
                if (onBack != null) {
                    Text("Profilini ve hedeflerini güncelle.", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = onBack, modifier = Modifier.widthIn(min = 140.dp)) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Geri dön", maxLines = 1)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Text("İlk programını sana göre ayarlayalım.", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text("Bu seçimleri daha sonra ayarlardan yeniden düzenleyebilirsin.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            SectionCard("Profil bilgileri") {
                Text("Yaş", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                IntStepper(value = age, onMinus = { age = (age - 1).coerceAtLeast(13) }, onPlus = { age = (age + 1).coerceAtMost(90) })
                Spacer(modifier = Modifier.height(12.dp))
                Text("Cinsiyet", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                OptionSelectorRow(
                    options = listOf(
                        "Erkek" to { selectedGender = "Erkek" },
                        "Kadın" to { selectedGender = "Kadın" },
                        "Belirtmek istemiyorum" to { selectedGender = "Belirtmek istemiyorum" }
                    ),
                    selectedLabel = selectedGender
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Hedef", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                OptionSelectorRow(
                    options = listOf(
                        "Kas / kütle artışı" to { selectedGoal = "Kas / kütle artışı" },
                        "Genel form" to { selectedGoal = "Genel form" },
                        "Yağ kaybı" to { selectedGoal = "Yağ kaybı" }
                    ),
                    selectedLabel = selectedGoal
                )
            }
        }
        item {
            SectionCard("Kilo hedefi") {
                Text("Güncel ağırlık", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                WeightStepper(
                    weight = currentWeight,
                    onMinus = {
                        currentWeight = (currentWeight - 0.5).coerceAtLeast(40.0)
                        targetWeight = targetWeight.coerceAtLeast(currentWeight)
                    },
                    onPlus = {
                        currentWeight += 0.5
                        targetWeight = targetWeight.coerceAtLeast(currentWeight)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Hedef ağırlık", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                WeightStepper(
                    weight = targetWeight,
                    onMinus = { targetWeight = (targetWeight - 0.5).coerceAtLeast(currentWeight) },
                    onPlus = { targetWeight = targetWeight + 0.5 }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Kilo artışı için 14 gün yükseliş yoksa günlük kalorini yaklaşık 250-300 kcal artır.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            SectionCard("Bölgesel ölçüler") {
                Text("Ölçülerini şimdi girersen analiz alanı gelişimini gün gün gösterir.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                MeasurementInputField(label = "Göğüs", value = chestInput, onValueChange = { chestInput = sanitizeDecimalInput(it) })
                Spacer(modifier = Modifier.height(10.dp))
                MeasurementInputField(label = "Bel", value = waistInput, onValueChange = { waistInput = sanitizeDecimalInput(it) })
                Spacer(modifier = Modifier.height(10.dp))
                MeasurementInputField(label = "Kalça", value = hipInput, onValueChange = { hipInput = sanitizeDecimalInput(it) })
                Spacer(modifier = Modifier.height(10.dp))
                MeasurementInputField(label = "Kol", value = armInput, onValueChange = { armInput = sanitizeDecimalInput(it) })
                Spacer(modifier = Modifier.height(10.dp))
                MeasurementInputField(label = "Bacak", value = thighInput, onValueChange = { thighInput = sanitizeDecimalInput(it) })
                Spacer(modifier = Modifier.height(10.dp))
                MeasurementInputField(label = "Omuz", value = shoulderInput, onValueChange = { shoulderInput = sanitizeDecimalInput(it) })
            }
        }
        item {
            SectionCard("Başlangıç programı") {
                Text("Program tipi", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                OptionSelectorRow(
                    options = ProgramStyle.entries.map { style -> style.label to { selectedStyleName = style.name } },
                    selectedLabel = selectedStyle.label
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Düzey", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                OptionSelectorRow(
                    options = ProgramLevel.entries.map { level -> level.label to { selectedLevelName = level.name } },
                    selectedLabel = selectedLevel.label
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("${selectedLevel.label} • ${selectedLevel.weekRange}", color = MaterialTheme.colorScheme.primary)
                Text(selectedLevel.goalText, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(selectedStyle.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Button(
                onClick = {
                    onSaveProfile(age, selectedGender, selectedGoal, currentWeight, targetWeight, measurements, selectedStyle, selectedLevel)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (onBack == null) "Kurulumu tamamla" else "Profili kaydet")
            }
        }
    }
}

@Composable
private fun TodayScreen(
    paddingValues: PaddingValues,
    state: PersistedState,
    recommended: WorkoutDay,
    onOpenWorkout: (String) -> Unit
) {
    val preset = selectedProgram(state)
    val progress = profileProgress(state)
    val weeklyPercent = weeklyCompletionPercent(state)
    val showDailyProgress = !recommended.isRestDay
    LazyColumn(
        contentPadding = screenPadding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GradientHero(
                title = if (recommended.isRestDay) "Bugün toparlanma günü" else "Bugün ne yapacağım?",
                subtitle = if (recommended.isRestDay) recommended.recoveryText ?: recommended.summary else "${recommended.title} • ${recommended.estimatedMinutes} dakika",
                buttonLabel = if (recommended.isRestDay) "Yarının planına bak" else "Antrenmanı başlat",
                onClick = { onOpenWorkout(recommended.id) }
            )
        }
        item {
            SectionCard("Gün Seç") {
                Text("İstersen haftadan farklı bir günü doğrudan açabilirsin.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                DaySelectorRow(state = state, selectedDayId = recommended.id, onSelectDay = onOpenWorkout)
            }
        }
        item {
            SectionCard("Aktif Program") {
                Text("${preset.title} • ${preset.cadenceLabel}", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(preset.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (showDailyProgress) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        InlineIdentityPill(
                            assetPath = "file:///android_asset/seviye/${progress.levelAssetIndex}.png",
                            label = "Seviye ${progress.level}",
                            modifier = Modifier.weight(1f)
                        )
                        InlineIdentityPill(
                            assetPath = "file:///android_asset/rutbe/${progress.rankAssetIndex}.svg",
                            label = progress.rankName,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        if (showDailyProgress) {
            item { SummaryRow(state) }
            item {
                SectionCard("Bu hafta durumun") {
                    Text(motivationMessage(state), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(progress = { weeklyPercent / 100f }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$weeklyPercent% haftalık tamamlama", fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item {
            SectionCard("Hızlı bilgi") {
                Text("Kalori ayarı: 14 gün artış yoksa günlük alıma yaklaşık 250-300 kcal ekle.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Protein hedefi: 95 - 130 g", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Mevcut ağırlık: ${weightDisplay(state.currentWeightKg)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Otomatik yedekleme: Hazır", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ProgramScreen(
    paddingValues: PaddingValues,
    state: PersistedState,
    onOpenWorkout: (String) -> Unit
) {
    val preset = selectedProgram(state)
    val isCustomProgram = state.programSource() == ProgramSource.KISEL
    LazyColumn(
        contentPadding = screenPadding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionCard("Haftalık plan") {
                Text("${preset.title} • ${preset.cadenceLabel}", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(preset.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (isCustomProgram) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Kişisel mod açık: günler ve hareketler senin düzenine göre özelleştirildi.", color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(preset.recommendation, color = MaterialTheme.colorScheme.primary)
            }
        }
        item {
            SectionCard("Isınma ve Progresyon") {
                preset.guidance.forEach { line ->
                    Text("• $line", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            SectionCard("Beslenme Notları") {
                preset.nutritionNotes.forEach { line ->
                    Text("• $line", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        items(workoutPlan(state)) { day ->
            val ratio = dayCompletionRatio(day, state)
            SectionCard("${scheduledDayLabel(day)} • ${day.title}") {
                Text(day.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(onClick = { }, label = { Text(if (day.isRestDay) "Toparlanma" else "${day.estimatedMinutes} dk • ${day.focus}") })
                if (!day.isRestDay) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Hareketler", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    day.exercises.forEach { exercise ->
                        Text("• ${exercise.name} • ${exercise.targetArea}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${(ratio * 100).toInt()}% gün ilerlemesi", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { onOpenWorkout(day.id) }) { Text("Günü aç") }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(day.recoveryText.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun WorkoutScreen(
    paddingValues: PaddingValues,
    day: WorkoutDay,
    state: PersistedState,
    expandedCards: MutableMap<String, Boolean>,
    onSetDone: (Exercise) -> Unit,
    onUndoSet: (Exercise) -> Unit,
    onResetDay: () -> Unit,
    onCompleteWorkout: () -> Unit,
    onSelectDay: (String) -> Unit,
    timerSnapshot: WorkoutTimerSnapshot,
    onStartWorkoutTimer: () -> Unit,
    onPauseWorkoutTimer: () -> Unit,
    onResetWorkoutTimer: () -> Unit
) {
    val progress = dayCompletionRatio(day, state)
    val workoutCompleted = isWorkoutCompleted(state, day)
    val elapsedMs = rememberVisibleElapsedMs(timerSnapshot, day.id)
    LazyColumn(
        contentPadding = screenPadding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionCard("Gün seçimi") {
                Text("Program içinden istediğin güne geçebilirsin.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                DaySelectorRow(state = state, selectedDayId = day.id, onSelectDay = onSelectDay)
            }
        }
        item {
            if (day.isRestDay) {
                SectionCard(scheduledDayLabel(day)) {
                    Text(day.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                SectionCard("${scheduledDayLabel(day)} • ${day.focus}") {
                    Text(day.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${(progress * 100).toInt()}% tamamlandı", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    WorkoutTimerSection(
                        day = day,
                        timerSnapshot = timerSnapshot,
                        elapsedMs = elapsedMs,
                        onStart = onStartWorkoutTimer,
                        onPause = onPauseWorkoutTimer,
                        onReset = onResetWorkoutTimer
                    )
                }
            }
        }
        if (day.isRestDay) {
            item {
                SectionCard("Bugünün odağı") {
                    Text(day.recoveryText ?: "Dinlenme ve su tüketimi.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(day.exercises) { exercise ->
                ExerciseCard(day, exercise, state, expandedCards, onSetDone, onUndoSet)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onCompleteWorkout, enabled = progress >= 1f && !workoutCompleted, modifier = Modifier.weight(1f)) {
                        Text(if (workoutCompleted) "Antrenman tamamlandı" else "Antrenmanı tamamla")
                    }
                    OutlinedButton(onClick = onResetDay, modifier = Modifier.weight(1f)) {
                        Text("Bugünü sıfırla")
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryScreen(
    paddingValues: PaddingValues,
    query: String,
    onQueryChange: (String) -> Unit,
    expandedCards: MutableMap<String, Boolean>,
    lowMediaMode: Boolean
) {
    val filtered = allExercises().filter {
        it.name.contains(query, ignoreCase = true) || it.targetArea.contains(query, ignoreCase = true)
    }
    LazyColumn(
        contentPadding = screenPadding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Hareket ara") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        items(filtered) { exercise ->
            val expanded = expandedCards["library_${exercise.id}"] == true
            SectionCard(exercise.name) {
                Text("${exercise.targetArea} • ${exercise.equipment}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(exercise.quickNote)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { expandedCards["library_${exercise.id}"] = !expanded }) {
                    Text(if (expanded) "Detayı gizle" else "Detayı aç")
                }
                if (expanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    MediaPlaceholder(lowMediaMode, exercise.mediaCaption)
                    Spacer(modifier = Modifier.height(12.dp))
                    exercise.steps.forEach { step ->
                        Text("• $step", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sık hata: ${exercise.commonMistake}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("İpucu: ${exercise.tip}", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun ProgressScreen(
    paddingValues: PaddingValues,
    state: PersistedState,
    onOpenAchievements: () -> Unit
) {
    val badges = unlockedBadges(state)
    val achievements = achievementProgressList(state)
    val lockedAchievements = achievements.filterNot { it.isUnlocked }
    val progress = profileProgress(state)
    val preset = selectedProgram(state)
    val activityEntries = activityLogEntries(state).take(20)
    var selectedMetricName by rememberSaveable { mutableStateOf(AnalysisMetric.WEIGHT.name) }
    var selectedBadgeId by rememberSaveable { mutableStateOf<String?>(null) }
    var logPage by rememberSaveable(activityEntries.size) { mutableIntStateOf(0) }
    val selectedMetric = AnalysisMetric.valueOf(selectedMetricName)
    val chartPoints = analysisSeriesFor(state, selectedMetric)
    val pageCount = (activityEntries.size.coerceAtLeast(1) + 4) / 5
    val safeLogPage = logPage.coerceIn(0, pageCount - 1)
    val visibleLogs = activityEntries.drop(safeLogPage * 5).take(5)
    val selectedBadge = badges.firstOrNull { it.id == selectedBadgeId }

    LazyColumn(
        contentPadding = screenPadding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { SummaryRow(state) }
        item {
            SectionCard("Tecrübe ve Rütbe") {
                CompactIdentityRow(
                    title = "Seviye",
                    assetPath = "file:///android_asset/seviye/${progress.levelAssetIndex}.png",
                    primaryLabel = "Seviye ${progress.level}",
                    secondaryLabel = progress.levelTitle
                )
                Spacer(modifier = Modifier.height(10.dp))
                CompactIdentityRow(
                    title = "Rütbe",
                    assetPath = "file:///android_asset/rutbe/${progress.rankAssetIndex}.svg",
                    primaryLabel = progress.rankName,
                    secondaryLabel = "Şu anki motivasyon rütben"
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(progress = { progress.progressToNextLevel }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Text("${progress.totalExperience} XP toplam", fontWeight = FontWeight.SemiBold)
                Text(progress.serviceMedalLabel, color = MaterialTheme.colorScheme.primary)
                if (progress.level < MAX_PROFILE_LEVEL) {
                    Text("${progress.currentLevelXp} / ${progress.neededXpForNextLevel} XP", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            SectionCard("Analiz ve Ölçümler") {
                Text("Aktif program: ${preset.title}", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Mevcut ağırlık: ${weightDisplay(state.currentWeightKg)} • Hedef: ${weightDisplay(state.targetWeightKg)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                OptionSelectorRow(
                    options = AnalysisMetric.entries.map { metric -> metric.label to { selectedMetricName = metric.name } },
                    selectedLabel = selectedMetric.label
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (chartPoints.isEmpty()) {
                    Text("Henüz yeterli ölçüm verisi yok. Profil ekranından ölçülerini güncelle.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    val firstValue = chartPoints.first().second
                    val latestValue = chartPoints.last().second
                    val delta = latestValue - firstValue
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        MetricCard("İlk kayıt", measurementDisplay(firstValue, selectedMetric.unit), Icons.Outlined.CalendarMonth, Modifier.weight(1f))
                        MetricCard("Son kayıt", measurementDisplay(latestValue, selectedMetric.unit), Icons.Outlined.AutoGraph, Modifier.weight(1f))
                        MetricCard("Fark", signedMeasurementDisplay(delta, selectedMetric.unit), Icons.Outlined.TrackChanges, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TrendChart(points = chartPoints, metric = selectedMetric)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Toplam ${chartPoints.size} kayıt • İlk gün ${chartPoints.first().first.formatDateLabel()} • Son gün ${chartPoints.last().first.formatDateLabel()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Son ölçüler", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                MeasurementSnapshotGrid(state.currentMeasurements)
            }
        }
        item {
            SectionCard("Rozetler ve Başarımlar") {
                Text("${achievements.count { it.isUnlocked }} / ${achievements.size} başarım açıldı", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                if (badges.isEmpty()) {
                    Text("Rozetler antrenman tamamlandıkça açılacak.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    RewardGrid(badges = badges, onSelectBadge = { selectedBadgeId = it.id })
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Açılmayan başarımlar", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                if (lockedAchievements.isEmpty()) {
                    Text("Tüm başarımlar açıldı.", color = MaterialTheme.colorScheme.primary)
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    lockedAchievements.take(3).forEach { achievement ->
                        AchievementPreviewRow(achievement)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                TextButton(onClick = onOpenAchievements) {
                    Text("Başarımları aç")
                }
            }
        }
        item {
            SectionCard("XP Kuralları") {
                Text("Günlük giriş: +120 XP", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Tamamlanan zorunlu gün: seviye düzeyine göre +650 / +800 / +950 XP", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Haftalık tüm zorunlu günler biterse ek bonus XP verilir.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Giriş yapılmayan günler ve kaçırılan planlı günler, bir sonraki açılışta XP düşüşü olarak işlenir.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            SectionCard("Analitik Özeti") {
                Text("Toplam tamamlanan antrenman: ${totalCompletedWorkouts(state)}")
                Text("Yaklaşık hacim: ${estimatedTotalVolume(state)} kg", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Bu hafta tamamlama: ${weeklyCompletionPercent(state)}%", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            SectionCard("Beslenme ve Takip Notları") {
                Text("Kalori ayarı: 14 gün artış yoksa günlük alıma yaklaşık 250-300 kcal ekle.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Protein hedefi: 95 - 130 g", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            SectionCard("Etkinlik Günlüğü") {
                if (activityEntries.isEmpty()) {
                    Text("Henüz etkinlik kaydı oluşmadı.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(
                        "Son 20 işlem içinden sayfa ${safeLogPage + 1} / $pageCount",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { logPage = (safeLogPage - 1).coerceAtLeast(0) },
                            enabled = safeLogPage > 0,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.ChevronLeft, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Önceki", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { logPage = (safeLogPage + 1).coerceAtMost(pageCount - 1) },
                            enabled = safeLogPage < pageCount - 1,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sonraki", maxLines = 1)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Outlined.ChevronRight, contentDescription = null)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    visibleLogs.groupBy { it.recordedOn }.forEach { (date, entries) ->
                        Text(date.toDisplayDate(), fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        entries.forEach { entry ->
                            ActivityLogRow(entry)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
    if (selectedBadge != null) {
        AlertDialog(
            onDismissRequest = { selectedBadgeId = null },
            confirmButton = {
                TextButton(onClick = { selectedBadgeId = null }) {
                    Text("Kapat")
                }
            },
            title = { Text(selectedBadge.title) },
            text = { Text(selectedBadge.description) }
        )
    }
}

@Composable
private fun SettingsScreen(
    paddingValues: PaddingValues,
    state: PersistedState,
    onSelectProgramStyle: (ProgramStyle) -> Unit,
    onSelectProgramLevel: (ProgramLevel) -> Unit,
    onToggleMediaMode: () -> Unit,
    onOpenReleaseNotes: () -> Unit,
    onOpenProfileSetup: () -> Unit,
    onUseRecommendedProgram: () -> Unit,
    onUseCustomProgram: () -> Unit,
    onResetCustomProgram: () -> Unit,
    onOpenProgramEditor: () -> Unit
) {
    val activePreset = selectedProgram(state)
    val recommendedPreset = recommendedProgram(state)
    val isCustomProgram = state.programSource() == ProgramSource.KISEL
    LazyColumn(
        contentPadding = screenPadding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionCard("Kişisel profil") {
                Text(profileHeadline(state), fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Güncel ağırlık: ${weightDisplay(state.currentWeightKg)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Hedef ağırlık: ${weightDisplay(state.targetWeightKg)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Aktif hedef: ${state.goalLabel}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!state.currentMeasurements.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Son ölçüler: Bel ${measurementDisplay(state.currentMeasurements.waistCm)} • Göğüs ${measurementDisplay(state.currentMeasurements.chestCm)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onOpenProfileSetup) { Text("Profili düzenle") }
            }
        }
        item {
            SectionCard("Program Yönetimi") {
                Text("Aktif mod", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                OptionSelectorRow(
                    options = listOf(
                        ProgramSource.ONERILEN.label to onUseRecommendedProgram,
                        ProgramSource.KISEL.label to onUseCustomProgram
                    ),
                    selectedLabel = state.programSource().label
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(state.programSource().description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Önerilen varsayılan", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("${recommendedPreset.title} • ${recommendedPreset.cadenceLabel}", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(recommendedPreset.recommendation, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Program tipi", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                OptionSelectorRow(
                    options = ProgramStyle.entries.map { it.label to { onSelectProgramStyle(it) } },
                    selectedLabel = state.programStyle().label
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(state.programStyle().description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Düzey", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                OptionSelectorRow(
                    options = ProgramLevel.entries.map { it.label to { onSelectProgramLevel(it) } },
                    selectedLabel = state.programLevel().label
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("${state.programLevel().label} • ${state.programLevel().weekRange}", color = MaterialTheme.colorScheme.primary)
                Text(state.programLevel().goalText, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    if (isCustomProgram) {
                        "Şu an aktif: ${activePreset.title} • ${activePreset.cadenceLabel}"
                    } else {
                        "Şu an aktif olan plan önerilen varsayılan plan."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Not: Kişisel planda gün veya hareket değiştirildiğinde bu haftanın ilerlemesi yeni plana göre temizlenir.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (isCustomProgram) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onOpenProgramEditor, modifier = Modifier.weight(1f)) {
                            Text("Kişisel planı düzenle")
                        }
                        TextButton(onClick = onResetCustomProgram, modifier = Modifier.weight(1f)) {
                            Text("Önerilen planı kopyala")
                        }
                    }
                } else {
                    TextButton(onClick = onOpenProgramEditor) {
                        Text("Kişisel programı oluştur")
                    }
                }
            }
        }
        item {
            SectionCard("Veri Yedekleme") {
                Text("Android otomatik yedekleme etkin.", fontWeight = FontWeight.SemiBold)
                Text("Aynı Google hesabında yedekleme açıksa uygulamayı yeniden kurduğunda kayıtlı veriler geri yüklenebilir.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Son yerel kayıt: ${lastSavedDisplay(state)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            SectionCard("Performans Tercihi") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Düşük medya modu", fontWeight = FontWeight.SemiBold)
                        Text("Pil tüketimini azaltmak için görsel alanlarını hafif tutar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = state.lowMediaMode, onCheckedChange = { onToggleMediaMode() })
                }
            }
        }
        item {
            SectionCard("Uygulama bilgisi") {
                Text(VERSION_DISPLAY)
                Text("Geliştirici: Pop Marley © 2026", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onOpenReleaseNotes) { Text("Sürüm notlarını aç") }
            }
        }
    }
}

@Composable
private fun ReleaseNotesScreen(
    paddingValues: PaddingValues
) {
    val pages = releasePages()
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedPage = pages[selectedIndex]
    LazyColumn(
        contentPadding = screenPadding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionCard("Sürüm seçimi") {
                OptionSelectorRow(
                    options = pages.mapIndexed { index, page ->
                        page.versionLabel to { selectedIndex = index }
                    },
                    selectedLabel = selectedPage.versionLabel
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { selectedIndex = (selectedIndex - 1).coerceAtLeast(0) },
                        enabled = selectedIndex > 0,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Önceki")
                    }
                    OutlinedButton(
                        onClick = { selectedIndex = (selectedIndex + 1).coerceAtMost(pages.lastIndex) },
                        enabled = selectedIndex < pages.lastIndex,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sonraki")
                    }
                }
            }
        }
        item {
            SectionCard("Bu sürümde yapılanlar") {
                Text(selectedPage.versionLabel, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text(selectedPage.releasedOn, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                selectedPage.items.forEach { item ->
                    Text("• $item", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ProgramEditorScreen(
    paddingValues: PaddingValues,
    state: PersistedState,
    selectedDayId: String,
    query: String,
    onSelectDay: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onUpdateTitle: (String, String) -> Unit,
    onClearDay: (String) -> Unit,
    onAddExercise: (String, Exercise) -> Unit,
    onRemoveExercise: (String, String) -> Unit,
    onResetFromRecommended: () -> Unit
) {
    val plan = customProgramTemplate(state)
    val selectedDay = plan.firstOrNull { it.id == selectedDayId } ?: plan.first()
    val filteredExercises = allExercises()
        .filterNot { candidate -> selectedDay.exercises.any { it.name == candidate.name } }
        .filter {
            query.isBlank() ||
                it.name.contains(query, ignoreCase = true) ||
                it.targetArea.contains(query, ignoreCase = true) ||
                it.equipment.contains(query, ignoreCase = true)
        }
        .sortedBy { it.name }
    val groupedSelectedExercises = selectedDay.exercises.groupBy(::editorGroupLabel)
    val groupedFilteredExercises = filteredExercises.groupBy(::editorGroupLabel)

    LazyColumn(
        contentPadding = screenPadding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionCard("Kişisel plan") {
                Text("Bu düzen haftanın günlerine göre kalıcı çalışır. Örneğin salı için yaptığın seçim, her yeni haftada yine salı gününe uygulanır.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Egzersiz olmayan günler otomatik olarak dinlenme günü sayılır.", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onResetFromRecommended) {
                    Text("Önerilen planı yeniden kopyala")
                }
            }
        }
        item {
            SectionCard("Hafta düzeni") {
                Text("Düzenlemek istediğin günü seç.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                WeekdaySelectorRow(days = plan, selectedDayId = selectedDay.id, onSelectDay = onSelectDay)
            }
        }
        item {
            SectionCard("${selectedDay.dayLabel} planı") {
                OutlinedTextField(
                    value = selectedDay.title,
                    onValueChange = { onUpdateTitle(selectedDay.id, it) },
                    label = { Text("Seans adı") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Bu plan her ${selectedDay.dayLabel.lowercase()} tekrar eder.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (selectedDay.isRestDay) {
                        "Bu gün şu an dinlenme günü olarak işaretleniyor."
                    } else {
                        "Odak: ${selectedDay.focus} • Yaklaşık ${selectedDay.estimatedMinutes} dk"
                    },
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onClearDay(selectedDay.id) },
                    enabled = !selectedDay.isRestDay
                ) {
                    Text("Günü dinlenme yap")
                }
            }
        }
        item {
            SectionCard("Seçili hareketler") {
                if (selectedDay.exercises.isEmpty()) {
                    Text("Henüz hareket eklenmedi. Aşağıdan egzersiz eklediğinde bu gün aktif antrenmana dönüşür.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    editorGroupOrder().forEach { group ->
                        val exercises = groupedSelectedExercises[group].orEmpty()
                        if (exercises.isNotEmpty()) {
                            Text(group, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            exercises.forEach { exercise ->
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(exercise.name, fontWeight = FontWeight.SemiBold)
                                        Text("${exercise.targetArea} • ${exercise.equipment}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${exercise.repText} • ${exercise.restSeconds} sn dinlenme", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        TextButton(onClick = { onRemoveExercise(selectedDay.id, exercise.id) }) {
                                            Text("Bu hareketi kaldır")
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionCard("Bölgeye göre hareket ekle") {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    label = { Text("Hareket ara") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (filteredExercises.isEmpty()) {
                    Text("Aramana uygun yeni hareket bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    editorGroupOrder().forEach { group ->
                        val exercises = groupedFilteredExercises[group].orEmpty()
                        if (exercises.isNotEmpty()) {
                            Text(group, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            exercises.forEach { exercise ->
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(exercise.name, fontWeight = FontWeight.SemiBold)
                                        Text("${exercise.targetArea} • ${exercise.equipment}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${exercise.repText} • ${exercise.restSeconds} sn dinlenme", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        TextButton(onClick = { onAddExercise(selectedDay.id, exercise) }) {
                                            Text("Bu güne ekle")
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementsScreen(
    paddingValues: PaddingValues,
    state: PersistedState
) {
    var showingLockedOnly by rememberSaveable { mutableStateOf(true) }
    val achievements = achievementProgressList(state)
    val visibleAchievements = if (showingLockedOnly) achievements.filterNot { it.isUnlocked } else achievements

    LazyColumn(
        contentPadding = screenPadding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionCard("Başarım filtresi") {
                OptionSelectorRow(
                    options = listOf(
                        "Açılmayanlar" to { showingLockedOnly = true },
                        "Tümü" to { showingLockedOnly = false }
                    ),
                    selectedLabel = if (showingLockedOnly) "Açılmayanlar" else "Tümü"
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "${achievements.count { it.isUnlocked }} / ${achievements.size} başarım açıldı.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (visibleAchievements.isEmpty()) {
            item {
                SectionCard("Başarımlar") {
                    Text("Gösterilecek kilitli başarım kalmadı.", color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            items(visibleAchievements) { achievement ->
                SectionCard(achievement.title) {
                    AchievementDetailRow(achievement)
                }
            }
        }
    }
}

@Composable
private fun AchievementPreviewRow(achievement: AchievementProgress) {
    AchievementRowContent(achievement = achievement, compact = true)
}

@Composable
private fun AchievementDetailRow(achievement: AchievementProgress) {
    AchievementRowContent(achievement = achievement, compact = false)
}

@Composable
private fun AchievementRowContent(
    achievement: AchievementProgress,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (achievement.isUnlocked) Icons.Outlined.CheckCircle else Icons.Outlined.TrackChanges,
            contentDescription = null,
            tint = if (achievement.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(if (compact) 24.dp else 30.dp)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(achievement.title, fontWeight = FontWeight.SemiBold)
                Text(
                    if (achievement.isUnlocked) "Açıldı" else achievement.progressLabel,
                    color = if (achievement.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!compact || !achievement.isUnlocked) {
                Text(achievement.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LinearProgressIndicator(
                progress = { achievement.current.toFloat() / achievement.target.coerceAtLeast(1).toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WeekdaySelectorRow(
    days: List<WorkoutDay>,
    selectedDayId: String,
    onSelectDay: (String) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        days.sortedBy { it.dayOfWeek.value }.forEach { day ->
            if (day.id == selectedDayId) {
                Button(onClick = { onSelectDay(day.id) }) {
                    Text(day.dayLabel)
                }
            } else {
                OutlinedButton(onClick = { onSelectDay(day.id) }) {
                    Text(day.dayLabel)
                }
            }
        }
    }
}

private fun editorGroupLabel(exercise: Exercise): String {
    val area = exercise.targetArea.lowercase()
    return when {
        "göğüs" in area -> "Göğüs"
        "sırt" in area || "lat" in area -> "Sırt"
        "omuz" in area -> "Omuz"
        "arka kol" in area || "triceps" in area -> "Arka Kol"
        "ön üst kol" in area || "ön kol" in area || "biseps" in area || "brachialis" in area -> "Ön Kol ve Biseps"
        "quadriceps" in area || "baldır" in area || "bacak" in area -> "Bacak"
        "hamstring" in area || "kalça" in area -> "Arka Bacak ve Kalça"
        "karın" in area || "core" in area -> "Karın ve Core"
        else -> "Genel Destek"
    }
}

private fun editorGroupOrder(): List<String> = listOf(
    "Göğüs",
    "Sırt",
    "Omuz",
    "Arka Kol",
    "Ön Kol ve Biseps",
    "Bacak",
    "Arka Bacak ve Kalça",
    "Karın ve Core",
    "Genel Destek"
)

@Composable
private fun WorkoutTimerSection(
    day: WorkoutDay,
    timerSnapshot: WorkoutTimerSnapshot,
    elapsedMs: Long,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit
) {
    val hasTimerSession = timerSnapshot.currentDayId != null
    val isCurrentDayTimer = timerSnapshot.belongsTo(day.id)
    Text("Antrenman sayacı", fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(4.dp))
    if (hasTimerSession && !isCurrentDayTimer) {
        Text(
            "Şu an ${timerSnapshot.currentDayTitle} için çalışan bir sayaç var. Yeni sayaç başlatırsan önce onu sıfırlaman gerekir.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onReset) {
            Text("Aktif sayacı sıfırla")
        }
    } else {
        Text(formatElapsedDuration(elapsedMs), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            if (timerSnapshot.isRunning && isCurrentDayTimer) {
                "Bildirim alanında görünür ve ekran kapalıyken çalışmayı sürdürür."
            } else {
                "Sayaç yalnızca çalışırken düşük öncelikli bildirim kullanır."
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { if (timerSnapshot.isRunning && isCurrentDayTimer) onPause() else onStart() },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (timerSnapshot.isRunning && isCurrentDayTimer) "Durdur" else "Başlat")
            }
            OutlinedButton(
                onClick = onReset,
                enabled = isCurrentDayTimer && elapsedMs > 0L,
                modifier = Modifier.weight(1f)
            ) {
                Text("Sıfırla")
            }
        }
    }
}

@Composable
private fun rememberVisibleElapsedMs(
    timerSnapshot: WorkoutTimerSnapshot,
    dayId: String
): Long {
    var nowElapsed by remember(timerSnapshot.currentDayId, timerSnapshot.startedAtElapsedMs, timerSnapshot.accumulatedElapsedMs, timerSnapshot.isRunning) {
        mutableStateOf(SystemClock.elapsedRealtime())
    }

    LaunchedEffect(timerSnapshot.currentDayId, timerSnapshot.startedAtElapsedMs, timerSnapshot.accumulatedElapsedMs, timerSnapshot.isRunning) {
        nowElapsed = SystemClock.elapsedRealtime()
        while (timerSnapshot.isRunning && timerSnapshot.belongsTo(dayId)) {
            delay(1_000)
            nowElapsed = SystemClock.elapsedRealtime()
        }
    }

    return if (timerSnapshot.belongsTo(dayId)) timerSnapshot.elapsedMs(nowElapsed) else 0L
}

@Composable
private fun ExerciseCard(
    day: WorkoutDay,
    exercise: Exercise,
    state: PersistedState,
    expandedCards: MutableMap<String, Boolean>,
    onSetDone: (Exercise) -> Unit,
    onUndoSet: (Exercise) -> Unit
) {
    val key = "${day.id}_${exercise.id}"
    val expanded = expandedCards[key] == true
    val completed = completedSetCount(day, exercise.id, state)
    val timerSuggestion = exercise.timerSuggestionSeconds
    var timerRemaining by rememberSaveable(key) { mutableIntStateOf(timerSuggestion ?: 0) }
    var timerRunning by rememberSaveable("${key}_timer_running") { mutableStateOf(false) }

    LaunchedEffect(timerRunning, timerRemaining, key) {
        if (!timerRunning || timerRemaining <= 0) return@LaunchedEffect
        delay(1000)
        timerRemaining -= 1
        if (timerRemaining <= 0) timerRunning = false
    }

    SectionCard(exercise.name) {
        Text("${exercise.targetArea} • ${exercise.equipment}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("${exercise.repText} • ${exercise.restSeconds} sn dinlenme", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Koç Notu", fontWeight = FontWeight.SemiBold)
        Text(exercise.quickNote, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(12.dp))
        Text("$completed / ${exercise.setCount} set tamamlandı", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onSetDone(exercise) },
                enabled = completed < exercise.setCount,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.DoneAll, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (completed >= exercise.setCount) "Tamamlandı" else "Set tamamla")
            }
            OutlinedButton(
                onClick = { onUndoSet(exercise) },
                enabled = completed > 0,
                modifier = Modifier.weight(1f)
            ) {
                Text("Set geri al")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(onClick = { expandedCards[key] = !expanded }, modifier = Modifier.weight(1f)) {
                Text(if (expanded) "Detayı gizle" else "Detayı aç")
            }
            if (timerSuggestion != null) {
                OutlinedButton(onClick = {
                    timerRemaining = timerSuggestion
                    timerRunning = false
                }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Timer, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${timerSuggestion} sn")
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        if (timerSuggestion != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Süre modu", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(formatSeconds(timerRemaining), style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (timerRemaining <= 0) timerRemaining = timerSuggestion
                        timerRunning = !timerRunning
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(if (timerRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (timerRunning) "Duraklat" else "Başlat")
                }
                OutlinedButton(
                    onClick = {
                        timerRemaining = timerSuggestion
                        timerRunning = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Başa al")
                }
            }
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(12.dp))
            MediaPlaceholder(state.lowMediaMode, exercise.mediaCaption)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Uygulama Adımları", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            exercise.steps.forEachIndexed { index, step ->
                Text("${index + 1}. $step", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sık Hata", fontWeight = FontWeight.SemiBold)
            Text(exercise.commonMistake, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Profesyonel İpucu", fontWeight = FontWeight.SemiBold)
            Text(exercise.tip, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun GradientHero(
    title: String,
    subtitle: String,
    buttonLabel: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(subtitle, color = Color.White.copy(alpha = 0.9f))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(buttonLabel)
            }
        }
    }
}

@Composable
private fun SummaryRow(state: PersistedState) {
    val progress = profileProgress(state)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        MetricCard("Seri", "${streakCount(state)} gün", Icons.Outlined.Whatshot, Modifier.weight(1f))
        MetricCard("Haftalık", "${weeklyCompletionPercent(state)}%", Icons.Outlined.CheckCircle, Modifier.weight(1f))
        MetricCard("Seviye", "${progress.level}", Icons.Outlined.FitnessCenter, Modifier.weight(1f))
    }
}

@Composable
private fun MetricCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun MeasurementInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        trailingIcon = { Text("cm") },
        modifier = Modifier.fillMaxWidth()
    )
}

private fun decimalInputValue(value: Double?): String {
    if (value == null) return ""
    return if (value == value.toInt().toDouble()) value.toInt().toString() else "%.1f".format(value).replace('.', ',')
}

private fun sanitizeDecimalInput(raw: String): String {
    val filtered = raw.filter { it.isDigit() || it == ',' || it == '.' }.replace('.', ',')
    val commaIndex = filtered.indexOf(',')
    return if (commaIndex == -1) filtered else filtered.substring(0, commaIndex + 1) + filtered.substring(commaIndex + 1).replace(",", "")
}

private fun decimalFromInput(raw: String): Double? =
    raw.trim().replace(',', '.').takeIf { it.isNotEmpty() }?.toDoubleOrNull()

private fun signedMeasurementDisplay(value: Double, unit: String): String {
    val sign = if (value > 0) "+" else ""
    return "$sign${measurementDisplay(value, unit)}"
}

private fun String.toDisplayDate(): String = parseDate(this)?.format(shortDateFormatter) ?: this

private fun LocalDate.formatDateLabel(): String = format(shortDateFormatter)

@Composable
private fun MeasurementSnapshotGrid(measurements: BodyMeasurements) {
    val items = listOf(
        "Göğüs" to measurementDisplay(measurements.chestCm),
        "Bel" to measurementDisplay(measurements.waistCm),
        "Kalça" to measurementDisplay(measurements.hipCm),
        "Kol" to measurementDisplay(measurements.armCm),
        "Bacak" to measurementDisplay(measurements.thighCm),
        "Omuz" to measurementDisplay(measurements.shoulderCm)
    )
    items.chunked(2).forEach { rowItems ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rowItems.forEach { (label, value) ->
                MetricCard(label, value, Icons.Outlined.TrackChanges, Modifier.weight(1f))
            }
            if (rowItems.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun RewardGrid(badges: List<Badge>, onSelectBadge: (Badge) -> Unit) {
    badges.chunked(2).forEachIndexed { index, rowBadges ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rowBadges.forEach { badge ->
                BadgeCard(badge = badge, modifier = Modifier.weight(1f), onClick = { onSelectBadge(badge) })
            }
            if (rowBadges.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        if (index != badges.chunked(2).lastIndex) {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ActivityLogRow(entry: ActivityLogEntry) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(entry.accentLabel, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(entry.title, fontWeight = FontWeight.SemiBold)
                Text(entry.detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TrendChart(points: List<Pair<LocalDate, Double>>, metric: AnalysisMetric) {
    val minValue = points.minOf { it.second }
    val maxValue = points.maxOf { it.second }
    val range = (maxValue - minValue).takeIf { it > 0.0 } ?: 1.0
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val lineColor = MaterialTheme.colorScheme.primary
    val pointColor = MaterialTheme.colorScheme.secondary

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val horizontalPadding = 18.dp.toPx()
                val verticalPadding = 18.dp.toPx()
                val chartWidth = size.width - horizontalPadding * 2
                val chartHeight = size.height - verticalPadding * 2
                val xStep = if (points.size <= 1) 0f else chartWidth / (points.size - 1)
                val path = Path()

                drawLine(
                    color = outlineColor,
                    start = androidx.compose.ui.geometry.Offset(horizontalPadding, size.height - verticalPadding),
                    end = androidx.compose.ui.geometry.Offset(size.width - horizontalPadding, size.height - verticalPadding),
                    strokeWidth = 3f
                )

                points.forEachIndexed { index, (_, value) ->
                    val x = if (points.size == 1) size.width / 2f else horizontalPadding + index * xStep
                    val normalized = ((value - minValue) / range).toFloat()
                    val y = size.height - verticalPadding - normalized * chartHeight
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                )

                points.forEachIndexed { index, (_, value) ->
                    val x = if (points.size == 1) size.width / 2f else horizontalPadding + index * xStep
                    val normalized = ((value - minValue) / range).toFloat()
                    val y = size.height - verticalPadding - normalized * chartHeight
                    drawCircle(
                        color = pointColor,
                        radius = 7f,
                        center = androidx.compose.ui.geometry.Offset(x, y)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(points.first().first.formatDateLabel(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(metric.label, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Text(points.last().first.formatDateLabel(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun InlineIdentityPill(assetPath: String, label: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageLoader = rememberBadgeImageLoader()
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(assetPath).crossfade(false).build(),
                imageLoader = imageLoader,
                contentDescription = label,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp)
            )
            Text(label, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun NoticeOverlay(notice: InAppNotice?, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = notice != null,
        enter = slideInHorizontally(initialOffsetX = { it / 2 }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it / 2 }) + fadeOut(),
        modifier = modifier
    ) {
        val currentNotice = notice ?: return@AnimatedVisibility
        val context = LocalContext.current
        val imageLoader = rememberBadgeImageLoader()
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentNotice.assetPath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(currentNotice.assetPath).crossfade(false).build(),
                        imageLoader = imageLoader,
                        contentDescription = currentNotice.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(6.dp)
                    )
                } else {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(currentNotice.title, fontWeight = FontWeight.Bold)
                    Text(currentNotice.detail, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

private fun buildNoticeForStateChange(oldState: PersistedState, newState: PersistedState): InAppNotice? {
    val oldProgress = profileProgress(oldState)
    val newProgress = profileProgress(newState)
    if (newProgress.level > oldProgress.level) {
        return InAppNotice(
            title = "Tebrikler, seviye atladın",
            detail = "Seviye ${newProgress.level} • ${newProgress.levelTitle}",
            assetPath = "file:///android_asset/seviye/${newProgress.levelAssetIndex}.png"
        )
    }
    if (newProgress.rankName != oldProgress.rankName) {
        return InAppNotice(
            title = "Yeni rütbe açıldı",
            detail = newProgress.rankName,
            assetPath = "file:///android_asset/rutbe/${newProgress.rankAssetIndex}.svg"
        )
    }
    val oldBadgeIds = unlockedBadges(oldState).map { it.id }.toSet()
    val newBadge = unlockedBadges(newState).firstOrNull { it.id !in oldBadgeIds }
    if (newBadge != null) {
        return InAppNotice(
            title = "Yeni rozet kazandın",
            detail = newBadge.title
        )
    }
    val newAchievementId = newState.unlockedAchievementIds.firstOrNull { it !in oldState.unlockedAchievementIds.toSet() }
    if (newAchievementId != null) {
        val achievement = achievementProgressList(newState).firstOrNull { it.id == newAchievementId }
        if (achievement != null) {
            return InAppNotice(
                title = "Yeni başarım açıldı",
                detail = achievement.title
            )
        }
    }
    return null
}

@Composable
private fun DaySelectorRow(state: PersistedState, selectedDayId: String, onSelectDay: (String) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        workoutPlan(state).forEach { day ->
            val label = scheduledDayLabel(day)
            if (day.id == selectedDayId) {
                Button(onClick = { onSelectDay(day.id) }) {
                    Text(label)
                }
            } else {
                OutlinedButton(onClick = { onSelectDay(day.id) }) {
                    Text(label)
                }
            }
        }
    }
}

@Composable
private fun OptionSelectorRow(
    options: List<Pair<String, () -> Unit>>,
    selectedLabel: String
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { (label, action) ->
            if (label == selectedLabel) {
                Button(onClick = action) { Text(label) }
            } else {
                OutlinedButton(onClick = action) { Text(label) }
            }
        }
    }
}

@Composable
private fun WeightStepper(weight: Double, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = onMinus) { Text("-") }
        Text(weightDisplay(weight), textAlign = TextAlign.Center, modifier = Modifier.width(84.dp))
        OutlinedButton(onClick = onPlus) { Text("+") }
    }
}

@Composable
private fun IntStepper(value: Int, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = onMinus) { Text("-") }
        Text("$value", textAlign = TextAlign.Center, modifier = Modifier.width(84.dp))
        OutlinedButton(onClick = onPlus) { Text("+") }
    }
}

@Composable
private fun MediaPlaceholder(lowMediaMode: Boolean, caption: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.SlowMotionVideo, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(if (lowMediaMode) "Düşük medya modu açık" else "Görsel / video alanı hazır", fontWeight = FontWeight.SemiBold)
            Text(caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun rememberBadgeImageLoader(): ImageLoader {
    val context = LocalContext.current
    return remember {
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .crossfade(false)
            .build()
    }
}

@Composable
private fun CompactIdentityRow(
    title: String,
    assetPath: String,
    primaryLabel: String,
    secondaryLabel: String
) {
    val context = LocalContext.current
    val imageLoader = rememberBadgeImageLoader()
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(assetPath)
                    .crossfade(false)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = primaryLabel,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(6.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(primaryLabel, fontWeight = FontWeight.Bold)
                Text(secondaryLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun BadgeCard(badge: Badge, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(badge.title, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(badge.description, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.weight(1f))
            Text("Dokun ve aç", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun screenPadding(paddingValues: PaddingValues): PaddingValues {
    return PaddingValues(
        start = 20.dp,
        end = 20.dp,
        top = paddingValues.calculateTopPadding() + 12.dp,
        bottom = paddingValues.calculateBottomPadding() + 24.dp
    )
}

private fun formatSeconds(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    val minutePart = safe / 60
    val secondPart = safe % 60
    return "${minutePart.toString().padStart(2, '0')}:${secondPart.toString().padStart(2, '0')}"
}

private fun formatElapsedDuration(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1_000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}



