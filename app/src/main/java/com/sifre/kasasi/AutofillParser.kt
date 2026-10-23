package com.sifre.kasasi

import android.app.assist.AssistStructure
import android.text.InputType
import android.view.View
import android.view.autofill.AutofillId

class ParsedForm {
    var usernameId: AutofillId? = null
    var passwordId: AutofillId? = null
    var candidateId: AutofillId? = null
    var domain: String? = null
    var packageName: String? = null

    fun hasFields(): Boolean = usernameId != null || passwordId != null
}

object AutofillParser {

    fun parse(structure: AssistStructure): ParsedForm {
        val form = ParsedForm()
        for (i in 0 until structure.windowNodeCount) {
            walk(structure.getWindowNodeAt(i).rootViewNode, form)
        }
        if (form.usernameId == null && form.passwordId != null) {
            form.usernameId = form.candidateId
        }
        return form
    }

    private fun walk(node: AssistStructure.ViewNode, form: ParsedForm) {
        if (form.domain == null && !node.webDomain.isNullOrEmpty()) {
            form.domain = node.webDomain
        }
        if (form.packageName == null && !node.idPackage.isNullOrEmpty()) {
            form.packageName = node.idPackage
        }

        val id = node.autofillId
        if (id != null && node.autofillType == View.AUTOFILL_TYPE_TEXT) {
            val hints = node.autofillHints?.toList() ?: emptyList()
            val isPassword =
                hints.contains(View.AUTOFILL_HINT_PASSWORD) ||
                    isPasswordInput(node.inputType)
            val isUser =
                hints.contains(View.AUTOFILL_HINT_USERNAME) ||
                    hints.contains(View.AUTOFILL_HINT_EMAIL_ADDRESS)

            if (isPassword) {
                if (form.passwordId == null) form.passwordId = id
            } else if (isUser) {
                if (form.usernameId == null) form.usernameId = id
            } else if (form.passwordId == null && isTextInput(node.inputType)) {
                form.candidateId = id
            }
        }

        for (i in 0 until node.childCount) {
            walk(node.getChildAt(i), form)
        }
    }

    private fun isTextInput(t: Int): Boolean {
        return (t and InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT
    }

    private fun isPasswordInput(t: Int): Boolean {
        val cls = t and InputType.TYPE_MASK_CLASS
        val v = t and InputType.TYPE_MASK_VARIATION
        return when (cls) {
            InputType.TYPE_CLASS_TEXT ->
                v == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                    v == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                    v == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            InputType.TYPE_CLASS_NUMBER ->
                v == InputType.TYPE_NUMBER_VARIATION_PASSWORD
            else -> false
        }
    }
}
