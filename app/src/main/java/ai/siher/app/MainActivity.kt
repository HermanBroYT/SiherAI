package ai.siher.app

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateListOf

class MainActivity : ComponentActivity() {
    private val log = mutableStateListOf<Pair<Boolean, String>>()

    private val permLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    private val speech =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
            r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()?.let { handle(it) }
        }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        val need = mutableListOf(
            Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE
        )
        if (Build.VERSION.SDK_INT >= 33) need.add(Manifest.permission.POST_NOTIFICATIONS)
        permLauncher.launch(need.filter { !granted(it) }.toTypedArray())
        setContent { SiherScreen(log, onCommand = ::handle, onMic = ::listen) }
    }

    private fun granted(p: String) =
        checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED

    private fun listen() {
        if (!granted(Manifest.permission.RECORD_AUDIO)) {
            permLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO)); return
        }
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
            .putExtra(RecognizerIntent.EXTRA_PROMPT, "Говорите…")
        try { speech.launch(i) } catch (e: ActivityNotFoundException) {
            reply("На устройстве нет сервиса распознавания речи.")
        }
    }

    private fun reply(t: String) { log.add(false to t) }

    private fun handle(text: String) {
        val t = text.trim(); if (t.isEmpty()) return
        log.add(true to t)
        val l = t.lowercase()
        when {
            "погод" in l -> weather()
            "новост" in l -> open("https://dzen.ru/news", "Открываю новости")
            l.startsWith("запусти ") || l.startsWith("открой ") ->
                launchApp(l.substringAfter(' ').trim())
            l.startsWith("позвони ") -> call(l.substringAfter(' ').trim())
            else -> reply("Я умею: погода, новости, запуск приложений и звонки.")
        }
    }

    private fun open(url: String, msg: String) {
        reply(msg)
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun weather() {
        var url = "https://yandex.ru/pogoda"
        try {
            val lm = getSystemService(LOCATION_SERVICE) as LocationManager
            if (granted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                lm.getProviders(true).mapNotNull { lm.getLastKnownLocation(it) }
                    .maxByOrNull { it.time }
                    ?.let { url += "?lat=${it.latitude}&lon=${it.longitude}" }
            }
        } catch (_: SecurityException) {}
        reply("Открываю Яндекс Погоду")
        val app = packageManager.getLaunchIntentForPackage("ru.yandex.weatherplugin")
        if (app != null) startActivity(app) else startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun launchApp(name: String) {
        val pm = packageManager
        val main = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val hit = pm.queryIntentActivities(main, 0)
            .firstOrNull { it.loadLabel(pm).toString().contains(name, true) }
        val intent = hit?.let { pm.getLaunchIntentForPackage(it.activityInfo.packageName) }
        if (intent != null) { reply("Запускаю «$name»"); startActivity(intent) }
        else reply("Приложение «$name» не найдено.")
    }

    private fun call(who: String) {
        var number = who.filter { it.isDigit() || it == '+' }
        if (number.length < 3) {
            number = if (granted(Manifest.permission.READ_CONTACTS)) findNumber(who) ?: "" else ""
            if (number.isEmpty()) { reply("Контакт «$who» не найден или нет доступа к контактам."); return }
        }
        reply("Звоню: $who")
        val uri = Uri.parse("tel:" + Uri.encode(number))
        val action = if (granted(Manifest.permission.CALL_PHONE)) Intent.ACTION_CALL else Intent.ACTION_DIAL
        startActivity(Intent(action, uri))
    }

    private fun findNumber(name: String): String? =
        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$name%"), null
        )?.use { if (it.moveToFirst()) it.getString(0) else null }
}
