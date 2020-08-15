(ns yggdrasil.util)

(defn map-kv [f m]
  (into {} (map f m)))

(defn map-v [f m]
  (into {} (map (fn [[k v]] [k (f v)]) m)))
