(ns msmemer.core
    (:require [reagent.core :as reagent :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [antizer.reagent :as ant]
              [matchbox.core :as m]
              [matchbox.registry :as mr]
              [matchbox.async :as ma] 
              [cljs.core.async :as async]
              [cljsjs.moment]

              )) 

;;-------------------------
;;Firebase Authentication

(def prn-chan (async/chan))


(defn safe-prn [& msgs]
  (async/put! prn-chan msgs))




;;sets the url for the database we are accessing
(def base-uri "https://msmemr-e15f2.firebaseio.com")

;;connects to the database with a random integer as a key; we want to use specific integers (physician numbers)
;; Have everyone connect to a single top level key! 
(def r (m/connect base-uri "main"))

;;still not working.
;; Firebase has sec reules. Auth controls the scope of the data allowed by a patient
;;Store username and query for that specific persons data 




(def userinfo (reagent/atom {:name "Test" :message  "test2"}))
(def test-atom (reagent/atom 4))

(defn test-input []
  [:div
   [ant/input {:placeholder "Enter Username Here" :on-change #(swap! userinfo assoc :name (-> % .-target .-value))}]
   [ant/input {:placeholder "Enter Message here" :on-change #(swap!  userinfo assoc :message (-> % .-target .-value))}]])

(defn test-display []
  [:div
   [:p "Name is currently : " (@userinfo :name)]
   [:p "Message is currently: " (@userinfo :message)]
   ])

(defn fireform []
  (fn [props]
    (let [fireform (ant/get-form)]
      [ant/form {:onSubmit #() :layout "inline"}
       [ant/form-item {:label "Username"}
        [ant/input {:placeholder "Enter Username" :on-change #(swap! userinfo assoc :name (-> % .-target .-value))}]]
       [ant/form-item {:label "Password"}
        [ant/input {:placeholder "Enter Password" :on-change #(swap! userinfo assoc :message (-> % .-target .-value))}]]
       [ant/form-item {:wrapper-col {:offset 6}}
        ]
       [ant/form-item
        [ant/button  {:type "primary" :on-click #( (m/reset! r @userinfo))}  "Login"  ]   ]
       ]
      )))

 
;;button connected to firebase; will initiate and set a database entry to working
(defn firebutton []
  [:div
   [ant/button {:type "primary" :on-click #(m/reset! r "working")} "Set Working"]])

;;button connected to firebase; will reset the value of the database entry above to working again
(defn firebutton2 []
  [:div
   [ant/button {:type "primary" :on-click #(m/reset! r "Worked again!")} "Set to Working Again!"]])

(defn fireinput []
  [:div
   [ant/input {:placeholder "Enter Text Here" :onPressEnter #(m/reset! r (-> % .-target .-value))}]])

;;--------------------------
;; Components

(defn my-calendar []
  [ant/calendar {:fullscreen false :default-value (js/moment)}]
)

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

(defn side-menu []
   [ant/layout-content {:style { :position "relative" :margin-left "40%" :margin-right "38%"}}
    [ant/menu {:mode "horizontal" :theme "light" :style {:text-align "center"}}
     [ant/menu-item [:a {:href "/"} "Home"] ]
     [ant/menu-item [:a {:href "/login"} "Login"]]
     [ant/menu-item [:a {:href "/about"} "About"]]
     [ant/menu-item [:a {:href "/test"} "Test"]]
   ]]
   )


(defn title-banner []
  [:div
   [ant/layout
    [ant/layout-header {:class "banner" :style {:background "#999":text-align "center"}} "Perdix Medical Solutions"]
    [ant/layout-header {:class "banner" :style {:background "#999" :text-align "center"}} "Smarter Software. Happier Patients"]
   [side-menu]]])

;; -------------------------
;; Views

(defn home-page []
  [:div
     [title-banner]])
(defn about-page []
  [:div
   [title-banner]
   [:h1 {:style {:text-align "center"}} "About Perdix Medical Solutions"]
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
   [title-banner]
   [ant/layout 
   [ant/layout-content {:style {:margin-left "20%" :margin-right "20%"}}
    [:div  [:h1 {:style {:margin-top "5%":text-align "center"}} "Welcome! Lets get you logged in!"]]]

    [ant/layout-content {:style {:margin-left "35%" :margin-top "1%"}}
    [fireform]]]
    ]
  )

(defn test-page []
  [:div
   [title-banner]
   [fireform]
   [test-display]
   ])


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
