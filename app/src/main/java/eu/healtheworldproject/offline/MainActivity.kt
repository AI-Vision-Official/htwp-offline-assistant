package eu.healtheworldproject.offline

import android.content.SharedPreferences
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this) { }
        val cards = loadCards(this)
        val prefs = getSharedPreferences("htwp", MODE_PRIVATE)
        setContent {
            App(cards, prefs) { text, lang ->
                tts?.language = Locale(lang)
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "step")
            }
        }
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun App(cards: List<Card>, prefs: SharedPreferences, speak: (String, String) -> Unit) {
    var lang by remember { mutableStateOf(prefs.getString("lang", "en") ?: "en") }
    var number by remember { mutableStateOf(prefs.getString("num", "") ?: "") }
    var text by remember { mutableStateOf("") }
    var card by remember { mutableStateOf<Card?>(null) }
    var step by remember { mutableIntStateOf(0) }
    var noMatch by remember { mutableStateOf(false) }

    fun t(en: String, nl: String) = if (lang == "nl") nl else en
    val numText = number.ifBlank { t("your local emergency number", "het alarmnummer") }
    fun open(c: Card) { card = c; step = 0; noMatch = false }
    fun setLang(l: String) { lang = l; prefs.edit().putString("lang", l).apply() }

    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            val c = card
            if (c == null) {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(t("Tell me what's happening", "Vertel me wat er aan de hand is"),
                        fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(t("If someone is in danger, call $numText now.",
                        "Als iemand in gevaar is, bel nu $numText."), fontSize = 18.sp)
                    OutlinedTextField(
                        value = text, onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontSize = 20.sp), minLines = 2
                    )
                    Button(
                        onClick = { val m = match(cards, text); if (m != null) open(m) else noMatch = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(t("Help me", "Help me"), fontSize = 20.sp) }
                    if (noMatch) Text(
                        t("I'm not sure I understood. Please choose below.",
                            "Ik weet niet zeker of ik het begrijp. Kies hieronder."), fontSize = 18.sp)
                    cards.forEach { cd ->
                        OutlinedButton(onClick = { open(cd) }, modifier = Modifier.fillMaxWidth()) {
                            Text(cd.title[lang] ?: cd.title["en"] ?: cd.id, fontSize = 18.sp)
                        }
                    }
                    HorizontalDivider()
                    OutlinedTextField(
                        value = number,
                        onValueChange = { number = it; prefs.edit().putString("num", it).apply() },
                        label = { Text(t("Local emergency number (set once)", "Lokaal alarmnummer (eenmalig invullen)")) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row {
                        TextButton(onClick = { setLang("en") }) { Text("English") }
                        TextButton(onClick = { setLang("nl") }) { Text("Nederlands") }
                    }
                    Text(t("DRAFT: content not yet reviewed by experts. This app does not replace emergency services and cannot call anyone.",
                        "CONCEPT: inhoud nog niet beoordeeld door deskundigen. Deze app vervangt geen hulpdiensten en kan niemand bellen."),
                        fontSize = 14.sp)
                }
            } else {
                BackHandler { card = null }
                val steps = (c.steps[lang] ?: c.steps["en"] ?: emptyList()).map { it.replace("{NUM}", numText) }
                val last = step >= steps.lastIndex
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!c.reviewed) Text(
                        t("DRAFT: not yet reviewed by experts", "CONCEPT: nog niet beoordeeld door deskundigen"),
                        color = Color(0xFFB00020), fontWeight = FontWeight.Bold)
                    Text(c.title[lang] ?: c.title["en"] ?: c.id, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(t("Step ${step + 1} of ${steps.size}", "Stap ${step + 1} van ${steps.size}"), fontSize = 16.sp)
                    Text(steps[step], fontSize = 28.sp, lineHeight = 36.sp)
                    Button(
                        onClick = { if (last) card = null else step++ },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (last) t("Finished", "Klaar") else t("Done, next", "Gedaan, volgende"), fontSize = 22.sp) }
                    OutlinedButton(onClick = { speak(steps[step], lang) }, modifier = Modifier.fillMaxWidth()) {
                        Text(t("Read aloud", "Lees voor"), fontSize = 18.sp)
                    }
                    if (step > 0) OutlinedButton(onClick = { step-- }, modifier = Modifier.fillMaxWidth()) {
                        Text(t("Previous step", "Vorige stap"), fontSize = 18.sp)
                    }
                    TextButton(onClick = { card = null }) { Text(t("Start over", "Opnieuw beginnen"), fontSize = 18.sp) }
                }
            }
        }
    }
}
