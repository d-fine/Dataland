package org.dataland.datalandproxyfinder.configurations

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource

/**
 * Provides the [ChatClient] with the proxy search system prompt. Only active for the "azure" provider.
 */
@Configuration
@ConditionalOnProperty(name = ["dataland.proxy-finder.provider"], havingValue = "azure")
class ProxySearchChatClientConfiguration {
    /**
     * Builds the chat client used for the proxy search.
     */
    @Bean
    fun proxySearchChatClient(
        chatModel: ChatModel,
        @Value("classpath:prompts/proxy-search.st") systemPrompt: Resource,
    ): ChatClient = ChatClient.builder(chatModel).defaultSystem(systemPrompt).build()
}
