//package org.numpol.podotaro
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
//
//// Represent different screens in the demo.
//sealed class GameScreen {
//    object Menu : GameScreen()
//    object Schedule : GameScreen()
//    object Dialogue : GameScreen()
//}
//
//@Composable
//fun DemoGame() {
//    var currentScreen by remember { mutableStateOf<GameScreen>(GameScreen.Menu) }
//    var relationship by remember { mutableStateOf(50) } // A simple stat example
//    var currentDay by remember { mutableStateOf(1) }
//
//    // Main container
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFF101010))
//            .padding(16.dp)
//    ) {
//        when (currentScreen) {
//            is GameScreen.Menu -> {
//                MenuScreen(
//                    onStart = { currentScreen = GameScreen.Schedule },
//                    onDialogue = { currentScreen = GameScreen.Dialogue }
//                )
//            }
//            is GameScreen.Schedule -> {
//                ScheduleScreen(
//                    day = currentDay,
//                    onAdvanceDay = {
//                        currentDay++
//                        // Return to menu after scheduling, for example
//                        currentScreen = GameScreen.Menu
//                    },
//                    onDialogue = { currentScreen = GameScreen.Dialogue }
//                )
//            }
//            is GameScreen.Dialogue -> {
//                DialogueScreen(
//                    relationship = relationship,
//                    onChoiceMade = { increase ->
//                        // Adjust relationship stat based on choice
//                        relationship += if (increase) 5 else -5
//                        // After dialogue, go back to schedule or menu
//                        currentScreen = GameScreen.Schedule
//                    }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun MenuScreen(onStart: () -> Unit, onDialogue: () -> Unit) {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = "Welcome to Your Demo Dating Sim",
//            style = MaterialTheme.typography.headlineMedium,
//            color = Color.White
//        )
//        Spacer(modifier = Modifier.height(24.dp))
//        Text(
//            text = "Start Your Day",
//            modifier = Modifier
//                .clickable { onStart() }
//                .padding(8.dp),
//            color = Color.Cyan
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(
//            text = "Talk to Someone",
//            modifier = Modifier
//                .clickable { onDialogue() }
//                .padding(8.dp),
//            color = Color.Magenta
//        )
//    }
//}
//
//@Composable
//fun ScheduleScreen(day: Int, onAdvanceDay: () -> Unit, onDialogue: () -> Unit) {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = "Day $day: Choose Your Activity",
//            style = MaterialTheme.typography.headlineSmall,
//            color = Color.White
//        )
//        Spacer(modifier = Modifier.height(24.dp))
//        // For simplicity, we offer two options: Advance Day or Talk
//        Text(
//            text = "Advance to Next Day",
//            modifier = Modifier
//                .clickable { onAdvanceDay() }
//                .padding(8.dp),
//            color = Color.Green
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(
//            text = "Have a Conversation",
//            modifier = Modifier
//                .clickable { onDialogue() }
//                .padding(8.dp),
//            color = Color.Yellow
//        )
//    }
//}
//
//@Composable
//fun DialogueScreen(relationship: Int, onChoiceMade: (Boolean) -> Unit) {
//    Column(
//        modifier = Modifier.fillMaxSize().padding(16.dp),
//        verticalArrangement = Arrangement.SpaceEvenly,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = "Dialogue Scene",
//            style = MaterialTheme.typography.headlineSmall,
//            color = Color.White
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(
//            text = "Your current relationship level is: $relationship",
//            color = Color.White
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(
//            text = "Choose a positive response",
//            modifier = Modifier
//                .clickable { onChoiceMade(true) }
//                .padding(8.dp),
//            color = Color.Cyan
//        )
//        Spacer(modifier = Modifier.height(8.dp))
//        Text(
//            text = "Choose a negative response",
//            modifier = Modifier
//                .clickable { onChoiceMade(false) }
//                .padding(8.dp),
//            color = Color.Red
//        )
//    }
//}
