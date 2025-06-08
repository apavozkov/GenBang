(ns genotype-generator.gennadiy
  (:require [clojure.edn :as edn]))

(defn read-constraints [filename]
  (try
    (-> (slurp filename)
        (edn/read-string))
    (catch Exception e
      (println "Ошибка чтения файла:" (.getMessage e))
      (System/exit 1))))

(defn generate-number [[min max]]
  (+ min (rand-int (inc (- max min)))))

(defn -main []
  (let [constraints (read-constraints "limits.edn")
      genotype (vec (map generate-number constraints))]
    (when (not= 16 (count constraints))
      (println "Ошибка: требуется ровно 16 пар чисел")
      (System/exit 1))
    (println genotype) ; вывод генотипа
    genotype)) ; возврат значения 
