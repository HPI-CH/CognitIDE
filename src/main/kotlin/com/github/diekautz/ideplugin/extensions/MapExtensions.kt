package com.github.diekautz.ideplugin.extensions

fun <K> MutableMap<K, Double>.increment(key: K, delta: Double, defaultValue: Double = 0.0) {
    val value = getOrDefault(key, defaultValue)
    this[key] = value + delta
}

fun <K> MutableMap<K, Int>.increment(key: K, delta: Int = 1, defaultValue: Int = 0) {
    val value = getOrDefault(key, defaultValue)
    this[key] = value + delta
}