# Vaja 2 – Aplikacija z recepti (nadgradnja)

**Avtor:** Ime Priimek (vpisna številka)

## 1. Nadgradnja ideje

Tema ostaja **aplikacija z recepti** iz vaje 1. Ideja je razširjena v osnovno delujočo spletno aplikacijo: uporabnik v brskalniku dodaja, pregleduje, ureja in briše recepte. Vsak recept ima naslov, opis, sestavine, postopek, čas priprave in število porcij. Aplikacija omogoča tudi **iskanje** po naslovu in sestavinah ter **filtriranje** po času priprave. V naslednjih nalogah načrtujem še uporabniške račune.

## 2. Arhitektura

Frontend in backend sta ločena projekta v ločenih mapah:

```
Priimek_vaja2/
├── backend/            Spring Boot (REST API, Swagger) – vrata 8080
├── frontend/           Node.js + Express (uporabniški vmesnik) – vrata 3000
├── docker-compose.yml  PostgreSQL baza – vrata 5432
└── slike/              zaslonski posnetki
```

```
Brskalnik → Frontend (Express, :3000) ──fetch──► Backend (Spring Boot, :8080) ──JPA──► PostgreSQL (:5432)
```

- **Backend:** Java 17, Spring Boot 3.3.5, Spring Data JPA, Validation, springdoc-openapi (Swagger UI), PostgreSQL gonilnik.
- **Frontend:** Node.js, Express (postreže statične datoteke), HTML/CSS/JavaScript. Podatke pridobiva izključno prek REST API-ja backenda. Backend ima nastavljen CORS za `http://localhost:3000`.

## 3. Podatkovna baza (PostgreSQL)

Baza `recepti` vsebuje tabelo `recipe`, ki jo Hibernate ustvari samodejno (`ddl-auto=update`).

| Stolpec | Tip | Opis |
|---------|-----|------|
| `id` | bigint (PK, samodejno) | primarni ključ |
| `title` | varchar | naslov (obvezen) |
| `description` | varchar(1000) | kratek opis |
| `ingredients` | varchar(2000) | sestavine |
| `instructions` | varchar(4000) | postopek |
| `preparation_time` | int | čas priprave v minutah (≥ 1) |
| `servings` | int | število porcij (≥ 1) |

## 4. REST API in Swagger

Swagger UI: **http://localhost:8080/swagger-ui.html** (OpenAPI JSON: `/v3/api-docs`). Vse operacije so opisane z oznakami `@Operation` in `@ApiResponse`, model pa ima primere vrednosti.

| Operacija | Metoda | URL | Odgovor |
|-----------|--------|-----|---------|
| Create | POST | `/api/recipes` | 201 / 400 |
| Read (vsi) | GET | `/api/recipes` | 200 |
| Iskanje in filter | GET | `/api/recipes?search=moka&maxTime=30` | 200 |
| Read (eden) | GET | `/api/recipes/{id}` | 200 / 404 |
| Update | PUT | `/api/recipes/{id}` | 200 / 400 / 404 |
| Delete | DELETE | `/api/recipes/{id}` | 204 / 404 |

Primer telesa:

```json
{
  "title": "Palačinke",
  "description": "Klasične domače palačinke",
  "ingredients": "250 g moke, 2 jajci, 5 dl mleka, ščepec soli",
  "instructions": "Vse sestavine zmešaj v gladko testo in speci na vroči ponvi.",
  "preparationTime": 30,
  "servings": 4
}
```

### Dodatni funkcionalnosti

**Iskanje po naslovu in sestavinah** (`search`): vrne recepte, katerih naslov ali sestavine vsebujejo iskano besedilo. Razlike med velikimi in malimi črkami ni (iskanje `moka` najde tudi `Moka`).

**Filtriranje po času priprave** (`maxTime`): vrne recepte, katerih čas priprave je manjši ali enak podani vrednosti (v minutah).

Oba parametra sta neobvezna in ju je mogoče združiti (veljati morata oba pogoja). Primeri:

| Zahtevek | Rezultat |
|----------|----------|
| `GET /api/recipes` | vsi recepti |
| `GET /api/recipes?search=moka` | recepti z besedo "moka" v naslovu ali sestavinah |
| `GET /api/recipes?maxTime=30` | recepti s časom priprave do 30 min |
| `GET /api/recipes?search=moka&maxTime=30` | oba pogoja hkrati |

**Backend:** poizvedba `searchAndFilter` v `RecipeRepository` (JPQL `LIKE` in `<=`) ter dva neobvezna parametra v `RecipeController.getAll`.
**Frontend:** iskalno polje (poizvedba se pošlje 300 ms po zadnji črki), spustni seznam časov priprave, gumb *Počisti* in prikaz števila najdenih receptov.

Napake se vračajo v enotni obliki `{"message": "..."}`.

## 5. Zagon

**1. Baza** (potreben Docker Desktop):

```bash
docker compose up -d
```

Brez Dockerja namestite PostgreSQL in ustvarite bazo `recepti` (uporabnik `postgres`, geslo `postgres`, vrata 5432). Drugačne nastavitve se lahko podajo z okoljskimi spremenljivkami `DB_URL`, `DB_USER`, `DB_PASSWORD`.

**2. Backend:** v IntelliJ odprite mapo `backend` in zaženite `ReceptiApplication` (ali `mvn spring-boot:run`). Strežnik teče na `http://localhost:8080`.

**3. Frontend** (potreben Node.js):

```bash
cd frontend
npm install
npm start
```

Aplikacija je dostopna na **http://localhost:3000**.

## 6. Testiranje

### Frontend (brskalnik)

**Prikaz seznama receptov (Read – vsi)**
![Seznam](../slike/frontend_list.png)

**Dodajanje recepta (Create)**
![Create](../slike/frontend_create.png)

**Podrobnosti recepta (Read – eden)**
![Detail](../slike/frontend_detail.png)

**Urejanje recepta (Update)**
![Update](../slike/frontend_update.png)

**Brisanje recepta (Delete)**
![Delete](../slike/frontend_delete.png)

**Iskanje (npr. "moka")**
![Iskanje](../slike/frontend_search.png)

**Filtriranje po času priprave (npr. do 30 min)**
![Filter](../slike/frontend_filter.png)

**Iskanje in filter skupaj**
![Iskanje in filter](../slike/frontend_search_filter.png)

### Swagger UI

**Pregled dokumentiranih operacij**
![Swagger](../slike/swagger.png)

**Preizkus POST /api/recipes v Swaggerju**
![Swagger POST](../slike/swagger_post.png)

**Preizkus GET /api/recipes z `search` in `maxTime`**
![Swagger iskanje](../slike/swagger_search.png)

### Podatkovna baza

**Tabela `recipe` v PostgreSQL (pgAdmin ali `psql`)**
![Baza](../slike/db_tables.png)
