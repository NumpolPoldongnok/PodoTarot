//package org.numpol.podotaro
//
//// =====================
//// DOMAIN LAYER
//// =====================
//
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.ViewModel
//import kotlinx.datetime.DateTimeUnit
//import kotlinx.datetime.LocalDate
//import kotlinx.datetime.Month
//import kotlinx.datetime.plus
//import org.numpol.podotaro.presentation.getDialogueForGirl
//
//enum class SkillType { STUDY, EXERCISE, SOCIAL }
//enum class ActivityType { STUDY, WORK, EXERCISE, CLUB, CALL, DATE, RELAX, EXAM }
//enum class GirlType { SAME_YEAR, JUNIOR, SENIOR, SEXY_PROFESSOR }
//enum class RelationshipStatus { FRIEND, DATING }
//
//data class Player(
//    var energy: Int = 100,
//    var study: Int = 0,
//    var exercise: Int = 0,
//    var social: Int = 0,
//    var money: Int = 0
//)
//
//data class TimeSlot(
//    val startHour: Int,
//    val endHour: Int,
//    val activityType: ActivityType,
//    val location: String
//)
//
//data class GirlCharacter(
//    val type: GirlType,
//    val name: String,
//    val personality: ActivityType,
//    var affection: Int = 0,
//    var schedule: List<TimeSlot> = emptyList(),
//    var relationshipStatus: RelationshipStatus = RelationshipStatus.FRIEND,
//    var hasTalked: Boolean = false
//)
//
//data class Activity(
//    val name: String,
//    val duration: Int,          // in hours
//    val energyCost: Int,
//    val skillEffects: Map<SkillType, Int>,
//    val bonusAffection: Int = 0,
//    val activityType: ActivityType,
//    val availableStart: Int,    // inclusive
//    val availableEnd: Int,      // exclusive
//    val moneyReward: Int = 0
//)
//
//fun performActivity(activity: Activity, player: Player): Player {
//    return if (activity.activityType == ActivityType.RELAX && activity.name.contains("พักผ่อน")) {
//        // For a 1-hr nap, restore 10% energy.
//        val restoreAmount = 10
//        player.energy = (player.energy + restoreAmount).coerceAtMost(100)
//        player
//    } else {
//        player.energy = (player.energy - activity.energyCost).coerceAtLeast(0)
//        activity.skillEffects.forEach { (skill, effect) ->
//            when (skill) {
//                SkillType.STUDY -> player.study += effect
//                SkillType.EXERCISE -> player.exercise += effect
//                SkillType.SOCIAL -> player.social += effect
//            }
//        }
//        if (activity.moneyReward > 0) {
//            player.money += activity.moneyReward
//        }
//        player
//    }
//}
//
//fun getDayOfMonth(date: LocalDate): Int = when(date.month) {
//    Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY, Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
//    Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
//    Month.FEBRUARY -> if ((date.year % 4 == 0 && date.year % 100 != 0) || (date.year % 400 == 0)) 29 else 28
//    else -> 30
//}
//
//fun isLastDayOfMonth(date: LocalDate): Boolean = date.dayOfMonth == getDayOfMonth(date)
//
//fun getDefaultGirlSchedule(): List<TimeSlot> = listOf(
//    TimeSlot(10, 12, ActivityType.STUDY, "Library"),
//    TimeSlot(14, 16, ActivityType.EXERCISE, "Gym"),
//    TimeSlot(18, 20, ActivityType.CLUB, "Café")
//)
//
//// =====================
//// PRESENTATION LAYER
//// =====================
//
//import androidx.lifecycle.ViewModel
//import androidx.compose.runtime.mutableStateOf
//
//enum class GameScreen { MENU, SCHEDULE, CONVERSATION_SELECTION, CONVERSATION, STATUS, GIFT }
//
//class GameViewModel : ViewModel() {
//    var currentScreen by mutableStateOf(GameScreen.MENU)
//    var currentDate by mutableStateOf(LocalDate(2024, 1, 1))
//    var currentHour by mutableStateOf(6)
//    var player by mutableStateOf(Player())
//    var conversationGirl: GirlCharacter? = null
//    var currentDialogue: DialogueNode? = null
//    var pendingConversationGirls = mutableStateListOf<GirlCharacter>()
//    var activityResultPopup by mutableStateOf<String?>(null)
//
//    // Define weekday and weekend activities.
//    val activitiesWeekday = listOf(
//        Activity("เรียน (เช้า)", 2, 5, mapOf(SkillType.STUDY to 5), activityType = ActivityType.STUDY, availableStart = 8, availableEnd = 12),
//        Activity("เรียน (บ่าย)", 2, 5, mapOf(SkillType.STUDY to 5), activityType = ActivityType.STUDY, availableStart = 14, availableEnd = 18),
//        Activity("ชมรม", 2, 4, mapOf(SkillType.SOCIAL to 4), activityType = ActivityType.CLUB, availableStart = 12, availableEnd = 20),
//        Activity("ออกกำลังกาย", 1, 8, mapOf(SkillType.EXERCISE to 4), activityType = ActivityType.EXERCISE, availableStart = 0, availableEnd = 24),
//        Activity("งานพิเศษ", 4, 10, emptyMap(), moneyReward = 50, activityType = ActivityType.WORK, availableStart = 18, availableEnd = 22)
//    )
//    val activitiesWeekend = listOf(
//        Activity("งานพิเศษ (กะเช้า)", 6, 10, emptyMap(), moneyReward = 100, activityType = ActivityType.WORK, availableStart = 10, availableEnd = 16),
//        Activity("งานพิเศษ (กะเย็น)", 6, 10, emptyMap(), moneyReward = 100, activityType = ActivityType.WORK, availableStart = 16, availableEnd = 22),
//        Activity("งานพิเศษ (กะค่ำ)", 4, 10, emptyMap(), moneyReward = 100, activityType = ActivityType.WORK, availableStart = 22, availableEnd = 24),
//        Activity("วิ่ง (เช้า)", 2, 5, mapOf(SkillType.EXERCISE to 6), activityType = ActivityType.EXERCISE, availableStart = 6, availableEnd = 10),
//        Activity("ชกมวย (เย็น)", 2, 8, mapOf(SkillType.EXERCISE to 6), activityType = ActivityType.EXERCISE, availableStart = 18, availableEnd = 22)
//    )
//
//    // Initialize girls.
//    var girls = mutableStateListOf(
//        GirlCharacter(GirlType.SAME_YEAR, "มิน", ActivityType.STUDY, schedule = getDefaultGirlSchedule()),
//        GirlCharacter(GirlType.JUNIOR, "ฟ้า", ActivityType.EXERCISE, schedule = getDefaultGirlSchedule()),
//        GirlCharacter(GirlType.SENIOR, "แสง", ActivityType.CLUB, schedule = getDefaultGirlSchedule()),
//        GirlCharacter(GirlType.SEXY_PROFESSOR, "อาจารย์สุวรรณ", ActivityType.RELAX, schedule = getDefaultGirlSchedule())
//    )
//
//    // Game flow use-cases:
//    fun startDay() {
//        if (player.energy == 0) {
//            activityResultPopup = "คุณหักโหมเกินไป\nคุณเริ่มไม่สบาย ต้องพักผ่อนแล้ว"
//        } else {
//            currentScreen = GameScreen.SCHEDULE
//        }
//    }
//
//    fun nap() {
//        val oldEnergy = player.energy
//        // Use a 1-hr nap activity.
//        player = performActivity(Activity("พักผ่อน (1 ชม.)", 1, 0, emptyMap(), activityType = ActivityType.RELAX, availableStart = 0, availableEnd = 24), player)
//        currentHour = (currentHour + 1) % 24
//        if (currentHour == 0) currentDate = currentDate.plus(1, DateTimeUnit.DAY)
//        activityResultPopup = "คุณพักผ่อน 1 ชม.\nพลังงาน: +${player.energy - oldEnergy}"
//        currentScreen = GameScreen.MENU
//    }
//
//    fun sleep3() {
//        val oldEnergy = player.energy
//        player.energy = (player.energy + 30).coerceAtMost(100)
//        repeat(3) {
//            currentHour = (currentHour + 1) % 24
//            if (currentHour == 0) currentDate = currentDate.plus(1, DateTimeUnit.DAY)
//        }
//        activityResultPopup = "คุณนอน 3 ชม.\nพลังงาน: +${player.energy - oldEnergy}"
//        currentScreen = GameScreen.MENU
//    }
//
//    fun sleepUntil6() {
//        player.energy = 100
//        player.exercise += 12
//        currentDate = currentDate.plus(1, DateTimeUnit.DAY)
//        currentHour = 6
//        activityResultPopup = "คุณนอนจนถึงเช้าวันถัดไป\nพลังงานเต็มและ คะแนนออกกำลังกาย +12"
//        currentScreen = GameScreen.MENU
//    }
//
//    fun selectActivity(activity: Activity) {
//        if (currentHour !in activity.availableStart until activity.availableEnd) return
//        if (player.energy < activity.energyCost) {
//            activityResultPopup = "พลังงานไม่พอสำหรับกิจกรรมนี้"
//            return
//        }
//        val activityStartTime = currentHour
//        val oldStudy = player.study
//        val oldExercise = player.exercise
//        val oldSocial = player.social
//        val oldMoney = player.money
//        player = performActivity(activity, player)
//        val studyDiff = player.study - oldStudy
//        val exerciseDiff = player.exercise - oldExercise
//        val socialDiff = player.social - oldSocial
//        val moneyDiff = player.money - oldMoney
//        currentHour = (currentHour + activity.duration) % 24
//        if (currentHour < activity.availableStart) currentDate = currentDate.plus(1, DateTimeUnit.DAY)
//        if (activity.activityType != ActivityType.RELAX) {
//            val meetingGirls = girls.filter { girl ->
//                girl.schedule.any { slot ->
//                    activityStartTime in slot.startHour until slot.endHour &&
//                            slot.activityType == activity.activityType
//                }
//            }
//            if (meetingGirls.isNotEmpty()) {
//                pendingConversationGirls.clear()
//                pendingConversationGirls.addAll(meetingGirls)
//                currentScreen = GameScreen.CONVERSATION_SELECTION
//            } else {
//                activityResultPopup = "ผลลัพธ์กิจกรรม:\nเรียน: +$studyDiff, ออกกำลังกาย: +$exerciseDiff, สังคม: +$socialDiff, เงิน: +$moneyDiff"
//                currentScreen = GameScreen.SCHEDULE
//            }
//        } else {
//            activityResultPopup = "ผลลัพธ์กิจกรรม:\nเรียน: +$studyDiff, ออกกำลังกาย: +$exerciseDiff, สังคม: +$socialDiff, เงิน: +$moneyDiff"
//            currentScreen = GameScreen.SCHEDULE
//        }
//        if (player.energy == 0) {
//            activityResultPopup = "คุณหักโหมเกินไป\nคุณเริ่มไม่สบาย ต้องพักผ่อนแล้ว"
//            currentScreen = GameScreen.MENU
//        }
//    }
//
//    fun callGirl(girl: GirlCharacter) {
//        player.energy = (player.energy - 10).coerceAtLeast(0)
//        currentHour = (currentHour + 1) % 24
//        if (currentHour == 0) currentDate = currentDate.plus(1, DateTimeUnit.DAY)
//        conversationGirl = girl
//        currentDialogue = getDialogueForGirl(girl)
//        currentScreen = GameScreen.CONVERSATION
//    }
//}
//
//// =====================
//// UI LAYER
//// =====================
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.setValue
//import kotlinx.datetime.DayOfWeek
//import kotlinx.datetime.LocalDate
//import kotlinx.datetime.Month
//import kotlinx.datetime.plus
//import kotlinx.datetime.DateTimeUnit
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.sp
//import androidx.compose.runtime.Composable
//
//// Top-level composable that displays the appropriate screen based on the ViewModel.
//@Composable
//fun TokimekiGameScreen(viewModel: GameViewModel) {
//    when (viewModel.currentScreen) {
//        GameScreen.MENU -> MenuScreenUI(viewModel)
//        GameScreen.SCHEDULE -> ScheduleScreenUI(viewModel)
//        GameScreen.CONVERSATION_SELECTION -> ConversationSelectionScreenUI(viewModel)
//        GameScreen.CONVERSATION -> ConversationScreenUI(viewModel)
//        GameScreen.STATUS -> StatusScreenUI(viewModel)
//        GameScreen.GIFT -> GiftScreenUI(viewModel)
//    }
//    if ((viewModel.currentScreen == GameScreen.MENU || viewModel.currentScreen == GameScreen.SCHEDULE) &&
//        viewModel.activityResultPopup != null) {
//        ActivityResultPopupUI(message = viewModel.activityResultPopup!!) {
//            viewModel.activityResultPopup = null
//        }
//    }
//}
//
//@Composable
//fun MenuScreenUI(viewModel: GameViewModel) {
//    MenuScreen(
//        date = viewModel.currentDate,
//        playedDay = viewModel.currentDate.dayOfYear,
//        currentHour = viewModel.currentHour,
//        energy = viewModel.player.energy,
//        money = viewModel.player.money,
//        onStartDay = { viewModel.startDay() },
//        onViewStatus = { viewModel.currentScreen = GameScreen.STATUS },
//        onSleep = { viewModel.sleepUntil6() },
//        onNap = { viewModel.nap() },
//        onSleep3 = { viewModel.sleep3() }
//    )
//}
//
//@Composable
//fun ScheduleScreenUI(viewModel: GameViewModel) {
//    val activities = if (isWeekend(viewModel.currentDate)) viewModel.activitiesWeekend else viewModel.activitiesWeekday
//    ScheduleScreen(
//        currentDate = viewModel.currentDate,
//        currentHour = viewModel.currentHour,
//        player = viewModel.player,
//        activities = activities,
//        girls = viewModel.girls,
//        onActivitySelected = { viewModel.selectActivity(it) },
//        onConversationGirlSelected = { viewModel.callGirl(it) },
//        onBack = { viewModel.currentScreen = GameScreen.MENU }
//    )
//}
//
//@Composable
//fun ConversationSelectionScreenUI(viewModel: GameViewModel) {
//    ConversationSelectionScreen(
//        pendingGirls = viewModel.pendingConversationGirls,
//        onSelectGirl = { viewModel.callGirl(it) },
//        onSkip = {
//            viewModel.pendingConversationGirls.clear()
//            viewModel.currentScreen = GameScreen.SCHEDULE
//        }
//    )
//}
//
//@Composable
//fun ConversationScreenUI(viewModel: GameViewModel) {
//    if (viewModel.conversationGirl != null && viewModel.currentDialogue != null) {
//        ConversationScreen(
//            partnerName = viewModel.conversationGirl!!.name,
//            dialogueNode = viewModel.currentDialogue!!,
//            onOptionSelected = { option ->
//                viewModel.conversationGirl?.let { girl ->
//                    girl.affection += option.affectionChange
//                    if (option.nextNodeId == "after_proposal") {
//                        girl.relationshipStatus = RelationshipStatus.DATING
//                        viewModel.currentDialogue = afterProposalNode
//                    }
//                }
//                viewModel.conversationGirl = null
//                viewModel.currentScreen = if (viewModel.pendingConversationGirls.isNotEmpty()) GameScreen.CONVERSATION_SELECTION else GameScreen.SCHEDULE
//            },
//            onSkip = {
//                viewModel.conversationGirl = null
//                viewModel.currentScreen = if (viewModel.pendingConversationGirls.isNotEmpty()) GameScreen.CONVERSATION_SELECTION else GameScreen.SCHEDULE
//            }
//        )
//    }
//}
//
//@Composable
//fun StatusScreenUI(viewModel: GameViewModel) {
//    StatusScreen(
//        date = viewModel.currentDate,
//        currentHour = viewModel.currentHour,
//        player = viewModel.player,
//        girls = viewModel.girls,
//        onBack = { viewModel.currentScreen = GameScreen.MENU },
//        onBuyGift = { viewModel.currentScreen = GameScreen.GIFT }
//    )
//}
//
//@Composable
//fun GiftScreenUI(viewModel: GameViewModel) {
//    GiftScreen(
//        player = viewModel.player,
//        girls = viewModel.girls,
//        onGiftPurchased = { cost, girl ->
//            if (viewModel.player.money >= cost) {
//                viewModel.player.money -= cost
//                girl.affection += 15
//            }
//            viewModel.currentScreen = GameScreen.STATUS
//        },
//        onBack = { viewModel.currentScreen = GameScreen.STATUS }
//    )
//}
//
//@Composable
//fun ActivityResultPopupUI(message: String, onDismiss: () -> Unit) {
//    ActivityResultPopup(message = message, onDismiss = onDismiss)
//}
