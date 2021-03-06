(ns chess.movelogic.bitboard.api
  (:require   [chess.movelogic.bitboard.moves
               :only  (generate-moves find-piece-moves check?)
               :as moves ])
  (:require [chess.movelogic.bitboard.chessboard
             :only (move-piece initial-board read-fen print-board) :as chessboard])
  (:require [chess.movelogic.protocol :as proto])
  (:use [chess.movelogic.bitboard bitoperations])
  (:use [chess.movelogic.bitboard.file-rank :only (lookup-file lookup-rank)]))

(require '[clojure.core.reducers :as r])
(import 'chess.movelogic.bitboard.chessboard.GameState)
(defn- coord->square [[file rank]]
  "in the moment our chess uses [7 7] as h8 bitboard internaly uses [8 8] as h8" 
  (- (+ (* 8 (inc rank)) (inc file)) 9))

(defn- square->coord [square]
  "in the moment our chess uses [7 7] as h8 bitboard internaly uses [8 8] as h8" 
  [(dec (aget ^ints lookup-file square)) (dec (aget ^ints lookup-rank square))])

(defn native->coords [col]
  (r/map (fn [sq]
           (square->coord sq)) col))

(defn pos-on-board? "checks if a positition is on the chess board"
  [[x y]]
  (every? (fn [n] (<= 0 n 7)) [x y]))

(defn- move-piece-coord 
  "move piece by coordinates from and dest" 
  [game-state from-coord dest-coord]
  {:pre [(pos-on-board? from-coord) (pos-on-board? dest-coord)]}
  (let [from-sq (coord->square from-coord)
        dest-sq (coord->square dest-coord)
        piece   (nth (:board game-state) from-sq)]
    (chessboard/move-piece game-state piece from-sq dest-sq)))

(defn- generate-moves-coord [game-state]
  (map (fn [[p from dest & data]]
         (list (square->coord from) (square->coord dest)))
       (moves/generate-moves game-state)))

(defn native-generate-moves [game-state]
  " returns moves in the form ([:P 56 57].....)"
  (moves/generate-moves game-state))


(defn native-move-piece [game-state piece from-sq dest-sq promotion]
  "move by squares"
  (chessboard/move-piece game-state piece from-sq dest-sq promotion))


(defn possible-moves [game-state coord]
  (let [square (coord->square coord)]
    (map  (fn[[_ _ dest]] (square->coord dest))
          (moves/possible-moves game-state (bit-set 0 square)))))

(defn piece-at [game-state coord]
  (get-in game-state [:board (coord->square coord) ]))

(defn check? [game-state]
  (true? (moves/check? game-state)))


(extend-type GameState 
  proto/MoveGenerator
  (generate-moves [this]
    (generate-moves-coord this))

  (possible-moves [this coord]
    (possible-moves this coord))

  (move-piece [this from to]
    (move-piece-coord this from to)) 

  (test-check? [this](check? this))

  (filter-positions-by-color [this white]
    (native->coords (if (and white true) 
                      (:whitepieces this) 
                      (:blackpieces this))))   

  (get-piece [this position]
    (piece-at this position))

  (print-board [this] 
    (chessboard/print-board this)))


(def initial-board chessboard/initial-board)

(defn read-fen [str]
  (chessboard/read-fen str))








