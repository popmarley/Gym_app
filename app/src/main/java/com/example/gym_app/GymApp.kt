package com.example.gym_app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SCREEN_TODAY = "today"
private const val SCREEN_PLAN = "plan"
private const val SCREEN_WORKOUT = "workout"
private const val SCREEN_LIBRARY = "library"
private const val SCREEN_PROGRESS = "progress"
private const val SCREEN_SETTINGS = "settings"
private const val SCREEN_RELEASE_NOTES = "release_notes"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymFlowApp() {
    val context = LocalContext.current
    val store = remember { GymStore(context) }
    var persistedState by remember { mutableStateOf(store.loadState().normalizeForCurrentWeek()) }
    var currentScreen by rememberSaveable { mutableStateOf(SCREEN_TODAY) }
    var previousMainScreen by rememberSaveable { mutableStateOf(SCREEN_TODAY) }
    var selectedWorkoutId by rememberSaveable { mutableStateOf(recommendedWorkout(persistedState).id) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var timerRemaining by rememberSaveable { mutableIntStateOf(0) }
    var timerRunning by rememberSaveable { mutableStateOf(false) }
    val expandedCards = remember { mutableStateMapOf<String, Boolean>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun persist(update: (PersistedState) -> PersistedState) {
        val newState = update(persistedState).normalizeForCurrentWeek()
        persistedState = newState
        store.saveState(newState)
    }

    fun openMain(screen: String) {
        currentScreen = screen
        previousMainScreen = screen
        if (screen == SCREEN_WORKOUT) selectedWorkoutId = recommendedWorkout(persistedState).id
    }

    fun openWorkout(dayId: String) {
        selectedWorkoutId = dayId
        currentScreen = SCREEN_WORKOUT
    }

    LaunchedEffect(timerRunning, timerRemaining) {
        if (!timerRunning || timerRemaining <= 0) return@LaunchedEffect
        delay(1000)
        timerRemaining -= 1
        if (timerRemaining <= 0) timerRunning = false
    }

    val activeWorkout = workoutById(selectedWorkoutId)
    val normalizedTodayState = persistedState.normalizeForCurrentWeek()
    val todayRecommended = recommendedWorkout(normalizedTodayState)
    val title = screenTitle(currentScreen, activeWorkout.title)
    val showBack = currentScreen == SCREEN_WORKOUT || currentScreen == SCREEN_SETTINGS || currentScreen == SCREEN_RELEASE_NOTES

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = {
                            currentScreen = if (currentScreen == SCREEN_RELEASE_NOTES) SCREEN_SETTINGS else previousMainScreen
                        }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
                        }
                    }
                },
                actions = {
                    if (!showBack) {
                        IconButton(onClick = { currentScreen = SCREEN_SETTINGS }) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Ayarlar")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (currentScreen != SCREEN_SETTINGS && currentScreen != SCREEN_RELEASE_NOTES) {
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
        Surface(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                SCREEN_TODAY -> TodayScreen(paddingValues, normalizedTodayState, todayRecommended, ::openWorkout) { currentScreen = SCREEN_RELEASE_NOTES }
                SCREEN_PLAN -> ProgramScreen(paddingValues, normalizedTodayState, ::openWorkout)
                SCREEN_WORKOUT -> WorkoutScreen(
                    paddingValues, activeWorkout, normalizedTodayState, expandedCards, timerRemaining, timerRunning,
                    onWeightChange = { exerciseId, delta -> persist { it.adjustExerciseWeight(exerciseId, delta) } },
                    onSetDone = { exercise ->
                        persist { it.completeSet(activeWorkout, exercise.id) }
                        if (exercise.restSeconds > 0) {
                            timerRemaining = exercise.restSeconds
                            timerRunning = true
                        }
                    },
                    onResetDay = {
                        persist { it.resetDay(activeWorkout.id) }
                        timerRemaining = 0
                        timerRunning = false
                    },
                    onToggleTimer = { timerRunning = !timerRunning },
                    onRestartTimer = { seconds ->
                        timerRemaining = seconds
                        timerRunning = seconds > 0
                    },
                    onCompleteWorkout = {
                        persist { it.completeWorkout(activeWorkout) }
                        scope.launch { snackbarHostState.showSnackbar("Harika. ${activeWorkout.title} tamamlandi.") }
                    }
                )
                SCREEN_LIBRARY -> LibraryScreen(paddingValues, searchQuery, { searchQuery = it }, expandedCards, normalizedTodayState.lowMediaMode)
                SCREEN_PROGRESS -> ProgressScreen(paddingValues, normalizedTodayState)
                SCREEN_SETTINGS -> SettingsScreen(
                    paddingValues, normalizedTodayState,
                    onAdjustWeight = { delta -> persist { it.adjustCurrentWeight(delta) } },
                    onToggleMediaMode = { persist { it.toggleLowMediaMode() } },
                    onOpenReleaseNotes = { currentScreen = SCREEN_RELEASE_NOTES }
                )
                SCREEN_RELEASE_NOTES -> ReleaseNotesScreen(paddingValues)
            }
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
        else -> "GymFlow"
    }
}

@Composable
private fun TodayScreen(
    paddingValues: PaddingValues,
    state: PersistedState,
    recommended: WorkoutDay,
    onOpenWorkout: (String) -> Unit,
    onOpenReleaseNotes: () -> Unit
) {
    val weeklyPercent = weeklyCompletionPercent(state)
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
        item {
            SectionCard("Hızlı bilgi") {
                Text("Kalori hedefi: 2.300 - 2.400 kcal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Protein hedefi: 100 - 120 g", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Mevcut ağırlık: ${weightDisplay(state.currentWeightKg)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            SectionCard(VERSION_DISPLAY) {
                Text("Proje kayıt dosyası: $DONE_TODO_FILE_NAME", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onOpenReleaseNotes) { Text("Sürüm notlarını ac") }
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
    LazyColumn(
        contentPadding = screenPadding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionCard("Haftalık plan") {
                Text("4 ana gun + 1 opsiyonel gun ile 45-50 dakikalik kas kazanim akisi.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(workoutPlan()) { day ->
            val ratio = dayCompletionRatio(day, state)
            SectionCard("${day.dayLabel} • ${day.title}") {
                Text(day.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(onClick = { }, label = { Text(if (day.isRestDay) "Toparlanma" else "${day.estimatedMinutes} dk • ${day.focus}") })
                if (!day.isRestDay) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${(ratio * 100).toInt()}% gün ilerlemesi", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { onOpenWorkout(day.id) }) { Text("Gunu ac") }
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
    timerRemaining: Int,
    timerRunning: Boolean,
    onWeightChange: (String, Double) -> Unit,
    onSetDone: (Exercise) -> Unit,
    onResetDay: () -> Unit,
    onToggleTimer: () -> Unit,
    onRestartTimer: (Int) -> Unit,
    onCompleteWorkout: () -> Unit
) {
    val progress = dayCompletionRatio(day, state)
    LazyColumn(
        contentPadding = screenPadding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionCard("${day.dayLabel} • ${day.focus}") {
                Text(day.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Text("${(progress * 100).toInt()}% tamamlandi", fontWeight = FontWeight.SemiBold)
            }
        }
        item {
            SectionCard("Süre sayacı") {
                Text(formatSeconds(timerRemaining), style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onToggleTimer) {
                        Icon(if (timerRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (timerRunning) "Duraklat" else "Başlat")
                    }
                    OutlinedButton(onClick = { onRestartTimer(60) }) {
                        Icon(Icons.Outlined.Timer, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("60 sn")
                    }
                    OutlinedButton(onClick = { onRestartTimer(0) }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sıfırla")
                    }
                }
            }
        }
        if (day.isRestDay) {
            item {
                SectionCard("Bugünun odağı") {
                    Text(day.recoveryText ?: "Dinlenme ve su tuketimi.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(day.exercises) { exercise ->
                ExerciseCard(day, exercise, state, expandedCards, onWeightChange, onSetDone, onRestartTimer)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onCompleteWorkout, enabled = progress >= 1f, modifier = Modifier.weight(1f)) {
                        Text("Antrenmanı tamamla")
                    }
                    OutlinedButton(onClick = onResetDay, modifier = Modifier.weight(1f)) {
                        Text("Bugünu sıfırla")
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
                    Text(if (expanded) "Detayı gizle" else "Detayı ac")
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
    state: PersistedState
) {
    val badges = unlockedBadges(state)
    LazyColumn(
        contentPadding = screenPadding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { SummaryRow(state) }
        item {
            SectionCard("Analitik özeti") {
                Text("Toplam tamamlanan antrenman: ${totalCompletedWorkouts(state)}")
                Text("Yaklasik hacim: ${estimatedTotalVolume(state)} kg", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Bu hafta tamamlama: ${weeklyCompletionPercent(state)}%", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            SectionCard("Beslenme ve kilo takibi") {
                Text("Mevcut ağırlık: ${weightDisplay(state.currentWeightKg)}")
                Text("Hedef ağırlık: ${weightDisplay(state.targetWeightKg)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Kalori hedefi: 2.300 - 2.400 kcal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Protein hedefi: 100 - 120 g", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            SectionCard("Rozetler") {
                if (badges.isEmpty()) {
                    Text("Rozetler antrenman tamamlandikca acilacak.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        badges.forEach { badge -> BadgeCard(badge) }
                    }
                }
            }
        }
        item {
            SectionCard("Son kayıtlar") {
                if (state.completedWorkouts.isEmpty()) {
                    Text("Henüz tamamlanan antrenman kaydı yok.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    state.completedWorkouts.takeLast(6).reversed().forEach { workout ->
                        Text("${workout.completedOn} • ${workout.workoutTitle} • ${workout.estimatedVolumeKg} kg", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    paddingValues: PaddingValues,
    state: PersistedState,
    onAdjustWeight: (Double) -> Unit,
    onToggleMediaMode: () -> Unit,
    onOpenReleaseNotes: () -> Unit
) {
    LazyColumn(
        contentPadding = screenPadding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionCard("Kişisel profil") {
                Text("26 yas • erkek • kas / kütle artisi hedefi")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Güncel ağırlık: ${weightDisplay(state.currentWeightKg)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                WeightStepper(weight = state.currentWeightKg, onMinus = { onAdjustWeight(-0.5) }, onPlus = { onAdjustWeight(0.5) })
            }
        }
        item {
            SectionCard("Performans tercihi") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Düşük medya modu", fontWeight = FontWeight.SemiBold)
                        Text("Pil tuketimini azaltmak için görsel alanlarini hafif tutar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = state.lowMediaMode, onCheckedChange = { onToggleMediaMode() })
                }
            }
        }
        item {
            SectionCard("Uygulama bilgisi") {
                Text(VERSION_DISPLAY)
                Text("Tam Türkçe arayüz aktif", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Kayit dosyası: $DONE_TODO_FILE_NAME", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onOpenReleaseNotes) { Text("Sürüm notlarını ac") }
            }
        }
    }
}

@Composable
private fun ReleaseNotesScreen(
    paddingValues: PaddingValues
) {
    LazyColumn(
        contentPadding = screenPadding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(releaseSections()) { section ->
            SectionCard(section.title) {
                Text(section.versionLabel, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                section.items.forEach { item ->
                    Text("• $item", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    day: WorkoutDay,
    exercise: Exercise,
    state: PersistedState,
    expandedCards: MutableMap<String, Boolean>,
    onWeightChange: (String, Double) -> Unit,
    onSetDone: (Exercise) -> Unit,
    onRestartTimer: (Int) -> Unit
) {
    val key = "${day.id}_${exercise.id}"
    val expanded = expandedCards[key] == true
    val completed = state.weeklyProgress[day.id]?.completedSets?.get(exercise.id) ?: 0
    val currentWeight = state.exerciseWeights[exercise.id] ?: 0.0
    SectionCard(exercise.name) {
        Text("${exercise.targetArea} • ${exercise.repText} • ${exercise.restSeconds} sn dinlenme", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Text(exercise.quickNote, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(12.dp))
        if (!exercise.isBodyweightOnly) {
            WeightStepper(weight = currentWeight, onMinus = { onWeightChange(exercise.id, -2.5) }, onPlus = { onWeightChange(exercise.id, 2.5) })
            Spacer(modifier = Modifier.height(12.dp))
        }
        Text("$completed / ${exercise.setCount} set tamamlandı", fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { onSetDone(exercise) }, enabled = completed < exercise.setCount) {
                Icon(Icons.Outlined.DoneAll, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (completed >= exercise.setCount) "Tamamlandi" else "Set tamamla")
            }
            OutlinedButton(onClick = { expandedCards[key] = !expanded }) {
                Text(if (expanded) "Detayı gizle" else "Detayı ac")
            }
            if (exercise.timerSuggestionSeconds != null) {
                OutlinedButton(onClick = { onRestartTimer(exercise.timerSuggestionSeconds) }) {
                    Icon(Icons.Outlined.Timer, contentDescription = null)
                }
            }
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(12.dp))
            MediaPlaceholder(state.lowMediaMode, exercise.mediaCaption)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Nasil yapilir", fontWeight = FontWeight.Bold)
            exercise.steps.forEach { step ->
                Text("• $step", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sık hata: ${exercise.commonMistake}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("İpucu: ${exercise.tip}", color = MaterialTheme.colorScheme.primary)
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
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        MetricCard("Streak", "${streakCount(state)} gün", Icons.Outlined.Whatshot, Modifier.weight(1f))
        MetricCard("Haftalık", "${weeklyCompletionPercent(state)}%", Icons.Outlined.CheckCircle, Modifier.weight(1f))
        MetricCard("Toplam", "${totalCompletedWorkouts(state)} antrenman", Icons.Outlined.FitnessCenter, Modifier.weight(1f))
    }
}

@Composable
private fun MetricCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.Bold)
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
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))
            content()
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
            Text(if (lowMediaMode) "Düşük medya modu acik" else "Görsel / video alani hazir", fontWeight = FontWeight.SemiBold)
            Text(caption, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BadgeCard(badge: Badge) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp).width(170.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(badge.title, fontWeight = FontWeight.Bold)
            Text(badge.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
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



