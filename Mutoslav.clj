(ns mutate-genotype
  (:require [clojure.edn :as edn])
  (:gen-class))

(defn- read-genotype [s] (edn/read-string s))
(defn- load-limits [] (edn/read-string (slurp "limits.edn")))

(defn- random-index [genotype] (rand-int (count genotype)))
(defn- mutation-delta [] (rand-nth [-1 1]))

(defn- clamp [v min max] (-> v (max min) (min max))

(defn- mutate [genotype limits]
  (let [idx (random-index genotype)
        [min max] (nth limits idx)
        delta (mutation-delta)
        current (nth genotype idx)
        new-val (+ current delta)]
    (assoc genotype idx (clamp new-val min max))))

(defn -main [& args]
  (when (empty? args)
    (println "Usage: clojure -J-Dclojure.main.report=stderr -Sdeps '{:deps {org.clojure/clojure {:mvn/version \"1.11.1\"}}}' -M -m mutate-genotype \"[your vector]\"")
    (System/exit 1))
  
  (let [genotype (try (read-genotype (first args))
        limits (load-limits)]
    (when (not= (count genotype) (count limits))
      (println "Genotype and limits length mismatch")
      (System/exit 1))
    (println (mutate genotype limits))))

(System/exit 0)
