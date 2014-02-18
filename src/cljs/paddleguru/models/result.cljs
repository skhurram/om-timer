(ns paddleguru.models.result
  (:require [clojure.set :as set]
            [schema.core :as s]
            [paddleguru.util :as u])
  (:import [goog.ui IdGenerator])
  (:require-macros [schema.macros :as sm]))

(defn guid []
  (.getNextUniqueId (.getInstance IdGenerator)))

;; ## Records

(def stamp (s/named s/Int "timestamp of state"))

;;TODO: add entry ID in here, and change event number to event id!
(sm/defrecord ResultEntry
    [uuid :- (s/named s/Str "Unique Result Entry ID")
     id :- (s/named s/Str "Racer number")
     event-num :- (s/named s/Int "Event number")
     time :- (s/named s/Str "result time")])

(sm/defn empty-entry :- ResultEntry
  "Returns an empty entry."
  []
  (ResultEntry. (guid) "" 0 ""))

(sm/defrecord StopwatchState
    [active? :- (s/named boolean "stopwatch running?")
     stopwatch-time :- (s/named s/Int "stopwatch time in millis")
     results :- [ResultEntry]
     timestamp :- stamp])

(sm/defrecord TimingAppState
    [stopwatch :- StopwatchState
     focus-cell :- (s/named s/Int "Rank of racer-number input to autofocus.")
     athletes
     live-results? :- (s/named boolean "broadcast live results?")])

(def initial-timingapp-state
  (TimingAppState.
   (StopwatchState. false 0 [] 0)
   1
   []
   true))
;; ## Validators

(defn valid-racer-number?
  "Takes in a string from an input box and returns true if it is a
  valid number or is empty ('' or nil)"
  [n]
         (js/isFinite n)
                        )

(def valid-time-formats
  [#"^[0-9][0-9]:[0-5][0-9]:[0-5][0-9].[0-9][0-9]$" ;;hh:mm:ss.ms
   #"^[0-9][0-9]:[0-5][0-9]:[0-5][0-9]$"            ;;hh:mm:ss
   #"^[0-9]:[0-5][0-9]:[0-5][0-9].[0-9][0-9]$"      ;;h:mm:ss.ms
   #"^[0-9]:[0-5][0-9]:[0-5][0-9]$"                 ;;h:mm:ss
   #"^dns$"
   #"^dq$"
   #"^dnf$"])

(defn valid-racer-time?
  "Takes in a string from an input box and returns true if it is
  formatted as hh:mm:ss.m, or dns, or dnf."
  [s]
  (boolean (or (empty? s)
               (some (fn [regex] (re-find regex (u/lower-case s)))
                     valid-time-formats))))

(defn format-racer-info
  "Takes in a map of information about a racer, and returns a string
  description. Useful for lables on timing app or results page."
  [info]
  (if (seq info)
    (str (get info "fullnames") " (" (get info "boat-type") ", "
         (get info "age-group") ")")
    "Not Found!"))

;; ## State Transitions
;;
;; These functions combine a state and some new event to produce a new
;; state.
(sm/defn enable-live :- TimingAppState
  "Enables broadcasting of live results."
  [timingapp :- TimingAppState]
  (assoc timingapp :live-results? true))

(sm/defn disable-live :- TimingAppState
  "Enables broadcasting of live results."
  [timingapp :- TimingAppState]
  (assoc timingapp :live-results? false))

(sm/defn change-focus :- TimingAppState
  "Changes the autofocus cell (for mark! or inserting a row, for
  example)."
  [timingapp :- TimingAppState rank :- s/Int]
  (assoc timingapp :focus-cell rank))

(sm/defn reset :- TimingAppState
  [timing :- TimingAppState]
  initial-timingapp-state)

(sm/defn change-racer-number :- StopwatchState
  "Changes the racer number of the ResultEntry at the given index to
  the new number."
  [s :- StopwatchState index :- s/Int new-number :- s/Str event-number :- s/Int]
  (-> s
      (assoc-in [:results index :id] new-number)
      (assoc-in [:results index :event-num] event-number)))

(sm/defn change-racer-time :- StopwatchState
  "Changes the racer time of the ResultEntry at the given index to the
  new time."
  [s :- StopwatchState index :- s/Int new-time :- s/Str]
  (assoc-in s [:results index :time] new-time))

(sm/defn delete-result :- StopwatchState
  "Deletes the result at the given index."
  [s :- StopwatchState index :- s/Int]
  (update-in s [:results] u/delete-index index))

(sm/defn insert-empty-result :- StopwatchState
  "Inserts an empty result into the results vector."
  [s :- StopwatchState index :- s/Int]
  (update-in s [:results]
             u/insert-index
             index
             (empty-entry)))

(sm/defn mark-timestamp :- StopwatchState
  "Appends a ResultEntry with a new-time string (hh:mm:ss.ms) to the
  results in StopwatchState."
  [s :- StopwatchState]
  (update-in s [:results] conj (ResultEntry. (guid) "" 0 (u/ms-to-time (:stopwatch-time s)))))

(sm/defn update-time :- StopwatchState
  "Updates the supplied StopwatchState to the supplied timestamp."
  [s :- StopwatchState new-timestamp :- stamp]
  (let [{:keys [stopwatch-time active? timestamp]} s]
    (cond (<= new-timestamp timestamp) s
          active? (assoc s
                    :stopwatch-time (-> new-timestamp
                                        (- timestamp)
                                        (+ stopwatch-time))
                    :timestamp new-timestamp)
          :else (assoc s :timestamp new-timestamp))))

(sm/defn start :- StopwatchState
  "Updates the timer and sets its active value to true."
  [s :- StopwatchState]
  (assoc s :active? true))

(sm/defn stop :- StopwatchState
  "Updates the timer and sets its active value to false."
  [s :- StopwatchState]
  (assoc s :active? false))

(sm/defn toggle :- StopwatchState
  "Toggles the current timer state and updates it forward to the new
  time."
  [s :- StopwatchState]
  (if (:active? s)
    (stop s)
    (start s)))

(sm/defn combine :- StopwatchState
  "Combines an old and a new timer state by merging the result vectors
  together. If a user shows up in the both the new and old result
  vectors, the new one wins.

  TODO: Note that this is probably the wrong behavior for now. You
  probably want to see if there are any discrepancies between the new
  and old states and return that as a diff that you can ship back to
  the user."
  [l :- StopwatchState r :- StopwatchState]
  (let [[old new] (sort-by :timestamp [l r])
        old-results (:results old)]
    (update-in new [:results]
               (fn [new-results]
                 (let [new-racers (set (map first new-results))
                       filtered (remove (comp new-racers first) old-results)]
                   (concat filtered new-results))))))

(defn event-start-time
  "Note: This is an approximation! Computes start time by taking the
  current system time in ms, and subtracting the watch time in ms. If
  you stop the watch for an extended period, and start again, the
  event-start-time will be off by the amount of time that the watch
  was stopped."
  [timer-state]
  (u/local-time-str (- (:timestamp timer-state)
                       (:stopwatch-time timer-state))))

;;;;;;;;;;;; This file autogenerated from src/cljx/paddleguru/models/result.cljx
