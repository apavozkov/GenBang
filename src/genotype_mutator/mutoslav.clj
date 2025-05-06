(ns genotype-mutator.mutoslav
  (:require [clojure.edn :as edn]))

(defn read-constraints [filename]
  (edn/read-string (slurp filename)))

(defn- clamp [value [min-val max-val]]
  (cond
    (< value min-val) min-val
    (> value max-val) max-val
    :else value))

(defn- mutate-value [current-value constraints]
  (let [delta (if (zero? (rand-int 2)) -1 1)
        new-value (+ current-value delta)]
    (clamp new-value constraints)))

(defn mutate-genotype [genotype constraints]
  (let [index (rand-int (count genotype))
        constraints-pair (nth constraints index)]
    (update genotype index #(mutate-value % constraints-pair))))

(defn -main [& args]
  (when (not= (count args) 1)
    (println "Ошибка: передайте генотип в виде EDN-строки (например, \"[0 1 2 ... 15]\")")
    (System/exit 1))

  (let [genotype-str (first args)
        genotype (edn/read-string genotype-str)
        constraints (read-constraints "limits.edn")]

    (when (not (and (= (count genotype) 16)
                    (= (count constraints) 16)))
      (println "Ошибка: генотип и ограничения должны содержать ровно 16 элементов")
      (System/exit 1))

    (let [mutated (mutate-genotype genotype constraints)]
      (println mutated)
      mutated)))
