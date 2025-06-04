(ns compare-genotypes.compare
  (:require [clojure.math :as math])
  (:import 
           [java.awt.image BufferedImage PixelGrabber]
           [javax.imageio ImageIO]
           [java.io File]
))

(defn grab-pixels
  "Returns an array containing the pixel values of image."
  [image]
  (let [w (. image (getWidth))
        h (. image (getHeight))
        pixels (make-array (. Integer TYPE) (* w h))]
    (doto (new PixelGrabber image 0 0 w h pixels 0 w)
      (.grabPixels))
    pixels))


(defn get-euclid-distance  [generated target]
    (let [width  (.getWidth generated) height (.getHeight generated) result (atom 0)]
        (assert (= (.getWidth generated) (.getWidth target)) "Please use pictures of equal sizes")
        (assert (= (.getHeight generated) (.getHeight target)) "Please use pictures of equal sizes")
        (doseq [ x (range width) y (range height)]
            (swap! result + (math/pow (/ (- (.getRGB generated x y) (.getRGB target x y)) 16777215) 2))
        )               
        (math/sqrt @result)
    )
)

(defn read-image [filepath]
  (ImageIO/read (File. filepath)))


(defn -main [& args]
  (when (not= (count args) 2)
    (println "clojure -M -m compare-genotypes.compare result.png biomorph.png")
    (System/exit 1))

  (let [generated (read-image (str (first args)))
        target (read-image (str (second args)))]
    (println (get-euclid-distance  generated target))
    (System/exit 0)))