(ns paddleguru.client.timer
  "Example of how to start rewriting the timing namespace."
  (:refer-clojure :exclude [atom])
  (:require clojure.browser.repl ;; someone has to include this.
            [dommy.core :as dommy]
            [kioo.reagent :as kioo :include-macros true]
            [reagent.core :as reagent :refer [atom]]
            [ajax.core :as ajax]
            [paddleguru.util :as u]
            [paddleguru.models.result :as result]
            [paddleguru.client.shared :as shared]
            [enfocus.core :as ef]
            [enfocus.events :as ev]
            [schema.core :as s]
            [clojure.set :as cset])
  (:require-macros [schema.macros :as sm])
  (:use-macros [dommy.macros :only [sel sel1]]
               [enfocus.macros :only [deftemplate defaction defsnippet clone-for
                                      wait-for-load]]))

;;;;;;;;;;;;;;;;VARS;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defonce timingapp
  (atom result/initial-timingapp-state)) :- result/TimingApp

;;map of boat number to racer info map w keys: "fullnames",
;;"usernames", "boat-type", "gender", "age-group", "event-number", "event-name"
(defonce athletes
  (atom
   {"13" {"fullnames" "Bob Glynn", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:bglynn4151"], "gender" "male", "boat-type" "SUP (All)", "age-group" "60+"}, "24" {"fullnames" "Hilary Andersen", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:hilarya"], "gender" "female", "boat-type" "SUP (All)", "age-group" "50+"}, "14" {"fullnames" "Lia Gaetano", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:faib19"], "gender" "female", "boat-type" "K1", "age-group" "18-49"}, "36" {"fullnames" "Mitch Boothe", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:Mitch"], "gender" "male", "boat-type" "SUP (All)", "age-group" "50+"}, "37" {"fullnames" "Debbie Broughan", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:DebbieBroughan"], "gender" "female", "boat-type" "SUP (All)", "age-group" "60+"}, "16" {"fullnames" "Maylanie Bevens Guerra", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:Maylan"], "gender" "female", "boat-type" "SUP (All)", "age-group" "18-49"}, "38" {"fullnames" "Steve Funk", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:funk256"], "gender" "male", "boat-type" "SUP (All)", "age-group" "18-49"}, "39" {"fullnames" "Ryan Funk", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:ryanf"], "gender" "male", "boat-type" "SUP (All)", "age-group" "U17"}, "29" {"fullnames" "Michael Melville", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:scoutiy"], "gender" "male", "boat-type" "SUP (All)", "age-group" "60+"}, "1" {"fullnames" "Tim Stylianapoupalopolous", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:PADAWAN"], "gender" "male", "boat-type" "Kayak", "age-group" "60+"}, "2" {"fullnames" "Kyle Ly", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:kylely"], "gender" "male", "boat-type" "SUP (All)", "age-group" "50+"}, "3" {"fullnames" "Jeff Roy", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:jroy"], "gender" "male", "boat-type" "SUP (All)", "age-group" "50+"}, "6" {"fullnames" "Todd Bowers", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:todd351"], "gender" "male", "boat-type" "Surfski Single", "age-group" "18-49"}, "8" {"fullnames" "William Fenton", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:wintermutt"], "gender" "male", "boat-type" "Kayak", "age-group" "50+"}, "50" {"fullnames" "Jennifer Bottini", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:jbottini"], "gender" "female", "boat-type" "SUP (All)", "age-group" "18-49"}, "51" {"fullnames" "Sue Lutz", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:Cassie12"], "gender" "female", "boat-type" "Surfski Single", "age-group" "50+"}, "52" {"fullnames" "Barney Pugh", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:bpugh"], "gender" "male", "boat-type" "SUP (All)", "age-group" "60+"}, "53" {"fullnames" "Albert Romvari", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:aromvari"], "gender" "male", "boat-type" "Surfski Single", "age-group" "50+"}, "10" {"fullnames" "Steve Wilson, Mike Ammon", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:shortwave" "paddleguru.user:paddlerunride"], "gender" "male", "boat-type" "C2", "age-group" "50+"}, "54" {"fullnames" "Tom Dorno", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:Tom"], "gender" "male", "boat-type" "Surfski Single", "age-group" "50+"}}))

(defonce cdn-assets
  (atom {:remove "https://paddleguru.com/img/redx.png"
         :insert "https://paddleguru.com/img/greenarrow.png"}))

(def timing-template "/html/timing.html")

;;;;;;;;;;;;;;;;;SELECTORS;;;;;;;;;;;;;;;;;;;;;;;;;
(def start-button
  (sel1 "input[name=\"starter\"]"))

(def mark-button
  (sel1 "input[name=\"marker\"]"))

(def delete-all-button
  (sel1 "input[name=\"clearer\"]"))

(def clock
  (sel1 "#clock"))

(def clock-form
  (sel1 "#clockform"))

(def regatta-title
  (dommy/value (sel1 "input[name='title']")))

(def userid
  (dommy/value (sel1 "input[name='userid']")))

;;TO DO: kill event-numbers
(def event-numbers
  ["1"])

(def event-ids ;;JSON array of events being timed
  (dommy/value (sel1 "input[name=\"event-ids\"]")))

(def enable-live-button
  (sel1 "#enable-live"))

(def disable-live-button
  (sel1 "#disable-live"))

(def offline-notification
  (sel1 "a.timing-notification-error"))

(def online-notification
  (sel1 "a.timing-notification-ok"))

(defn errors?
  "Returns true if there are errors in any ResultEntry (improperly formatted id or time)."
  [results]
  (not-every? (fn [res] (and (result/valid-racer-number? (:id res))
                             (result/valid-racer-time? (:time res))))
              results))

(defn info-label [racer-num]
  (if (empty? racer-num)
    ""
    (result/format-racer-info (get @athletes (str racer-num)))))

(defn event-name [racer-num]
  (if (empty? racer-num)
    ""
    (get-in @athletes [(str racer-num) "event-name"])))

(defn show-alert-when-leaving! []
  (set! (.-onbeforeunload js/window)
        (fn [] "Note: Check to make sure it says 'All changes saved to cloud' for extra backup.")))

;;;;;;;;;;;;;;;;;ACTIONS;;;;;;;;;;;;;;;;;;;;;;;;

(defn toggle!
  "Toggles timer state (Start or Stop)"
  [e]
  (swap! timingapp update-in [:stopwatch] result/toggle))

(defn mark!
  "Adds a result with a timestamp, and sets the focus to the newest
  finisher row."
  [e]
  (swap! timingapp
         (fn [{:keys [stopwatch] :as old-timingapp}]
           (-> (result/change-focus old-timingapp
                                    (inc (count (:results stopwatch))))
               (assoc :stopwatch (if (:active? stopwatch)
                                   (result/mark-timestamp stopwatch)
                                   stopwatch))))))

(defn delete-all!
  "Clears the StopwatchState, erasing all data." [e]
  (when (js/confirm "Are you sure you want to clear the timing app for this event?")
    (swap! timingapp result/reset)))

(defn update-time!
  "Used to update the time on the clock, every 10 ms." []
  (swap! timingapp update-in [:stopwatch] (fn [old-stopwatch]
                                            (result/update-time old-stopwatch (u/now))))
  (js/setTimeout update-time! 10))

(defn show-online!
  "Shows message showing that user is online."
  []
  (dommy/set-attr! online-notification :class "timing-notification-ok")
  (dommy/set-attr! offline-notification :class "timing-notification-error hide"))

(defn show-offline!
  "Shows message indicating that user is in offline mode."
  []
  (dommy/set-attr! online-notification :class "timing-notification-ok hide")
  (dommy/set-attr! offline-notification :class "timing-notification-error"))

(defn autofocus-and-update
  "Takes in a timingapp, a racer-number rank to autofocus on, and a
  new stopwatch to assoc into the timingapp."
  [timingapp focus-rank stopwatch]
  (-> (result/change-focus timingapp focus-rank)
      (assoc :stopwatch stopwatch)))

(defn change-racer-number!
  "Changes a racer number for a ResultEntry"
  [rank number]
  (swap! timingapp
         (fn [{:keys [stopwatch] :as old-timingapp}]
           (let [cleaned (u/remove-spaces number)
                 event-num (u/str-to-int (get-in @athletes [cleaned "event-number"]))
                 focus-rank (if (= (inc rank) (count (:results stopwatch)))
                              (inc rank) ;;marked, focus top
                              rank)]
             (autofocus-and-update old-timingapp
                                   focus-rank
                                   (result/change-racer-number stopwatch
                                                               (dec rank)
                                                               cleaned
                                                               event-num))))))

(defn change-racer-time!
  "Changes a racer time for a ResultEntry"
  [rank time]
  (swap! timingapp
         (fn [{:keys [stopwatch] :as old-timingapp}]
           (let [focus-rank (if (= (inc rank) (count (:results stopwatch)))
                              (inc rank) ;;marked, focus top
                              rank)]
             (autofocus-and-update old-timingapp
                                   focus-rank
                                   (result/change-racer-time stopwatch
                                                             (dec rank)
                                                             (u/remove-spaces time)))))))

(defn insert-row!
  "Inserts a row at the specified rank"
  [rank]
  (swap! timingapp
         (fn [{:keys [stopwatch] :as old-timingapp}]
           (autofocus-and-update old-timingapp
                                 rank
                                 (result/insert-empty-result stopwatch (dec rank))))))

(defn delete-row!
  "Deletes a row at the specified rank"
  [rank]
  (swap! timingapp
         (fn [{:keys [stopwatch] :as old-timingapp}]
           (autofocus-and-update old-timingapp
                                 (dec rank)
                                 (result/delete-result stopwatch (dec rank))))))

(defn enable-live!
  "Enables broadcasting of live results." []
  (swap! timingapp result/enable-live))

(defn disable-live!
  "Disables broadcasting of live results." []
  (swap! timingapp result/disable-live))

(defn initialize-state!
  "If there are hidden form fields with data from a previously saved
  timing session, use them, otherwise setup a clear initial state."
  []
  ;;TODO: This should take in saved timing state from server and do a
  ;;comparison of :timestamp to see which one to render.
  (reset! timingapp {}))

;;;;;;;;;;;;;;;;;TEMPLATING;;;;;;;;;;;;;;;;;;;;;;;;;

(defn finished
  "Takes in a vector of ResultEntries, and returns the racer numbers
  of finishers."
  [results all-numbers]
  (let [finished-nums (into #{} (map :id results))]
    ;;we take the intersection to weed out incorrect racer nums
    (cset/intersection all-numbers finished-nums)))

(defn unfinished
  "Takes in a vector of ResultEntries, and returns the racer numbers
  of paddlers who have not yet finished."
  [results all-numbers]
  (let [finished-numbers (finished results all-numbers)]
    (cset/difference all-numbers finished-numbers)))

(defsnippet remaining-paddlers timing-template
  [:div.remaining-paddlers]
  [results all-numbers] ;;vector of ResultEntries
  [:p] (clone-for [num (unfinished results all-numbers)]
                  (let [info (get @athletes num)]
                    (ef/content (str "#" num " " (get info "fullnames"))))))

;;;;;;;;;;;;;;;;;ON LOAD;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn parse-saved-results
  "Parses the racer number, event-number and time input fields in the
  HTML send by the server when there was a previously saved session."
  []
  (let [times (map dommy/value (sel "td input.timestamp"))
        racer-nums (map dommy/value (sel "td input.timing-boat-number"))
        event-nums (map #(u/str-to-int (dommy/attr % :data-event-number))
                        (sel "td input.timestamp"))]
    (->> (interleave racer-nums event-nums times)
         (partition 3)
         (map (fn [[racernum eventnum time]]
                (result/ResultEntry. racernum eventnum time))))))

(defn all-number-set []
  (into #{} (keys @athletes)))

(defn finisher-row [{:keys [result rank focus-rank]}]
  (let [racer-text (atom (:id result))
        time-text  (atom (:time result))
        needs-focus (atom (= rank focus-rank))]
    (fn [_]
      (kioo/component "public/html/timing.html"
                      [:tr.finisher]
                      {[:td.rank] (kioo/content (str rank "."))
                       [:input.timing-boat-number] (kioo/do->
                                                    (if @needs-focus
                                                      (do (reset! needs-focus false)
                                                          (kioo/set-attr :autoFocus true))
                                                      identity)
                                                    (if ((some-fn empty? (all-number-set)) @racer-text)
                                                      (kioo/remove-class "error")
                                                      (kioo/add-class "error"))
                                                    (kioo/set-attr :name (str "racer-number" rank)
                                                                   :data-rank rank
                                                                   :data-event-number (:event-num result)
                                                                   :on-change #(reset! racer-text (-> % .-target .-value))
                                                                   :on-blur (fn [_]
                                                                              (change-racer-number! rank @racer-text))
                                                                   :value @racer-text))
                       [:input.timestamp] (let [time @time-text]
                                            (kioo/do->
                                             (kioo/set-attr :ref "timeField"
                                                            :value time
                                                            :placeholder (when (empty? time)
                                                                           "hh:mm:ss.ms")
                                                            :data-event-number (:event-num result)
                                                            :data-rank rank
                                                            :on-change #(reset! time-text (-> % .-target .-value))
                                                            :on-blur (fn [_] (change-racer-time! rank @time-text))
                                                            :name (str "time" rank))
                                             (if (result/valid-racer-time? time)
                                               (kioo/remove-class "error")
                                               (kioo/add-class "error"))))
                       [:a.remove] (kioo/set-attr :data-rank rank
                                                  :on-click (fn [_] (delete-row! rank)))
                       [:a.remove :img] (kioo/set-attr :src (:remove @cdn-assets))
                       [:a.insert] (kioo/set-attr :data-rank rank
                                                  :on-click (fn [_] (insert-row! rank)))
                       [:a.insert :img] (kioo/set-attr :src (:insert @cdn-assets))
                       [:td.roweventlabel] (kioo/substitute nil)
                       [:td.rowlabel] (let [label (info-label (:id result))]
                                        (kioo/do-> (kioo/set-attr :name (str "label" rank))
                                                   (kioo/content label)
                                                   (if (= label "Not Found!")
                                                     (kioo/add-class "error")
                                                     (kioo/remove-class "error"))))}))))

(defn header-row []
  (kioo/component "public/html/timing.html"
                  [:table.finishers :thead]
                  {[:td.event-column] (kioo/substitute nil)}))

(defn finisher-rows []
  (kioo/component "public/html/timing.html"
                  [:table.finishers]
                  {[:thead] (kioo/substitute (header-row))
                   [:tbody] (kioo/content
                             (let [{:keys [stopwatch focus-cell]} @timingapp]
                               (.log js/console "New results: " (pr-str (:results stopwatch)))
                               (reverse
                                (map-indexed (fn [idx result]
                                               [finisher-row {:result result
                                                              :rank (inc idx)
                                                              :key (or (not-empty (:id result))
                                                                       (inc idx))
                                                              :focus-rank focus-cell}])
                                             (:results stopwatch)))))}))
;; Example Bullshit

(defn reagent []
  (reagent/render-component [finisher-rows]
                            (sel1 :.react_shell)))

(defn on-load
  "Called when the window has finished loading, initializes state and
  adds watchers."
  []
  (add-watch timingapp :timer-toggle
             (fn [_ _ _ new-timingapp]
               (dommy/set-value! start-button
                                 (if (:active? (:stopwatch new-timingapp))
                                   "Stop Timer"
                                   "Start Timer"))))
  (add-watch timingapp :update-time
             (fn [_ _ _ new-timingapp]
               (dommy/set-text! clock (u/ms-to-time (:stopwatch-time (:stopwatch new-timingapp))))))
  (add-watch timingapp :results-change
             (fn [_ _ old-timingapp new-timingapp]
               (when (not= (:results (:stopwatch old-timingapp))
                           (:results (:stopwatch new-timingapp)))
                 (let [results (:results (:stopwatch new-timingapp))
                       all-numbers (all-number-set)]
                   (wait-for-load
                    (ef/at [:div.remaining-paddlers]
                           (ef/substitute
                            (remaining-paddlers results all-numbers)))))
                 (shared/maybe-display-errors
                  (errors? (:results (:stopwatch new-timingapp)))))))

  ;;setup listeners
  (dommy/listen! mark-button :click mark!)
  (dommy/listen! start-button :click toggle!)
  (dommy/listen! enable-live-button :click enable-live!)
  (dommy/listen! disable-live-button :click disable-live!)
  (dommy/listen! delete-all-button :click delete-all!)
  (comment
    (dommy/listen! [js/document :input.timing-boat-number]
                   :change (fn [e] (change-racer-number!
                                    (u/str-to-int (u/get-target-attr e "data-rank"))
                                    (u/get-target-value e))))
    (dommy/listen! [js/document :input.timestamp]
                   :change (fn [e] (change-racer-time!
                                    (u/str-to-int (u/get-target-attr e "data-rank"))
                                    (u/get-target-value e))))
    (dommy/listen! [js/document :a.remove]
                   :click (fn [e] (delete-row!
                                   (u/str-to-int (u/get-target-attr e "data-rank")))))
    (dommy/listen! [js/document :a.insert]
                   :click (fn [e] (insert-row!
                                   (u/str-to-int (u/get-target-attr e "data-rank"))))))
  (dommy/listen! js/document
                 :keypress (fn [e]
                             ;;Prevent submit on enter for all devices.
                             ;;Mark on enter for desktops.
                             (let [size (-> js/document
                                            (.-documentElement)
                                            (.-clientWidth))]
                               (when (= (.-which e) 13)
                                 (do (.preventDefault e)
                                     (when (> size 992)
                                       (mark! e)))))))
  (reagent)
  ;;alert if you try to leave page
  (show-alert-when-leaving!))

;;TESTS:
;;1: If you load timing page with no remote or local data, should
;;initialize an empty TimingApp

;;2. If you load a timing page with local data only, it should
;;initialize timing app with that.

;;3. If you load a timing page with remote data only, it should use
;;that.

;;4. If you load timing page with local and remote data, should
;;initialize timing app with whichever one has the newer timestamp.

;;5. Test each of the mutators to the timingappstate.

;;6. Make sure UI elements are not shown until the timingappstate has
;;been initialized.

;;7. Test back and forwards buttons to make sure nothing gets nuked.

;;8. Test publishing results in various scenarios - make sure times
;;are still saved. For example - change a racer time and then
;;immediately click publish (without clicking outside the racer time
;;cell).

;;9: Timing on ipad, press start. Turn it off. Before finishers start
;;coming in, turn it back on. May try to auto-reload page. If they're
;;online-- all good. If they're offline - will keep saying
;;loading. Only solution is to stash athletes data locally.


(defn change-type [old new]
  (let [old-count (count old)
        new-count (count new)
        [[deleted] _ [changed]] (clojure.data/diff old new)]
    (cond (= old-count new-count) [:modification changed]
          (> old-count new-count) [:deletion deleted]
          :else [:addition changed])))
