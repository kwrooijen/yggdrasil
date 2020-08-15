(ns yggdrasil.sente
  (:require
   [taoensso.sente :as sente]
   [taoensso.sente.server-adapters.aleph :refer [get-sch-adapter]]
   [yggdrasil.db :refer [server-db]]
   [yggdrasil.core :as y]
   [yggdrasil.util :refer [map-kv]]))

(let [chsk-server (sente/make-channel-socket-server!
                   (get-sch-adapter)
                   {:packer :edn
                    :user-id-fn (comp :uid :session)})
      {:keys [ch-recv send-fn connected-uids ajax-post-fn ajax-get-or-ws-handshake-fn]} chsk-server]
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv)
  (def chsk-send!                    send-fn)
  (def connected-uids                connected-uids))

(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id)

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (println "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-server event}))))

(defmethod -event-msg-handler :chsk/ws-ping [_])

(defmethod -event-msg-handler
  :some/request-id
  [{:as ev-msg :keys [event uid id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)]
    (when ?reply-fn
      (?reply-fn true))))

(defmethod -event-msg-handler
  :chsk/uidport-close
  [{:keys [event uid id ring-req]}]
  (let [session (:session ring-req)]
    (swap! server-db dissoc uid)))

(defmethod -event-msg-handler
  :chsk/uidport-open
  [{:keys [uid]}])

(defmethod -event-msg-handler
  :yggdrasil/atom-reset!
  [{:keys [uid event ring-req send-fn]}]
  (let [[_ {:atom/keys [key value]}] event]
    (reset! (get-in @server-db [uid key]) value)
    (let [db (map-kv (fn [[k v]] [k @v]) (get @server-db uid))]
      (y/handle-atom key ring-req (get @server-db uid))
      (doseq [[k v] (get @server-db uid)]
        (when-not (= @v (get db k))
          (chsk-send! uid [:atom/reset! {:atom/key k :atom/value @v}]))))))

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg))

(defn start-router []
  (sente/start-server-chsk-router! ch-chsk event-msg-handler))

(defn handler-get [request]
  (ring-ajax-get-or-ws-handshake request))

(defn handler-post [request]
  (ring-ajax-post request))
