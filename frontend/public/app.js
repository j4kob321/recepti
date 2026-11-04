// Naslov zalednega sistema (Spring Boot)
const API = "http://localhost:8080/api";

let recipes = [];

const $ = (selector) => document.querySelector(selector);

// ---------- pomožne funkcije ----------

// Zaščita pred vstavljanjem HTML kode iz podatkov
function esc(value) {
  return String(value ?? "").replace(/[&<>"']/g, (c) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  }[c]));
}

async function api(path, options = {}) {
  const response = await fetch(API + path, {
    headers: { "Content-Type": "application/json" },
    ...options
  });
  if (!response.ok) {
    let message = "Napaka " + response.status;
    try {
      const data = await response.json();
      if (data.errors) message = Object.values(data.errors).join(", ");
      else if (data.message) message = data.message;
    } catch (e) { /* odgovor brez telesa */ }
    throw new Error(message);
  }
  return response.status === 204 ? null : response.json();
}

function showMessage(text, type = "ok") {
  const el = $("#message");
  el.textContent = text;
  el.className = "message " + type;
  clearTimeout(showMessage.timer);
  showMessage.timer = setTimeout(() => { el.className = "message hidden"; }, 4000);
}

// Izvede funkcijo in prikaže morebitno napako uporabniku
async function run(fn) {
  try {
    await fn();
  } catch (e) {
    const text = e instanceof TypeError
      ? "Strežnika ni mogoče doseči. Ali backend teče na vratih 8080?"
      : e.message;
    showMessage(text, "error");
  }
}

// ---------- branje (Read) ----------

// Vsi recepti ali rezultati iskanja in filtra (GET /recipes?search=...&maxTime=...)
async function loadAll() {
  const params = new URLSearchParams();
  const search = $("#search").value.trim();
  const maxTime = $("#max-time").value;
  if (search) params.set("search", search);
  if (maxTime) params.set("maxTime", maxTime);
  const query = params.toString();
  recipes = await api("/recipes" + (query ? "?" + query : ""));
  renderRecipes(query !== "");
}

function renderRecipes(filtered = false) {
  const box = $("#recipe-list");
  $("#result-count").textContent = filtered ? `Najdenih receptov: ${recipes.length}` : "";
  if (!recipes.length) {
    box.innerHTML = filtered
      ? '<p class="empty">Noben recept ne ustreza iskanju.</p>'
      : '<p class="empty">Ni receptov. Dodajte prvega!</p>';
    return;
  }
  box.innerHTML = recipes.map((r) => `
    <article class="card">
      <h3>${esc(r.title)}</h3>
      <p>${esc(r.description)}</p>
      <p class="meta">${r.preparationTime} min · ${r.servings} porcij</p>
      <div class="actions">
        <button data-action="view-recipe" data-id="${r.id}">Podrobnosti</button>
        <button data-action="edit-recipe" data-id="${r.id}">Uredi</button>
        <button class="danger" data-action="delete-recipe" data-id="${r.id}">Izbriši</button>
      </div>
    </article>`).join("");
}

// En recept (GET /recipes/{id})
async function viewRecipe(id) {
  const r = await api("/recipes/" + id);
  $("#detail-content").innerHTML = `
    <div class="detail">
      <h2>${esc(r.title)}</h2>
      <p class="meta">${r.preparationTime} min · ${r.servings} porcij</p>
      <h4>Opis</h4><p>${esc(r.description)}</p>
      <h4>Sestavine</h4><p>${esc(r.ingredients)}</p>
      <h4>Postopek</h4><p>${esc(r.instructions)}</p>
    </div>`;
  $("#detail-dialog").showModal();
}

// ---------- ustvari / uredi / izbriši ----------

function openRecipeForm(recipe) {
  $("#recipe-dialog-title").textContent = recipe ? "Uredi recept" : "Nov recept";
  $("#recipe-id").value = recipe ? recipe.id : "";
  $("#recipe-title").value = recipe ? recipe.title : "";
  $("#recipe-description").value = recipe?.description ?? "";
  $("#recipe-ingredients").value = recipe?.ingredients ?? "";
  $("#recipe-instructions").value = recipe?.instructions ?? "";
  $("#recipe-time").value = recipe ? recipe.preparationTime : 30;
  $("#recipe-servings").value = recipe ? recipe.servings : 4;
  $("#recipe-dialog").showModal();
}

async function editRecipe(id) {
  openRecipeForm(await api("/recipes/" + id));
}

// Create (POST) ali Update (PUT), odvisno od tega, ali je id že znan
async function saveRecipe() {
  const id = $("#recipe-id").value;
  const body = {
    title: $("#recipe-title").value.trim(),
    description: $("#recipe-description").value,
    ingredients: $("#recipe-ingredients").value,
    instructions: $("#recipe-instructions").value,
    preparationTime: Number($("#recipe-time").value),
    servings: Number($("#recipe-servings").value)
  };
  await api(id ? "/recipes/" + id : "/recipes", {
    method: id ? "PUT" : "POST",
    body: JSON.stringify(body)
  });
  $("#recipe-dialog").close();
  showMessage(id ? "Recept je posodobljen." : "Recept je dodan.");
  await loadAll();
}

async function deleteRecipe(id) {
  if (!confirm("Ali res želite izbrisati ta recept?")) return;
  await api("/recipes/" + id, { method: "DELETE" });
  showMessage("Recept je izbrisan.");
  await loadAll();
}

// ---------- dogodki ----------

const actions = {
  "new-recipe": () => openRecipeForm(null),
  "view-recipe": viewRecipe,
  "edit-recipe": editRecipe,
  "delete-recipe": deleteRecipe,
  "close-dialog": (id, button) => button.closest("dialog").close()
};

document.addEventListener("click", (e) => {
  const button = e.target.closest("[data-action]");
  if (button) run(() => actions[button.dataset.action](button.dataset.id, button));
});

$("#recipe-form").addEventListener("submit", (e) => { e.preventDefault(); run(saveRecipe); });

// Iskanje (z zakasnitvijo, da se ne pošlje zahtevek ob vsaki črki) in filter
let searchTimer;
$("#search").addEventListener("input", () => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => run(loadAll), 300);
});
$("#max-time").addEventListener("change", () => run(loadAll));
$("#clear-filters").addEventListener("click", () => {
  $("#search").value = "";
  $("#max-time").value = "";
  run(loadAll);
});

run(loadAll);
