package org.catafratta.strukt

import org.catafratta.strukt.processor.DeclaredStruct

internal fun fieldsOf(vararg fields: Pair<String, String>): List<DeclaredStruct.Field> =
    fields.map { (name, typeName) -> DeclaredStruct.Field(name, typeName) }


internal fun parseHex(hex: CharSequence): ByteArray {
    val hexStr = hex.toString().replace(" ", "")

    require(hexStr.length.rem(2) == 0)

    return hexStr.chunked(2) { hexByte -> hexByte.toString().toInt(16).toByte() }.toByteArray()
}
