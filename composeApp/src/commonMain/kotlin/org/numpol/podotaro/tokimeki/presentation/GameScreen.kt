package org.numpol.podotaro.tokimeki.presentation
import androidx.compose.runtime.Composable
import org.numpol.podotaro.tokimeki.domain.RelationshipStatus

@Composable
fun TokimekiGameScreen(viewModel: GameViewModel) {
    when (viewModel.currentScreen) {
        GameScreen.MENU -> MenuScreenUI(viewModel)
        GameScreen.SCHEDULE -> ScheduleScreenUI(viewModel)
        GameScreen.CONVERSATION_SELECTION -> ConversationSelectionScreenUI(viewModel)
        GameScreen.CONVERSATION -> ConversationScreenUI(viewModel)
        GameScreen.STATUS -> StatusScreenUI(viewModel)
        GameScreen.GIFT -> GiftScreenUI(viewModel)
    }
    if ((viewModel.currentScreen == GameScreen.MENU || viewModel.currentScreen == GameScreen.SCHEDULE) &&
        viewModel.activityResultPopup != null) {
        ActivityResultPopupUI(message = viewModel.activityResultPopup!!) {
            viewModel.activityResultPopup = null
        }
    }
}

@Composable
fun MenuScreenUI(viewModel: GameViewModel) {
    MenuScreen(
        date = viewModel.currentDate,
        playedDay = viewModel.currentDate.dayOfYear,
        currentHour = viewModel.currentHour,
        energy = viewModel.player.energy,
        money = viewModel.player.money,
        onStartDay = { viewModel.startDay() },
        onViewStatus = { viewModel.currentScreen = GameScreen.STATUS },
        onSleep = { viewModel.sleepUntil6() },
        onNap = { viewModel.nap() },
        onSleep3 = { viewModel.sleep3() }
    )
}

@Composable
fun ScheduleScreenUI(viewModel: GameViewModel) {
    val activities = if (isWeekend(viewModel.currentDate)) viewModel.activitiesWeekend else viewModel.activitiesWeekday
    ScheduleScreen(
        currentDate = viewModel.currentDate,
        currentHour = viewModel.currentHour,
        player = viewModel.player,
        activities = activities,
        girls = viewModel.girls,
        onActivitySelected = { viewModel.selectActivity(it) },
        onConversationGirlSelected = { viewModel.callGirl(it) },
        onBack = { viewModel.currentScreen = GameScreen.MENU }
    )
}

@Composable
fun ConversationSelectionScreenUI(viewModel: GameViewModel) {
    ConversationSelectionScreen(
        pendingGirls = viewModel.pendingConversationGirls,
        onSelectGirl = { viewModel.callGirl(it) },
        onSkip = {
            viewModel.pendingConversationGirls.clear()
            viewModel.currentScreen = GameScreen.SCHEDULE
        }
    )
}

@Composable
fun ConversationScreenUI(viewModel: GameViewModel) {
    if (viewModel.conversationGirl != null && viewModel.currentDialogue != null) {
        ConversationScreen(
            partnerName = viewModel.conversationGirl!!.name,
            dialogueNode = viewModel.currentDialogue!!,
            onOptionSelected = { option ->
                viewModel.conversationGirl?.let { girl ->
                    girl.affection += option.affectionChange
                    if (option.nextNodeId == "after_proposal") {
                        girl.relationshipStatus = RelationshipStatus.DATING
                        //viewModel.currentDialogue = afterProposalNode
                    }
                }
                viewModel.conversationGirl = null
                viewModel.currentScreen = if (viewModel.pendingConversationGirls.isNotEmpty())
                    GameScreen.CONVERSATION_SELECTION else GameScreen.SCHEDULE
            },
            onSkip = {
                viewModel.conversationGirl = null
                viewModel.currentScreen = if (viewModel.pendingConversationGirls.isNotEmpty())
                    GameScreen.CONVERSATION_SELECTION else GameScreen.SCHEDULE
            }
        )
    }
}

@Composable
fun StatusScreenUI(viewModel: GameViewModel) {
    StatusScreen(
        date = viewModel.currentDate,
        currentHour = viewModel.currentHour,
        player = viewModel.player,
        girls = viewModel.girls,
        onBack = { viewModel.currentScreen = GameScreen.MENU },
        onBuyGift = { viewModel.currentScreen = GameScreen.GIFT }
    )
}

@Composable
fun GiftScreenUI(viewModel: GameViewModel) {
    GiftScreen(
        player = viewModel.player,
        girls = viewModel.girls,
        onGiftPurchased = { cost, girl ->
            if (viewModel.player.money >= cost) {
                viewModel.player.money -= cost
                girl.affection += 15
            }
            viewModel.currentScreen = GameScreen.STATUS
        },
        onBack = { viewModel.currentScreen = GameScreen.STATUS }
    )
}

@Composable
fun ActivityResultPopupUI(message: String, onDismiss: () -> Unit) {
    ActivityResultPopup(message = message, onDismiss = onDismiss)
}
