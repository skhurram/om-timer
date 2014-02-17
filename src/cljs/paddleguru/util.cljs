(ns paddleguru.util
  (:require [clojure.string :as s]
            [schema.core :as sm]
            [goog.string :as gstring]
            [goog.string.format :as gformat]))

(defn insert [v pos val]
  (apply conj (subvec v 0 pos) val (subvec v pos)))

(defn map-by [key-func val-func coll]
  (into {} (map (juxt key-func val-func) coll)))

(defn between
  "returns a predicate that checks that the supplied number falls
  between the inclusive lower and exclusive upper bounds supplied."
  [low high]
  (fn [x]
    (and (>= x low)
         (< x high))))

(defn leaves
  "Takes in a nested map structure (all leaves must have equal depth),
  and returns the keys of the lowest level map concated
  together. Useful for getting all usernames in a rankings map."
  [m]
  (if (map? (first (vals m)))
    (mapcat leaves (vals m))
    (keys m)))

(defn mapk
  "Maps the keyspace using the supplied function. Any duplicate keys
  will get knocked out in a nondeterministic order, so be careful!"
  [f m]
  (into {} (for [[k v] m]
             [(f k) v])))

(defn map-values
  "Maps the keyspace using the supplied function. Any duplicate keys
  will get knocked out in a nondeterministic order, so be careful!"
  [f m]
  (into {} (for [[k v] m]
             [k (f v)])))

(defn merge-with-map
  "Returns a map that consists of the rest of the maps conj-ed onto
  the first.  If a key occurs in more than one map, the mapping(s)
  from the latter (left-to-right) will be combined with the mapping in
  the result by looking up the proper merge function and in the
  supplied map of key -> merge-fn and using that for the big merge. If
  a key doesn't have a merge function, the right value wins (as with
  merge)."
  [merge-fns & maps]
  (when (some identity maps)
    (let [merge-entry (fn [m e]
                        (let [k (key e) v (val e)]
                          (if-let [f (and (contains? m k)
                                          (merge-fns k))]
                            (assoc m k (f (get m k) v))
                            (assoc m k v))))
          merge2 (fn [m1 m2]
                   (reduce merge-entry (or m1 {}) (seq m2)))]
      (reduce merge2 maps))))

(defn no-duplicates?
  "Takes in a collection and tests if all the elements are unique, ie
  no duplicates."
  [c]
  (= c (distinct c)))

(defn find-duplicates [seq]
  (for [[id freq] (frequencies seq)
        :when (> freq 1)]
    id))

(defn update-in-all [m ks f]
  (reduce #(update-in %1 [%2] f) m ks))

(defn delete-index
  "Takes in a vector and an index, and returns a vector without the
  element at the given index."
  [v index]
  (vec (concat (subvec v 0 index)
               (subvec v (inc index) (count v)))))

(defn insert-index
  "Takes in a vector, index, and element and returns a vector with the
  element inserted at the given index (shifting the rest over)."
  [v index element]
  (vec (concat (subvec v 0 index)
               [element]
               (subvec v index (count v)))))

(def separate
  "Can be used in conjunction with a predicate function and a
collection to return: [(remove predicate items), (filter predicate
items)]"
  (juxt remove filter))

(defn wrap-p
  [s]
  {:tag :p
   :content [s]})

(defn wrap-all-in-ps
  [c]
  (map wrap-p (flatten c)))

(def div-rem
  "Takes two numbers and returns a 2-vector of [quotient, remainder]"
  (juxt quot mod))








(defn ^:export remove-spaces [input]
  (when input
    (s/replace input " " "")))

(defn ^:export lower-case [input]
  (when input

    (.toLowerCase input)))

(defn remove-special-chars [input]
  (when input
    (s/replace input #"[^a-zA-Z0-9]+" "")))






(defn ^:export str-to-int
  "converts string to ints, returns 0 for exceptions."
  [s]
  (js/parseInt s)



  )

(defn ^:export str-to-float
  "converts string to float, returns 0 for exceptions."
  [s]
  (js/parseFloat s)



  )

(defn str-to-double
  "converts string to doubles, returns 0 for exceptions."
  [s]
  (js/parseFloat s)



  )

(defn pretty-int
  "Takes in an int or a str, adds commas where appropriate, returns a
  string."
  [n]
  (->> (reverse (str n))
       (partition 3 3 [])
       (interpose [\,])
       (map #(apply str %))
       (apply str)
       (s/reverse)))

(def div-mod (juxt quot mod))

(defn ^:export ms-to-time [millis]
  (let [[s ms] (div-mod millis 1000)
        [min s] (div-mod s 60)
        [hr min] (div-mod min 60)]
    (       gstring/format              "%02d:%02d:%02d.%02d" hr min s
                                        (int (       Math.floor                  (/ ms 10))))))

(defn ^:export time-to-ms [time]
  (let [parsed (vec (map str-to-int
                         (re-seq #"[0-9]+" time)))]
    (+ (* (parsed 0) 3600000)
       (* (parsed 1) 60000)
       (* (parsed 2) 1000)
       (* (parsed 3) 10))))

(def max-time
  (time-to-ms "99:59:59.99"))

(defn ^:export to-currency [n]
  (let [num (str-to-double n)
        nocommas (                    gstring/format "%.2f" num)
        whole-decimal-vec (s/split nocommas #"\.")
        whole-num-str (pretty-int (first whole-decimal-vec))]
    (str "$" whole-num-str "." (second whole-decimal-vec))))














































































































(defn log [s]
  (.log js/console s))

(defn ^:export now
  "Returns the current UTC time (since epoch) in ms."  []
  (js/Date.now))

(defn ^:export get-target-attr
  "Takes in an HTML DOM event, and returns the value of the given
  attribute for the event's target element. Useful for listeners."
  [event attr]
  (-> (.-selectedTarget event)
      (.-attributes)
      (.getNamedItem attr)
      (.-value)))

(defn ^:export get-target-value
  "Takes in an HTML DOM event, and returns the value of the event's
  target element. Useful for listeners."
  [event]
  (.-value (.-selectedTarget event)))

(defn local-time-str
  "Takes in a timestamp in ms, and returns local time str (ie 1:02:23 PM)"
  [ms]
  (.toLocaleTimeString (js/Date. ms)))
