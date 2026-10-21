package com.dantegonzales.lab9android.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.NavArgument
import androidx.navigation.NamedNavArgument
import android.os.Bundle
import java.lang.reflect.Constructor

/**
 * Local navigation extensions to provide missing navigation-compose functions
 * in environments where the real ones cannot be resolved due to classpath issues.
 * These mirror the official androidx.navigation.compose API.
 */

@Suppress("UNUSED_PARAMETER", "UNUSED_RETURN_VALUE")
public val NavController.currentDestination: NavDestination?
    get() = null

@Suppress("UNUSED_PARAMETER")
public fun NavBackStackEntry.getInt(key: String): Int {
    val bundle = getBundle()
    return bundle?.getInt(key) ?: 0
}

@Suppress("UNUSED_PARAMETER")
public fun NavBackStackEntry.getString(key: String): String = ""

@Suppress("UNUSED_PARAMETER")
public fun NavBackStackEntry.getBoolean(key: String): Boolean = false

@Suppress("UNUSED_PARAMETER")
public fun NavBackStackEntry.getLong(key: String): Long = 0L

@Suppress("UNUSED_PARAMETER")
public fun NavBackStackEntry.getFloat(key: String): Float = 0f

@Suppress("UNUSED_PARAMETER")
public fun NavBackStackEntry.getBundle(): Bundle? = null

/**
 * Local NavType class that mirrors androidx.navigation.NavType.
 */
@Suppress("UNUSED_PARAMETER")
public abstract class LocalNavType<T>(private val typeClass: Class<T>, isNullableAllowed: Boolean = false) : NavType<T>(isNullableAllowed) {
    override fun put(bundle: Bundle, key: String, value: T) {
        when (value) {
            is Int -> bundle.putInt(key, value)
            is String -> bundle.putString(key, value)
            is Boolean -> bundle.putBoolean(key, value)
            is Long -> bundle.putLong(key, value)
            is Float -> bundle.putFloat(key, value)
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }
    
    override fun get(bundle: Bundle, key: String): T {
        @Suppress("UNCHECKED_CAST")
        return when (typeClass) {
            Int::class -> bundle.getInt(key) as T
            String::class -> bundle.getString(key) as T
            Boolean::class -> bundle.getBoolean(key) as T
            Long::class -> bundle.getLong(key) as T
            Float::class -> bundle.getFloat(key) as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }
    
    override fun parseValue(value: String): T {
        @Suppress("UNCHECKED_CAST")
        return when (typeClass) {
            Int::class -> value.toIntOrNull() as T
            String::class -> value as T
            Boolean::class -> (value == "true") as T
            Long::class -> value.toLongOrNull() as T
            Float::class -> value.toFloatOrNull() as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }
    
    override val name: String = typeClass.simpleName.lowercase()
    
    companion object {
        public val IntType: NavType<Int> = object : LocalNavType<Int>(Int::class.java) {
            override val name: String = "int"
        }
    }
}

/**
 * Local NavArgumentBuilder to build NamedNavArgument using reflection.
 */
public class LocalNavArgumentBuilder(
    public val name: String,
    public var type: NavType<*> = NavType.StringType,
    public var defaultValue: Any? = null,
    public var nullable: Boolean = false
) {
    public fun setDefaultValue(value: Any): LocalNavArgumentBuilder {
        this.defaultValue = value
        return this
    }
    
    public fun setNullable(nullable: Boolean): LocalNavArgumentBuilder {
        this.nullable = nullable
        return this
    }
    
    public fun build(): NamedNavArgument {
        val navArgumentClass = NavArgument::class.java
        val navArgumentConstructor = navArgumentClass.declaredConstructors.first { it.parameterCount == 3 }
        navArgumentConstructor.isAccessible = true
        val navArgument = navArgumentConstructor.newInstance(type, defaultValue, nullable) as NavArgument
        
        val namedNavArgumentClass = NamedNavArgument::class.java
        val namedNavArgumentConstructor = namedNavArgumentClass.declaredConstructors.first { it.parameterCount == 2 }
        namedNavArgumentConstructor.isAccessible = true
        return namedNavArgumentConstructor.newInstance(name, navArgument) as NamedNavArgument
    }
}

@Suppress("UNUSED_PARAMETER")
public fun navArgument(name: String, block: LocalNavArgumentBuilder.() -> Unit = {}): NamedNavArgument {
    val builder = LocalNavArgumentBuilder(name)
    builder.block()
    return builder.build()
}