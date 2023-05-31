(ns olorm.fagsamling.ny
  (:require [olorm.serve :as s]))

::teodor :samme-som :olorm.fagsamling.ny/teodor

::s/hei


[:teodor (keyword "teodor")]

['teodor (symbol "teodor")]

(quote
 (+ 1 2 3))

(for [x (range 0 10)]
  (* 10 x))

(defmacro for2 [& args]
  `(for ~@args))

(macroexpand-1 '(for2 [x (range 0 10)] (* 10 x)))

[1 2 3]
[+ 1 2 3]

(list 1 2 3)
'(1 2)

"teodor"

:teodor

::teodor

(let [preference
      {::teodor :clojure
       ::oddmund :elixir}]
  (::teodor preference))

;; preference.teodor
