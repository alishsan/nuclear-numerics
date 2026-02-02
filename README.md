# nuclear-numerics

A Clojure library providing numerical methods for nuclear physics calculations, extracted from the dwba project for shared use across multiple projects.

## Overview

This library provides core numerical methods used in nuclear physics calculations:

- **Numerov Integration**: High-order finite difference method for solving second-order differential equations
- **Coupled Channels Solver**: Numerov-based solver for systems of coupled differential equations
- **Nuclear Potentials**: Common nuclear potential functions (Woods-Saxon, etc.)

## Installation

### From Source

```bash
git clone <repository-url>
cd nuclear-numerics
lein install
```

### As Dependency

Add to your `project.clj`:

```clojure
:dependencies [[org.clojars.alishsan/nuclear-numerics "0.1.0-SNAPSHOT"]]
```

The library is available on Clojars: https://clojars.org/org.clojars.alishsan/nuclear-numerics

For local development, use:

```clojure
:dependencies [[org.clojars.alishsan/nuclear-numerics "0.1.0-SNAPSHOT" :local/root "../nuclear-numerics"]]
```

## Usage

### Numerov Integration

```clojure
(require '[nuclear.numerov.core :as numerov])

;; Calculate mass factor
(def mass-factor (numerov/mass-factor-from-mu 469.46)) ; p+n reduced mass

;; Solve radial Schrödinger equation
(def solution (numerov/solve-numerov 
               10.0    ; Energy (MeV)
               0       ; Angular momentum l
               40.0    ; Potential depth V0 (MeV)
               2.0     ; Radius R0 (fm)
               0.6     ; Diffuseness a0 (fm)
               mass-factor
               0.01    ; Step size h (fm)
               20.0))  ; Maximum radius (fm)

;; Check Wronskian conservation
(def wronskian (numerov/check-wronskian solution 10.0 0 40.0 2.0 0.6 mass-factor 0.01))
```

### Coupled Channels

```clojure
(require '[nuclear.coupled-channels.core :as cc])

;; Define channels
(def ch0 (cc/make-channel 0 10.0 :ground))  ; l=0, E=10.0 MeV
(def ch1 (cc/make-channel 2 8.0 :excited)) ; l=2, E=8.0 MeV

;; Define coupling
(def couplings [{:from 0 :to 1 :beta 0.25 :strength 1.0}])

;; Solve coupled system
(def solution (cc/solve-coupled-channels-numerov
               [ch0 ch1]
               couplings
               10.0      ; Incident energy
               mass-factor
               40.0      ; V0
               2.0       ; R0
               0.6       ; a0
               0.01      ; h
               20.0))    ; r-max

;; Extract channel wavefunctions
(def u-ground (cc/extract-channel-wavefunction solution 0))
(def u-excited (cc/extract-channel-wavefunction solution 1))
```

## API Reference

### `nuclear.numerov.core`

- `solve-numerov` - Main Numerov solver
- `f-r-numerov` - Effective potential function
- `woods-saxon-potential` - Woods-Saxon potential
- `bessel-start-l1` - Bessel function initialization
- `check-wronskian` - Wronskian conservation check
- `numerov-convergence-test` - Convergence testing
- `mass-factor-from-mu` - Calculate mass factor from reduced mass

### `nuclear.coupled-channels.core`

- `make-channel` - Create channel definition
- `solve-coupled-channels-numerov` - Main coupled channels solver
- `coupled-channels-potential-matrix` - Calculate potential matrix
- `coupled-channels-f-matrix` - Calculate f-matrix for Numerov
- `extract-channel-wavefunction` - Extract individual channel solution

## Dependencies

- **Clojure 1.12.0+**
- **fastmath 3.0.0-alpha4-SNAPSHOT** - High-performance mathematical operations

## Development

### Running Tests

```bash
lein test
```

### Building

```bash
lein jar
lein install  # Install to local Maven repository
```

## License

EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0

## Related Projects

- **dwba**: Distorted Wave Born Approximation calculations (uses this library)
- **esym**: Neutron star symmetry energy calculations (uses this library)
