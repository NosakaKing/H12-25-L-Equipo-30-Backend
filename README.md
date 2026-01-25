# ✈️ FlightOnTime - Sistema de Predicción de Retrasos de Vuelos

Sistema completo de predicción inteligente de retrasos de vuelos en Estados Unidos, combinando Machine Learning avanzado con una interfaz web moderna y 3D interactiva.

---

- [Descripción General](#-descripción-general)
- [Enlaces del Proyecto](#-enlaces-del-proyecto)
- [Arquitectura del Sistema](#arquitectura-del-sistema)
- [Instalación y Ejecución](#-instalación-y-ejecución)
- [Dependencias y Versiones](#-dependencias-y-versiones)
- [Ejemplos de Petición y Respuesta](#-ejemplos-de-petición-y-respuesta)
- [Dataset Utilizado](#-dataset-utilizado)
- [Características del Frontend](#-características-del-frontend)
- [Configuración Avanzada](#-configuración-avanzada)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Testing](#-testing)
- [Troubleshooting](#-troubleshooting)
- [Licencia](#-licencia)
- [Equipo](#-equipo)

---

## 📋 Descripción General

**FlightOnTime** utiliza un modelo Random Forest optimizado para predecir si un vuelo se retrasará más de 15 minutos, basándose en:

- 📊 Datos históricos de 6.5M de vuelos (2019)
- 🌤️ Condiciones meteorológicas en tiempo real
- ✈️ Información operativa de aerolíneas
- 🏢 Patrones de tráfico aeroportuario

### 🔗 Enlaces del Proyecto

Acceso directo a los recursos fundamentales del sistema:

- **Código Fuente

- **Repositorio en GitHub Data Science**: [H12-25-L-Equipo-30-Data-Science](https://github.com/Giaioneg/H12-25-L-Equipo-30-Data-Science)
- **Repositorio en GitHub FrontEnd**: [H12-25-L-Equipo-30-Data-Science](https://github.com/Giaioneg/flight-frontend-hackathon)
- **Repositorio en GitHub Backend**: [H12-25-L-Equipo-30-Data-Science](https://github.com/NosakaKing/H12-25-L-Equipo-30-Backend)

---

**Métricas del Modelo:**

- Accuracy: 76.77%
- Recall: 42% (detección de retrasos)
- Precision: 86% (clase mayoritaria)

---

## Arquitectura del Sistema

```text
┌─────────────────┐
│   FRONTEND      │  React + Three.js (Puerto 3000)
│   (3D + UI)     │  
└────────┬────────┘
         │ HTTP POST
         ▼
┌─────────────────┐
│   BACKEND       │  Java Spring Boot (Puerto 8080)
│   Microservice  │  • Caché Inteligente (DB)
│                 │  • Circuit Breaker
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌─────────┐ ┌──────────────┐
│ Cloud   │ │ Local ONNX   │
│ Python  │ │ Fallback     │
│ API     │ │ (Sin Internet)│
└─────────┘ └──────────────┘
    │
    ▼
┌─────────────────┐
│ Open-Meteo API  │  (Clima en Tiempo Real)
└─────────────────┘
```

**Sistema de 3 Capas de Resiliencia:**

1. **⚡ L1 - Caché**: Respuesta en <10ms si el vuelo ya fue consultado
2. **☁️ L2 - Cloud AI**: Predicción con Python + clima real (FastAPI)
3. **🛡️ L3 - Fallback Local**: Modelo ONNX embebido en Java (funciona offline)

---

## 🚀 Instalación y Ejecución

### Prerrequisitos

- **Java**: 17+
- **Python**: 3.9 - 3.11
- **Node.js**: 16+ (para servir frontend)
- **Maven**: 3.8+
- **RAM**: Mínimo 2GB disponibles

### 1️⃣ Clonar el Repositorio

```bash
git clone <repository-url>
cd FlightOnTime
```

### 2️⃣ Backend (Java Spring Boot)

```bash
cd backend

# Ejecutar con base de datos H2 en memoria (desarrollo)
./mvnw spring-boot:run

# O con Docker
docker build -t flightpredictor .
docker run -p 8080:8080 flightpredictor
```

**Servidor corriendo en:** `http://localhost:8080`

### 3️⃣ Servicio de Data Science (Python FastAPI)

```bash
cd data-science

# Crear entorno virtual
python -m venv .venv
source .venv/bin/activate  # Linux/Mac
.\.venv\Scripts\activate   # Windows

# Instalar dependencias
pip install -r requirements.txt

# Descomprimir modelo ONNX (si es necesario)
cd artifacts
unzip flight_delay_rf_weighted.onnx.zip
cd ..

# Iniciar API
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

**Servidor corriendo en:** `http://localhost:8000`

### 4️⃣ Frontend (Web App)

```bash
cd public

# Opción 1: Python
python -m http.server 3000

# Opción 2: Node.js
npx serve -p 3000
```

**Aplicación disponible en:** `http://localhost:3000`

---

## 📦 Dependencias y Versiones

### Backend (Java)

```xml
<!-- pom.xml principales -->
<java.version>17</java.version>
<spring-boot.version>3.5.0</spring-boot.version>

<!-- Dependencias clave -->
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- resilience4j-spring-boot3 (Circuit Breaker)
- onnxruntime (1.16.0) - Modelo local
- mysql-connector-java (Producción)
- h2database (Desarrollo)
```

### Data Science (Python)

```txt
pandas==2.1.4
numpy==1.26.2
scikit-learn==1.3.2
onnx==1.15.0
onnxruntime==1.16.3
fastapi==0.104.1
uvicorn==0.24.0
pydantic==2.5.2
requests==2.31.0
```

### Frontend

```json
{
  "dependencies": {
    "three": "^0.150.0",
    "three/examples/jsm/loaders/GLTFLoader": "^0.150.0"
  },
  "fonts": "Google Fonts - Inter (300-700)"
}
```

---

## 🔌 Ejemplos de Petición y Respuesta

### Endpoint Principal

**URL:** `POST http://localhost:8080/api/prediction`

**Headers:**

```http
Content-Type: application/json
```

### Ejemplo 1: Predicción Exitosa (Cloud)

**Request:**

```json
{
  "CARRIER_NAME": "Delta Air Lines Inc.",
  "DEPARTING_AIRPORT": "John F. Kennedy International",
  "DATE": "2026-01-28",
  "TIME": "14:30"
}
```

**Response (200 OK):**

```json
{
  "prediction": "RETRASADO",
  "probability": 0.73,
  "details": "Clima: Tiempo Real (Open-Meteo) | Riesgo Ruta: 0.24",
  "weather_used": {
    "rain": 0.45,
    "wind": 18.2
  },
  "source": "CLOUD_API"
}
```

### Ejemplo 2: Fallback Local (Sin Internet)

**Request:**

```json
{
  "CARRIER_NAME": "American Airlines Inc.",
  "DEPARTING_AIRPORT": "Chicago O'Hare International",
  "DATE": "2026-01-30",
  "TIME": "08:00"
}
```

**Response (200 OK):**

```json
{
  "prediction": "PUNTUAL",
  "probability": 0.28,
  "details": "Modelo local activado (sin conexión a cloud)",
  "source": "LOCAL_ONNX"
}
```

### Ejemplo 3: Caché (Consulta Repetida)

**Response (200 OK - <10ms):**

```json
{
  "prediction": "PUNTUAL",
  "probability": 0.15,
  "details": "Resultado desde caché",
  "source": "CACHE",
  "cached_at": "2026-01-24T10:30:00Z"
}
```

### Errores Comunes

**400 - Bad Request:**

```json
{
  "error": "Formato de fecha inválido",
  "message": "Use formato YYYY-MM-DD"
}
```

**503 - Service Unavailable:**

```json
{
  "error": "Modelo no disponible",
  "message": "El sistema está iniciando. Intente en 30 segundos."
}
```

---

## 📊 Dataset Utilizado

### Información del Dataset

- **Nombre:** 2019 Airline Delays and Cancellations
- **Fuente:** [Kaggle Dataset](https://www.kaggle.com/datasets/threnjen/2019-airline-delays-and-cancellations)
- **Registros:** 6,489,062 vuelos
- **Período:** Año 2019 (pre-pandemia)
- **Alcance:** Estados Unidos continental
- **Aerolíneas:** 17 operadores principales
- **Aeropuertos:** 96 hubs principales
- **Tamaño:** ~2GB (CSV)

### Variables Principales

| Variable | Descripción | Tipo |
| ---------- | ------------- | ------ |
| `DEP_DEL15` | Retraso >15 min (Target) | Binario (0/1) |
| `CARRIER_NAME` | Aerolínea | Categórico |
| `DEPARTING_AIRPORT` | Aeropuerto origen | Categórico |
| `MONTH` | Mes del año | Numérico (1-12) |
| `DAY_OF_WEEK` | Día de semana | Numérico (1-7) |
| `DEP_TIME_BLK` | Bloque horario | Categórico |
| `PRCP` | Precipitación (pulgadas) | Numérico |
| `AWND` | Velocidad viento (mph) | Numérico |
| `SNOW` | Nieve (pulgadas) | Numérico |
| `PLANE_AGE` | Edad del avión | Numérico |
| `CONCURRENT_FLIGHTS` | Tráfico simultáneo | Numérico |

### Preprocesamiento Aplicado

1. **Target Encoding:** Conversión de categorías a riesgo histórico
2. **Imputación:** Valores climáticos faltantes → 0
3. **Smart Lookups:** Datos operativos por aerolínea/aeropuerto
4. **Manejo de Desbalance:** Peso 3x para clase minoritaria (retrasos)

### Distribución de Clases

- **Puntual (0):** 81% de vuelos
- **Retrasado (1):** 19% de vuelos

---

## 🎨 Características del Frontend

### Tecnologías Visuales

- **Three.js:** Modelo 3D Airbus A380 rotando
- **CSS3:** Gradientes animados en rojo/azul
- **Responsive Design:** Adaptado a desktop, tablet y móvil
- **Efectos Modernos:** Blur, animaciones suaves, glassmorphism

### Diseño Responsive

| Dispositivo | Breakpoint | Características |
| ------------- | ------------ | ----------------- |
| Desktop | >768px | Layout completo, modelo 3D pantalla completa |
| Tablet | ≤768px | Textos escalables con `clamp()` |
| Móvil | ≤480px | Diseño compacto, botones táctiles grandes |

---

## 🛡️ Resiliencia y Rendimiento

### Circuit Breaker (Resilience4j)

```yaml
resilience4j:
  circuitbreaker:
    instances:
      cloudApi:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        permitted-number-of-calls-in-half-open-state: 3
```

### Estrategia de Fallback

1. Intenta Cloud API (timeout: 5s)
2. Si falla → Activa modelo ONNX local
3. Registra evento para monitoreo

### Optimizaciones

- **Caché de Predicciones:** Reduce carga en el modelo
- **ONNX Runtime:** 3-5x más rápido que Joblib
- **Lazy Loading:** Modelo carga solo cuando se necesita

---

## 🔧 Configuración Avanzada

### Variables de Entorno (Backend)

```bash
# Producción
SPRING_PROFILES_ACTIVE=prod
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=flightdb
MYSQL_USER=admin
MYSQL_PASSWORD=secure_password

# Cloud API
CLOUD_API_URL=http://localhost:8000
CLOUD_API_TIMEOUT=5000

# Modelo Local
LOCAL_MODEL_PATH=/app/artifacts/flight_delay_rf_weighted.onnx
```

### CORS (Desarrollo vs Producción)

**Desarrollo:**

```python
allow_origins=["*"]  # Python API
```

**Producción:**

```python
allow_origins=[
    "https://flightontime.com",
    "https://api.flightontime.com"
]
```

---

## 📁 Estructura del Proyecto

```text
FlightOnTime/
│
├── backend/                    # Java Spring Boot
│   ├── src/main/java/
│   │   └── com.flight.predictor/
│   ├── pom.xml
│   └── Dockerfile
│
├── data-science/               # Python FastAPI + ML
│   ├── artifacts/
│   │   ├── flight_delay_rf_weighted.onnx
│   │   ├── *_risk_map.joblib
│   │   └── frontend_options.json
│   ├── main.py
│   ├── requirements.txt
│   └── Prediction-model.ipynb
│
├── public/                     # Frontend Web
│   ├── index.html
│   ├── css/style.css
│   ├── js/
│   │   ├── script.js
│   │   └── background3d.js
│   └── assets/
│       └── models/a380.glb
│
└── README.md                   # Este archivo
```

---

## 🧪 Testing

### Health Check (Data Science)

```bash
cd data-science
python Utils/check_model_ready.py
```

**Salida esperada:**

```bash
✅ ¡MODELO LISTO! El servicio de Data Science está operativo.
```

### Test de Integración

```bash
# Backend + Python API corriendo
curl -X POST "http://localhost:8080/api/prediction" \
-H "Content-Type: application/json" \
-d '{
  "CARRIER_NAME": "United Air Lines Inc.",
  "DEPARTING_AIRPORT": "San Francisco International",
  "DATE": "2026-01-25",
  "TIME": "10:00"
}'
```

---

## 🐛 Troubleshooting

| Problema | Causa | Solución |
| ---------- | ------- | ---------- |
| Error 503 (Backend) | Modelo ONNX no cargado | Esperar 30s al inicio |
| Error 503 (Python) | ONNX Runtime no instalado | `pip install onnxruntime==1.16.3` |
| Modelo 3D no carga | Ruta incorrecta | Verificar `/public/assets/models/a380.glb` |
| Timeout en clima | Firewall bloquea Open-Meteo | Usar `verify=False` en requests |
| CORS error | Frontend en distinto origen | Configurar `allow_origins` en FastAPI |

---

## 📝 Licencia

Proyecto desarrollado para el Hackathon No Country.  
Todos los derechos reservados © 2026

---

## 👥 Equipo

- **Backend:** Java Spring Boot Team
- **Data Science:** ML/AI Team
- **Frontend:** UX/UI Design Team
- **3D Modeling:** Three.js Specialists

---

**✈️ FlightOnTime** - Predicción inteligente de retrasos con tecnología de vanguardia.
