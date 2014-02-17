(ns paddleguru.client.timer
  (:require clojure.browser.repl
            [cljs.core.async :refer [put! chan <! timeout]]
            [om.core :as om :include-macros true]
            [kioo.om :as kioo :include-macros true]
            [paddleguru.util :as u]
            [paddleguru.models.result :as result]
            [enfocus.core :as ef]
            [enfocus.events :as ev]
            [clojure.set :as cset])
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:use-macros [dommy.macros :only [sel1]]
               [enfocus.macros :only [defsnippet clone-for]]))

(enable-console-print!)



;; ## State

(def athletes
  {"13" {"fullnames" "Bob Glynn", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:bglynn4151"], "gender" "male", "boat-type" "SUP (All)", "age-group" "60+"}, "24" {"fullnames" "Hilary Andersen", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:hilarya"], "gender" "female", "boat-type" "SUP (All)", "age-group" "50+"}, "14" {"fullnames" "Lia Gaetano", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:faib19"], "gender" "female", "boat-type" "K1", "age-group" "18-49"}, "36" {"fullnames" "Mitch Boothe", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:Mitch"], "gender" "male", "boat-type" "SUP (All)", "age-group" "50+"}, "37" {"fullnames" "Debbie Broughan", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:DebbieBroughan"], "gender" "female", "boat-type" "SUP (All)", "age-group" "60+"}, "16" {"fullnames" "Maylanie Bevens Guerra", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:Maylan"], "gender" "female", "boat-type" "SUP (All)", "age-group" "18-49"}, "38" {"fullnames" "Steve Funk", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:funk256"], "gender" "male", "boat-type" "SUP (All)", "age-group" "18-49"}, "39" {"fullnames" "Ryan Funk", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:ryanf"], "gender" "male", "boat-type" "SUP (All)", "age-group" "U17"}, "29" {"fullnames" "Michael Melville", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:scoutiy"], "gender" "male", "boat-type" "SUP (All)", "age-group" "60+"}, "1" {"fullnames" "Tim Stylianapoupalopolous", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:PADAWAN"], "gender" "male", "boat-type" "Kayak", "age-group" "60+"}, "2" {"fullnames" "Kyle Ly", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:kylely"], "gender" "male", "boat-type" "SUP (All)", "age-group" "50+"}, "3" {"fullnames" "Jeff Roy", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:jroy"], "gender" "male", "boat-type" "SUP (All)", "age-group" "50+"}, "6" {"fullnames" "Todd Bowers", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:todd351"], "gender" "male", "boat-type" "Surfski Single", "age-group" "18-49"}, "8" {"fullnames" "William Fenton", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:wintermutt"], "gender" "male", "boat-type" "Kayak", "age-group" "50+"}, "50" {"fullnames" "Jennifer Bottini", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:jbottini"], "gender" "female", "boat-type" "SUP (All)", "age-group" "18-49"}, "51" {"fullnames" "Sue Lutz", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:Cassie12"], "gender" "female", "boat-type" "Surfski Single", "age-group" "50+"}, "52" {"fullnames" "Barney Pugh", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:bpugh"], "gender" "male", "boat-type" "SUP (All)", "age-group" "60+"}, "53" {"fullnames" "Albert Romvari", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:aromvari"], "gender" "male", "boat-type" "Surfski Single", "age-group" "50+"}, "10" {"fullnames" "Steve Wilson, Mike Ammon", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:shortwave" "paddleguru.user:paddlerunride"], "gender" "male", "boat-type" "C2", "age-group" "50+"}, "54" {"fullnames" "Tom Dorno", "event-name" "5 Mile", "event-number" 1, "user-ids" ["paddleguru.user:Tom"], "gender" "male", "boat-type" "Surfski Single", "age-group" "50+"}})

(defonce timingapp
  (-> result/initial-timingapp-state
      (assoc :athletes athletes
             :cdn {:remove "https://paddleguru.com/img/redx.png"
                   :insert "https://paddleguru.com/img/greenarrow.png"})
      (atom))) :- result/TimingApp

(defn info-label [racer-num]
  (if (empty? racer-num)
    ""
    (result/format-racer-info (get @athletes (str racer-num)))))

;; ## Actions

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

;;;;;;;;;;;;;;;;;ON LOAD;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn all-number-set []
  (into #{} (keys @athletes)))

(defn finisher-row [focus-rank rank result owner]
  (reify
    om/IInitState
    (init-state [_]
      {:needs-focus (= rank focus-rank)
       :racer-text (:id result)
       :time-text (:time result)})
    om/IRenderState
    (render-state [_ {:keys [racer-text time-text needs-focus]}]
      (kioo/component "public/html/timing.html"
                      [:tr.finisher]
                      {[:td.rank] (kioo/content (str rank "."))
                       [:input.timing-boat-number] (kioo/do->
                                                    (if needs-focus
                                                      (do (reset! needs-focus false)
                                                          (kioo/set-attr :autoFocus true))
                                                      identity)
                                                    (if ((some-fn empty? (all-number-set)) @racer-text)
                                                      (kioo/remove-class "error")
                                                      (kioo/add-class "error"))
                                                    (kioo/set-attr :name (str "racer-number" rank)
                                                                   :data-rank rank
                                                                   :data-event-number (:event-num result)
                                                                   :value racer-text
                                                                   :onChange #(reset! racer-text (-> % .-target .-value))
                                                                   :onBlur (fn [_] (change-racer-number! rank @racer-text))))
                       [:input.timestamp] (let [time @time-text]
                                            (kioo/do->
                                             (kioo/set-attr :ref "timeField"
                                                            :value time
                                                            :placeholder (when (empty? time)
                                                                           "hh:mm:ss.ms")
                                                            :data-event-number (:event-num result)
                                                            :data-rank rank
                                                            :onChange #(reset! time-text (-> % .-target .-value))
                                                            :onBlur (fn [_] (change-racer-time! rank @time-text))
                                                            :name (str "time" rank))
                                             (if (result/valid-racer-time? time)
                                               (kioo/remove-class "error")
                                               (kioo/add-class "error"))))
                       [:a.remove] (kioo/set-attr :data-rank rank
                                                  :onClick (fn [_] (delete-row! rank)))
                       [:a.remove :img] (kioo/set-attr :src (:remove (:cdn @timingapp)))
                       [:a.insert] (kioo/set-attr :data-rank rank
                                                  :onClick (fn [_] (insert-row! rank)))
                       [:a.insert :img] (kioo/set-attr :src (:insert (:cdn @timingapp)))
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
                               (reverse
                                (map-indexed (fn [idx result]
                                               [finisher-row {:result result
                                                              :rank (inc idx)
                                                              :key (or (not-empty (:id result))
                                                                       (inc idx))
                                                              :focus-rank focus-cell}])
                                             (:results stopwatch)))))}))

(defn clock [stopwatch owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (go (loop []
            (<! (timeout 10))
            ;; This spot sometimes "Cannot read property 'firstChild' of undefined"
            (om/transact! stopwatch #(result/update-time % (u/now)))
            (recur))))
    om/IRender
    (render [_]
      (kioo/component "public/html/timing.html"
                      [:.clock-container]
                      {[:#clock] (kioo/content
                                  (if-let [time (:stopwatch-time stopwatch)]
                                    (u/ms-to-time time)
                                    "Loading..."))
                       [:input.mark] (kioo/set-attr :onClick
                                                    (fn [_]
                                                      (when (:active? @stopwatch)
                                                        (om/transact! stopwatch result/mark-timestamp))))}))))

(defn timing-shell [state owner]
  (kioo/component "public/html/timing.html"
                  [:div.content]
                  {
                   [:h4.event-name] (kioo/substitute nil)
                   [:.clock-container] (kioo/content
                                        (om/build clock (:stopwatch state)))
                   #_#_[:tbody.timestamps] (let [results (:results (:stopwatch state))
                                                 focus-rank (:focus-cell state)]
                                             (kioo/content
                                              (om/build-all (partial finisher-row focus-rank)
                                                            (map-indexed #(assoc %2 :rank %1) results))))
                   [:div.remaining-paddlers] (let [results (:results (:stopwatch state))
                                                   all-numbers (into #{} (keys (:athletes state)))]
                                               (kioo/content
                                                (for [num (unfinished results all-numbers)]
                                                  (let [info (get (:athletes state) num)]
                                                    (kioo/html
                                                     [:p (str "#" num " " (get info "fullnames"))])))))
                   [:input.start] (kioo/set-attr :value (if (:active? (:stopwatch state))
                                                          "Stop Timer"
                                                          "Start Timer")
                                                 :onClick (fn [_] (om/transact! state :stopwatch result/toggle)))
                   ;; Because we're only timing a single event.
                   [:td.event-column] (kioo/substitute "")
                   [:td.roweventlabel] (kioo/substitute "")
                   }))

(om/root timing-shell
         timingapp
         {:target (sel1 :.react_shell)})
