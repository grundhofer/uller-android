package eu.sebaro.uller

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface PrefDelegate<T> : ReadWriteProperty<Any, T> {
    val key: String
}

class KPrefDelegate(private val prefs: SharedPreferences, val json: Json = Json) {
    constructor(ctx: Context, name: String, json: Json = Json) : this(ctx.getSharedPreferences(name, Context.MODE_PRIVATE), json)

    fun string(key: String, defaultValue: String): PrefDelegate<String> = KStringDelegate(key, defaultValue)
    fun stringNA(key: String): PrefDelegate<String?> = KStringNADelegate(key)
    fun boolean(key: String, defaultValue: Boolean): PrefDelegate<Boolean> = KBooleanDelegate(key, defaultValue)

    inline fun <reified T : Any> obj(key: String, defaultValue: T): PrefDelegate<T> = obj(json.serializersModule.serializer(), key, defaultValue)
    fun <T : Any> obj(serializer: KSerializer<T>, key: String, defaultValue: T): PrefDelegate<T> = KObjectDelegate(serializer, key, defaultValue)

    inline fun <reified T : Any> objNA(key: String): PrefDelegate<T?> = objNA(key, json.serializersModule.serializer())
    fun <T : Any> objNA(key: String, serializer: KSerializer<T>): PrefDelegate<T?> = KObjectDelegateNA(serializer, key)

    inline fun <reified T : Any> objList(key: String, defaultValue: List<T>): PrefDelegate<List<T>> = objList(key, json.serializersModule.serializer(), defaultValue)
    fun <T : Any> objList(key: String, serializer: KSerializer<T>, defaultValue: List<T> = emptyList()): PrefDelegate<List<T>> {
        val listSerializer = ListSerializer(serializer)
        return KObjectDelegate(listSerializer, key, defaultValue)
    }

    private inner class KStringDelegate(override val key: String, private val defaultValue: String) : PrefDelegate<String> {
        override fun getValue(thisRef: Any, property: KProperty<*>) = prefs.getString(key, null) ?: defaultValue
        override fun setValue(thisRef: Any, property: KProperty<*>, value: String) = prefs.edit { putString(key, value) }
    }

    private inner class KStringNADelegate(override val key: String) : PrefDelegate<String?> {
        override fun getValue(thisRef: Any, property: KProperty<*>) = prefs.getString(key, null)
        override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) = prefs.edit { putString(key, value) }
    }

    private inner class KBooleanDelegate(override val key: String, private val defaultValue: Boolean) : PrefDelegate<Boolean> {
        override fun getValue(thisRef: Any, property: KProperty<*>) = prefs.getBoolean(key, defaultValue)
        override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) = prefs.edit { putBoolean(key, value) }
    }

    private inner class KObjectDelegate<T : Any>(val serializer: KSerializer<T>, override val key: String, private val defaultValue: T) : PrefDelegate<T> {
        override fun getValue(thisRef: Any, property: KProperty<*>) = prefs.getString(key, null)?.let { json.decodeFromString(serializer, it) } ?: defaultValue
        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) = prefs.edit { putString(key, json.encodeToString(serializer, value)) }
    }

    private inner class KObjectDelegateNA<T : Any>(val serializer: KSerializer<T>, override val key: String) : PrefDelegate<T?> {
        override fun getValue(thisRef: Any, property: KProperty<*>) = prefs.getString(key, null)?.let { json.decodeFromString(serializer, it) }
        override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
            prefs.edit {
                if (value != null) {
                    putString(key, json.encodeToString(serializer, value))
                } else {
                    remove(key)
                }
            }
        }
    }

    fun cleanup() {
        prefs.edit { clear() }
    }
}
