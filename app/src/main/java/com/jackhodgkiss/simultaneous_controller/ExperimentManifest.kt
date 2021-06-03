package com.jackhodgkiss.simultaneous_controller

class ExperimentManifest(
    var keyGenerationMode: KeyGenerationMode = KeyGenerationMode.SIMULTANEOUS,
    var quantizationFunction: QuantizationFunction = QuantizationFunction.TWO_LEVEL,
    var experimentDuration: ExperimentDuration = ExperimentDuration.THIRTY_SECONDS
)

enum class KeyGenerationMode(val id: Int) {
    SIMULTANEOUS(1),
    CONSECUTIVELY(2)
}

enum class QuantizationFunction(val id: Int) {
    TWO_LEVEL(3),
    MULTI_LEVEL(4)
}

enum class ExperimentDuration(val id: Int) {
    THIRTY_SECONDS(5),
    SIXTY_SECONDS(6),
    NINETY_SECONDS(7)
}