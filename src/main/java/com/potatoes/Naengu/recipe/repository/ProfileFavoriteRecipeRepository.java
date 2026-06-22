package com.potatoes.Naengu.recipe.repository;

import com.potatoes.Naengu.profile.domain.model.Profile;
import com.potatoes.Naengu.recipe.domain.model.ProfileFavoriteRecipe;
import com.potatoes.Naengu.recipe.domain.model.Recipe;
import com.potatoes.Naengu.recipe.dto.RecipeCountDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfileFavoriteRecipeRepository extends JpaRepository<ProfileFavoriteRecipe, Long> {

    boolean existsByProfileAndRecipe(Profile profile, Recipe recipe);

    Optional<ProfileFavoriteRecipe> findByProfileAndRecipe(Profile profile, Recipe recipe);

    long countByRecipe(Recipe recipe);

    @Query("""
            SELECT pfr FROM ProfileFavoriteRecipe pfr
            WHERE pfr.profile = :profile
            ORDER BY pfr.createdAt DESC, pfr.id DESC
            """)
    List<ProfileFavoriteRecipe> findByProfileLatest(
            @Param("profile") Profile profile,
            Pageable pageable
    );

    @Query("""
            SELECT pfr FROM ProfileFavoriteRecipe pfr
            WHERE pfr.profile = :profile
              AND (pfr.createdAt < :cursorCreatedAt
                   OR (pfr.createdAt = :cursorCreatedAt AND pfr.id < :cursorId))
            ORDER BY pfr.createdAt DESC, pfr.id DESC
            """)
    List<ProfileFavoriteRecipe> findByProfileAfterCursor(
            @Param("profile") Profile profile,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
               select new com.potatoes.Naengu.recipe.dto.RecipeCountDto(
                 pfr.recipe.id,
                 count(pfr)
               )
               from ProfileFavoriteRecipe pfr
               where pfr.recipe.id in :recipeIds
               group by pfr.recipe.id
            """)
    List<RecipeCountDto> countByRecipeIds(@Param("recipeIds") List<Long> recipeIds);

    @Query("""
               select pfr.recipe.id
               from ProfileFavoriteRecipe pfr
               where pfr.profile = :profile
               and pfr.recipe.id in :recipeIds
            """)
    List<Long> findLikedRecipeIdsByProfile(
            @Param("profile") Profile profile,
            @Param("recipeIds") List<Long> recipeIds
    );

}
