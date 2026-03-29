package com.example.gym_app

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.IsoFields
import java.util.Locale
import kotlin.math.roundToInt

const val VERSION_NAME_LABEL = "1.7"
const val BUILD_LABEL = "300326,0417"
const val VERSION_DISPLAY = "Sürüm $VERSION_NAME_LABEL (Derleme $BUILD_LABEL)"
const val XP_PER_LEVEL = 5_000
const val MAX_PROFILE_LEVEL = 40
const val XP_FOR_LEVEL_40 = 195_000

enum class ProgramStyle(val label: String, val description: String) {
    FULL_BODY("Full Body", "Aynı hareketleri daha sık pratik etmek ve haftalık frekansı yükseltmek için idealdir."),
    BOLGESEL("Bölgesel", "Her gün belirli bir bölgeye odaklanmayı seven kullanıcılar için daha motive edici bir akış sunar.")
}

enum class ProgramLevel(
    val label: String,
    val weekRange: String,
    val readinessText: String,
    val goalText: String
) {
    BASLANGIC("Başlangıç", "1-8. hafta", "Formun dalgalanıyorsa veya düzenli kayıt tutmaya yeni başlıyorsan uygundur.", "Teknik öğrenmek ve hareket standardı kurmak."),
    ORTA("Orta", "9-24. hafta", "Temel hareketleri güvenle yapıyor ve yük takibi tutabiliyorsan uygundur.", "Hacmi artırmak ve zayıf bölgeleri netleştirmek."),
    ILERI("İleri", "25. hafta ve sonrası", "Toparlanmanı yönetebiliyor ve deload mantığını biliyorsan uygundur.", "Uzmanlaşma, dalgalı yükleme ve eksik bölge önceliği.")
}

enum class ProgramSource(val label: String, val description: String) {
    ONERILEN("Önerilen Program", "Hazır program kataloğunu aktif tutar."),
    KISEL("Kişisel Program", "Önerilen planı temel alıp gün ve hareketleri kendine göre düzenlemeni sağlar.")
}

data class Exercise(
    val id: String,
    val name: String,
    val targetArea: String,
    val equipment: String,
    val setCount: Int,
    val repText: String,
    val referenceReps: Int,
    val restSeconds: Int,
    val quickNote: String,
    val steps: List<String>,
    val commonMistake: String,
    val tip: String,
    val isBodyweightOnly: Boolean = false,
    val timerSuggestionSeconds: Int? = null,
    val mediaCaption: String = "Bu karta optimize edilmiş yerel görsel veya kısa video alanı eklenebilir."
)

data class WorkoutDay(
    val id: String,
    val title: String,
    val dayLabel: String,
    val dayOfWeek: DayOfWeek,
    val focus: String,
    val estimatedMinutes: Int,
    val summary: String,
    val exercises: List<Exercise>,
    val isMandatory: Boolean,
    val isRestDay: Boolean = false,
    val recoveryText: String? = null
)

data class DayProgress(
    val completedSets: Map<String, Int> = emptyMap(),
    val isCompleted: Boolean = false,
    val completedOn: String? = null
)

data class CompletedWorkout(
    val dayId: String,
    val workoutTitle: String,
    val completedOn: String,
    val estimatedVolumeKg: Int
)

data class ExperienceEntry(
    val recordedOn: String,
    val amount: Int,
    val reason: String
)

data class AchievementProgress(
    val id: String,
    val title: String,
    val description: String,
    val current: Int,
    val target: Int,
    val progressLabel: String,
    val isUnlocked: Boolean
)

data class BodyMeasurements(
    val chestCm: Double? = null,
    val waistCm: Double? = null,
    val hipCm: Double? = null,
    val armCm: Double? = null,
    val thighCm: Double? = null,
    val shoulderCm: Double? = null
) {
    fun isEmpty(): Boolean = listOf(chestCm, waistCm, hipCm, armCm, thighCm, shoulderCm).all { it == null }
}

data class MeasurementEntry(
    val recordedOn: String,
    val weightKg: Double,
    val measurements: BodyMeasurements
)

data class ActivityLogEntry(
    val id: String,
    val recordedOn: String,
    val title: String,
    val detail: String,
    val accentLabel: String
)

enum class AnalysisMetric(val label: String, val unit: String) {
    WEIGHT("Kilo", "kg"),
    CHEST("Göğüs", "cm"),
    WAIST("Bel", "cm"),
    HIP("Kalça", "cm"),
    ARM("Kol", "cm"),
    THIGH("Bacak", "cm"),
    SHOULDER("Omuz", "cm")
}

data class PersistedState(
    val weekKey: String = currentWeekKey(),
    val weeklyProgress: Map<String, DayProgress> = emptyMap(),
    val exerciseWeights: Map<String, Double> = emptyMap(),
    val completedWorkouts: List<CompletedWorkout> = emptyList(),
    val experienceLog: List<ExperienceEntry> = emptyList(),
    val currentWeightKg: Double = 59.0,
    val targetWeightKg: Double = 70.0,
    val lowMediaMode: Boolean = true,
    val age: Int = 26,
    val genderLabel: String = "Erkek",
    val goalLabel: String = "Kas / kütle artışı",
    val profileCompleted: Boolean = false,
    val selectedProgramStyle: String = ProgramStyle.FULL_BODY.name,
    val selectedProgramLevel: String = ProgramLevel.BASLANGIC.name,
    val activeProgramSource: String = ProgramSource.ONERILEN.name,
    val customProgramDays: List<WorkoutDay> = emptyList(),
    val unlockedAchievementIds: List<String> = emptyList(),
    val currentMeasurements: BodyMeasurements = BodyMeasurements(),
    val measurementHistory: List<MeasurementEntry> = emptyList(),
    val totalExperience: Int = 0,
    val lastOpenedOn: String? = null,
    val lastSavedAt: String? = null
)

data class Badge(
    val id: String,
    val title: String,
    val description: String
)

data class ReleasePage(
    val versionLabel: String,
    val releasedOn: String,
    val items: List<String>
)

data class ProgramPreset(
    val id: String,
    val title: String,
    val style: ProgramStyle,
    val level: ProgramLevel,
    val cadenceLabel: String,
    val summary: String,
    val recommendation: String,
    val guidance: List<String>,
    val nutritionNotes: List<String>,
    val days: List<WorkoutDay>
)

data class ProfileProgress(
    val totalExperience: Int,
    val level: Int,
    val levelTitle: String,
    val nextLevelLabel: String,
    val progressToNextLevel: Float,
    val currentLevelXp: Int,
    val neededXpForNextLevel: Int,
    val rankName: String,
    val rankAssetIndex: Int,
    val levelAssetIndex: Int,
    val serviceMedalCount: Int,
    val serviceMedalLabel: String
)

private data class ExerciseTemplate(
    val id: String,
    val name: String,
    val targetArea: String,
    val equipment: String,
    val quickNote: String,
    val steps: List<String>,
    val commonMistake: String,
    val tip: String,
    val isBodyweightOnly: Boolean = false,
    val timerSuggestionSeconds: Int? = null
)

private val levelTitles = listOf(
    "Acemi 1", "Er 2", "Er 3", "Er 4",
    "Onbaşı 5", "Onbaşı 6", "Onbaşı 7", "Onbaşı 8",
    "Çavuş 9", "Çavuş 10", "Çavuş 11", "Çavuş 12",
    "Üstçavuş 13", "Üstçavuş 14", "Üstçavuş 15", "Üstçavuş 16",
    "Başçavuş 17", "Başçavuş 18", "Başçavuş 19", "Başçavuş 20",
    "Teğmen 21", "Teğmen 22", "Teğmen 23", "Teğmen 24",
    "Yüzbaşı 25", "Yüzbaşı 26", "Yüzbaşı 27", "Yüzbaşı 28",
    "Binbaşı 29", "Binbaşı 30", "Binbaşı 31", "Binbaşı 32",
    "Albay 33", "Albay 34", "Albay 35",
    "Tuğgeneral 36", "Tümgeneral 37", "Korgeneral 38", "Orgeneral 39", "Küresel General 40"
)

private val competitiveRanks = listOf(
    "Gümüş 1", "Gümüş 2", "Gümüş 3", "Gümüş 4", "Seçkin Gümüş", "Usta Seçkin Gümüş",
    "Altın Nova 1", "Altın Nova 2", "Altın Nova 3", "Usta Altın Nova",
    "Usta Muhafız 1", "Usta Muhafız 2", "Seçkin Muhafız", "Seçkin Usta Muhafız",
    "Efsanevi Kartal", "Usta Efsanevi Kartal", "Birinci Sınıf Üstün Usta", "Dünyaca Seçkin"
)

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
val shortDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("tr", "TR"))

fun currentWeekKey(date: LocalDate = LocalDate.now()): String {
    val week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
    val weekYear = date.get(IsoFields.WEEK_BASED_YEAR)
    return "$weekYear-W${week.toString().padStart(2, '0')}"
}

fun startOfWeek(date: LocalDate = LocalDate.now()): LocalDate =
    date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

fun scheduledDateFor(day: WorkoutDay, today: LocalDate = LocalDate.now()): LocalDate =
    startOfWeek(today).plusDays((day.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())

fun scheduledDateLabel(day: WorkoutDay, today: LocalDate = LocalDate.now()): String =
    scheduledDateFor(day, today).format(shortDateFormatter)

fun scheduledDayLabel(day: WorkoutDay, today: LocalDate = LocalDate.now()): String {
    val baseLabel = "${day.dayLabel} • ${scheduledDateLabel(day, today)}"
    return if (day.isRestDay) "$baseLabel • Dinlenme" else baseLabel
}

fun PersistedState.normalizeForCurrentWeek(today: LocalDate = LocalDate.now()): PersistedState {
    val expectedKey = currentWeekKey(today)
    if (weekKey == expectedKey) {
        val hasLegacyKeys = weeklyProgress.keys.any { parseDate(it) == null }
        return if (hasLegacyKeys) copy(weeklyProgress = emptyMap()) else this
    }
    return copy(weekKey = expectedKey, weeklyProgress = emptyMap())
}

fun PersistedState.programStyle(): ProgramStyle =
    runCatching { ProgramStyle.valueOf(selectedProgramStyle) }.getOrDefault(ProgramStyle.FULL_BODY)

fun PersistedState.programLevel(): ProgramLevel =
    runCatching { ProgramLevel.valueOf(selectedProgramLevel) }.getOrDefault(ProgramLevel.BASLANGIC)

fun PersistedState.programSource(): ProgramSource =
    runCatching { ProgramSource.valueOf(activeProgramSource) }.getOrDefault(ProgramSource.ONERILEN)

fun parseDate(value: String?): LocalDate? =
    value?.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it, dateFormatter) }.getOrNull() }

private fun progressKey(day: WorkoutDay, today: LocalDate = LocalDate.now()): String =
    scheduledDateFor(day, today).toString()

private fun dayProgress(state: PersistedState, day: WorkoutDay, today: LocalDate = LocalDate.now()): DayProgress? =
    state.weeklyProgress[progressKey(day, today)]

private fun repReference(repText: String): Int {
    val numbers = Regex("\\d+").findAll(repText).map { it.value.toInt() }.toList()
    return when {
        numbers.isEmpty() -> 1
        numbers.size == 1 -> numbers.first()
        else -> ((numbers.first() + numbers.last()) / 2.0).roundToInt()
    }
}

private fun template(
    id: String,
    name: String,
    targetArea: String,
    equipment: String,
    quickNote: String,
    steps: List<String>,
    commonMistake: String,
    tip: String,
    isBodyweightOnly: Boolean = false,
    timerSuggestionSeconds: Int? = null
): ExerciseTemplate = ExerciseTemplate(id, name, targetArea, equipment, quickNote, steps, commonMistake, tip, isBodyweightOnly, timerSuggestionSeconds)

private val baseExerciseCatalog: Map<String, ExerciseTemplate> = listOf(
    template("goblet_squat", "Goblet Squat", "Quadriceps / kalça / core", "Dambil veya kettlebell", "Dambılı göğüs önünde sabit tutup karın basıncını korursan hareket çok daha güvenli olur.", listOf("Dambılı göğüs önünde dik tut ve karnını sık.", "Kalça ile dizleri birlikte bükerek kontrollü aşağı in.", "Ayağın orta hattından iterek kalk ve üstte dengeni bozma."), "Ağırlığı öne kaçırmak veya alt noktada beli yuvarlamak.", "İnişi iki saniye kontrol etmek hem teknik hem kas hissi için çok değerlidir."),
    template("back_squat", "Back Squat", "Quadriceps / kalça / arka bacak", "Barbell", "Squat'ta her tekrarın başında aynı nefes ve brace düzenini kurmak gerekir.", listOf("Barı trapezlerine yerleştir ve küçük adımlarla kurulum al.", "Kalça ile dizleri birlikte kırarak ağırlığı ayağın orta hattında tut.", "Topuk ve orta ayaktan iterek ayağa kalk."), "Aşağıda gevşeyip bel pozisyonunu kaybetmek.", "Bar yolunun dik kalması, squat kalitesinin en iyi göstergelerinden biridir."),
    template("hack_squat", "Hack Squat", "Quadriceps / kalça", "Hack squat makinesi", "Makine sabitler ama derinlik ve diz takibi hâlâ senin kontrolündedir.", listOf("Sırtını pede yerleştir ve ayaklarını platforma dengeli kur.", "Kontrollü in, kalçanın pedden ayrılmasına izin verme.", "Ayağın tamamıyla iterek yukarı çık."), "Yarım tekrarlarla yük kovalamak.", "Quadriceps vurgusu için ağrısız ve güçlü ayak yerini sabitle."),
    template("leg_press", "Leg Press", "Quadriceps / kalça", "Leg press makinesi", "Bel ve kalça pedde kalırken en güvenli derinliği bulmak hareketin ana hedefidir.", listOf("Sırtını ve kalçanı pede tam yerleştir.", "Dizleri kontrollü bükerek aşağı in.", "Ayağın tamamıyla iterek kalk ve dizleri sertçe kilitleme."), "Kalçayı pedden kaldırmak veya yarım tekrar yapmak.", "İnişi kontrol edip çıkışı güçlü yapmak quadriceps hissini artırır."),
    template("romanian_deadlift", "Romanian Deadlift", "Hamstring / kalça", "Barbell veya dambil", "Amaç yere gitmek değil, hamstring gerilimini koruyarak kalçadan kırılmaktır.", listOf("Dizleri hafif bük ve omuzları rahat bırak.", "Kalçayı geriye göndererek barı bacaklara yakın indir.", "Kalçayı ileri sürerek ayağa dön."), "Barı öne kaçırmak veya belden çekmek.", "Aşağıda esneme değil gerilim aramak bu hareketin ana püf noktasıdır."),
    template("bench_press", "Bench Press", "Göğüs / arka kol / ön omuz", "Bench ve barbell", "Omuzları geriye-aşağı sabitleyip ayak sürüşünü korumak, bench'i hem güçlü hem güvenli yapar.", listOf("Göz hizanı barın altına getirip bench'e yat.", "Barı alt göğüs hattına kontrollü indir.", "Aynı çizgide yukarı it ve dirsekleri tamamen dışarı açma."), "Omuzları öne yuvarlamak veya bileği gereğinden fazla kırmak.", "Göğse indirip geriye-yukarı it düşüncesi bar yolunu düzeltir."),
    template("incline_dumbbell_press", "Incline Dumbbell Press", "Üst göğüs / ön omuz / arka kol", "Ayarlanabilir bench ve dambil", "Bench açısı orta düzeyde kaldığında üst göğüs daha iyi çalışır.", listOf("Bench'i yaklaşık 20-30 derece ayarla.", "Dambilleri omuz çizgisinde başlatıp kontrollü aşağı in.", "Alt noktadaki gerilimi kaybetmeden yukarı it."), "Bench'i fazla dik kurmak veya omuzu öne düşürmek.", "Tempo bozuluyorsa ağırlık büyük ihtimalle fazladır."),
    template("machine_chest_press", "Machine Chest Press", "Göğüs / arka kol", "Göğüs press makinesi", "Makinede amaç hız değil, kas üzerinde sürekli gerilim üretmektir.", listOf("Sele ve tutacak yüksekliğini göğüs hattına ayarla.", "Kontrollü it, omuzların öne kapanmasına izin verme.", "Yavaş geri dön ve gerilimi koru."), "Ağırlığı zıplatmak.", "Sabit tempo ve tam aralık makine press'te çok değerli."),
    template("cable_fly", "Cable Fly", "Göğüs", "Cable crossover", "Bu harekette itmek değil, göğüs kasını içe toplama hissi önemlidir.", listOf("Kabloları omuz hizası civarında ayarla.", "Dirsek açını sabit tutarak kolları göğüs önünde buluştur.", "Aynı yay çizgisinde kontrollü açıl."), "Hareketi press'e çevirmek.", "Ağırlığı değil sıkışmayı kovala."),
    template("lat_pulldown", "Lat Pulldown", "Lat / ön üst kol", "Lat pulldown makinesi", "Barı enseye değil üst göğüs hattına çekmek daha güvenli ve etkilidir.", listOf("Diz pedini ayarla ve göğsünü hafif dikleştir.", "Dirsekleri aşağı sürerek barı göğüs üstüne çek.", "Yukarı dönüşte ağırlığı bırakma."), "Bel salınımıyla tekrar çıkarmak.", "Kollarla değil dirsek yolu ile çekmeyi düşün."),
    template("weighted_pull", "Weighted Pull-Up veya Ağır Pulldown", "Lat / orta sırt / ön üst kol", "Barfiks barı veya pulldown", "Ağır dikey çekişte sallanmayı azaltmak, yükü gerçekten sırt kasına gönderir.", listOf("Omuzları aşağı yerleştirip gövdeyi sıkı tut.", "Dirsekleri aşağı ve hafif geriye sürerek çek.", "Negatif fazı acele etmeden tamamla."), "Alt gövdeyi savurmak.", "Ağır günde tekrar azalsa da çizgi bozulmamalı."),
    template("seated_cable_row", "Seated Cable Row", "Orta sırt / lat / arka omuz", "Cable row", "Çekişi belden değil sırttan başlatmak bu hareketin ana farkıdır.", listOf("Ayaklarını platforma yerleştir ve omuzları kulaklardan uzak tut.", "Tutacağı alt karın hattına çekip kürek kemiklerini kısa süre topla.", "Kontrollü uzayarak tekrar başına dön."), "Öne-arkaya sallanmak.", "Her tekrarda aynı gövde açısını koru."),
    template("chest_supported_row", "Chest-Supported Row", "Orta sırt / lat / arka omuz", "Destekli row bench'i veya makine", "Göğüs desteği bel yorgunluğunu azaltır; bu yüzden burada hilesiz ağır sırt işi çıkarmak kolaydır.", listOf("Göğsünü desteğe yerleştir ve boynu nötr tut.", "Dirsekleri geriye sürerek kürek kemiklerini topla.", "Ağırlığı kontrollü bırak."), "Göğsü destekten ayırıp momentuma kaçmak.", "Çekiş sonunda kısa sıkışma verimi artırır."),
    template("one_arm_dumbbell_row", "One-Arm Dumbbell Row", "Lat / orta sırt / arka omuz", "Bench ve dambil", "Tek kol row'da gövdeyi döndürmemek, gerçekten sırtla çekmeyi sağlar.", listOf("Bir el ve dizini bench'e yerleştir.", "Dambılı kalçaya doğru çek ve dirseği kaburgaya yakın tut.", "Üstte kısa sıkışma yapıp kontrollü in."), "Gövde rotasyonu ile hile yapmak.", "Elinle değil dirseğinle çektiğini düşün."),
    template("overhead_press", "Overhead Press", "Omuz / arka kol / üst gövde stabilitesi", "Barbell veya dambil", "Kalça ve karın sıkılığı bozulursa yük hızlıca bele kaçar.", listOf("Ağırlığı omuz hizasında başlat ve gövdeyi sık.", "Baş üzerinden yukarı it, geçişte başı hafif geri çek.", "Üstte omuzları kulaklara gömme."), "Belden aşırı geriye kaçmak.", "Bar yolunu yüzüne yakın tutmak kuvveti artırır.")
).associateBy { it.id }

private fun plannedExercise(
    templateId: String,
    setCount: Int,
    repText: String,
    restSeconds: Int,
    nameOverride: String? = null,
    quickNoteOverride: String? = null,
    isBodyweightOnlyOverride: Boolean? = null,
    timerSuggestionSeconds: Int? = null
): Exercise {
    val template = exerciseCatalog.getValue(templateId)
    return Exercise(
        id = if (nameOverride == null) template.id else "${template.id}_${nameOverride.lowercase().replace(" ", "_")}",
        name = nameOverride ?: template.name,
        targetArea = template.targetArea,
        equipment = template.equipment,
        setCount = setCount,
        repText = repText,
        referenceReps = repReference(repText),
        restSeconds = restSeconds,
        quickNote = quickNoteOverride ?: template.quickNote,
        steps = template.steps,
        commonMistake = template.commonMistake,
        tip = template.tip,
        isBodyweightOnly = isBodyweightOnlyOverride ?: template.isBodyweightOnly,
        timerSuggestionSeconds = timerSuggestionSeconds ?: template.timerSuggestionSeconds
    )
}

private fun recoveryDay(
    id: String,
    title: String,
    dayLabel: String,
    dayOfWeek: DayOfWeek,
    minutes: Int,
    summary: String,
    recoveryText: String
): WorkoutDay = WorkoutDay(
    id = id,
    title = title,
    dayLabel = dayLabel,
    dayOfWeek = dayOfWeek,
    focus = "Toparlanma",
    estimatedMinutes = minutes,
    summary = summary,
    exercises = emptyList(),
    isMandatory = false,
    isRestDay = true,
    recoveryText = recoveryText
)

private fun optionalDay(
    id: String,
    title: String,
    dayLabel: String,
    dayOfWeek: DayOfWeek,
    focus: String,
    estimatedMinutes: Int,
    summary: String,
    exercises: List<Exercise>
): WorkoutDay = WorkoutDay(id, title, dayLabel, dayOfWeek, focus, estimatedMinutes, summary, exercises, false, false)

private fun baseGuidance(level: ProgramLevel, style: ProgramStyle): List<String> {
    val styleLine = when (style) {
        ProgramStyle.FULL_BODY -> "Full Body seçimi, aynı hareketleri daha sık pratik edip kas frekansını artırmak için kullanıldı."
        ProgramStyle.BOLGESEL -> "Bölgesel split seçimi, belirli kas günlerine odaklanarak motivasyonu yüksek tutmak için kullanıldı."
    }
    return listOf(
        "${level.label} düzey: ${level.readinessText}",
        "Ana hedef: ${level.goalText}",
        styleLine,
        "Her antrenmanda 4-5 dakika genel ısınma, 3-4 dakika mobilite, ilk ana harekette 2-3 rampa seti kullan.",
        "Büyük hareketlerde 90-150 saniye, izolasyonlarda 45-75 saniye dinlen.",
        "Örnek progresyon: 3x8-10 hedefinde 8-8-8 ile başla; tekrarlar 10-10-10 olunca ağırlığı yüzde 2,5-5 artır.",
        "Performans iki hafta üst üste düşerse veya 5-8 hafta dolarsa deload uygula."
    )
}

private fun nutritionNotes(): List<String> = listOf(
    "Her sabah aç karnına tartıl ve 7 günün ortalamasını izle.",
    "14 gün boyunca kilo artmıyorsa günlük alımına yaklaşık 250-300 kcal ekle.",
    "Protein hedefini 95-130 gram aralığında tut ve gün içine yay.",
    "Antrenman öncesi karbonhidrat + protein, sonrası yine protein ağırlıklı öğün planla.",
    "Sıvı kaloriler ve ara öğünler, iştahı düşük kullanıcıda kilo almayı kolaylaştırır."
)

private fun programCatalog(today: LocalDate = LocalDate.now()): List<ProgramPreset> = listOf(
    beginnerFullBodyProgram(today),
    beginnerSplitProgram(),
    intermediateFullBodyProgram(),
    intermediateSplitProgram(),
    advancedFullBodyProgram(),
    advancedSplitProgram()
)

private fun beginnerFullBodyProgram(today: LocalDate): ProgramPreset {
    val useAFirst = today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) % 2 == 0
    val dayAExercises = listOf(
        plannedExercise("goblet_squat", 3, "8-10 tekrar", 90),
        plannedExercise("bench_press", 3, "6-8 tekrar", 120),
        plannedExercise("lat_pulldown", 3, "8-10 tekrar", 90),
        plannedExercise("romanian_deadlift", 2, "8-10 tekrar", 120),
        plannedExercise("rope_pushdown", 2, "10-12 tekrar", 60),
        plannedExercise("wrist_curl", 2, "12-15 tekrar", 45),
        plannedExercise("reverse_wrist_curl", 2, "12-15 tekrar", 45)
    )
    val dayBExercises = listOf(
        plannedExercise("leg_press", 3, "10-12 tekrar", 90),
        plannedExercise("incline_dumbbell_press", 3, "8-10 tekrar", 90),
        plannedExercise("seated_cable_row", 3, "8-10 tekrar", 90),
        plannedExercise("hip_thrust", 2, "10-12 tekrar", 90),
        plannedExercise("dumbbell_curl", 2, "10-12 tekrar", 60),
        plannedExercise("plank", 2, "30-45 saniye", 45, timerSuggestionSeconds = 45),
        plannedExercise("hanging_knee_raise", 2, "10-15 tekrar", 45)
    )
    val monday = if (useAFirst) WorkoutDay("bfb_mon_a", "Full Body A", "Pazartesi", DayOfWeek.MONDAY, "Squat, yatay itiş, dikey çekiş", 55, "Teknik kalite ve temel kalıp öğrenimi odaklı ilk gün.", dayAExercises, true) else WorkoutDay("bfb_mon_b", "Full Body B", "Pazartesi", DayOfWeek.MONDAY, "Alt vücut denge ve üst gövde denge", 52, "Sırt-göğüs dengesi ve kalça aktivasyonu odaklı ilk gün.", dayBExercises, true)
    val wednesday = if (useAFirst) WorkoutDay("bfb_wed_b", "Full Body B", "Çarşamba", DayOfWeek.WEDNESDAY, "Alt vücut denge ve üst gövde denge", 52, "Hafta ortasında hacmi dengeli tutan ikinci gün.", dayBExercises, true) else WorkoutDay("bfb_wed_a", "Full Body A", "Çarşamba", DayOfWeek.WEDNESDAY, "Squat, yatay itiş, dikey çekiş", 55, "Temel hareketleri tekrar ettiren teknik günü.", dayAExercises, true)
    val friday = if (useAFirst) WorkoutDay("bfb_fri_a", "Full Body A", "Cuma", DayOfWeek.FRIDAY, "Squat, yatay itiş, dikey çekiş", 55, "Haftayı aynı ana kalıplarla kapatan tekrar günü.", dayAExercises, true) else WorkoutDay("bfb_fri_b", "Full Body B", "Cuma", DayOfWeek.FRIDAY, "Alt vücut denge ve üst gövde denge", 52, "A/B dönüşümünün kapanış seansı.", dayBExercises, true)
    return ProgramPreset(
        id = "baslangic_full_body",
        title = "Başlangıç Full Body",
        style = ProgramStyle.FULL_BODY,
        level = ProgramLevel.BASLANGIC,
        cadenceLabel = "Haftada 3 gün • A/B dönüşümlü",
        summary = "Rehbere göre senin profilinde ilk 4 hafta için en pratik başlangıç önerisi budur.",
        recommendation = "Tekniğini hızlı oturtmak ve aynı hareketlerde daha sık pratik yapmak istiyorsan Full Body ile başla.",
        guidance = baseGuidance(ProgramLevel.BASLANGIC, ProgramStyle.FULL_BODY) + listOf(
            "İlk 2 hafta gerçek failure'a gitme; çoğu sette 2-3 tekrar cebinde kalsın.",
            "Her antrenman sonunda bugün en temiz setim hangisiydi sorusuna kısa not düş."
        ),
        nutritionNotes = nutritionNotes(),
        days = listOf(
            monday,
            recoveryDay("bfb_tue_recovery", "Aktif Toparlanma", "Salı", DayOfWeek.TUESDAY, 20, "Yürüyüş ve mobilite günü.", "Bugün 15-20 dakika yürüyüş, hafif omuz-kalça mobilitesi ve iyi uyku yeterli."),
            wednesday,
            recoveryDay("bfb_thu_recovery", "Aktif Toparlanma", "Perşembe", DayOfWeek.THURSDAY, 20, "İkinci toparlanma penceresi.", "Yarınki seans için su, beslenme ve rahat mobiliteye odaklan."),
            friday,
            recoveryDay("bfb_sat_mobility", "Mobilite ve Yürüyüş", "Cumartesi", DayOfWeek.SATURDAY, 20, "Haftayı gevşetme günü.", "Bugün zorlayıcı ağırlık yerine kısa yürüyüş ve esneme yap."),
            recoveryDay("bfb_sun_rest", "Tam Dinlenme", "Pazar", DayOfWeek.SUNDAY, 0, "Tam dinlenme.", "Tam dinlen ve pazartesi için enerjini topla.")
        )
    )
}

private fun beginnerSplitProgram(): ProgramPreset = ProgramPreset(
    id = "baslangic_bolgesel",
    title = "Başlangıç Bölgesel Split",
    style = ProgramStyle.BOLGESEL,
    level = ProgramLevel.BASLANGIC,
    cadenceLabel = "Haftada 4 gün",
    summary = "Göğüs, sırt, bacak ve omuz odaklı sade ama dengeli bir başlangıç split'i.",
    recommendation = "Bugün hangi bölgeyi vuracağım motivasyonu seni daha diri tutuyorsa bölgesel split daha sürdürülebilir olabilir.",
    guidance = baseGuidance(ProgramLevel.BASLANGIC, ProgramStyle.BOLGESEL),
    nutritionNotes = nutritionNotes(),
    days = listOf(
        WorkoutDay("bbs_mon_chest", "Göğüs + Arka Kol", "Pazartesi", DayOfWeek.MONDAY, "Yatay itiş öğrenimi", 55, "Göğüs ve triseps için teknik-hacim dengesi kurar.", listOf(plannedExercise("bench_press", 3, "6-8 tekrar", 120), plannedExercise("incline_dumbbell_press", 3, "8-10 tekrar", 90), plannedExercise("cable_fly", 2, "12-15 tekrar", 60), plannedExercise("rope_pushdown", 3, "10-12 tekrar", 60), plannedExercise("overhead_triceps_extension", 2, "10-12 tekrar", 60)), true),
        WorkoutDay("bbs_tue_back", "Sırt + Ön Üst Kol + Ön Kol", "Salı", DayOfWeek.TUESDAY, "Çekiş ve kavrama", 58, "Sırt kalıbını ve kavrama dayanıklılığını geliştiren çekiş günü.", listOf(plannedExercise("lat_pulldown", 3, "8-10 tekrar", 90), plannedExercise("seated_cable_row", 3, "8-10 tekrar", 90), plannedExercise("one_arm_dumbbell_row", 2, "10 tekrar / taraf", 75), plannedExercise("dumbbell_curl", 3, "10-12 tekrar", 60), plannedExercise("hammer_curl", 2, "10-12 tekrar", 60), plannedExercise("wrist_curl", 2, "12-15 tekrar", 45)), true),
        recoveryDay("bbs_wed_recovery", "Aktif Toparlanma", "Çarşamba", DayOfWeek.WEDNESDAY, 20, "Bacak gününe hazırlanma penceresi.", "Kalça açıcılar, yürüyüş ve iyi uyku ile perşembe gününe hazırlan."),
        WorkoutDay("bbs_thu_legs", "Bacak + Karın", "Perşembe", DayOfWeek.THURSDAY, "Alt gövde tabanı", 58, "Quadriceps, arka bacak ve karın hattını teknik odakla birleştirir.", listOf(plannedExercise("goblet_squat", 3, "8-10 tekrar", 120, nameOverride = "Goblet Squat veya Back Squat"), plannedExercise("romanian_deadlift", 3, "8-10 tekrar", 120), plannedExercise("leg_press", 2, "10-12 tekrar", 90), plannedExercise("calf_raise", 3, "12-15 tekrar", 60), plannedExercise("plank", 2, "30-45 saniye", 45, timerSuggestionSeconds = 45), plannedExercise("cable_crunch", 2, "12-15 tekrar", 45)), true),
        WorkoutDay("bbs_fri_shoulders", "Omuz + Genel Destek", "Cuma", DayOfWeek.FRIDAY, "Denge ve postür", 48, "Omuz dengesi, arka omuz ve postür kalitesi için destekleyici gün.", listOf(plannedExercise("overhead_press", 3, "6-8 tekrar", 90), plannedExercise("lateral_raise", 3, "12-15 tekrar", 60), plannedExercise("face_pull", 3, "12-15 tekrar", 60), plannedExercise("machine_chest_press", 2, "10-12 tekrar", 60, nameOverride = "Incline Push-Up veya Machine Press"), plannedExercise("farmers_carry", 2, "20-30 metre", 60)), true),
        recoveryDay("bbs_sat_reset", "Hafif Gün", "Cumartesi", DayOfWeek.SATURDAY, 15, "Yürüyüş ve gevşeme.", "Bugün yalnızca hafif yürüyüş ve toparlanma yeterli."),
        recoveryDay("bbs_sun_rest", "Tam Dinlenme", "Pazar", DayOfWeek.SUNDAY, 0, "Tam dinlenme.", "Yeni haftaya enerjiyle girmek için tam dinlen.")
    )
)

private fun intermediateFullBodyProgram(): ProgramPreset = ProgramPreset(
    id = "orta_full_body",
    title = "Orta Düzey Full Body",
    style = ProgramStyle.FULL_BODY,
    level = ProgramLevel.ORTA,
    cadenceLabel = "Haftada 4 gün",
    summary = "Her gün tüm vücudu çalıştırır ama vurgu değiştirir; kas başına haftalık işi dengeli yükseltir.",
    recommendation = "9-12. hafta sonrası için rehberde önerilen en doğal yükseltme seçeneklerinden biridir.",
    guidance = baseGuidance(ProgramLevel.ORTA, ProgramStyle.FULL_BODY),
    nutritionNotes = nutritionNotes(),
    days = listOf(
        WorkoutDay("ifb_mon_day1", "Gün 1 - Squat ve Yatay Press", "Pazartesi", DayOfWeek.MONDAY, "Squat ve yatay press", 55, "Haftanın ağır alt vücut ve yatay itiş tabanını kurar.", listOf(plannedExercise("back_squat", 4, "5-7 tekrar", 150, nameOverride = "Back Squat veya Hack Squat"), plannedExercise("bench_press", 4, "6-8 tekrar", 120), plannedExercise("chest_supported_row", 3, "8-10 tekrar", 90), plannedExercise("lateral_raise", 2, "12-15 tekrar", 60), plannedExercise("overhead_triceps_extension", 2, "10-12 tekrar", 60, nameOverride = "Triceps Overhead Extension")), true),
        WorkoutDay("ifb_tue_day2", "Gün 2 - Hinge ve Dikey Çekiş", "Salı", DayOfWeek.TUESDAY, "Hinge ve dikey çekiş", 58, "Arka zincir ve kanat gelişimini öne taşıyan full body günü.", listOf(plannedExercise("romanian_deadlift", 4, "6-8 tekrar", 120), plannedExercise("lat_pulldown", 4, "6-10 tekrar", 90, nameOverride = "Lat Pulldown veya Pull-Up"), plannedExercise("incline_dumbbell_press", 3, "8-10 tekrar", 90), plannedExercise("split_squat", 2, "8-10 tekrar / bacak", 75), plannedExercise("hammer_curl", 2, "10-12 tekrar", 60)), true),
        recoveryDay("ifb_wed_recovery", "Aktif Toparlanma", "Çarşamba", DayOfWeek.WEDNESDAY, 20, "Kas onarımı ve mobilite penceresi.", "Bugün hafif hareket, su ve beslenme kalitesiyle toparlanmayı hızlandır."),
        WorkoutDay("ifb_thu_day3", "Gün 3 - Hacim Günü", "Perşembe", DayOfWeek.THURSDAY, "Üst gövde denge ve hacim", 52, "Makine ve cable hareketleriyle toplam haftalık hacmi artırır.", listOf(plannedExercise("leg_press", 3, "10-12 tekrar", 90), plannedExercise("machine_chest_press", 3, "8-12 tekrar", 90), plannedExercise("seated_cable_row", 3, "10-12 tekrar", 90), plannedExercise("lateral_raise", 2, "12-15 tekrar", 45), plannedExercise("face_pull", 2, "12-15 tekrar", 45), plannedExercise("wrist_curl", 2, "12-15 tekrar", 45), plannedExercise("reverse_curl", 2, "12-15 tekrar", 45, nameOverride = "Reverse Wrist Curl")), true),
        WorkoutDay("ifb_fri_day4", "Gün 4 - Glute-Ham ve Kol", "Cuma", DayOfWeek.FRIDAY, "Glute-ham ve kol", 56, "Kalça, sırt ve kolları ikinci kez uyararak haftayı dengeler.", listOf(plannedExercise("hip_thrust", 3, "8-10 tekrar", 90), plannedExercise("one_arm_dumbbell_row", 3, "8-10 tekrar / taraf", 75), plannedExercise("overhead_press", 3, "6-8 tekrar", 90), plannedExercise("dumbbell_curl", 2, "10-12 tekrar", 60), plannedExercise("rope_pushdown", 2, "10-12 tekrar", 60), plannedExercise("cable_crunch", 2, "12-15 tekrar", 45)), true),
        recoveryDay("ifb_sat_walk", "Yürüyüş ve Esneme", "Cumartesi", DayOfWeek.SATURDAY, 20, "Düşük yoğunluklu toparlanma.", "İstersen 15-20 dakika yürüyüş ve kısa mobilite uygula."),
        recoveryDay("ifb_sun_rest", "Tam Dinlenme", "Pazar", DayOfWeek.SUNDAY, 0, "Tam dinlenme.", "Bugün tam dinlen ve pazartesi squat kalitesine hazırlan.")
    )
)

private fun intermediateSplitProgram(): ProgramPreset = ProgramPreset(
    id = "orta_bolgesel",
    title = "Orta Düzey Bölgesel Split",
    style = ProgramStyle.BOLGESEL,
    level = ProgramLevel.ORTA,
    cadenceLabel = "Haftada 5 gün",
    summary = "Klasik split hissini korurken kas başına haftalık işi ve toplam hacmi artırır.",
    recommendation = "Full Body sonrası motivasyonun bölgesel odakla daha iyi gidiyorsa rehberde doğrudan önerilen geçişlerden biridir.",
    guidance = baseGuidance(ProgramLevel.ORTA, ProgramStyle.BOLGESEL),
    nutritionNotes = nutritionNotes(),
    days = listOf(
        WorkoutDay("ibs_mon_chest", "Göğüs + Arka Kol", "Pazartesi", DayOfWeek.MONDAY, "Ağır itiş", 58, "Haftanın ana göğüs ve triseps kuvvet günü.", listOf(plannedExercise("bench_press", 4, "5-7 tekrar", 120), plannedExercise("incline_dumbbell_press", 3, "8-10 tekrar", 90), plannedExercise("machine_chest_press", 2, "10-12 tekrar", 75), plannedExercise("rope_pushdown", 3, "10-12 tekrar", 60), plannedExercise("overhead_triceps_extension", 2, "12-15 tekrar", 60)), true),
        WorkoutDay("ibs_tue_back", "Sırt + Ön Üst Kol + Ön Kol", "Salı", DayOfWeek.TUESDAY, "Ağır çekiş", 60, "Dikey ve yatay çekişi birlikte öne taşıyan sırt günü.", listOf(plannedExercise("weighted_pull", 4, "6-8 tekrar", 120), plannedExercise("chest_supported_row", 3, "8-10 tekrar", 90), plannedExercise("seated_cable_row", 2, "10-12 tekrar", 75, nameOverride = "Cable Row"), plannedExercise("dumbbell_curl", 3, "8-10 tekrar", 60, nameOverride = "Barbell veya EZ Curl"), plannedExercise("hammer_curl", 2, "10-12 tekrar", 60), plannedExercise("reverse_curl", 2, "15 tekrar", 45, nameOverride = "Reverse Wrist Curl")), true),
        WorkoutDay("ibs_wed_legs", "Bacak", "Çarşamba", DayOfWeek.WEDNESDAY, "Quad baskın", 55, "Quadriceps ve alt vücut kuvvet tabanını büyüten bacak günü.", listOf(plannedExercise("back_squat", 4, "5-7 tekrar", 150, nameOverride = "Back Squat veya Hack Squat"), plannedExercise("leg_press", 3, "10-12 tekrar", 90), plannedExercise("walking_lunge", 2, "10 adım", 75), plannedExercise("calf_raise", 4, "10-15 tekrar", 60)), true),
        WorkoutDay("ibs_thu_shoulders", "Omuz + Karın", "Perşembe", DayOfWeek.THURSDAY, "Deltoid gelişimi", 50, "Omuz hacmi ve core desteğini öne çıkaran gün.", listOf(plannedExercise("overhead_press", 4, "5-7 tekrar", 120), plannedExercise("lateral_raise", 4, "12-15 tekrar", 45), plannedExercise("rear_delt_fly", 3, "12-15 tekrar", 45, nameOverride = "Rear Delt Fly veya Face Pull"), plannedExercise("cable_crunch", 3, "12-15 tekrar", 45), plannedExercise("plank", 2, "30-45 saniye", 45, timerSuggestionSeconds = 45)), true),
        WorkoutDay("ibs_fri_posterior", "Arka Zincir + Eksik Bölge", "Cuma", DayOfWeek.FRIDAY, "Hamstring, kalça ve zayıf alan", 55, "Arka zincir ve geride kalan kas grubuna ek iş ayrılmış gün.", listOf(plannedExercise("romanian_deadlift", 4, "6-8 tekrar", 120), plannedExercise("hip_thrust", 3, "8-10 tekrar", 90), plannedExercise("romanian_deadlift", 3, "10-12 tekrar", 75, nameOverride = "Leg Curl"), plannedExercise("machine_chest_press", 2, "10-12 tekrar", 60, nameOverride = "Seçmeli Eksik Bölge"), plannedExercise("farmers_carry", 2, "20-30 metre", 60)), true),
        recoveryDay("ibs_sat_recovery", "Aktif Toparlanma", "Cumartesi", DayOfWeek.SATURDAY, 20, "Yürüyüş ve mobilite günü.", "Beş günlük split sonrası hafif tempoda dolaşım artırmak yeterlidir."),
        recoveryDay("ibs_sun_rest", "Tam Dinlenme", "Pazar", DayOfWeek.SUNDAY, 0, "Tam dinlenme.", "Yeni haftaya girmeden önce uyku ve beslenmeyi toparla.")
    )
)

private fun advancedFullBodyProgram(): ProgramPreset = ProgramPreset(
    id = "ileri_full_body",
    title = "İleri Düzey Full Body",
    style = ProgramStyle.FULL_BODY,
    level = ProgramLevel.ILERI,
    cadenceLabel = "Haftada 4 gün + 1 opsiyonel pump günü",
    summary = "Aynı hareketleri daha sık ama daha akıllı dağıtan, kuvvet ve hipertrofiyi birlikte taşıyan ileri düzey akış.",
    recommendation = "Hareket kaliten yüksek, toparlanman iyi ve ağır gün ile hacim gününü bilinçli ayırabiliyorsan uygundur.",
    guidance = baseGuidance(ProgramLevel.ILERI, ProgramStyle.FULL_BODY),
    nutritionNotes = nutritionNotes(),
    days = listOf(
        WorkoutDay("afb_mon_day1", "Gün 1 - Alt Vücut Kuvvet + Göğüs", "Pazartesi", DayOfWeek.MONDAY, "Alt vücut kuvvet ve ağır göğüs", 55, "Squat ve bench çizgisinde ağır ama temiz setlerin günü.", listOf(plannedExercise("back_squat", 5, "3-5 tekrar", 150), plannedExercise("bench_press", 4, "4-6 tekrar", 150), plannedExercise("chest_supported_row", 3, "6-8 tekrar", 90), plannedExercise("lateral_raise", 2, "15-20 tekrar", 45)), true),
        WorkoutDay("afb_tue_day2", "Gün 2 - Posterior Chain + Dikey Çekiş", "Salı", DayOfWeek.TUESDAY, "Posterior chain ve ağır dikey çekiş", 58, "Hamstring, lat ve karın kontrolünü ağır setlerle öne taşır.", listOf(plannedExercise("romanian_deadlift", 4, "5-7 tekrar", 120), plannedExercise("weighted_pull", 4, "5-8 tekrar", 120), plannedExercise("incline_dumbbell_press", 3, "8-10 tekrar", 90), plannedExercise("reverse_curl", 2, "12-15 tekrar", 60), plannedExercise("ab_wheel", 2, "8-12 tekrar", 60)), true),
        recoveryDay("afb_wed_recovery", "Aktif Toparlanma", "Çarşamba", DayOfWeek.WEDNESDAY, 20, "Deload mikro penceresi.", "Bugün ek yükleme değil, sinir sistemi ve eklem konforu odakta kalsın."),
        WorkoutDay("afb_thu_day3", "Gün 3 - Hacim / Pump Tam Vücut", "Perşembe", DayOfWeek.THURSDAY, "Hacim ve pump", 52, "Makine ve cable hareketleriyle haftalık hacmi güvenli biçimde yükseltir.", listOf(plannedExercise("hack_squat", 3, "10-12 tekrar", 90, nameOverride = "Hack Squat veya Leg Press"), plannedExercise("machine_chest_press", 3, "10-12 tekrar", 75), plannedExercise("seated_cable_row", 3, "10-12 tekrar", 75), plannedExercise("lateral_raise", 3, "12-15 tekrar", 45), plannedExercise("rope_pushdown", 3, "12-15 tekrar", 45), plannedExercise("dumbbell_curl", 3, "10-12 tekrar", 60, nameOverride = "Curl Varyasyonu")), true),
        WorkoutDay("afb_fri_day4", "Gün 4 - Omuz, Sırt ve Glute", "Cuma", DayOfWeek.FRIDAY, "Omuz ve sırt yoğunluğu", 55, "Omuz kuvveti, lat çizgisi ve kalça yoğunluğunu birleştirir.", listOf(plannedExercise("overhead_press", 4, "5-7 tekrar", 120), plannedExercise("one_arm_dumbbell_row", 3, "8-10 tekrar / taraf", 75), plannedExercise("hip_thrust", 3, "8-10 tekrar", 90), plannedExercise("hammer_curl", 2, "10-12 tekrar", 60), plannedExercise("farmers_carry", 2, "20-30 metre", 60)), true),
        optionalDay("afb_sat_optional", "Opsiyonel Pump Günü", "Cumartesi", DayOfWeek.SATURDAY, "Zayıf bölge ve pump", 35, "İleri düzey kullanıcı için kısa zayıf bölge tamamlama günü.", listOf(plannedExercise("rear_delt_fly", 3, "12-15 tekrar", 45), plannedExercise("lateral_raise", 3, "15-20 tekrar", 45), plannedExercise("rope_pushdown", 3, "12-15 tekrar", 45), plannedExercise("dumbbell_curl", 3, "12-15 tekrar", 45), plannedExercise("farmers_carry", 2, "20-30 metre", 60))),
        recoveryDay("afb_sun_rest", "Tam Dinlenme", "Pazar", DayOfWeek.SUNDAY, 0, "Tam dinlenme.", "İleri programın verimi için bugünü gerçekten dinlenerek kapat.")
    )
)

private fun advancedSplitProgram(): ProgramPreset = ProgramPreset(
    id = "ileri_bolgesel",
    title = "İleri Düzey Bölgesel Split",
    style = ProgramStyle.BOLGESEL,
    level = ProgramLevel.ILERI,
    cadenceLabel = "Haftada 5 gün",
    summary = "Ağır gün ve hacim günü mantığını aynı split içinde kullanan, toparlanmayı yakından izleyen ileri düzey plan.",
    recommendation = "Artık ağır-hacim ayrımı yapabiliyor ve toparlanmanı haftalık bazda yönetebiliyorsan uygundur.",
    guidance = baseGuidance(ProgramLevel.ILERI, ProgramStyle.BOLGESEL),
    nutritionNotes = nutritionNotes(),
    days = listOf(
        WorkoutDay("abs_mon_chest", "Göğüs + Arka Kol (Ağır)", "Pazartesi", DayOfWeek.MONDAY, "Düşük-orta tekrar", 55, "Ağır bench çizgisi ve destekleyici üst göğüs hacmi.", listOf(plannedExercise("bench_press", 5, "3-5 tekrar", 150), plannedExercise("incline_dumbbell_press", 3, "6-8 tekrar", 120, nameOverride = "Incline Press"), plannedExercise("machine_chest_press", 2, "8-10 tekrar", 90, nameOverride = "Dip veya Machine Press"), plannedExercise("rope_pushdown", 3, "10-12 tekrar", 60, nameOverride = "Pushdown")), true),
        WorkoutDay("abs_tue_back", "Sırt + Ön Üst Kol + Ön Kol (Ağır)", "Salı", DayOfWeek.TUESDAY, "Kalınlık ve lat", 58, "Ağır dikey ve yatay çekişle sırt kalınlığını hedefler.", listOf(plannedExercise("weighted_pull", 4, "5-7 tekrar", 120), plannedExercise("chest_supported_row", 4, "6-8 tekrar", 120, nameOverride = "Barbell veya Chest-Supported Row"), plannedExercise("dumbbell_curl", 3, "8-10 tekrar", 60, nameOverride = "EZ Curl"), plannedExercise("hammer_curl", 2, "10-12 tekrar", 60), plannedExercise("reverse_curl", 2, "12-15 tekrar", 60)), true),
        WorkoutDay("abs_wed_legs", "Bacak (Ağır)", "Çarşamba", DayOfWeek.WEDNESDAY, "Kuvvet tabanı", 58, "Ağır squat ve Romanian deadlift ile alt vücut kuvvet tabanı oluşturur.", listOf(plannedExercise("back_squat", 5, "3-5 tekrar", 150), plannedExercise("romanian_deadlift", 4, "6-8 tekrar", 120), plannedExercise("leg_press", 2, "10-12 tekrar", 90), plannedExercise("calf_raise", 3, "12-15 tekrar", 60)), true),
        WorkoutDay("abs_thu_shoulders", "Omuz + Üst Vücut Pump", "Perşembe", DayOfWeek.THURSDAY, "Denge ve hacim", 50, "Omuz hacmi ve üst vücudun eksik kalan alanları için pump odaklı gün.", listOf(plannedExercise("overhead_press", 4, "5-7 tekrar", 120), plannedExercise("lateral_raise", 4, "12-20 tekrar", 45), plannedExercise("rear_delt_fly", 3, "15 tekrar", 45), plannedExercise("machine_chest_press", 2, "12-15 tekrar", 60, nameOverride = "Machine Chest Press veya Cable Row"), plannedExercise("cable_crunch", 3, "12-15 tekrar", 45)), true),
        WorkoutDay("abs_fri_posterior", "Arka Zincir + Kollar", "Cuma", DayOfWeek.FRIDAY, "Uzmanlaşma", 48, "Arka zincir ve kol hacmini kısa ama yoğun biçimde toplar.", listOf(plannedExercise("hip_thrust", 4, "6-8 tekrar", 90), plannedExercise("romanian_deadlift", 3, "10-12 tekrar", 75, nameOverride = "Leg Curl"), plannedExercise("rope_pushdown", 3, "12 tekrar", 45, nameOverride = "Pushdown"), plannedExercise("dumbbell_curl", 3, "12 tekrar", 45, nameOverride = "Curl"), plannedExercise("farmers_carry", 2, "20-30 metre", 60)), true),
        recoveryDay("abs_sat_recovery", "Aktif Toparlanma", "Cumartesi", DayOfWeek.SATURDAY, 20, "Eklem rahatlatma ve yürüyüş.", "İleri düzey split'te bugünü hafif tutmak haftalık toparlanmayı korur."),
        recoveryDay("abs_sun_rest", "Tam Dinlenme", "Pazar", DayOfWeek.SUNDAY, 0, "Tam dinlenme.", "Bir sonraki ağır haftaya kaliteli uyku ve beslenme ile gir.")
    )
)

fun recommendedProgram(state: PersistedState, today: LocalDate = LocalDate.now()): ProgramPreset {
    return programCatalog(today).firstOrNull {
        it.style == state.programStyle() && it.level == state.programLevel()
    } ?: beginnerFullBodyProgram(today)
}

private fun cloneWorkoutDay(day: WorkoutDay): WorkoutDay =
    day.copy(exercises = day.exercises.map { exercise -> exercise.copy(steps = exercise.steps.toList()) })

private fun defaultRecoveryText(dayLabel: String): String =
    "$dayLabel günü için hafif yürüyüş, kısa mobilite ve iyi uyku yeterlidir."

private fun deriveCustomAreas(exercises: List<Exercise>): List<String> =
    exercises
        .flatMap { exercise -> exercise.targetArea.split("/") }
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()

private fun customDayTitle(currentTitle: String, dayLabel: String, areas: List<String>): String =
    when {
        currentTitle.isNotBlank() && currentTitle != "Dinlenme" -> currentTitle
        areas.isNotEmpty() -> areas.take(2).joinToString(" + ")
        else -> "$dayLabel Dinlenme"
    }

private fun customDayFocus(areas: List<String>): String =
    areas.take(2).joinToString(" + ").ifBlank { "Kişisel odak" }

private fun customDayMinutes(exercises: List<Exercise>): Int =
    (exercises.sumOf { it.setCount } * 4).coerceIn(25, 80)

private fun normalizeCustomWorkoutDay(day: WorkoutDay): WorkoutDay {
    val uniqueExercises = day.exercises.distinctBy { it.name }
    if (uniqueExercises.isEmpty()) {
        return WorkoutDay(
            id = day.id,
            title = if (day.title.isBlank()) "Dinlenme" else day.title,
            dayLabel = day.dayLabel,
            dayOfWeek = day.dayOfWeek,
            focus = "Toparlanma",
            estimatedMinutes = 0,
            summary = "Bugün planlı antrenman yok.",
            exercises = emptyList(),
            isMandatory = false,
            isRestDay = true,
            recoveryText = day.recoveryText ?: defaultRecoveryText(day.dayLabel)
        )
    }
    val areas = deriveCustomAreas(uniqueExercises)
    return WorkoutDay(
        id = day.id,
        title = customDayTitle(day.title, day.dayLabel, areas),
        dayLabel = day.dayLabel,
        dayOfWeek = day.dayOfWeek,
        focus = customDayFocus(areas),
        estimatedMinutes = customDayMinutes(uniqueExercises),
        summary = if (areas.isEmpty()) {
            "Kişisel olarak oluşturduğun aktif gün."
        } else {
            "${areas.take(3).joinToString(", ")} odaklarını aynı seansta birleştiren kişisel gün."
        },
        exercises = uniqueExercises,
        isMandatory = true,
        isRestDay = false,
        recoveryText = null
    )
}

private fun defaultCustomProgramDays(state: PersistedState, today: LocalDate = LocalDate.now()): List<WorkoutDay> =
    recommendedProgram(state, today).days.map(::cloneWorkoutDay).map(::normalizeCustomWorkoutDay)

private fun customProgramDays(state: PersistedState, today: LocalDate = LocalDate.now()): List<WorkoutDay> {
    val baseDays = if (state.customProgramDays.isEmpty()) defaultCustomProgramDays(state, today) else state.customProgramDays
    return baseDays.sortedBy { it.dayOfWeek.value }.map(::normalizeCustomWorkoutDay)
}

private fun customProgramPreset(state: PersistedState, today: LocalDate = LocalDate.now()): ProgramPreset {
    val recommended = recommendedProgram(state, today)
    val days = customProgramDays(state, today)
    val activeDayCount = days.count { !it.isRestDay }
    return ProgramPreset(
        id = "kisisel_program",
        title = "Kişisel Program",
        style = state.programStyle(),
        level = state.programLevel(),
        cadenceLabel = when (activeDayCount) {
            0 -> "Dinlenme odaklı hafta"
            1 -> "Haftada 1 gün"
            else -> "Haftada $activeDayCount gün"
        },
        summary = "Önerilen planı temel alıp günleri ve hareketleri kendine göre düzenlediğin kişisel akış.",
        recommendation = "İstersen ayarlardan önerilen plana dönebilir ya da kişisel planını tekrar düzenleyebilirsin.",
        guidance = recommended.guidance,
        nutritionNotes = nutritionNotes(),
        days = days
    )
}

fun customProgramTemplate(state: PersistedState, today: LocalDate = LocalDate.now()): List<WorkoutDay> =
    customProgramDays(state, today)

fun selectedProgram(state: PersistedState, today: LocalDate = LocalDate.now()): ProgramPreset =
    if (state.programSource() == ProgramSource.KISEL) customProgramPreset(state, today) else recommendedProgram(state, today)

fun workoutPlan(state: PersistedState, today: LocalDate = LocalDate.now()): List<WorkoutDay> =
    selectedProgram(state, today).days

fun workoutById(id: String, state: PersistedState, today: LocalDate = LocalDate.now()): WorkoutDay {
    val plan = workoutPlan(state, today)
    return plan.firstOrNull { it.id == id } ?: recommendedWorkout(state, today)
}

fun allExercises(): List<Exercise> =
    programCatalog(LocalDate.of(2026, 3, 2)).flatMap { it.days }.flatMap { it.exercises }.distinctBy { it.name }.sortedBy { it.name }

fun totalSets(day: WorkoutDay): Int = day.exercises.sumOf { it.setCount }

fun completedSets(day: WorkoutDay, state: PersistedState, today: LocalDate = LocalDate.now()): Int {
    val dayProgress = dayProgress(state, day, today) ?: return 0
    return day.exercises.sumOf { exercise ->
        dayProgress.completedSets[exercise.id]?.coerceIn(0, exercise.setCount) ?: 0
    }
}

fun completedSetCount(day: WorkoutDay, exerciseId: String, state: PersistedState, today: LocalDate = LocalDate.now()): Int =
    dayProgress(state, day, today)?.completedSets?.get(exerciseId)?.coerceAtLeast(0) ?: 0

fun dayCompletionRatio(day: WorkoutDay, state: PersistedState, today: LocalDate = LocalDate.now()): Float {
    if (day.exercises.isEmpty()) return 0f
    return completedSets(day, state, today).toFloat() / totalSets(day).toFloat()
}

fun isWorkoutCompleted(state: PersistedState, day: WorkoutDay, today: LocalDate = LocalDate.now()): Boolean =
    dayProgress(state, day, today)?.isCompleted == true

fun weeklyCompletionRatio(state: PersistedState, today: LocalDate = LocalDate.now()): Float {
    val mandatoryDays = workoutPlan(state, today).filter { it.isMandatory && !it.isRestDay }
    if (mandatoryDays.isEmpty()) return 0f
    val completedCount = mandatoryDays.count { isWorkoutCompleted(state, it, today) }
    return completedCount.toFloat() / mandatoryDays.size.toFloat()
}

fun weeklyCompletionPercent(state: PersistedState, today: LocalDate = LocalDate.now()): Int =
    (weeklyCompletionRatio(state, today) * 100).roundToInt()

fun totalCompletedWorkouts(state: PersistedState): Int = state.completedWorkouts.size

fun estimatedTotalVolume(state: PersistedState): Int = state.completedWorkouts.sumOf { it.estimatedVolumeKg }

fun streakCount(state: PersistedState, today: LocalDate = LocalDate.now()): Int {
    val distinctDates = state.completedWorkouts.mapNotNull { parseDate(it.completedOn) }.distinct().toSet()
    if (distinctDates.isEmpty()) return 0
    val startingPoint = when {
        distinctDates.contains(today) -> today
        distinctDates.contains(today.minusDays(1)) -> today.minusDays(1)
        else -> return 0
    }
    var streak = 0
    var cursor = startingPoint
    while (distinctDates.contains(cursor)) {
        streak += 1
        cursor = cursor.minusDays(1)
    }
    return streak
}

private fun experienceRewardForWorkout(day: WorkoutDay, level: ProgramLevel): Int {
    val base = when (level) {
        ProgramLevel.BASLANGIC -> 650
        ProgramLevel.ORTA -> 800
        ProgramLevel.ILERI -> 950
    }
    return if (day.isMandatory) base else (base * 0.5).roundToInt()
}

private fun weeklyCompletionReward(level: ProgramLevel): Int = when (level) {
    ProgramLevel.BASLANGIC -> 250
    ProgramLevel.ORTA -> 350
    ProgramLevel.ILERI -> 450
}

private fun missedMandatoryPenalty(level: ProgramLevel): Int = when (level) {
    ProgramLevel.BASLANGIC -> 90
    ProgramLevel.ORTA -> 110
    ProgramLevel.ILERI -> 130
}

private fun loginReward(): Int = 120

private fun noLoginPenalty(): Int = 45

private fun hasCompletionForDay(state: PersistedState, dayId: String, date: LocalDate): Boolean =
    state.completedWorkouts.any { it.dayId == dayId && parseDate(it.completedOn) == date }

private fun PersistedState.addExperience(amount: Int, reason: String, date: LocalDate): PersistedState {
    if (amount == 0) return this
    val updatedXp = (totalExperience + amount).coerceAtLeast(0)
    val updatedLog = (experienceLog + ExperienceEntry(date.toString(), amount, reason)).takeLast(24)
    return copy(totalExperience = updatedXp, experienceLog = updatedLog)
}

fun PersistedState.applyDailyOpen(today: LocalDate = LocalDate.now()): PersistedState {
    var state = normalizeForCurrentWeek(today)
    val lastOpen = parseDate(lastOpenedOn)
    if (lastOpen == null) {
        state = state.addExperience(loginReward(), "İlk günlük giriş", today)
        return state.copy(lastOpenedOn = today.toString(), lastSavedAt = today.toString())
    }
    if (!today.isAfter(lastOpen)) return state.copy(lastSavedAt = today.toString())

    var missedLoginDays = 0
    var missedMandatoryDays = 0
    var cursor = lastOpen.plusDays(1)
    while (cursor.isBefore(today)) {
        missedLoginDays += 1
        val scheduledDay = workoutPlan(state, cursor).first { it.dayOfWeek == cursor.dayOfWeek }
        if (scheduledDay.isMandatory && !scheduledDay.isRestDay && !hasCompletionForDay(state, scheduledDay.id, cursor)) {
            missedMandatoryDays += 1
        }
        cursor = cursor.plusDays(1)
    }
    if (missedLoginDays > 0) state = state.addExperience(-(missedLoginDays * noLoginPenalty()), "$missedLoginDays gün uygulamaya giriş yapılmadı", today)
    if (missedMandatoryDays > 0) state = state.addExperience(-(missedMandatoryDays * missedMandatoryPenalty(state.programLevel())), "$missedMandatoryDays planlı antrenman günü kaçırıldı", today)
    state = state.addExperience(loginReward(), "Günlük giriş", today)
    return state.copy(lastOpenedOn = today.toString(), lastSavedAt = today.toString()).normalizeForCurrentWeek(today)
}

fun profileProgress(state: PersistedState): ProfileProgress {
    val cappedXp = state.totalExperience.coerceAtMost(XP_FOR_LEVEL_40)
    val level = (cappedXp / XP_PER_LEVEL + 1).coerceAtMost(MAX_PROFILE_LEVEL)
    val floor = ((level - 1) * XP_PER_LEVEL).coerceAtMost(XP_FOR_LEVEL_40)
    val progress = if (level >= MAX_PROFILE_LEVEL) 1f else (cappedXp - floor).toFloat() / XP_PER_LEVEL.toFloat()
    val rankStep = XP_FOR_LEVEL_40.toDouble() / competitiveRanks.size.toDouble()
    val rankIndex = ((cappedXp / rankStep).toInt()).coerceIn(0, competitiveRanks.lastIndex)
    val medalCount = state.totalExperience / XP_FOR_LEVEL_40
    val medalLabel = when {
        medalCount <= 0 -> "Henüz hizmet rozeti yok"
        medalCount == 1 -> "2026 Hizmet Rozeti kazanıldı"
        else -> "2026 Hizmet Rozeti seviye $medalCount"
    }
    return ProfileProgress(
        totalExperience = state.totalExperience,
        level = level,
        levelTitle = levelTitles[level - 1],
        nextLevelLabel = if (level >= MAX_PROFILE_LEVEL) "Maksimum seviye" else levelTitles[level],
        progressToNextLevel = progress,
        currentLevelXp = (cappedXp - floor).coerceAtLeast(0),
        neededXpForNextLevel = if (level >= MAX_PROFILE_LEVEL) 0 else XP_PER_LEVEL,
        rankName = competitiveRanks[rankIndex],
        rankAssetIndex = rankIndex + 1,
        levelAssetIndex = level,
        serviceMedalCount = medalCount,
        serviceMedalLabel = medalLabel
    )
}

private data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val progress: (PersistedState) -> Int
)

private val achievementDefinitions = listOf(
    AchievementDefinition("first_workout", "İlk Seans", "İlk antrenmanını tamamla.", 1) { totalCompletedWorkouts(it) },
    AchievementDefinition("three_workouts", "Ritim Başladı", "Toplam 3 antrenman tamamla.", 3) { totalCompletedWorkouts(it) },
    AchievementDefinition("ten_workouts", "İstikrar Kuruldu", "Toplam 10 antrenman tamamla.", 10) { totalCompletedWorkouts(it) },
    AchievementDefinition("twenty_five_workouts", "Demir Disiplin", "Toplam 25 antrenman tamamla.", 25) { totalCompletedWorkouts(it) },
    AchievementDefinition("fifty_workouts", "Salon Müdavimi", "Toplam 50 antrenman tamamla.", 50) { totalCompletedWorkouts(it) },
    AchievementDefinition("streak_three", "3 Gün Seri", "3 günlük antrenman serisi yakala.", 3) { streakCount(it) },
    AchievementDefinition("streak_seven", "7 Gün Seri", "7 günlük antrenman serisi yakala.", 7) { streakCount(it) },
    AchievementDefinition("week_complete", "Haftayı Kapat", "Bir haftanın tüm zorunlu antrenmanlarını tamamla.", 100) { weeklyCompletionPercent(it) },
    AchievementDefinition("volume_10000", "İlk 10 Ton", "Toplam yaklaşık 10.000 kg hacim üret.", 10_000) { estimatedTotalVolume(it) },
    AchievementDefinition("volume_50000", "Yarım Yüzlük Hacim", "Toplam yaklaşık 50.000 kg hacim üret.", 50_000) { estimatedTotalVolume(it) },
    AchievementDefinition("measurement_two_logs", "Ölçü Takipçisi", "En az 2 ölçüm kaydı gir.", 2) { it.measurementHistory.size },
    AchievementDefinition("measurement_five_logs", "Veri Arşivi", "En az 5 ölçüm kaydı gir.", 5) { it.measurementHistory.size },
    AchievementDefinition("custom_program_live", "Kendi Yolun", "Kişisel program modunu aktif kullan.", 1) { if (it.programSource() == ProgramSource.KISEL) 1 else 0 },
    AchievementDefinition("level_ten", "Seviye 10", "Seviye 10'a ulaş.", 10) { profileProgress(it).level },
    AchievementDefinition("level_twenty", "Seviye 20", "Seviye 20'ye ulaş.", 20) { profileProgress(it).level },
    AchievementDefinition("level_thirty", "Seviye 30", "Seviye 30'a ulaş.", 30) { profileProgress(it).level },
    AchievementDefinition("level_forty", "Seviye 40", "Maksimum seviye olan 40'a ulaş.", 40) { profileProgress(it).level },
    AchievementDefinition("service_medal", "Hizmet Rozeti", "Seviye 40'a ulaşıp rozet kazan.", 1) { profileProgress(it).serviceMedalCount }
)

fun achievementProgressList(state: PersistedState): List<AchievementProgress> =
    achievementDefinitions.map { definition ->
        val current = definition.progress(state)
        val unlocked = definition.id in state.unlockedAchievementIds || current >= definition.target
        val shownCurrent = current.coerceAtMost(definition.target)
        AchievementProgress(
            id = definition.id,
            title = definition.title,
            description = definition.description,
            current = shownCurrent,
            target = definition.target,
            progressLabel = "$shownCurrent / ${definition.target}",
            isUnlocked = unlocked
        )
    }

fun PersistedState.withAchievementUnlocks(): PersistedState {
    val unlockedNow = achievementDefinitions
        .filter { definition -> definition.progress(this) >= definition.target }
        .map { it.id }
    val merged = (unlockedAchievementIds + unlockedNow).distinct()
    return if (merged == unlockedAchievementIds) this else copy(unlockedAchievementIds = merged)
}

fun unlockedBadges(state: PersistedState): List<Badge> {
    val progress = profileProgress(state)
    val badges = mutableListOf<Badge>()
    if (totalCompletedWorkouts(state) >= 1) badges += Badge("ilk_adim", "İlk Adım", "İlk antrenman kaydını oluşturdun.")
    if (totalCompletedWorkouts(state) >= 3) badges += Badge("ritme_giris", "Ritme Giriş", "Üç tamamlanan antrenmanla düzen kurmaya başladın.")
    if (totalCompletedWorkouts(state) >= 10) badges += Badge("duzen_oturdu", "Düzen Oturdu", "On seans barajını geçtin.")
    if (totalCompletedWorkouts(state) >= 25) badges += Badge("demir_sabir", "Demir Sabır", "Yirmi beş antrenmanla kalıcı ritim kurdun.")
    if (weeklyCompletionPercent(state) >= 100) badges += Badge("hafta_tamam", "Hafta Tamam", "Bu haftanın tüm zorunlu antrenmanlarını bitirdin.")
    if (streakCount(state) >= 3) badges += Badge("devam_zinciri", "Devam Zinciri", "En az 3 günlük devam zinciri kurdun.")
    if (streakCount(state) >= 7) badges += Badge("istikrar_serisi", "İstikrar Serisi", "7 günlük zincir yakaladın.")
    if (state.measurementHistory.size >= 2) badges += Badge("olcu_takibi", "Ölçü Takibi", "Ölçü ve kilo takibini düzenli kullanmaya başladın.")
    if (state.measurementHistory.size >= 5) badges += Badge("veri_arsivi", "Veri Arşivi", "Beşten fazla ölçüm kaydı oluşturdun.")
    if (estimatedTotalVolume(state) >= 10_000) badges += Badge("hacim_10", "İlk 10 Ton", "Toplam hacimde 10.000 kg sınırını geçtin.")
    if (estimatedTotalVolume(state) >= 50_000) badges += Badge("hacim_50", "Hacim Ustası", "Toplam hacimde 50.000 kg sınırını geçtin.")
    if (state.programSource() == ProgramSource.KISEL) badges += Badge("kendi_rota", "Kendi Rotan", "Kişisel program düzenini aktif olarak kullanıyorsun.")
    if (progress.level >= 10) badges += Badge("cavus_hatti", "Çavuş Hattı", "Seviye 10 ve üstüne çıkarak düzenli XP akışı kurdun.")
    if (progress.level >= 20) badges += Badge("orta_kademe", "Orta Kademe", "Seviye 20 ile sistemi gerçekten oturttun.")
    if (progress.level >= 30) badges += Badge("ileri_kademe", "İleri Kademe", "Seviye 30 ile uzun soluklu istikrar kurdun.")
    if (progress.level >= 40) badges += Badge("kuresel_general", "Küresel General", "Seviye 40'a ulaşıp maksimum profile seviyesini açtın.")
    if (progress.serviceMedalCount >= 1) badges += Badge("hizmet_rozeti", "Hizmet Rozeti", progress.serviceMedalLabel)
    return badges
}

fun motivationMessage(state: PersistedState, today: LocalDate = LocalDate.now()): String {
    val preset = selectedProgram(state, today)
    return when (weeklyCompletionPercent(state, today)) {
        0 -> "${preset.title} için ritmi bugün başlat. Küçük ama temiz bir seans, boş haftadan her zaman iyidir."
        in 1..49 -> "Hafta hareketlendi. Bugünkü seans seni yeni XP ve rütbe eşiğine yaklaştırır."
        in 50..99 -> "Düzen kuruldu. Kalan seansları tamamladığında haftalık bonus XP de açılacak."
        else -> if (streakCount(state, today) >= 3) "Devam zincirin iyi gidiyor. Şimdi form kalitesini koruyarak ilerle." else "Ana hedefler tamamlandı. Toparlanma ve beslenme ile haftayı güçlü kapat."
    }
}

fun weightDisplay(value: Double): String = if (value == value.toInt().toDouble()) "${value.toInt()} kg" else "${"%.1f".format(value).replace('.', ',')} kg"

fun measurementDisplay(value: Double?, unit: String = "cm"): String {
    if (value == null) return "Veri yok"
    val safeValue = (value * 10).roundToInt() / 10.0
    val formatted = if (safeValue == safeValue.toInt().toDouble()) safeValue.toInt().toString() else "%.1f".format(safeValue).replace('.', ',')
    return "$formatted $unit"
}

private fun sanitizeMeasurement(value: Double?): Double? {
    val safe = value?.takeIf { it > 0.0 } ?: return null
    return (safe * 10).roundToInt() / 10.0
}

private fun BodyMeasurements.normalized(): BodyMeasurements = BodyMeasurements(
    chestCm = sanitizeMeasurement(chestCm),
    waistCm = sanitizeMeasurement(waistCm),
    hipCm = sanitizeMeasurement(hipCm),
    armCm = sanitizeMeasurement(armCm),
    thighCm = sanitizeMeasurement(thighCm),
    shoulderCm = sanitizeMeasurement(shoulderCm)
)

fun BodyMeasurements.valueFor(metric: AnalysisMetric): Double? = when (metric) {
    AnalysisMetric.WEIGHT -> null
    AnalysisMetric.CHEST -> chestCm
    AnalysisMetric.WAIST -> waistCm
    AnalysisMetric.HIP -> hipCm
    AnalysisMetric.ARM -> armCm
    AnalysisMetric.THIGH -> thighCm
    AnalysisMetric.SHOULDER -> shoulderCm
}

private fun upsertMeasurementEntry(
    history: List<MeasurementEntry>,
    recordedOn: LocalDate,
    weightKg: Double,
    measurements: BodyMeasurements
): List<MeasurementEntry> {
    val entry = MeasurementEntry(recordedOn = recordedOn.toString(), weightKg = weightKg, measurements = measurements.normalized())
    return (history.filterNot { it.recordedOn == entry.recordedOn } + entry).sortedBy { it.recordedOn }
}

fun analysisSeriesFor(state: PersistedState, metric: AnalysisMetric): List<Pair<LocalDate, Double>> =
    state.measurementHistory.mapNotNull { entry ->
        val value = when (metric) {
            AnalysisMetric.WEIGHT -> entry.weightKg
            else -> entry.measurements.valueFor(metric)
        } ?: return@mapNotNull null
        parseDate(entry.recordedOn)?.let { it to value }
    }.sortedBy { it.first }

fun activityLogEntries(state: PersistedState): List<ActivityLogEntry> {
    val activityEntries = mutableListOf<Pair<LocalDate, Pair<Int, ActivityLogEntry>>>()
    state.experienceLog.forEachIndexed { index, entry ->
        val date = parseDate(entry.recordedOn) ?: return@forEachIndexed
        val sign = if (entry.amount >= 0) "+" else ""
        activityEntries += date to (
            (10_000 + index) to ActivityLogEntry(
                id = "xp_${entry.recordedOn}_$index",
                recordedOn = entry.recordedOn,
                title = "Tecrübe hareketi",
                detail = entry.reason,
                accentLabel = "$sign${entry.amount} XP"
            )
        )
    }
    state.completedWorkouts.forEachIndexed { index, workout ->
        val date = parseDate(workout.completedOn) ?: return@forEachIndexed
        activityEntries += date to (
            (20_000 + index) to ActivityLogEntry(
                id = "workout_${workout.completedOn}_${workout.dayId}_$index",
                recordedOn = workout.completedOn,
                title = workout.workoutTitle,
                detail = "Antrenman tamamlandı.",
                accentLabel = "${workout.estimatedVolumeKg} kg"
            )
        )
    }
    state.measurementHistory.forEachIndexed { index, entry ->
        val date = parseDate(entry.recordedOn) ?: return@forEachIndexed
        activityEntries += date to (
            (30_000 + index) to ActivityLogEntry(
                id = "measurement_${entry.recordedOn}_$index",
                recordedOn = entry.recordedOn,
                title = "Ölçüm güncellendi",
                detail = buildString {
                    append("Kilo: ${weightDisplay(entry.weightKg)}")
                    if (!entry.measurements.isEmpty()) {
                        append(" • Bel: ${measurementDisplay(entry.measurements.waistCm)}")
                    }
                },
                accentLabel = "Ölçüm"
            )
        )
    }
    return activityEntries
        .sortedWith(compareByDescending<Pair<LocalDate, Pair<Int, ActivityLogEntry>>> { it.first }.thenByDescending { it.second.first })
        .map { it.second.second }
}

fun profileHeadline(state: PersistedState): String = "${state.age} yaş • ${state.genderLabel.lowercase()} • ${state.goalLabel.lowercase()}"

fun lastSavedDisplay(state: PersistedState): String =
    parseDate(state.lastSavedAt)?.format(dateFormatter) ?: "Henüz kayıt yok"

fun PersistedState.completeProfile(
    age: Int,
    genderLabel: String,
    goalLabel: String,
    currentWeightKg: Double,
    targetWeightKg: Double,
    measurements: BodyMeasurements,
    style: ProgramStyle,
    level: ProgramLevel,
    today: LocalDate = LocalDate.now()
): PersistedState {
    val safeCurrentWeight = (currentWeightKg.coerceAtLeast(40.0) * 10).roundToInt() / 10.0
    val safeTargetWeight = (targetWeightKg.coerceAtLeast(safeCurrentWeight) * 10).roundToInt() / 10.0
    val normalizedMeasurements = measurements.normalized()
    val updatedHistory = upsertMeasurementEntry(measurementHistory, today, safeCurrentWeight, normalizedMeasurements)
    return copy(
        age = age.coerceIn(13, 90),
        genderLabel = genderLabel.ifBlank { "Belirtilmedi" },
        goalLabel = goalLabel.ifBlank { "Genel form" },
        currentWeightKg = safeCurrentWeight,
        targetWeightKg = safeTargetWeight,
        profileCompleted = true,
        selectedProgramStyle = style.name,
        selectedProgramLevel = level.name,
        activeProgramSource = if (programSource() == ProgramSource.KISEL && customProgramDays.isNotEmpty()) ProgramSource.KISEL.name else ProgramSource.ONERILEN.name,
        customProgramDays = customProgramDays,
        currentMeasurements = normalizedMeasurements,
        measurementHistory = updatedHistory,
        weekKey = currentWeekKey(today),
        weeklyProgress = emptyMap(),
        lastSavedAt = today.toString()
    )
}

fun PersistedState.toggleLowMediaMode(): PersistedState = copy(lowMediaMode = !lowMediaMode, lastSavedAt = LocalDate.now().toString())

fun PersistedState.selectProgramStyle(style: ProgramStyle, today: LocalDate = LocalDate.now()): PersistedState {
    if (programStyle() == style) return this
    return copy(selectedProgramStyle = style.name, weekKey = currentWeekKey(today), weeklyProgress = emptyMap(), lastSavedAt = today.toString())
}

fun PersistedState.selectProgramLevel(level: ProgramLevel, today: LocalDate = LocalDate.now()): PersistedState {
    if (programLevel() == level) return this
    return copy(selectedProgramLevel = level.name, weekKey = currentWeekKey(today), weeklyProgress = emptyMap(), lastSavedAt = today.toString())
}

fun PersistedState.enableCustomProgram(today: LocalDate = LocalDate.now()): PersistedState {
    val days = customProgramDays(this, today)
    return copy(
        activeProgramSource = ProgramSource.KISEL.name,
        customProgramDays = days,
        weekKey = currentWeekKey(today),
        weeklyProgress = emptyMap(),
        lastSavedAt = today.toString()
    )
}

fun PersistedState.useRecommendedProgram(today: LocalDate = LocalDate.now()): PersistedState =
    copy(
        activeProgramSource = ProgramSource.ONERILEN.name,
        weekKey = currentWeekKey(today),
        weeklyProgress = emptyMap(),
        lastSavedAt = today.toString()
    )

fun PersistedState.resetCustomProgram(today: LocalDate = LocalDate.now()): PersistedState =
    copy(
        activeProgramSource = ProgramSource.KISEL.name,
        customProgramDays = defaultCustomProgramDays(this, today),
        weekKey = currentWeekKey(today),
        weeklyProgress = emptyMap(),
        lastSavedAt = today.toString()
    )

fun PersistedState.updateCustomDayTitle(dayId: String, title: String, today: LocalDate = LocalDate.now()): PersistedState {
    val updatedDays = customProgramDays(this, today).map { day ->
        if (day.id == dayId) normalizeCustomWorkoutDay(day.copy(title = title)) else day
    }
    return copy(customProgramDays = updatedDays, lastSavedAt = today.toString())
}

fun PersistedState.clearCustomDay(dayId: String, today: LocalDate = LocalDate.now()): PersistedState {
    val updatedDays = customProgramDays(this, today).map { day ->
        if (day.id == dayId) {
            normalizeCustomWorkoutDay(day.copy(title = "Dinlenme", exercises = emptyList(), recoveryText = defaultRecoveryText(day.dayLabel)))
        } else {
            day
        }
    }
    return copy(
        activeProgramSource = ProgramSource.KISEL.name,
        customProgramDays = updatedDays,
        weekKey = currentWeekKey(today),
        weeklyProgress = emptyMap(),
        lastSavedAt = today.toString()
    )
}

fun PersistedState.addExerciseToCustomDay(dayId: String, exercise: Exercise, today: LocalDate = LocalDate.now()): PersistedState {
    val updatedDays = customProgramDays(this, today).map { day ->
        if (day.id == dayId) {
            val baseTitle = if (day.title == "Dinlenme") "" else day.title
            val nextExercises = if (day.exercises.any { it.name == exercise.name }) day.exercises else day.exercises + exercise.copy(steps = exercise.steps.toList())
            normalizeCustomWorkoutDay(day.copy(title = baseTitle, exercises = nextExercises))
        } else {
            day
        }
    }
    return copy(
        activeProgramSource = ProgramSource.KISEL.name,
        customProgramDays = updatedDays,
        weekKey = currentWeekKey(today),
        weeklyProgress = emptyMap(),
        lastSavedAt = today.toString()
    )
}

fun PersistedState.removeExerciseFromCustomDay(dayId: String, exerciseId: String, today: LocalDate = LocalDate.now()): PersistedState {
    val updatedDays = customProgramDays(this, today).map { day ->
        if (day.id == dayId) {
            normalizeCustomWorkoutDay(day.copy(exercises = day.exercises.filterNot { it.id == exerciseId }))
        } else {
            day
        }
    }
    return copy(
        activeProgramSource = ProgramSource.KISEL.name,
        customProgramDays = updatedDays,
        weekKey = currentWeekKey(today),
        weeklyProgress = emptyMap(),
        lastSavedAt = today.toString()
    )
}

fun PersistedState.completeSet(day: WorkoutDay, exerciseId: String, today: LocalDate = LocalDate.now()): PersistedState {
    val exercise = day.exercises.firstOrNull { it.id == exerciseId } ?: return this
    val key = progressKey(day, today)
    val currentDay = weeklyProgress[key] ?: DayProgress()
    val currentCount = currentDay.completedSets[exerciseId] ?: 0
    if (currentCount >= exercise.setCount) return this
    val updatedDay = currentDay.copy(
        completedSets = currentDay.completedSets + (exerciseId to (currentCount + 1)),
        completedOn = currentDay.completedOn ?: scheduledDateFor(day, today).toString()
    )
    val updatedState = copy(weeklyProgress = weeklyProgress + (key to updatedDay), lastSavedAt = today.toString())
    val allSetsCompleted = day.exercises.all { plannedExercise ->
        (updatedDay.completedSets[plannedExercise.id] ?: 0) >= plannedExercise.setCount
    }
    return if (allSetsCompleted) updatedState.completeWorkout(day, today) else updatedState
}

fun PersistedState.undoSet(day: WorkoutDay, exerciseId: String, today: LocalDate = LocalDate.now()): PersistedState {
    val exercise = day.exercises.firstOrNull { it.id == exerciseId } ?: return this
    val key = progressKey(day, today)
    val currentDay = weeklyProgress[key] ?: return this
    val currentCount = currentDay.completedSets[exerciseId] ?: return this
    if (currentCount <= 0) return this

    val updatedSets = currentDay.completedSets.toMutableMap().apply {
        if (currentCount <= 1) remove(exerciseId) else put(exerciseId, (currentCount - 1).coerceAtMost(exercise.setCount))
    }
    val updatedDay = currentDay.copy(
        completedSets = updatedSets,
        isCompleted = false,
        completedOn = if (updatedSets.isEmpty()) null else currentDay.completedOn
    )

    val beforeRatio = weeklyCompletionRatio(this, today)
    var updated = if (updatedSets.isEmpty()) {
        copy(weeklyProgress = weeklyProgress - key, lastSavedAt = today.toString())
    } else {
        copy(weeklyProgress = weeklyProgress + (key to updatedDay), lastSavedAt = today.toString())
    }

    if (currentDay.isCompleted) {
        val completedOn = parseDate(currentDay.completedOn) ?: scheduledDateFor(day, today)
        updated = updated.copy(
            completedWorkouts = completedWorkouts.filterNot { workout ->
                workout.dayId == day.id && workout.completedOn == completedOn.toString()
            }
        ).addExperience(-experienceRewardForWorkout(day, programLevel()), "${day.title} kaydı geri alındı", today)

        if (beforeRatio >= 1f && weeklyCompletionRatio(updated, today) < 1f) {
            updated = updated.addExperience(-weeklyCompletionReward(programLevel()), "${selectedProgram(updated, today).title} hafta bonusu geri alındı", today)
        }
    }

    return updated
}

fun PersistedState.resetDay(day: WorkoutDay, today: LocalDate = LocalDate.now()): PersistedState {
    val key = progressKey(day, today)
    val currentDay = weeklyProgress[key] ?: return this
    val beforeRatio = weeklyCompletionRatio(this, today)
    var updated = copy(weeklyProgress = weeklyProgress - key, lastSavedAt = today.toString())

    if (currentDay.isCompleted) {
        val completedOn = parseDate(currentDay.completedOn) ?: scheduledDateFor(day, today)
        updated = updated.copy(
            completedWorkouts = completedWorkouts.filterNot { workout ->
                workout.dayId == day.id && workout.completedOn == completedOn.toString()
            }
        ).addExperience(-experienceRewardForWorkout(day, programLevel()), "${day.title} sıfırlandı", today)

        if (beforeRatio >= 1f && weeklyCompletionRatio(updated, today) < 1f) {
            updated = updated.addExperience(-weeklyCompletionReward(programLevel()), "${selectedProgram(updated, today).title} hafta bonusu geri alındı", today)
        }
    }

    return updated
}

fun estimatedVolumeForDay(day: WorkoutDay, state: PersistedState, today: LocalDate = LocalDate.now()): Int {
    val dayProgress = dayProgress(state, day, today) ?: return 0
    return day.exercises.sumOf { exercise ->
        val setsDone = dayProgress.completedSets[exercise.id] ?: 0
        val weight = state.exerciseWeights[exercise.id] ?: 0.0
        (setsDone * exercise.referenceReps * weight).roundToInt()
    }
}

fun PersistedState.completeWorkout(day: WorkoutDay, completedOn: LocalDate = LocalDate.now()): PersistedState {
    val key = progressKey(day, completedOn)
    val existing = weeklyProgress[key] ?: DayProgress()
    if (existing.isCompleted) return this
    val finalSets = day.exercises.associate { exercise ->
        val current = existing.completedSets[exercise.id] ?: 0
        exercise.id to current.coerceAtLeast(exercise.setCount)
    }
    val updatedDay = existing.copy(completedSets = finalSets, isCompleted = true, completedOn = completedOn.toString())
    val updatedHistory = completedWorkouts + CompletedWorkout(day.id, day.title, completedOn.toString(), day.exercises.sumOf { exercise -> ((exerciseWeights[exercise.id] ?: 0.0) * exercise.referenceReps * exercise.setCount).roundToInt() })
    val beforeRatio = weeklyCompletionRatio(this, completedOn)
    var updated = copy(weeklyProgress = weeklyProgress + (key to updatedDay), completedWorkouts = updatedHistory, lastSavedAt = completedOn.toString())
        .addExperience(experienceRewardForWorkout(day, programLevel()), "${day.title} tamamlandı", completedOn)
    if (beforeRatio < 1f && weeklyCompletionRatio(updated, completedOn) >= 1f) {
        updated = updated.addExperience(weeklyCompletionReward(programLevel()), "${selectedProgram(updated, completedOn).title} haftası tamamlandı", completedOn)
    }
    return updated
}

fun recommendedWorkout(state: PersistedState, today: LocalDate = LocalDate.now()): WorkoutDay {
    val plan = workoutPlan(state, today)
    val todaysWorkout = plan.first { it.dayOfWeek == today.dayOfWeek }
    if (!todaysWorkout.isRestDay && todaysWorkout.isMandatory && !isWorkoutCompleted(state, todaysWorkout, today)) return todaysWorkout
    return plan.firstOrNull { it.isMandatory && !it.isRestDay && !isWorkoutCompleted(state, it, today) }
        ?: plan.firstOrNull { !it.isRestDay } ?: plan.first()
}

fun releasePages(): List<ReleasePage> = listOf(
    ReleasePage(
        versionLabel = VERSION_DISPLAY,
        releasedOn = "30 Mart 2026",
        items = listOf(
            "Ölçüm geçmişi eklendi; göğüs, bel, kalça, kol, bacak ve omuz verileri tarih bazlı saklanıp analiz ekranında grafikle gösterilir hale geldi.",
            "İlerleme ekranı daha görsel bir yapıya taşındı; sabit boyutlu rozet kartları, ölçüm grafiği ve sayfalı etkinlik günlüğü eklendi.",
            "Profil düzenleme ekranına görünür geri dönüş ve detaylı ölçü alanları eklendi; ayarlardan dönünce kullanıcı son olduğu ekrana geri götürülüyor.",
            "Antrenman ekranında tüm setler tamamlanınca seans otomatik tamamlanmış sayılıyor ve yeni seviye, rozet veya başarım için sağ üstte kayan bildirim gösteriliyor.",
            "Bugün ekranında aktif program kartına küçük seviye ve rütbe görselleri eklendi; ayarlar ekranındaki gereksiz kilo değiştirme adımı kaldırıldı."
        )
    ),
    ReleasePage(
        versionLabel = "Sürüm 1.6",
        releasedOn = "30 Mart 2026",
        items = listOf(
            "Başarımlar ekranı eklendi; açılmayan başarımlar Steam tarzı ilerleme mantığıyla görüntülenebilir hale geldi.",
            "Ayarlar butonu tüm ana ekranlar ve detay ekranlarında erişilebilir hale getirildi.",
            "Sürüm notları tek liste yerine sürüm sürüm sayfalı yapıya taşındı ve tarih bilgisi eklendi.",
            "Seviye ve rütbe alanı büyük kartlar yerine küçük görsel + tek satır metin olacak şekilde sadeleştirildi.",
            "Kişisel program düzenleme ekranı haftanın günleri bazında tekrar eden yapıya göre sadeleştirildi ve hareketler bölgelere ayrıldı.",
            "Antrenman ekranına başlat, durdur ve sıfırla işlevli genel antrenman sayacı eklendi; sayaç arka planda düşük öncelikli bildirimle çalışabiliyor."
        )
    ),
    ReleasePage(
        versionLabel = "Sürüm 1.5",
        releasedOn = "30 Mart 2026",
        items = listOf(
            "Kişisel program düzenleyici eklendi; kullanıcı önerilen planı varsayılan tutup isterse gün ve hareketleri kendine göre değiştirebilir hale geldi.",
            "Program ekranında her hareketin yanında baskın çalıştığı bölge görünür hale getirildi.",
            "İlerleme ekranına yerel CS2 seviye ve rütbe görselleri bağlandı; rütbe adları Türkçeleştirildi."
        )
    ),
    ReleasePage(
        versionLabel = "Sürüm 1.4",
        releasedOn = "29 Mart 2026",
        items = listOf(
            "Set tamamlama kartına set geri alma işlevi eklendi; yanlış dokunuşlar tek tek geri alınabilir hale getirildi.",
            "Gün seçimi tarih bazlı etiketlerle yenilendi; her hafta günler yeni tarihleriyle temiz başlangıç mantığında gösterilir hale getirildi.",
            "İlk kurulumda kişisel profil ayarlama akışı eklendi; yaş, kilo, hedef ve başlangıç programı kullanıcı tarafından seçilebilir oldu.",
            "Telefonun geri tuşu davranışı düzenlendi; iç ekranlardan uygulamanın ana menüsüne dönüş akışı kuruldu.",
            "Genel süre sayacı kaldırıldı; süre takibi yalnızca plank gibi gerçekten ihtiyaç duyan hareket kartlarının içine taşındı."
        )
    ),
    ReleasePage(
        versionLabel = "Sürüm 1.3",
        releasedOn = "29 Mart 2026",
        items = listOf(
            "Kişiye özel rehber baz alınarak başlangıç, orta ve ileri düzey için full body ile bölgesel program kataloğu eklendi.",
            "Ayarlar ekranına aktif program seçimi eklendi; seçime göre tüm haftalık plan otomatik değişir hale getirildi.",
            "Android otomatik yedekleme kuralları açıldı; aynı Google hesabında uygulama verisinin geri yüklenebilmesi için shared preferences yedeğe dahil edildi.",
            "CS2 esinli 40 seviye, 18 rütbe, günlük giriş XP'si ve seviye 40 hizmet rozeti sistemi eklendi.",
            "Bugün ekranındaki gereksiz sürüm kartı kaldırıldı; sürüm bilgisi yalnızca ayarlar altında bırakıldı."
        )
    ),
    ReleasePage(
        versionLabel = "Sürüm 1.2",
        releasedOn = "29 Mart 2026",
        items = listOf(
            "Uygulama ikonu özel görselle güncellendi.",
            "Hareket anlatımları daha profesyonel ve detaylı hale getirildi.",
            "Kas kazanımı odaklı önerilen programın ana bileşik hareket dengesi revize edildi."
        )
    ),
    ReleasePage(
        versionLabel = "Sürüm 1.1",
        releasedOn = "29 Mart 2026",
        items = listOf(
            "Gün seçimi işlevi eklendi.",
            "Arayüz metinleri Türkçeleştirildi ve bozuk ifadeler temizlendi.",
            "Dinlenme sayacı ve set tamamlama akışı geliştirildi.",
            "Düşük medya modu ile pil dostu kullanım güçlendirildi."
        )
    ),
    ReleasePage(
        versionLabel = "Sürüm 1.0",
        releasedOn = "26 Mart 2026",
        items = listOf(
            "Temel antrenman akışı, ilerleme kartları ve antrenman tamamlama mantığı kuruldu.",
            "İlk program ekranları, hareket kütüphanesi ve sürüm kayıt standardı oluşturuldu."
        )
    )
)

private val exerciseCatalog: Map<String, ExerciseTemplate> = baseExerciseCatalog + listOf(
    template("lateral_raise", "Lateral Raise", "Yan omuz", "Dambil veya cable", "Yan omuz için ağır savuruş değil, temiz tekrar ve negatif kontrol gerekir.", listOf("Dirsekleri hafif yumuşak tutarak başla.", "Kolları yana ve hafif öne doğru kaldır.", "Ağırlığı bırakmadan kontrollü in."), "Beli savurarak tekrar çıkarmak.", "Hafif ağırlıkla yüksek kalite tekrar çoğu zaman daha etkilidir."),
    template("face_pull", "Face Pull", "Arka omuz / üst sırt", "Cable ve ip aparatı", "Postür ve arka omuz dengesi için en temiz hareketlerden biridir.", listOf("İpi yüz hizasında ayarla.", "Dirsekleri dışa açıp ipi yüzünün iki yanına çek.", "Kontrollü biçimde başlangıca dön."), "İpi göğse çekmek.", "Dirseklerin dışarı, ellerin yüze ayrılıyor hissi arka omuz vurgusunu artırır."),
    template("rear_delt_fly", "Rear Delt Fly", "Arka omuz", "Makine, cable veya dambil", "Arka omuz hareketinde ağırlığı büyütmektense çizgiyi korumak daha değerlidir.", listOf("Gövdeyi sabit tut ve kolları hafif bükülü başlat.", "Kolları dışa ve geriye açarak arka omuzu sık.", "Negatifte kontrollü dön."), "Trapeze kaçmak için omuzları yükseltmek.", "Daha hafif yük ve uzun negatif arka omuzda çok iyi çalışır."),
    template("rope_pushdown", "Rope Pushdown", "Arka kol", "Cable ve ip aparatı", "Dirseklerin sabit kalması, arka kolu gerçekten izole etmenin temel şartıdır.", listOf("Dirsekleri gövdenin yanında sabitle.", "İpi aşağı iterken altta hafif ayır ve arka kolu sık.", "Kontrollü şekilde yukarı dön."), "Gövde salınımıyla ağırlığı itmek.", "Alt noktadaki kısa sıkışma arka kol hissini ciddi biçimde artırır."),
    template("overhead_triceps_extension", "Overhead Triceps Extension", "Arka kol uzun baş", "Cable veya tek dambil", "Uzun başı çalıştırmak için gerilim altındaki alt noktayı korumak gerekir.", listOf("Dirsekleri baş yanında başlat.", "Üst kolları sabit tutup ön kollarla aç-kapat yap.", "Alt noktada gerilimi hisset ama omuzu zorlama."), "Dirsekleri tamamen yana açmak.", "Dirsekler kaçıyorsa ağırlık fazla olabilir."),
    template("dumbbell_curl", "Dumbbell Curl", "Ön üst kol / ön kol fleksörleri", "Dambil", "Biseps için özellikle iniş kısmını kontrol etmek, yukarı kaldırmaktan daha büyük fark yaratır.", listOf("Dirsekleri gövdeye yakın tut.", "Dambılı kaldırırken avuç içini yukarı çevir.", "Yavaşça aşağı dön."), "Dirseği öne sürmek veya kalçadan hız almak.", "Kısa sıkışma ve yavaş negatif biceps verimini artırır."),
    template("hammer_curl", "Hammer Curl", "Brachialis / ön kol / biseps", "Dambil", "Hammer curl hem kol kalınlığı hem kavrama gücü için çok iyi bir destektir.", listOf("Avuç içleri birbirine bakacak şekilde başlat.", "Dirsekleri sabit tutarak dambılı kaldır.", "Aşağıda kontrollü açıl."), "Dambılı çapraz savurmak.", "Nötr bilek ve yavaş negatif ön kol katkısını artırır."),
    template("reverse_curl", "Reverse Curl", "Ön kol ekstansörleri / brachioradialis", "EZ bar veya dambil", "Ters tutuş bilek ve ön kol dengesini güçlendirir; burada kontrol ağırlıktan önemlidir.", listOf("Ters tutuş al ve dirsekleri gövdeye yakın sabitle.", "Ön kolu kaldırırken bileği nötr tut.", "Yavaşça aşağı in."), "Bileği kırmak veya kalça ile momentuma kaçmak.", "Daha düşük ağırlıkla temiz eksantrik daha verimlidir."),
    template("wrist_curl", "Wrist Curl", "Ön kol fleksörleri", "Dambil veya kısa bar", "Bileği tam aralıkta çalıştırmak hareketin asıl verimini oluşturur.", listOf("Ön kolu bench'e veya uyluğa sabitle.", "Bileği tam aralıkta bük.", "Kontrollü geri dön."), "Tüm kolu hareket ettirmek.", "Yüksek tekrar ve tam aralık ön kol için çoğu zaman en konforlu çözümdür."),
    template("hip_thrust", "Hip Thrust", "Kalça / hamstring yardımcı", "Bench ve barbell", "Amaç bel köprüsü kurmak değil, kalçayı güçlü ve kontrollü uzatmaktır.", listOf("Sırt üstünü bench'e yerleştir ve ayaklarını dengeli kur.", "Kaburgaları aşağıda tutarak kalçayı yukarı it.", "Üstte glute sıkışmasını kısa süre koru."), "Belden köprü yapmak.", "Çeneyi hafif içerde tutmak beli korumaya yardım eder."),
    template("split_squat", "Split Squat", "Quadriceps / kalça", "Dambil veya vücut ağırlığı", "Tek taraflı bacak hareketlerinde diz ve kalça çizgisi korunursa denge çok daha iyi olur.", listOf("Dengeli bir adım mesafesi kur.", "Gövdeyi dik tutup kontrollü biçimde aşağı in.", "Ön ayağın orta hattından iterek kalk."), "Ön dizi içe düşürmek.", "Daha kısa ama sabit adım mesafesi hareketi çoğu kullanıcı için temizler."),
    template("walking_lunge", "Walking Lunge", "Quadriceps / kalça / denge", "Dambil veya vücut ağırlığı", "Lunge'da her adımı kontrollü yerleştirmek, yükten çok daha önemlidir.", listOf("Uzun ama dengeli bir adım at.", "Arka dizi yere yaklaşacak kadar kontrollü in.", "Ön ayağın tamamıyla iterek diğer adıma geç."), "Gövdeyi öne kapatmak.", "Yüksek kalite lunge için ilk hedef ritimdir."),
    template("calf_raise", "Calf Raise", "Baldır", "Makine, smith veya step", "Alt ve üst noktadaki kısa duraklamalar verimi dramatik biçimde yükseltir.", listOf("Gövdeyi dik tut ve topukları kontrollü aşağı bırak.", "Ayak ucuna yüksel ve üstte kısa sıkışma yap.", "Kontrollü biçimde tekrar aşağı in."), "Yarım aralıkta zıplayarak tekrar yapmak.", "Tempo ve duraklama baldır için çoğu zaman ekstra ağırlıktan daha değerlidir."),
    template("plank", "Plank", "Core / karın / anti-ekstansiyon", "Vücut ağırlığı", "Plank'te süreyi değil pozisyonu kovalamak gerekir.", listOf("Dirsekleri omuz altında kur ve vücudu tek çizgi yap.", "Kaburgaları aşağı çekip kalçayı hafif sık.", "Nefesi tutmadan pozisyonu koru."), "Belin çökmesi veya kalçanın aşırı yükselmesi.", "Kısa ama kusursuz plank setleri her zaman daha değerlidir.", isBodyweightOnly = true, timerSuggestionSeconds = 45),
    template("cable_crunch", "Cable Crunch", "Karın", "Cable ve ip aparatı", "Crunch'ta kalçayı değil, göğüs kafesini pelvise yaklaştırma hareketini düşünmek gerekir.", listOf("Kablo karşısında diz çök ve ipi başın iki yanında sabitle.", "Kaburgaları pelvise doğru kıvırarak kapan.", "Yavaşça açılarak tekrar başına dön."), "Hareketi kalçadan yapmak.", "Kısa tam aralık ve kontrollü tempo karın hissini artırır."),
    template("hanging_knee_raise", "Hanging Knee Raise", "Karın / kalça fleksörleri", "Barfiks barı veya roman chair", "Barfiks barında sallanmayı azaltmak, karın işini gerçekten hedefe taşır.", listOf("Barı sağlam kavra ve omuzları aşağı yerleştir.", "Dizleri kontrollü biçimde göğse yaklaştır.", "Aşağı inerken salınımı kontrol et."), "Bacakları hızla savurmak.", "Daha az tekrar ama daha az sallanma, daha kaliteli karın çalışması demektir.", isBodyweightOnly = true),
    template("ab_wheel", "Ab Wheel", "Karın / anti-ekstansiyon", "Ab wheel", "Ne kadar uzağa gittiğin değil, bel çökmeden ne kadar kontrollü kaldığın önemlidir.", listOf("Diz çökerek wheel'i omuzların altında başlat.", "Bel hattını nötr koruyarak öne açıl.", "Karınla geri toplanarak başlangıca dön."), "Bel boşluğunu kaybetmek.", "Menzili yavaş yavaş artırmak en güvenli yöntemdir.", isBodyweightOnly = true),
    template("farmers_carry", "Farmer's Carry", "Ön kol / core / postür", "Dambil veya trap bar", "Yürürken omuzları kulaklardan uzak ve gövdeyi dik tutmak hareketin ana hedefidir.", listOf("Dambılları sağlam kavra ve ayağa dik kalk.", "Kısa ve ritmik adımlarla dik yürürken yana eğilme.", "Bitirişte ağırlığı kontrollü indir."), "Omuzları kulaklara çekmek.", "Carry, kavrama ve core dayanıklılığını aynı anda toplar."),
    template("reverse_wrist_curl", "Reverse Wrist Curl", "Ön kol ekstansörleri", "Dambil veya kısa bar", "Ekstansör tarafını çalıştırmak, bilek dengesini güçlendirir.", listOf("Ön kolu sabitle ve avuç içi aşağı bakacak şekilde tut.", "Bileği yukarı kaldırırken dirsek ve omuz pozisyonunu koru.", "Yavaşça aşağı dön."), "Ağırlığı savurmak.", "Wrist curl ile dönüşümlü kullanmak bilek dengesini destekler.")
).associateBy { it.id }
