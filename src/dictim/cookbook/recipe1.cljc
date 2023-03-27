(ns dictim.cookbook.recipe1
  (:require [dictim.utils :refer [elem-key ctr? shape? cmt? conn? attrs?]]))

;; ****************************************************************
;; *                        Recipe 1                              *
;; *                  Manipulating dictim                         *
;; *        inserting, deleting, updating, filtering              *
;; ****************************************************************

;; Not included in the dicitm library as just standard Clojure
;; tree manipulation functions, just here tuned to the dictim syntax.
;; Many other tree manipulation approaches would also work!

;; ****************************************************************
;; 1. Inserting an element

;; Some test dictim to play around with.

(def dict
  [{:direction :right}
   [:dogs "All Dogs"
    [:toy-dogs "Handbag rats"
     [:myt "Miniature Yorkshire Terrier"]
     [:lhc "Long haired Chihuahua" {:style {:fill "red"}}]
     [:myt "->" :lhc "Equally terrible"]]
    [:large-dogs "Large breeds"
     [:gshep "German Shepherd"]]]
   {:near :gshep}])

;; insert an element

(defn ctr-split
  "splits a container into [(non-contained) (contained)]"
  [c]
  (split-with (complement vector?) c))


;; insertion, deletion, updating, filtering all need to traverse the tree
;; (of dictim elements). We need a traversal function..

(defn down [f e]
  (if (not (ctr? e))
    e
    (let [[c es] (ctr-split e)] (into (vec c) (f es)))))



;; works over every element in the tree checking for a match.

(defn insert-elem
  "Inserts elem into dictim :before/:after element identified by
  idfn."
  [elem placement pred elems]
  (letfn [(insert-fn [elems]
            (reduce
             (fn [acc cur]
               (if (pred cur)
                 (case placement
                   :before  (conj acc elem (down insert-fn cur))
                   :after   (conj acc (down insert-fn cur) elem))
                 (conj acc (down insert-fn cur))))
             []
             elems))]
    (insert-fn elems)))


;; use it. e.g.

(insert-elem [:mpood "Miniature Poodle"] :before #(= :myt (elem-key %)) dict)


(insert-elem
 [:working-dogs "Working Breeds"
  [:wcs "Working Cocker Spaniel"]]
 :after #(= "Handbag rats" (second %)) dict)



;; ****************************************************************
;; 2. remove elements

(defn remove-elems
  "Removes any element satisfying pred."
  [pred elems]
  (letfn [(remove-fn [elems]
            (reduce
             (fn [acc cur]
               (if (pred cur)
                 acc
                 (conj acc (down remove-fn cur))))
             []
             elems))]
    (remove-fn elems)))


;; use it. e.g.

(remove-elems #(= :lhc (first %)) dict)

(remove-elems attrs? dict)



;; ****************************************************************
;; 3. update elements

(defn update-elems
  "Updates any elements satisfying pred."
  [elems pred f & args]
  (letfn [(update-fn [elems]
            (reduce
             (fn [acc cur]
               (if (pred cur)
                 (conj acc (apply f cur args))
                 (conj acc (down update-fn cur))))
             []
             elems))]
    (update-fn elems)))


;; use it. e.g.

(update-elems dict attrs? assoc :a 1)



;; ****************************************************************
;; 4. filtering elements


(defn filter-elems
  "Filters to just those elements satisfying pred."
  [pred elems]
  (filterv
   pred
   (rest
    (tree-seq ctr?
              (partial filter #(or (map? %) (vector? %)))
              elems))))


;; use it. e.g.

(filter-elems attrs? dict)

(filter-elems #(or (ctr? %) (attrs? %)) dict)
