package app.qinbly.android

import android.content.Context
import android.content.SharedPreferences

/**
 * Preferências da sessão com as chaves "da conta" guardadas separadas por usuário (rascunho, chats fixados e silenciados,
 * palavras ocultas, avisos, quem você segue...). O que é do aparelho (token, tema, tamanho da letra...) passa direto.
 * A conta é lida a cada chamada, então trocar de conta troca os dados na hora, sem reiniciar o app.
 * Dados antigos (guardados antes de existir esta separação) vão para a primeira conta que abrir cada chave.
 */
internal class QinblyScopedPrefs(private val base: SharedPreferences) : SharedPreferences {
    private val scopedPrefixes = listOf(
        "following_", "muted_", "pinned_", "inbox_hidden_", "draft_text", "hidden_words", "story_default_audience",
        "story_muted", "story_mute_migrated", "market_city", "market_recent_cities", "qinbly_live_search_history",
        "notif_seen_ids", "bg_notified", "setting_notice_", "setting_notifications", "setting_activity_status", "setting_comments"
    )

    private fun account(): String = base.getString("username", "").orEmpty().removePrefix("@").trim().lowercase().ifBlank { "anon" }

    private fun scoped(key: String?): String? {
        if (key == null || scopedPrefixes.none { key.startsWith(it) }) return key
        val acct = account()
        val target = "$key@$acct"
        if (acct != "anon" && !base.contains(target) && base.contains(key)) {
            val value = base.all[key]
            val e = base.edit()
            when (value) {
                is String -> e.putString(target, value)
                is Boolean -> e.putBoolean(target, value)
                is Int -> e.putInt(target, value)
                is Long -> e.putLong(target, value)
                is Float -> e.putFloat(target, value)
                is Set<*> -> e.putStringSet(target, value.filterIsInstance<String>().toSet())
                else -> Unit
            }
            e.remove(key).apply()
        }
        return target
    }

    override fun getAll(): MutableMap<String, *> = base.all
    override fun getString(key: String?, defValue: String?): String? = base.getString(scoped(key), defValue)
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = base.getStringSet(scoped(key), defValues)
    override fun getInt(key: String?, defValue: Int): Int = base.getInt(scoped(key), defValue)
    override fun getLong(key: String?, defValue: Long): Long = base.getLong(scoped(key), defValue)
    override fun getFloat(key: String?, defValue: Float): Float = base.getFloat(scoped(key), defValue)
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = base.getBoolean(scoped(key), defValue)
    override fun contains(key: String?): Boolean = base.contains(scoped(key))
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = base.registerOnSharedPreferenceChangeListener(listener)
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = base.unregisterOnSharedPreferenceChangeListener(listener)

    override fun edit(): SharedPreferences.Editor = ScopedEditor(base.edit())

    private inner class ScopedEditor(private val inner: SharedPreferences.Editor) : SharedPreferences.Editor {
        override fun putString(key: String?, value: String?): SharedPreferences.Editor { inner.putString(scoped(key), value); return this }
        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor { inner.putStringSet(scoped(key), values); return this }
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor { inner.putInt(scoped(key), value); return this }
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor { inner.putLong(scoped(key), value); return this }
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor { inner.putFloat(scoped(key), value); return this }
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor { inner.putBoolean(scoped(key), value); return this }
        override fun remove(key: String?): SharedPreferences.Editor { inner.remove(scoped(key)); return this }
        override fun clear(): SharedPreferences.Editor { inner.clear(); return this }
        override fun commit(): Boolean = inner.commit()
        override fun apply() = inner.apply()
    }
}

/** A sessão do app com as chaves da conta separadas por usuário. */
internal fun SharedPreferences.byAccount(): SharedPreferences = if (this is QinblyScopedPrefs) this else QinblyScopedPrefs(this)

/**
 * Nome de arquivo de preferências por conta (chats fixados, mensagens ocultas, figurinhas favoritas).
 * O arquivo antigo, compartilhado por todas as contas, passa inteiro para a primeira conta que abrir e fica vazio.
 */
internal fun qinblyAccountFile(context: Context, base: String): String {
    val acct = context.getSharedPreferences("qinbly_session", Context.MODE_PRIVATE).getString("username", "").orEmpty().removePrefix("@").trim().lowercase().ifBlank { "anon" }
    val name = base + "_" + acct
    if (acct != "anon") {
        val legacy = context.getSharedPreferences(base, Context.MODE_PRIVATE)
        val old = legacy.all
        if (old.isNotEmpty()) {
            val e = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit()
            for ((k, v) in old) when (v) {
                is String -> e.putString(k, v)
                is Boolean -> e.putBoolean(k, v)
                is Int -> e.putInt(k, v)
                is Long -> e.putLong(k, v)
                is Float -> e.putFloat(k, v)
                is Set<*> -> e.putStringSet(k, v.filterIsInstance<String>().toSet())
                else -> Unit
            }
            e.apply()
            legacy.edit().clear().apply()
        }
    }
    return name
}
