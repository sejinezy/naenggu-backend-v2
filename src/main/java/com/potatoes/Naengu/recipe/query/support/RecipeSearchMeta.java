package com.potatoes.Naengu.recipe.query.support;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public record RecipeSearchMeta(
        Map<Long, Integer> totalIngredientCountMap,
        Map<Long, Integer> likeCountMap,
        Map<Long, Integer> reviewCountMap,
        Map<Long, Integer> matchedIngredientCountMap,
        Set<Long> likedRecipeIds
) {
    public static RecipeSearchMeta empty() {
        return new RecipeSearchMeta(
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptySet()
        );
    }
}
