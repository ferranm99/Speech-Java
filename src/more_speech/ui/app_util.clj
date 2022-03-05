(ns more-speech.ui.app-util
  (:require [more-speech.ui.widget :as w]))


(defn toggle-event-thread [button state]
  (let [id (:id button)
        frame-path (drop-last (:path button))
        open-thread (get-in state [:application :open-thread])
        open-thread (if (contains? open-thread id)
                      (disj open-thread id)
                      (conj open-thread id))
        state (assoc-in state [:application :open-thread] open-thread)]
    (w/redraw-widget state frame-path)))

(defn select-header [id state]
  (let [selected (get-in state [:application :selected-header])
        state (if (= id selected)
                (assoc-in state [:application :selected-header] nil)
                (assoc-in state [:application :selected-header] id))
        state (w/redraw-widget state [:application :article-window])
        state (w/redraw-widget state [:application :header-window])]
    state))