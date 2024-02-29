(ns dictim.cookbook.recipe2
  (:require [dictim.graph.core :as g]
            [dictim.d2.compile :as c]
            [dictim.cookbook.data.app-data :refer [small-data big-data]]
            [dictim.cookbook.data.generate :refer [apps flows]]))


;; ****************************************************************
;; *                        Recipe 2                              *
;; *                    Building dictim                           *
;; *            Producing architectural diagrams                  *
;; ****************************************************************

;; Let's pick a use case of generating architectural diagrams in a large,
;; complex organization that has lots of different applications and data
;; flows between them.


;; see the dictim.cookbook.data.generate namespace for sample data generation.


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

(defn template->d2
  ([spec] (template->d2 spec :apps :flows))
  ([spec node-key edge-key]
   (let [transform-fn (or (-> spec :transform-fn) identity)
         data (transform-fn (-> spec :data))]
     (let [nodes (->> (-> data node-key)
                      (filter (or (-> spec :filters :app-filter-fn) identity)))
           edges (->> (-> data edge-key)
                      (filter (or (-> spec :filters :flow-filter-fn) identity)))
           directives (-> spec :directives)
           dictim ((-> spec :dictim-fn) nodes edges (-> spec :dictim-fn-params))
           dictim' (if directives (cons directives dictim) dictim)
           d2 (apply c/d2 dictim')]
       (println "\n")
       (clojure.pprint/pprint (clojure.walk/prewalk
                               (fn [form] (if (string? form)
                                            form #_(str "\"" form "\"")
                                            form))
                               dictim))
       (println "\n")
       (println d2)
       (spit (-> spec :file-out) d2)))))


;; let's define a path to write the .d2 files out to:
(def path "samples/in.d2")

;; You should have the d2 executable installed: https://github.com/terrastruct/d2#install
;; and running watching the file you've defined above. e.g. In a terminal:
;; d2 -w -l tala in.d2 out.svg
;; I like the 'tala' layout engine for most purposes.


;; example usage
(defn- has? [coll xs]
  (first (filter #(contains? (set xs) %) coll)))


(def data (atom {}))

;; Show show example apps in the repl and some flows
(def my-apps (take 2 (apps)))


;; Generate some random diagrams, over and over.
(let [apps (take 11 (apps))
      flows (take 10 (flows apps))]
  (reset! data {:apps apps :flows flows})
  (template->d2
   {:data {:apps apps
           :flows flows }
    :filters {:filter-fn identity
              :app-filter-fn identity
              :flow-filter-fn identity}
    :dictim-fn g/graph->dictim
    :dictim-fn-params {:node->key (fn [n] (str (:id n)))
                       :node->attrs (fn [n] {:label (str (:name n))})
                       :edge->attrs (fn [e] {:label (:data-type e)})
                       :node->cluster :dept
                       :cluster->parent (fn [_] :bank)}
    :file-out path}))


;; *********************************************************************************
;; look at small data


(template->d2
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

(template->d2
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

(template->d2
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
                                                     :stroke "red"
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
(template->d2
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
(template->d2
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
                                                     :animated "true"
                                                     :stroke "blue"}

                                                    :else {})})
                     :node->cluster :dept
                     :cluster->parent (fn [_] :bank)}
  :file-out path})


;; same as before but group differently and fmt the edge apps differently
;; ceam diagrams
(defn process-diagram
  [data transform-fn]
  (template->d2
   (merge
    {:data data
     :transform-fn transform-fn}
    {:dictim-fn g/graph->dictim
     :dictim-fn-params {:node->key :id
                        :node->attrs (fn [n] (cond
                                               (contains? #{:ins :outs :inouts} (:relation n))
                                               (merge
                                                {:label (str (:name n))
                                                 :shape "circle"
                                                 :width "60"
                                                 :height "60"})

                                               :else
                                               (merge
                                                {:label (str (:name n))
                                                 :shape "rectangle"
                                                 :style.fill "'#dbbd95'"
                                                 :style.stroke "'#9e7641'"
                                                 :style.shadow "true"}
                                                (when (:tooltip n) {:tooltip (:tooltip n)})
                                                (when (:link n) {:link (:link n)}))))
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
     :file-out path})))


(process-diagram small-data (zoom :dept true "Finance" "Equities" "Risk"))


(process-diagram small-data (zoom :dept true "Finance" "Equities" "Corporate"))


(process-diagram big-data (zoom :dept true "Corporate"))


(process-diagram big-data (zoom :dept true "Finance"))





;; Let's 'group' the same data a different way -> by business process

(template->d2
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
                                           :style.stroke "'#4eb8ed'"
                                           :style.stroke-width "4"
                                           :style.font-size "16"
                                           :style.font-color "red"})
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


(template->d2
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


(template->d2
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


(template->d2
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
                                                      {:stroke-dash "10"
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


(template->d2
 {:data {
         :edges [["current state" "Golden Sources"]
                 ["current state" "Lifecycle"]
                 ["current state" "Capture"]
                 ["current state" "Risk"]
                 ["current state" "Settlement"]
                 ["current state" "Random2"]
                 ["Random2" "Random1"]
                 ["Random1" "Reporting"]]}
  :dictim-fn g/graph->dictim
  :file-out path
  :directives {:direction "right"}}
 :nodes
 :edges)


;; *********************************************************************************


(def use-cases
  {:nodes
   '({:case :a :id :db_a :name "Metadata DBS"}
     {:case :a :id :mt_a :name "Middle tier"}
     {:case :a :id :tp_a :name "transpiler"}
     {:case :a :id :gui_a :name "browser"}
     {:case :a :id :user_a :name "User"}
     
     {:case :b :id :db_b :name "Metadata DB"}
     {:case :b :id :mt_b :name "Middle tier"}
     {:case :b :id :tp_b :name "transpiler"}
     {:case :b :id :gui_b :name "Terrastruct gui"}
     {:case :b :id :user_b :name "User"})
   :edges
   '({:src :db_a :dest :mt_a :label "query data"}
     {:src :mt_a :dest :tp_a :label "data"}
     {:src :tp_a :dest :gui_a :label "d2"}
     {:src :gui_a :dest :user_a :label "view diagram"}
     
     {:src :db_b :dest :mt_b :label "query data"}
     {:src :mt_b :dest :tp_b :label "data"}
     {:src :tp_b :dest :gui_b :label "d2"}
     {:src :gui_b :dest :user_b :label "view & edit diagram"}
     {:src :user_b :dest :gui_b :label "update d2"}
     {:src :gui_b :dest :tp_b :label "updated d2"}
     {:src :tp_b :dest :mt_b :label "updated data"}
     {:src :mt_b :dest :db_b :label "store updated data"})})


;; example diagram - the proposed flow
(template->d2
 {:data use-cases
  :directives {:direction "right"}
  :dictim-fn g/graph->dictim
  :dictim-fn-params {:node->key :id
                     :node->attrs (fn [n] {:label (:name n)
                                           :shape (case (:id n)
                                                    :user_a "person"
                                                    :user_b "person"
                                                    "rectangle")})
                     :edge->attrs (fn [e] {:label (:label e)})
                     :node->cluster (fn [n] (:case n))
                     :cluster->attrs (fn [c] (case c
                                               :a {:label "Dynamic View"}
                                               :b {:label "View and edit"}))}
  :file-out path}
 :nodes
 :edges)
