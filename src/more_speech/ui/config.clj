(ns more-speech.ui.config)

(def no-fill [nil])
(def black [0 0 0])
(def white [255 255 255])

(def window-margin 60)                                      ; vertical space taken up by menu bar and window title.


(def thumb-normal [200 200 200])
(def thumb-dragged [150 150 150])
(def thumb-h 15)
(def thumb-margin 5)

(def window-title-height 20)
(def scroll-bar-w 20)
(def scroll-bar-button-w 15)
(def scroll-bar-button-h 15)
(def scroll-bar-button-top-margin 5)
(def scroll-bar-button-bottom-margin (+ scroll-bar-button-top-margin
                                        scroll-bar-button-h))

(def header-window-dimensions
  {:x 20 :y 10
   :width 100                                               ;window width in chars
   :text-width 90                                           ;header text width in chars
   :left-margin 20                                          ;px
   :height-fraction 5/8                                     ;of screen
   })

(def header-lines 2)
(def header-top-margin 2)
(def header-bottom-margin 2)

(def article-window-top-margin 10)
(def article-window-bottom-margin 10)

(def article-window-dimensions
  {
   :text-width 90
   })

(def author-window-dimensions
  {
   :left-margin 10
   :top-margin 10
   :width 40
   :key-abbreviation 9
   :height-fraction 1/4
   })

(def edit-window-dimensions
  {
   :left-margin 10
   :top-margin 10
   :width 100
   :text-width 90
   :bottom-margin 10
   })


