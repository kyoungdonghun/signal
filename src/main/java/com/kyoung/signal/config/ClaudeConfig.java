package com.kyoung.signal.config;

import com.kyoung.signal.infrastructure.claude.ClaudeApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClaudeConfig {

    @Value("${claude.api.key}")
    private String apiKey;

    @Value("${claude.api.url:https://api.anthropic.com/v1/messages}")
    private String apiUrl;

    @Value("${claude.api.model.sonnet:claude-sonnet-4-6}")
    private String sonnetModel;

    @Value("${claude.api.model.opus:claude-opus-4-6}")
    private String opusModel;

    @Value("${claude.api.model.haiku:claude-haiku-4-5-20251001}")
    private String haikuModel;

    /** NF / NtCompare 등 구조화된 출력, 배치 처리 */
    @Bean("sonnetClient")
    public ClaudeApiClient sonnetClient() {
        return new ClaudeApiClient(apiKey, sonnetModel, apiUrl);
    }

    /** CA — 핵심 교차 판단 레이어 */
    @Bean("opusClient")
    public ClaudeApiClient opusClient() {
        return new ClaudeApiClient(apiKey, opusModel, apiUrl);
    }

    /** NT — 분류 작업 전용 (Haiku 검증 후 NT 메인으로 전환 예정) */
    @Bean("haikuClient")
    public ClaudeApiClient haikuClient() {
        return new ClaudeApiClient(apiKey, haikuModel, apiUrl);
    }
}
