package dev.lubos.bencode

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val torrentFileFormatted = """
			d
			  8:announce
			    41:http://bttracker.debian.org:6969/announce
			  7:comment
			    35:"Debian CD from cdimage.debian.org"
			  13:creation date
			    i1573903810e
			  4:info
			    d
			      6:length
			        i351272960e
			      4:name
			        31:debian-10.2.0-amd64-netinst.iso
			      12:piece length
			        i262144e
			      6:pieces
			        8:asdffdsa
			    e
			e
		""".trimIndent()

private val torrentFile =
	"d8:announce41:http://bttracker.debian.org:6969/announce7:comment35:\"Debian CD from cdimage.debian.org\"13:creation datei1573903810e9:httpseedsl145:https://cdimage.debian.org/cdimage/release/10.2.0//srv/cdbuilder.debian.org/dst/deb-cd/weekly-builds/amd64/iso-cd/debian-10.2.0-amd64-netinst.iso145:https://cdimage.debian.org/cdimage/archive/10.2.0//srv/cdbuilder.debian.org/dst/deb-cd/weekly-builds/amd64/iso-cd/debian-10.2.0-amd64-netinst.isoe4:infod6:lengthi351272960e4:name31:debian-10.2.0-amd64-netinst.iso12:piece lengthi262144e6:pieces8:asdffdsaee"

class BencodeParserTests {

	@Test
	fun testEncodeString() {
		val parser = BencodeParser()
		assertEquals("4:papa", parser.encode("papa"))
	}

	@Test
	fun testEncodeIntPositive() {
		val parser = BencodeParser()
		assertEquals("i500e", parser.encode(500))
	}

	@Test
	fun testEncodeIntNegative() {
		val parser = BencodeParser()
		assertEquals("i-3e", parser.encode(-3))
	}

	@Test
	fun testEncodeList() {
		val parser = BencodeParser()
		assertEquals("l4:asdf4:fdsae", parser.encode(listOf("asdf", "fdsa")))
	}

	@Test
	fun testEncodeMap() {
		val parser = BencodeParser()
		assertEquals(
			"d4:asdf4:fdsa4:fdsa4:asdfe", parser.encode(mapOf("asdf" to "fdsa", "fdsa" to "asdf"))
		)
	}

	@Test
	fun testDecodeString() {
		val parser = BencodeParser()
		val (bencode, offset) = parser.decodeString("4:asdf")
		assertEquals(Bencode.String("asdf"), bencode)
		assertEquals(6, offset)
	}

	@Test
	fun testDecodeInt() {
		val parser = BencodeParser()
		val (bencode, offset) = parser.decodeInt("i500e")
		assertEquals(Bencode.Int(500), bencode)
		assertEquals(5, offset)
	}

	@Test
	fun testDecodeList() {
		val parser = BencodeParser()
		val (bencode, offset) = parser.decodeList("l4:asdf4:fdsae")
		assertEquals(
			Bencode.List(
				listOf(
					Bencode.String("asdf"), Bencode.String("fdsa")
				)
			), bencode
		)
		assertEquals(14, offset)
	}

	@Test
	fun testDecodeMap() {
		val parser = BencodeParser()
		val (bencode, offset) = parser.decodeMap("d4:asdf4:fdsa4:fdsa4:asdfe")
		assertEquals(
			Bencode.Map(
				mapOf(
					Bencode.String("asdf") to Bencode.String("fdsa"),
					Bencode.String("fdsa") to Bencode.String("asdf"),
				)
			), bencode
		)
		assertEquals(26, offset)
	}

	@Test
	fun testDecodeTorrentFormatted() {
		val parser = BencodeParser()
		val bencode = parser.decode(torrentFileFormatted).map
		val info = bencode.value[Bencode.String("info")]!!.map

		assertTrue {
			info.value[Bencode.String("pieces")] == Bencode.String("asdffdsa")
		}
	}

	@Test
	fun testDecodeTorrent() {
		val parser = BencodeParser()
		val bencode = parser.decode(torrentFile).map
		val info = bencode.value[Bencode.String("info")]!!.map

		assertTrue {
			info.value[Bencode.String("pieces")] == Bencode.String("asdffdsa")
		}
	}
}
