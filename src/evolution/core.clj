(ns evolution.core
  (:require [genotype-generator.gennadiy :as gena] ; генератор случайных генотипов
            [genotype-mutator.mutoslav :as mutya] ; мутатор генотипов
            [construct-creature.konstraktin :as kostya] ; создаёт изображение по генотипу
            [compare-genotypes.compare :as comp] ; сравнивает изображение с целевым
            [clojure.java.io :as io] ; работа с файлами
            [clojure.edn :as edn])) ; чтение/запись данных

;; Создаем папку для результатов
(def output-dir "evolution_results") ; имя папки
(io/make-parents (str output-dir "/placeholder")) ; создаём папку, если её нет

(defn move-to-output [file]
  (let [dest (str output-dir "/" (.getName (io/file file)))] ; новый путь
    (io/copy (io/file file) (io/file dest)) ; копируем файл
    (io/delete-file file) ; удаляем исходный
    dest)) ; возвращаем новый путь

(defn generate-images [genotypes iteration]
  (map-indexed (fn [idx genotype] ; для каждого генотипа
                 (let [temp-file "biomorph.png"
                       _ (kostya/-main (pr-str genotype)) ; создаём изображение
                       final-file (str "biomorph_" iteration "_" idx ".png") ; новое имя
                       moved-file (move-to-output temp-file) ; перемещаем в папку
                       renamed-file (str output-dir "/" final-file) ; полный путь
                       _ (.renameTo (io/file moved-file) (io/file renamed-file)) ; переименовываем
                       distance (comp/-main renamed-file "target.png")] ; сравниваем с целевым
                   {:genotype genotype ; возвращаем генотип,
                    :image renamed-file ; путь к изображению
                    :distance distance})) ; и расстояние до цели
               genotypes))

(defn find-best [evaluations]
  (let [best (apply min-key :distance evaluations)] ; выбираем вариант с минимальным расстоянием
    (println "\nРезультаты текущей итерации:")
    (doseq [{:keys [image distance]} (sort-by :distance evaluations)] ; сортируем по расстоянию
      (println (format "  %s - расстояние: %.3f" 
                      (.getName (io/file image)) 
                      distance)))
    best)) ; возвращаем лучший вариант

(defn -main [& args]
  (when (or (empty? args) (< (count args) 2)) ; проверяем аргументы
    (println "Как запустить: clojure -M -m evolution.core <num-genotypes> <max-stagnant-iterations>")
    (System/exit 1))
  
  (let [num-genotypes (Integer/parseInt (first args)) ; число генотипов на итерацию
        max-stagnant (Integer/parseInt (second args))] ; макс. итераций без улучшений
    (loop [iteration 0 ; номер итерации
           stagnant-count 0 ; счётчик итераций без улучшений
           current-genotypes (repeatedly num-genotypes gena/-main) ; начальные генотипы
           best-distance Double/MAX_VALUE ; лучшее расстояние
           best-genotype nil ; лучший генотип
           best-image nil ; лучшее изображение
           previous-distance Double/MAX_VALUE ; хранение предыдущего расстояния
           previous-genotype nil] ; и предыдущего генотипа
      
      ;; Выводим информацию о текущей итерации
      (println (format "\n=== Итерация %d === (Застой: %d/%d)" 
                      iteration stagnant-count max-stagnant))
      
      ;; Генерируем и оцениваем изображения
      (let [evaluations (generate-images current-genotypes iteration)
            {:keys [genotype image distance] :as best} (find-best evaluations)]
               
        ;; Выводим лучший результат итерации
        (println "\n🏆 Лучший в этой итерации:")
        (println (format "  Изображение: %s" (.getName (io/file image))))
        (println (format "  Расстояние: %.3f" distance))
        (println (format "  Генотип: %s" (pr-str genotype)))
        
        ;; Проверяем условия:
        (cond
        ;; 1. Нашли идеальное совпадение (расстояние = 0)
        (zero? distance)
        (do (println "\n🎉 УСПЕХ: найдено совпадение!")
            (println "   Изображение:" (.getName (io/file image)))
            (System/exit 0))
        
        ;; 2. Слишком долго нет улучшений
        (>= stagnant-count max-stagnant)
        (do (println (format "\n⛔ Провал: Нет улучшений за %d итераций" max-stagnant))
            (println "   Лучшее расстояние:" best-distance)  
            (println "   Лучшее изображение:" (when best-image (.getName (io/file best-image))))
            (System/exit 1))
        
        ;; 3. Нашли улучшение
        (< distance best-distance)
        (do (println (format "\n✨ Улучшение найдено! (Предыдущий лучший: %.3f)" best-distance))
            (recur (inc iteration) ; новая итерация
                    0 ; сбрасываем счётчик застоя
                    (repeatedly num-genotypes #(mutya/-main (pr-str genotype))) ; мутируем лучший генотип
                    distance  ; новое лучшее расстояние
                    genotype  ; новый лучший генотип
                    image   ; новое лучшее изображение
                    best-distance ; предыдущее лучшее расстояние
                    best-genotype ; и предыдущий лучший генотип
            ))

        ;; 4. Улучшений нет
        :else
;        (do (println "\n➡ Улучшений нет. Мутирую последний лучший генотип...")
;            (recur (inc iteration)
;                    (inc stagnant-count) ; увеличиваем счётчик застоя
;                    (repeatedly num-genotypes #(mutya/-main (pr-str best-genotype))) ; мутируем предыдущий лучший
;                    best-distance
;                    best-genotype
;                    best-image)))))))
        (let [best-of-two (if (< previous-distance distance)
        previous-genotype
        genotype)]
        (println (format "\n➡Улучшений нет. Мутирую лучший из 2 последних поколений: %.3f..."
        (min previous-distance distance)))
          (recur (inc iteration)
                  (inc stagnant-count)
                  (repeatedly num-genotypes #(mutya/-main (pr-str best-of-two)))
                  best-distance
                  best-genotype
                  best-image
                  distance ; текущее становится предыдущим
                  genotype))))))) ; текущий становится предыдущим