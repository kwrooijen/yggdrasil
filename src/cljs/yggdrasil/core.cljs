(ns yggdrasil.core
  (:require
   [goog.crypt.base64 :as b64]
   [cljs.tagged-literals :refer [*cljs-data-readers*]]
   [clojure.tools.reader.edn :as edn]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom]
   [yggdrasil.db :refer [db]]
   [yggdrasil.util :refer [map-kv]]
   [yggdrasil.ws]))

(defn get-key [key]
  (let [el (js/document.querySelector (str "[" key "]"))
        result (-> (.-attributes el)
                   (aget key)
                   (.-value)
                   (b64/decodeString)
                   (->> (edn/read-string {:readers *cljs-data-readers*})))]
    (.remove el)
    result))

(defn add-atom-watcher [k a]
  (add-watch a :yggdrasil/watcher
             (fn [key atom old-state new-state]
               (yggdrasil.ws/chsk-send!
                [:yggdrasil/atom-reset!
                 {:atom/key k
                  :atom/value new-state}]))))

(defn values->atoms [values]
  (map-kv (fn [[k v]]
            (let [a (atom v)]
              (add-atom-watcher k a)
              [k a]))
          values))

(defn mount-root
  ([document-id html] (mount-root document-id html (fn [])))
  ([document-id html callback]
   (js/window.addEventListener
    "load"
    (fn []
      (let [atoms (get-key "yggdrasil-atoms")
            values (get-key "yggdrasil-values")]
        (set! db (merge (values->atoms atoms) values))
        (reagent.dom/render html (.getElementById js/document document-id))
        (callback))))))
