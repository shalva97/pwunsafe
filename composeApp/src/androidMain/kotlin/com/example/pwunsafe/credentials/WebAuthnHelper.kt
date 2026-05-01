package com.example.pwunsafe.credentials

import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

object WebAuthnHelper {

    /** Generate a fresh EC P-256 key pair. Returns (credentialId, privateKeyPkcs8Base64, publicKeyCoseBase64). */
    fun generateKeyPair(): Triple<ByteArray, String, String> {
        val gen = KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp256r1"))
        }
        val pair = gen.generateKeyPair()
        val credentialId = ByteArray(32).also { java.security.SecureRandom().nextBytes(it) }
        val privateBase64 = Base64.getEncoder().encodeToString(pair.private.encoded)
        val coseBytes = encodeCosePublicKey(pair.public as ECPublicKey)
        val coseBase64 = Base64.getEncoder().encodeToString(coseBytes)
        return Triple(credentialId, privateBase64, coseBase64)
    }

    /**
     * Encode an EC P-256 public key as a CBOR COSE_Key map.
     * Map of 5 entries: kty=2, alg=-7, crv=1, x=<32 bytes>, y=<32 bytes>
     */
    fun encodeCosePublicKey(pubKey: ECPublicKey): ByteArray {
        val w = pubKey.w
        val xBytes = unsignedBigIntBytes(w.affineX, 32)
        val yBytes = unsignedBigIntBytes(w.affineY, 32)

        return buildCbor {
            writeMap(5)
            writeInt(1); writeInt(2)         // kty: EC2
            writeInt(3); writeNegInt(6)      // alg: ES256 (-7)
            writeNegInt(0); writeInt(1)      // crv: P-256 (-1 → 1)
            writeNegInt(1); writeBytes(xBytes) // x (-2)
            writeNegInt(2); writeBytes(yBytes) // y (-3)
        }
    }

    /** Build the authenticatorData byte array for assertion (get). */
    fun buildAuthenticatorDataForAssertion(rpId: String, signCount: Int): ByteArray {
        val rpIdHash = sha256(rpId.toByteArray())
        val flags: Byte = 0x01 // UP (user present)
        return rpIdHash + flags + intToBytes(signCount)
    }

    /** Build the authenticatorData for attestation (create), including attested credential data. */
    fun buildAuthenticatorDataForAttestation(
        rpId: String,
        credentialId: ByteArray,
        cosePublicKey: ByteArray,
    ): ByteArray {
        val rpIdHash = sha256(rpId.toByteArray())
        val flags: Byte = 0x45 // UP + AT (attested credential data present)
        val aaguid = ByteArray(16)
        val credIdLen = shortToBytes(credentialId.size)
        return rpIdHash + flags + intToBytes(0) + aaguid + credIdLen + credentialId + cosePublicKey
    }

    /** Sign authenticatorData + SHA-256(clientDataJSON) with the stored PKCS#8 private key. */
    fun signAssertion(
        privateKeyPkcs8Base64: String,
        authenticatorData: ByteArray,
        clientDataJsonBytes: ByteArray,
    ): ByteArray {
        val keyBytes = Base64.getDecoder().decode(privateKeyPkcs8Base64)
        val privateKey = KeyFactory.getInstance("EC").generatePrivate(PKCS8EncodedKeySpec(keyBytes))
        val clientDataHash = sha256(clientDataJsonBytes)
        val payload = authenticatorData + clientDataHash
        return Signature.getInstance("SHA256withECDSA").run {
            initSign(privateKey)
            update(payload)
            sign()
        }
    }

    fun sha256(data: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(data)

    fun base64Url(bytes: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    fun base64UrlDecode(s: String): ByteArray =
        Base64.getUrlDecoder().decode(s)

    private fun unsignedBigIntBytes(n: java.math.BigInteger, length: Int): ByteArray {
        val raw = n.toByteArray()
        return when {
            raw.size == length -> raw
            raw.size > length -> raw.copyOfRange(raw.size - length, raw.size)
            else -> ByteArray(length - raw.size) + raw
        }
    }

    private fun intToBytes(value: Int): ByteArray =
        byteArrayOf((value shr 24).toByte(), (value shr 16).toByte(), (value shr 8).toByte(), value.toByte())

    private fun shortToBytes(value: Int): ByteArray =
        byteArrayOf((value shr 8).toByte(), value.toByte())

    // ── Minimal CBOR builder ──────────────────────────────────────────────

    private fun buildCbor(block: CborWriter.() -> Unit): ByteArray =
        CborWriter().apply(block).toByteArray()

    private class CborWriter {
        private val buf = mutableListOf<Byte>()

        fun writeMap(count: Int) = writeMajor(5, count)
        fun writeInt(v: Int) = writeMajor(0, v)
        fun writeNegInt(v: Int) = writeMajor(1, v) // encodes as -(v+1)
        fun writeBytes(data: ByteArray) {
            writeMajor(2, data.size)
            data.forEach { buf.add(it) }
        }

        private fun writeMajor(major: Int, info: Int) {
            val m = major shl 5
            when {
                info <= 23 -> buf.add((m or info).toByte())
                info <= 0xFF -> { buf.add((m or 24).toByte()); buf.add(info.toByte()) }
                info <= 0xFFFF -> {
                    buf.add((m or 25).toByte())
                    buf.add((info shr 8).toByte())
                    buf.add(info.toByte())
                }
                else -> {
                    buf.add((m or 26).toByte())
                    buf.add((info shr 24).toByte())
                    buf.add((info shr 16).toByte())
                    buf.add((info shr 8).toByte())
                    buf.add(info.toByte())
                }
            }
        }

        fun toByteArray() = buf.toByteArray()
    }
}
