package com.example.gym_app

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.IsoFields
import kotlin.math.roundToInt

const val VERSION_NAME_LABEL = "1.0"
const val BUILD_LABEL = "290326,1936"
const val VERSION_DISPLAY = "Versiyon 1.0 (İş Derlemesi 290326,1936)"
const val DONE_TODO_FILE_NAME = "yapilanlar_ve_yapilacaklar.txt"

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
    val mediaCaption: String = "Optimize yerel görsel veya kisa video alani bu karta eklenebilir."
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

data class PersistedState(
    val weekKey: String = currentWeekKey(),
    val weeklyProgress: Map<String, DayProgress> = emptyMap(),
    val exerciseWeights: Map<String, Double> = emptyMap(),
    val completedWorkouts: List<CompletedWorkout> = emptyList(),
    val currentWeightKg: Double = 59.0,
    val targetWeightKg: Double = 70.0,
    val lowMediaMode: Boolean = true
)

data class Badge(
    val title: String,
    val description: String
)

data class ReleaseSection(
    val title: String,
    val versionLabel: String,
    val items: List<String>
)

fun currentWeekKey(date: LocalDate = LocalDate.now()): String {
    val week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
    val weekYear = date.get(IsoFields.WEEK_BASED_YEAR)
    return "$weekYear-W${week.toString().padStart(2, '0')}"
}

fun PersistedState.normalizeForCurrentWeek(today: LocalDate = LocalDate.now()): PersistedState {
    val expectedKey = currentWeekKey(today)
    if (weekKey == expectedKey) return this
    return copy(weekKey = expectedKey, weeklyProgress = emptyMap())
}

private fun makeExercise(
    id: String,
    name: String,
    targetArea: String,
    equipment: String,
    setCount: Int,
    repText: String,
    referenceReps: Int,
    restSeconds: Int,
    quickNote: String,
    steps: List<String>,
    commonMistake: String,
    tip: String,
    isBodyweightOnly: Boolean = false,
    timerSuggestionSeconds: Int? = null
): Exercise {
    return Exercise(
        id = id,
        name = name,
        targetArea = targetArea,
        equipment = equipment,
        setCount = setCount,
        repText = repText,
        referenceReps = referenceReps,
        restSeconds = restSeconds,
        quickNote = quickNote,
        steps = steps,
        commonMistake = commonMistake,
        tip = tip,
        isBodyweightOnly = isBodyweightOnly,
        timerSuggestionSeconds = timerSuggestionSeconds
    )
}

fun workoutPlan(): List<WorkoutDay> {
    val upperAExercises = listOf(
        makeExercise(
            "bench_press",
            "Bench Press veya Chest Press",
            "Göğüs / ön omuz",
            "Bench veya makine",
            3,
            "6-8 tekrar",
            7,
            90,
            "Ana göğüs hareketi. Son tekrarlar zorlayıcı olabilir.",
            listOf(
                "Sırtini bench'e yerleştir ve ayaklarini yere sabitle.",
                "Bari ya da tutacagi göğsüne kontrollu indir.",
                "Nefes vererek yukari it ve omuzlarini kulaga cekme."
            ),
            "Agirligi fazla secip formu bozmak.",
            "Hiz dusse bile hareket formu ayni kalsin."
        ),
        makeExercise(
            "seated_cable_row",
            "Seated Cable Row",
            "Sırt",
            "Cable row",
            3,
            "8-10 tekrar",
            9,
            75,
            "Sırti sikistir; bel ile cekme.",
            listOf(
                "Göğsünu dik tut ve kulpu kontrollu kavra.",
                "Dirsekleri geriye cekip kurek kemiklerini yaklastir.",
                "One donerken agirligi birakma."
            ),
            "Govdeyi geriye savurmak.",
            "Cekisi ellerle degil sırtla baslat."
        ),
        makeExercise(
            "incline_db_press",
            "Incline Dumbbell Press",
            "Üst göğüs",
            "Ayarlanabilir bench + dambil",
            3,
            "8-10 tekrar",
            9,
            75,
            "Üst göğüs ve ön omuz desteği.",
            listOf(
                "Bench'i hafif eğimli ayarla.",
                "Dambilleri göğüs hizasindan yukari it.",
                "Asagi inerken omuzlarini onde yuvarlama."
            ),
            "Bench acisini fazla dik yapmak.",
            "Dambilleri carpistirmana gerek yok."
        ),
        makeExercise(
            "lat_pulldown",
            "Lat Pulldown",
            "Kanat / sırt",
            "Lat pulldown makinesi",
            3,
            "10-12 tekrar",
            11,
            60,
            "Barı üst göğse çek.",
            listOf(
                "Dizlerini pedin altina sabitle.",
                "Bari enseye degil ust gogse cek.",
                "Yukarida agirligi ziplatmadan geri don."
            ),
            "Boynu ileri uzatmak.",
            "Cekerken göğsü hafif onde tut."
        ),
        makeExercise(
            "lateral_raise",
            "Dumbbell Lateral Raise",
            "Omuz yan bas",
            "Dambil",
            2,
            "12-15 tekrar",
            13,
            45,
            "Kontrollü tekrar ile omuz genişliği hedeflenir.",
            listOf(
                "Dambilleri yanlarda baslat.",
                "Kollari omuz hizasina kadar yana ac.",
                "Asagi inerken savurma yapma."
            ),
            "Belden momentum almak.",
            "Küçük ağırlıkla temiz tekrar daha iyi çalışır."
        ),
        makeExercise(
            "triceps_pushdown",
            "Cable Triceps Pushdown",
            "Arka kol",
            "Cable istasyonu",
            2,
            "10-12 tekrar",
            11,
            45,
            "Dirsekler govdeye yakin kalir.",
            listOf(
                "Bari göğüs hizasinda al.",
                "Dirsekleri sabitle ve asagi it.",
                "Donuste kontrolu kaybetme."
            ),
            "Dirsekleri ileri geri oynatmak.",
            "Hareketi sadece ön kolla tamamla."
        ),
        makeExercise(
            "incline_db_curl",
            "Incline Dumbbell Curl",
            "Ön kol",
            "Incline bench + dambil",
            2,
            "10-12 tekrar",
            11,
            45,
            "Sallanmadan tam aralikta yap.",
            listOf(
                "Bench'e yaslan ve kollari serbest birak.",
                "Dambili omza cekip tepe noktada sik.",
                "Asagi inerken omuzu onde dusurme."
            ),
            "Govdeyi sallamak.",
            "Her tekrarda ayni tempoyu koru."
        )
    )

    val lowerAExercises = listOf(
        makeExercise(
            "leg_press",
            "Leg Press veya Guvenli Squat",
            "Ön bacak",
            "Leg press / squat",
            3,
            "8-10 tekrar",
            9,
            90,
            "Ilk haftalarda guvenli makine tercihi olur.",
            listOf(
                "Sırtini mindere tam yasla.",
                "Ayaklarini omuz genisliginde yerlestir.",
                "Asagi inerken dizlerin ice çokmesin."
            ),
            "Dizi tam kilitlemek.",
            "Topuktan kuvvet al."
        ),
        makeExercise(
            "romanian_deadlift",
            "Romanian Deadlift",
            "Arka bacak / kalça",
            "Bar veya dambil",
            3,
            "8-10 tekrar",
            9,
            90,
            "Bel yuvarlama yok, kalçayi geriye gonder.",
            listOf(
                "Dizlerini hafif kir.",
                "Agirligi bacaklara yakin indir.",
                "Kalça ile tekrar ayağa don."
            ),
            "Belden kapanmak.",
            "Asagi inme derinliginden once formu koru."
        ),
        makeExercise(
            "walking_lunge",
            "Walking Lunge veya Split Squat",
            "Bacak / denge",
            "Vucut agirligi veya dambil",
            2,
            "10/10 tekrar",
            20,
            60,
            "Her bacak ayri calisir.",
            listOf(
                "Bir adim one at ve kontrollu in.",
                "On diz ayak yonunu takip etsin.",
                "Topuktan guc alip ayaga don."
            ),
            "Adimi çok kisa tutmak.",
            "Denge için acele etme."
        ),
        makeExercise(
            "leg_curl",
            "Seated veya Lying Leg Curl",
            "Hamstring",
            "Leg curl makinesi",
            2,
            "10-12 tekrar",
            11,
            60,
            "Hamstring izolasyonu.",
            listOf(
                "Makineyi boyuna göre ayarla.",
                "Topuklari kalçaya cek.",
                "Asagi donuste agirligi birakma."
            ),
            "Kalça kaldirarak hile yapmak.",
            "Yanma hissi normaldir."
        ),
        makeExercise(
            "standing_calf_raise",
            "Standing Calf Raise",
            "Baldir",
            "Calf makinesi",
            3,
            "12-15 tekrar",
            13,
            45,
            "Tam asagi, tam yukari calis.",
            listOf(
                "Topuklari kontrollu sekilde asagi birak.",
                "Parmak ucuna kuvvetle yuksel.",
                "Ustte kisa sure bekle."
            ),
            "Yarim tekrar yapmak.",
            "Yavaş negatif daha çok hissettirir."
        ),
        makeExercise(
            "plank",
            "Plank",
            "Karin / core",
            "Mat",
            3,
            "30-45 saniye",
            1,
            30,
            "Kalça dusmesin, boyun notr kalsin.",
            listOf(
                "Dirsekleri omuzlarin altina koy.",
                "Karni sik ve nefesini tutma.",
                "Sure boyunca belini sabit tut."
            ),
            "Sure uzasin diye formu bozmak.",
            "30 saniye temiz plank çok degerlidir.",
            isBodyweightOnly = true,
            timerSuggestionSeconds = 40
        )
    )

    val upperBExercises = listOf(
        makeExercise(
            "shoulder_press",
            "Seated Dumbbell Shoulder Press",
            "Omuz",
            "Dambil",
            3,
            "8-10 tekrar",
            9,
            75,
            "Omuz için ana hareket.",
            listOf(
                "Sırtini bench'e yasla.",
                "Dambilleri kulak hizasindan yukari it.",
                "Bel boslugunu gereksiz buyutme."
            ),
            "Belden fazla destek almak.",
            "Karin kaslarini hafif siki tut."
        ),
        makeExercise(
            "chest_supported_row",
            "Chest Supported Row",
            "Sırt",
            "Destekli row makinesi",
            3,
            "8-10 tekrar",
            9,
            75,
            "Bel yukunu azaltir.",
            listOf(
                "Göğsünu padoya yasla.",
                "Dirsekleri geriye cek.",
                "Donuste omuzlari onde birakma."
            ),
            "Sırti yuvarlamak.",
            "Cekiste göğsün pad'e sabit kalsin."
        ),
        makeExercise(
            "machine_chest_press",
            "Machine Chest Press veya Cable Fly",
            "Göğüs",
            "Makine veya cable",
            3,
            "10-12 tekrar",
            11,
            60,
            "Göğüsun ikinci haftalik temasi.",
            listOf(
                "Omuzlari geride tut.",
                "Tutacaklari ileri it.",
                "Asagi donerken göğüste gerginlik koru."
            ),
            "Omuzu onde sikistirmak.",
            "Ağırlıktan once hissi hedefle."
        ),
        makeExercise(
            "neutral_pulldown",
            "Neutral Grip Lat Pulldown",
            "Kanat",
            "Lat pulldown",
            3,
            "10-12 tekrar",
            11,
            60,
            "Sırt genisligi odağı.",
            listOf(
                "Tutacaklari rahat kavra.",
                "Dirsekleri asagi ve geriye cek.",
                "Yukarida agirligi ziplatma."
            ),
            "Boynu ileri uzatmak.",
            "Dirsek yoluna odaklan."
        ),
        makeExercise(
            "hammer_curl",
            "Hammer Curl",
            "Ön kol / ön kol alt",
            "Dambil",
            2,
            "10-12 tekrar",
            11,
            45,
            "Ön kola ek destek verir.",
            listOf(
                "Avuclar birbirine bakacak sekilde basla.",
                "Dirsekleri sabit tutup yukari cek.",
                "Asagi inerken savurma yapma."
            ),
            "Govdeyi sallamak.",
            "Her iki kol ayni ritimde olsun."
        ),
        makeExercise(
            "overhead_triceps_extension",
            "Overhead Cable Triceps Extension",
            "Arka kol uzun bas",
            "Cable istasyonu",
            2,
            "10-12 tekrar",
            11,
            45,
            "Uzun bas vurgusu.",
            listOf(
                "Ipi bas ustune al.",
                "Dirsekleri sabit tutup yukari uzat.",
                "Donuste boynu one itme."
            ),
            "Dirsekleri acip kapamak.",
            "Hareket cizgini bozmadan tekrar et."
        )
    )

    val lowerBExercises = listOf(
        makeExercise(
            "hack_squat",
            "Hack Squat, Goblet Squat veya Smith Squat",
            "Ön bacak",
            "Hack squat / dambil / smith",
            3,
            "8-10 tekrar",
            9,
            90,
            "Ön bacak odağı.",
            listOf(
                "Ayaklarini dengeli konumla.",
                "Kalça ve dizleri birlikte kir.",
                "Topuktan kuvvet alip yuksel."
            ),
            "Ağırlık hirsiyla hareket derinligini kaybetmek.",
            "Temiz tekrar onceligi koru."
        ),
        makeExercise(
            "hip_hinge",
            "Hip Hinge: RDL veya Back Extension",
            "Arka bacak / kalça",
            "Bar, dambil veya roman chair",
            3,
            "10 tekrar",
            10,
            75,
            "Kalça dominant temiz calis.",
            listOf(
                "Kalça kirimini hisset.",
                "Belini duz tut.",
                "Kalçayi ileri surerek bitir."
            ),
            "Belden kapanmak.",
            "Karin kaslarini aktif tut."
        ),
        makeExercise(
            "leg_extension",
            "Leg Extension",
            "Quadriceps",
            "Leg extension makinesi",
            2,
            "12-15 tekrar",
            13,
            45,
            "Quadriceps bitirici.",
            listOf(
                "Makineyi dizine uygun ayarla.",
                "Ayagi yukari uzat.",
                "Asagi inerken direnci koru."
            ),
            "Hareketi ziplatarak yapmak.",
            "Ustte kisa bir sikma ekle."
        ),
        makeExercise(
            "leg_curl_b",
            "Seated Leg Curl",
            "Hamstring",
            "Leg curl makinesi",
            2,
            "12-15 tekrar",
            13,
            45,
            "Hamstring ikinci temas.",
            listOf(
                "Topuklari kalçaya cek.",
                "Kalçalari koltukta sabit tut.",
                "Yavaş geri don."
            ),
            "Negatif kismi atlamak.",
            "Yavaş geri donus daha çok hissettirir."
        ),
        makeExercise(
            "standing_calf_raise_b",
            "Standing Calf Raise",
            "Baldir",
            "Calf makinesi",
            3,
            "12-15 tekrar",
            13,
            45,
            "Yavaş eksantrik ile calis.",
            listOf(
                "Topuklari asagi sal.",
                "Parmak ucuna cik.",
                "Kontrollu geri don."
            ),
            "Yarim tekrar yapmak.",
            "Tam aralik baldir için onemli."
        ),
        makeExercise(
            "hanging_knee_raise",
            "Hanging Knee Raise veya Reverse Crunch",
            "Karin",
            "Bar veya mat",
            3,
            "12-15 tekrar",
            13,
            30,
            "Karin ve kalça kontrolu.",
            listOf(
                "Dizleri karin bolgesine cek.",
                "Belini savurmadan yuksel.",
                "Asagi inerken kontrolu koru."
            ),
            "Sallanarak tekrar cikarmak.",
            "Az tekrar ama temiz tekrar yeterlidir."
        )
    )

    val optionalDayExercises = listOf(
        makeExercise(
            "cable_curl",
            "Cable Curl",
            "Ön kol",
            "Cable",
            2,
            "12-15 tekrar",
            13,
            45,
            "Kol hacmi için hafif pump.",
            listOf(
                "Dirsekleri sabit tut.",
                "Tutacagi omza dogru cek.",
                "Asagi inerken savurma yapma."
            ),
            "Omzu harekete dahil etmek.",
            "Hafif ağırlıkla yanma kovala."
        ),
        makeExercise(
            "rope_pushdown",
            "Rope Pushdown",
            "Arka kol",
            "Cable",
            2,
            "12-15 tekrar",
            13,
            45,
            "Dirsek dostu hafif pump.",
            listOf(
                "Dirseklerini sabit tut.",
                "Ipi asagi iterken disari ayir.",
                "Yavaş geri don."
            ),
            "Govde ile agirligi asagi itmek.",
            "Arka kolu tepe noktada hisset."
        ),
        makeExercise(
            "face_pull",
            "Face Pull",
            "Arka omuz / ust sırt",
            "Cable",
            2,
            "12-15 tekrar",
            13,
            45,
            "Arka omuz destegi.",
            listOf(
                "Ipi yuz hizasina cek.",
                "Dirsekleri disa ac.",
                "Kurek kemiklerini geriye yaklastir."
            ),
            "Ipi gogse cekmek.",
            "Yuz hizasi hedefin olsun."
        ),
        makeExercise(
            "incline_walk",
            "Incline Yuruyus veya Bisiklet",
            "Toparlanma",
            "Kosu bandi / bisiklet",
            1,
            "10-15 dakika",
            1,
            0,
            "Rahat tempo ve toparlanma odağı.",
            listOf(
                "Konusabilecek kadar rahat tempoda kal.",
                "Nefesini zorlama.",
                "Bitiste hafif esneme yap."
            ),
            "Opsiyonel gunu yorucu ana antrenmana cevirmek.",
            "Bu gun eksik kalanlari toparlama gunu olsun.",
            isBodyweightOnly = true,
            timerSuggestionSeconds = 600
        )
    )

    return listOf(
        WorkoutDay("upper_a", "Üst Vücut A", "Pazartesi", DayOfWeek.MONDAY, "Göğüs, sırt, omuz ve kollar", 48, "Kas kazanımı odaklı ilk üst vücut seansı.", upperAExercises, true),
        WorkoutDay("lower_a", "Alt Vücut A + Karın", "Salı", DayOfWeek.TUESDAY, "Ön bacak, arka bacak ve core", 47, "Güvenli temel bacak günü ve core kurulumu.", lowerAExercises, true),
        WorkoutDay("recovery_mid", "Aktif Toparlanma", "Çarşamba", DayOfWeek.WEDNESDAY, "Toparlanma", 20, "Hafif yürüyüş, mobilite, su ve uyku odağı.", emptyList(), false, true, "Bugün ağırlık yerine hafif yürüyüş, su ve uyku kalitesi odakta kalsın."),
        WorkoutDay("upper_b", "Üst Vücut B", "Perşembe", DayOfWeek.THURSDAY, "Omuz, sırt, göğüs ve kollar", 49, "Haftanın ikinci üst vücut teması.", upperBExercises, true),
        WorkoutDay("lower_b", "Alt Vücut B + Karın", "Cuma", DayOfWeek.FRIDAY, "Ön bacak, arka bacak, baldır ve karın", 46, "Alt vücut hacmini ikinci kez destekleyen gün.", lowerBExercises, true),
        WorkoutDay("optional_pump", "Opsiyonel 5. Gün", "Cumartesi", DayOfWeek.SATURDAY, "Kol, omuz ve toparlanma", 35, "Enerjin varsa hafif pump ve eksik kapatma günü.", optionalDayExercises, false),
        WorkoutDay("recovery_full", "Tam Dinlenme", "Pazar", DayOfWeek.SUNDAY, "Toparlanma", 0, "Tam dinlenme ve haftaya hazırlık.", emptyList(), false, true, "Bugün tam dinlen. Yarın üst vücut seansı için enerji biriktir.")
    )
}

fun releaseSections(): List<ReleaseSection> = listOf(
    ReleaseSection(
        title = "Yapılanlar",
        versionLabel = VERSION_DISPLAY,
        items = listOf(
            "Türkçe ana sayfa ve bugun ne yapacagim karti eklendi.",
            "Gunluk antrenman ekrani, set tamamlama ve sure sayaci akisi kuruldu.",
            "Hareket kutuphanesi ve baslangic seviyesine uygun anlatim bloklari eklendi.",
            "Haftalik tamamlama, streak, rozetler ve ilerleme kartlari hazirlandi.",
            "Sürüm notlari ekrani ile proje ici kayıt standardi olusturuldu."
        )
    ),
    ReleaseSection(
        title = "Yapılacaklar",
        versionLabel = "Planlanan: Versiyon 1.1 ve sonrasi",
        items = listOf(
            "Yerel hareket görselleri ve kisa video klipleri optimize edilerek eklenecek.",
            "Hatırlatıcı bildirimler ve veri yedekleme secenegi acilacak.",
            "Ileri seviye özel rutin olusturma ve sağlık entegrasyonlari genisletilecek."
        )
    )
)

fun workoutById(id: String): WorkoutDay = workoutPlan().first { it.id == id }

fun allExercises(): List<Exercise> = workoutPlan().flatMap { it.exercises }.distinctBy { it.id }

fun recommendedWorkout(state: PersistedState, today: LocalDate = LocalDate.now()): WorkoutDay {
    val normalized = state.normalizeForCurrentWeek(today)
    val plan = workoutPlan()
    val todaysWorkout = plan.first { it.dayOfWeek == today.dayOfWeek }
    if (!todaysWorkout.isRestDay && todaysWorkout.isMandatory && !isWorkoutCompleted(normalized, todaysWorkout.id)) {
        return todaysWorkout
    }
    return plan.firstOrNull { day ->
        day.isMandatory && !day.isRestDay && !isWorkoutCompleted(normalized, day.id)
    } ?: plan.first { it.id == "upper_a" }
}

fun totalSets(day: WorkoutDay): Int = day.exercises.sumOf { it.setCount }

fun completedSets(day: WorkoutDay, state: PersistedState): Int {
    val dayProgress = state.weeklyProgress[day.id] ?: return 0
    return day.exercises.sumOf { exercise ->
        dayProgress.completedSets[exercise.id]?.coerceIn(0, exercise.setCount) ?: 0
    }
}

fun dayCompletionRatio(day: WorkoutDay, state: PersistedState): Float {
    if (day.exercises.isEmpty()) return 0f
    return completedSets(day, state).toFloat() / totalSets(day).toFloat()
}

fun isWorkoutCompleted(state: PersistedState, dayId: String): Boolean {
    return state.weeklyProgress[dayId]?.isCompleted == true
}

fun weeklyCompletionRatio(state: PersistedState): Float {
    val mandatoryDays = workoutPlan().filter { it.isMandatory && !it.isRestDay }
    if (mandatoryDays.isEmpty()) return 0f
    val completedCount = mandatoryDays.count { isWorkoutCompleted(state, it.id) }
    return completedCount.toFloat() / mandatoryDays.size.toFloat()
}

fun weeklyCompletionPercent(state: PersistedState): Int = (weeklyCompletionRatio(state) * 100).roundToInt()

fun totalCompletedWorkouts(state: PersistedState): Int = state.completedWorkouts.size

fun estimatedTotalVolume(state: PersistedState): Int = state.completedWorkouts.sumOf { it.estimatedVolumeKg }

fun streakCount(state: PersistedState, today: LocalDate = LocalDate.now()): Int {
    val distinctDates = state.completedWorkouts
        .mapNotNull { runCatching { LocalDate.parse(it.completedOn) }.getOrNull() }
        .distinct()
        .toSet()
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

fun unlockedBadges(state: PersistedState): List<Badge> {
    val badges = mutableListOf<Badge>()
    val completed = totalCompletedWorkouts(state)
    if (completed >= 1) badges += Badge("Ilk Adim", "Ilk antrenmanini sisteme kaydettin.")
    if (completed >= 3) badges += Badge("Ritme Giris", "Uc tamamlanmis antrenman ile duzeni kurdun.")
    if (weeklyCompletionPercent(state) >= 100) badges += Badge("Hafta Tamam", "Bu haftanin 4 ana antrenmanini bitirdin.")
    if (streakCount(state) >= 3) badges += Badge("Zincir Korundu", "En az 3 gunluk devam zincirin var.")
    return badges
}

fun motivationMessage(state: PersistedState, today: LocalDate = LocalDate.now()): String {
    val streak = streakCount(state, today)
    val weekly = weeklyCompletionPercent(state)
    return when {
        weekly == 0 -> "Bu hafta ilk antrenmanı açıp ritmi başlat. Kısa bir giriş bile ilerleme yaratır."
        weekly in 1..49 -> "Hafta hareketlenmis durumda. Bugünku seans seni haftalik hedefe yaklastirir."
        weekly in 50..99 -> "Düzeni kurdun. Kalan seansları tamamlayınca güçlü bir hafta çıkacak."
        streak >= 3 -> "Devam zincirin gayet iyi gidiyor. Formu koruyup yüklenmeyi aceleye getirme."
        else -> "Bu hafta ana hedef tamamlandi. Haftayi toparlanarak ve notlarını guncelleyerek kapa."
    }
}

fun weightDisplay(value: Double): String {
    return if (value == value.toInt().toDouble()) {
        "${value.toInt()} kg"
    } else {
        "${"%.1f".format(value).replace('.', ',')} kg"
    }
}

fun PersistedState.adjustExerciseWeight(exerciseId: String, delta: Double): PersistedState {
    val current = exerciseWeights[exerciseId] ?: 0.0
    val updated = (current + delta).coerceAtLeast(0.0)
    val newWeights = exerciseWeights.toMutableMap()
    if (updated == 0.0) {
        newWeights.remove(exerciseId)
    } else {
        newWeights[exerciseId] = (updated * 10).roundToInt() / 10.0
    }
    return copy(exerciseWeights = newWeights)
}

fun PersistedState.adjustCurrentWeight(delta: Double): PersistedState {
    val updated = (currentWeightKg + delta).coerceAtLeast(40.0)
    return copy(currentWeightKg = (updated * 10).roundToInt() / 10.0)
}

fun PersistedState.toggleLowMediaMode(): PersistedState = copy(lowMediaMode = !lowMediaMode)

fun PersistedState.completeSet(day: WorkoutDay, exerciseId: String): PersistedState {
    val exercise = day.exercises.firstOrNull { it.id == exerciseId } ?: return this
    val currentDay = weeklyProgress[day.id] ?: DayProgress()
    val currentCount = currentDay.completedSets[exerciseId] ?: 0
    if (currentCount >= exercise.setCount) return this
    val updatedSets = currentDay.completedSets.toMutableMap()
    updatedSets[exerciseId] = currentCount + 1
    val updatedDay = currentDay.copy(completedSets = updatedSets)
    return copy(weeklyProgress = weeklyProgress + (day.id to updatedDay))
}

fun PersistedState.resetDay(dayId: String): PersistedState {
    if (!weeklyProgress.containsKey(dayId)) return this
    return copy(weeklyProgress = weeklyProgress - dayId)
}

fun estimatedVolumeForDay(day: WorkoutDay, state: PersistedState): Int {
    val dayProgress = state.weeklyProgress[day.id] ?: return 0
    return day.exercises.sumOf { exercise ->
        val reps = exercise.referenceReps
        val setsDone = dayProgress.completedSets[exercise.id] ?: 0
        val weight = state.exerciseWeights[exercise.id] ?: 0.0
        (setsDone * reps * weight).roundToInt()
    }
}

fun PersistedState.completeWorkout(day: WorkoutDay, completedOn: LocalDate = LocalDate.now()): PersistedState {
    val existing = weeklyProgress[day.id] ?: DayProgress()
    if (existing.isCompleted) return this
    val finalSets = day.exercises.associate { exercise ->
        val current = existing.completedSets[exercise.id] ?: 0
        exercise.id to current.coerceAtLeast(exercise.setCount)
    }
    val updatedDay = existing.copy(
        completedSets = finalSets,
        isCompleted = true,
        completedOn = completedOn.toString()
    )
    val history = completedWorkouts.toMutableList()
    history += CompletedWorkout(
        dayId = day.id,
        workoutTitle = day.title,
        completedOn = completedOn.toString(),
        estimatedVolumeKg = day.exercises.sumOf { exercise ->
            val reps = exercise.referenceReps
            val weight = exerciseWeights[exercise.id] ?: 0.0
            (exercise.setCount * reps * weight).roundToInt()
        }
    )
    return copy(weeklyProgress = weeklyProgress + (day.id to updatedDay), completedWorkouts = history)
}



