package com.teambanjarahills.mydownlinemanager

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.Gravity
import android.widget.*
import java.util.Locale

class CncReminderCenterActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var counter: TextView
    private lateinit var heading: TextView
    private lateinit var message: TextView
    private var tts: TextToSpeech? = null
    private var lang = "te"
    private var current = 0
    private var playingAll = false

    private data class ShortItem(val title: String, val te: String, val hi: String, val en: String)
    private val items = listOf(
        ShortItem("CNC ప్రారంభమైంది", "నమస్కారం! మీ Vestige CNC ప్రయాణం ప్రారంభమైంది. మీకు వర్తించే ప్రస్తుత CNC ఆఫర్ నిబంధనలు, నెలవారీ అర్హతలు తెలుసుకోండి. మీ టీమ్ మీకు మార్గదర్శనం చేస్తుంది.", "नमस्कार! आपकी Vestige CNC यात्रा शुरू हो गई है। लागू CNC ऑफर की शर्तें और हर महीने की पात्रता जानें। आपकी टीम मार्गदर्शन करेगी।", "Hello! Your Vestige CNC journey has started. Please check the current CNC offer terms and monthly eligibility. Your team is here to guide you."),
        ShortItem("CNC Product Voucher", "CNC ఆఫర్ నిబంధనలు పూర్తిచేసిన అర్హులైన సభ్యులకు ప్రస్తుత పథకం ప్రకారం Product Voucher లభించే అవకాశం ఉంటుంది. వోచర్ విలువ, అర్హతలు, మినహాయింపులు అధికారిక ఆఫర్‌లో తనిఖీ చేయండి.", "CNC ऑफर की शर्तें पूरी करने वाले पात्र सदस्यों को मौजूदा योजना के अनुसार प्रोडक्ट वाउचर मिल सकता है। राशि, पात्रता और अपवाद आधिकारिक ऑफर में देखें।", "Eligible members who meet the CNC offer terms may receive a product voucher under the current scheme. Check the official offer for voucher value, eligibility and exclusions."),
        ShortItem("Consistency ముఖ్యం", "CNC ఆఫర్‌లో వరుస నెలల అర్హతలు ముఖ్యమైనవి కావచ్చు. మీ కొనుగోలు వివరాలు, PV మరియు బిల్లింగ్ తేదీలను అధికారిక నిబంధనలతో సరిపోల్చుకోండి.", "CNC ऑफर में लगातार महीनों की पात्रता महत्वपूर्ण हो सकती है। अपनी खरीदारी, PV और बिलिंग तारीखों को आधिकारिक शर्तों से मिलाएँ।", "Consecutive monthly eligibility may matter for a CNC offer. Check your purchases, PV and billing dates against the official terms."),
        ShortItem("మీ CNC స్థితి చెక్ చేయండి", "ఈ నెల మీ CNC అర్హత పూర్తయిందా? మీ బిల్లింగ్ వివరాలు మరియు ఖాతా స్థితిని తనిఖీ చేయండి. సందేహాలుంటే మీ టీమ్ లీడర్‌ను అడగండి.", "क्या इस महीने आपकी CNC पात्रता पूरी हुई? अपनी बिलिंग जानकारी और अकाउंट स्थिति जाँचें। सवाल हों तो टीम लीडर से पूछें।", "Have you met this month's CNC eligibility? Review your billing details and account status. Ask your team leader if you have questions."),
        ShortItem("చివరి CNC రిమైండర్", "మీ CNC ఆఫర్ గడువు మిస్ కాకుండా ప్రస్తుత అధికారిక తేదీలను గమనించండి. అర్హతలు పూర్తయ్యాయో లేదో నిర్ధారించుకోండి. Team Banjarahills మీకు సహాయం చేయడానికి సిద్ధంగా ఉంది.", "CNC ऑफर की समय-सीमा न चूकें। मौजूदा आधिकारिक तारीखें देखें और पात्रता की पुष्टि करें। Team Banjarahills आपकी मदद के लिए तैयार है।", "Keep track of the current official CNC offer dates so you do not miss the deadline. Confirm your eligibility. Team Banjarahills is ready to help.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lang = getSharedPreferences("downline_data", MODE_PRIVATE).getString("language", "te") ?: "te"
        if (lang == "en") lang = "te"
        tts = TextToSpeech(this, this)
        buildUi()
        showItem(0)
    }

    override fun onInit(status: Int) { if (status == TextToSpeech.SUCCESS) setVoiceLanguage() }

    private fun setVoiceLanguage() {
        val locale = when (lang) { "hi" -> Locale("hi", "IN"); "en" -> Locale("en", "IN"); else -> Locale("te", "IN") }
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(this, "ఈ భాష Voice మీ ఫోన్‌లో లేదు. Android Text-to-Speech settings‌లో install చేయండి.", Toast.LENGTH_LONG).show()
        }
    }

    private fun buildUi() {
        val scroll = ScrollView(this).apply { setBackgroundColor(0xFFF2F6FC.toInt()); isFillViewport = true }
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(28)) }
        scroll.addView(root); setContentView(scroll)
        root.addView(TextView(this).apply {
            text = "🎬  CNC REMINDER CENTER"; textSize = 22f; setTextColor(-1); gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD; setPadding(dp(12), dp(20), dp(12), dp(20))
            background = gradient(intArrayOf(0xFF123B70.toInt(), 0xFF5B3AA8.toInt(), 0xFFB83280.toInt()), 20)
        }, params(-1, -2).apply { bottomMargin = dp(12) })

        val languages = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("తెలుగు" to "te", "हिन्दी" to "hi", "English" to "en").forEach { pair ->
            languages.addView(button(pair.first, 0xFF2878C8.toInt()) {
                lang = pair.second
                getSharedPreferences("downline_data", MODE_PRIVATE).edit().putString("language", lang).apply()
                setVoiceLanguage(); showItem(current)
            }, LinearLayout.LayoutParams(0, dp(46), 1f).apply { setMargins(dp(3), dp(4), dp(3), dp(10)) })
        }
        root.addView(languages)

        val player = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(dp(20), dp(24), dp(20), dp(24))
            background = gradient(intArrayOf(0xFF102E56.toInt(), 0xFF155B83.toInt(), 0xFF5B3AA8.toInt()), 24)
            elevation = dp(4).toFloat()
        }
        counter = TextView(this).apply { textSize = 13f; setTextColor(0xFFFFD88A.toInt()); gravity = Gravity.CENTER }
        heading = TextView(this).apply { textSize = 24f; typeface = Typeface.DEFAULT_BOLD; setTextColor(-1); gravity = Gravity.CENTER; setPadding(0, dp(14), 0, dp(14)) }
        message = TextView(this).apply { textSize = 17f; setTextColor(-1); gravity = Gravity.CENTER; setLineSpacing(dp(5).toFloat(), 1f) }
        player.addView(counter, params(-1, -2)); player.addView(heading, params(-1, -2)); player.addView(message, params(-1, -2))
        root.addView(player, params(-1, -2).apply { bottomMargin = dp(12) })

        root.addView(button("▶  ఈ షార్ట్ వాయిస్‌లో వినండి", 0xFF159B63.toInt()) { playCurrent() }, params(-1, dp(52)).apply { bottomMargin = dp(8) })
        root.addView(button("▶▶  5 షార్ట్స్ వరుసగా వినండి", 0xFF7B42C2.toInt()) { playAll() }, params(-1, dp(52)).apply { bottomMargin = dp(8) })
        root.addView(button("⏹  Voice ఆపండి", 0xFF52647A.toInt()) { playingAll = false; tts?.stop() }, params(-1, dp(48)).apply { bottomMargin = dp(8) })
        root.addView(button("📲  ఈ CNC సందేశం WhatsApp‌లో షేర్", 0xFF159B63.toInt()) { shareCurrent() }, params(-1, dp(52)).apply { bottomMargin = dp(14) })
        root.addView(TextView(this).apply { text = "మీ CNC Reminder Shorts"; textSize = 18f; typeface = Typeface.DEFAULT_BOLD; setTextColor(0xFF17365D.toInt()) }, params(-1, -2).apply { bottomMargin = dp(8) })
        items.indices.forEach { i ->
            root.addView(button((i + 1).toString() + ". " + items[i].title, 0xFF2878C8.toInt()) { showItem(i); playCurrent() },
                params(-1, dp(50)).apply { bottomMargin = dp(6) })
        }
        root.addView(TextView(this).apply {
            text = "గమనిక: CNC ఆఫర్ విలువలు, PV, అర్హతలు, తేదీలు మారవచ్చు. తాజా అధికారిక Vestige నిబంధనలనే అనుసరించండి. ఇది యాప్‌లో వాయిస్‌తో కూడిన షార్ట్ ప్రెజెంటేషన్; MP4 వీడియో లేదా మ్యూజిక్ ట్రాక్ కాదు."
            textSize = 12f; setTextColor(0xFF52647A.toInt()); setPadding(dp(4), dp(14), dp(4), 0)
        }, params(-1, -2))
    }

    private fun textFor(i: Int): String {
        val item = items[i]
        return when (lang) { "hi" -> item.hi; "en" -> item.en; else -> item.te }
    }
    private fun showItem(i: Int) {
        current = i.coerceIn(0, items.lastIndex)
        counter.text = "TEAM BANJARAHILLS  •  SHORT " + (current + 1) + " / " + items.size
        heading.text = items[current].title
        message.text = textFor(current)
        heading.alpha = 0f; message.alpha = 0f
        heading.animate().alpha(1f).setDuration(350).start()
        message.animate().alpha(1f).setDuration(500).start()
    }
    private fun playCurrent() {
        setVoiceLanguage()
        tts?.speak(textFor(current), TextToSpeech.QUEUE_FLUSH, null, "cnc_short_" + current)
    }
    private fun playAll() { playingAll = true; tts?.stop(); setVoiceLanguage(); speakAt(0) }
    private fun speakAt(i: Int) {
        if (!playingAll || i >= items.size) { playingAll = false; return }
        showItem(i)
        val id = "cnc_sequence_" + i
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) { if (utteranceId == id) runOnUiThread { speakAt(i + 1) } }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { runOnUiThread { playingAll = false } }
        })
        tts?.speak(textFor(i), TextToSpeech.QUEUE_FLUSH, null, id)
    }
    private fun shareCurrent() {
        val title = items[current].title
        val body = textFor(current)
        val text = "🌟 *TEAM BANJARAHILLS VESTIGE – CNC REMINDER* 🌟\n\n" + title + "\n\n" + body + "\n\nప్రస్తుత CNC ఆఫర్ నిబంధనలను అధికారికంగా తనిఖీ చేయండి."
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=" + Uri.encode(text)))) }
        catch (_: Exception) { startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }, "Share CNC reminder")) }
    }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun params(w: Int, h: Int) = LinearLayout.LayoutParams(w, h)
    private fun gradient(colors: IntArray, radius: Int) = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors).apply { cornerRadius = dp(radius).toFloat() }
    private fun button(label: String, color: Int, action: () -> Unit) = Button(this).apply {
        text = label; textSize = 14f; isAllCaps = false; setTextColor(-1); setBackgroundColor(color); setOnClickListener { action() }
    }
    override fun onDestroy() { playingAll = false; tts?.stop(); tts?.shutdown(); super.onDestroy() }
}
