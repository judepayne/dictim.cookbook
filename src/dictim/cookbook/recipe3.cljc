(ns dictim.cookbook.recipe3
  (:require [dictim.graph.core  :as g]
            [dictim.dot.compile  :as c]
            [clojure.string     :as str]
            [clojure.java.shell :as sh]
            [dictim.cookbook.data.app-data :refer [small-data big-data]]
            [dictim.cookbook.data.generate :refer [apps flows]]))


;; ****************************************************************
;; *                        Recipe 3                              *
;; *                    Building dictim                           *
;; *    Producing architectural diagrams with Graphviz dot        *
;; ****************************************************************


;; Same use case as recipe 2. We're just gonna output to dot format instead




;; For that diagram spec, let's build a function to handle it

(def path-to-dot "/opt/homebrew/bin/dot")


(defn- format-error [s err]
  (apply str
    err "\n"
    (interleave
      (map
        (fn [idx s]
          (format "%3d: %s" idx s))
        (range)
        (str/split-lines s))
      (repeat "\n"))))


(defn dot->svg
  "Takes a string containing a dot specification of a graph, and returns a string containing SVG."
  [s & {:keys [path] :or {path path-to-dot}}]
  (let [s' (str/replace s "\\\\n" "\n") ;; for multi-line labels
        {:keys [out err]} (sh/sh path "-Tsvg" :in s')]
    (or
     out
     (throw (IllegalArgumentException. ^String (str "Graphviz!: "(format-error s' err)))))))


(defn template->dot
  ([spec] (template->dot spec :apps :flows))
  ([spec node-key edge-key]
   (let [transform-fn (or (-> spec :transform-fn) identity)
         data (transform-fn (-> spec :data))]
     (let [nodes (->> (-> data node-key)
                      (filter (or (-> spec :filters :app-filter-fn) identity)))
           edges (concat (->> (-> data edge-key)
                             (filter (or (-> spec :filters :flow-filter-fn) identity)))
                         (map #(assoc % :scaffold true) (apply concat (repeat 5 (-> spec :scaffold)))))
           directives (-> spec :directives)
           dictim ((-> spec :dictim-fn) nodes edges (-> spec :dictim-fn-params))
           dictim' (if directives (cons directives dictim) dictim)
           dot (apply c/dot dictim')
           svg (dot->svg dot)]
       (spit "samples/in.dot" dot)  ; for debug, delete.
       (spit (-> spec :file-out) (dot->svg dot))))))


;; let's define a path to write the .d2 files out to:
(def path "samples/out-dot.svg")


;; example usage
(defn- has? [coll xs]
  (first (filter #(contains? (set xs) %) coll)))


(def data (atom {}))


;; *********************************************************************************
;; look at small data


(template->dot
 {:data small-data
  :filters {:app-filter-fn identity
            :flow-filter-fn identity}
  :dictim-fn g/graph->dictim
  :dictim-fn-params {:node->key :id
                     :node->attrs (fn [n] {:label (str (:name n))})
                     :edge->attrs (fn [e] {:label (:data-type e)})
                     :node->cluster :dept
                     :cluster->parent (fn [_] "bank")
                     :cluster->attrs (fn [c] {:label c})
                     :qualify? false}
  :file-out path})


;; highlight apps that participate in various functions..
;; pink = Pricing, green = Quoting, oragne = Order Mgt

(let [apps (take 15 (apps))
      flows (take 15 (flows apps))
      random-data {:apps apps
            :flows flows}
      data small-data
      depts (into #{} (map :dept (:apps data)))]
  (template->dot
   {:data data
    :filters {:app-filter-fn identity
              :flow-filter-fn identity}
    :dictim-fn g/graph->dictim
    :dictim-fn-params {:node->key :id
                       :node->attrs (fn [n] (merge {:label (str (:name n))
                                                    :shape "rectangle"
                                                    :fontname "trebuchet MS" :fontsize 8 :color "#384896"
                                                    :penwidth 0.7}
                                                   #_(cond
                                                       (has? (:functions n) ["Pricing"])
                                                       {:fillcolor "pink" :style "filled"}

                                                       (has? (:functions n) ["Quoting"])
                                                       {:fillcolor "green" :style "filled" :fontcolor "#ffffff"}

                                                       (has? (:functions n) ["Order Mgt"])
                                                       {:fillcolor "orange" :style "filled"}
                                                       
                                                       :else {:fillcolor "#f5f7fc" :style "filled"})))
                       :edge->attrs (fn [e] (merge (when (not (:scaffold e))
                                                     (merge
                                                      {:xlabel (:data-type e) :fontsize 6 :color "#384896"
                                                       :fontname "trebuchet MS italic" :fontcolor "#656565"
                                                       :arrowsize 0.5 :penwidth 0.6 :constraint true}
                                                      #_(when (= "quotes" (:data-type e))
                                                          {:color "blue" :style "dashed" :penwidth 2})))
                                                   (when (:scaffold e)
                                                     {:color "#222222" :penwidth 2 :weight 10
                                                      :style "invis"
                                                      })))
                       :node->cluster :dept
                       :cluster->parent (fn [_] "bank")
                       :cluster->attrs (fn [c] (merge
                                                {:label c :style "filled" :color "#384896" :penwidth 0.7}
                                                (if (not= c "bank")
                                                  {:fillcolor "#ebf0fc"}
                                                  {:fillcolor "#e3eafc"})))
                       :qualify? false}
    :directives {:splines "ortho" :fontname "trebuchet MS" :fontsize 8 :nodesep 0.8 :ranksep 0.9
                 :concentrate true :rankdir "TB"}
    :scaffold (filter
               (fn [scaff]
                 (and (contains? depts (:src scaff)) (contains? depts (:dest scaff))))
               '({:src "Securities" :dest "Risk"} {:src "Securities" :dest "Funds"}
                 {:src "Securities" :dest "Equities"}
                 {:src "Fixed Income" :dest "Risk"} {:src "Fixed Income" :dest "Funds"}
                 {:src "Fixed Income" :dest "Equities"}
                 {:src "Risk" :dest "Finance"}
                 {:src "Funds" :dest "Finance"}{:src "Equities" :dest "Finance"}
                 {:src "Finance" :dest "Corporate"}))
    :file-out path}))
