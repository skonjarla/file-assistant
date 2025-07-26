package net.konjarla.aifilemonitor.llm;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.konjarla.aifilemonitor.tools.model.FileClassification;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmService {
    @NonNull
    private final ChatClient photoChatClient;
    @NonNull
    private final ChatClient openAiclassificationChatClient;

    public FileClassification processPhotoContents(File file) {
        int tryCount = 0;
        while (tryCount < 3) {
            try {
                UserMessage userMessage = UserMessage.builder()
                        .text("Explain what do you see on this picture?")
                        .media(new Media(MimeTypeUtils.IMAGE_PNG, getResourceFromFile(file)))
                        .build();
                FileClassification response = photoChatClient
                        .prompt(new Prompt(List.of(userMessage)))
                        .call()
                        .entity(FileClassification.class);
                //.content();
                assert response != null;
                return response;
            } catch (Exception e) {
                log.error("Error processing image: {}", file, e);
                log.info("Processed Image. Retrying...{} of 3", tryCount);
                tryCount++;
            }
        }
        throw new RuntimeException("Failed to process image after 3 tries");
    }

    public FileClassification classifyText(String text) {
        int tryCount = 0;
        while (tryCount < 3) {
            try {
                UserMessage userMessage = UserMessage.builder()
                        .text(text)
                        .build();
                FileClassification response = openAiclassificationChatClient
                        .prompt(new Prompt(List.of(userMessage)))
                        .call()
                        .entity(FileClassification.class);
                assert response != null;
                return response;
            } catch (Exception e) {
                log.error("Error processing text: {}", text, e);
                log.info("Processed text. Retrying...{} of 3", tryCount);
                tryCount++;
            }
        }
        throw new RuntimeException("Failed to process text after 3 tries");
    }

    private Resource getResourceFromFile(File file) {
        return new FileSystemResource(file);
    }
} 