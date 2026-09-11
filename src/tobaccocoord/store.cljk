(ns tobaccocoord.store
  "SSoT for the ISCO-08 7516 tobacco preparation and tobacco-products
  manufacturing floor scheduling/logistics coordination actor (itonami
  actor pattern, ADR-2607121000 / CLAUDE.md Actors section; README's
  'Robotics premise' — a factory-floor scheduling/logistics
  coordination robot performs crew scheduling, batch/inventory/
  progress record logging and tobacco-materials supply-order
  coordination for a tobacco preparation and tobacco-products
  manufacturing crew under this advisor/governor pair, which never
  dispatches hardware itself, never performs tobacco-preparation or
  tobacco-product-making work itself, and never finalizes a
  manufacturing-execution decision or a regulatory-compliance-
  clearance decision (tax-stamp affixing, labeling-compliance
  determination), nor overrides a shop safety officer's judgment —
  those remain the shop safety officer's / regulatory-compliance
  function's exclusive judgment). Modeled closely on
  cloud-itonami-isco-7211's foundrycoord.store for the
  regulated-manufacturing-clearance-plus-workshop-safety domain shape.

  Domain:

    preparer — a registered tobacco preparer / tobacco-products maker
               crew member (:preparer-id, :name)
    facility — a registered tobacco-manufacturing facility
               {:facility-id :name :max-supply-cost number}.
               `:max-supply-cost` is an informational registered
               ceiling used only to decide whether a
               `:coordinate-supply-order` proposal escalates to human
               sign-off (the governor never blocks a within-threshold
               order outright; it only decides commit vs. escalate).
    record   — a committed operating record (a logged batch/inventory/
               progress entry, a scheduled crew/machine operation, a
               flagged regulatory-compliance/equipment-hazard/dust-
               exposure concern, or a coordinated tobacco-materials
               supply order) — written ONLY via commit-record!.
    ledger   — append-only audit trail, commit or hold.")

(defprotocol Store
  (preparer [s preparer-id])
  (facility [s facility-id])
  (records-of [s preparer-id])
  (ledger [s])
  (register-preparer! [s preparer])
  (register-facility! [s facility])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (preparer [_ preparer-id] (get-in @a [:preparers preparer-id]))
  (facility [_ facility-id] (get-in @a [:facilities facility-id]))
  (records-of [_ preparer-id] (filter #(= preparer-id (:preparer-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-preparer! [s p]
    (swap! a assoc-in [:preparers (:preparer-id p)] p) s)
  (register-facility! [s f]
    (swap! a assoc-in [:facilities (:facility-id f)] f) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:preparers {} :facilities {} :records [] :ledger []}
                                    seed)))))
