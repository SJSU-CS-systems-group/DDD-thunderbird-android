package app.k9mail.provider

import android.content.Context
import app.k9mail.core.common.provider.AppNameProvider
import net.discdd.mail.R

class K9AppNameProvider(
    context: Context,
) : AppNameProvider {
    override val appName: String by lazy {
        context.getString(R.string.app_name)
    }
}
