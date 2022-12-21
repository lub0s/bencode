package dev.lubos.bencode

internal val skippableChars = setOf(' ', '\n')

class BencodeParser {

	fun encode(input: Any): String = when (input) {
		is String -> "${input.length}:$input"
		is Int -> "i${input}e"
		is List<*> -> "l${input.joinToString(separator = "") { encode(it!!) }}e"
		is Map<*, *> -> "d${
			input.entries.joinToString(separator = "") {
				encode(it.key!!) + encode(it.value!!)
			}
		}e"

		else -> error("unsupported bencode type")
	}

	fun decode(input: String): Bencode =
		decodeInternal(input).first

	internal fun decodeInternal(input: String): Pair<Bencode, Int> {
		var index = 0

		while (input[index] == ' ' || input[index] == '\n') {
			index += 1
		}

		val (bencode, offset) = when (input[index]) {
			'i' -> decodeInt(input.substring(index))
			'l' -> decodeList(input.substring(index))
			'd' -> decodeMap(input.substring(index))
			else -> decodeString(input.substring(index))
		}

		index += offset

		return bencode to index
	}

	// 4:asdf
	internal fun decodeString(input: String): Pair<Bencode, Int> {
		val digits = input.split(":")[0]
		val len = digits.toInt()
		val value = input.substring(digits.length + 1, digits.length + 1 + len)
		return Bencode.String(value) to (digits.length + 1 + len)
	}

	// i500e; i-500e
	internal fun decodeInt(input: String): Pair<Bencode, Int> {
		val offset = input.indexOfFirst { it == 'e' }
		val value = input.substring(1, offset).toInt()
		return Bencode.Int(value) to offset + 1
	}

	// l4:asdf4:fdsae
	internal fun decodeList(input: String): Pair<Bencode, Int> {
		val list = mutableListOf<Bencode>()
		var index = 1
		while (index < input.length) {
			// todo: improve
			if (input[index] in skippableChars) {
				index += 1
				continue
			}
			if (input[index] == 'e') {
				index += 1
				break
			}
			val (bencode, offset) = decodeInternal(input.substring(index))
			list += bencode
			index += offset
		}
		return Bencode.List(list) to index
	}

	// d4:asdf4:fdsa4:fdsa4:asdfe
	internal fun decodeMap(input: String): Pair<Bencode, Int> {
		val map = mutableMapOf<Bencode, Bencode>()
		var index = 1

		while (index < input.length) {
			// todo: improve
			if (input[index] in skippableChars) {
				index += 1
				continue
			}
			if (input[index] == 'e') {
				index += 1
				break
			}
			val (key, offset) = decodeInternal(input.substring(index))
			val (value, offset2) = decodeInternal(input.substring(index + offset))

			map += key to value
			index += (offset + offset2)
		}

		return Bencode.Map(map) to index
	}
}
