(ns dictim.cookbook.recipe2
  (:require [graphdict.graph :as g]
            [dictim.compile :as c]))


;; ****************************************************************
;; *                        Recipe 2                              *
;; *                    Building dictim                           *
;; *            Producing architectural diagrams                  *
;; ****************************************************************

;; Let's pick a use case of generating architectural diagrams in a large,
;; complex organization that has lots of different applications and data
;; flows between them.


;; Let's generate some random apps, each with a :key, :name, :owner, :dept and (set of business) :functions

(defn app-id [] (str (gensym "app")))


(defn app-name [] (rand-nth
               ["Trade Aid" "Trade pad" "Trade propel" "Crypto Bot" "Ark Crypto" "Book Relay"
                "Book down" "Data Source" "ARC3" "Booking Flash" "Globe-X" "Apac Ace" "Acmaze"
                "Book good" "Risk Sheet" "Seven heaven" "Credit guard" "Risk Global" "Attunity"
                "Data Solar" "Flame" "Flame minor" "Data sky" "Zone out" "Commune New"]))


(defn owner [] (rand-nth
                ["Joesph" "Tomos" "Dale" "Deepak" "Darcie" "Lowri" "India" "Lakshmi" "Wendy"]))

;; department
(defn dept [] (rand-nth
               ["Equities" "Fixed Income" "Corporate" "Securities" "Funds" "Risk" "Finance"]))


(defn functions [] (rand-nth
                    ["Quoting" "Pricing" "Data Master" "Order Mgt" "Execution" "Booking"
                     "Position Keeping" "Risk Mgt" "Accounting" "Allocations"]))


(defn tco [] (* 100000 (rand-int 20)))


(defn process [] (rand-nth
                  ["p.112" "p.113" "p.114"]))


(defn app-functions [] (list (functions) (functions)))


(defn apps
  "Usage e.g.: (take 5 (apps))"
  []
  (cons {:id (app-id)
         :name (app-name)
         :owner (owner)
         :dept (dept)
         :functions (app-functions)
         :tco (tco)
         :process (process)}
        (lazy-seq (apps))))


;; Let's generate some random data flows (between the apps)
;; each flow has a :src, :dest and a :data-type

(defn data-type [] (rand-nth
                    ["trades" "positions" "client master" "security reference" "quotes"
                     "orders" "allocations" "greeks" "instructions" "payments"]))


(defn flows
  "Usage e.g.: (take 5 (flows)"
  [apps]
  (let [verts (map :id apps)
        vert #(rand-nth verts)]
    (cons {:src (vert)
           :dest (vert)
           :data-type (data-type)}
          (lazy-seq (flows apps)))))


;; Now, let's outline the data required to specify a (d2) diagram
;; Will look like
;; {:data {:apps [..] :flows [..]}
;;  :transform-fn ..
;;  :filters {:app-filter-fn ..
;;            :flow-filter-fn ..
;;            :filter-fn ..   <- works on all
;;  :dictim-fn <function that converts to dictim>
;;  :dicitm-fn-params e.g. {a map of params to dictim-fn}
;;  :file-out ..name of d2 file to output ..}

;; For that diagram spec, let's build a function to handle it

(defn arch-diagram->d2
  [spec]
  (let [transform-fn (or (-> spec :transform-fn) identity)
        data (transform-fn (-> spec :data))]
    (let [nodes (->> (-> data :apps)
                     (filter (or (-> spec :filters :app-filter-fn) identity)))
          edges (->> (-> data :flows)
                     (filter (or (-> spec :filters :flow-filter-fn) identity)))
          dictim ((-> spec :dictim-fn) nodes edges (-> spec :dictim-fn-params))]
      (spit (-> spec :file-out) (apply c/d2 dictim)))))


;; let's define a path to write the .d2 files out to:
(def path "../dictim/test/dictim/d2/in.d2")

;; You should have the d2 executable installed: https://github.com/terrastruct/d2#install
;; and running watching the file you've defined above. e.g. In a terminal:
;; d2 -w -l tala in.d2 out.svg
;; I like the 'tala' layout engine for most purposes.


;; example usage
(defn- has? [coll xs]
  (first (filter #(contains? (set xs) %) coll)))


(def data (atom {}))


;; Generate some random diagrams, over and over.
(let [apps (take 10 (apps))
      flows (take 12 (flows apps))]
  (reset! data {:apps apps :flows flows})
  (arch-diagram->d2
   {:data {:apps apps
           :flows flows }
    :filters {:filter-fn identity
              :app-filter-fn identity
              :flow-filter-fn identity #_(contains?
                                          #{"trades" "positions" "quotes"}
                                          (:data-type %)) }
    :dictim-fn g/graph->dictim
    :dictim-fn-params {:node->key (fn [n] (str (:id n)))
                       :node->attrs (fn [n] {:label (str (:name n))})
                       :edge->attrs (fn [e] {:label (:data-type e)})
                       :node->cluster :dept
                       :cluster->parent (fn [_] :bank)}
    :file-out path}))


;; *********************************************************************************
;; look at small data
(declare small-data)


(arch-diagram->d2
 {:data small-data
  :filters {:app-filter-fn identity
            :flow-filter-fn identity}
  :dictim-fn g/graph->dictim
  :dictim-fn-params {:node->key :id
                     :node->attrs (fn [n] {:label (str (:name n))})
                     :edge->attrs (fn [e] {:label (:data-type e)})
                     :node->cluster :dept
                     :cluster->parent (fn [_] :bank)}
  :file-out path})


;; highlight apps that participate in various functions..
;; pink = Pricing, green = Quoting, oragne = Order Mgt

(arch-diagram->d2
 {:data small-data
  :filters {:app-filter-fn identity
            :flow-filter-fn identity}
  :dictim-fn g/graph->dictim
  :dictim-fn-params {:node->key :id
                     :node->attrs (fn [n] {:label (str (:name n))
                                           :style (cond
                                                      (has? (:functions n) ["Pricing"])
                                                      {:fill "pink"}

                                                      (has? (:functions n) ["Quoting"])
                                                      {:fill "green"}

                                                      (has? (:functions n) ["Order Mgt"])
                                                      {:fill "orange"}
                                                      
                                                      :else {})})
                     :edge->attrs (fn [e] {:label (:data-type e)})
                     :node->cluster :dept
                     :cluster->parent (fn [_] :bank)}
  :file-out path})


;; Highlight flows of 'Client Master' data

(arch-diagram->d2
 {:data small-data
  :filters {:app-filter-fn identity
            :flow-filter-fn identity}
  :dictim-fn g/graph->dictim
  :dictim-fn-params {:node->key :id
                     :node->attrs (fn [n] {:label (str (:name n))
                                           :style (cond
                                                      (has? (:functions n) ["Pricing"])
                                                      {:fill "pink"}

                                                      (has? (:functions n) ["Quoting"])
                                                      {:fill "green"}

                                                      (has? (:functions n) ["Order Mgt"])
                                                      {:fill "orange"}
                                                      
                                                      :else {})})
                     :edge->attrs (fn [e] {:label (:data-type e)
                                           :style (cond
                                                    (= "client master" (:data-type e))
                                                    {:stroke-dash "4"
                                                     :animated "true"
                                                     :stroke "brown"
                                                     :stroke-width 2}

                                                    :else {})})
                     :node->cluster :dept
                     :cluster->parent (fn [_] :bank)}
  :file-out path})


;; Zoom in function
;; traversals are connections that cross the boundary of the zoom
;; extremely hacky - should have used Loom's graph algorithms.
(defn zoom [k traversals? & vs]
  (fn [data]
    (let [apps' (filter (fn [app] (contains? (set vs) (get app k))) (-> data :apps))
          apps'' (filter (fn [app] (not (contains? (set vs) (get app k)))) (-> data :apps))
          appids (set (map :id apps'))
          flows (if traversals?
                  (reduce
                   (fn [acc cur]
                     (cond
                       (and (contains? appids (:src cur))
                            (contains? appids (:dest cur)))
                       (cons (assoc cur :relation :inside) acc)
                       
                       (contains? appids (:src cur))
                       (cons (assoc cur :relation :out) acc)

                       (contains? appids (:dest cur))
                       (cons (assoc cur :relation :in) acc)

                       :else acc))
                   nil
                   (-> data :flows))
                  (filter #(and (contains? appids (:src %))
                                (contains? appids (:dest %)))
                          (-> data :flows)))
          inids (set (map :src (filter #(= :in (:relation %)) flows)))
          outids (set (map :dest (filter #(= :out (:relation %)) flows)))
          inoutids (clojure.set/intersection inids outids)
          inids (clojure.set/difference inids inoutids)
          outids (clojure.set/difference outids inoutids)
          inapps (map #(assoc % :relation :ins) (filter #(contains? inids (:id %)) apps''))
          outapps (map #(assoc % :relation :outs) (filter #(contains? outids (:id %)) apps''))
          inoutapps (map #(assoc % :relation :inouts) (filter #(contains? inoutids (:id %)) apps''))]
      {:apps (concat (map #(assoc % :relation :inside)  apps') inapps outapps inoutapps)
       :flows flows})))


;; filter down to just Securities and Equities
(arch-diagram->d2
 {:data small-data
  :transform-fn (zoom :dept false "Securities" "Equities")
  :filters {:app-filter-fn identity
            :flow-filter-fn identity}
  :dictim-fn g/graph->dictim
  :dictim-fn-params {:node->key :id
                     :node->attrs (fn [n] {:label (str (:name n))
                                           :style (cond
                                                    (has? (:functions n) ["Pricing"])
                                                    {:fill "pink"}

                                                    (has? (:functions n) ["Quoting"])
                                                    {:fill "green"}

                                                    (has? (:functions n) ["Order Mgt"])
                                                    {:fill "orange"}
                                                      
                                                    :else {})})
                     :edge->attrs (fn [e] {:label (:data-type e)
                                           :style (cond
                                                    (= "trades" (:data-type e))
                                                    {:stroke-dash 5
                                                     :stroke-width 2
                                                     :animated true
                                                     :stroke "green"}

                                                    :else {})})
                     :node->cluster :dept
                     :cluster->parent (fn [_] :bank)}
  :file-out path})


;; filter down to just Securities, but show anything that has a direct input or output with Securities
(arch-diagram->d2
 {:data small-data
  :transform-fn (zoom :dept true "Securities")
  :filters {:app-filter-fn identity
            :flow-filter-fn identity}
  :dictim-fn g/graph->dictim
  :dictim-fn-params {:node->key :id
                     :node->attrs (fn [n] {:label (str (:name n))
                                           :style (cond
                                                    (has? (:functions n) ["Pricing"])
                                                    {:fill "pink"}

                                                    (has? (:functions n) ["Quoting"])
                                                    {:fill "green"}

                                                    (has? (:functions n) ["Order Mgt"])
                                                    {:fill "orange"}
                                                      
                                                    :else {})})
                     :edge->attrs (fn [e] {:label (:data-type e)
                                           :style (cond
                                                    (= "client master" (:data-type e))
                                                    {:stroke-dash "3"
                                                     :animated "false"
                                                     :stroke "brown"}

                                                    :else {})})
                     :node->cluster :dept
                     :cluster->parent (fn [_] :bank)}
  :file-out path})


;; same as before but group differently and fmt the edge apps differently
(arch-diagram->d2
 {:data small-data
  :transform-fn (zoom :dept true "Finance" "Equities" "Corporate")
  :filters {:app-filter-fn identity
            :flow-filter-fn identity}
  :dictim-fn g/graph->dictim
  :dictim-fn-params {:node->key :id
                     :node->attrs (fn [n] (cond
                                            (contains? #{:ins :outs :inouts} (:relation n))
                                            {:label (str (:name n))
                                             :shape "circle"
                                             :width "60"
                                             :height "60"}

                                            :else
                                            {:label (str (:name n))
                                             :shape "rectangle"
                                             :style.fill "'#dbbd95'"
                                             :style.stroke "'#9e7641'"
                                             :style.shadow "true"}))
                     :edge->attrs (fn [e] {:label (:data-type e)
                                           :style.stroke "'#9e642b'"})
                     :node->cluster :relation
                     :cluster->attrs (fn [c] (cond
                                               (= :inside c)
                                               {:label "business process"
                                                :style.fill "'#fff7de'"
                                                :style.stroke "'#4a423b'"}

                                               (= :p_112 c)
                                               {:label (if (= :inouts c)
                                                         "in and outs"
                                                         c)
                                                :style.fill "'#fefefe'"
                                                :style.stroke-width "1"
                                                :style.stroke "'#eeeeee'"}

                                               :else
                                               {:label (if (= :inouts c)
                                                         "in and outs"
                                                         c)
                                                :style.fill "'#fafafa'"
                                                :style.stroke-width "1"
                                                :style.stroke "'#eeeeee'"}))
                     :cluster->parent (fn [_] :p_112)}
  :file-out path})





;; Let's 'group' the same data a different way -> by business process

(arch-diagram->d2
 {:data small-data
  :filters {:app-filter-fn identity
            :flow-filter-fn #(= "client master" (:data-type %))}
  :dictim-fn g/graph->dictim
  :dictim-fn-params {:node->key :id
                     :node->attrs (fn [n] {:label (str (:name n))
                                           :shape "hexagon"
                                           :style (cond
                                                    (has? (:functions n) ["Pricing"])
                                                    {:fill "pink"}

                                                    (has? (:functions n) ["Quoting"])
                                                    {:fill "green"}

                                                    (has? (:functions n) ["Order Mgt"])
                                                    {:fill "orange"}
                                                      
                                                    :else {})})
                     :edge->attrs (fn [e] {:label (:data-type e)
                                           :stroke "'#4eb8ed'"
                                           :stroke-width "4"
                                           :font-size "16"
                                           :font-color "red"})
                     :node->cluster :process
                     :cluster->parent (fn [_] "Process View")
                     :cluster->attrs (fn [c] (cond
                                               (= c "Process View")
                                               {:style.fill "'#f7f6f5'" }
                                               
                                               :else {:style.fill "'#f5f0e1'"}))}
  :file-out path})


;; Or by owner..

(def org-structure
  {"India" "Paulie", "Tomos" "Paulie", "Dale" "Sandra",
   "Lakshmi" "Sandra", "Darcie" "Sandra", "Deepak" "Stuart",
   "Wendy" "Parandeep", "Parandeep" "Barney", "Barney" "David",
   "David" "Stuart", "Stuart" "Mike", "Sandra" "Oona", "Paulie"
   "Oona"})


(arch-diagram->d2
 {:data small-data
  :filters {:app-filter-fn identity
            :flow-filter-fn identity}
  :dictim-fn g/graph->dictim
  :dictim-fn-params {:node->key :id
                     :node->attrs (fn [n] {:label (str (:name n))})
                     :edge->attrs (fn [e] {:label (:data-type e)})
                     :node->cluster :owner
                     :cluster->parent org-structure}
  :file-out path})

;; *********************************************************************************
;; look at big data

(declare big-data)


(arch-diagram->d2
 {:data big-data
  :filters {:app-filter-fn identity
            :flow-filter-fn identity}
  :dictim-fn g/graph->dictim
  :dictim-fn-params {:node->key :id
                     :node->attrs (fn [n] {:label (str (:name n))
                                           :style (cond
                                                    (has? (:functions n) ["Pricing"])
                                                    {:fill "pink"}

                                                    (has? (:functions n) ["Quoting"])
                                                    {:fill "green"}

                                                    (has? (:functions n) ["Order Mgt"])
                                                    {:fill "orange"}
                                                      
                                                    :else {})})
                     :edge->attrs (fn [e] {:label (:data-type e)
                                           :style (cond
                                                    (= "client master" (:data-type e))
                                                    {:stroke-dash "3"
                                                     :animated "true"
                                                     :stroke "brown"}

                                                    :else {})})
                     :node->cluster :dept
                     :cluster->parent (fn [_] :bank)}
  :file-out path})


;; zoom in to one/ two departments. apply a tco heatmap

(def heatmap-colours
  ["'#fff6e6'" "'#fce7c6'" "'#f9d8a7'" "'#f6c889'" "'#f4b76c'" "'#f1a64f'" "'#ef9332'"
   "'#ed800c'" "'#b35e04'"])


(defn heatmap
  [lower-bound upper-bound n]
  (let [step (/ (- upper-bound lower-bound) 8)
        ranges (into [] (range lower-bound (+ step upper-bound) step))
        i (loop [idx 0]
            (if (> (get ranges idx) n)
              idx
              (recur (inc idx))))]
    (get heatmap-colours i)))


(def tco-map (partial heatmap 0 2000000))


(arch-diagram->d2
 (let [k :dept
       vs '("Equities")]
   {:data big-data
    :transform-fn (apply zoom k true vs)  
    :filters {:app-filter-fn identity
              :flow-filter-fn identity}
    :dictim-fn g/graph->dictim
    :dictim-fn-params {:node->key :id
                       :node->attrs (fn [n] {:label (str (:name n)
                                                         "\\ntco: "
                                                         (format "%,12d" (:tco n))
                                                         "\\nowner: "
                                                         (:owner n))
                                             :style (let [tco (:tco n)]
                                                      {:fill (tco-map tco)})})
                       :edge->attrs (fn [e] {:label (:data-type e)
                                             :style (cond
                                                      (= "orders" (:data-type e))
                                                      {:stroke-dash "5"
                                                       :animated "false"
                                                       :stroke "green"}

                                                      (= "trades" (:data-type e))
                                                      {:stroke-dash "5"
                                                       :animated "false"
                                                       :stroke "blue"}

                                                      (= "greeks" (:data-type e))
                                                      {:stroke-dash "5"
                                                       :animated "true"
                                                       :stroke "red"}

                                                      (= "allocations" (:data-type e))
                                                      {:stroke-dash "5"
                                                       :animated "true"
                                                       :stroke "brown"}
                                                      

                                                      :else {})})
                       :node->cluster k
                       :cluster->parent (fn [_] :bank)
                       :cluster->attrs (fn [c] (if (not (contains? (set vs) c))
                                                 {:style.fill "white"}
                                                 {}))}
    :file-out path}))


;; *********************************************************************************





(def small-data
  {:apps
   '({:id "app14147",
      :name "ARC3",
      :owner "Deepak",
      :dept "Securities",
      :functions ("Order Mgt" "Execution"),
      :tco 600000,
      :process "p112"}
     {:id "app14148",
      :name "Flame",
      :owner "Lakshmi",
      :dept "Finance",
      :functions ("Quoting" "Allocations"),
      :tco 0,
      :process "p113"}
     {:id "app14149",
      :name "Data Solar",
      :owner "Wendy",
      :dept "Funds",
      :functions ("Pricing" "Data Master"),
      :tco 700000,
      :process "p114"}
     {:id "app14150",
      :name "Flame minor",
      :owner "Lakshmi",
      :dept "Equities",
      :functions ("Order Mgt" "Pricing"),
      :tco 400000,
      :process "p114"}
     {:id "app14151",
      :name "Booking Flash",
      :owner "Darcie",
      :dept "Securities",
      :functions ("Execution" "Accounting"),
      :tco 1600000,
      :process "p112"}
     {:id "app14152",
      :name "Book Relay",
      :owner "Tomos",
      :dept "Finance",
      :functions ("Execution" "Accounting"),
      :tco 1500000,
      :process "p112"}
     {:id "app14153",
      :name "ARC3",
      :owner "India",
      :dept "Corporate",
      :functions ("Allocations" "Pricing"),
      :tco 1000000,
      :process "p114"}
     {:id "app14154",
      :name "Data Solar",
      :owner "Wendy",
      :dept "Securities",
      :functions ("Accounting" "Data Master"),
      :tco 1800000,
      :process "p113"}
     {:id "app14155",
      :name "Risk Sheet",
      :owner "Tomos",
      :dept "Risk",
      :functions ("Pricing" "Booking"),
      :tco 1600000,
      :process "p114"}
     {:id "app14156",
      :name "Data sky",
      :owner "Dale",
      :dept "Risk",
      :functions ("Position Keeping" "Position Keeping"),
      :tco 1600000,
      :process "p113"}),
   :flows
   '({:src "app14152", :dest "app14148", :data-type "positions"}
     {:src "app14155", :dest "app14153", :data-type "client master"}
     {:src "app14147", :dest "app14149", :data-type "trades"}
     {:src "app14154", :dest "app14149", :data-type "client master"}
     {:src "app14155", :dest "app14148", :data-type "client master"}
     {:src "app14153", :dest "app14151", :data-type "quotes"}
     {:src "app14156", :dest "app14152", :data-type "client master"}
     {:src "app14151", :dest "app14150", :data-type "trades"}
     {:src "app14155", :dest "app14147", :data-type "client master"}
     {:src "app14152", :dest "app14152", :data-type "instructions"}
     {:src "app14155", :dest "app14147", :data-type "client master"}
     {:src "app14150", :dest "app14152", :data-type "quotes"}
     {:src "app14154", :dest "app14150", :data-type "positions"}
     {:src "app14151", :dest "app14155", :data-type "quotes"}
     {:src "app14147", :dest "app14147", :data-type "allocations"})})


(def big-data
  {:apps
   '({:id "app14488",
      :name "Credit guard",
      :owner "India",
      :dept "Finance",
      :functions ("Quoting" "Pricing"),
      :tco 700000,
      :process "p114"}
     {:id "app14489",
      :name "Credit guard",
      :owner "Lakshmi",
      :dept "Securities",
      :functions ("Risk Mgt" "Accounting"),
      :tco 0,
      :process "p112"}
     {:id "app14490",
      :name "Seven heaven",
      :owner "Joesph",
      :dept "Risk",
      :functions ("Accounting" "Booking"),
      :tco 0,
      :process "p112"}
     {:id "app14491",
      :name "Risk Global",
      :owner "Wendy",
      :dept "Equities",
      :functions ("Pricing" "Order Mgt"),
      :tco 600000,
      :process "p114"}
     {:id "app14492",
      :name "Credit guard",
      :owner "Lakshmi",
      :dept "Equities",
      :functions ("Pricing" "Execution"),
      :tco 100000,
      :process "p112"}
     {:id "app14493",
      :name "Data sky",
      :owner "Joesph",
      :dept "Risk",
      :functions ("Risk Mgt" "Data Master"),
      :tco 1600000,
      :process "p114"}
     {:id "app14494",
      :name "Book Relay",
      :owner "Lakshmi",
      :dept "Securities",
      :functions ("Order Mgt" "Order Mgt"),
      :tco 1400000,
      :process "p112"}
     {:id "app14495",
      :name "Seven heaven",
      :owner "Lakshmi",
      :dept "Finance",
      :functions ("Order Mgt" "Accounting"),
      :tco 500000,
      :process "p113"}
     {:id "app14496",
      :name "Globe-X",
      :owner "Wendy",
      :dept "Corporate",
      :functions ("Quoting" "Booking"),
      :tco 900000,
      :process "p113"}
     {:id "app14497",
      :name "Risk Global",
      :owner "Joesph",
      :dept "Funds",
      :functions ("Execution" "Position Keeping"),
      :tco 500000,
      :process "p112"}
     {:id "app14498",
      :name "Risk Global",
      :owner "Wendy",
      :dept "Risk",
      :functions ("Risk Mgt" "Position Keeping"),
      :tco 700000,
      :process "p112"}
     {:id "app14499",
      :name "Trade propel",
      :owner "India",
      :dept "Finance",
      :functions ("Data Master" "Pricing"),
      :tco 400000,
      :process "p113"}
     {:id "app14500",
      :name "Acmaze",
      :owner "Tomos",
      :dept "Fixed Income",
      :functions ("Accounting" "Order Mgt"),
      :tco 100000,
      :process "p112"}
     {:id "app14501",
      :name "Acmaze",
      :owner "Tomos",
      :dept "Finance",
      :functions ("Risk Mgt" "Data Master"),
      :tco 400000,
      :process "p113"}
     {:id "app14502",
      :name "Ark Crypto",
      :owner "Wendy",
      :dept "Finance",
      :functions ("Risk Mgt" "Allocations"),
      :tco 1500000,
      :process "p113"}
     {:id "app14503",
      :name "Ark Crypto",
      :owner "India",
      :dept "Fixed Income",
      :functions ("Allocations" "Position Keeping"),
      :tco 1700000,
      :process "p114"}
     {:id "app14504",
      :name "Booking Flash",
      :owner "India",
      :dept "Risk",
      :functions ("Risk Mgt" "Accounting"),
      :tco 700000,
      :process "p114"}
     {:id "app14505",
      :name "Book good",
      :owner "Deepak",
      :dept "Securities",
      :functions ("Pricing" "Allocations"),
      :tco 300000,
      :process "p113"}
     {:id "app14506",
      :name "Data Source",
      :owner "Lakshmi",
      :dept "Corporate",
      :functions ("Booking" "Order Mgt"),
      :tco 100000,
      :process "p114"}
     {:id "app14507",
      :name "Crypto Bot",
      :owner "Wendy",
      :dept "Risk",
      :functions ("Allocations" "Allocations"),
      :tco 1100000,
      :process "p114"}
     {:id "app14508",
      :name "Flame minor",
      :owner "Lowri",
      :dept "Finance",
      :functions ("Booking" "Accounting"),
      :tco 600000,
      :process "p112"}
     {:id "app14509",
      :name "Commune New",
      :owner "Deepak",
      :dept "Finance",
      :functions ("Booking" "Accounting"),
      :tco 400000,
      :process "p112"}
     {:id "app14510",
      :name "Data Solar",
      :owner "Deepak",
      :dept "Finance",
      :functions ("Booking" "Pricing"),
      :tco 1200000,
      :process "p113"}
     {:id "app14511",
      :name "Data Solar",
      :owner "Joesph",
      :dept "Finance",
      :functions ("Position Keeping" "Pricing"),
      :tco 1200000,
      :process "p112"}
     {:id "app14512",
      :name "Data Source",
      :owner "Tomos",
      :dept "Risk",
      :functions ("Pricing" "Quoting"),
      :tco 1100000,
      :process "p112"}
     {:id "app14513",
      :name "Ark Crypto",
      :owner "Tomos",
      :dept "Finance",
      :functions ("Position Keeping" "Quoting"),
      :tco 1600000,
      :process "p112"}
     {:id "app14514",
      :name "Book Relay",
      :owner "Deepak",
      :dept "Corporate",
      :functions ("Risk Mgt" "Order Mgt"),
      :tco 1500000,
      :process "p114"}
     {:id "app14515",
      :name "Trade propel",
      :owner "Lowri",
      :dept "Securities",
      :functions ("Booking" "Allocations"),
      :tco 700000,
      :process "p114"}
     {:id "app14516",
      :name "Booking Flash",
      :owner "Lowri",
      :dept "Corporate",
      :functions ("Data Master" "Execution"),
      :tco 200000,
      :process "p113"}
     {:id "app14517",
      :name "Zone out",
      :owner "Wendy",
      :dept "Securities",
      :functions ("Booking" "Quoting"),
      :tco 500000,
      :process "p114"}
     {:id "app14518",
      :name "Trade Aid",
      :owner "Darcie",
      :dept "Finance",
      :functions ("Risk Mgt" "Risk Mgt"),
      :tco 1100000,
      :process "p113"}
     {:id "app14519",
      :name "Flame minor",
      :owner "Darcie",
      :dept "Finance",
      :functions ("Data Master" "Order Mgt"),
      :tco 1800000,
      :process "p113"}
     {:id "app14520",
      :name "Seven heaven",
      :owner "Tomos",
      :dept "Equities",
      :functions ("Order Mgt" "Execution"),
      :tco 100000,
      :process "p112"}
     {:id "app14521",
      :name "ARC3",
      :owner "Darcie",
      :dept "Funds",
      :functions ("Data Master" "Risk Mgt"),
      :tco 1000000,
      :process "p113"}
     {:id "app14522",
      :name "Apac Ace",
      :owner "Wendy",
      :dept "Equities",
      :functions ("Risk Mgt" "Quoting"),
      :tco 400000,
      :process "p112"}
     {:id "app14523",
      :name "ARC3",
      :owner "Wendy",
      :dept "Fixed Income",
      :functions ("Allocations" "Allocations"),
      :tco 600000,
      :process "p113"}
     {:id "app14524",
      :name "Risk Sheet",
      :owner "Lowri",
      :dept "Securities",
      :functions ("Execution" "Risk Mgt"),
      :tco 400000,
      :process "p114"}
     {:id "app14525",
      :name "Flame",
      :owner "Lakshmi",
      :dept "Risk",
      :functions ("Pricing" "Booking"),
      :tco 1100000,
      :process "p112"}
     {:id "app14526",
      :name "Commune New",
      :owner "Tomos",
      :dept "Funds",
      :functions ("Pricing" "Quoting"),
      :tco 1100000,
      :process "p114"}
     {:id "app14527",
      :name "Risk Global",
      :owner "Tomos",
      :dept "Equities",
      :functions ("Pricing" "Risk Mgt"),
      :tco 900000,
      :process "p112"}),
   :flows
   '({:src "app14522", :dest "app14500", :data-type "security reference"}
     {:src "app14526", :dest "app14489", :data-type "client master"}
     {:src "app14496", :dest "app14501", :data-type "positions"}
     {:src "app14516", :dest "app14505", :data-type "client master"}
     {:src "app14517", :dest "app14492", :data-type "trades"}
     {:src "app14518", :dest "app14496", :data-type "orders"}
     {:src "app14525", :dest "app14495", :data-type "greeks"}
     {:src "app14521", :dest "app14500", :data-type "trades"}
     {:src "app14511", :dest "app14490", :data-type "orders"}
     {:src "app14523", :dest "app14508", :data-type "positions"}
     {:src "app14527", :dest "app14507", :data-type "allocations"}
     {:src "app14488", :dest "app14506", :data-type "greeks"}
     {:src "app14508", :dest "app14504", :data-type "positions"}
     {:src "app14519", :dest "app14516", :data-type "positions"}
     {:src "app14494", :dest "app14495", :data-type "payments"}
     {:src "app14527", :dest "app14502", :data-type "greeks"}
     {:src "app14522", :dest "app14510", :data-type "orders"}
     {:src "app14519", :dest "app14505", :data-type "quotes"}
     {:src "app14514", :dest "app14495", :data-type "orders"}
     {:src "app14498", :dest "app14512", :data-type "payments"}
     {:src "app14506", :dest "app14499", :data-type "orders"}
     {:src "app14502", :dest "app14516", :data-type "trades"}
     {:src "app14514", :dest "app14507", :data-type "trades"}
     {:src "app14511", :dest "app14503", :data-type "payments"}
     {:src "app14503", :dest "app14514", :data-type "instructions"}
     {:src "app14515", :dest "app14523", :data-type "orders"}
     {:src "app14500", :dest "app14495", :data-type "greeks"}
     {:src "app14508", :dest "app14525", :data-type "orders"}
     {:src "app14507", :dest "app14493", :data-type "greeks"}
     {:src "app14509", :dest "app14498", :data-type "quotes"}
     {:src "app14503", :dest "app14505", :data-type "client master"}
     {:src "app14513", :dest "app14488", :data-type "client master"}
     {:src "app14527", :dest "app14522", :data-type "orders"}
     {:src "app14494", :dest "app14513", :data-type "instructions"}
     {:src "app14488", :dest "app14527", :data-type "greeks"}
     {:src "app14500", :dest "app14526", :data-type "security reference"}
     {:src "app14504", :dest "app14496", :data-type "allocations"}
     {:src "app14488", :dest "app14508", :data-type "trades"}
     {:src "app14519", :dest "app14512", :data-type "trades"}
     {:src "app14523", :dest "app14491", :data-type "orders"}
     {:src "app14510", :dest "app14494", :data-type "client master"}
     {:src "app14527", :dest "app14506", :data-type "trades"}
     {:src "app14497", :dest "app14520", :data-type "allocations"}
     {:src "app14516", :dest "app14504", :data-type "quotes"}
     {:src "app14491", :dest "app14527", :data-type "greeks"}
     {:src "app14495", :dest "app14498", :data-type "quotes"}
     {:src "app14494", :dest "app14521", :data-type "orders"}
     {:src "app14502", :dest "app14516", :data-type "greeks"}
     {:src "app14490", :dest "app14527", :data-type "client master"}
     {:src "app14523", :dest "app14509", :data-type "greeks"})})
