(ns dictim.cookbook.recipe5
  (:require [dictim.graph.core :as g]
            [dictim.d2.compile :as c]
            [clojure.data.json :as json]))


;; *****************************************
;; *           Diagram Specs               *
;; *****************************************

;; This is for building network diagrams - classic boxes and edges diagrams.
;; Templates look like this...

;; {:nodes *sequence-of-node-maps*
;;  :edges *sequences-of-edge-maps*
;;  :node->key :id
;;  :nodes {:labels *label-specs*
;;          :styles *style-specs*}
;;  :edges {:labels *label-specs*
;;          :styles *style-specs*}

;;  ;;and optionally..

;;  :node->container :dept
;;  :container->parent "a-lookup-map"
;;  :container->attrs "a-lookup-map"
;;  }

;; Please see lower down for an example diagram spec.

;; This namespace allows for a dictim diagram to be specified simply with
;; only data; no functions required! The data part is made up of nodes and edges
;; where each node and each edge is just a Clojure map.
;; Styling and Labelling rules are called (style- and label-) specs and allow
;; for conditional application.
;; grouping is specified using two keys: node->container which determines
;; the box each node could be put in, and container->parent lookup hierarchy
;; which is specified as map.
;; *****************************************


(def ^{:private true} comparators
  {:equals =
   :not-equals not=
   :contains some
   :doesnt-contain (complement some)
   :> >
   :< <
   :<= <=
   :>= >=})


;; *****************************************
;; *              Conditions               *
;; *****************************************

;; Conditions are the way to filter how a label rule (called a 'spec' - see below) or
;; a style spec is conditionally applied. A condition is a three element vector..
;;   element 1 is a 'comparator' in keyword form (above)
;;   element 2 is the key used extract the value to-be-compared from the node
;;     or edge map.
;;   element 3 is the value-to-be-compared-against
;; e.g.
;; `[:contains :business-functions "booking"]` or `[:> :tco 1200000]`

;; Conditions can be chained together and when they are, the first element in the vector
;; must be either `:and` or `:or`.
;; e.g.
;; [:and [:contains :business-functions "booking"][:> "tco" 1200000]]
;; means that both conditions should be true for the condition to be satisfied. Whereas
;; [:or [:contains :business-functions "booking"][:> "tco" 1200000]]
;; means that only one or other condition should be true.

(defn- get*
  "A generalized version of get/ get-in.
   If k is a keyword/ string, performs a normal get from the map m, otherwise
   if k is a vector of keywords/ strings performs a get-in."
  [m k]
  (cond
    (keyword? k)         (k m)
    (string? k)          (get m k)
    (and (vector? k)
         (every? #(or (string? %) (keyword? %)) k))
    (get-in m k)
    
    :else (throw (Exception. (str "Key must be a keyword, string or vector of either.")))))


(defn- contains-vectors?
  "Returns true if coll contains one or more vectors."
  [coll]
  (some vector? coll))


(defmacro ^{:private true} single-condition
  "Returns code that tests whether the condition is true for the item
   specified by sym."
  [sym condition]
  `(let [[comparator# k# v#] ~condition
         v-found# (get* ~sym k#)
         comp# (comparator# comparators)]

     (cond
       (and (not (coll? v-found#))
            (or (= :contains comparator#) (= :doesnt-contain comparator#)))
       (throw (Exception. (str ":contains and :doesnt-contain can only be used on collections. "
                               "No collection was found under the key " k#
                               " for the item " ~sym)))

       (coll? v-found#)
       (comp# (conj #{} v#) v-found#)

       :else
       (comp# v-found# v#))))


(defmacro ^{:private true} condition
  "Returns code that tests whether the condition/s is/are true for the item
   specified by sym."
  [sym condition]
  `(if (contains-vectors? ~condition)
     (if (= (first ~condition) :or)
       (some identity (map #(single-condition ~sym %) (rest ~condition)))
       (every? identity (map #(single-condition ~sym %) (rest ~condition))))
     (single-condition ~sym ~condition)))

;; *****************************************
;; *                Specs                  *
;; *****************************************

;; Specs are either a style spec or a label spec, both one or two element vectors.
;; When there's no conditional logic required, a spec has only one element; the 'spec instruction'.

;; In the case of style spec, the instruction is the map of dictim/ d2 styles to be applied
;; and their values e.g.
;; `{color "red"}`

;; In the case of a label spec, it can be any of:
;; - a map like:
;;     {:show-key? true/false (defaults to false) :key *keyword/ string/ vector or keywords/ strings}
;;     - a keyword/ string are used to directly extract the value for the label from the top level
;;       of the node or edge map.
;;     - a vector of keywords is for extracting a nested value when the node or edge is
;;       represented by nested maps.
;;     :show-key? if true to include the key in the label, e.g. `tco: 2000000`
;; OR a vector of such maps for composite labels.


;; When conditional logic is required, the second element in the vector should be a condition
;; as described in the 'Conditions' section above.
;; example label spec:
;; `[:id [:and [:contains :business-functions "booking"][:> :tco 1200000]]]`
;; another:
;; `[[:app_name :app_id] [:or [:contains :business-functions "pricing"][:equals :owner "Maria"]]]`
;; example style spec
;; `[{:color "red"} [:or [:contains :business-functions "booking"][:> :tco 1200000]]]`
;; another:
;; `[{:color "black"}]`

;; *****************************************
;; *           Spec Validation             *
;; *****************************************

(defn- valid-single-condition?
  [condition]
  (and
   ;; is a vector
   (vector? condition)

   ;; the first item is a comparator
   (some #{(first condition)} (keys comparators))

   ;; has 3 elements
   (= 3 (count condition))))


(defn- valid-condition?
  [condition]
  (if (and (vector? condition) (contains-vectors? condition))
    (and
     (let [comp (first condition)]
       (some #{comp} [:or :and]))
     (every? valid-single-condition? (rest condition)))
    (valid-single-condition? condition)))


(defn- valid-style? [style]
  (map? style))


(defn- valid-label? [lbl]
  ;; simple validation for label instructions. TODO improve
  (or (map? lbl)
      (and (vector? lbl)
           (every? map? lbl))))


(defn- specs-valid?
  [spec-type specs]
  (let [counts (map count specs)]
    (and
     ;; each spec should should have 1 or 2 elements
     (every? #(or (= 1 %) (= 2 %)) counts)

     ;; there should be no more than 1 one-element spec. the :else clause
     (let [ones (filter #(= 1 %) counts)
           count-ones (count ones)]
       (< count-ones 2))

     ;; the one element spec should be a valid label or style instruction
     (let [else-spec (first (filter #(= 1 (count %)) specs))]
       (if (nil? else-spec)
         true
         (case spec-type
           :label (valid-label? (first else-spec))
           :style (valid-style? (first else-spec)))))

     ;; two element specs are a map and a valid condition
     (let [conditional-specs (filter #(= 2 (count %)) specs)]
       (if (nil? conditional-specs)
         true
         (every?
          (fn [[style-or-label condition]]
            (and
             (case spec-type
               :label (valid-label? style-or-label)
               :style (valid-style? style-or-label))
             (valid-condition? condition)))
          conditional-specs))))))


(defn- put-last
  [pred coll]
  "Puts the first item in coll that satisfies pred to the end."
  (let [splits (split-with (complement pred) coll)]
    (cond
      (empty? (first splits))
      (conj (into [] (rest (second splits))) (first coll))

      (empty? (second splits))
      coll

      :else
      (let [front (into [] (concat (first splits) (rest (second splits))))]
        (conj front (first (second splits)))))))


(defn- without-condition-spec?
  [spec]
  (= 1 (count spec)))


(defn- convert-without-condition-spec
  [spec]
  (if (without-condition-spec? spec)
    (list :else (first spec))
    spec))


(defn- prep-specs
  "If there's an :else clause, ensure it's at the end."
  [spec-type specs]
  (if (not (specs-valid? spec-type specs))
    (throw (Exception. "specs are not valid."))
    (->> (put-last without-condition-spec? specs)
         (map reverse)
         (map convert-without-condition-spec))))


(defn- prep-label-component
  "Preps a single part of a label instruction."
  [item label-instruction-component]
  (let [k (:key label-instruction-component)
        tag (when (:show-key? label-instruction-component)
              (str (name k) ": "))]
    (if k

      (cond
        (and (vector? k)
             (every? #(or (string? %) (keyword? %)) k))
        (str tag (get-in item k))

        (or (string? k) (keyword? k))    (str tag (get item k nil))

        :else                            nil)
      
      (throw (Exception. (str "Label spec instruction: " label-instruction-component
                              " does not specify a key!"))))))


(defn- prep-label
  [item label-instruction]
  (cond
    (and (vector? label-instruction)
         (every? map? label-instruction))

    (apply str (interpose "\n" (map #(prep-label-component item %) label-instruction)))  

    (map? label-instruction)            (prep-label-component item label-instruction)

    :else nil))


(defmacro ^{:private true} specs
  "Convert specs into functions which are matched against the value stored
  in sym. spec-type must be either :label or :style."
  [sym spec-type specs]
  `(let [sp# (prep-specs ~spec-type ~specs)
         as-fns# (reduce
                  (fn [acc# [condition# res#]]
                    (let [res1# (if (= :label ~spec-type)
                                  (prep-label ~sym res#) res#)]
                      (if (valid-condition? condition#)
                        (conj acc# [(condition ~sym condition#) res1#])
                        (conj acc# [condition# res1#]))))
                  []
                  sp#)]
     as-fns#))


(defn- first-true
  "Returns the application of f on the first value whose resolved spec is true."
  ([resolved-spec] (first-true identity resolved-spec))
  ([f resolved-spec]
   (reduce
    (fn [acc [resolved? v]]
      (if resolved? (reduced (f v)) acc))
    nil
    resolved-spec)))


(defmacro ^{:private true} spec-fn
  "Converts a node->attrs/ edge-attrs expression that use data to 
  express specs and conditions into a function."
  [m]
  `(fn [item#]
     (let [label# (:labels ~m)
           style# (:styles ~m)
           lbl# (when label# [:label (first-true (specs item# :label label#))])
           stl# (when style# [:style (first-true (specs item# :style style#))])
           res# (remove nil? (concat lbl# stl#))]
       (apply assoc {} res#))))


;; example diaagram spec
(def ^{:private true} ex-diagram
  {:nodes '({:id "app12872",
             :name "Trade pad",
             :owner "Lakshmi",
             :dept "Finance",
             :functions ("Position Keeping" "Quoting"),
             :tco 1200000,
             :process "p.112"}
            {:id "app12873",
             :name "Data Source",
             :owner "India",
             :dept "Securities",
             :functions ("Booking" "Order Mgt"),
             :tco 1100000,
             :process "p.114"}
            {:id "app12874",
             :name "Crypto Bot",
             :owner "Joesph",
             :dept "Equities",
             :functions ("Accounting" "Booking"),
             :tco 500000,
             :process "p.112"}
            {:id "app12875",
             :name "Data Solar",
             :owner "Deepak",
             :dept "Securities",
             :functions ("Position Keeping" "Data Master"),
             :tco 1000000,
             :process "p.114"}
            {:id "app12876",
             :name "Data Solar",
             :owner "Lakshmi",
             :dept "Risk",
             :functions ("Accounting" "Data Master"),
             :tco 1700000,
             :process "p.114"})
   :edges '({:src "app12874", :dest "app12875", :data-type "security reference"}
            {:src "app12874", :dest "app12876", :data-type "quotes"}
            {:src "app12875", :dest "app12875", :data-type "instructions"}
            {:src "app12874", :dest "app12872", :data-type "instructions"}
            {:src "app12875", :dest "app12874", :data-type "client master"}
            {:src "app12875", :dest "app12874", :data-type "allocations"})
   :node->key :id
   :node->container :dept
   :container->parent {"Finance" "2LOD" "Risk" "2LOD" "Securities" "FO" "Equities" "FO"}
   :node-specs {:labels [[{:key :owner} [:equals :dept "Equities"]][{:key :name}]]}
   :edge-specs {:labels [[{:key :data-type}]]}
   :container->attrs {"Securities" {:style.fill "green"}}
   })


(defn- valid-diagram-spec?
  [diag]
  (cond
    (let [nds (-> diag :nodes)]
      (not (and nds
                (every? map? nds))))
    (throw (Exception. "A diagram spec must include valid nodes data under {:data {:nodes ..."))

    (let [eds (-> diag :edges)]
      (when eds (not (every? map? eds))))
    (throw (Exception. "The diagram spec's edge data under {:data {:edges ...  is not valid."))

    (not (-> diag :node->key))
    (throw (Exception. "A diagram spec must include a `:node->key` entry."))

    :else true))


;; *****************************************
;; *             Public API                *
;; *****************************************

(defn network-diagram->d2
  "Takes a diagram spec and produces d2.
   A diagram spec is a map which must have keys:
     [:data :nodes] a sequence of nodes (each of which is an arbitrary map).
     [:data :edges] a sequence of edges ( ditto ).
     :node->key     the key used to extract a unique value (the id) from each node.
   and optionally can have keys:
     :node-specs  a map with spec entries under :labels and :styles keys.
     :edge-specs  ditto
     :node->container a key applied to each node to determine which container it is in
     :container->parent a map with containers mapped to their parent containers.
     :container->attrs a map of the container (name) to a map of d2 styling elements,
       e.g. {\"my container\" {:style.fill \"pink\"} ...}}"
  [diag]
  (when (valid-diagram-spec? diag)
    (let [nodes (-> diag :nodes)
          edges (-> diag :edges)
          node->key (-> diag :node->key)
          node-fn (if (-> diag :node-specs) (spec-fn (-> diag :node-specs)) (constantly nil))
          edge-fn (if (-> diag :edge-specs) (spec-fn (-> diag :edge-specs)) (constantly nil))
          node->container (-> diag :node->container)
          container->parent (-> diag :container->parent)
          container->attrs (-> diag :container->attrs)
          directives (-> diag :directives)
          dictim-fn-params (cond->
                               {:node->key node->key
                                :node->attrs node-fn
                                :edge->attrs edge-fn
                                :cluster->attrs container->attrs}
                               node->container (assoc :node->cluster node->container)
                               container->parent (assoc :cluster->parent container->parent))
          dictim (g/graph->dictim nodes edges dictim-fn-params)
          dictim' (if directives (cons directives dictim) dictim)]
      (apply c/d2 dictim'))))


(def ^{:private true} path "samples/in.d2")


(defn out [diag]
  (spit path (network-diagram->d2 diag)))


;; serialization/ deserialization of diagram specs



(defn serialize-diagram
  "Serializes a diagram spec to json."
  [diagram-spec]
  (json/write-str diagram-spec))


(defn- convert-element [element]
  (cond
    (map? element)
    (into {} (map (fn [[k v]] [k (keyword v)]) element))

    (vector? element)
    (conj (mapv keyword (take 2 element)) (last element))

    :else element))


(defn- convert-specs [specs]
  (mapv #(mapv convert-element %) specs))


(defn deserialize-diagram
  "Properly deserializes the json representation of a diagram spec."
  [json-text]
  (json/read-str json-text
                    :key-fn keyword
                    :value-fn (fn [k v]
                                (cond
                                  (or (= k :labels) (= k :styles))
                                  (convert-specs v)

                                  (or (= k :node->container) (= k :node->key))
                                  (keyword v)

                                  (or (= k :container->parent) (= k :container->attrs))
                                  (into {}
                                        (map (fn [[k v]] [(name k) v]) v))

                                  :else v))))
