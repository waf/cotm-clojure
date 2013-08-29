(ns cotm.core
  (require [lanterna.screen :as s]
           [clojure.data.json :as json]))

(defn draw-random [scr text]
  (let [[x y] (map #(rand-int %) (s/get-size scr))]
    (s/clear scr)
    (s/put-string scr x y text)
    (s/redraw scr)))
  
(defn get-commands [path]
  (json/read-str (slurp path)))

(defn get-active-commands [cmds]
  (keys (filter second cmds)))

(defn cycle-commands [scr cmds]
  (dotimes [n 15] 
    (draw-random scr (rand-nth cmds))
    (Thread/sleep 400)))

(defn -main [& args]
  (let [scr (s/get-screen :text)
        cmds (get-commands "resources/commands.json")
        actives (get-active-commands cmds)
        chosen (rand-nth actives)]
    (s/in-screen scr
                 (cycle-commands scr (remove #{chosen} actives))
                 (draw-random scr chosen)
                 (s/get-key-blocking scr))))
