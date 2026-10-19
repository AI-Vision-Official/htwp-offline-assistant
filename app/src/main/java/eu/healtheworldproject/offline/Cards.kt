package eu.healtheworldproject.offline

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Card(
    val id: String,
    val priority: Int,
    val reviewed: Boolean,
    val keywords: List<String>,
    val title: Map<String, String>,
    val steps: Map<String, List<String>>
)

private fun JSONArray.strings() = (0 until length()).map { getString(it) }

fun loadCards(ctx: Context): List<Card> {
    val arr = JSONArray(ctx.assets.open("cards.json").bufferedReader().use { it.readText() })
    return (0 until arr.length()).map { i ->
        val o: JSONObject = arr.getJSONObject(i)
        val title = o.getJSONObject("title")
        val steps = o.getJSONObject("steps")
        Card(
            id = o.getString("id"),
            priority = o.getInt("priority"),
            reviewed = o.getBoolean("reviewed"),
            keywords = o.getJSONArray("keywords").strings(),
            title = title.keys().asSequence().associateWith { title.getString(it) },
            steps = steps.keys().asSequence().associateWith { steps.getJSONArray(it).strings() }
        )
    }
}

/** Rule-based safety layer: any matching keyword counts, the highest-priority card wins. No AI involved. */
fun match(cards: List<Card>, text: String): Card? {
    val t = text.lowercase()
    return cards.filter { c -> c.keywords.any { t.contains(it) } }.maxByOrNull { it.priority }
}
