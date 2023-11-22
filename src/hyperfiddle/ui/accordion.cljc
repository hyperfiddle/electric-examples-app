(ns hyperfiddle.ui.accordion
  (:require
   [heroicons.electric.v24.outline :as i]
   [hyperfiddle.electric-dom2 :as dom]
   [hyperfiddle.ui.disclosure :as dis]
   [clojure.set :as set]
   [hyperfiddle.electric :as e]))

(defmacro accordion [& body]
  `(dom/div (dom/props {:class ["accordion divide-y divide-gray-100"
                                "w-full bg-white border rounded-md px-2"]})
     ~@body))

(e/def disabled? false)

(defmacro entry [props & body]
  `(let [props# ~props]
     (dis/disclosure (set/rename-keys props# {::open? ::dis/open?, ::preload? ::dis/preload?, ::keep-loaded? ::dis/keep-loaded?
                                              ::disabled? ::dis/disabled?}) ; forward props
       (dom/props {:class "py-2 break-inside-avoid"})
       (binding [disabled? (::disabled? props# false)]
         ~@body))))

(defmacro header [& body]
  `(dis/button (dom/props {:style {:display :flex, :flex-gap "0.5rem", :align-items :center, :width "100%"}})
     (when-not disabled?
       (if dis/open?
         (i/chevron-down (dom/props {:style {:width "2rem"} :class "print:hidden"}))
         (i/chevron-right (dom/props {:style {:width "2rem"} :class "print:hidden"}))))
     ~@body))

(defmacro body [& body]
  `(dis/panel {} (dom/props {:class "p-2"})
     ~@body))


(comment

  (accordion
    (entry {::open? true, ::keep-loaded? true}
      (header (dom/span (dom/text "Section 1")))
      (body (dom/p (dom/text "Content"))))
    (entry {::open? false, ::keep-loaded? true}
      (header (dom/span (dom/text "Section 2")))
      (body (dom/p (dom/text "Content")))))

  )
