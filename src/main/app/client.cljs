(ns app.client
  (:require
   [com.fulcrologic.fulcro-css.css-injection :as inj]
   [com.fulcrologic.fulcro-css.localized-dom :as dom]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]))

(defonce app (app/fulcro-app))

(defsc Person [this {:person/keys [name age]}]
  {:css [[:.red {:color "red"}]]}
  (dom/div :.red
   (dom/p "Name: " name)
   (dom/p "Age: " age)
           (dom/ul (for [i (range 1 100)]
                     (dom/li (str "Index " i))))))

(def ui-person (comp/factory Person))

(defsc Root [this props]
  {:css-include [Person]
   :query []}
  (js/console.log (inj/compute-css {:component Root}))
  [(inj/style-element {:component Root})
   (dom/div
    (ui-person {:person/name "Joe" :person/age 22}))])

(defn ^:export init
  "Shadow-cljs sets this up to be our entry-point function. See shadow-cljs.edn `:init-fn` in the modules of the main build."
  []
  (app/mount! app Root "app")
  (js/console.log "Loaded"))

(defn ^:export refresh
  "During development, shadow-cljs will call this on every hot reload of source. See shadow-cljs.edn"
  []
  ;; re-mounting will cause forced UI refresh, update internals, etc.
  (app/mount! app Root "app")
  (js/console.log "Hot reload"))
