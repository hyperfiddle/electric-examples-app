(ns hyperfiddle.ui.virtual-scroll
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.electric-ui4 :as ui]
            [missionary.core :as m]
            #?(:cljs goog.style))
  (:import [hyperfiddle.electric Pending]))

#?(:cljs
   (defn resize-observer [node]
     (->> (m/observe (fn [!]
                       (! [(.-clientWidth node) (.-clientHeight node)])
                       (let [obs (new js/ResizeObserver (fn [entries]
                                                          (let [content-box-size (-> entries (aget 0) .-contentBoxSize (aget 0))]
                                                            (! [(.-inlineSize content-box-size) (.-blockSize content-box-size)]))))]
                         (.observe obs node)
                         #(.unobserve obs))))
       (m/relieve {}))))

(e/defn* ResizeObserver [node]
  (new (resize-observer node)))

;; ---

(defn slice [coll start width]
  (if (vector? coll)
    (subvec coll (min (max 0 start) (count coll)) (min (+ start width) (count coll))) ; O(1)
    (take width (drop start coll))                                 ; O(n)
    ))

(defn window [row-height padding-top rows-count scrollTop clientHeight]
  (let [scrollable-height  (+ (* rows-count row-height) padding-top)
        first-row-index    (max 0 (Math/floor (/ scrollTop row-height)))
        mounted-rows-count (max 0 (min (- rows-count first-row-index) (Math/ceil (/ (- clientHeight padding-top) row-height))))]
    [scrollable-height first-row-index mounted-rows-count]))

(e/def rows-count 0)
(e/def row-height 30)
(e/def first-row-index 0)
(e/def mounted-rows-count 0)
(e/def !scroll-watchers nil) ; (atom #{})

(e/defn* RegisterScrollWatch [callback]
  (when (some? !scroll-watchers)
    (swap! !scroll-watchers conj callback)
    (e/on-unmount #(swap! !scroll-watchers disj callback))))

(defn notify-scroll-watches [scroll-watchers]
  (doseq [watcher scroll-watchers]
    (watcher)))

(defn scrollbar-width []  #?(:clj 0, :cljs (goog.style/getScrollbarWidth)))

(e/def scrollbar-thinkness (scrollbar-width))

(defmacro virtual-scroll
  "
TODO doc
- `rows-count`  0
- `row-height`  30 (px)
- `padding-top` 0  (px)
"
  [{::keys [rows-count row-height padding-top] :or {padding-top 0}} & body]
  `(e/client
     (binding [rows-count (or ~rows-count 0)
               row-height (or ~row-height 30)
               !scroll-watchers (atom #{})]
       (dom/div (dom/props {:class "virtual-scroll"})
         (dom/on! "scroll" (partial notify-scroll-watches (e/watch !scroll-watchers)))
         (let [[scrollTop# ~'_ ~'_] (new (ui/scroll-state< dom/node))
               clientHeight#        (second (new ResizeObserver dom/node))
               [scrollable-height# first-row-index# mounted-rows-count#]
               (window row-height (or ~padding-top 0) rows-count (js/Math.floor scrollTop#) clientHeight#)
               scrollbar-padding# (if (inc (> scrollable-height# clientHeight#)) scrollbar-thinkness 0)]
           (dom/props {:style {:height                      (str (+ scrollable-height# scrollbar-padding#) "px")
                               :--virtual-scroll-row-height (str row-height "px")}})
           (dom/div (dom/props {:style {:position :absolute,:min-width "1px", :height (str scrollable-height# "px") :visibility :hidden}
                                :aria-hidden true}))
           (binding [first-row-index    first-row-index#
                     mounted-rows-count mounted-rows-count#]
             ~@body))))))

(e/def index 0)
(e/def row-number 0)

(e/defn* ServerSidePagination "Called on server" [rows RenderRow]
  (e/client
    (let [first-row-index    first-row-index ; binding unification hack
          mounted-rows-count mounted-rows-count]
      (try
        (e/server
          (let [rows-index (zipmap (range) (slice rows first-row-index mounted-rows-count))]
            (e/for [idx (sort (keys rows-index))]
              (binding [index      idx
                        row-number (+ first-row-index idx)]
                (RenderRow. (get rows-index idx))))))
        (catch Pending _) ; Ensures smooth scrolling - otherwise rows’s content will mount/unmount on scroll and slow the scroll behavior down
        ))))

(e/defn* ClientSidePagination "Called on client" [rows RenderRow]
  (e/client
    (let [rows-index (zipmap (range) (slice rows first-row-index mounted-rows-count))]
      (e/for [idx (sort (keys rows-index))]
        (binding [index      idx
                  row-number (+ first-row-index idx)]
          (RenderRow. (get rows-index idx)))))))

