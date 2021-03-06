(ns chess.test.ai.move-selection.min-max
  (:use [chess.core])
  (:require [chess.movelogic.protocol :only 
             (generate-moves test-check? move-piece checkmated? filter-non-check-moves) :as moves])
  (:use [chess.ai.move-selection.min-max 
         :only (min-max MAXRATING rate-board  build-tree )]
        [clojure.pprint]
        [clojure.test]))

(def checkmated-board "r1bqkb1r/pppp1Qpp/2n2n2/4p3/2B1P3/8/PPPP1PPP/RNB1K1NR b KQkq - 0 1")

(def checkmated-in-one-turn "r1bqkb1r/pppp1ppp/2n2n2/4p2Q/2B1P3/8/PPPP1PPP/RNB1K1NR w KQkq - 0 1")


(deftest test-min-max
  (are [x y z] (= x (min-max  (read-fen y) z))
       '((7 4) (5 6)) checkmated-in-one-turn 2
       '((7 4) (5 6)) checkmated-in-one-turn 1
       '((0 7) (2 5)) "B7/8/8/8/k5K1/2Q5/8/8 w - - 0 0" 1
       '((2 6) (6 6)) "5nkr/1QR3pp/5p2/8/1r2q3/8/3R2PP/B5K1 w - - 0 0" 1
       '((3 4) (4 6)) "5rk1/5pp1/8/R2N4/8/6K1/8/8 w - - 0 1" 3))

(deftest test-checkmated?
  (is (moves/checkmated? (read-fen checkmated-board )))
  (is (not(moves/checkmated? (initial-board)))))

(deftest test-rate-board
  (is (= MAXRATING (rate-board (read-fen checkmated-board) 1))))

(deftest test-build-tree
  (let [tree (build-tree (read-fen checkmated-in-one-turn) 2)]
    (is (= MAXRATING (:score tree)))))


(deftest test-filter-non-check-moves
  (let [game-state (read-fen "5rk1/4Npp1/8/R7/8/6K1/8/8 b - - 0 1")
        moves (moves/generate-moves game-state)]
    (is (= '(((6 7) (7 6)) ((6 7) (7 7))) (moves/filter-non-check-moves game-state moves true)))))
                                        ;(run-tests)