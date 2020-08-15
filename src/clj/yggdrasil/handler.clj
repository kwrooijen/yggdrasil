(ns yggdrasil.handler
  (:require
   [buddy.core.codecs :as codecs]
   [buddy.core.codecs.base64 :as base64]
   [hiccup.core :as hiccup]
   [hiccup.util]
   [yggdrasil.db :refer [server-db]]
   [yggdrasil.reagent :refer [process-reagent]]
   [yggdrasil.util :refer [map-v]]))

(defn- values->atoms [values]
  (map-v atom values))

(defn- merge-atoms-values [body template page]
  {:html/atoms
   (merge (:template/atoms template)
          (:body/atoms body)
          (:page/atoms page))
   :html/values
   (merge (:template/values (meta template))
          (:body/values (meta body))
          (:page/values (meta page)))})

(defn- pre-process-fn [request v]
  (if (fn? v)
    (v request)
    v))

(defn- encode [s]
  (-> s
      (pr-str)
      (base64/encode)
      (codecs/bytes->str)))

(defn handler [request page-key {:keys [template body page]} ]
  (let [{:html/keys [atoms values]} (merge-atoms-values body template page)
        atoms (map-v (partial pre-process-fn request) atoms)
        values (map-v (partial pre-process-fn request) values)
        uid (str (java.util.UUID/randomUUID))]
    (with-bindings {#'yggdrasil.db/db (merge (values->atoms atoms) values)}
      (swap! server-db assoc uid (values->atoms atoms))
      {:status 200
       :headers {"Content-Type" "text/html"}
       :session (assoc (:session request) :uid uid)
       :body (hiccup/html
              (conj [:meta {:yggdrasil-page-key (encode page-key)}]
                    [:meta {:yggdrasil-atoms  (encode atoms)}]
                    [:meta {:yggdrasil-values (encode (merge values (select-keys request [:path-params])))}]
                    [:meta {:id "csrf" :data-csrf-token (:anti-forgery-token request)}]
                    (process-reagent ((:template/html template) ((:body/html body) (:page/html page))))))})))
