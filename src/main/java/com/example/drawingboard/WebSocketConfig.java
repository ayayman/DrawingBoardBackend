package com.example.drawingboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private WebSocketBoardHandler webSocketBoardHandler;
    private WebSocketInterceptor webSocketInterceptor;

    @Autowired
    public WebSocketConfig(WebSocketBoardHandler webSocketBoardHandler, WebSocketInterceptor webSocketInterceptor) {
        this.webSocketBoardHandler = webSocketBoardHandler;
        this.webSocketInterceptor = webSocketInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(webSocketBoardHandler, "/api/board")
                .addInterceptors(webSocketInterceptor)
                .setAllowedOrigins("*");
    }
}

