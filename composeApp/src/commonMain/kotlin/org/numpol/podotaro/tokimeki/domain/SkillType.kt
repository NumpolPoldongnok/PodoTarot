package org.numpol.podotaro.tokimeki.domain

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus
import kotlinx.datetime.DateTimeUnit

// -------------------------------
// Domain Enums
// -------------------------------
enum class SkillType { STUDY, EXERCISE, SOCIAL }

enum class ActivityType { STUDY, WORK, EXERCISE, CLUB, CALL, DATE, RELAX, EXAM }

enum class GirlType { SAME_YEAR, JUNIOR, SENIOR, SEXY_PROFESSOR }

enum class RelationshipStatus { FRIEND, DATING }

// -------------------------------
// Domain Data Classes
// -------------------------------
data class Player(
    var energy: Int = 100,
    var study: Int = 0,
    var exercise: Int = 0,
    var social: Int = 0,
    var money: Int = 0
)

data class TimeSlot(
    val startHour: Int,
    val endHour: Int,
    val activityType: ActivityType,
    val location: String
)

data class GirlCharacter(
    val type: GirlType,
    val name: String,
    val personality: ActivityType, // For simplicity, her primary interest
    var affection: Int = 0,
    var schedule: List<TimeSlot> = emptyList(),
    var relationshipStatus: RelationshipStatus = RelationshipStatus.FRIEND,
    var hasTalked: Boolean = false // Indicates if the player has talked with her before
)

data class Activity(
    val name: String,
    val duration: Int,          // in hours
    val energyCost: Int,
    val skillEffects: Map<SkillType, Int>,
    val bonusAffection: Int = 0,  // bonus if activity matches a girl's schedule
    val activityType: ActivityType,
    val availableStart: Int,    // inclusive start time
    val availableEnd: Int,      // exclusive end time
    val moneyReward: Int = 0     // money earned (if any)
)

// -------------------------------
// Domain Functions
// -------------------------------
/**
 * performActivity applies the effects of an activity on the player.
 * For a "พักผ่อน (1 ชม.)" activity, it restores 10 energy.
 * For other activities, it subtracts the energy cost and updates the player's stats.
 */
fun performActivity(activity: Activity, player: Player): Player {
    return if (activity.activityType == ActivityType.RELAX && activity.name.contains("พักผ่อน")) {
        // For a 1-hr nap, restore 10 energy points.
        val restoreAmount = 10
        player.energy = (player.energy + restoreAmount).coerceAtMost(100)
        player
    } else {
        player.energy = (player.energy - activity.energyCost).coerceAtLeast(0)
        activity.skillEffects.forEach { (skill, effect) ->
            when (skill) {
                SkillType.STUDY -> player.study += effect
                SkillType.EXERCISE -> player.exercise += effect
                SkillType.SOCIAL -> player.social += effect
            }
        }
        if (activity.moneyReward > 0) {
            player.money += activity.moneyReward
        }
        player
    }
}

/**
 * Returns the number of days in the month for the given date.
 */
fun getDayOfMonth(date: LocalDate): Int = when(date.month) {
    Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY, Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
    Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
    Month.FEBRUARY -> if ((date.year % 4 == 0 && date.year % 100 != 0) || (date.year % 400 == 0)) 29 else 28
    else -> 30
}

/**
 * Returns true if the given date is the last day of its month.
 */
fun isLastDayOfMonth(date: LocalDate): Boolean = date.dayOfMonth == getDayOfMonth(date)

/**
 * Provides a default schedule for a girl.
 */
fun getDefaultGirlSchedule(): List<TimeSlot> = listOf(
    TimeSlot(10, 12, ActivityType.STUDY, "Library"),
    TimeSlot(14, 16, ActivityType.EXERCISE, "Gym"),
    TimeSlot(18, 20, ActivityType.CLUB, "Café")
)

fun getDayOfWeekThai(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> "จันทร์"
        DayOfWeek.TUESDAY -> "อังคาร"
        DayOfWeek.WEDNESDAY -> "พุธ"
        DayOfWeek.THURSDAY -> "พฤหัสบดี"
        DayOfWeek.FRIDAY -> "ศุกร์"
        DayOfWeek.SATURDAY -> "เสาร์"
        DayOfWeek.SUNDAY -> "อาทิตย์"
        else -> TODO()
    }
}