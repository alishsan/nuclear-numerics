(ns nuclear.potentials.core
  "Nuclear potential functions for nuclear physics calculations.
   
   Provides common potential forms used in nuclear physics:
   - Woods-Saxon potential
   - Complex Woods-Saxon (with imaginary part)
   - Derivatives of potentials
   
   These are generic potential functions that can be used with
   various numerical methods (Numerov, etc.)"
  (:require [fastmath.core :as m]))

;; ============================================================================
;; Woods-Saxon Potential
;; ============================================================================

(defn woods-saxon
  "Woods-Saxon potential.
   
   V(r) = -V0 / (1 + exp((r - R0)/a0))
   
   Parameters:
   - r: Radial distance (fm)
   - params: Vector [V0, R0, a0]
     - V0: Potential depth (MeV)
     - R0: Radius parameter (fm)
     - a0: Diffuseness parameter (fm)
   
   Returns: Potential value (MeV)
   
   Example:
   (woods-saxon 2.0 [50.0 2.0 0.6])  ; V at r=2.0 fm"
  [r [V0 R0 a0]]
  (/ (* -1.0 V0) (+ 1.0 (Math/exp (/ (- r R0) a0)))))

(defn woods-saxon-derivative
  "Calculate derivative of Woods-Saxon potential: dV/dr.
   
   For Woods-Saxon potential:
   V(r) = -V0 / (1 + exp((r - R0)/a0))
   
   The derivative is:
   dV/dr = (V0/a0) · exp((r - R0)/a0) / [1 + exp((r - R0)/a0)]²
   
   Parameters:
   - r: Radial distance (fm)
   - params: Vector [V0, R0, a0]
     - V0: Potential depth (MeV)
     - R0: Radius parameter (fm)
     - a0: Diffuseness parameter (fm)
   
   Returns: dV/dr (MeV/fm)
   
   Example:
   (woods-saxon-derivative 3.0 [50.0 2.0 0.6])  ; dV/dr at r=3 fm"
  [r [V0 R0 a0]]
  (let [exp-arg (/ (- r R0) a0)
        exp-val (Math/exp exp-arg)
        denominator (+ 1.0 exp-val)
        denominator-squared (* denominator denominator)]
    (/ (* V0 exp-val) (* a0 denominator-squared))))

;; ============================================================================
;; Complex Woods-Saxon Potential
;; ============================================================================

(defn woods-saxon-complex
  "Complex Woods-Saxon potential.
   
   V(r) = -(V0 + iW0) / (1 + exp((r - R0)/a0))
   
   Parameters:
   - r: Radial distance (fm)
   - params: Vector [V0, R0, a0] or [V0, R0, a0, W0]
     - V0: Real potential depth (MeV)
     - R0: Radius parameter (fm)
     - a0: Diffuseness parameter (fm)
     - W0: Imaginary potential depth (MeV, optional)
   
   Returns: Complex number (real and imaginary parts)
   
   Note: Uses fastmath complex numbers. Returns a map with :real and :imag keys.
   
   Example:
   (woods-saxon-complex 2.0 [50.0 2.0 0.6])           ; Real only
   (woods-saxon-complex 2.0 [50.0 2.0 0.6 10.0])     ; With imaginary part"
  [r params]
  (let [[V0 R0 a0 W0] (if (= (count params) 3)
                        (conj params 0.0)  ; Add W0=0 if not provided
                        params)
        denominator (+ 1.0 (Math/exp (/ (- r R0) a0)))
        real-part (/ (* -1.0 V0) denominator)
        imag-part (if W0
                   (/ (* -1.0 W0) denominator)
                   0.0)]
    {:real real-part :imag imag-part}))

;; ============================================================================
;; Utility Functions
;; ============================================================================

(defn potential-at-radius
  "Calculate potential value at a specific radius.
   
   Convenience function that dispatches based on potential type.
   
   Parameters:
   - r: Radial distance (fm)
   - potential-type: Keyword (:woods-saxon, :woods-saxon-complex)
   - params: Potential parameters
   
   Returns: Potential value"
  [r potential-type params]
  (case potential-type
    :woods-saxon (woods-saxon r params)
    :woods-saxon-complex (woods-saxon-complex r params)
    (throw (IllegalArgumentException. (str "Unknown potential type: " potential-type)))))

(defn potential-depth
  "Extract potential depth from parameters.
   
   Parameters:
   - params: Potential parameters [V0, R0, a0] or [V0, R0, a0, W0]
   
   Returns: V0 (MeV)"
  [params]
  (first params))

(defn potential-radius
  "Extract radius parameter from potential parameters.
   
   Parameters:
   - params: Potential parameters [V0, R0, a0] or [V0, R0, a0, W0]
   
   Returns: R0 (fm)"
  [params]
  (second params))

(defn potential-diffuseness
  "Extract diffuseness parameter from potential parameters.
   
   Parameters:
   - params: Potential parameters [V0, R0, a0] or [V0, R0, a0, W0]
   
   Returns: a0 (fm)"
  [params]
  (nth params 2))
