package si.um.recepti.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import si.um.recepti.model.Recipe;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    /**
     * Iskanje in filtriranje v eni poizvedbi:
     *  - search: besedilo, ki ga vsebuje naslov ALI sestavine (brez razlike med velikimi in malimi črkami),
     *  - maxTime: največji čas priprave v minutah.
     * Prazen niz pri search in zelo velika vrednost pri maxTime pomenita "brez omejitve".
     */
    @Query("""
            SELECT r FROM Recipe r
            WHERE r.preparationTime <= :maxTime
              AND (LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(r.ingredients) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY r.id
            """)
    List<Recipe> searchAndFilter(@Param("search") String search, @Param("maxTime") int maxTime);
}
