///**
// * Tokimeki Game Demo – Final Code Sample
// *
// * Game Duration: 100 days starting from Monday, January 1 (using kotlinx-datetime).
// *
// * Changes implemented:
// * 1. In the ScheduleScreen activity list, each activity item is now displayed in two rows:
// *    - The first row shows the availability icon and the activity name.
// *    - The second row shows details: energy cost, the wait time until start (or "เริ่มทันที"), and duration.
// * 2. The activity summary (result or warning popup) is shown when a result message exists on either MENU or SCHEDULE.
// * 3. Other previously implemented features remain.
// */
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import kotlinx.datetime.*
//import kotlinx.datetime.DayOfWeek
//import kotlinx.datetime.Month
//
//// ──────────────────────────────
//// HELPER: Convert DayOfWeek to Thai abbreviation
//// ──────────────────────────────
//
//fun getDayOfWeekThai(dayOfWeek: DayOfWeek): String = when(dayOfWeek) {
//    DayOfWeek.MONDAY -> "จันทร์"
//    DayOfWeek.TUESDAY -> "อังคาร"
//    DayOfWeek.WEDNESDAY -> "พุธ"
//    DayOfWeek.THURSDAY -> "พฤหัสบดี"
//    DayOfWeek.FRIDAY -> "ศุกร์"
//    DayOfWeek.SATURDAY -> "เสาร์"
//    DayOfWeek.SUNDAY -> "อาทิตย์"
//    else -> TODO()
//}
//
//// ──────────────────────────────
//// ENUMERATIONS & DATA MODELS
//// ──────────────────────────────
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
//    val personality: ActivityType, // For simplicity, her primary interest
//    var affection: Int = 0,
//    var schedule: List<TimeSlot> = listOf(),
//    var relationshipStatus: RelationshipStatus = RelationshipStatus.FRIEND,
//    var hasTalked: Boolean = false // Indicates if the player has talked with her before
//)
//
//data class Activity(
//    val name: String,
//    val duration: Int,          // in hours
//    val energyCost: Int,
//    val skillEffects: Map<SkillType, Int>,
//    val bonusAffection: Int = 0,  // bonus if activity matches a girl's schedule
//    val activityType: ActivityType,
//    val availableStart: Int,     // inclusive start time
//    val availableEnd: Int,       // exclusive end time
//    val moneyReward: Int = 0     // money earned (if any)
//)
//
//data class DialogueOption(
//    val text: String,
//    val affectionChange: Int,
//    val nextNodeId: String? = null
//)
//
//data class DialogueNode(
//    val id: String,
//    val dialogueText: String,
//    val options: List<DialogueOption>
//)
//
//enum class GameScreen { MENU, SCHEDULE, CONVERSATION_SELECTION, CONVERSATION, STATUS, GIFT }
//
//// ──────────────────────────────
//// DATE & HOLIDAY HELPER FUNCTIONS (using kotlinx-datetime)
//// ──────────────────────────────
//
//fun isHoliday(date: LocalDate): Boolean {
//    val sampleHolidays = listOf(
//        LocalDate(2024, 1, 15),
//        LocalDate(2024, 2, 5)
//    )
//    return date in sampleHolidays
//}
//
//fun isWeekend(date: LocalDate): Boolean =
//    date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
//
//fun isWeekday(date: LocalDate): Boolean = !isWeekend(date)
//
//fun daysInMonth(date: LocalDate): Int = when(date.month) {
//    Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY, Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
//    Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
//    Month.FEBRUARY -> if ((date.year % 4 == 0 && date.year % 100 != 0) || (date.year % 400 == 0)) 29 else 28
//    else -> 30
//}
//
//fun isLastDayOfMonth(date: LocalDate): Boolean = date.dayOfMonth == daysInMonth(date)
//
//// ──────────────────────────────
//// ACTIVITY DEFINITIONS (SAMPLE)
//// ──────────────────────────────
//
//// Weekday activities:
//val morningClass = Activity(
//    name = "เรียน (เช้า)",
//    duration = 2,
//    energyCost = 5,
//    skillEffects = mapOf(SkillType.STUDY to 5),
//    activityType = ActivityType.STUDY,
//    availableStart = 8,
//    availableEnd = 12
//)
//val afternoonClass = Activity(
//    name = "เรียน (บ่าย)",
//    duration = 2,
//    energyCost = 5,
//    skillEffects = mapOf(SkillType.STUDY to 5),
//    activityType = ActivityType.STUDY,
//    availableStart = 14,
//    availableEnd = 18
//)
//val clubActivity = Activity(
//    name = "ชมรม",
//    duration = 2,
//    energyCost = 4,
//    skillEffects = mapOf(SkillType.SOCIAL to 4),
//    activityType = ActivityType.CLUB,
//    availableStart = 12,
//    availableEnd = 20
//)
//val exerciseActivity = Activity(
//    name = "ออกกำลังกาย",
//    duration = 1,
//    energyCost = 8,
//    skillEffects = mapOf(SkillType.EXERCISE to 4),
//    activityType = ActivityType.EXERCISE,
//    availableStart = 0,
//    availableEnd = 24
//)
//val workWeekday = Activity(
//    name = "งานพิเศษ (วันจันทร์-ศุกร์)",
//    duration = 4,
//    energyCost = 10,
//    skillEffects = emptyMap(),
//    activityType = ActivityType.WORK,
//    availableStart = 18,
//    availableEnd = 22,
//    moneyReward = 50
//)
//// Weekend additional activities:
//val workWeekendMorning = Activity(
//    name = "งานพิเศษ (เสาร์-อาทิตย์ กะเช้า)",
//    duration = 6,
//    energyCost = 10,
//    skillEffects = emptyMap(),
//    activityType = ActivityType.WORK,
//    availableStart = 10,
//    availableEnd = 16,
//    moneyReward = 100
//)
//val workWeekendEvening = Activity(
//    name = "งานพิเศษ (เสาร์-อาทิตย์ กะเย็น)",
//    duration = 6,
//    energyCost = 10,
//    skillEffects = emptyMap(),
//    activityType = ActivityType.WORK,
//    availableStart = 16,
//    availableEnd = 22,
//    moneyReward = 100
//)
//val workWeekendNight = Activity(
//    name = "งานพิเศษ (เสาร์-อาทิตย์ กะค่ำ)",
//    duration = 4,
//    energyCost = 10,
//    skillEffects = emptyMap(),
//    activityType = ActivityType.WORK,
//    availableStart = 22,
//    availableEnd = 24,
//    moneyReward = 100
//)
//val runningActivity = Activity(
//    name = "วิ่ง (เช้า)",
//    duration = 2,
//    energyCost = 5,
//    skillEffects = mapOf(SkillType.EXERCISE to 6),
//    activityType = ActivityType.EXERCISE,
//    availableStart = 6,
//    availableEnd = 10
//)
//val boxingActivity = Activity(
//    name = "ชกมวย (เย็น)",
//    duration = 2,
//    energyCost = 8,
//    skillEffects = mapOf(SkillType.EXERCISE to 6),
//    activityType = ActivityType.EXERCISE,
//    availableStart = 18,
//    availableEnd = 22
//)
//// In ScheduleScreen, only a 1-hr nap option is provided.
//val napActivity = Activity(
//    name = "พักผ่อน (1 ชม.)",
//    duration = 1,
//    energyCost = 0,
//    skillEffects = emptyMap(), // Restores 10% energy
//    activityType = ActivityType.RELAX,
//    availableStart = 0,
//    availableEnd = 24
//)
//// Full sleep for recovery in MenuScreen ("นอนจนถึง 6 โมงเช้าวันถัดไป") is handled via onSleep.
//// New option: "นอน (3 ชม.)" in MenuScreen to recover 30 energy is handled via onSleep3.
//
//// ──────────────────────────────
//// DIALOGUE TREE FUNCTIONS FOR GIRLS
//// ──────────────────────────────
//
//fun getDialogueForGirl(girl: GirlCharacter): DialogueNode {
//    return when {
//        girl.relationshipStatus == RelationshipStatus.DATING -> {
//            DialogueNode(
//                id = "in_relationship",
//                dialogueText = "หวัดดีที่รัก ${girl.name}, วันนี้เป็นอย่างไรบ้าง?",
//                options = listOf(
//                    DialogueOption(text = "สบายดี", affectionChange = 2),
//                    DialogueOption(text = "เหนื่อย", affectionChange = -2)
//                )
//            )
//        }
//        girl.affection >= 50 -> {
//            DialogueNode(
//                id = "proposal",
//                dialogueText = "${girl.name} รู้สึกสนิทกับคุณมากแล้วนะ... ขอเป็นแฟนกันได้ไหม?",
//                options = listOf(
//                    DialogueOption(text = "รับ", affectionChange = 10, nextNodeId = "after_proposal"),
//                    DialogueOption(text = "ไม่รับ", affectionChange = -10)
//                )
//            )
//        }
//        girl.affection >= 20 -> {
//            DialogueNode(
//                id = "activity_meeting",
//                dialogueText = "เจอกันอีกแล้วนะ ${girl.name}! วันนี้คุณทำกิจกรรมอะไรอยู่?",
//                options = listOf(
//                    DialogueOption(text = "แค่เดินผ่านมา", affectionChange = 0),
//                    DialogueOption(text = "สวัสดีค่ะ", affectionChange = 5)
//                )
//            )
//        }
//        else -> {
//            DialogueNode(
//                id = "first_meeting",
//                dialogueText = "สวัสดีค่ะ ${girl.name}, นี่เป็นครั้งแรกที่เจอกันใช่ไหม?",
//                options = listOf(
//                    DialogueOption(text = "ผมเป็นนักศึกษา", affectionChange = 5),
//                    DialogueOption(text = "แค่ผ่านไป", affectionChange = -3)
//                )
//            )
//        }
//    }
//}
//
//val afterProposalNode = DialogueNode(
//    id = "after_proposal",
//    dialogueText = "เยี่ยมเลย! ตอนนี้เราเป็นแฟนกันแล้วนะ",
//    options = listOf(
//        DialogueOption(text = "ขอบคุณค่ะ", affectionChange = 5)
//    )
//)
//
//// ──────────────────────────────
//// HELPER FUNCTIONS: generateScheduleForGirl & performActivity
//// ──────────────────────────────
//
//fun generateScheduleForGirl(girl: GirlCharacter): List<TimeSlot> {
//    // For demo purposes, return a fixed schedule.
//    return listOf(
//        TimeSlot(startHour = 10, endHour = 12, activityType = ActivityType.STUDY, location = "Library"),
//        TimeSlot(startHour = 14, endHour = 16, activityType = ActivityType.EXERCISE, location = "Gym"),
//        TimeSlot(startHour = 18, endHour = 20, activityType = ActivityType.CLUB, location = "Café")
//    )
//}
//
//fun performActivity(activity: Activity, player: Player): Player {
//    return if (activity.activityType == ActivityType.RELAX && activity.name.contains("พักผ่อน")) {
//        // For a 1-hr nap, restore 10% energy.
//        val restorePercentage = 10
//        val restoreAmount = (100 * restorePercentage) / 100
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
//// ──────────────────────────────
//// ACTIVITY RESULT POPUP COMPOSABLE
//// ──────────────────────────────
//
//@Composable
//fun ActivityResultPopup(message: String, onDismiss: () -> Unit) {
//    Box(modifier = Modifier
//        .fillMaxSize()
//        .background(Color(0x88000000)),
//        contentAlignment = Alignment.Center) {
//        Column(modifier = Modifier
//            .background(Color.White)
//            .padding(16.dp)) {
//            Text(message, color = Color.Black)
//            Spacer(modifier = Modifier.height(12.dp))
//            Text("ตกลง", modifier = Modifier
//                .clickable { onDismiss() }
//                .padding(8.dp), color = Color.Blue)
//        }
//    }
//}
//
//// ──────────────────────────────
//// MAIN COMPOSABLE & UI COMPONENTS
//// ──────────────────────────────
//
//@Composable
//fun TokimekiGameDemo() {
//    // Global game state
//    var currentScreen by remember { mutableStateOf(GameScreen.MENU) }
//    var currentDate by remember { mutableStateOf(LocalDate(2024, 1, 1)) }  // Start date: Jan 1, 2024
//    var currentHour by remember { mutableStateOf(6) }  // Start at 06:00
//    var player by remember { mutableStateOf(Player()) }
//    var conversationGirl by remember { mutableStateOf<GirlCharacter?>(null) }
//    var currentDialogue by remember { mutableStateOf<DialogueNode?>(null) }
//    var pendingConversationGirls by remember { mutableStateOf(listOf<GirlCharacter>()) }
//    var activityResultPopup by remember { mutableStateOf<String?>(null) }
//
//    // End game after 100 days.
//    val maxDays = 100
//    // Compute played day (assumes starting at day 1).
//    val playedDay = currentDate.dayOfYear
//
//    // Initialize girls.
//    val girls = remember {
//        mutableStateListOf(
//            GirlCharacter(GirlType.SAME_YEAR, "มิน", ActivityType.STUDY).apply {
//                schedule = generateScheduleForGirl(this)
//            },
//            GirlCharacter(GirlType.JUNIOR, "ฟ้า", ActivityType.EXERCISE).apply {
//                schedule = generateScheduleForGirl(this)
//            },
//            GirlCharacter(GirlType.SENIOR, "แสง", ActivityType.CLUB).apply {
//                schedule = generateScheduleForGirl(this)
//            },
//            GirlCharacter(GirlType.SEXY_PROFESSOR, "อาจารย์สุวรรณ", ActivityType.RELAX).apply {
//                schedule = generateScheduleForGirl(this)
//            }
//        )
//    }
//
//    // Choose work activity based on weekday/weekend.
//    val workActivity = if (isWeekday(currentDate)) workWeekday else workWeekendMorning
//    // Build available activities.
//    val activities = if (isWeekend(currentDate)) {
//        listOf(
//            workWeekendMorning,
//            workWeekendEvening,
//            workWeekendNight,
//            runningActivity,
//            boxingActivity,
//            clubActivity,
//            exerciseActivity
//        )
//    } else {
//        listOf(
//            morningClass,
//            afternoonClass,
//            clubActivity,
//            exerciseActivity,
//            workActivity
//        )
//    }
//
//    // Exam event: on the last day of the month (if not a holiday), add bonus.
//    if (isLastDayOfMonth(currentDate) && !isHoliday(currentDate)) {
//        player.money += player.study / 10
//    }
//
//    Box(modifier = Modifier
//        .fillMaxSize()
//        .background(Color(0xFF202020))
//        .padding(16.dp)) {
//        when (currentScreen) {
//            GameScreen.MENU -> {
//                MenuScreen(
//                    date = currentDate,
//                    playedDay = playedDay,
//                    currentHour = currentHour,
//                    energy = player.energy,
//                    money = player.money,
//                    onStartDay = {
//                        if (player.energy == 0) {
//                            activityResultPopup = "คุณหักโหมเกินไป\nคุณเริ่มไม่สบาย ต้องพักผ่อนแล้ว"
//                        } else {
//                            currentScreen = GameScreen.SCHEDULE
//                        }
//                    },
//                    onViewStatus = { currentScreen = GameScreen.STATUS },
//                    onSleep = {
//                        // Full sleep until 6:00 AM next day.
//                        player.energy = 100
//                        player.exercise += 12
//                        currentDate = currentDate.plus(1, DateTimeUnit.DAY)
//                        currentHour = 6
//                        activityResultPopup = "คุณนอนจนถึงเช้าวันถัดไป\nพลังงานเต็มและ คะแนนออกกำลังกาย +12"
//                    },
//                    onNap = {
//                        // 1-hr nap: restore 10% energy and advance time by 1 hr.
//                        val oldEnergy = player.energy
//                        player = performActivity(napActivity, player)
//                        currentHour = (currentHour + 1) % 24
//                        if (currentHour == 0) {
//                            currentDate = currentDate.plus(1, DateTimeUnit.DAY)
//                        }
//                        activityResultPopup = "คุณพักผ่อน 1 ชม.\nพลังงาน: +${player.energy - oldEnergy}"
//                    },
//                    onSleep3 = {
//                        // Sleep 3 hrs: recover 30 energy and advance time by 3 hrs.
//                        val oldEnergy = player.energy
//                        player.energy = (player.energy + 30).coerceAtMost(100)
//                        repeat(3) {
//                            currentHour = (currentHour + 1) % 24
//                            if (currentHour == 0) {
//                                currentDate = currentDate.plus(1, DateTimeUnit.DAY)
//                            }
//                        }
//                        activityResultPopup = "คุณนอน 3 ชม.\nพลังงาน: +${player.energy - oldEnergy}"
//                    }
//                )
//            }
//            GameScreen.SCHEDULE -> {
//                ScheduleScreen(
//                    currentDate = currentDate,
//                    currentHour = currentHour,
//                    player = player,
//                    activities = activities,
//                    girls = girls,
//                    onActivitySelected = { activity ->
//                        if (currentHour !in activity.availableStart until activity.availableEnd) return@ScheduleScreen
//                        if (player.energy < activity.energyCost) {
//                            activityResultPopup = "พลังงานไม่พอสำหรับกิจกรรมนี้"
//                            return@ScheduleScreen
//                        }
//                        val activityStartTime = currentHour
//                        // Capture old stats for result summary.
//                        val oldStudy = player.study
//                        val oldExercise = player.exercise
//                        val oldSocial = player.social
//                        val oldMoney = player.money
//                        player = performActivity(activity, player)
//                        val studyDiff = player.study - oldStudy
//                        val exerciseDiff = player.exercise - oldExercise
//                        val socialDiff = player.social - oldSocial
//                        val moneyDiff = player.money - oldMoney
//                        // Before showing result popup, if there are meeting girls, show conversation first.
//                        val newHour = currentHour + activity.duration
//                        currentHour = newHour % 24
//                        if (newHour >= 24) {
//                            currentDate = currentDate.plus(1, DateTimeUnit.DAY)
//                        }
//                        if (activity.activityType != ActivityType.RELAX) {
//                            val meetingGirls = girls.filter { girl ->
//                                girl.schedule.any { slot ->
//                                    activityStartTime in slot.startHour until slot.endHour &&
//                                            slot.activityType == activity.activityType
//                                }
//                            }
//                            if (meetingGirls.isNotEmpty()) {
//                                pendingConversationGirls = meetingGirls
//                                currentScreen = GameScreen.CONVERSATION_SELECTION
//                            } else {
//                                currentScreen = GameScreen.SCHEDULE
//                            }
//                        } else {
//                            currentScreen = GameScreen.SCHEDULE
//                        }
//                        if (player.energy == 0) {
//                            activityResultPopup = "คุณหักโหมเกินไป\nคุณเริ่มไม่สบาย ต้องพักผ่อนแล้ว"
//                            currentScreen = GameScreen.MENU
//                        }
//                    },
//                    onConversationGirlSelected = { selectedGirl ->
//                        // When a call is initiated, subtract 10 energy and advance time by 1 hr.
//                        player.energy = (player.energy - 10).coerceAtLeast(0)
//                        currentHour = (currentHour + 1) % 24
//                        if (currentHour == 0) {
//                            currentDate = currentDate.plus(1, DateTimeUnit.DAY)
//                        }
//                        conversationGirl = selectedGirl
//                        currentDialogue = getDialogueForGirl(selectedGirl)
//                        currentScreen = GameScreen.CONVERSATION
//                    },
//                    onBack = { currentScreen = GameScreen.MENU }
//                )
//            }
//            GameScreen.CONVERSATION_SELECTION -> {
//                ConversationSelectionScreen(
//                    pendingGirls = pendingConversationGirls,
//                    onSelectGirl = { selectedGirl ->
//                        conversationGirl = selectedGirl
//                        currentDialogue = getDialogueForGirl(selectedGirl)
//                        selectedGirl.hasTalked = true
//                        pendingConversationGirls = pendingConversationGirls.filter { it != selectedGirl }
//                        currentScreen = GameScreen.CONVERSATION
//                    },
//                    onSkip = {
//                        pendingConversationGirls = emptyList()
//                        currentScreen = GameScreen.SCHEDULE
//                    }
//                )
//            }
//            GameScreen.CONVERSATION -> {
//                if (conversationGirl != null && currentDialogue != null) {
//                    ConversationScreen(
//                        partnerName = conversationGirl!!.name,
//                        dialogueNode = currentDialogue!!,
//                        onOptionSelected = { option ->
//                            conversationGirl?.let { girl ->
//                                girl.affection += option.affectionChange
//                                if (option.nextNodeId == "after_proposal") {
//                                    girl.relationshipStatus = RelationshipStatus.DATING
//                                    currentDialogue = afterProposalNode
//                                }
//                            }
//                            conversationGirl = null
//                            currentScreen = if (pendingConversationGirls.isNotEmpty()) GameScreen.CONVERSATION_SELECTION else GameScreen.SCHEDULE
//                        },
//                        onSkip = {
//                            conversationGirl = null
//                            currentScreen = if (pendingConversationGirls.isNotEmpty()) GameScreen.CONVERSATION_SELECTION else GameScreen.SCHEDULE
//                        }
//                    )
//                } else {
//                    currentScreen = GameScreen.SCHEDULE
//                }
//            }
//            GameScreen.STATUS -> {
//                StatusScreen(
//                    date = currentDate,
//                    currentHour = currentHour,
//                    player = player,
//                    girls = girls,
//                    onBack = { currentScreen = GameScreen.MENU },
//                    onBuyGift = { currentScreen = GameScreen.GIFT }
//                )
//            }
//            GameScreen.GIFT -> {
//                GiftScreen(
//                    player = player,
//                    girls = girls,
//                    onGiftPurchased = { cost, girl ->
//                        if (player.money >= cost) {
//                            player.money -= cost
//                            girl.affection += 15
//                        }
//                        currentScreen = GameScreen.STATUS
//                    },
//                    onBack = { currentScreen = GameScreen.STATUS }
//                )
//            }
//        }
//        // Show ActivityResultPopup if a result message exists (on MENU or SCHEDULE).
//        if ((currentScreen == GameScreen.MENU || currentScreen == GameScreen.SCHEDULE) && activityResultPopup != null) {
//            ActivityResultPopup(message = activityResultPopup!!) {
//                activityResultPopup = null
//            }
//        }
//    }
//}
//
//@Composable
//fun MenuScreen(
//    date: LocalDate,
//    playedDay: Int,
//    currentHour: Int,
//    energy: Int,
//    money: Int,
//    onStartDay: () -> Unit,
//    onViewStatus: () -> Unit,
//    onSleep: () -> Unit,
//    onNap: () -> Unit,
//    onSleep3: () -> Unit
//) {
//    Box(modifier = Modifier.fillMaxSize()) {
//        Column(modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally) {
//            Text("Tokimeki Demo", style = MaterialTheme.typography.headlineMedium, color = Color.White)
//            Spacer(modifier = Modifier.height(8.dp))
//            Text("วันที่เล่น: Day $playedDay", color = Color.Magenta)
//            Spacer(modifier = Modifier.height(8.dp))
//            Text("วันที่: $date (${getDayOfWeekThai(date.dayOfWeek)})", color = Color.Cyan)
//            Text("เวลาปัจจุบัน: $currentHour:00", color = Color.Cyan)
//            Text("พลังงาน: $energy", color = Color.Green)
//            Text("เงิน: $money", color = Color.Yellow)
//            Spacer(modifier = Modifier.height(24.dp))
//            // "เริ่มกิจกรรม" button at the very top.
//            Text("เริ่มกิจกรรม", modifier = Modifier.clickable {
//                if (energy == 0) {
//                    // Energy 0 warning handled in onStartDay.
//                } else {
//                    onStartDay()
//                }
//            }.padding(8.dp), color = Color.Yellow)
//            Spacer(modifier = Modifier.height(12.dp))
//            // Sleep options.
//            Text("พักผ่อน (1 ชม.)", modifier = Modifier.clickable { onNap() }.padding(8.dp), color = Color.Blue)
//            Spacer(modifier = Modifier.height(12.dp))
//            Text("นอน (3 ชม.)", modifier = Modifier.clickable { onSleep3() }.padding(8.dp), color = Color.Blue)
//            Spacer(modifier = Modifier.height(12.dp))
//            Text("นอนจนถึง 6 โมงเช้าวันถัดไป", modifier = Modifier.clickable { onSleep() }.padding(8.dp), color = Color.Blue)
//            Spacer(modifier = Modifier.height(16.dp))
//            Text("ดูสถานะ", modifier = Modifier.clickable { onViewStatus() }.padding(8.dp), color = Color.Magenta)
//        }
//        // Back button at top-right.
//        Text("กลับ", modifier = Modifier
//            .align(Alignment.TopEnd)
//            .clickable { /* Back action if needed in MenuScreen */ }
//            .padding(8.dp), color = Color.Red)
//    }
//}
//
//@Composable
//fun ScheduleScreen(
//    currentDate: LocalDate,
//    currentHour: Int,
//    player: Player,
//    activities: List<Activity>,
//    girls: List<GirlCharacter>,
//    onActivitySelected: (Activity) -> Unit,
//    onConversationGirlSelected: (GirlCharacter) -> Unit,
//    onBack: () -> Unit
//) {
//    Box(modifier = Modifier.fillMaxSize()) {
//        Column(modifier = Modifier.fillMaxSize()) {
//            // Header: date, day-of-week, current time, energy, and money.
//            Text("วันที่: $currentDate (${getDayOfWeekThai(currentDate.dayOfWeek)})   เวลาปัจจุบัน: $currentHour:00   พลังงาน: ${player.energy}   เงิน: ${player.money}",
//                color = Color.White, modifier = Modifier.padding(8.dp))
//            Text("ตารางของสาว", color = Color.White, modifier = Modifier.padding(8.dp))
//            girls.forEach { girl ->
//                val currentSlot = girl.schedule.find { currentHour in it.startHour until it.endHour }
//                val statusText = if (currentSlot != null) "กำลัง ${currentSlot.activityType} ที่ ${currentSlot.location}" else "ว่าง"
//                Row(modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(4.dp)
//                    .background(Color.DarkGray)) {
//                    Text("${girl.name}: ", color = Color.Yellow, modifier = Modifier.weight(1f).padding(8.dp))
//                    Text(statusText, color = Color.White, modifier = Modifier.padding(8.dp))
//                    // If girl is free, add a call button.
//                    if (currentSlot == null) {
//                        val iconColor = if (girl.hasTalked) Color.Green else Color.Gray
//                        Text("☎", color = iconColor, modifier = Modifier
//                            .clickable(enabled = girl.hasTalked) { onConversationGirlSelected(girl) }
//                            .padding(8.dp))
//                    }
//                }
//            }
//            Spacer(modifier = Modifier.height(16.dp))
//            Text("กิจกรรมของคุณ", color = Color.White, modifier = Modifier.padding(8.dp))
//            // Each activity item is now shown as two rows.
//            activities.forEach { activity ->
//                val available = currentHour in activity.availableStart until activity.availableEnd
//                val availabilityIcon = if (available) "✓" else "✗"
//                val hoursUntilStart = if (currentHour < activity.availableStart) activity.availableStart - currentHour else 0
//                val startText = if (hoursUntilStart > 0) "เริ่มในอีก $hoursUntilStart ชม." else "เริ่มทันที"
//                Column(modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable { if (available) onActivitySelected(activity) }
//                    .padding(8.dp)
//                    .background(Color.Gray)
//                ) {
//                    Row(modifier = Modifier.fillMaxWidth()) {
//                        Text(availabilityIcon, color = if (available) Color.Green else Color.Red, modifier = Modifier.padding(8.dp))
//                        Text(activity.name, color = Color.White, modifier = Modifier.weight(1f))
//                    }
//                    Row(modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) {
//                        Text("พลังงาน: ${activity.energyCost}", color = Color.LightGray)
//                        Spacer(modifier = Modifier.width(16.dp))
//                        Text(startText, color = Color.LightGray)
//                        Spacer(modifier = Modifier.width(16.dp))
//                        Text("ใช้เวลา: ${activity.duration} ชม.", color = Color.LightGray)
//                    }
//                }
//                Spacer(modifier = Modifier.height(8.dp))
//            }
//            Spacer(modifier = Modifier.height(16.dp))
//        }
//        // Place the Back button at top-right.
//        Text("กลับ", modifier = Modifier
//            .align(Alignment.TopEnd)
//            .clickable { onBack() }
//            .padding(8.dp), color = Color.Red)
//    }
//}
//
//@Composable
//fun ConversationSelectionScreen(
//    pendingGirls: List<GirlCharacter>,
//    onSelectGirl: (GirlCharacter) -> Unit,
//    onSkip: () -> Unit
//) {
//    Box(modifier = Modifier.fillMaxSize().background(Color(0x88000000)), contentAlignment = Alignment.Center) {
//        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
//            Text("คุณพบกับหลายคน!", color = Color.White, style = MaterialTheme.typography.headlineSmall)
//            Spacer(modifier = Modifier.height(12.dp))
//            Text("เลือกคุยกับใครก่อน:", color = Color.White)
//            Spacer(modifier = Modifier.height(16.dp))
//            pendingGirls.forEach { girl ->
//                Text(girl.name, color = Color.Cyan, modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable { onSelectGirl(girl) }
//                    .padding(12.dp)
//                    .background(Color.DarkGray))
//                Spacer(modifier = Modifier.height(8.dp))
//            }
//            Spacer(modifier = Modifier.height(16.dp))
//            Text("ข้ามการคุย", modifier = Modifier.clickable { onSkip() }.padding(12.dp), color = Color.Yellow)
//        }
//    }
//}
//
//@Composable
//fun ConversationScreen(
//    partnerName: String,
//    dialogueNode: DialogueNode,
//    onOptionSelected: (DialogueOption) -> Unit,
//    onSkip: () -> Unit
//) {
//    Box(modifier = Modifier.fillMaxSize()) {
//        Column(modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)) {
//            Text("สนทนากับ $partnerName", color = Color.Yellow, style = MaterialTheme.typography.headlineSmall)
//            Spacer(modifier = Modifier.height(12.dp))
//            Text(dialogueNode.dialogueText, color = Color.White)
//            Spacer(modifier = Modifier.height(16.dp))
//            dialogueNode.options.forEach { option ->
//                Text(option.text, modifier = Modifier
//                    .clickable { onOptionSelected(option) }
//                    .padding(8.dp), color = Color.Cyan)
//                Spacer(modifier = Modifier.height(8.dp))
//            }
//            Spacer(modifier = Modifier.height(24.dp))
//            Text("ข้ามการสนทนา", modifier = Modifier.clickable { onSkip() }.padding(8.dp), color = Color.Yellow)
//        }
//        // Back button at top-right.
//        Text("กลับ", modifier = Modifier
//            .align(Alignment.TopEnd)
//            .clickable { onSkip() }
//            .padding(8.dp), color = Color.Red)
//    }
//}
//
//@Composable
//fun StatusScreen(
//    date: LocalDate,
//    currentHour: Int,
//    player: Player,
//    girls: List<GirlCharacter>,
//    onBack: () -> Unit,
//    onBuyGift: () -> Unit
//) {
//    Box(modifier = Modifier.fillMaxSize()) {
//        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//            Text("สถานะเกม", color = Color.White)
//            Text("วันที่: $date (${getDayOfWeekThai(date.dayOfWeek)})", color = Color.Cyan)
//            Text("เวลาปัจจุบัน: $currentHour:00", color = Color.Cyan)
//            Text("พลังงาน: ${player.energy}", color = Color.Green)
//            Text("เงิน: ${player.money}", color = Color.Yellow)
//            Spacer(modifier = Modifier.height(16.dp))
//            Text("ทักษะ:", color = Color.LightGray)
//            Text("เรียน: ${player.study}", color = Color.White)
//            Text("ออกกำลังกาย: ${player.exercise}", color = Color.White)
//            Text("สังคม: ${player.social}", color = Color.White)
//            Spacer(modifier = Modifier.height(16.dp))
//            Text("สถานะสาว:", color = Color.Magenta)
//            girls.forEach { girl ->
//                Text("${girl.name} (Affection: ${girl.affection}, Status: ${girl.relationshipStatus})", color = Color.White)
//            }
//            Spacer(modifier = Modifier.height(24.dp))
//            Text("ซื้อของขวัญ", modifier = Modifier.clickable { onBuyGift() }.padding(8.dp), color = Color.Cyan)
//            Spacer(modifier = Modifier.height(16.dp))
//        }
//        // Back button at top-right.
//        Text("กลับ", modifier = Modifier
//            .align(Alignment.TopEnd)
//            .clickable { onBack() }
//            .padding(8.dp), color = Color.Red)
//    }
//}
//
//@Composable
//fun GiftScreen(
//    player: Player,
//    girls: List<GirlCharacter>,
//    onGiftPurchased: (cost: Int, girl: GirlCharacter) -> Unit,
//    onBack: () -> Unit
//) {
//    Box(modifier = Modifier.fillMaxSize()) {
//        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//            Text("ซื้อของขวัญ (ราคา: 100)", color = Color.White)
//            Spacer(modifier = Modifier.height(16.dp))
//            girls.forEach { girl ->
//                Row(modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable { onGiftPurchased(100, girl) }
//                    .padding(8.dp)
//                    .background(Color.DarkGray)) {
//                    Text("ซื้อของขวัญให้ ${girl.name}", color = Color.White, modifier = Modifier.weight(1f))
//                }
//                Spacer(modifier = Modifier.height(8.dp))
//            }
//            Spacer(modifier = Modifier.height(24.dp))
//        }
//        // Back button at top-right.
//        Text("กลับ", modifier = Modifier
//            .align(Alignment.TopEnd)
//            .clickable { onBack() }
//            .padding(8.dp), color = Color.Red)
//    }
//}
