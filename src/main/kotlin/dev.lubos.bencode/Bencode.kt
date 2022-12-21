package dev.lubos.bencode

sealed class Bencode {
	data class String(val value: kotlin.String): Bencode()
	data class Int(val value: kotlin.Int): Bencode()
	data class List(val value: kotlin.collections.List<Bencode>): Bencode()
	data class Map(val value: kotlin.collections.Map<Bencode, Bencode>): Bencode()

	val string get() = this as String
	val int get() = this as Int
	val list get() = this as List
	val map get() = this as Map
}
