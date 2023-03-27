(ns dictim.cookbook.data.generate)


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

