(ns yggdrasil.core)

(defmulti handle-atom (fn [k _ _] k))

(defmethod handle-atom :default [_ _ _])
