(ns yggdrasil.util.vector)

(defn insert [v i e]
  (vec (concat (take i v) [e] (drop i v))))
