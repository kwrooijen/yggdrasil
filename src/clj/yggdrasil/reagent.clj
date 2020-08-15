(ns yggdrasil.reagent)

(defmulti process-reagent
  (fn [v]
    (cond
      (fn? v)              :reagent/component
      (not (coll? v))      :reagent/value
      (keyword? (first v)) :reagent/element
      (fn? (first v))      :reagent/fn
      (coll? (first v))    :reagent/collection)))

(defmethod process-reagent :reagent/component [v]
  (process-reagent (v)))

(defmethod process-reagent :reagent/value [v]
  v)

(defmethod process-reagent :reagent/fn [[f & xs]]
  (process-reagent (apply f xs)))

(defmethod process-reagent :reagent/element [[tag & xs]]
  (let [[opts body] (if (map? (first xs))
                      [(first xs) (rest xs)]
                      [{} xs])]
    (->> body
         (map process-reagent)
         (into [tag opts]))))

(defmethod process-reagent :reagent/collection [v]
  (map process-reagent v))

(defmethod process-reagent nil [_]
  nil)
