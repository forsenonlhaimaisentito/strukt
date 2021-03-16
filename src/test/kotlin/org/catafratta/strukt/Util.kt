package org.catafratta.strukt

import org.catafratta.strukt.processor.StructDef
import org.catafratta.strukt.processor.isPrimitive

internal fun fieldsOf(vararg fields: Pair<String, String>): List<StructDef.Field> =
    fields.map { (name, typeName) ->
        when {
            typeName.isPrimitive -> StructDef.Field.Primitive(name, typeName)
            else -> StructDef.Field.Object(name, typeName)
        }
    }


internal fun parseHex(hex: CharSequence): ByteArray {
    val hexStr = hex.toString().replace(" ", "")

    require(hexStr.length.rem(2) == 0)

    return hexStr.chunked(2) { hexByte -> hexByte.toString().toInt(16).toByte() }.toByteArray()
}
