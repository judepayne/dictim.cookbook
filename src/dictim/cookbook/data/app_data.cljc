(ns dictim.cookbook.data.app-data)


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
      :process "p114"
      :tooltip "This application .. etc etc"
      :link "https://en.wikipedia.org/wiki/Flame"}
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
      :process "p113"
      :tooltip "This application blah blah .."}
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
