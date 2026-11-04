# Recepti – aplikacija za shranjevanje receptov

Spletna aplikacija za dodajanje, pregledovanje, urejanje, brisanje, iskanje in filtriranje kuharskih receptov.
Frontend (Node.js + Express) in backend (Spring Boot REST API) sta ločena projekta, podatki so shranjeni v PostgreSQL.

## Kazalo

- [O projektu](#o-projektu)
- [Uporabljene tehnologije](#uporabljene-tehnologije)
- [Struktura projekta](#struktura-projekta)
- [Navodila za nameščanje in zagon](#navodila-za-nameščanje-in-zagon)
- [REST API](#rest-api)
- [Navodila za razvijalce](#navodila-za-razvijalce)
- [Standardi kodiranja](#standardi-kodiranja)

## O projektu

Cilj projekta je preprosta, a popolna aplikacija za upravljanje zbirke receptov. Vsak recept ima naslov, opis, sestavine, postopek, čas priprave (v minutah) in število porcij.

**Funkcionalnosti**

- ustvarjanje, branje, urejanje in brisanje receptov (CRUD),
- iskanje po naslovu in sestavinah (brez razlike med velikimi in malimi črkami),
- filtriranje po največjem času priprave,
- validacija vnosa in enotne napake v obliki `{"message": "..."}`,
- dokumentacija API-ja v Swagger UI.

**Načrtovano:** uporabniški računi.

**Avtor:** Jakob Strmčnik 1002744059F

## Uporabljene tehnologije

| Del | Tehnologija | Verzija |
|---|---|---|
| Backend | Java (JDK) | 17 |
| Backend | Spring Boot (Web, Data JPA, Validation) | 3.3.5 |
| Backend | springdoc-openapi (Swagger UI) | 2.6.0 |
| Backend | PostgreSQL JDBC gonilnik | verzija iz Spring Boot BOM |
| Backend | Maven | 3.6.3 ali novejši |
| Baza | PostgreSQL (prek Docker Compose) | 16 |
| Frontend | Node.js | 18 ali novejši (razvito z 22) |
| Frontend | Express | ^4.19.2 |
| Frontend | HTML, CSS, JavaScript (brez ogrodja) | – |
| Orodja | Docker / Docker Compose | – |

## Struktura projekta

```
recepti/
├── backend/                          Spring Boot REST API (vrata 8080)
│   ├── pom.xml
│   └── src/main/
│       ├── java/si/um/recepti/
│       │   ├── ReceptiApplication.java
│       │   ├── config/               CORS, obravnava napak, Swagger
│       │   ├── controller/           REST končne točke
│       │   ├── model/                JPA entitete
│       │   └── repository/           dostop do baze
│       └── resources/
│           └── application.properties
├── frontend/                         Express strežnik + statični UI (vrata 3000)
│   ├── package.json
│   ├── server.js
│   └── public/                       index.html, style.css, app.js
├── docs/                             poročilo in dodatna dokumentacija
├── slike/                            zaslonski posnetki
├── docker-compose.yml                PostgreSQL baza (vrata 5432)
├── .gitignore
└── README.md
```

| Mapa / datoteka | Vsebina |
|---|---|
| `backend/.../config` | `CorsConfig` (dovoli klice s `http://localhost:3000`), `GlobalExceptionHandler` (enotne napake), `OpenApiConfig` (Swagger) |
| `backend/.../controller` | `RecipeController` – REST končne točke `/api/recipes` |
| `backend/.../model` | `Recipe` – entiteta z validacijskimi pravili |
| `backend/.../repository` | `RecipeRepository` – JPA repozitorij in poizvedba `searchAndFilter` |
| `frontend/server.js` | Express strežnik, ki postreže statične datoteke |
| `frontend/public` | uporabniški vmesnik; podatke pridobiva prek `fetch` iz backenda |
| `docker-compose.yml` | zagon PostgreSQL baze `recepti` |

Arhitektura:

```
Brskalnik → Frontend (Express :3000) ──fetch──► Backend (Spring Boot :8080) ──JPA──► PostgreSQL (:5432)
```

## Navodila za nameščanje in zagon

### Predpogoji

| Orodje | Preverite z | Opomba |
|---|---|---|
| Git | `git --version` | |
| JDK 17+ | `java -version` | |
| Maven 3.6.3+ | `mvn -version` | ni potreben, če backend zaženete v IntelliJ IDEA |
| Node.js 18+ (z npm) | `node -v` | |
| Docker Desktop | `docker --version` | ali ročno nameščen PostgreSQL 16 |

### 1. Kloniranje

```bash
git clone https://github.com/j4kob321/recepti.git
cd recepti
```

### 2. Baza (PostgreSQL)

**Možnost A – Docker (priporočeno):**

```bash
docker compose up -d
```

Ustvari bazo `recepti` (uporabnik `postgres`, geslo `postgres`, vrata `5432`). Zaustavitev: `docker compose down` (podatki ostanejo v volumnu `pgdata`).

**Možnost B – lasten PostgreSQL:** namestite PostgreSQL 16 in ustvarite bazo:

```sql
CREATE DATABASE recepti;
```

Tabelo `recipe` ustvari Hibernate samodejno (`ddl-auto=update`), skriptov ni treba zagnati.

### 3. Okoljske spremenljivke

Privzete vrednosti ustrezajo `docker-compose.yml`, zato jih ni treba nastavljati. Drugačne nastavitve lahko podate z okoljskimi spremenljivkami:

| Spremenljivka | Opis | Privzeto |
|---|---|---|
| `DB_URL` | JDBC povezava | `jdbc:postgresql://localhost:5432/recepti` |
| `DB_USER` | uporabnik baze | `postgres` |
| `DB_PASSWORD` | geslo baze | `postgres` |
| `PORT` (frontend) | vrata frontenda | `3000` |

### 4. Zagon backenda

```bash
cd backend
mvn spring-boot:run
```

Ali v IntelliJ IDEA: odprite mapo `backend` in zaženite `ReceptiApplication`.

- API: http://localhost:8080/api/recipes
- Swagger UI: http://localhost:8080/swagger-ui.html

### 5. Zagon frontenda

V novem terminalu:

```bash
cd frontend
npm install
npm start
```

Aplikacija je dostopna na **http://localhost:3000**.

> Backend mora teči pred uporabo frontenda. Frontend kliče `http://localhost:8080/api`, CORS pa je v backendu dovoljen samo za `http://localhost:3000`. Če spremenite vrata, popravite `API` v `frontend/public/app.js` in `CorsConfig.java`.

### Pogosti problemi

| Težava | Rešitev |
|---|---|
| `Connection refused` na vratih 5432 | baza ne teče – `docker compose up -d` |
| Vrata 8080 / 3000 / 5432 so zasedena | zaustavite drug proces ali spremenite vrata |
| Napaka CORS v brskalniku | frontend mora teči na `http://localhost:3000` |

## REST API

| Operacija | Metoda | URL | Odgovor |
|---|---|---|---|
| Vsi recepti (z iskanjem in filtrom) | GET | `/api/recipes?search=moka&maxTime=30` | 200 |
| En recept | GET | `/api/recipes/{id}` | 200 / 404 |
| Nov recept | POST | `/api/recipes` | 201 / 400 |
| Posodobitev | PUT | `/api/recipes/{id}` | 200 / 400 / 404 |
| Brisanje | DELETE | `/api/recipes/{id}` | 204 / 404 |

Parametra `search` in `maxTime` sta neobvezna in ju je mogoče združiti. Primer telesa zahtevka:

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

Podrobno poročilo z zaslonskimi posnetki: [docs/vaja2-porocilo.md](docs/vaja2-porocilo.md).

## Navodila za razvijalce

### Potek dela z Gitom

1. Posodobite glavno vejo: `git checkout main && git pull origin main`
2. Ustvarite vejo za nalogo: `git checkout -b feature/kratek-opis`
3. Delajte v majhnih, smiselnih commitih.
4. Objavite vejo: `git push -u origin feature/kratek-opis`
5. Na GitHubu odprite **Pull Request** proti `main`, opišite spremembe in počakajte na pregled.
6. Po odobritvi združite (merge) in izbrišite vejo.

Neposredno pisanje v `main` se izogibamo.

### Poimenovanje vej

| Predpona | Namen | Primer |
|---|---|---|
| `feature/` | nova funkcionalnost | `feature/uporabniski-racuni` |
| `bugfix/` | popravek napake | `bugfix/validacija-casa` |
| `docs/` | dokumentacija | `docs/posodobitev-readme` |

### Sporočila commitov

Oblika `tip: kratek opis v povednem naklonu`, kjer je tip `feat`, `fix`, `docs`, `refactor` ali `test`.
Primeri: `feat: dodano iskanje po sestavinah`, `fix: popravek filtra časa priprave`.

### Pred pull requestom

- backend se prevede in zažene (`mvn spring-boot:run`),
- aplikacija deluje po zgornjih navodilih na čisti bazi,
- v repozitorij ne dodajate `target/`, `node_modules/` in gesel.

## Standardi kodiranja

**Java (backend)**

- paketi z malimi črkami (`si.um.recepti`), razredi `PascalCase`, metode in spremenljivke `camelCase`, konstante `UPPER_SNAKE_CASE`,
- plastna arhitektura: `controller` → `repository` (poslovna logika, ko bo obsežnejša, v `service`),
- validacija z Bean Validation (`@NotBlank`, `@Min`) na entitetah,
- REST končne točke dokumentirajte z `@Operation` in `@ApiResponse`,
- napake vračajte v obliki `{"message": "..."}` prek `GlobalExceptionHandler`,
- nastavitve, ki so odvisne od okolja, naj gredo v okoljske spremenljivke, ne v kodo.

**JavaScript (frontend)**

- `camelCase` za spremenljivke in funkcije, `const`/`let` namesto `var`,
- vsi podatki prihajajo prek REST API-ja, brez trdo kodiranih podatkov,
- 2 presledka za zamik, podpičja na koncu stavkov.

**Splošno**

- imena v kodi so angleška (`Recipe`, `preparationTime`), uporabniška besedila so slovenska,
- komentarji kratki in pojasnjujejo *zakaj*, ne *kaj*,
- gesla in skrivnosti se ne objavljajo v repozitorij.
