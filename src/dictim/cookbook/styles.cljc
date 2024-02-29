(ns dictim.cookbook.styles)


;; how labels and styles should be specified

(def comparators
  {:equals =
   :not-equals not=
   :contains some
   :doesnt-contain (complement some)
   :> >
   :< <
   :<= <=
   :>= >=})

;; *** Selectors ***
;; each selector is a vector of 3 elements
;; element 1 is a comparator in keyword form.
;; element 2 is the key used to extract the value to be compared from the node or edge map.
;; element 3 is the value to be compared against

;; example selectors..
[:contains :business-functions "booking"]
[:> "tco" 1200000]

;; Multiple Selectors
;; selectors can be chained together with either an 'and' operator or an 'or' like so
[:and [:contains :business-functions "booking"] [:> "tco" 1200000]]


(defn contains-vectors?
  "Returns true if coll contains one or more vectors."
  [coll]
  (some vector? coll))


(defn selector->fn
  "Takes a selector and returns a predicate function that matches the selector
   against its argument."
  [[comparator k v]]
  (fn [item]
    (let [v-found (k item)]
      ((comparator comparators) v-found v))))


(defn selectors->pred
  "Takes a single selector or a vector of selectors (whose first element must be
   either ':and' or ':or' and returns a predicate that takes the item and returns
   either true or false depending on whether the selector/s are satisfied."
  [selectors]
  (if (contains-vectors? selectors) ;; multiple selectors
    (let [selector-fns (map selector->fn (rest selectors))]
      (fn [item]
        (if (= :or (first selectors))
          (true? (some (fn [f] (f item)) selector-fns))
          (every? (fn [f] (f item)) selector-fns))))
    (selector->fn selectors)))


;; Defining how labels and determined - an example...
{:labels
 
 [[:node
   ;; a node label spec. May be either a 2 element or 3 element vector
   ;; element 1 should be either :node or :edge
   ;; element 2 is the key used to extract the label from the node map. e.g.
   :application-name
   ;; optional element 3 is a single selector or vector of selectors used to winnow
   ;; down nodes that this node label spec should apply to.
   [:and [:contains :business-functions "booking"] [:> "tco" 1200000]]
   ]
  [:node
   ;; another node label spec
   ;; element1, element2 etc
   ]
  [:edge
   ;; an edge label spec
   ]]
 
 :styles
 [[:node
   ;; a node style spec. May be either a 2 element or 3 element vector
   ;; element 1 should be either :node or :edge
   ;; element 2 is the style itself specified as a map. e.g.
   {:color "red"}
   ;; optional element 3 is a single selector or vector of selectors used to winnow
   ;; down nodes that this node label spec should apply to.
   [:or [:contains :business-functions "booking"] [:> "tco" 1200000]]   
   ]]}



