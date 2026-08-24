# ReadApp

Aplicación end-to-end para la gestión, consulta y préstamo de libros, desarrollada como proyecto universitario. La solución combina una base de datos relacional, una base de datos no relacional y caché para ofrecer una experiencia completa de búsqueda, reserva y administración bibliográfica.

## Descripción general

ReadApp es una plataforma web orientada a la gestión de libros y préstamos entre usuarios, con funcionalidades para:

- buscar y filtrar libros por género, idioma, estado y otros criterios
- consultar detalles de cada libro
- registrar usuarios con distintos roles
- reservar y administrar préstamos
- evaluar libros y visualizar métricas
- gestionar contenido y datos desde un panel administrativo

La aplicación fue diseñada como un proyecto integral de software, integrando frontend, backend, bases de datos y servicios auxiliares en una arquitectura moderna y modular.

## Stack tecnológico

- Frontend: React + Vite
- Backend: Kotlin + Spring Boot
- Base de datos relacional: PostgreSQL
- Base de datos no relacional: MongoDB
- Caché: Redis
- Autenticación: JWT
- Seguridad: Spring Security
- APIs: REST y métricas con GraphQL
- Contenedores: Docker / Docker Compose

## Arquitectura

La aplicación se divide en dos grandes partes:

- Frontend: interfaz web para usuarios y administración
- Backend: lógica de negocio, autenticación, validaciones, persistencia y servicios

La persistencia se distribuye en:
- PostgreSQL para datos transaccionales y estructurados
- MongoDB para contenido, métricas y registros de uso
- Redis para cache y optimización de consultas frecuentes

## Funcionalidades principales

### Para lectores
- registro e inicio de sesión
- visualización del catálogo
- filtros avanzados de búsqueda
- consulta de detalles de libros
- reservas y préstamos
- historial de préstamos
- valoración de títulos

### Para publicadores
- publicación y edición de libros
- gestión del estado y disponibilidad
- visualización de libros propios
- mantenimiento del catálogo

### Para administración
- panel de KPIs y métricas
- seguimiento de actividad
- análisis de uso del sistema

## Flujo de funcionamiento

1. El usuario accede a la aplicación desde el frontend.
2. Se autentica mediante JWT.
3. El backend valida credenciales y roles.
4. El frontend consulta el catálogo y el perfil del usuario.
5. El backend obtiene información desde PostgreSQL y MongoDB según el caso.
6. Redis almacena o reutiliza resultados para acelerar consultas repetidas.
7. La aplicación entrega la información al usuario con una experiencia segura y rápida.

## Requisitos previos

Antes de iniciar el proyecto, asegurate de tener instalado:

- Java 17 o superior
- Gradle
- Node.js y npm
- Docker y Docker Compose
- Git

## Instalación y configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/FedericoTomasBalborin/ReadApp
cd ReadApp
```

### 2. Configurar variables de entorno

Crea un archivo de entorno para el backend con las credenciales necesarias para PostgreSQL, MongoDB, Redis y JWT.

Ejemplo de variables:

```env
POSTGRES_URL=jdbc:postgresql://localhost:5432/phm
POSTGRES_USER=postgres
POSTGRES_PSW=tu_password
MONGO_PSW=tu_password_mongo
REDIS_URL=redis://localhost:6379
JWT_SECRET_KEY=tu_clave_secreta_muy_segura
```

### 3. Levantar infraestructura

Desde la raíz del proyecto o del backend, levantá los servicios con Docker:

```bash
docker compose up -d
```

Esto inicia los servicios necesarios para la base de datos y la caché del sistema.

### 4. Ejecutar el backend

```bash
cd backend
./gradlew bootRun
```

### 5. Ejecutar el frontend

```bash
cd frontend
npm install
npm run dev
```

La aplicación quedará disponible en el puerto configurado por Vite, normalmente en el navegador local.

## Estructura del proyecto

```text
ReadApp/
├── backend/               # API, lógica de negocio, seguridad, configuración y persistencia
├── frontend/              # aplicación React/Vite para la interfaz de usuario
├── docs/                  # documentación técnica del proyecto
├── docker-compose.yml     # servicios de infraestructura
├── README.md              # documentación general del proyecto
└── LICENSE                # licencia del proyecto, si aplica
```

## Casos de uso principales

- un usuario quiere buscar un libro por título o autor
- un publicador quiere publicar o editar un ejemplar
- un lector quiere reservar un libro para préstamo
- un administrador quiere visualizar KPIs y métricas de actividad
- el sistema quiere reducir latencia usando Redis para consultas repetidas

## Documentación adicional

Este repositorio incluye documentación técnica y de dominio en distintas secciones del proyecto, enfocada en:

- controladores
- servicios
- DTOs
- repositorios
- modelos de dominio
- casos de uso y reglas de negocio

## Créditos y contribuciones

Este proyecto fue desarrollado como trabajo universitario y representa una integración completa de frontend, backend, infraestructura y bases de datos.

### Equipo / Github

- Federico Balborin (FedericoTomasBalborin)
- Martin del Lojo (martinmdl)
- Martin Benedetto (TinchoBtt)
- Franco Demaino (FrancoDemaino)
- Ivan Miranda (ivan1299)

- Profesores: Fernando Dodino (fdodino), Juan Contardo (Juancete)