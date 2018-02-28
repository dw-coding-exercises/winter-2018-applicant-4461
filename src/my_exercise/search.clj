(ns my-exercise.search
  (:require [hiccup.page :refer [html5]]
            [hiccup.form :as hform]
            [hiccup.core :as hcore]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [clj-http.client :as client]
            [my-exercise.us-state :as us-state]))

(defn header [_]
  [:head
   [:meta {:charset "UTF-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1.0, maximum-scale=1.0"}]
   [:title "My next elections"]
   [:link {:rel "stylesheet" :href "default.css"}]])

; TODO - SECURE FORM?

(defn clean-placename [placename]
  (let [lower (clojure.string/lower-case placename)
        clean (clojure.string/replace lower #"\s" "_")]
    clean))

; TODO Handle nulls, and malicious code
(defn parse-ocd [params]
  (let [district {:country "us"
                  :state (clean-placename (:state params))       
                  :city (clean-placename (:city params))}
        state-ocd (str "ocd-division/country:"
                       (:country district)
                       "/state:"
                       (:state district))
        place-ocd (str state-ocd
                       "/place:"
                       (:city district))]
    (str "district-divisions=" state-ocd "," place-ocd)))

(defn get-elections [ocd-query]
   (client/get
    (str "https://api.turbovote.org/elections/upcoming?"
         ocd-query)
    {:accept :json}))

(defn parse-elections [elections]
  (for [election elections]
    (if (:body election)
      (hform/text-area (parse-election election)))))

(defn elections-div [request]
  (let [ocd-query (parse-ocd (:params request))
        elections (-> ocd-query get-elections parse-elections)]
    [:div {:class "elections-list"}
     [:h1 "Next elections"]
     (if (empty? elections)
       [:p "No upcoming elections found"]                      
       elections)
     ]))

(defn page [request]
  (html5
   (header request)
   (elections-div request)))

