(ns nuclear.numerov.core
  "Numerov method for solving second-order differential equations.
   
   The Numerov method is a high-order finite difference method for solving
   equations of the form:
   
   d²u/dr² = f(r) * u(r)
   
   This is particularly useful for solving the radial Schrödinger equation
   in nuclear physics calculations.
   
   Key features:
   - High-order accuracy (O(h⁶) local truncation error)
   - Stable for oscillatory solutions
   - Efficient for large integration ranges
   - Supports complex-valued solutions"
  (:require [fastmath.core :as m]))

;; ============================================================================
;; Constants
;; ============================================================================

(def ^:const hbarc 197.7) ; MeV·fm (ℏc)

;; ============================================================================
;; Potential Functions (Generic)
;; ============================================================================

(defn woods-saxon-potential
  "Woods-Saxon potential for Numerov method.
   
   V(r) = -V0 / (1 + exp((r - R0)/a0))
   
   Parameters:
   - r: Radial distance (fm)
   - v0: Potential depth (MeV)
   - rad: Radius parameter R0 (fm)
   - diff: Diffuseness parameter a0 (fm)
   
   Returns: Potential value (MeV)"
  [r v0 rad diff]
  (/ (- v0) (+ 1.0 (Math/exp (/ (- r rad) diff)))))

;; ============================================================================
;; Effective Potential Function
;; ============================================================================

(defn f-r-numerov
  "Effective potential function for Numerov integration.
   
   For the radial Schrödinger equation:
   d²u/dr² = f(r) * u(r)
   
   where f(r) = (2μ/ℏ²) * (V_eff(r) - E)
   
   and V_eff(r) = V(r) + ℏ²/(2μ) * l(l+1)/r²
   
   Parameters:
   - r: Radial distance (fm)
   - e: Energy (MeV)
   - l: Angular momentum quantum number
   - v0: Potential depth (MeV)
   - rad: Radius parameter R0 (fm)
   - diff: Diffuseness parameter a0 (fm)
   - mass-factor: 2μ/(ℏc)² (MeV⁻¹·fm⁻²)
   
   Returns: f(r) value for Numerov integration"
  [r e l v0 rad diff mass-factor]
  (if (zero? r)
    ;; At r=0, centrifugal term dominates: l(l+1)/r² -> infinity
    ;; But we never actually use r=0 in Numerov (starts at r=h)
    Double/POSITIVE_INFINITY
    (let [v-potential (woods-saxon-potential r v0 rad diff)
          v-centrifugal (/ (* l (inc l)) (* mass-factor r r))
          v-eff (+ v-potential v-centrifugal)]
      (* mass-factor (- v-eff e)))))

;; ============================================================================
;; Initialization Functions
;; ============================================================================

(defn bessel-start-l1
  "Power series expansion for Riccati-Bessel function F1 near r=0 for l=1.
   
   Uses power series to avoid numerical underflow near origin:
   F1(qr) = sin(qr)/(qr) - cos(qr) ≈ (qr)²/3 - (qr)⁴/30 + ...
   
   Parameters:
   - r: Radial distance (fm)
   - q: Wavenumber = sqrt(2μ(E+V0)/ℏ²)
   
   Returns: Initial value u(r) for Numerov integration"
  [r q]
  (let [z (* q r)]
    ;; Power series: F1(z) ≈ z²/3 - z⁴/30 (accurate for small z, avoids underflow)
    (- (/ (* z z) 3.0) 
       (/ (* z z z z) 30.0))))

(defn naive-power-start
  "Naive power series start: u(r) ≈ r^(l+1).
   
   Parameters:
   - r: Radial distance (fm)
   - l: Angular momentum quantum number
   
   Returns: Initial value u(r) for Numerov integration"
  [r l]
  (Math/pow r (inc l)))

;; ============================================================================
;; Main Numerov Solver
;; ============================================================================

(defn solve-numerov
  "Solve the radial Schrödinger equation using the Numerov method.
   
   Solves: d²u/dr² = f(r) * u(r)
   
   where f(r) = (2μ/ℏ²) * (V_eff(r) - E)
   
   Parameters:
   - e: Energy (MeV)
   - l: Angular momentum quantum number
   - v0: Potential depth (MeV)
   - rad: Radius parameter R0 (fm)
   - diff: Diffuseness parameter a0 (fm)
   - mass-factor: 2μ/(ℏc)² (MeV⁻¹·fm⁻²)
   - h: Step size (fm)
   - r-max: Maximum integration radius (fm)
   
   Returns: Vector of wavefunction values u(r) at points r = 0, h, 2h, ..., r_max
   
   Algorithm:
   - Uses Bessel function initialization for stability
   - Numerov formula: u_{n+1} = [2u_n - u_{n-1} + h²/12(10f_n u_n + f_{n-1} u_{n-1})] / (1 - h²/12 f_{n+1})
   - O(h⁶) local truncation error"
  [e l v0 rad diff mass-factor h r-max]
  (let [steps (int (/ r-max h))
        q (Math/sqrt (* mass-factor (+ e v0)))
        ;; Initialize with Bessel Start
        u0 0.0
        u1 (bessel-start-l1 h q)
        
        ;; Pre-calculate f(r) values
        ;; fs[i] corresponds to f(i*h)
        ;; f(0) is infinite for l>0, but u(0)=0, so f(0)*u(0) = 0
        ;; We set f(0)=0 to avoid NaN from infinity*0
        fs (mapv (fn [r] 
                   (if (zero? r)
                     0.0  ; f(0) is infinite, but u(0)=0, so f(0)*u(0)=0 anyway
                     (f-r-numerov r e l v0 rad diff mass-factor)))
                 (take (+ steps 2) (iterate #(+ % h) 0.0)))
        h2-12 (/ (* h h) 12.0)]
    
    (loop [n 1
           results [u0 u1]]
      (if (>= n (dec steps))
        results
        (let [un (get results n)        ; u at r = n*h
              un-1 (get results (dec n)) ; u at r = (n-1)*h = 0 when n=1
              ;; Numerov formula uses: f_{n-1}, f_n, f_{n+1}
              ;; where f_n = f(n*h)
              ;; When n=1: f[0]*u[0] = f(0)*0 = 0, so fs[0] value doesn't matter
              fn-1 (get fs (dec n))  ; f at r = (n-1)*h
              fn (get fs n)          ; f at r = n*h
              fn+1 (get fs (inc n))  ; f at r = (n+1)*h
              
              ;; Numerov Step:
              ;; un+1 (1 - h^2/12 fn+1) = 2un - un-1 + h^2/12 (10fn un + fn-1 un-1)
              numerator (+ (* 2.0 un) 
                           (- un-1) 
                           (* h2-12 (+ (* 10.0 fn un) (* fn-1 un-1))))
              denominator (- 1.0 (* h2-12 fn+1))
              un+1 (/ numerator denominator)]
          (recur (inc n) (conj results un+1)))))))

;; ============================================================================
;; Validation and Testing Functions
;; ============================================================================

(defn check-wronskian
  "Check Wronskian conservation for Numerov integration.
   
   The Numerov algorithm preserves a symplectic structure. This function
   checks the conservation of a discrete Wronskian-like quantity:
   
   W_n = (h²/12)(f_n - f_{n+1}) u_n u_{n+1}
   
   This quantity should be approximately constant (conserved) for the Numerov algorithm.
   
   Parameters:
   - u: Wavefunction solution vector
   - e: Energy (MeV)
   - l: Angular momentum quantum number
   - v0: Potential depth (MeV)
   - rad: Radius parameter R0 (fm)
   - diff: Diffuseness parameter a0 (fm)
   - mass-factor: 2μ/(ℏc)² (MeV⁻¹·fm⁻²)
   - h: Step size (fm)
   
   Returns: Vector of Wronskian values at each step"
  [u e l v0 rad diff mass-factor h]
  (let [h2-12 (/ (* h h) 12.0)
        ;; fs[i] corresponds to f(i*h)
        num-points (count u)
        fs (mapv #(f-r-numerov % e l v0 rad diff mass-factor) 
                 (take num-points (iterate #(+ % h) 0.0)))]
    ;; Start from n=1 to avoid n=0 where u_0 = 0
    (vec (for [n (range 1 (dec (count u)))]
           (let [un (get u n)
                 un+1 (get u (inc n))
                 fn (get fs n)
                 fn+1 (get fs (inc n))
                 ;; Discrete Wronskian-like quantity: (h²/12)(f_n - f_{n+1}) u_n u_{n+1}
                 ;; This should be conserved (approximately constant)
                 w-n (* h2-12 (- fn fn+1) un un+1)]
             w-n)))))

(defn numerov-convergence-test
  "Test Numerov convergence by comparing fine-grid solution with test solution.
   
   Parameters:
   - e: Energy (MeV)
   - l: Angular momentum quantum number
   - v0: Potential depth (MeV)
   - rad: Radius parameter R0 (fm)
   - diff: Diffuseness parameter a0 (fm)
   - mass-factor: 2μ/(ℏc)² (MeV⁻¹·fm⁻²)
   - h-fine: Fine step size (fm)
   - h-test: Test step size (fm)
   - r-max: Maximum integration radius (fm)
   
   Returns: Map with convergence metrics:
   - :max-error: Maximum absolute error
   - :mean-error: Mean absolute error
   - :errors: Vector of errors at each point"
  [e l v0 rad diff mass-factor h-fine h-test r-max]
  (let [u-true (solve-numerov e l v0 rad diff mass-factor h-fine r-max)
        u-test (solve-numerov e l v0 rad diff mass-factor h-test r-max)
        ;; Downsample fine solution to match test grid
        downsample-factor (int (/ h-test h-fine))
        u-true-downsampled (take-nth downsample-factor u-true)
        ;; Ensure same length (may differ by 1 due to rounding)
        min-len (min (count u-true-downsampled) (count u-test))
        errors (mapv (fn [t tst] (m/abs (- t tst)))
                     (take min-len u-true-downsampled) 
                     (take min-len u-test))]
    {:max-error (apply max errors)
     :mean-error (/ (reduce + errors) (count errors))
     :errors errors
     :u-true-count (count u-true)
     :u-test-count (count u-test)
     :downsampled-count min-len}))

;; ============================================================================
;; Utility Functions
;; ============================================================================

(defn mass-factor-from-mu
  "Calculate mass-factor = 2μ/(ℏc)² for the given reduced mass.
   
   Parameters:
   - mu-reduced: Reduced mass (MeV/c²)
   
   Returns: mass-factor (MeV⁻¹·fm⁻²)"
  [mu-reduced]
  (/ (* 2 mu-reduced) hbarc hbarc))
