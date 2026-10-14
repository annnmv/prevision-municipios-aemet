# Previsión por municipio — AEMET

Buscador de municipios de España con previsión del día siguiente (AEMET OpenData).
Backend Spring Boot + frontend Angular, organizados en capas estilo hexagonal.

## API key de AEMET (2 min)

1. Date de alta en https://opendata.aemet.es/centrodedescargas/altaUsuario (email).
2. Te llega la key al correo (mira spam).
3. Expórtala (nunca va en el código):
```bash
export AEMET_API_KEY="tu_key_aqui"
```

## Arranque en local

```bash
# Backend :8080 (otra terminal con AEMET_API_KEY exportada)
cd backend
mvn spring-boot:run
# Swagger: http://localhost:8080/swagger-ui.html

# Frontend :4200
cd frontend
npm install
npm start
# http://localhost:4200
```

## Endpoints

- `GET /api/municipios?nombre=mad` — busca municipios (mínimo 2 letras, máx 20).
  Ej: `[{"codigo":"28079","nombre":"Madrid"}, ...]`
- `GET /api/prevision/{codigo}?unidad=G_CEL|G_FAH` — previsión del día siguiente.
  `unidad` opcional, por defecto `G_CEL`. Ej (`unidad=G_FAH`):
```json
{
  "mediaTemperatura": 69.8,
  "unidadTemperatura": "G_FAH",
  "probPrecipitacion": [
    {"probabilidad": 0, "periodo": "00-06"},
    {"probabilidad": 0, "periodo": "06-12"},
    {"probabilidad": 0, "periodo": "12-18"},
    {"probabilidad": 0, "periodo": "18-24"}
  ]
}
```

Supuestos: "día siguiente" = mañana en Europe/Madrid; la media es el promedio de las
temperaturas horarias que da AEMET (o (máx+mín)/2 si no hay desglose); tramos sin dato = 0.

## Docker

```bash
# Construir
docker build -t TU_USUARIO/aemet-backend ./backend
docker build -t TU_USUARIO/aemet-frontend ./frontend
# Probar en local (construye las imágenes y arranca todo)
AEMET_API_KEY=xxx docker compose up --build
# front http://localhost:4200, back http://localhost:8080
# Publicar en Docker Hub (requiere docker login)
docker tag aemet-meteo-app-backend TU_USUARIO/aemet-backend:latest
docker tag aemet-meteo-app-frontend TU_USUARIO/aemet-frontend:latest
docker push TU_USUARIO/aemet-backend:latest
docker push TU_USUARIO/aemet-frontend:latest
```

Mira el nombre exacto con `docker images` y sustituye `TU_USUARIO` por tu usuario de Docker Hub.

## Estructura (hexagonal)

```
backend/.../domain        modelo (Municipio, PrevisionDia, UnidadTemperatura) + puertos
backend/.../application   casos de uso (MunicipioService, PrevisionService)
backend/.../infrastructure
  in/rest                 controllers + filtro de rate limit + errores propios
  out/aemet               único que conoce AEMET (flujo en 2 pasos)
frontend/src/app
  core                    modelos + icono/condición
  infrastructure          llamadas al backend (HttpClient solo aquí)
  presentation            BuscadorComponent + iconos SVG propios
```

Rendimiento: el maestro de municipios (~3 MB) se descarga una vez y se filtra en
memoria; cada previsión se cachea 3 h con lock por municipio; rate limit 60/min por IP;
si AEMET falla, 502 con mensaje y se mantiene lo cacheado. Detalle en `PREGUNTAS.md`.

## Tests y CI

```bash
cd backend && mvn test   # unitarios + ArchUnit, sin key real
```
Cada push/PR ejecuta backend + build del front en GitHub Actions (`.github/workflows/ci.yml`).
