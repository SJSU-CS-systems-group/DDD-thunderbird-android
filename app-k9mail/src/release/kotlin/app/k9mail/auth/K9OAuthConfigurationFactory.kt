package app.k9mail.auth

import app.k9mail.core.common.oauth.OAuthConfiguration
import app.k9mail.core.common.oauth.OAuthConfigurationFactory

@Suppress("ktlint:standard:max-line-length")
class K9OAuthConfigurationFactory : OAuthConfigurationFactory {
    override fun createConfigurations(): Map<List<String>, OAuthConfiguration> {
        return mapOf(
        )
    }
}
