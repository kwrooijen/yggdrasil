(ns yggdrasil.ws
  (:require
   [taoensso.sente  :as sente]
   [yggdrasil.db :refer [db]]))

(def ?csrf-token
  (.getAttribute (js/document.querySelector "[data-csrf-token]")
                 "data-csrf-token"))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket-client! "/chsk" ?csrf-token)]
  (def chsk       chsk)
  (def ch-chsk    ch-recv)
  (def chsk-send! send-fn)
  (def chsk-state state))

(defmulti -event-msg-handler :id)

(defn event-msg-handler [ev-msg] (-event-msg-handler ev-msg))

(defmethod -event-msg-handler
  :default
  [{:as ev-msg :keys [event]}]
  (println "Unhandled event: %s" event))

(defmethod -event-msg-handler :chsk/recv
  [{:as ev-msg :keys [?data]}]
  (let [[event-type {:atom/keys [key value]}] ?data]
    (cond
      (#{:atom/reset!} event-type)
      (reset! (get db key) value)
      :else nil)))

(defmethod -event-msg-handler :chsk/handshake [_])

(defmethod -event-msg-handler :chsk/state [_])

(defonce router_ (atom nil))

(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))

(defn start-router! []
  (stop-router!)
  (reset! router_
    (sente/start-client-chsk-router!
      ch-chsk event-msg-handler)))

(defn start! [] (start-router!))

(defonce _start-once (start!))
