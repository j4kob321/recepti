package si.um.recepti.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import si.um.recepti.model.Recipe;
import si.um.recepti.repository.RecipeRepository;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@Tag(name = "Recepti", description = "CRUD operacije nad recepti")
public class RecipeController {

    private final RecipeRepository repository;

    public RecipeController(RecipeRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Vrne recepte (z neobveznim iskanjem in filtriranjem)",
            description = "Brez parametrov vrne vse recepte. Parameter search poišče besedilo v naslovu ali sestavinah, "
                    + "maxTime pa omeji čas priprave. Če sta podana oba, morata biti izpolnjena oba pogoja.")
    public List<Recipe> getAll(
            @Parameter(description = "Iskano besedilo v naslovu ali sestavinah (brez razlike med velikimi in malimi črkami)", example = "moka")
            @RequestParam(required = false) String search,
            @Parameter(description = "Največji čas priprave v minutah", example = "30")
            @RequestParam(required = false) Integer maxTime) {
        String text = search == null ? "" : search.trim();
        int limit = maxTime == null ? Integer.MAX_VALUE : maxTime;
        return repository.searchAndFilter(text, limit);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Vrne en recept po id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recept najden"),
            @ApiResponse(responseCode = "404", description = "Recept ne obstaja")
    })
    public Recipe getOne(@PathVariable Long id) {
        return find(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Doda nov recept")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Recept ustvarjen"),
            @ApiResponse(responseCode = "400", description = "Neveljavni podatki")
    })
    public Recipe create(@Valid @RequestBody Recipe recipe) {
        recipe.setId(null); // id določi baza
        return repository.save(recipe);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Posodobi obstoječi recept")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recept posodobljen"),
            @ApiResponse(responseCode = "400", description = "Neveljavni podatki"),
            @ApiResponse(responseCode = "404", description = "Recept ne obstaja")
    })
    public Recipe update(@PathVariable Long id, @Valid @RequestBody Recipe updated) {
        Recipe existing = find(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setIngredients(updated.getIngredients());
        existing.setInstructions(updated.getInstructions());
        existing.setPreparationTime(updated.getPreparationTime());
        existing.setServings(updated.getServings());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Izbriše recept")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Recept izbrisan"),
            @ApiResponse(responseCode = "404", description = "Recept ne obstaja")
    })
    public void delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recept ne obstaja");
        }
        repository.deleteById(id);
    }

    private Recipe find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recept ne obstaja"));
    }
}
