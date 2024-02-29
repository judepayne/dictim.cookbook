(ns dictim.cookbook.recipe4
  (:require [dictim.graph.core :as g]
            [dictim.d2.compile :as c]))


(def path "samples/in.d2")


(def ex-sequence
  [:various
   "demonstrate various things"
   [:block
    "Block Text - e.g. Markdown"
    [:a "|md ## A header |"]
    [:b "|md text in a box |" {:shape :rectangle}]
    [:c "|md *some italic text* |"]
    [:a "--" :b "->" :c]]
   [:class
    "Class diagram"
    {"shape" "class"}
    ["\\#reader" "io.RuneReader"]
    ["method(a unint64)" "(x, y, int)"]
    ["-lookahead" "'[]rune'"]] ;; <-- best way to escape strings for d2
   ["convs"
    "Office Conversations"
    ["conv1"
     "Office conversation 1"
     {"shape" "sequence_diagram"}
     [:comment "This is a comment"]
     [:list "bob" "alice"]
     ["alice" "Alice" {"shape" "person", "style" {"fill" "orange"}}]
     ["bob.\"In the eyes of my (dog), I'm a man.\""]
     ["awkward small talk"
      ["alice" "->" "bob" "um, hi"]
      ["bob" "->" "alice" "oh, hello"]
      ["icebreaker attempt"
       ["alice" "->" "bob" "what did you have for lunch?"]]
      ["fail"
       {"style" {"fill" "green"}}
       ["bob" "->" "alice" "that's personal"]]]]
    ["conv2"
     "Office conversation 2"
     {"shape" "sequence_diagram"}
     [:list "simon" "trev"]
     ["simon" "Simon" {"shape" "person"}]
     ["trev" "Trevor"]
     ["failed conversation"
      ["simon" "->" "trev" "seen the football"]
      ["trev" "->" "simon" "no, I was at my gran's"]
      ["Carry on anyway"
       ["simon" "->" "trev" "mate, you missed a classic"]]]]
    ["conv1" "->" "conv2" "spot the difference?"]]])


(spit path (c/d2 ex-sequence))
