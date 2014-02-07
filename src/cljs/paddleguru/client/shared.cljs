(ns paddleguru.client.shared
  "Example of how to start rewriting the timing namespace."
  (:require [dommy.core :as dommy]
            [paddleguru.models.result :as result])
  (:use-macros [dommy.macros :only [sel sel1]]))

(def submit-button
  (sel1 "#results-button"))

(def alert
  (sel1 "div.alert"))

(defn show-alert []
  (dommy/remove-class! alert "hide"))

(defn hide-alert []
  (dommy/add-class! alert "hide"))

(defn disable-submit []
  (dommy/remove-class! submit-button :btn-primary)
  (dommy/add-class! submit-button :btn-danger)
  (dommy/set-text! submit-button "Fix Errors Before Submit")
  (dommy/set-attr! submit-button :disabled))

(defn enable-submit []
  (dommy/remove-class! submit-button :btn-danger)
  (dommy/add-class! submit-button :btn-primary)
  (dommy/set-text! submit-button "Publish Results")
  (dommy/remove-attr! submit-button :disabled))

(defn maybe-display-errors
  "Takes in results from the StopwatchState, and if it finds any
  errors, it disables the submit btuton and shows the alert div."
  [any-errors?]
  (if any-errors?
    (do (disable-submit)
        (show-alert))
    (do (enable-submit)
        (hide-alert))))
