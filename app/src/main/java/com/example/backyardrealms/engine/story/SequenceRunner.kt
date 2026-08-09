package com.example.backyardrealms.engine.story

class SequenceRunner {
    sealed class Step {
        data class Message(val text: String) : Step()
        data class Action(val run: () -> Unit) : Step()
    }
    private val steps = ArrayDeque<Step>()
    var message: String? = null
        private set
    var isActive: Boolean = false
        private set

    fun start(newSteps: List<Step>) {
        steps.clear(); steps.addAll(newSteps); message = null; isActive = true
        advance()
    }
    fun clear() { steps.clear(); message = null; isActive = false }
    fun interact() {
        if (!isActive || message == null) return
        message = null; advance()
    }
    private fun advance() {
        while (steps.isNotEmpty()) {
            when (val step = steps.removeFirst()) {
                is Step.Message -> { message = step.text; return }
                is Step.Action -> step.run()
            }
        }
        message = null; isActive = false
    }
}
