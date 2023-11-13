(ns user.tutorial-lifecycle
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]))

(e/defn BlinkerComponent []
  (e/client
    (dom/h1 (dom/text "blink!"))
    (println 'component-did-mount)
    (e/on-unmount #(println 'component-will-unmount))))

(e/defn Lifecycle []
  (e/client
    (if (= 0 (int (mod e/system-time-secs 2)))
      (BlinkerComponent.))))
