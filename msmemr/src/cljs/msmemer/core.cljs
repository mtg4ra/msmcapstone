(ns msmemer.core
    (:require [reagent.core :as reagent :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [antizer.reagent :as ant]
              [matchbox.core :as m]
              ))


;;--------------------------
;; Components
(def test-atom (atom 0))
(def root (m/connect "https://msmemr-e15f2.firebaseapp.com"))
(defn actual-form []
  (fn [props]
    (let [my-form (ant/get-form)]
      [ant/form
       [ant/form-item {:label "Name"}
        (ant/decorate-field my-form "name" {:rules [{:required true}]}
                            [ant/input])]
       [ant/form-item {:label "Password"}
        ;; validates that the password field is not empty
        (ant/decorate-field my-form "password" {:rules [{:required true}]}
                            [ant/input])]])))


;;Name card takes a name, the text for the card. 
(defn name-card [name text]
  [ant/card {:title name}
   text
   ])
;;Name-card-pic takes the name, text, and picture url of a card
(defn name-card-pic [name text pic-url]
  [ant/card {:title name}
   text
   [:div [:img {:width "80%" :height "80%":src pic-url}]]])

;; -------------------------
;; Views

(defn home-page []
  [:div [:h1 {:style {:text-align "center" :color "blue"}}"Welcome to Perdix Medical Solutions" ]

   [:div @test-atom]
   [ant/button {:type "primary" :on-click (fn [] (swap! test-atom inc)) } "Increase Counter"]
   [ant/button {:type "primary" :on-click #(reset! test-atom 0)} "Reset counter"]
   [:div [:a {:href "/login"} "Go to Login Page"]]
   [:div [:a {:href "/about"} "go to About Page"]]
   [:div [:a {:href "/test"} "Go to Test Page"]]
   ])

(defn about-page []
  [:div [:h1 {:style {:text-align "center"}} "About msmemer"]
   [:div [:a {:href "/"} "go to the home page"]]
     [name-card "Perdix Medical Solutions" "Perdix Medical Solutions is a Charlottesville Startup focusing on improving patinet-physician relationships thorugh better, smarter software" ]
   [:div
   [ant/row {:type "flex" :justify "top" :gutter 0}
    [ant/col {:span 8}
     [name-card-pic "Mitchell Gillin, CEO" "Mitch Gillin is a 4th year Biomedical Engineering student with a passion for human centered design and healthcare software. Mitch also enjoys cooking, golfing, and the occasional cold, refreshing beverage" "https://media-exp1.licdn.com/mpr/mpr/shrinknp_400_400/AAEAAQAAAAAAAAwIAAAAJDAxNjFlYWVlLTFjZmMtNGM1YS1iYTNlLTE1OWZlODRkYTRiOQ.jpg"]]
    [ant/col {:span 8}
     [name-card-pic "Sean Rouffa" "Sean Rouffa is a 4th year Biomedical Engineering student who enjoys water polo and patient-focused design." "https://media-exp1.licdn.com/media/AAEAAQAAAAAAAAczAAAAJGE2M2NhNzg2LWY0MGEtNDU2ZC1iNGNlLWJmYjM1YjlkYjk4Mg.jpg"]]
    [ant/col {:span 8}
    [name-card-pic "Matthew Zetkulic" "Matthew is a 4th year Biomedical Engineer who excels at database management and rowing" "https://media-exp1.licdn.com/media/AAEAAQAAAAAAAA23AAAAJDcyN2IxMGI3LThlM2UtNDVmYy05Mzk5LTUyYzI2ZGVjZTg0NQ.jpg"]]
    ]]
   ]
   )

(defn login-page []
  [:div
   [:div  [:h1 {:style {:align "right"}} "Welcome! Lets get you logged in!"]]
   [:div (ant/create-form (actual-form))]
  [:div [:a {:href "/"} "Home Page"]]
  [:div [:a {:href "/about"} "About Page"]]]
  )

(defn test-page []
  [:div
   [:div (ant/create-form (actual-form))]])


;; -------------------------
;; Routes

(def page (atom #'home-page))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
  (reset! page #'home-page))

(secretary/defroute "/about" []
  (reset! page #'about-page))

(secretary/defroute "/login" []
  (reset! page #'login-page))

(secretary/defroute "/test" []
  (reset! page #'test-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
