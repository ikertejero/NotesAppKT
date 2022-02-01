import kotlin.random.Random

class LoremIpsum {
    val loremString = "Lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod " +
            "tempor incididunt ut labore et dolore magna aliqua Ut enim ad minim veniam quis " +
            "nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat Duis" +
            " aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat" +
            " nulla pariatur Excepteur sint occaecat cupidatat non proident sunt in culpa qui" +
            " officia deserunt mollit anim id est laborum"
    val loremSplitted = loremString.lowercase().split(" ")
    fun getTitle() : String {
        var randomTitle = ""
        for (i in 1..Random.nextInt(1,4)) // Words
            randomTitle += loremSplitted[Random.nextInt(1,loremSplitted.size)].replaceFirstChar { it.uppercase() } + " "
        return randomTitle.dropLast(1)
    }
    fun getBody() : String {
        var randomBody = ""
        val x = Random.nextInt(2,6); val y = Random.nextInt(3,11)
        for (i in 1..x) {// Sentences
            var randomSentence = ""
            for (j in 1..y) // Words
                randomSentence += loremSplitted[Random.nextInt(1, loremSplitted.size)] + " "
            randomBody += (randomSentence.dropLast(1) + ". ").replaceFirstChar { it.uppercase() }
        }
        return randomBody.dropLast(1)
    }
}
