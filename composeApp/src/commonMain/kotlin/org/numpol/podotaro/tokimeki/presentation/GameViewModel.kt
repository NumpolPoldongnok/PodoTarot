package org.numpol.podotaro.tokimeki.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.numpol.podotaro.tokimeki.domain.Activity
import org.numpol.podotaro.tokimeki.domain.ActivityType
import org.numpol.podotaro.tokimeki.domain.GirlCharacter
import org.numpol.podotaro.tokimeki.domain.GirlType
import org.numpol.podotaro.tokimeki.domain.Player
import org.numpol.podotaro.tokimeki.domain.SkillType
import org.numpol.podotaro.tokimeki.domain.getDefaultGirlSchedule
import org.numpol.podotaro.tokimeki.domain.performActivity

// Enumeration of screens.
enum class GameScreen { MENU, SCHEDULE, CONVERSATION_SELECTION, CONVERSATION, STATUS, GIFT }

// A simple GameViewModel that holds the game state and exposes use-case functions.
class GameViewModel : ViewModel() {

    // State variables.
    var currentScreen by mutableStateOf(GameScreen.MENU)
    var currentDate by mutableStateOf(LocalDate(2024, 1, 1))
    var currentHour by mutableStateOf(6)
    var player by mutableStateOf(Player())
    var conversationGirl: GirlCharacter? = null
    var currentDialogue: DialogueNode? = null
    var pendingConversationGirls = mutableStateListOf<GirlCharacter>()
    var activityResultPopup by mutableStateOf<String?>(null)

    // Define some default activities.
    val activitiesWeekday = listOf(
        Activity("เรียน (เช้า)", 2, 5, mapOf(SkillType.STUDY to 5),
            activityType = ActivityType.STUDY, availableStart = 8, availableEnd = 12),
        Activity("เรียน (บ่าย)", 2, 5, mapOf(SkillType.STUDY to 5),
            activityType = ActivityType.STUDY, availableStart = 14, availableEnd = 18),
        Activity("ชมรม", 2, 4, mapOf(SkillType.SOCIAL to 4),
            activityType = ActivityType.CLUB, availableStart = 12, availableEnd = 20),
        Activity("ออกกำลังกาย", 1, 8, mapOf(SkillType.EXERCISE to 4),
            activityType = ActivityType.EXERCISE, availableStart = 0, availableEnd = 24),
        Activity("งานพิเศษ", 4, 10, emptyMap(), moneyReward = 50,
            activityType = ActivityType.WORK, availableStart = 18, availableEnd = 22)
    )
    val activitiesWeekend = listOf(
        Activity("งานพิเศษ (กะเช้า)", 6, 10, emptyMap(), moneyReward = 100,
            activityType = ActivityType.WORK, availableStart = 10, availableEnd = 16),
        Activity("งานพิเศษ (กะเย็น)", 6, 10, emptyMap(), moneyReward = 100,
            activityType = ActivityType.WORK, availableStart = 16, availableEnd = 22),
        Activity("งานพิเศษ (กะค่ำ)", 4, 10, emptyMap(), moneyReward = 100,
            activityType = ActivityType.WORK, availableStart = 22, availableEnd = 24),
        Activity("วิ่ง (เช้า)", 2, 5, mapOf(SkillType.EXERCISE to 6),
            activityType = ActivityType.EXERCISE, availableStart = 6, availableEnd = 10),
        Activity("ชกมวย (เย็น)", 2, 8, mapOf(SkillType.EXERCISE to 6),
            activityType = ActivityType.EXERCISE, availableStart = 18, availableEnd = 22)
    )

    // Initialize girls with a default schedule.
    var girls = mutableStateListOf(
        GirlCharacter(GirlType.SAME_YEAR, "มิน", ActivityType.STUDY, schedule = getDefaultGirlSchedule()),
        GirlCharacter(GirlType.JUNIOR, "ฟ้า", ActivityType.EXERCISE, schedule = getDefaultGirlSchedule()),
        GirlCharacter(GirlType.SENIOR, "แสง", ActivityType.CLUB, schedule = getDefaultGirlSchedule()),
        GirlCharacter(GirlType.SEXY_PROFESSOR, "อาจารย์สุวรรณ", ActivityType.RELAX, schedule = getDefaultGirlSchedule())
    )

    // Use-case functions:

    fun startDay() {
        if (player.energy == 0) {
            activityResultPopup = "คุณหักโหมเกินไป\nคุณเริ่มไม่สบาย ต้องพักผ่อนแล้ว"
        } else {
            currentScreen = GameScreen.SCHEDULE
        }
    }

    fun nap() {
        val oldEnergy = player.energy
        // Use a 1-hr nap activity.
        player = performActivity(Activity("พักผ่อน (1 ชม.)", 1, 0, emptyMap(),
            activityType = ActivityType.RELAX, availableStart = 0, availableEnd = 24), player)
        currentHour = (currentHour + 1) % 24
        if (currentHour == 0) currentDate = currentDate.plus(1, DateTimeUnit.DAY)
        activityResultPopup = "คุณพักผ่อน 1 ชม.\nพลังงาน: +${player.energy - oldEnergy}"
        currentScreen = GameScreen.MENU
    }

    fun sleep3() {
        val oldEnergy = player.energy
        player.energy = (player.energy + 30).coerceAtMost(100)
        repeat(3) {
            currentHour = (currentHour + 1) % 24
            if (currentHour == 0) currentDate = currentDate.plus(1, DateTimeUnit.DAY)
        }
        activityResultPopup = "คุณนอน 3 ชม.\nพลังงาน: +${player.energy - oldEnergy}"
        currentScreen = GameScreen.MENU
    }

    fun sleepUntil6() {
        player.energy = 100
        player.exercise += 12
        currentDate = currentDate.plus(1, DateTimeUnit.DAY)
        currentHour = 6
        activityResultPopup = "คุณนอนจนถึงเช้าวันถัดไป\nพลังงานเต็มและ คะแนนออกกำลังกาย +12"
        currentScreen = GameScreen.MENU
    }

    fun selectActivity(activity: Activity) {
        if (currentHour !in activity.availableStart until activity.availableEnd) return
        if (player.energy < activity.energyCost) {
            activityResultPopup = "พลังงานไม่พอสำหรับกิจกรรมนี้"
            return
        }
        val activityStartTime = currentHour
        val oldStudy = player.study
        val oldExercise = player.exercise
        val oldSocial = player.social
        val oldMoney = player.money
        player = performActivity(activity, player)
        val studyDiff = player.study - oldStudy
        val exerciseDiff = player.exercise - oldExercise
        val socialDiff = player.social - oldSocial
        val moneyDiff = player.money - oldMoney
        currentHour = (currentHour + activity.duration) % 24
        if (currentHour < activity.availableStart) currentDate = currentDate.plus(1, DateTimeUnit.DAY)
        if (activity.activityType != ActivityType.RELAX) {
            val meetingGirls = girls.filter { girl ->
                girl.schedule.any { slot ->
                    activityStartTime in slot.startHour until slot.endHour &&
                            slot.activityType == activity.activityType
                }
            }
            if (meetingGirls.isNotEmpty()) {
                pendingConversationGirls.clear()
                pendingConversationGirls.addAll(meetingGirls)
                currentScreen = GameScreen.CONVERSATION_SELECTION
            } else {
                activityResultPopup = "ผลลัพธ์กิจกรรม:\nเรียน: +$studyDiff, ออกกำลังกาย: +$exerciseDiff, สังคม: +$socialDiff, เงิน: +$moneyDiff"
                currentScreen = GameScreen.SCHEDULE
            }
        } else {
            activityResultPopup = "ผลลัพธ์กิจกรรม:\nเรียน: +$studyDiff, ออกกำลังกาย: +$exerciseDiff, สังคม: +$socialDiff, เงิน: +$moneyDiff"
            currentScreen = GameScreen.SCHEDULE
        }
        if (player.energy == 0) {
            activityResultPopup = "คุณหักโหมเกินไป\nคุณเริ่มไม่สบาย ต้องพักผ่อนแล้ว"
            currentScreen = GameScreen.MENU
        }
    }

    fun callGirl(girl: GirlCharacter) {
        player.energy = (player.energy - 10).coerceAtLeast(0)
        currentHour = (currentHour + 1) % 24
        if (currentHour == 0) currentDate = currentDate.plus(1, DateTimeUnit.DAY)
        conversationGirl = girl
        currentDialogue = getDialogueForGirl(girl)
        currentScreen = GameScreen.CONVERSATION
    }
}
