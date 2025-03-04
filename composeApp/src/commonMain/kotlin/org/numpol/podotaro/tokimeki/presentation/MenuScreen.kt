package org.numpol.podotaro.tokimeki.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import org.numpol.podotaro.tokimeki.domain.Activity
import org.numpol.podotaro.tokimeki.domain.GirlCharacter
import org.numpol.podotaro.tokimeki.domain.Player
import org.numpol.podotaro.tokimeki.domain.RelationshipStatus
import org.numpol.podotaro.tokimeki.domain.getDayOfWeekThai

// 1. DialogueNode (and DialogueOption)
data class DialogueOption(
    val text: String,
    val affectionChange: Int,
    val nextNodeId: String? = null
)

data class DialogueNode(
    val id: String,
    val dialogueText: String,
    val options: List<DialogueOption>
)


// 2. getDialogueForGirl
fun getDialogueForGirl(girl: GirlCharacter): DialogueNode {
    return when {
        girl.relationshipStatus == RelationshipStatus.DATING -> {
            DialogueNode(
                id = "in_relationship",
                dialogueText = "หวัดดีที่รัก ${girl.name}, วันนี้เป็นอย่างไรบ้าง?",
                options = listOf(
                    DialogueOption(text = "สบายดี", affectionChange = 2),
                    DialogueOption(text = "เหนื่อย", affectionChange = -2)
                )
            )
        }
        girl.affection >= 50 -> {
            DialogueNode(
                id = "proposal",
                dialogueText = "${girl.name} รู้สึกสนิทกับคุณมากแล้วนะ... ขอเป็นแฟนกันได้ไหม?",
                options = listOf(
                    DialogueOption(text = "รับ", affectionChange = 10, nextNodeId = "after_proposal"),
                    DialogueOption(text = "ไม่รับ", affectionChange = -10)
                )
            )
        }
        girl.affection >= 20 -> {
            DialogueNode(
                id = "activity_meeting",
                dialogueText = "เจอกันอีกแล้วนะ ${girl.name}! วันนี้คุณทำกิจกรรมอะไรอยู่?",
                options = listOf(
                    DialogueOption(text = "แค่เดินผ่านมา", affectionChange = 0),
                    DialogueOption(text = "สวัสดีค่ะ", affectionChange = 5)
                )
            )
        }
        else -> {
            DialogueNode(
                id = "first_meeting",
                dialogueText = "สวัสดีค่ะ ${girl.name}, นี่เป็นครั้งแรกที่เจอกันใช่ไหม?",
                options = listOf(
                    DialogueOption(text = "ผมเป็นนักศึกษา", affectionChange = 5),
                    DialogueOption(text = "แค่ผ่านไป", affectionChange = -3)
                )
            )
        }
    }
}


// 3. MenuScreen
// Displays date, played day, current time, energy, money, and buttons.
@Composable
fun MenuScreen(
    date: LocalDate,
    playedDay: Int,
    currentHour: Int,
    energy: Int,
    money: Int,
    onStartDay: () -> Unit,
    onViewStatus: () -> Unit,
    onSleep: () -> Unit,
    onNap: () -> Unit,
    onSleep3: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tokimeki Demo", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text("วันที่เล่น: Day $playedDay", color = Color.Magenta)
            Spacer(modifier = Modifier.height(8.dp))
            Text("วันที่: $date (${getDayOfWeekThai(date.dayOfWeek)})", color = Color.Cyan)
            Text("เวลาปัจจุบัน: $currentHour:00", color = Color.Cyan)
            Text("พลังงาน: $energy", color = Color.Green)
            Text("เงิน: $money", color = Color.Yellow)
            Spacer(modifier = Modifier.height(24.dp))
            // "เริ่มกิจกรรม" button at the very top.
            Text("เริ่มกิจกรรม", modifier = Modifier
                .clickable { onStartDay() }
                .padding(8.dp), color = Color.Yellow)
            Spacer(modifier = Modifier.height(12.dp))
            // Sleep options:
            Text("พักผ่อน (1 ชม.)", modifier = Modifier
                .clickable { onNap() }
                .padding(8.dp), color = Color.Blue)
            Spacer(modifier = Modifier.height(12.dp))
            Text("นอน (3 ชม.)", modifier = Modifier
                .clickable { onSleep3() }
                .padding(8.dp), color = Color.Blue)
            Spacer(modifier = Modifier.height(12.dp))
            Text("นอนจนถึง 6 โมงเช้าวันถัดไป", modifier = Modifier
                .clickable { onSleep() }
                .padding(8.dp), color = Color.Blue)
            Spacer(modifier = Modifier.height(16.dp))
            Text("ดูสถานะ", modifier = Modifier
                .clickable { onViewStatus() }
                .padding(8.dp), color = Color.Magenta)
        }
        // Back button placed at top-right.
        Text("กลับ", modifier = Modifier
            .align(Alignment.TopEnd)
            .clickable { /* Back action if needed */ }
            .padding(8.dp), color = Color.Red)
    }
}


// 4. isWeekend
fun isWeekend(date: LocalDate): Boolean =
    date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY


// 5. ScheduleScreen
@Composable
fun ScheduleScreen(
    currentDate: LocalDate,
    currentHour: Int,
    player: Player,
    activities: List<Activity>,
    girls: List<GirlCharacter>,
    onActivitySelected: (Activity) -> Unit,
    onConversationGirlSelected: (GirlCharacter) -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Text(
                "วันที่: $currentDate (${getDayOfWeekThai(currentDate.dayOfWeek)})   เวลาปัจจุบัน: $currentHour:00   พลังงาน: ${player.energy}   เงิน: ${player.money}",
                color = Color.White, modifier = Modifier.padding(8.dp)
            )
            Text("ตารางของสาว", color = Color.White, modifier = Modifier.padding(8.dp))
            girls.forEach { girl ->
                val currentSlot = girl.schedule.find { currentHour in it.startHour until it.endHour }
                val statusText = if (currentSlot != null)
                    "กำลัง ${currentSlot.activityType} ที่ ${currentSlot.location}"
                else "ว่าง"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .background(Color.DarkGray)
                ) {
                    Text("${girl.name}: ", color = Color.Yellow, modifier = Modifier.weight(1f).padding(8.dp))
                    Text(statusText, color = Color.White, modifier = Modifier.padding(8.dp))
                    // Call button if girl is free
                    if (currentSlot == null) {
                        val iconColor = if (girl.hasTalked) Color.Green else Color.Gray
                        Text("☎", color = iconColor, modifier = Modifier
                            .clickable(enabled = girl.hasTalked) { onConversationGirlSelected(girl) }
                            .padding(8.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("กิจกรรมของคุณ", color = Color.White, modifier = Modifier.padding(8.dp))
            // Each activity item in two rows.
            activities.forEach { activity ->
                val available = currentHour in activity.availableStart until activity.availableEnd
                val availabilityIcon = if (available) "✓" else "✗"
                val hoursUntilStart = if (currentHour < activity.availableStart)
                    activity.availableStart - currentHour else 0
                val startText = if (hoursUntilStart > 0) "เริ่มในอีก $hoursUntilStart ชม." else "เริ่มทันที"
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (available) onActivitySelected(activity) }
                        .padding(8.dp)
                        .background(Color.Gray)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(availabilityIcon, color = if (available) Color.Green else Color.Red, modifier = Modifier.padding(8.dp))
                        Text(activity.name, color = Color.White, modifier = Modifier.weight(1f))
                    }
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text("พลังงาน: ${activity.energyCost}", color = Color.LightGray)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(startText, color = Color.LightGray)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("ใช้เวลา: ${activity.duration} ชม.", color = Color.LightGray)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        // Back button at top-right.
        Text("กลับ", modifier = Modifier
            .align(Alignment.TopEnd)
            .clickable { onBack() }
            .padding(8.dp), color = Color.Red)
    }
}


// 6. ConversationSelectionScreen
@Composable
fun ConversationSelectionScreen(
    pendingGirls: List<GirlCharacter>,
    onSelectGirl: (GirlCharacter) -> Unit,
    onSkip: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("คุณพบกับหลายคน!", color = Color.White, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(12.dp))
            Text("เลือกคุยกับใครก่อน:", color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            pendingGirls.forEach { girl ->
                Text(
                    girl.name,
                    color = Color.Cyan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectGirl(girl) }
                        .padding(12.dp)
                        .background(Color.DarkGray)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("ข้ามการคุย", modifier = Modifier.clickable { onSkip() }.padding(12.dp), color = Color.Yellow)
        }
    }
}


// 7. ConversationScreen
@Composable
fun ConversationScreen(
    partnerName: String,
    dialogueNode: DialogueNode,
    onOptionSelected: (DialogueOption) -> Unit,
    onSkip: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("สนทนากับ $partnerName", color = Color.Yellow, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(12.dp))
            Text(dialogueNode.dialogueText, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            dialogueNode.options.forEach { option ->
                Text(
                    option.text,
                    modifier = Modifier
                        .clickable { onOptionSelected(option) }
                        .padding(8.dp),
                    color = Color.Cyan
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("ข้ามการสนทนา", modifier = Modifier.clickable { onSkip() }.padding(8.dp), color = Color.Yellow)
        }
        // Back button at top-right.
        Text("กลับ", modifier = Modifier
            .align(Alignment.TopEnd)
            .clickable { onSkip() }
            .padding(8.dp), color = Color.Red)
    }
}


// 8. StatusScreen
@Composable
fun StatusScreen(
    date: LocalDate,
    currentHour: Int,
    player: Player,
    girls: List<GirlCharacter>,
    onBack: () -> Unit,
    onBuyGift: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("สถานะเกม", color = Color.White)
            Text("วันที่: $date (${getDayOfWeekThai(date.dayOfWeek)})", color = Color.Cyan)
            Text("เวลาปัจจุบัน: $currentHour:00", color = Color.Cyan)
            Text("พลังงาน: ${player.energy}", color = Color.Green)
            Text("เงิน: ${player.money}", color = Color.Yellow)
            Spacer(modifier = Modifier.height(16.dp))
            Text("ทักษะ:", color = Color.LightGray)
            Text("เรียน: ${player.study}", color = Color.White)
            Text("ออกกำลังกาย: ${player.exercise}", color = Color.White)
            Text("สังคม: ${player.social}", color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Text("สถานะสาว:", color = Color.Magenta)
            girls.forEach { girl ->
                Text("${girl.name} (Affection: ${girl.affection}, Status: ${girl.relationshipStatus})", color = Color.White)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("ซื้อของขวัญ", modifier = Modifier.clickable { onBuyGift() }.padding(8.dp), color = Color.Cyan)
            Spacer(modifier = Modifier.height(16.dp))
        }
        // Back button at top-right.
        Text("กลับ", modifier = Modifier
            .align(Alignment.TopEnd)
            .clickable { onBack() }
            .padding(8.dp), color = Color.Red)
    }
}


// 9. GiftScreen
@Composable
fun GiftScreen(
    player: Player,
    girls: List<GirlCharacter>,
    onGiftPurchased: (cost: Int, girl: GirlCharacter) -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("ซื้อของขวัญ (ราคา: 100)", color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            girls.forEach { girl ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGiftPurchased(100, girl) }
                        .padding(8.dp)
                        .background(Color.DarkGray)
                ) {
                    Text("ซื้อของขวัญให้ ${girl.name}", color = Color.White, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        // Back button at top-right.
        Text("กลับ", modifier = Modifier
            .align(Alignment.TopEnd)
            .clickable { onBack() }
            .padding(8.dp), color = Color.Red)
    }
}


// 10. ActivityResultPopup
@Composable
fun ActivityResultPopup(message: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(message, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            Text("ตกลง", modifier = Modifier
                .clickable { onDismiss() }
                .padding(8.dp), color = Color.Blue)
        }
    }
}
