package com.inkr8.data

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

