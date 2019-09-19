(ns app.client
  (:require
   [com.fulcrologic.fulcro-css.css-injection :as inj]
   [com.fulcrologic.fulcro-css.localized-dom :as ldom]
   [com.fulcrologic.fulcro.dom :as dom]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [goog.object :as gobj]
   [taoensso.timbre :as log]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.rendering.keyframe-render :as keyframe]
   [com.fulcrologic.fulcro.rendering.ident-optimized-render :as ior]))

(defonce app (app/fulcro-app {:optimized-render! ior/render!}))

(defn t []
  (doseq [n (range 1 10000)]
    (js/React.createElement "div" #js {} #js ["Hi"])))

(defn t2 []
  (doseq [n (range 1 10000)]
    (dom/div {:classes ["red"]} "dom")))

(defn t3 []
  (doseq [n (range 1 10000)]
    (ldom/div {:classes ["red"]} "ldom")))

(defn red-fn []
  {:classes ["red"]})

(defn t4 []
  (doseq [n (range 1 10000)]
    (ldom/div (ldom/div (red-fn) "ldom"))))


(comment
 (time (t))
 (time (t2))
 (time (t3))
 (time (t4))

 (clojure.core/defmacro
   div
   "Returns a React DOM element. Can be invoked in several ways

    These two are made equivalent at compile time
    (div \"hello\")
    (div nil \"hello\")

    These two are made equivalent at compile time
    (div {:onClick f} \"hello\")
    (div #js {:onClick f} \"hello\")

    There is also a shorthand for CSS id and class names
    (div :#the-id.klass.other-klass \"hello\")
    (div :#the-id.klass.other-klass {:onClick f} \"hello\")"
   [& args]
   (clojure.core/let
    [tag__14243__auto__ "div"]
     (try
       (com.fulcrologic.fulcro.dom/emit-tag tag__14243__auto__ args)
       (catch
        clojure.lang.ExceptionInfo
        e__14244__auto__
         (throw
          (clojure.core/ex-info
           (com.fulcrologic.fulcro.dom/syntax-error &form e__14244__auto__)
           (clojure.core/ex-data e__14244__auto__)))))))

 )

(defn should-component-update?
  [this next-props next-state]
  (let [current-props  (comp/props this)
        current-state  (comp/get-state this)
        props-changed? (not= current-props next-props)
        state-changed? (not= current-state next-state)]
    (log/debug {:current-props  current-props
                :next-props     next-props
                :props-changed? props-changed?
                :state-changed? state-changed?})
    (or props-changed? state-changed?)))

(defsc PersonName [this {:person-name/keys [name]}]
  ;{:shouldComponentUpdate should-component-update?}
  (dom/p "master " name))

(def ui-person-name (comp/factory PersonName))

(defsc Person [this {:person/keys [name age]}
               {:keys [inc-victories] :as c}]
  {:shouldComponentUpdate should-component-update?
   :ident                 (fn [] [:component/id :person])
   :query                 [:person/name :person/age]}
  [(dom/div {:classes ["red"]} "dom"
            (ui-person-name {:person-name/name name})
            (dom/p "age:" age)
            (dom/button {:onClick #(m/set-integer! this :person/age
                                                   :value (inc age))}
                        "klicka")
            (dom/button {:onClick inc-victories}
                        "victory!"))

   #_(ldom/div {:classes ["red"]} "ldom")])

(def ui-person (comp/factory Person))


(defn handlers [this & ks]
  (select-keys (comp/get-state this) ks))

(defsc Wrapper [this {:wrapper/keys [person victories]}]
  {:css-include    [Person]
   :ident          (fn [] [:component/id :wrapper])
   :initial-state  (fn [_]
                     {:wrapper/person    {:person/age  10
                                          :person/name "Joe"}
                      :wrapper/victories 1})
   :query          [:wrapper/victories
                    {:wrapper/person (comp/get-query Person)}]
   :initLocalState (fn [this _]
                     {:inc-victories
                      #(let [{:wrapper/keys [victories]} (comp/props this)]
                         (m/set-integer! this
                                         :wrapper/victories
                                         :value (inc victories)))})}
  (log/debug (comp/get-state this :inc-victories))
  [#_(inj/style-element {:component Root})
   (dom/div
    (ui-person (comp/computed
                person
                (handlers this :inc-victories)))
    (dom/p "victoriess: " victories))])

(def ui-wrapper (comp/factory Wrapper))

(defsc Root [this {:root/keys [wrapper]}]
  {:initial-state (fn [_] {:root/wrapper (comp/get-initial-state Wrapper)})
   :query         [{:root/wrapper (comp/get-query Wrapper)}]}
  (ui-wrapper wrapper))

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

