(ns cotm.core
  (require [lanterna.screen :as s]
           [clojure.data.json :as json])
  (:gen-class))

(def cmd-file "resources/commands.json")

(defn pause [time]
  (Thread/sleep time))

(defn get-active-commands [cmds]
  ; cmds is a map, iterate over [command, isActive] pairs
  (keys (filter second cmds))) 

(defn draw-text [scr x y text]
  (doto scr
    (s/clear)
    (s/put-string x y text)
    (s/redraw)))

; draw 'text' to a random point on the screen, making sure 
; the text won't run off the end
(defn draw-random [scr text]
  (let [[x y] (map #(rand-int (- % (count text))) 
                   (s/get-size scr))]
    (draw-text scr x y text)
    [x y]))

; flash a series of random commands on the screen in various positions
(defn animate-random [scr cmds]
  (dotimes [n (+ 15 (rand-int 5))] 
    (draw-random scr (rand-nth cmds))
    (pause (* n 50))))

; given 'from' and 'to' points (each a vector of [x y]), generate 
; a list of consecutive points between 'from' and 'to'
(defn generate-path [from to]
  (let [move-one (fn [to from] 
                   ; abuse the fact that compare returns -1, 0, 1 for lt, eq, gt respectively
                   (map (fn [f t] (+ f (compare t f))) from to))
        not-finished? (partial not= to)]
    (take-while not-finished?
                (iterate (partial move-one to) from))))

(defn get-centered [scr text]
  (let [[sx sy] (s/get-size scr)
        cx (int (/ (- sx (count text)) 2))
        cy (int (/ sy 2))]
    [cx cy]))

; place the text on the screen in a random position, then 
; animate it to the center of the screen
(defn animate-random-to-center [scr text]
  (let [[cx cy] (get-centered scr text)]
    (doseq [[x y] (generate-path (draw-random scr text) [cx cy])]
      (draw-text scr x y text)
      (pause 80))
    (draw-text scr cx cy text)))

(defn flash [scr text times]
  (let [[cx cy] (get-centered scr text)]
    (dotimes [n times]
      (s/clear scr) (s/redraw scr)
      (pause 400)
      (s/put-string scr cx cy text) (s/redraw scr) 
      (pause 400))))

(defn -main [& args]
  (let [scr (s/get-screen)
        cmds (json/read-str (slurp cmd-file))
        actives (get-active-commands cmds)
        chosen (rand-nth actives)]
    (s/in-screen scr
                 (animate-random scr (remove #{chosen} actives))
                 (animate-random-to-center scr chosen)
                 (flash scr chosen 3)
                 (spit cmd-file (json/write-str (assoc cmds chosen false)))
                 (s/get-key-blocking scr))))
