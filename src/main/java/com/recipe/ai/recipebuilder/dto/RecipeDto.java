package com.recipe.ai.recipebuilder.dto;


import java.util.List;
import java.util.Map;

public record RecipeDto(
        String recipeName,
        String description,
        List<String> ingredients,
        Map<String, String> ingredientAlternatives,
        List<String> stepByStepInstructions
) {}
