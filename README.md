# Strukt

A binary encoding and decoding library for Kotlin.

Strukt is a library and annotation processor which generates encoding and
decoding code at compile-time. It aims to simplify the implementation of binary
parsers and encoders as much as possible, without compromising on flexibility.

The project is still in development and will reach version 1.0 once a minimum
set of features is implemented. Version numbers follow the [semantic versioning]
rules, therefore the API might change before v1.0 is reached.

## Example

```kotlin
@Struct  // Mark class as struct
data class SomeStruct(  // It is NOT required to use data classes
    val intField: Int,
    var longField: Long  // Both val and var are supported
)

fun main() {
    val buf = ByteBuffer.allocate(12)  // Allocate a buffer
    val original = SomeStruct(1234, 5678)  // Create a struct instance

    val strukt = Strukt.Builder().build()  // Obtain a Strukt instance

    strukt.write(original, buf)  // Write the struct to buf

    buf.position(0)  // Reset buf position

    val decoded = strukt.read<SomeStruct>(buf)  // Read the struct from buf

    assert(original == decoded)
}
```

## Features

Right now, the following are supported:

- Primitive types
- Infinitely nested structs

Planned features:

- Fixed size array fields
- Variable size array fields:
    - Based on preceding integer field
    - Based on terminator sequence for ByteArray
- Tagged unions via sealed classes
- Custom type codecs (resolved at compile-time)

## Documentation

Human friendly documentation is coming soon, meanwhile you can generate a KDoc
reference by running:

```
./gradlew dokkaHtml
```

The result will be written to docs/.

## Installation

This library requires [kapt].

### Gradle

#### Groovy DSL (build.gradle)

```groovy
plugins {
    id "org.jetbrains.kotlin.kapt" version "1.4.31"
}

dependencies {
    def struktVersion = "0.1.0"

    implementation "org.catafratta.strukt:strukt:$struktVersion"
    kapt "org.catafratta.strukt:strukt:$struktVersion"
}
```

#### Kotlin DSL (build.gradle.kts)

```kotlin
plugins {
    kotlin("kapt") version "1.4.31"
}

dependencies {
    val struktVersion = "0.1.0"

    implementation("org.catafratta.strukt:strukt:$struktVersion")
    kapt("org.catafratta.strukt:strukt:$struktVersion")
}
```

## Contributing

Pull requests are welcome, please make sure that all tests pass before
submitting your patch. New code submitted to this repo must be covered by tests.

[semantic versioning]: https://semver.org/ "Semantic Versioning"

[kapt]: https://kotlinlang.org/docs/kapt.html "kapt docs"
