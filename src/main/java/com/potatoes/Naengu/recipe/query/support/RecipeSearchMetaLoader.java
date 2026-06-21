package com.potatoes.Naengu.recipe.query.support;

import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.recipe.domain.model.Recipe;
import com.potatoes.Naengu.recipe.dto.RecipeCountDto;
import com.potatoes.Naengu.recipe.dto.RecipeIngredientIdDto;
import com.potatoes.Naengu.recipe.repository.ProfileFavoriteRecipeRepository;
import com.potatoes.Naengu.recipe.repository.RecipeIngredientRepository;
import com.potatoes.Naengu.reviewrecipe.repository.RecipeReviewRepository;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeSearchMetaLoader {

    private final RecipeIngredientRepository recipeIngredientRepository;
    private final ProfileFavoriteRecipeRepository profileFavoriteRecipeRepository;
    private final RecipeReviewRepository recipeReviewRepository;

    public RecipeSearchMeta load(List<Recipe> recipes, Set<Long> fridgeIngredientIds, Profile profile) {
        if (recipes.isEmpty()) {
            return RecipeSearchMeta.empty();
        }

        List<Long> recipeIds = recipes.stream()
                .map(Recipe::getId)
                .toList();

        return new RecipeSearchMeta(
                toCountMap(recipeIngredientRepository.countByRecipeIds(recipeIds)),
                toCountMap(profileFavoriteRecipeRepository.countByRecipeIds(recipeIds)),
                toCountMap(recipeReviewRepository.countByRecipeIds(recipeIds)),
                calculateMatchedIngredientCountMap(recipeIds, fridgeIngredientIds),
                new HashSet<>(profileFavoriteRecipeRepository.findLikedRecipeIdsByProfile(profile, recipeIds))
        );
    }

    public RecipeSearchMeta loadAnonymous(List<Recipe> recipes) {
        if (recipes.isEmpty()) {
            return RecipeSearchMeta.empty();
        }

        List<Long> recipeIds = recipes.stream()
                .map(Recipe::getId)
                .toList();

        return new RecipeSearchMeta(
                toCountMap(recipeIngredientRepository.countByRecipeIds(recipeIds)),
                toCountMap(profileFavoriteRecipeRepository.countByRecipeIds(recipeIds)),
                toCountMap(recipeReviewRepository.countByRecipeIds(recipeIds)),
                Collections.emptyMap(),
                Collections.emptySet()
        );
    }

    private Map<Long, Integer> toCountMap(List<RecipeCountDto> counts) {
        return counts.stream()
                .collect(Collectors.toMap(
                        RecipeCountDto::recipeId,
                        count -> Math.toIntExact(count.count())
                ));
    }

    private Map<Long, Integer> calculateMatchedIngredientCountMap(
            List<Long> recipeIds,
            Set<Long> fridgeIngredientIds
    ) {
        if (fridgeIngredientIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Set<Long>> recipeIngredientIdsMap = new HashMap<>();

        List<RecipeIngredientIdDto> rows =
                recipeIngredientRepository.findIngredientIdsByRecipeIds(recipeIds);

        for (RecipeIngredientIdDto row : rows) {
            recipeIngredientIdsMap
                    .computeIfAbsent(row.recipeId(), id -> new HashSet<>())
                    .add(row.ingredientId());
        }

        Map<Long, Integer> result = new HashMap<>();

        for (Map.Entry<Long, Set<Long>> entry : recipeIngredientIdsMap.entrySet()) {
            Set<Long> copiedIngredientIds = new HashSet<>(entry.getValue());
            copiedIngredientIds.retainAll(fridgeIngredientIds);
            result.put(entry.getKey(), copiedIngredientIds.size());
        }

        return result;
    }
}
