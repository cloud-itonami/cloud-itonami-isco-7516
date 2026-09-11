(ns tobaccocoord.governor
  "TobaccoCoordGovernor — the independent regulatory-compliance/
  safety/scope layer gating every factory-floor scheduling/logistics
  proposal an advisor may make for a tobacco preparation and
  tobacco-products manufacturing crew. The governor never dispatches
  hardware itself, never performs tobacco-preparation or
  tobacco-product-making work itself, and never finalizes a
  manufacturing-execution decision (e.g. deciding to proceed with a
  specific production run) or a regulatory-compliance-clearance
  decision (e.g. affixing a tax stamp or finalizing a
  labeling-compliance determination), nor overrides a shop safety
  officer's judgment — those are permanently out of this actor's
  scope and remain the shop safety officer's / regulatory-compliance
  function's exclusive judgment (README's 'Robotics premise': this
  actor coordinates FACTORY-FLOOR SCHEDULING/LOGISTICS ONLY — it never
  performs tobacco-preparation or tobacco-product-making work or
  clears regulatory-compliance decisions itself). Modeled closely on
  cloud-itonami-isco-7211's foundrycoord.governor for the
  regulated-manufacturing-clearance-plus-workshop-safety domain shape.

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. preparer provenance   — the crew member must be independently
                                verified/registered before any action.
    2. facility provenance   — the manufacturing facility must be
                                independently verified/registered
                                before any action.
    3. no-actuation           — proposal :effect must be :propose (the
                                governor never dispatches hardware and
                                never performs manufacturing work
                                itself; it only gates what the advisor
                                may coordinate).
    4. closed op-allowlist    — only :log-work-record,
                                :schedule-crew-operation,
                                :flag-compliance-concern and
                                :coordinate-supply-order may ever be
                                proposed; anything else is refused.
    5. scope-excluded action  — any proposal to directly finalize a
                                manufacturing-execution decision (e.g.
                                deciding to proceed with a specific
                                production run), or a regulatory-
                                compliance-clearance decision (e.g.
                                affixing a tax stamp or finalizing a
                                labeling-compliance determination), or
                                to override a shop safety officer's
                                judgment, is a hard, permanent block
                                (checked both against the proposed :op
                                and, defense-in-depth, against the
                                proposal's :rationale text — matched as
                                full finalization/execution ACTION
                                phrases such as \"proceed with the
                                production run\" / \"affix the tax
                                stamp\" / \"finalize the labeling
                                compliance determination\" / \"override
                                the shop safety officer's judgment\",
                                never as bare nouns like \"tobacco\",
                                \"tax stamp\" or \"labeling\", so the
                                check can never self-trip on the
                                advisor's own routine rationale text,
                                e.g. \"logged work record for preparer
                                …\" or \"scheduled crew operation for
                                tobacco-preparation task …\" or
                                \"…routed for shop safety officer
                                review\" — all three legitimately
                                contain bare nouns like \"tobacco\" or
                                \"safety officer\" but none is a
                                finalization action, and all are
                                exercised by
                                `governor-test/default-mock-advisor-proposals-never-self-trip-on-scope-exclusion`).
  ESCALATION invariants (:escalate? true, ALWAYS human sign-off
  regardless of confidence):
    6. :op :flag-compliance-concern (a regulatory-compliance /
                                equipment-hazard / dust-exposure
                                concern always escalates to a human,
                                never auto-commits).
    7. :op :coordinate-supply-order above `supply-cost-threshold`.
    8. low confidence (< `confidence-floor`)."
  (:require [kotoba.lang.text :as str]
            [tobaccocoord.store :as store]))

(def confidence-floor 0.6)
(def supply-cost-threshold 2000)

(def allowed-ops
  #{:log-work-record :schedule-crew-operation
    :flag-compliance-concern :coordinate-supply-order})

;; Defense-in-depth: none of these ops are ever in `allowed-ops`
;; above, so they are already refused by the closed-allowlist check
;; below; they are named again here — as explicit finalization/
;; execution ACTIONS, never bare nouns — so a future allowlist edit
;; cannot silently re-open this specific out-of-scope path without
;; also touching this list.
(def ^:private scope-excluded-ops
  #{:finalize-manufacturing-decision :authorize-production-run
    :proceed-with-production-run :finalize-production-decision
    :affix-tax-stamp :finalize-tax-stamp-compliance-decision
    :finalize-labeling-compliance-determination
    :override-shop-safety-officer-judgment
    :override-safety-officer-judgment})

;; Full finalization/execution ACTION phrases only — never bare nouns
;; ("tobacco", "tax stamp", "labeling", "safety", "factory", "officer")
;; — so this can never match inside the mock advisor's own default
;; rationale text (which legitimately contains those bare nouns, e.g.
;; "tobacco-preparation task" / "shop safety officer review"). See
;; `governor-test/default-mock-advisor-proposals-never-self-trip-on-scope-exclusion`.
(def ^:private scope-excluded-phrases
  ["proceed with the production run" "proceed with the run"
   "authorize the production run" "authorize the run"
   "finalize the production decision" "finalize the manufacturing decision"
   "affix the tax stamp" "affix the tax stamps"
   "finalize the labeling compliance determination"
   "finalize the labeling-compliance determination"
   "finalize the tax-stamp compliance decision"
   "override the shop safety officer's judgment"
   "override the safety officer's judgment"
   "override shop safety officer judgment"])

(defn- contains-excluded-phrase? [s]
  (let [s (str/lower (or s ""))]
    (boolean (some #(str/includes? s %) scope-excluded-phrases))))

(defn- hard-violations [proposal preparer-record facility-record]
  (let [{:keys [op rationale]} proposal]
    (cond-> []
      (nil? preparer-record)
      (conj {:rule :no-preparer
             :detail "未登録 preparer への提案は不可（preparer record は独立して検証・登録済みでなければならない）"})

      (nil? facility-record)
      (conj {:rule :no-facility
             :detail "未登録 facility への提案は不可（facility record は独立して検証・登録済みでなければならない）"})

      (not= :propose (:effect proposal))
      (conj {:rule :no-actuation
             :detail "effect は :propose のみ許可（governor は製造作業を直接実行しない）"})

      (not (contains? allowed-ops op))
      (conj {:rule :unknown-op
             :detail (str op " は closed op-allowlist に無い — 提案不可")})

      (or (contains? scope-excluded-ops op) (contains-excluded-phrase? rationale))
      (conj {:rule :scope-excluded-action
             :detail "製造実行判断の確定・規制コンプライアンス確定（タックススタンプ貼付/ラベリング適合判定）・shop safety officer の判断の上書きは、この actor の権限外 — 常に永続ブロック"}))))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a
  `store` implementing `tobaccocoord.store/Store`. Pure — never
  mutates the store, never dispatches a manufacturing operation."
  [request _context proposal store]
  (let [preparer-record (store/preparer store (:preparer-id request))
        facility-record (some->> (:facility-id proposal) (store/facility store))
        hard (hard-violations proposal preparer-record facility-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        supply-order-over-threshold?
        (and (= :coordinate-supply-order (:op proposal))
             (number? (:cost proposal))
             (> (:cost proposal) supply-cost-threshold))
        always-risky? (or (= :flag-compliance-concern (:op proposal))
                           supply-order-over-threshold?)]
    {:ok? (and (not hard?) (not low?) (not always-risky?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? always-risky?))}))
