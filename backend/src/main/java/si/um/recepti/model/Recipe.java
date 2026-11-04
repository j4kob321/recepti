package si.um.recepti.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotBlank(message = "Naslov je obvezen")
    @Schema(example = "Palačinke")
    private String title;

    @Column(length = 1000)
    @Schema(example = "Klasične domače palačinke")
    private String description;

    @Column(length = 2000)
    @Schema(example = "250 g moke, 2 jajci, 5 dl mleka, ščepec soli")
    private String ingredients;

    @Column(length = 4000)
    @Schema(example = "Vse sestavine zmešaj v gladko testo in speci na vroči ponvi.")
    private String instructions;

    @Min(value = 1, message = "Čas priprave mora biti vsaj 1 minuta")
    @Schema(example = "30", description = "Čas priprave v minutah")
    private int preparationTime;

    @Min(value = 1, message = "Število porcij mora biti vsaj 1")
    @Schema(example = "4")
    private int servings;

    public Recipe() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public int getPreparationTime() { return preparationTime; }
    public void setPreparationTime(int preparationTime) { this.preparationTime = preparationTime; }

    public int getServings() { return servings; }
    public void setServings(int servings) { this.servings = servings; }
}
