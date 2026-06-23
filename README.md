# proyint-backend

![CI/CD](https://github.com/ProyectoIntegradorCOS/proyint-backend/actions/workflows/deploy.yml/badge.svg)

Backend del proyecto **THAQHIRI** — API REST para el sistema de georreferenciación de la ONP. Desarrollado en Java 21 con Spring Boot 3.

## Tecnologías

- **Java 21** + **Spring Boot 3.5.7**
- **PostgreSQL** (via Spring Data JPA / Hibernate)
- **Docker** — imagen publicada en AWS ECR
- **GitHub Actions** — CI/CD con despliegue en EC2

## Endpoints principales

| Ruta | Descripción |
|---|---|
| `GET /actuator/health` | Health check |
| `GET /actuator/prometheus` | Métricas para Prometheus |
| `POST /login` | Autenticación |
| `/api/**` | Endpoints de negocio (gestión de visitas, colaboradores, mapas) |

## Ambientes

| Ambiente | EC2 | Puerto |
|---|---|---|
| dev | 44.237.58.16 | 5511 |
| qa | 44.245.108.123 | 5511 |
| prod | 44.238.218.85 | 5511 |

## Ejecución local

```bash
# Requiere PostgreSQL local o túnel al RDS de dev
mvn spring-boot:run
```

La configuración de conexión a BD se lee de `src/main/resources/application.yml` (no versionado).

## CI/CD

El pipeline `.github/workflows/deploy.yml` ejecuta:

1. **Tests**: `mvn test` — resultados publicados como artefacto `test-results` descargable en cada ejecución (Actions → Run → sección *Artifacts*)
2. **Build**: imagen Docker → push a ECR `thaqhiri-dev`
3. **Deploy Dev**: SSH a EC2 dev → `docker pull` + `docker run` (automático al mergear a `main`)
4. **Deploy QA**: requiere aprobación manual en GitHub Environment `qa`
5. **Deploy Prod**: requiere aprobación manual en GitHub Environment `prod`

**Disparadores por rama (TBD):**
- `feature/*` / `fix/*` → PR a `main` → solo CI (tests + build, sin deploy)
- Merge a `main` → CI + CD completo (Dev → QA → Prod)

Se usa una única imagen Docker (`thaqhiri-dev`) para los 3 ambientes, diferenciada por variables de entorno en tiempo de ejecución.

## Estructura

```
src/main/java/pe/gob/onp/thaqhiri/
├── auth/         # Autenticación y seguridad
├── controller/   # Controladores REST
├── dto/          # Data Transfer Objects
├── entity/       # Entidades JPA
├── exception/    # Manejo de errores
├── health/       # Health indicators
├── metrics/      # Micrometer / Prometheus
└── config/       # Configuración Spring
```
