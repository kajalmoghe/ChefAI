package com.recipe.ai.recipebuilder.service;


import com.recipe.ai.recipebuilder.dto.RecipeDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

@Service
public class RecipeAiService {

    private final ChatClient chatClient;

    public RecipeAiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public RecipeDto generateRecipe(String ingredients, String excludeRecipeName, String cuisine) {
        BeanOutputConverter<RecipeDto> converter = new BeanOutputConverter<>(RecipeDto.class);
        String format = converter.getFormat();

        String promptText = """
            You are a master chef. Create a highly delicious and creative recipe using primarily these ingredients: {ingredients}.
            You may add common pantry staples (salt, pepper, oil, water, basic spices) if necessary.
            
            {cuisineClause}
            
            {exclusionClause}
            
            Strictly format your response as a JSON object matching this schema:
            {format}
            """;

        String exclusionClause = (excludeRecipeName != null && !excludeRecipeName.isBlank())
                ? "CRITICAL: Do NOT suggest the recipe named '" + excludeRecipeName + "'. Give me a completely different dish."
                : "";

        String cuisineClause = (cuisine != null && !cuisine.equalsIgnoreCase("Any"))
                ? "CRITICAL: The recipe MUST strictly be an authentic or fusion " + cuisine + " style dish. Ensure the flavor profile matches this cuisine."
                : "You can choose any cuisine style that best fits the ingredients.";

        String response = chatClient.prompt()
                .user(u -> u.text(promptText)
                        .param("ingredients", ingredients)
                        .param("cuisineClause", cuisineClause)
                        .param("exclusionClause", exclusionClause)
                        .param("format", format))
                .call()
                .content();

        return converter.convert(response);
    }
}