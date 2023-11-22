(ns hyperfiddle.ui.disclosure
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]))

(e/def open?)
(e/def !open?)
(e/def panel-id)
(e/def preload?)
(e/def keep-loaded?)
(e/def !loaded?)
(e/def loaded?)
(e/def disabled?)

(defmacro disclosure [props & body]
  `(let [props# ~props]
     (binding [!open?       (atom (::open? props# false))
               preload?     (::preload? props# false)
               keep-loaded? (::keep-loaded? props# false)
               disabled?    (::disabled? props# false)
               panel-id     (str (gensym "disclosure-panel-"))
               !loaded?     (atom false)]
       (binding [open?   (e/watch !open?)
                 loaded? (e/watch !loaded?)]
         (dom/div (dom/props {:class "disclosure"})
                  ~@body)))))

(defmacro button [& body]
  `(dom/span (dom/props {:class         "disclosure-button"
                         :aria-expanded open?
                         :aria-controls panel-id})
     (when-not disabled?
       (dom/props {:role "button"})
       (dom/on! "click" #(swap! !open? not)))
     ~@body))

(defmacro panel [{::keys [open?]} & body]
  (let [body `(dom/div (dom/props {:class "disclosure-panel"
                                   :id panel-id})
                       (when (not open?)
                         (dom/props {:class "soft-display-none"}))
                       ~@body)]
    (if (and open? (not disabled?)) ; hardcoded
      body
      `(when (or open? preload? loaded?)
         (when keep-loaded? (reset! !loaded? true))
         ~body))))
