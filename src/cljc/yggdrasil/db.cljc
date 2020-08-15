(ns yggdrasil.db)

(defonce ^:dynamic db {})

#?(:clj
   (defonce server-db (atom {})))
