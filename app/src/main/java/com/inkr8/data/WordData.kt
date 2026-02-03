package com.inkr8.data

import kotlin.Int
import kotlin.random.Random

val vocabWords = listOf(
    Words(
        0,
        "leeway",
        "noun",
        "The sideways drift of a ship or boat to leeward of the desired course",
        "/ˈliˌweɪ/",
        "By manœuvring the sheets it could be made to keep the boat moving and reduce leeway.",
        "common"
    ),
    Words(
        1,
        "shorting",
        "noun",
        "The action of short",
        "/ˈʃɔrdɪŋ/",
        "The shorting for thy summer fruits and thy harvest is fallen.",
        "common"
    ),
    Words(
        2,
        "bulletproof",
        "verb",
        "To make (something) bulletproof",
        "/ˈbʊlətˌpruf/",
        "To bulletproof your legal arguments, use the most reliable source for determining case validity.",
        "common"
    ),
    Words(
        3,
        "causalism",
        "noun",
        "Any theory or approach ascribing particular importance to causes or causal relationships in understanding the nature of something.",
        "/ˈkɔzəˌlɪzəm/",
        "The doctrine of a motiveless volition would be only causalism.",
        "common"
    ),
    Words(
        4,
        "checkmated",
        "adj",
        "that has been placed in a position in which success, victory, etc., are impossible; thwarted, obstructed, or conclusively defeated.",
        "/ˈtʃɛkˌmeɪdᵻd/",
        "Her smile vanished as, deliberately, he swept pieces from the board to leave it bare but for her checkmated king.",
        "common"
    ),
)

val someThemes =  listOf(
    Theme(
        id = 1,
        name = "Philosophy",
        description = "The systematic study of fundamental questions about existence",
        difficulty = "Hard"

    ),
    Theme(
        id = 2,
        name = "Technology",
        description = "The application of scientific knowledge, skills, methods, and processes to achieve practical goals.",
        difficulty = "easy"

    ),
)

val someTopics = listOf(
    Topic(
        id = 1,
        themeId = someThemes[0].id,
        name = "Philosophy of the spirit",
        description = "Designates the construction of a philosophical system on the remote pattern of the rationalism",
        difficulty = "Tricky"
    ),
    Topic(
        id = 2,
        themeId = someThemes[0].id,
        name = "Stoicism",
        description = "An ancient Greco-Roman philosophy focused on achieving eudaimonia (happiness/flourishing) and ataraxia (serenity)",
        difficulty = "Tricky"
    ),
    Topic(
        id = 3,
        themeId = someThemes[1].id,
        name = "Machine Learning",
        description = "Develops algorithms capable of learning patterns from data to make predictions or decisions without being explicitly programmed",
        difficulty = "Medium"
    ),
    Topic(
        id = 4,
        themeId = someThemes[1].id,
        name = "Smartwatches",
        description = "Wearable, wrist-mounted computers with touchscreen interfaces that, in addition to telling time, function as extensions of smartphones",
        difficulty = "Easy"
    ),

)

fun getRandomWordExcluding(excludeId: Int? = null): Words {
    val availableWords = if (excludeId != null) {
        vocabWords.filter { it.id != excludeId }
    } else {
        vocabWords
    }
    return availableWords[Random.nextInt(availableWords.size)]
}

fun getRandomWords(count: Int): List<Words> {
    return vocabWords.shuffled().take(count)
}

fun getRandomTheme(): Theme {
    return someThemes[Random.nextInt(someThemes.size)]
}

fun getRandomTopicFromTheme(themeId: Int): Topic {
    val topicsInTheme = someTopics.filter { it.themeId == themeId }
    return topicsInTheme[Random.nextInt(topicsInTheme.size)]
}

fun getRandomThemeAndTopic(): Pair<Theme, Topic> {
    val theme = getRandomTheme()
    val topic = getRandomTopicFromTheme(theme.id)
    return Pair(theme, topic)
}