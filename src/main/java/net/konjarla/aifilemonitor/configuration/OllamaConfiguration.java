package net.konjarla.aifilemonitor.configuration;

import io.micrometer.observation.ObservationRegistry;
import net.konjarla.aifilemonitor.transport.RestClientInterceptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
public class OllamaConfiguration {
    @Value("${ollama.host}")
    private String ollamaHost;

    @Value("${ollama.chat.model}")
    private String ollamaChatModel;

    @Value("${ollama.photo.chat.model}")
    private String ollamaPhotoChatModel;

    @Value("${spring.ai.ollama.chat.options.num-ctx}")
    private Integer numCtx;

    @Value("${ollama.chat.options.temperature}")
    private Double ollamaChatTemperature;

    @Value("${vector.store.dim}")
    private Integer dim;

    @Value("${vector.store}")
    private String vector_store;

    @Value("${file.assistant.photo.system.prompt.file}")
    private Resource photoPrompt;

    @Value("${file_assistant_classification_system_prompt}")
    private Resource classificationPrompt;

    @Bean
    public OllamaApi ollamaApi() {
        RestClient.Builder builder = RestClient.builder();
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings
                .defaults()
                .withConnectTimeout(Duration.ofSeconds(60))
                .withReadTimeout(Duration.ofSeconds(300));
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect().build(settings);
        builder.requestFactory(requestFactory)
                .requestInterceptor(new RestClientInterceptor());
        WebClient.Builder webClientBuilder = WebClient.builder();

        return OllamaApi.builder()
                .baseUrl(ollamaHost)
                .restClientBuilder(builder)
                .webClientBuilder(webClientBuilder).build();
    }

    @Bean
    public ChatModel chatModel() {
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi())
                .defaultOptions(
                        OllamaOptions.builder()
                                .model(ollamaChatModel)
                                .numCtx(numCtx)
                                .temperature(ollamaChatTemperature)
                                .build())
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        ObservationRegistry observationRegistry = ObservationRegistry.create();
        ModelManagementOptions modelManagementOptions = ModelManagementOptions.builder()
                .pullModelStrategy(PullModelStrategy.WHEN_MISSING)
                .build();

        return new OllamaEmbeddingModel(ollamaApi(),
                OllamaOptions.builder()
                        .model(OllamaModel.MXBAI_EMBED_LARGE.id())
                        .build(), observationRegistry, modelManagementOptions);
    }

    @Bean
    public VectorStore fileVectorStore(JdbcTemplate jdbcTemplate) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel())
                .dimensions(dim)
                .distanceType(COSINE_DISTANCE)
                .indexType(HNSW)
                .initializeSchema(false)
                .schemaName("public")
                .vectorTableName(vector_store)
                .maxDocumentBatchSize(10000)
                .build();
    }

    @Bean
    public ChatClient chatClient( VectorStore fileVectorStore) {
        return ChatClient.builder(chatModel())
                //.defaultSystem(systemPrompt)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                        //new QuestionAnswerAdvisor(blendedVectorStore)
                )
                .build();
    }

    @Bean
    public ChatModel photoChatModel() {
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi())
                .defaultOptions(
                        OllamaOptions.builder()
                                .model(ollamaPhotoChatModel)
                                .numCtx(numCtx)
                                .temperature(ollamaChatTemperature)
                                .build())
                .build();
    }

    @Bean
    public ChatClient photoChatClient() {
        return ChatClient.builder(photoChatModel())
                .defaultSystem(photoPrompt)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                .build();
    }

    @Bean
    public ChatClient classificationChatClient() {
        return ChatClient.builder(chatModel())
                .defaultSystem(classificationPrompt)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                .build();
    }

}
