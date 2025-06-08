(ns construct-creature.konstraktin
  (:require [clojure.edn :as edn])
  (:import [java.awt Color Graphics2D BasicStroke RenderingHints]
           [java.awt.image BufferedImage]
           [javax.imageio ImageIO]
           [java.io File]
           [java.lang System]))

;; Константы
(def image-size 150)
(def center (/ image-size 2))
(def max-genes 15)
(def total-genes 16)
(def line-width 1)
(def line-color Color/BLACK)
(def background-color Color/WHITE) 

(defn select-random-genes [genes count]
  (let [indices (take count (shuffle (range max-genes)))]
    (map #(nth genes %) indices)))

;; Расчет направлений роста 
(defn calculate-stems [genes]
  (let [selected (select-random-genes genes 7)
        g (fn [i] (nth selected (mod i (count selected))))]
    [{:dx 0      :dy (g 0)}    ; Вверх
     {:dx (g 1)  :dy (g 2)}    ; Вверх-вправо
     {:dx (g 3)  :dy 0}        ; Вправо
     {:dx (g 4)  :dy (- (g 5))} ; Вниз-вправо
     {:dx 0      :dy (- (g 6))} ; Вниз
     {:dx (- (g 4)) :dy (- (g 5))} ; Вниз-влево
     {:dx (- (g 3)) :dy 0}        ; Влево
     {:dx (- (g 1)) :dy (g 2)}])) ; Вверх-влево


;; Построение сегментов
(defn render-segments [length stems dir {:keys [x y]}]
  (when (pos? length)
    (let [new-dir (mod dir 8)
          {:keys [dx dy]} (nth stems new-dir)
          new-x (+ x (* length dx))
          new-y (+ y (* length dy))
          segment {:start {:x x :y y} :finish {:x new-x :y new-y}}]
      (concat [segment]
              (render-segments (dec length) stems (inc dir) {:x new-x :y new-y})
              (render-segments (dec length) stems (dec dir) {:x new-x :y new-y})))))

;; Расчет границ фигуры
(defn calculate-bounds [segments]
  (reduce (fn [{:keys [min-x min-y max-x max-y]} {:keys [start finish]}]
            {:min-x (min min-x (:x start) (:x finish))
             :min-y (min min-y (:y start) (:y finish))
             :max-x (max max-x (:x start) (:x finish))
             :max-y (max max-y (:y start) (:y finish))})
          {:min-x 0 :min-y 0 :max-x 0 :max-y 0}
          segments))

;; Отрисовка биоморфы
(defn draw-biomorph [segments]
  (let [bounds (calculate-bounds segments)
        size (max (- (:max-x bounds) (:min-x bounds))
                  (- (:max-y bounds) (:min-y bounds)))
        scale (if (zero? size) 1.0 (/ 60 (max 1 size)))
        image (BufferedImage. image-size image-size BufferedImage/TYPE_INT_RGB)
        g (.createGraphics image)]
    
    (.setColor g background-color)
    (.fillRect g 0 0 image-size image-size)
    (.setRenderingHint g RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_OFF)
    (.setRenderingHint g RenderingHints/KEY_STROKE_CONTROL RenderingHints/VALUE_STROKE_PURE)
    (.setStroke g (BasicStroke. line-width BasicStroke/CAP_BUTT BasicStroke/JOIN_MITER))
    (.setColor g line-color)
    (.translate g center center)
    (.scale g scale scale)
    
    ;; Рисуем все сегменты
    (doseq [{:keys [start finish]} (remove nil? segments)]
      (let [x1 (int (:x start))
            y1 (int (- (:y start)))
            x2 (int (:x finish))
            y2 (int (- (:y finish)))]
        ;; Рисуем линию дважды для лучшей четкости
        (.drawLine g x1 y1 x2 y2)
        (.drawLine g x1 y1 x2 y2)))
    
    (.dispose g)
    image))

;; Генерация изображения из генотипа
(defn genotype-to-image [genotype]
  (let [stems (calculate-stems genotype)
        length (last genotype)
        segments (render-segments length stems 0 {:x 0 :y 0})]
    (draw-biomorph segments)))

;; Сохранение изображения
(defn save-image [image filename]
  (ImageIO/write image "PNG" (File. filename)))

(defn -main [& args]
  (when (not= (count args) 1)
    (println "Использование: clojure -M -m construct-creature.konstraktin \"[генотип]\"")
    (System/exit 1))
  
  (let [genotype (edn/read-string (first args))
        image (genotype-to-image genotype)]
    (save-image image "biomorph.png")
    (println "Генотип:" genotype)
    (println "Биоморфа сохранена в biomorph.png")
;    (System/exit 0)
))