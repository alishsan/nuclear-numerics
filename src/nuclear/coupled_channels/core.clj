(ns nuclear.coupled-channels.core
  "Coupled channels solver using Numerov integration.
   
   Solves systems of coupled differential equations of the form:
   
   d²u_α/dr² = Σ_β f_αβ(r) * u_β(r)
   
   where α, β are channel indices.
   
   This is used for nuclear physics calculations involving multiple
   reaction channels that are coupled through interaction potentials.
   
   Key features:
   - Solves coupled systems using Numerov method
   - Supports arbitrary number of channels
   - Handles channel coupling through potential matrix
   - Extracts individual channel wavefunctions"
  (:require [nuclear.numerov.core :as numerov]
            [fastmath.core :as m]))

;; ============================================================================
;; Channel Definition
;; ============================================================================

(defn make-channel
  "Define a channel for coupled channels calculation.
   
   Parameters:
   - l: Angular momentum quantum number
   - e: Channel energy (MeV)
   - label: Optional label for identification
   
   Returns: Channel map with :l, :e, and :label keys"
  ([l e]
   {:l l :e e :label nil})
  ([l e label]
   {:l l :e e :label label}))

;; ============================================================================
;; Coupling Matrix Elements
;; ============================================================================

(defn coupling-matrix-element
  "Calculate coupling matrix element between two channels.
   
   Parameters:
   - r: Radial distance (fm)
   - channel-i: First channel map
   - channel-j: Second channel map
   - coupling-spec: Coupling specification (implementation-dependent)
   - E-incident: Incident energy (MeV)
   
   Returns: Coupling matrix element V_ij(r)
   
   Note: This is a placeholder. Actual implementation depends on
   the specific coupling mechanism (deformation, transition, etc.)"
  [r channel-i channel-j coupling-spec E-incident]
  ;; Placeholder - should be implemented based on specific physics
  0.0)

;; ============================================================================
;; Potential Matrix
;; ============================================================================

(defn coupled-channels-potential-matrix
  "Calculate potential matrix for coupled channels at radial distance r.
   
   The potential matrix V_αβ(r) has:
   - Diagonal: V_αα(r) (self-interaction for channel α)
   - Off-diagonal: V_αβ(r) (coupling between channels α and β)
   
   Parameters:
   - r: Radial distance (fm)
   - channels: Vector of channel definitions
   - coupling-specs: Vector of coupling specifications (maps with :from, :to, :strength, etc.)
   - E-incident: Incident energy (MeV)
   - v0: Potential depth (MeV)
   - rad: Radius parameter R0 (fm)
   - diff: Diffuseness parameter a0 (fm)
   
   Returns: n×n matrix where n is the number of channels"
  [r channels coupling-specs E-incident v0 rad diff]
  (let [n-channels (count channels)
        matrix (vec (repeat n-channels (vec (repeat n-channels 0.0))))]
    (reduce
     (fn [acc [i channel-i]]
       (reduce
        (fn [acc2 [j channel-j]]
          (if (= i j)
            ;; Diagonal: Woods-Saxon potential for channel i
            (let [ws-pot (numerov/woods-saxon-potential r v0 rad diff)]
              (assoc-in acc2 [i j] ws-pot))
            ;; Off-diagonal: Coupling between channels i and j
            (let [;; Find coupling spec that connects channels i and j
                  coupling (first (filter #(or (and (= (:from %) i) (= (:to %) j))
                                               (and (= (:from %) j) (= (:to %) i)))
                                          coupling-specs))
                  coupling-val (if coupling
                                (* (or (:strength coupling) 1.0)
                                   (numerov/woods-saxon-potential r v0 rad diff)
                                   (or (:beta coupling) 0.1))  ; Simplified coupling
                                0.0)]
              (assoc-in acc2 [i j] coupling-val))))
        acc
        (map-indexed vector channels)))
     matrix
     (map-indexed vector channels))))

;; ============================================================================
;; f-Matrix for Numerov
;; ============================================================================

(defn coupled-channels-f-matrix
  "Calculate f-matrix for Numerov integration in coupled channels.
   
   For the coupled system:
   d²u_α/dr² = Σ_β f_αβ(r) * u_β(r)
   
   where f_αβ(r) = (2μ/ℏ²) * (V_αβ(r) - E_α δ_αβ)
   
   For diagonal elements, this matches f-r-numerov:
   f_αα(r) = (2μ/ℏ²) * (V_αα(r) - E_α)
   
   Parameters:
   - r: Radial distance (fm)
   - channels: Vector of channel definitions
   - coupling-specs: Vector of coupling specifications
   - E-incident: Incident energy (MeV)
   - mass-factor: 2μ/(ℏc)² (MeV⁻¹·fm⁻²)
   - v0: Potential depth (MeV)
   - rad: Radius parameter R0 (fm)
   - diff: Diffuseness parameter a0 (fm)
   
   Returns: n×n f-matrix where n is the number of channels"
  [r channels coupling-specs E-incident mass-factor v0 rad diff]
  (let [V-matrix (coupled-channels-potential-matrix r channels coupling-specs E-incident v0 rad diff)
        n-channels (count channels)]
    (vec (for [i (range n-channels)]
           (vec (for [j (range n-channels)]
                  (let [channel-i (nth channels i)
                        E-i (:e channel-i)
                        V-ij (get-in V-matrix [i j])]
                    (if (= i j)
                      ;; Diagonal: use f-r-numerov formula for consistency
                      ;; This matches: f-r-numerov(r, E, l, V0, R0, a0, mass-factor)
                      (numerov/f-r-numerov r E-i (:l channel-i) v0 rad diff mass-factor)
                      ;; Off-diagonal: coupling term
                      (* mass-factor V-ij)))))))))

;; ============================================================================
;; Main Coupled Channels Solver
;; ============================================================================

(defn solve-coupled-channels-numerov
  "Solve coupled channels equations using Numerov method.
   
   Solves the system of coupled differential equations:
   d²u_α/dr² = Σ_β f_αβ(r) * u_β(r)
   
   Parameters:
   - channels: Vector of channel definitions (from make-channel)
   - coupling-specs: Vector of coupling specifications
   - E-incident: Incident energy (MeV)
   - mass-factor: 2μ/(ℏc)² (MeV⁻¹·fm⁻²)
   - v0: Potential depth (MeV)
   - rad: Radius parameter R0 (fm)
   - diff: Diffuseness parameter a0 (fm)
   - h: Step size (fm)
   - r-max: Maximum integration radius (fm)
   
   Returns: Vector of wavefunction vectors, one for each channel.
   Each wavefunction vector contains values at r = 0, h, 2h, ..., r_max
   
   Example:
   (let [ch0 (make-channel 0 10.0 :ground)
         ch1 (make-channel 2 8.0 :excited)
         channels [ch0 ch1]
         couplings [...]]
     (solve-coupled-channels-numerov channels couplings 10.0 0.0247 40.0 2.0 0.6 0.01 20.0))"
  [channels coupling-specs E-incident mass-factor v0 rad diff h r-max]
  (let [n-channels (count channels)
        steps (int (/ r-max h))
        ;; Initialize each channel independently
        ;; For now, use simple initialization (can be improved)
        initial-conditions (vec (for [channel channels]
                                  (let [q (Math/sqrt (* mass-factor (+ (:e channel) v0)))]
                                    [0.0 (numerov/bessel-start-l1 h q)])))
        
        ;; Pre-calculate f-matrices at all radial points
        f-matrices (mapv (fn [r]
                           (coupled-channels-f-matrix r channels coupling-specs 
                                                      E-incident mass-factor v0 rad diff))
                         (take (+ steps 2) (iterate #(+ % h) 0.0)))
        h2-12 (/ (* h h) 12.0)]
    
    ;; Numerov integration for coupled system
    (loop [n 1
           results initial-conditions]
      (if (>= n (dec steps))
        results
        (let [;; Current wavefunction values: u_n[α] for each channel α
              u-n (vec (for [i (range n-channels)]
                         (get-in results [i n])))
              ;; Previous wavefunction values: u_{n-1}[α]
              u-n-1 (vec (for [i (range n-channels)]
                           (get-in results [i (dec n)])))
              ;; f-matrix at current and neighboring points
              f-n-1 (get f-matrices (dec n))
              f-n (get f-matrices n)
              f-n+1 (get f-matrices (inc n))
              
              ;; Numerov step for coupled system
              ;; For each channel α:
              ;; u_{n+1}[α] = [2u_n[α] - u_{n-1}[α] + h²/12 Σ_β (10f_αβ u_n[β] + f_αβ u_{n-1}[β])] / (1 - h²/12 f_{αα})
              u-n+1 (vec (for [alpha (range n-channels)]
                          (let [;; Sum over coupled channels
                                sum-term (reduce + (for [beta (range n-channels)]
                                                    (+ (* 10.0 (get-in f-n [alpha beta]) (get u-n beta))
                                                       (* (get-in f-n-1 [alpha beta]) (get u-n-1 beta)))))
                                numerator (+ (* 2.0 (get u-n alpha))
                                            (- (get u-n-1 alpha))
                                            (* h2-12 sum-term))
                                denominator (- 1.0 (* h2-12 (get-in f-n+1 [alpha alpha])))
                                u-alpha-n+1 (/ numerator denominator)]
                            u-alpha-n+1)))]
          (recur (inc n) 
                 (vec (for [i (range n-channels)]
                        (conj (get results i) (get u-n+1 i))))))))))

;; ============================================================================
;; Channel Extraction
;; ============================================================================

(defn extract-channel-wavefunction
  "Extract wavefunction for a specific channel from coupled channels solution.
   
   Parameters:
   - coupled-solution: Result from solve-coupled-channels-numerov
   - channel-id: Channel index (0-based)
   
   Returns: Wavefunction vector for the specified channel
   
   Example:
   (let [solution (solve-coupled-channels-numerov ...)
         u-ground (extract-channel-wavefunction solution 0)]
     u-ground)"
  [coupled-solution channel-id]
  (nth coupled-solution channel-id))
