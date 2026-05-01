package com.example.pwunsafe.autofill

import android.app.assist.AssistStructure
import android.text.InputType
import android.view.View
import android.view.autofill.AutofillId

class AssistStructureParser(structure: AssistStructure) {

    var usernameAutofillId: AutofillId? = null
    var passwordAutofillId: AutofillId? = null
    var usernameValue: String? = null
    var passwordValue: String? = null
    var webDomain: String? = null

    init {
        repeat(structure.windowNodeCount) { i ->
            parseNode(structure.getWindowNodeAt(i).rootViewNode)
        }
    }

    private fun parseNode(node: AssistStructure.ViewNode) {
        val hints = node.autofillHints
        val inputType = node.inputType

        if (!hints.isNullOrEmpty()) {
            when {
                hints.any { it in USERNAME_HINTS } -> {
                    usernameAutofillId = node.autofillId
                    usernameValue = node.autofillValue?.textValue?.toString()
                }
                hints.any { it in PASSWORD_HINTS } -> {
                    passwordAutofillId = node.autofillId
                    passwordValue = node.autofillValue?.textValue?.toString()
                }
            }
        } else {
            val isPassword = inputType and InputType.TYPE_MASK_VARIATION == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                inputType and InputType.TYPE_MASK_VARIATION == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                inputType and InputType.TYPE_MASK_VARIATION == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            val isText = inputType and InputType.TYPE_MASK_CLASS == InputType.TYPE_CLASS_TEXT

            when {
                isPassword -> {
                    passwordAutofillId = node.autofillId
                    passwordValue = node.autofillValue?.textValue?.toString()
                }
                isText && usernameAutofillId == null && node.isFocusable -> {
                    val hint = node.hint?.lowercase() ?: ""
                    val id = node.idEntry?.lowercase() ?: ""
                    if (USERNAME_KEYWORDS.any { hint.contains(it) || id.contains(it) }) {
                        usernameAutofillId = node.autofillId
                        usernameValue = node.autofillValue?.textValue?.toString()
                    }
                }
            }
        }

        if (webDomain == null) {
            webDomain = node.webDomain?.takeIf { it.isNotBlank() }
        }

        repeat(node.childCount) { i -> parseNode(node.getChildAt(i)) }
    }

    companion object {
        private val USERNAME_HINTS = setOf(
            View.AUTOFILL_HINT_USERNAME,
            View.AUTOFILL_HINT_EMAIL_ADDRESS,
            "username",
            "email",
        )
        private val PASSWORD_HINTS = setOf(
            View.AUTOFILL_HINT_PASSWORD,
            "password",
            "current-password",
        )
        private val USERNAME_KEYWORDS = listOf("user", "email", "login", "account", "name")
    }
}
