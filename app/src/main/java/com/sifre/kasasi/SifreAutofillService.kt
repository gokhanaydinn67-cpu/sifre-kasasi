package com.sifre.kasasi

import android.app.PendingIntent
import android.content.Intent
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.widget.RemoteViews

@Suppress("DEPRECATION")
class SifreAutofillService : AutofillService() {

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val structure = request.fillContexts.last().structure
        val form = AutofillParser.parse(structure)

        if (!form.hasFields() || form.packageName == packageName) {
            callback.onSuccess(null)
            return
        }

        val ids = listOfNotNull(form.usernameId, form.passwordId).toTypedArray()
        val hint = (form.domain ?: "") + " " + (form.packageName ?: "")

        val intent = Intent(this, AutofillAuthActivity::class.java)
            .putExtra(AutofillAuthActivity.EXTRA_USER, form.usernameId)
            .putExtra(AutofillAuthActivity.EXTRA_PASS, form.passwordId)
            .putExtra(AutofillAuthActivity.EXTRA_HINT, hint)

        val flags = PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        val pi = PendingIntent.getActivity(this, 1001, intent, flags)

        val presentation = RemoteViews(packageName, R.layout.autofill_item)
        presentation.setTextViewText(
            R.id.autofill_text,
            "Şifre Kasası ile doldur"
        )

        val response = FillResponse.Builder()
            .setAuthentication(ids, pi.intentSender, presentation)
            .build()
        callback.onSuccess(response)
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        callback.onSuccess()
    }
}
