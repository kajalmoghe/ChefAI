package com.recipe.ai.recipebuilder.Repository;



import com.recipe.ai.recipebuilder.model.SavedRecipe;
import com.recipe.ai.recipebuilder.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SavedRecipeRepository extends JpaRepository<SavedRecipe, Long> {

    // Custom query to find all recipes saved by a specific user, ordered by newest first
    List<SavedRecipe> findByUserOrderBySavedAtDesc(User user);
}
