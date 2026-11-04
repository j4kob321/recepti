const express = require("express");
const path = require("path");

const app = express();
const PORT = process.env.PORT || 3000;

// Strežnik samo postreže statične datoteke iz mape public.
// Vsi podatki prihajajo prek REST API-ja zaledja (Spring Boot, vrata 8080).
app.use(express.static(path.join(__dirname, "public")));

app.listen(PORT, () => {
  console.log(`Frontend teče na http://localhost:${PORT}`);
});
