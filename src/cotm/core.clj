(ns cotm.core
  (require [lanterna.screen :as s]
           [clojure.data.json :as json]))

(defn pause [time]
  (Thread/sleep time))

(defn get-active-commands [cmds]
  ; cmds is a map, iterate over [command, isActive] pairs
  (keys (filter second cmds))) 

(defn draw-text [scr x y text]
  (s/clear scr)
  (s/put-string scr x y text)
  (s/redraw scr))

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
    (pause 400)))

; given two [x y] points ('to' and 'from'), return an [x y] point that is
; one coordinate closer to the 'to' point than the 'from' point
; parameter order is reverse from expected for partial application convenience
(defn delta-move [to from]
  ; abuse the fact that compare returns -1, 0, 1 for lt, eq, gt
  (map (fn [p1 p2] (+ p1 (compare p2 p1))) from to))

; given 'from' and 'to' points, generate a list of points between 'from' and 'to'
(defn generate-path [from to]
  (take-while (partial not= to)
              (iterate (partial delta-move to) from)))

; place the text on the screen in a random position, then 
; animate it to the center of the screen
(defn animate-random-to-center [scr text]
  (let [[sx sy] (s/get-size scr)
        cx (int (/ (- sx (count text)) 2))
        cy (int (/ sy 2))]
  (doseq [[x y] (generate-path (draw-random scr text) [cx cy])]
    (draw-text scr x y text)
    (pause 100))
  (draw-text scr cx cy text)))

(defn -main [& args]
  (let [scr (s/get-screen)
        cmds (json/read-str (slurp "resources/commands.json"))
        actives (get-active-commands cmds)
        chosen (rand-nth actives)]
    (s/in-screen scr
                 (animate-random scr (remove #{chosen} actives))
                 (animate-random-to-center scr chosen)
                 (s/get-key-blocking scr))))
