package com.example.parabot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class RobotEmotion {
    HAPPY, SAD, SURPRISED, SLEEPY, NEUTRAL
}
data class FaceState(
    val emotion: RobotEmotion = RobotEmotion.NEUTRAL,
    val isBlinking: Boolean = false
)

class RobotViewModel : ViewModel() {
    private val _state = MutableStateFlow(FaceState())
    val state = _state.asStateFlow()

    init {
        // Auto-blink with random interval to seem alive
        viewModelScope.launch {
            while (true) {
                _state.value = _state.value.copy(isBlinking = false)
                delay(800 + Random.nextLong(1800))
                _state.value = _state.value.copy(isBlinking = true)
                delay(120)
                _state.value = _state.value.copy(isBlinking = false)
            }
        }
    }

    fun setEmotion(emotion: RobotEmotion) {
        _state.value = _state.value.copy(emotion = emotion)
    }
}