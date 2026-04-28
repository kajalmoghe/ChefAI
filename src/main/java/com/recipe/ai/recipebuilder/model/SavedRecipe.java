package com.recipe.ai.recipebuilder.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "saved_recipes")
public class SavedRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String recipeName;

    @Column(columnDefinition = "TEXT")
    private String description;

    // NEW: Column to store the cuisine type
    @Column(name = "cuisine")
    private String cuisine;

    @Column(name = "saved_at")
    private LocalDateTime savedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ElementCollection
    @CollectionTable(name = "saved_recipe_ingredients", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "ingredient")
    private List<String> ingredients;

    @ElementCollection
    @CollectionTable(name = "saved_recipe_steps", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "step", columnDefinition = "TEXT")
    private List<String> stepByStepInstructions;

    @ElementCollection
    @CollectionTable(name = "saved_recipe_alternatives", joinColumns = @JoinColumn(name = "recipe_id"))
    @MapKeyColumn(name = "ingredient_key")
    @Column(name = "alternative_value")
    private Map<String, String> ingredientAlternatives = new HashMap<>();

    @PrePersist
    protected void onCreate() {
        this.savedAt = LocalDateTime.now();
    }

    public SavedRecipe() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRecipeName() { return recipeName; }
    public void setRecipeName(String recipeName) { this.recipeName = recipeName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    public LocalDateTime getSavedAt() { return savedAt; }
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    public List<String> getStepByStepInstructions() { return stepByStepInstructions; }
    public void setStepByStepInstructions(List<String> stepByStepInstructions) { this.stepByStepInstructions = stepByStepInstructions; }
    public Map<String, String> getIngredientAlternatives() { return ingredientAlternatives; }
    public void setIngredientAlternatives(Map<String, String> ingredientAlternatives) { this.ingredientAlternatives = ingredientAlternatives; }
}