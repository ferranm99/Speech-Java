(ns more-speech.ui.article-window
  (:require
    [more-speech.ui.widget :refer [widget]]
    [more-speech.ui.button :refer [map->button
                                   up-arrow
                                   down-arrow
                                   draw-thumb]]
    [more-speech.ui.graphics :as g]
    [more-speech.nostr.util :refer [num->hex-string]]
    [more-speech.ui.header-frame :refer [scroll-up
                                         scroll-down
                                         map->header-frame]]
    [more-speech.ui.app-util :as app]
    [more-speech.ui.config :as config]
    ))

(declare setup-article-window
         draw-article-window
         update-article-window
         drag-thumb
         lock-thumb
         unlock-thumb
         thumb-position)

(defrecord article-window [x y w h page-up page-down]
  widget
  (setup-widget [widget state]
    (setup-article-window widget state))
  (update-widget [widget state]
    (update-article-window widget state))
  (draw-widget [widget state]
    (draw-article-window state widget)))

(defn setup-article-window [widget _state]
  (let [{:keys [x y w h]} widget
        dim config/header-frame-dimensions
        frame-path (concat (:path widget) [:header-frame])
        scroll-up (partial scroll-up frame-path)
        scroll-down (partial scroll-down frame-path)
        frame (map->header-frame {:x (+ x (:left-margin dim))
                                  :y (+ y (:top-margin dim))
                                  :w (- w (:right-margin dim)
                                        config/scroll-bar-w)
                                  :h (- h (:bottom-margin dim))
                                  :display-position 0})
        sb-button-offset (+ (/ config/scroll-bar-w 2)
                            (/ config/scroll-bar-button-w 2))
        sb-button-x (+ x w (- sb-button-offset) 0.5)
        widget (assoc widget
                 :header-frame frame
                 :page-up (map->button {:x sb-button-x
                                        :y (+ y config/scroll-bar-button-top-margin)
                                        :h config/scroll-bar-button-h
                                        :w config/scroll-bar-button-w
                                        :left-down scroll-down
                                        :left-held scroll-down
                                        :draw up-arrow})
                 :page-down (map->button {:x sb-button-x
                                          :y (+ y h (- config/scroll-bar-button-bottom-margin))
                                          :h config/scroll-bar-button-h
                                          :w config/scroll-bar-button-w
                                          :left-down scroll-up
                                          :left-held scroll-up
                                          :draw down-arrow})
                 :thumb (map->button {:x sb-button-x
                                      :y (thumb-position frame)
                                      :h config/thumb-h
                                      :w config/scroll-bar-button-w
                                      :draw draw-thumb
                                      :left-held drag-thumb
                                      :left-down lock-thumb
                                      :left-up unlock-thumb
                                      }))]
    widget))

(defn update-article-window [widget state]
  (let [header-frame (:header-frame widget)
        thumb-pos (thumb-position header-frame)
        thumb-path (concat (:path widget) [:thumb])]
    (assoc-in state (concat thumb-path [:y]) thumb-pos)))

(defn draw-article-window [state window]
  (let [application (:application state)
        g (:graphics application)]
    (g/with-translation
      g [(:x window) (:y window)]
      (fn [g]
        (g/stroke g [0 0 0])
        (g/stroke-weight g 2)
        (g/fill g config/white)
        (g/rect g [0 0 (:w window) (:h window)])))))

(defn- thumb-drag-height [frame]
  (- (:h frame)
     (* 2 (+ config/scroll-bar-button-top-margin
             config/scroll-bar-button-h
             config/thumb-margin))
     config/thumb-h))

(defn- thumb-origin [frame]
  (+ (:y frame)
     config/scroll-bar-button-top-margin
     config/scroll-bar-button-h
     config/thumb-margin))

(defn- thumb-position [header-frame]
  (let [display-position (get header-frame :display-position 0)
        total-headers (get header-frame :total-headers 0)
        height (thumb-drag-height header-frame)]
    (if (zero? total-headers)
      (thumb-origin header-frame)
      (+ (thumb-origin header-frame)
         (* height (/ display-position total-headers))))))

(defn- drag-thumb [button state]
  (let [graphics (get-in state [:application :graphics])
        thumb-path (:path button)
        parent-path (drop-last thumb-path)
        article-window-path (drop-last thumb-path)
        header-frame-path (concat article-window-path [:header-frame])
        header-frame (get-in state header-frame-path)
        total-headers (get header-frame :total-headers 0)
        height (thumb-drag-height header-frame)
        top (thumb-origin header-frame)
        [_ my _] (g/get-mouse graphics)
        dy (- my top)
        dy (max dy 0)
        dy (min dy height)
        display-position (* (/ dy height) total-headers)
        state (assoc-in state
                        (concat header-frame-path [:display-position])
                        display-position)]
    (app/update-widget state parent-path)))

(defn- lock-thumb [widget state]
  (app/lock-mouse state widget))

(defn- unlock-thumb [_widget state]
  (app/unlock-mouse state))
