(ns tobaccocoord.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [tobaccocoord.store :as store]
            [tobaccocoord.advisor :as advisor]
            [tobaccocoord.governor :as governor]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-preparer! st {:preparer-id "preparer-1" :name "Kobo Yamada"})
    (store/register-facility! st {:facility-id "F-1" :name "Kobo Tobacco Works" :max-supply-cost 2000})
    st))

(defn- op [op-kw & {:as extra}]
  (merge {:op op-kw :effect :propose :facility-id "F-1"
          :confidence 0.9 :stake :low}
         extra))

(def ^:private req {:preparer-id "preparer-1"})

(deftest ok-log-work-record
  (let [st (fresh-store)
        v (governor/check req {} (op :log-work-record) st)]
    (is (:ok? v))))

(deftest ok-schedule-crew-operation
  (let [st (fresh-store)
        v (governor/check req {} (op :schedule-crew-operation) st)]
    (is (:ok? v))))

(deftest ok-supply-order-at-threshold-boundary
  (testing "the supply-cost threshold escalate boundary is exclusive (over, not at)"
    (let [st (fresh-store)
          v (governor/check req {} (op :coordinate-supply-order :cost 2000) st)]
      (is (:ok? v)))))

(deftest hard-on-unregistered-preparer
  (let [st (fresh-store)
        v (governor/check {:preparer-id "nobody"} {} (op :log-work-record) st)]
    (is (:hard? v))
    (is (some #(= :no-preparer (:rule %)) (:violations v)))))

(deftest hard-on-unregistered-facility
  (let [st (fresh-store)
        v (governor/check req {} (op :log-work-record :facility-id "F-ghost") st)]
    (is (:hard? v))
    (is (some #(= :no-facility (:rule %)) (:violations v)))))

(deftest hard-on-no-actuation-violation
  (let [st (fresh-store)
        v (governor/check req {} (assoc (op :log-work-record) :effect :direct-write) st)]
    (is (:hard? v))
    (is (some #(= :no-actuation (:rule %)) (:violations v)))))

(deftest hard-on-op-outside-closed-allowlist
  (let [st (fresh-store)
        v (governor/check req {} (op :dispatch-equipment) st)]
    (is (:hard? v))
    (is (some #(= :unknown-op (:rule %)) (:violations v)))))

(deftest hard-on-scope-excluded-op-finalize-manufacturing-decision
  (testing "finalizing a manufacturing-execution decision is a permanent block, never a routine op"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :finalize-manufacturing-decision) :confidence 0.99) st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest hard-on-scope-excluded-op-authorize-production-run
  (testing "authorizing a production run is a permanent block, never a routine op"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :authorize-production-run) :confidence 0.99) st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest hard-on-scope-excluded-op-affix-tax-stamp
  (testing "affixing a tax stamp is a regulatory-compliance-clearance decision, a permanent block, never a routine op"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :affix-tax-stamp) :confidence 0.99) st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest hard-on-scope-excluded-op-finalize-labeling-compliance-determination
  (testing "finalizing a labeling-compliance determination is a regulatory-compliance-clearance decision, a permanent block, never a routine op"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :finalize-labeling-compliance-determination) :confidence 0.99) st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest hard-on-scope-excluded-op-override-shop-safety-officer-judgment
  (testing "overriding a shop safety officer's judgment is a shop safety officer's exclusive judgment, never this actor's"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :override-shop-safety-officer-judgment) :confidence 0.99) st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest hard-on-scope-excluded-rationale-production-run-even-with-allowlisted-op
  (testing "defense-in-depth: a rationale that itself attempts to proceed with the production run is blocked even if the op looks routine"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :schedule-crew-operation)
                                           :rationale "recommend we proceed with the production run now")
                             st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest hard-on-scope-excluded-rationale-tax-stamp-even-with-allowlisted-op
  (testing "defense-in-depth: a rationale that itself attempts to affix the tax stamp is blocked even if the op looks routine"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :log-work-record)
                                           :rationale "go ahead and affix the tax stamp on this batch")
                             st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest hard-on-scope-excluded-rationale-override-shop-safety-officer
  (testing "defense-in-depth: a rationale attempting to override the shop safety officer's judgment is blocked even if the op looks routine"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :log-work-record)
                                           :rationale "override the shop safety officer's judgment and proceed")
                             st)]
      (is (:hard? v))
      (is (some #(= :scope-excluded-action (:rule %)) (:violations v))))))

(deftest always-escalates-compliance-concern-even-at-high-confidence
  (testing "a regulatory-compliance/equipment-hazard/dust-exposure concern always requires human sign-off"
    (let [st (fresh-store)
          v (governor/check req {} (assoc (op :flag-compliance-concern :hazard-type :dust-exposure-risk)
                                           :confidence 0.99)
                             st)]
      (is (not (:hard? v)))
      (is (:escalate? v)))))

(deftest always-escalates-supply-order-above-threshold
  (let [st (fresh-store)
        v (governor/check req {} (assoc (op :coordinate-supply-order :cost 5000) :confidence 0.99) st)]
    (is (not (:hard? v)))
    (is (:escalate? v))))

(deftest escalates-low-confidence
  (let [st (fresh-store)
        v (governor/check req {} (assoc (op :log-work-record) :confidence 0.3) st)]
    (is (not (:hard? v)))
    (is (:escalate? v))))

(deftest default-mock-advisor-proposals-never-self-trip-on-scope-exclusion
  (testing "the governor's scope-exclusion term list must never match the mock advisor's own default rationale text for any allowlisted op — CLAUDE.md's known self-tripping bug pattern (rationale legitimately contains bare nouns like 'tobacco'/'tax stamp'/'labeling'/'safety officer', but never the full finalization-action phrases)"
    (let [st (fresh-store)
          adv (advisor/mock-advisor)
          ops [:log-work-record :schedule-crew-operation
               :flag-compliance-concern :coordinate-supply-order]]
      (doseq [o ops]
        (let [request {:preparer-id "preparer-1" :op o :facility-id "F-1"
                        :stake :low :task "routine tobacco-preparation task"
                        :hazard-type :dust-exposure-risk :cost 500}
              proposal (advisor/-advise adv st request)
              v (governor/check request {} proposal st)]
          (is (not (:hard? v))
              (str o " proposal unexpectedly hard-blocked: " (:violations v))))))))
