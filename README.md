# CineMX

Aplicación Android de cartelera: consulta las películas en exhibición a partir de la API
de [TMDB](https://developer.themoviedb.org/) y las presenta con su ficha completa, sobre
una sesión autenticada con [Supabase](https://supabase.com).

Kotlin · Jetpack Compose · Material 3 · MVVM + Clean Architecture

| | |
|---|---|
| **minSdk** | 24 (Android 7.0) |
| **compileSdk / targetSdk** | 35 |
| **Lenguaje** | Kotlin 2.1, 100 % del código |
| **UI** | Jetpack Compose, sin XML de layouts |
| **Paquete** | `com.cinemx.movies` |

---

## Credenciales de prueba

La aplicación no incluye pantalla de registro: el alta de usuarios se administra desde
Supabase. Para evaluar la prueba puede accederse con esta cuenta, ya creada y confirmada:

| | |
|---|---|
| **Correo** | `test@cinemx.dev` |
| **Contraseña** | `Test1234` |

El acceso con Google también está operativo en el APK de release y puede probarse con
cualquier cuenta del dispositivo: Supabase da de alta al usuario en el primer inicio de
sesión correcto.

---

## Funcionalidad

**Acceso.** Correo y contraseña contra Supabase Auth, con validación local previa,
mensajes de error diferenciados (credenciales inválidas, correo sin confirmar, exceso de
intentos, falta de red) y acceso opcional con Google. La sesión persiste entre arranques.

**Cartelera.** Listado paginado de estrenos con scroll infinito y refresco por gesto.
Cada tarjeta muestra póster, título, fecha de estreno y calificación. Los estados de
carga, error, lista vacía y reintento están cubiertos tanto en la carga inicial como en
la paginación. Una fila de filtros bajo el saludo acota el listado por género, con
"Todos" seleccionado de origen.

**Ficha de la película.** Imagen de cabecera, título, duración, fecha de estreno
localizada, clasificación por edad, calificación, géneros y sinopsis. Cada campo degrada
a "N/D" cuando TMDB no lo proporciona, en lugar de desaparecer de la interfaz.

**Cierre de sesión.** Con confirmación previa y limpieza completa de la pila de
navegación: el botón "atrás" no regresa a la sesión cerrada.

**Transversal.** Tema claro y oscuro con paleta de marca propia, presentación de
borde a borde, descripciones de contenido para lectores de pantalla y todos los textos
externalizados en recursos.

---

## Arquitectura

```
┌─────────────────┐      ┌──────────────┐      ┌─────────────────┐
│  presentation   │─────▶│    domain    │◀─────│      data       │
│                 │      │              │      │                 │
│ Compose screens │      │  Modelos     │      │ Retrofit / TMDB │
│ ViewModels      │      │  UseCases    │      │ Supabase Auth   │
│ UiState         │      │  Interfaces  │      │ DTOs + mappers  │
└─────────────────┘      └──────────────┘      └─────────────────┘
```

**Regla de dependencia.** `domain` es Kotlin puro y no depende de ninguna otra capa ni
del framework de Android. Los contratos (`AuthRepository`, `MovieRepository`) viven ahí;
sus implementaciones, en `data`. Ningún DTO cruza hacia la interfaz de usuario: todo pasa
por un mapper que produce modelos de dominio ya formateados.

La consecuencia práctica es que la capa de presentación desconoce el proveedor de datos.
Sustituir Supabase por otro backend de autenticación, o TMDB por otra fuente, es escribir
otra implementación de la interfaz, sin tocar ViewModels, pantallas ni pruebas.

### Estructura

```
com.cinemx.movies/
├── CineMxApp.kt              @HiltAndroidApp
├── MainActivity.kt           Activity única, edge-to-edge, splash
├── MainViewModel.kt          Resuelve la sesión antes de elegir destino
├── core/
│   ├── di/                   NetworkModule, SupabaseModule, RepositoryModule
│   ├── network/              TmdbInterceptor, safeApiCall, NetworkError
│   ├── ui/theme/             Color, Type, Theme (M3, claro y oscuro)
│   ├── ui/components/        ErrorView, LoadingView, EmptyView, RatingBadge, BrandMark
│   └── util/                 UiText, formateadores, composición de URLs
├── feature/auth/
│   ├── data/                 SupabaseAuthRepository, GoogleIdTokenProvider
│   ├── domain/               AuthRepository, User, SessionState, AuthError
│   └── presentation/         LoginScreen, LoginViewModel, LoginUiState
├── feature/movies/
│   ├── data/                 TmdbApi, DTOs, mappers, PagingSource, repositorio
│   ├── domain/               Movie, MovieDetail, Genre, MovieRepository, UseCases
│   └── presentation/
│       ├── list/             MovieListScreen, MovieCard, GenreFilterRow, MovieListViewModel
│       └── detail/           MovieDetailScreen, MovieDetailViewModel
└── navigation/               AppNavHost, rutas @Serializable
```

Un solo módulo `app`, organizado por feature y por capa. La separación en
`:core:network`, `:feature:movies`, etc. aporta tiempos de compilación incremental y
límites de visibilidad reales, pero a esta escala su coste de configuración supera el
beneficio; la estructura de paquetes ya refleja dónde caería cada corte.

### Estado y eventos

Cada pantalla expone un `UiState` inmutable como `StateFlow`, recolectado con
`collectAsStateWithLifecycle()`. Lo que debe ocurrir una sola vez (navegar, mostrar un
snackbar) viaja por un `Channel` independiente, de modo que una recomposición o un cambio
de configuración no lo repiten.

---

## Stack

| Área | Elección | Motivo |
|---|---|---|
| UI | Compose + Material 3 | Sistema de color por roles, componentes adaptativos, menos superficie que XML + vistas |
| Inyección | Hilt (KSP) | Grafo verificado en compilación; KSP evita el coste de kapt |
| Red | Retrofit + OkHttp + kotlinx.serialization | Serialización sin reflexión y un único punto para cabeceras e idioma |
| Concurrencia | Coroutines + Flow / StateFlow | Cancelación estructurada, ligada al ciclo de vida |
| Paginación | Paging 3 | Estados de carga, error y reintento por página ya resueltos |
| Imágenes | Coil 3 | API nativa de Compose, caché en memoria y disco |
| Navegación | Navigation Compose type-safe | Argumentos tipados y verificados, no cadenas |
| Autenticación | Supabase Auth + Credential Manager | `sessionStatus` ya es un `Flow`; `GoogleSignInClient` está obsoleto |
| Arranque | Core SplashScreen API | Sostiene el splash mientras se resuelve la sesión |
| Pruebas | JUnit, MockK, Turbine, coroutines-test, MockWebServer, paging-testing | Cubren ViewModels, repositorio y `PagingSource` |
| Estilo | ktlint + detekt | Formato y complejidad verificables en CI |
| Build | Version catalog (`libs.versions.toml`) | Versiones centralizadas y compartibles entre módulos |

---

## Decisiones técnicas

### Clasificación por edad

TMDB no expone la clasificación en `/movie/{id}`: reside en el recurso
`/movie/{id}/release_dates`, segmentada por país y con numerosas entradas vacías. Pedirla
como recurso aparte duplicaría la latencia de la pantalla de detalle, así que se solicita
en la misma llamada con `append_to_response=release_dates`.

La resolución recorre los países por preferencia (MX → US) y, dentro de cada uno, toma la
primera certificación no vacía; si ninguno aporta valor devuelve `null` y la interfaz
muestra "N/D". Los casos límite (país ausente, cadena vacía, bloque no solicitado,
diferencias de mayúsculas) están cubiertos en `CertificationMapperTest`.

### Modelo de error

Ninguna excepción cruda alcanza la interfaz. `safeApiCall` traduce lo que lanza Retrofit
a un tipo cerrado de dominio (`NoConnection`, `Unauthorized`, `NotFound`, `Server`,
`Unknown`) y el repositorio devuelve `Result<T>`. La capa de presentación convierte ese
tipo en un `UiText` localizado. `CancellationException` se relanza explícitamente para no
romper la cancelación estructurada de las corrutinas.

### Textos desacoplados del framework

`UiText` representa un texto que puede provenir de `strings.xml` o del servidor y se
resuelve contra un `Context` solo en el momento de pintarlo. Los ViewModels quedan libres
de dependencias de Android y se prueban con JUnit puro, sin Robolectric. Por la misma
razón el correo se valida con `Regex` en lugar de `android.util.Patterns`, que en la JVM
de pruebas devuelve `null`.

### Sesión y arranque

`MainViewModel` observa el estado de sesión de Supabase y el splash permanece visible
(`setKeepOnScreenCondition`) hasta que se resuelve. Así el primer destino compuesto ya es
el correcto y no aparece un parpadeo de la pantalla de acceso cuando existe sesión
guardada. Un fallo al refrescar el token se trata como sesión expirada.

### Supabase en lugar de Firebase

El enunciado sugería Firebase y admitía incluso un usuario y contraseña estáticos. Se optó
por Supabase Auth por tres motivos: evita acoplar la aplicación a los Servicios de Google
Play para algo que es puro HTTP, mantiene el proyecto en un único proveedor si más
adelante hiciera falta persistencia, y no obliga a versionar un `google-services.json`.

La decisión no filtra al resto del código: `AuthRepository` no menciona a Supabase por
ninguna parte, de modo que cambiar de proveedor es escribir otra implementación de esa
interfaz y cambiar la línea correspondiente del módulo de Hilt.

### Acceso con Google

Se usa Credential Manager, no el `GoogleSignInClient` obsoleto. El nonce viaja en dos
formas de manera deliberada: a Google se le entrega el **SHA-256** del valor, que queda
incrustado en el ID token, y a Supabase el valor **en claro**, que vuelve a hashear para
compararlo. Enviar el mismo valor a ambos lados es el error habitual en esta integración
y produce un `invalid nonce`.

### Filtro por género

`now_playing` no admite filtro de género, así que al elegir uno la paginación pasa a
`/discover/movie` con la ventana de cartelera: estreno en cine
(`with_release_type=2|3`) dentro de los últimos 45 días y orden por popularidad.
Filtrar en el cliente habría dejado sin resultados a los géneros ausentes de la
primera página.

El catálogo viene de `/genre/movie/list` y el repositorio lo guarda en memoria. Si esa
llamada falla, la fila de filtros no se dibuja.

### Paginación

`NowPlayingPagingSource` traduce el esquema de páginas de TMDB (`page` / `total_pages`) a
claves de Paging, devolviendo `nextKey = null` en la última página en lugar de solicitar
una vacía, tanto si la página viene de `now_playing` como de `discover`. `getRefreshKey`
se ancla a la posición visible para reanudar donde estaba el usuario. El género
seleccionado entra por `flatMapLatest`, de modo que cambiarlo descarta el `Pager`
anterior, y el resultado se cachea con `cachedIn(viewModelScope)` para que un cambio de
configuración no reinicie la carga desde la primera página.

### Imágenes y localización

Las rutas de TMDB son relativas; las URL se componen eligiendo el ancho por destino
(`w342` en listado, `w500` y `w780` en detalle) para no descargar píxeles que no se van a
mostrar. Las peticiones se hacen en `es-MX` con región `MX`; las fechas se formatean con
`java.time` y `FormatStyle.MEDIUM`, y las sinopsis vacías (frecuentes en localizaciones
no inglesas) caen a un texto de respaldo.

### Gestión de secretos

Ningún valor sensible se versiona. `app/build.gradle.kts` resuelve los secretos, por
orden, desde `secrets.properties` (ignorado por git), variables de entorno del CI y
`local.defaults.properties` (marcadores versionados, para que el proyecto compile en
limpio), y los expone a través de `BuildConfig`.

La clave `anon` de Supabase es pública por diseño: la protección real la dan las
políticas RLS del proyecto. El token de TMDB, en cambio, es extraíble de cualquier APK;
en un entorno productivo viviría tras un proxy propio y la aplicación nunca lo
transportaría.

---

## Puesta en marcha

### Requisitos

- JDK 17
- Android SDK 35

### Configuración

1. Copiar `secrets.properties.example` a `secrets.properties` y completar:

   ```properties
   TMDB_READ_TOKEN=...
   SUPABASE_URL=https://xxxxxxxxxxxx.supabase.co
   SUPABASE_ANON_KEY=...
   GOOGLE_WEB_CLIENT_ID=....apps.googleusercontent.com
   ```

2. Preparar el proyecto de Supabase: proveedores, usuario y cliente OAuth de Google.

3. Compilar:

   ```bash
   ./gradlew assembleDebug
   ```

> El repositorio no versiona el `gradle-wrapper.jar`. El pipeline de CI genera el wrapper
> automáticamente; en local se obtiene abriendo el proyecto en Android Studio o con
> `gradle wrapper --gradle-version 8.11.1`.

### Pruebas

```bash
./gradlew testDebugUnitTest
```

| Objetivo | Herramientas |
|---|---|
| Resolución de clasificación y formateadores | JUnit, casos límite |
| `LoginViewModel`, `MovieDetailViewModel` | MockK + Turbine + regla de dispatcher |
| `NowPlayingPagingSource` | `TestPager` de paging-testing |
| `MovieRepositoryImpl` | MockWebServer con respuestas reales de TMDB en `resources/` |

### Estilo

```bash
./gradlew ktlintCheck detekt
./gradlew ktlintFormat
```

ktlint y detekt están configurados en modo informativo: reportan sin interrumpir el
build. Para hacerlos bloqueantes:

```bash
./gradlew ktlintCheck detekt -PqualityAdvisory=false
```

---

## Integración continua

`codemagic.yaml` define dos flujos:

- **`debug`**: en cada push y pull request, genera el wrapper si falta, materializa los
  secretos desde el grupo de variables `cinemx_secrets`, ejecuta estilo y pruebas
  unitarias, y produce el APK de depuración.
- **`release`**: al publicar una etiqueta `v*`, lo anterior más el APK firmado con R8
  activo, publicando el `mapping.txt` junto al binario.

El keystore se inyecta desde *Code signing identities* (`cinemx_keystore`) y el flujo lo
traduce al `keystore.properties` que espera el script de build; ni el almacén ni sus
contraseñas entran en el repositorio.

`.github/workflows/ci.yml` replica la verificación en GitHub Actions.

Las reglas de R8 preservan explícitamente los serializadores de kotlinx.serialization,
las rutas de navegación y el motor Ktor que utiliza el cliente de Supabase, que son los
puntos donde la minificación suele romper la ejecución sin fallar la compilación.

---

## Trabajo futuro

- **Caché offline** con Room y `RemoteMediator`, para que la cartelera abra sin red y la
  red pase a ser un mecanismo de actualización en lugar de un requisito.
- **Modularización** por feature cuando el número de pantallas justifique el coste.
- **Pruebas de interfaz** de extremo a extremo con Compose UI Test y repositorios falsos
  inyectados por Hilt.
- **Proxy propio** para el token de TMDB, eliminando la credencial del binario.
