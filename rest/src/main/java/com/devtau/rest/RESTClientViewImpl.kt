package com.devtau.rest

import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import android.widget.Toast
import com.devtau.rest.model.Image
import com.devtau.rest.util.Logger
/**
 * Реализация интерфейса и наследование от него избавит нас от необходимости переопределять ВСЕ методы в каждом клиенте
 * Сейчас это не актуально, т.к. клиент у нас только MainActivity, но этот оверинжиниринг существенно повышает
 * гибкость системы для дальнейшего использования
 */
abstract class RESTClientViewImpl: RESTClientView {

    override fun showToast(msg: String) = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show()

    override fun showToast(@StringRes msgId: Int) = Toast.makeText(getContext(), msgId, Toast.LENGTH_SHORT).show()

    override fun showDialog(msg: String) {
        try {
            AlertDialog.Builder(getContext())
                    .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
                    .setMessage(msg).show()
        } catch (e: WindowManager.BadTokenException) {
            Logger.e(getLogTag(), "in showDialog. cannot show dialog")
            showToast(msg)
        }
    }

    override fun showDialog(@StringRes msgId: Int) = showDialog(getContext().getString(msgId))


    override fun processToken(callbackConfirmed: Boolean?, tempToken: String?, tempTokenSecret: String?) {}
    override fun showWebPage(page: String?) {}
    override fun processAccessToken(fullName: String?, receivedToken: String?, receivedTokenSecret: String?,
                                    userNsid: String?, username: String?) {}
    override fun processSearchResult(images: List<Image>?) {}
}