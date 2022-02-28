(ns more-speech.content.article
  (:require [clojure.spec.alpha :as s]
            [more-speech.nostr.util :refer [num->hex-string]]
            [clojure.set :as set])
  (:import (java.util Date)
           (java.text SimpleDateFormat)))

(s/def ::id number?)
(s/def ::group string?)
(s/def ::subject string?)
(s/def ::author string?)
(s/def ::time number?)
(s/def ::body string?)
(s/def ::thread-count number?)
(s/def ::header (s/keys :req-un [::id ::group ::subject ::author ::time ::body ::thread-count]))

(s/def ::author-nickname string?)
(s/def ::author-pubkey string?)
(s/def ::author-nickname-tuple (s/tuple ::author-pubkey ::author-nickname))

(defn make-header [id name time body thread-count indent]
  {:id id
   :group ""
   :author name
   :subject "?"
   :time time
   :body body
   :thread-count thread-count
   :indent indent}
  )

(defn format-time [time]
  (let [time (* time 1000)
        date (Date. (long time))]
    (.format (SimpleDateFormat. "dd MMM yy kk:mm:ss z") date))
  )

(defn abbreviate [s n]
  (if (<= (count s) n)
    s
    (str (subs s 0 n) "...")))

(defn abbreviate-body [body]
  (abbreviate body 95))

(defn abbreviate-author [author]
  (abbreviate author 20))

(defn abbreviate-key [pubkey]
  (abbreviate pubkey 8))

(defn markup-header [header]
  (let [thread-count (:thread-count header)
        indent (get header :indent 0)]
    [
     :regular
     (apply str (repeat indent "•"))
     :bold
     (abbreviate-author (:author header))
     :regular
     (if (pos? thread-count)
       (str " (" thread-count ")")
       "")
     :bold
     :pos 30
     (:subject header)
     :regular
     :pos 60
     (format-time (:time header))
     :new-line
     (abbreviate-body (:body header))
     :new-line
     ]))

(defn markup-author [[pubkey name]]
  [:bold
   (abbreviate-key (num->hex-string pubkey))
   :regular
   " - "
   name
   :new-line
   ])

(defn thread-events
  "returns events in threaded order."
  ([events event-map open-events]
   (thread-events events event-map open-events 0))
  ([events event-map open-events indent]
   (loop [events events
          threaded-events []
          processed-events #{}]
     (cond
       (empty? events)
       threaded-events

       (contains? processed-events (first events))
       (recur (rest events) threaded-events processed-events)

       :else
       (let [event-id (first events)
             event (get event-map event-id)
             references (:references event)
             no-references? (empty? references)
             not-open? (nil? (open-events event-id))
             no-thread? (or no-references? (and (zero? indent) not-open?))]
         (if no-thread?
           (recur (rest events)
                  (conj threaded-events (assoc event :indent indent))
                  (conj processed-events event-id))
           (let [thread (thread-events references event-map open-events (inc indent))
                 threaded-events (conj threaded-events (assoc event :indent indent))
                 threaded-events (vec (concat threaded-events thread))
                 processed-events (set/union processed-events (set (map :id thread)))]
             (recur (rest events)
                    threaded-events
                    (conj processed-events event-id)))))
       ))))
