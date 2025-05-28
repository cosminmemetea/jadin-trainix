package trainix.service;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoutineGeneratorService {

    private final OpenAiService openAi;

    public RoutineGeneratorService() {
        String key = System.getenv("OPENAI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Missing OPENAI_API_KEY");
        }
        // 60-second timeout on calls
        this.openAi = new OpenAiService(key, Duration.ofSeconds(60));
    }

    /**
     * Call ChatGPT to generate a 5-step daily routine for the given goal.
     */
    public List<String> generateRoutine(String goal) {
        String userPrompt = "Create a concise 5-step daily routine to achieve: \"" + goal + "\"";

        ChatCompletionRequest req = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(
                        new ChatMessage("system", "You are a helpful assistant that writes clear, numbered routines."),
                        new ChatMessage("user", userPrompt)
                ))
                .temperature(0.7)
                .maxTokens(300)
                .build();

        ChatCompletionChoice choice = openAi.createChatCompletion(req)
                .getChoices().get(0);

        // Split on line breaks, strip leading numbers & periods, drop empties
        return Arrays.stream(choice.getMessage().getContent().split("\\r?\\n"))
                .map(s -> s.replaceFirst("^[0-9]+[.)]\\s*", "").trim())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
