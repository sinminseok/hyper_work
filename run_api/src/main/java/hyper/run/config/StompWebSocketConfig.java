package hyper.run.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@EnableWebSocketMessageBroker
@Configuration
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${hyper.run.domain.websocket.game}")
    private String gameUrl; // /game

    @Value("${hyper.run.domain.websocket.publish}")
    private String pub; // /pub

    @Value("${hyper.run.domain.websocket.subscribe}")
    private String sub; // /sub


    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry.addEndpoint(gameUrl)
                .setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(final MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes(pub);
        registry.enableSimpleBroker(sub);
    }
}