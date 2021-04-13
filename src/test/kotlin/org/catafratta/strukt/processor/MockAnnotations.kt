package org.catafratta.strukt.processor

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.catafratta.strukt.FixedSize
import org.catafratta.strukt.SizeField

internal fun mockFixedSize(size: Int): FixedSize = mock {
    on { this.size } doReturn size
}

internal fun mockSizeField(fieldName: String): SizeField = mock {
    on { this.fieldName } doReturn fieldName
}
