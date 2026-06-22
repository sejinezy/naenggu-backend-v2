package com.potatoes.Naengu.recipe.repository;

import com.potatoes.Naengu.recipe.domain.model.Recipe;
import com.potatoes.Naengu.recipe.domain.model.RecipeIngredient;
import com.potatoes.Naengu.recipe.dto.RecipeCountDto;
import com.potatoes.Naengu.recipe.dto.RecipeIngredientIdDto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {

    List<RecipeIngredient> findByRecipe(Recipe recipe);

    int countByRecipe(Recipe recipe);

    @Query("""
                select new com.potatoes.Naengu.recipe.dto.RecipeCountDto(
                    ri.recipe.id,
                    count(ri)
                )
                from RecipeIngredient ri
                where ri.recipe.id in :recipeIds
                group by ri.recipe.id
            """)
    List<RecipeCountDto> countByRecipeIds(@Param("recipeIds") List<Long> recipeIds);


    @Query("""
               select new com.potatoes.Naengu.recipe.dto.RecipeIngredientIdDto(
                 ri.recipe.id,
                 ri.ingredient.id
               )
               from RecipeIngredient ri
               where ri.recipe.id in :recipeIds
            """)
    List<RecipeIngredientIdDto> findIngredientIdsByRecipeIds(@Param("recipeIds") List<Long> recipeIds);



}
