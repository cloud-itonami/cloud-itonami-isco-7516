(ns tobaccocoord.advisor
  "Tobacco Preparation Coordination Advisor — proposing a tobacco
  manufacturing-floor scheduling/logistics coordination operation (log
  a work record, schedule a crew operation, flag a regulatory-
  compliance/equipment-hazard/dust-exposure concern, coordinate a
  tobacco-materials supply order) from a crew roster, facility
  registration and compliance-reporting policy. Swappable mock/llm;
  the advisor ONLY proposes — `tobaccocoord.governor` independently
  gates every proposal and always escalates compliance concerns and
  above-threshold supply orders. The advisor never proposes to
  directly finalize a manufacturing-execution decision (e.g. deciding
  to proceed with a specific production run) or a regulatory-
  compliance-clearance decision (e.g. affixing a tax stamp or
  finalizing a labeling-compliance determination), and never proposes
  to override a shop safety officer's judgment — those stay
  permanently out of this actor's scope. Modeled closely on
  cloud-itonami-isco-7211's advisor for the
  regulated-manufacturing-clearance-plus-workshop-safety domain shape.

  A proposal: {:op :log-work-record|:schedule-crew-operation|
               :flag-compliance-concern|:coordinate-supply-order
               :effect :propose :preparer-id str :facility-id str
               :cost number :hazard-type kw :task str :stake kw
               :confidence n :rationale str}")

(defprotocol Advisor
  (-advise [advisor store request] "request -> proposal map"))

(defn- rationale-for [op preparer-id facility-id hazard-type]
  (case op
    :log-work-record
    (str "logged work record for preparer " preparer-id " at facility " facility-id)

    :schedule-crew-operation
    (str "scheduled crew operation for tobacco-preparation task at facility " facility-id)

    :flag-compliance-concern
    (str "flagged " (name (or hazard-type :hazard)) " concern for preparer "
         preparer-id " at facility " facility-id " — routed for shop safety officer review")

    :coordinate-supply-order
    (str "coordinated supply order for preparer " preparer-id " at facility " facility-id)

    (str "proposed " (name op) " for preparer " preparer-id " at facility " facility-id)))

(defn- infer [_store {:keys [op stake preparer-id facility-id cost hazard-type task]
                       :as request}]
  {:op op
   :effect :propose
   :preparer-id preparer-id
   :facility-id facility-id
   :cost cost
   :hazard-type hazard-type
   :task task
   :stake (or stake :low)
   :confidence (case (or stake :low) :high 0.7 :medium 0.85 :low 0.95)
   :rationale (rationale-for op preparer-id facility-id hazard-type)})

(defn mock-advisor []
  (reify Advisor
    (-advise [_ store request] (infer store request))))

(def ^:private system-prompt
  "You are a tobacco preparation and tobacco-products manufacturing
   factory-floor scheduling/logistics coordination advisor. Given a
   request, propose an :op (one of :log-work-record,
   :schedule-crew-operation, :flag-compliance-concern,
   :coordinate-supply-order), the :preparer-id, :facility-id, and any
   :cost/:hazard-type/:task fields, an honest :confidence and a
   :stake. Never propose an op outside this closed list, and never
   propose to directly finalize a manufacturing-execution decision
   (e.g. deciding to proceed with a specific production run), or a
   regulatory-compliance-clearance decision (e.g. affixing a tax
   stamp, or finalizing a labeling-compliance determination), or to
   override a shop safety officer's judgment — those are always out
   of this actor's scope; it coordinates factory-floor
   scheduling/logistics only and never performs tobacco-preparation or
   tobacco-product-making work or clears regulatory-compliance
   decisions itself. Regulatory-compliance and equipment-hazard/
   dust-exposure concerns always require human sign-off regardless of
   confidence.")

(defn- parse-proposal [content]
  (try
    (let [p (read-string content)]
      (if (map? p)
        (assoc p :effect :propose)
        {:op :unknown :effect :propose :confidence 0.0 :stake :high
         :rationale "unparseable LLM response"}))
    (catch #?(:clj Exception :cljs js/Error) _
      {:op :unknown :effect :propose :confidence 0.0 :stake :high
       :rationale "LLM response parse failure"})))

(defn llm-advisor
  [chat-model model-generate-fn gen-opts]
  (reify Advisor
    (-advise [_ _store request]
      (let [msgs [{:role :system :content system-prompt}
                  {:role :user :content (str "operation request: " (pr-str request))}]
            resp (model-generate-fn chat-model msgs gen-opts)]
        (parse-proposal (:content resp))))))
