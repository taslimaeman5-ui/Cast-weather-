# SkyCast Realistic & Smooth World Weather Map

A realistic, ultra-smooth 3D Earth globe and Google Maps–style 2D world exploration experience for SkyCast Weather, featuring vibrant emerald green continents, multi-depth sapphire blue oceans, realistic coastal shelf gradients, atmospheric fresnel glow, and fluid momentum-based physics.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following visual styling and interaction behaviors were confirmed during the clarification interview:

- **Confirmed Decision 1: Realistic Earth Color Palette**: Continents are rendered with rich emerald and moss green landmasses (`#2E7D32` to `#1B5E20`), while oceans transition from shallow turquoise coastal shelf waters (`#0284C7`) to deep sapphire ocean basins (`#0F2042` and `#0A1128`).
- **Confirmed Decision 2: Photorealistic Visual Enhancements**: Added 3D spherical radial lighting, illuminated continental relief shading, soft atmospheric rim glow (Fresnel blue halo), and graticule grid lines that react dynamically to globe rotation.
- **Confirmed Decision 3: Physics-Based Smooth Inertial Motion**: Touch interactions use fling/drag velocity momentum with exponential damping decay for fluid globe spinning and flat map panning, accompanied by spring-based zoom transitions and smooth radar marker pulse animations.

---

### 1. Overview & Core Concept

- **What It Does**: Upgrades the interactive world weather map in SkyCast with photorealistic Earth visual styling and silky 60fps gesture handling. Users can spin the 3D globe with momentum, toggle seamlessly between 3D sphere and 2D Mercator flat map projections, observe realistic green terrain and deep blue oceans, and tap any major world city to view synchronized live weather telemetry.
- **Target Audience / Persona**: Travelers, meteorology enthusiasts, and daily weather users seeking an engaging, intuitive planetary view of weather patterns and forecasts across continents.
- **Key Value**: Delivers Google Earth–level visual delight and tactile navigation directly inside a lightweight, battery-efficient Jetpack Compose application without heavy external SDK overhead or excessive APK bloat.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Interactive Exploration Flow**: User drags across the canvas to spin the 3D Earth globe. Releasing a swipe causes the globe to continue spinning smoothly with realistic rotational inertia before coming to a gentle rest.
2. **Projection Mode Transition**: Tapping the "3D Globe / 2D Map" pill button animates the camera between a spherical Earth in star-filled space and a full-bleed flat Mercator projection with smooth interpolation.
3. **Location Search & Fly-To**: Searching for any city or selecting quick category chips (Capitals, Warmest Spots, Rain Radar) initiates a smooth spherical camera flight that rotates and centers the target city under the camera with a pulsing radar marker.
4. **Weather Layer Switching**: Selecting different weather layers (Temperature, Precipitation, Wind, Satellite/Terrain) smoothly shifts color grading, highlighting rainfall zones and thermal signatures over the realistic green land and blue ocean backdrop.

#### Visual Identity & Theme
- **Aesthetic Direction**: Cinematic Earth Cartography with modern Material 3 overlays. Dark space background with subtle starry particle depth, contrasting against glowing planetary hues.
- **Color Palette & Atmosphere**:
  - `Ocean Abyss (Deep)`: `#070E1E` – Deep ocean trenches and night-side limb.
  - `Ocean Mid-Basin`: `#0F274A` to `#1E3A8A` – Vibrant maritime sapphire blue.
  - `Coastal Shelf & Shallows`: `#0284C7` / `#38BDF8` with 40% blend – Subtle luminous shorelines.
  - `Continental Landmass`: `#1F5E3B` base with `#2E7D32` illuminated highlights – Lush, natural vegetation green.
  - `Atmosphere Rim / Fresnel Glow`: Radial gradient of `#38BDF8` (80% to 0% alpha) hugging the planetary silhouette.
  - `Weather Overlay Accents`: Thermal amber (`#F59E0B`), precipitation azure (`#06B6D4`), and radar pulse ring (`#60A5FA`).
- **Typography & Hierarchy**: High-contrast, clean sans-serif typography using standard scalable `sp` units. Heavy bold weights for city names and temperatures, coupled with muted secondary details for coordinates and humidity.
- **Component Styling & Layout**: Floating frosted-glass pill controls, rounded floating action buttons with 48dp minimum touch targets, and a bottom sheet card displaying city telemetry with clean Material 3 tonal elevation.

#### Interactive Feedback & Motion
- **Inertial Momentum**: Touch drag gestures capture velocity vectors; upon touch release, an internal physics animator decelerates rotation smoothly using cubic exponential decay.
- **Pinch-to-Zoom Dynamics**: Dual-finger scaling with clamped min/max bounds and smooth spring interpolation.
- **Marker Pulse Animation**: Infinite transition pulsing rings around selected city pins to indicate active real-time radar observation.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Vector Canvas Projection vs. Raster Tile Map SDK**
  - *Chosen Approach*: Enhanced custom Jetpack Compose `Canvas` with spherical 3D rotation projection matrix, multi-layer continent polygon geometry, and radial lighting shaders.
  - *Why*: Eliminates hundreds of megabytes of third-party SDK dependencies, avoids external API key billing/rate limits, keeps the app size tiny, and ensures instant, offline-capable rendering with zero network latency.
  - *Alternatives Considered*: Google Maps SDK or Mapbox SDK. Rejected to protect the lightweight APK size constraint and eliminate third-party API dependencies.

- **Decision 2: Velocity Deceleration Coroutine for Inertia**
  - *Chosen Approach*: Coroutine-driven momentum accumulator using Compose `Animatable` or manual frame-delta inertia simulation with boundary clamping.
  - *Why*: Delivers high-end tactile feel identical to native mobile globe navigation without jitter or stutter on low-end devices.

---

### 4. Technical Architecture & Data Strategy *(Technical Reference)*

#### System Architecture & Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    SkyCast World Weather Screen                 │
└─────────────────────────────────────────────────────────────────┘
                               │
            ┌──────────────────┴──────────────────┐
            ▼                                     ▼
┌───────────────────────────────┐     ┌───────────────────────────┐
│     Touch Gesture Pipeline    │     │    Floating Controls      │
│  - Drag detection + Velocity  │     │  - 2D/3D Projection Mode  │
│  - Inertial Deceleration Spec │     │  - Quick Category Chips   │
│  - Pinch Transformable State  │     │  - Layer Selector (Temp,  │
│  - Tap Hit-Test Target Finder │     │    Rain, Wind, Satellite) │
└───────────────────────────────┘     └───────────────────────────┘
                │                                   │
                ▼                                   ▼
┌─────────────────────────────────────────────────────────────────┐
│               Jetpack Compose Canvas Render Pipeline            │
│  1. Space Backdrop & Starfield Gradient                         │
│  2. Oceanic Base Sphere with 3D Radial Sun Highlight (Blues)    │
│  3. Spherical Graticule Grid Lines (Equator, Parallels, Merid.) │
│  4. Realistic Continent Polygons (Emerald/Forest Greens)        │
│  5. Coastline Shading & Shoreline Halo Path                     │
│  6. Atmosphere Rim Lighting (Fresnel Specular Halo)             │
│  7. City Marker Overlay with Pulsing Radar Telemetry Pins       │
└─────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│              Google Maps Style City Detail Bottom Sheet         │
│  - City Name, Country, Continent, Condition Icon                │
│  - Temperature, Humidity, Wind Speed, UV Index, Pressure        │
│  - Quick Action: Set as Home City / View Detailed Forecast      │
└─────────────────────────────────────────────────────────────────┘
```

#### Interactive Component & State Mapping

| Component / Layer | State Variable | Interaction & Transition Behavior |
| :--- | :--- | :--- |
| **Globe 3D Canvas** | `globeRotationX`, `globeRotationY`, `velocityTracker` | Smooth horizontal & vertical rotation with inertia decay on drag release. Clamped vertical tilt (-80° to 80°). |
| **Flat Map Canvas** | `flatPanX`, `flatPanY`, `mapZoom` | Bidirectional 2D Mercator pan with continuous boundary wrapping and zoom clamping (0.6x to 3.8x). |
| **Projection Switch** | `projectionMode` (`GLOBE_3D` vs `FLAT_MAP`) | Instant state switch with camera coordinate translation, updating controls and graticule representation. |
| **City Selection** | `selectedCity`, `showCityBottomSheet` | Tap hit-testing on spherical coordinate projection opens the animated bottom telemetry sheet with spring reveal. |
| **Weather Layer** | `activeWeatherLayer` | Dropdown selection dynamically updates continent shaders, precipitation heatmaps, and wind velocity indicators. |
| **Recenter Button** | `mapResetCenterButton` | Smooth coroutine animation to standard front-facing coordinates (`rotX = 20f`, `rotY = 100f`, `zoom = 1.0f`). |
