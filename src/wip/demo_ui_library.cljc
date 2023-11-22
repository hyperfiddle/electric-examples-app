(ns wip.demo-ui-library
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]
            [hyperfiddle.ui.accordion :as acc]
            [hyperfiddle.ui.disclosure :as dis]
            [heroicons.electric.v24.outline :as i]))

;; ⏳ ✅ ⬜

(comment
  (macroexpand-1 '(i/chevron-down (dom/props {:class "w-4 print:hidden"})))
  )

(e/defn UILibrary []
  (e/client
    (acc/accordion                      ; TODO {:as dom/ul}
      (acc/entry {}                     ; TODO {:as dom/li}
        (acc/header (dom/h2 (dom/text "⏳ Disclosure")))
        (acc/body
          (dis/disclosure {}
            (dis/button (dom/text "Click me"))
            (dis/panel {}
              (dom/text "Disclosed content")))))
      (acc/entry {}
        (acc/header (dom/h2 (dom/text "⏳ Accordion")))
        (acc/body
          (acc/accordion
            (acc/entry {}
              (acc/header (dom/p (dom/text "First entry")))
              (acc/body (dom/p (dom/text "First content"))))
            (acc/entry {}
              (acc/header (dom/p (dom/text "Second entry")))
              (acc/body (dom/p (dom/text "Second content")))))))
      (acc/entry {}
        (acc/header (dom/h2 (dom/text "⬜ Tabs"))))

      (acc/entry {}
        (acc/header (dom/h2 (dom/text "⬜ Combobox"))))
      (acc/entry {}
        (acc/header (dom/h2 (dom/text "⬜ Picker"))))

      (acc/entry {}
        (acc/header (dom/h2 (dom/text "⬜ Stage"))))
      (acc/entry {}
        (acc/header (dom/h2 (dom/text "⬜ Dialog"))))
      (acc/entry {}
        (acc/header (dom/h2 (dom/text "⬜ Virtual Scroll"))))
      (acc/entry {}
        (acc/header (dom/h2 (dom/text "⬜ Datagrid"))))
      (acc/entry {}
        (acc/header (dom/h2 (dom/text "⬜ Editable Grid"))))

      (acc/entry {}
        (acc/header (dom/h2 (dom/text "⬜ Context Menu"))))
      (acc/entry {}
        (acc/header (dom/h2 (dom/text "⬜ Spinner"))))
      (acc/entry {}
        (acc/header (dom/h2 (dom/text "⬜ Loader"))))

      (acc/entry {}
        (acc/header (dom/h2 (dom/text "⬜ Tree")))))
    ))
