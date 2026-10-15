# Previsión por municipio — AEMET

Buscador de municipios de España con previsión del día siguiente (AEMET OpenData).
Backend Spring Boot + frontend Angular, separados en capas.

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

Sin instalar Java ni Node, solo con Docker y la key:

```bash
AEMET_API_KEY=xxx docker compose up --build
# front http://localhost:4200, back http://localhost:8080
```

Las imágenes también están publicadas en Docker Hub por si
se quieren usar directamente sin construir:

```bash
docker pull annnmv/aemet-backend:latest
docker pull annnmv/aemet-frontend:latest
```

## Cómo está organizado

```
backend/.../domain        modelo (Municipio, PrevisionDia, UnidadTemperatura) + puertos
backend/.../application   casos de uso (MunicipioService, PrevisionService)
backend/.../infrastructure
  in/rest                 controllers, rate limit y errores
  out/aemet               acceso a AEMET (flujo en 2 pasos)
frontend/src/app
  core                    modelos e iconos
  infrastructure          acceso al backend
  presentation            buscador
```

Rendimiento: el maestro de municipios (~3 MB) se descarga una vez y se filtra en
memoria; cada previsión se cachea 3 h con lock por municipio; rate limit 60/min por IP;
si AEMET falla, 502 con mensaje y se mantiene lo cacheado (más detalle en las respuestas enviadas con la entrega).

## Tests y CI

```bash
cd backend && mvn test   # unitarios + ArchUnit, sin key real
```
Cada push/PR ejecuta backend + build del front en GitHub Actions (`.github/workflows/ci.yml`).
