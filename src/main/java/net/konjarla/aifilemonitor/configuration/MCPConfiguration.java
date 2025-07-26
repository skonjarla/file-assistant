package net.konjarla.aifilemonitor.configuration;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.NonNull;
import net.konjarla.aifilemonitor.tools.FileSearchTool;
import net.konjarla.aifilemonitor.tools.VectorSearchTool;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

@Configuration
public class MCPConfiguration {

    @NonNull
    @Value("${vector.search.query.template}")
    private Resource vectorSearchQueryTemplate;

    @NonNull
    @Value("${vector.search.result.format.system.prompt}")
    private Resource vectorSearchResultFormatPrompt;

    @Value("${file.assistant.system.prompt}")
    private Resource systemPromptFileAssistant;

    @Bean
    public ToolCallbackProvider fileSearchTools(FileSearchTool fileSearchTool){
        return MethodToolCallbackProvider.builder().toolObjects(fileSearchTool).build();
    }

    @Bean
    public ToolCallbackProvider vectorSearchTools(VectorSearchTool vectorSearchTool){
        return MethodToolCallbackProvider.builder().toolObjects(vectorSearchTool).build();
    }

    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> vectorSearchQueryPrompt() {
        McpSchema.Prompt prompt = new McpSchema.Prompt("vector-search-query-template", "A Template for a vector search query",
                List.of(new McpSchema.PromptArgument("query", "The query to search for", true)));

        McpServerFeatures.SyncPromptSpecification promptSpecification = new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, getPromptRequest) -> {
            String queryArgument = (String) getPromptRequest.arguments().get("query");
            if (queryArgument == null) { queryArgument = "find me dinner menu"; }

            PromptTemplate promptTemplate = new PromptTemplate(vectorSearchQueryTemplate);
            Message message = promptTemplate.createMessage(Map.of("query", queryArgument));
            Prompt chatPrompt = new Prompt(message);
            String promptText = chatPrompt.getUserMessage().getText();

            McpSchema.PromptMessage userMessage = new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent(promptText));
            return new McpSchema.GetPromptResult("Vector Search Query", List.of(userMessage));
        });

        return List.of(promptSpecification);
    }

    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> myPrompts() {
        McpSchema.Prompt prompt = new McpSchema.Prompt("vector-search-result-format-system-prompt", "System prompt for vector search results",
                List.of(new McpSchema.PromptArgument("dummy", "Placeholder for nothing", false)));

        McpServerFeatures.SyncPromptSpecification promptSpecification = new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, getPromptRequest) -> {
            String queryArgument = (String) getPromptRequest.arguments().get("dummy");
            if (queryArgument == null) { queryArgument = "No other user queries"; }
            PromptTemplate promptTemplate = PromptTemplate.builder()
                    .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                    .resource(vectorSearchResultFormatPrompt)
                    .build();
            //PromptTemplate promptTemplate = new PromptTemplate(vectorSearchResultFormatPrompt);
            Message message = promptTemplate.createMessage(Map.of("dummy", queryArgument));
            Prompt userPrompt = new Prompt(message);
            String promptText = userPrompt.getUserMessage().getText();

            McpSchema.PromptMessage userMessage = new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent(promptText));
            return new McpSchema.GetPromptResult("Vector Search Result Formatter", List.of(userMessage));
        });

        return List.of(promptSpecification);
    }

    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> systemPromptForFileSearch() {
        McpSchema.Prompt prompt = new McpSchema.Prompt("file-search-system-prompt", "System prompt for File Search",
                List.of(new McpSchema.PromptArgument("dummy", "Placeholder for nothing", false)));

        McpServerFeatures.SyncPromptSpecification promptSpecification = new McpServerFeatures.SyncPromptSpecification
                (prompt, (exchange, getPromptRequest) -> {
            String queryArgument = (String) getPromptRequest.arguments().get("dummy");
            if (queryArgument == null) { queryArgument = "Ask user for any follow up questions"; }
            PromptTemplate promptTemplate = PromptTemplate.builder()
                    .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                    .resource(systemPromptFileAssistant)
                    .build();

            Message message = promptTemplate.createMessage(Map.of("dummy", queryArgument));
            Prompt userPrompt = new Prompt(message);
            String promptText = userPrompt.getUserMessage().getText();

            McpSchema.PromptMessage userMessage = new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent(promptText));
            return new McpSchema.GetPromptResult("File Search", List.of(userMessage));
        });

        return List.of(promptSpecification);
    }
}
