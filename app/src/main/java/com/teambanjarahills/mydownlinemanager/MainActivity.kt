package com.teambanjarahills.mydownlinemanager

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class Member(val id: String, var name: String, var phone: String, var doneMonth: String = "")

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private val members = mutableListOf<Member>()
    private lateinit var main: LinearLayout
    private lateinit var listArea: LinearLayout
    private lateinit var summary: TextView
    private lateinit var search: EditText
    private var filter = "ALL"
    private var tts: TextToSpeech? = null
    private val prefs by lazy { getSharedPreferences("downline_data", MODE_PRIVATE) }
    private val contactPermission = 201
    private val notifyPermission = 202
    private val monthKey get() = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        load()
        tts = TextToSpeech(this, this)
        createChannel()
        askNotificationPermission()
        ReminderScheduler.scheduleNext(this)
        buildScreen()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts?.language = Locale("te", "IN")
    }

    private fun buildScreen() {
        val scroll = ScrollView(this)
        main = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(22, 24, 22, 28) }
        scroll.addView(main); setContentView(scroll)

        main.addView(TextView(this).apply {
            text = "My Downline Manager\nTEAM BANJARAHILLS"
            textSize = 25f; setTextColor(0xFF17365D.toInt()); setPadding(0,0,0,12)
        })
        summary = TextView(this).apply { textSize = 17f; setPadding(0,8,0,12) }
        main.addView(summary)

        button("📥  Import Phone Contacts") { importContacts() }
        button("➕  Add Member Manually") { addManual() }
        button("🔔  Set CNC Reminder (2nd–14th, 9:00 AM)") {
            ReminderScheduler.scheduleNext(this)
            Toast.makeText(this, "Monthly CNC reminder is scheduled", Toast.LENGTH_LONG).show()
        }
        button("🔊  Listen to Telugu Reminder Voice") { speakReminder() }
        button("📤  Backup / Share Member List") { shareBackup() }

        search = EditText(this).apply { hint = "Search member name or phone"; setSingleLine(true) }
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { refreshList() }
            override fun afterTextChanged(s: Editable?) {}
        })
        main.addView(search)

        val filters = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        listOf("ALL", "PENDING", "DONE").forEach { f ->
            val b = Button(this).apply { text = f; setOnClickListener { filter = f; refreshList() } }
            filters.addView(b, LinearLayout.LayoutParams(0, -2, 1f))
        }
        main.addView(filters)
        listArea = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        main.addView(listArea)
        refreshList()
    }

    private fun button(label: String, action: () -> Unit) {
        main.addView(Button(this).apply { text = label; setOnClickListener { action() } })
    }

    private fun refreshList() {
        if (!::listArea.isInitialized) return
        summary.text = "మొత్తం సభ్యులు: ${members.size}\nCNC పూర్తయింది: ${members.count { it.doneMonth == monthKey }}   పెండింగ్: ${members.count { it.doneMonth != monthKey }}"
        listArea.removeAllViews()
        val q = if (::search.isInitialized) search.text.toString().trim().lowercase(Locale.getDefault()) else ""
        val filtered = members.filter {
            (it.name.lowercase(Locale.getDefault()).contains(q) || it.phone.contains(q)) &&
            (filter == "ALL" || (filter == "DONE" && it.doneMonth == monthKey) || (filter == "PENDING" && it.doneMonth != monthKey))
        }
        if (filtered.isEmpty()) {
            listArea.addView(TextView(this).apply { text = "సభ్యులు కనిపించలేదు"; textSize = 16f; setPadding(0,18,0,18) })
        }
        filtered.forEach { m ->
            val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(10,12,10,12) }
            card.setBackgroundColor(0xFFF1F5F9.toInt())
            card.addView(TextView(this).apply {
                text = "${m.name}\n${m.phone}\nCNC: ${if (m.doneMonth == monthKey) "DONE" else "PENDING"}"
                textSize = 16f
            })
            val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            actions.addView(smallButton(if (m.doneMonth == monthKey) "Undo" else "CNC Done") {
                m.doneMonth = if (m.doneMonth == monthKey) "" else monthKey
                save(); refreshList()
            })
            actions.addView(smallButton("Call") {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(m.phone)}")))
            })
            actions.addView(smallButton("Share") {
                val msg = "నమస్కారం ${m.name} గారు. Team Banjarahills నుంచి CNC రిమైండర్. ఈ నెల 2వ తేదీ నుంచి 14వ తేదీ లోపు మీ CNC పూర్తి చేసుకోండి. ధన్యవాదాలు."
                startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"; putExtra(Intent.EXTRA_TEXT, msg)
                }, "Reminder share"))
            })
            actions.addView(smallButton("Delete") {
                AlertDialog.Builder(this).setMessage("${m.name} ను తొలగించాలా?")
                    .setPositiveButton("Delete") { _, _ -> members.remove(m); save(); refreshList() }
                    .setNegativeButton("Cancel", null).show()
            })
            card.addView(actions)
            listArea.addView(card)
            listArea.addView(View(this).apply { setBackgroundColor(0xFFCCCCCC.toInt()) }, LinearLayout.LayoutParams(-1,1))
        }
    }

    private fun smallButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label; textSize = 11f; setOnClickListener { action() }
    }

    private fun importContacts() {
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), contactPermission)
        } else showContactPicker()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (requestCode == contactPermission && results.firstOrNull() == PackageManager.PERMISSION_GRANTED) showContactPicker()
        if (requestCode == notifyPermission && results.firstOrNull() == PackageManager.PERMISSION_GRANTED) createChannel()
    }

    private fun showContactPicker() {
        val names = mutableListOf<String>(); val phones = mutableListOf<String>(); val ids = mutableSetOf<String>()
        val c: Cursor? = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.CONTACT_ID),
            null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
        c?.use {
            val ni = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val pi = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val ii = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            while (it.moveToNext()) {
                val id = it.getString(ii) ?: continue
                if (ids.add(id)) { names.add(it.getString(ni) ?: "Unknown"); phones.add(it.getString(pi) ?: "") }
            }
        }
        if (names.isEmpty()) { toast("ఫోన్‌లో Contacts కనిపించలేదు"); return }
        val selected = BooleanArray(names.size)
        val labels = names.indices.map { "${names[it]} • ${phones[it]}" }.toTypedArray()
        AlertDialog.Builder(this).setTitle("సభ్యులను ఎంపిక చేయండి")
            .setMultiChoiceItems(labels, selected) { _, which, checked -> selected[which] = checked }
            .setPositiveButton("Import Selected") { _, _ ->
                var count = 0
                names.indices.filter { selected[it] }.forEach { i ->
                    val norm = normalize(phones[i])
                    if (norm.isNotBlank() && members.none { normalize(it.phone) == norm }) {
                        members.add(Member(UUID.randomUUID().toString(), names[i], phones[i])); count++
                    }
                }
                save(); refreshList(); toast("$count మంది సభ్యులు జోడించబడ్డారు")
            }.setNegativeButton("Cancel", null).show()
    }

    private fun addManual() {
        val name = EditText(this).apply { hint = "పేరు" }
        val phone = EditText(this).apply { hint = "ఫోన్ నంబర్"; inputType = 3 }
        val box = LinearLayout(this).apply { orientation = 1; setPadding(20,8,20,0); addView(name); addView(phone) }
        AlertDialog.Builder(this).setTitle("సభ్యుడిని జోడించండి").setView(box)
            .setPositiveButton("Save") { _, _ ->
                val n = name.text.toString().trim(); val p = phone.text.toString().trim()
                if (n.isNotBlank() && normalize(p).isNotBlank() && members.none { normalize(it.phone) == normalize(p) }) {
                    members.add(Member(UUID.randomUUID().toString(), n, p)); save(); refreshList()
                } else toast("పేరు మరియు కొత్త ఫోన్ నంబర్ ఇవ్వండి")
            }.setNegativeButton("Cancel", null).show()
    }

    private fun speakReminder() {
        val msg = "గుడ్ మార్నింగ్. నమస్కారం అండి. నేను లావణ్య మాట్లాడుతున్నాను, టీమ్ బంజారాహిల్స్ నుంచి. మన వెస్టిజ్ సి ఎన్ సి గురించి చిన్న రిమైండర్. ఈ నెల రెండు నుంచి పద్నాలుగో తేదీ లోపు మీ సి ఎన్ సి పూర్తి చేసుకోండి. సహాయం కావాలంటే నాకు చెప్పండి. థాంక్యూ అండి."
        tts?.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "cnc_voice")
            ?: toast("Text to Speech సిద్ధంగా లేదు")
    }

    private fun shareBackup() {
        val arr = JSONArray()
        members.forEach { arr.put(JSONObject().put("name",it.name).put("phone",it.phone).put("doneMonth",it.doneMonth)) }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_TEXT, arr.toString())
        }
        startActivity(Intent.createChooser(intent, "Backup share/save"))
    }

    private fun normalize(p: String) = p.filter { it.isDigit() }.takeLast(10)
    private fun save() {
        val a = JSONArray()
        members.forEach { a.put(JSONObject().put("id",it.id).put("name",it.name).put("phone",it.phone).put("done",it.doneMonth)) }
        prefs.edit().putString("members", a.toString()).apply()
    }
    private fun load() {
        try {
            val a = JSONArray(prefs.getString("members","[]"))
            for (i in 0 until a.length()) {
                val o = a.getJSONObject(i)
                members.add(Member(o.optString("id", UUID.randomUUID().toString()),o.optString("name"),o.optString("phone"),o.optString("done")))
            }
        } catch (_: Exception) {}
    }
    private fun toast(s: String) = Toast.makeText(this,s,Toast.LENGTH_LONG).show()
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel("cnc_reminders","CNC Reminders",NotificationManager.IMPORTANCE_HIGH))
        }
    }
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), notifyPermission)
    }
    override fun onDestroy() { tts?.stop(); tts?.shutdown(); super.onDestroy() }
}

object ReminderScheduler {
    fun scheduleNext(context: Context) {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        if (now.get(Calendar.DAY_OF_MONTH) > 14 || (now.get(Calendar.DAY_OF_MONTH) == 14 && now.after(next))) {
            next.add(Calendar.MONTH, 1); next.set(Calendar.DAY_OF_MONTH, 2)
        } else if (now.get(Calendar.DAY_OF_MONTH) < 2) next.set(Calendar.DAY_OF_MONTH, 2)
        else if (now.after(next)) next.add(Calendar.DAY_OF_MONTH, 1)
        if (next.get(Calendar.DAY_OF_MONTH) > 14) { next.add(Calendar.MONTH, 1); next.set(Calendar.DAY_OF_MONTH, 2) }
        val pi = PendingIntent.getBroadcast(context, 800, Intent(context, ReminderReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val am = context.getSystemService(AlarmManager::class.java)
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pi)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val notification = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context,"cnc_reminders") else Notification.Builder(context)
            notification.setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Team Banjarahills – CNC Reminder")
                .setContentText("ఈ నెల 2 నుంచి 14వ తేదీ లోపు మీ టీమ్ CNC ఫాలో-అప్ పూర్తి చేయండి.")
                .setAutoCancel(true)
            context.getSystemService(NotificationManager::class.java).notify(800, notification.build())
        }
        ReminderScheduler.scheduleNext(context)
    }
}
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) { ReminderScheduler.scheduleNext(context) }
}
