package com.recipe.ai.recipebuilder.controller;


import com.recipe.ai.recipebuilder.dto.RecipeDto;
import com.recipe.ai.recipebuilder.model.SavedRecipe;
import com.recipe.ai.recipebuilder.model.User;
import com.recipe.ai.recipebuilder.Repository.SavedRecipeRepository;
import com.recipe.ai.recipebuilder.Repository.UserRepository;
import com.recipe.ai.recipebuilder.service.RecipeAiService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class RecipeController {

    private final RecipeAiService aiService;
    private final SavedRecipeRepository savedRecipeRepository;
    private final UserRepository userRepository;

    public RecipeController(RecipeAiService aiService,
                            SavedRecipeRepository savedRecipeRepository,
                            UserRepository userRepository) {
        this.aiService = aiService;
        this.savedRecipeRepository = savedRecipeRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/generate")
    public String generateRecipe(
            @RequestParam("ingredients") String ingredients,
            @RequestParam(value = "excludeRecipe", required = false) String excludeRecipe,
            @RequestParam(value = "cuisine", required = false, defaultValue = "Any") String cuisine,
            Model model) {

        RecipeDto recipe = aiService.generateRecipe(ingredients, excludeRecipe, cuisine);

        model.addAttribute("recipe", recipe);
        model.addAttribute("originalIngredients", ingredients);
        model.addAttribute("selectedCuisine", cuisine);
        model.addAttribute("isSavedView", false);
        return "recipe";
    }

    @PostMapping("/recipe/save")
    public String saveRecipe(
            @RequestParam("recipeName") String recipeName,
            @RequestParam("description") String description,
            @RequestParam(value = "cuisine", required = false, defaultValue = "Any") String cuisine,
            @RequestParam(value = "ingredients", required = false) List<String> ingredients,
            @RequestParam(value = "stepByStepInstructions", required = false) List<String> stepByStepInstructions,
            @RequestParam(value = "alternatives", required = false) List<String> alternatives,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, String> alternativesMap = new HashMap<>();
        if (alternatives != null) {
            for (String alt : alternatives) {
                String[] parts = alt.split("\\|");
                if (parts.length == 2) {
                    alternativesMap.put(parts[0], parts[1]);
                }
            }
        }

        SavedRecipe savedRecipe = new SavedRecipe();
        savedRecipe.setRecipeName(recipeName);
        savedRecipe.setDescription(description);
        savedRecipe.setCuisine(cuisine); // Save the cuisine here!
        savedRecipe.setIngredients(ingredients);
        savedRecipe.setStepByStepInstructions(stepByStepInstructions);
        savedRecipe.setIngredientAlternatives(alternativesMap);
        savedRecipe.setUser(user);

        savedRecipeRepository.save(savedRecipe);

        return "redirect:/my-recipes";
    }

    @GetMapping("/my-recipes")
    public String viewSavedRecipes(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SavedRecipe> myRecipes = savedRecipeRepository.findByUserOrderBySavedAtDesc(user);
        model.addAttribute("recipes", myRecipes);
        return "my-recipes";
    }

    @GetMapping("/recipe/{id}")
    public String viewSingleRecipe(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        SavedRecipe savedRecipe = savedRecipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        if (!savedRecipe.getUser().getUsername().equals(userDetails.getUsername())) {
            return "redirect:/my-recipes";
        }

        RecipeDto dto = new RecipeDto(
                savedRecipe.getRecipeName(),
                savedRecipe.getDescription(),
                savedRecipe.getIngredients(),
                savedRecipe.getIngredientAlternatives(),
                savedRecipe.getStepByStepInstructions()
        );

        model.addAttribute("recipe", dto);
        model.addAttribute("recipeId", savedRecipe.getId());
        // Load the actual saved cuisine from the database
        model.addAttribute("selectedCuisine", savedRecipe.getCuisine() != null ? savedRecipe.getCuisine() : "Any");
        model.addAttribute("isSavedView", true);
        return "recipe";
    }

    @PostMapping("/recipe/delete/{id}")
    public String deleteRecipe(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        SavedRecipe savedRecipe = savedRecipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        if (savedRecipe.getUser().getUsername().equals(userDetails.getUsername())) {
            savedRecipeRepository.delete(savedRecipe);
        }

        return "redirect:/my-recipes";
    }
}