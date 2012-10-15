;just for now a copy of chess.move-selection
;
(ns chess.bitboard.move-selection
  (:use [chess.bitboard.api])
  (:use [chess.board-rating :only (rate)])
  (:use [clojure.java.io])
  (:use [clojure.pprint]))

(def MAXRATING 9999999)

(defn move2board [[piece pos1 pos2 promotion] game-state]
  (native-move-piece game-state piece pos1 pos2 promotion))

(defn moves2boards [moves game-state]
  "creates new game-states for the given boards"
  (map #(move2board % game-state) moves))

(defn pprint-move [[from to]]
  (let [chars (seq "abcdefgh")
        f (fn [[x y]] (str (nth chars x) (inc y))) ]
    (str (f from) "->" (f to))))

(defn whites-turn? [game-state]
  (= :w (:turn game-state)))

(defn change-turn [game-state]
  "changes the turn to the next player"
  (if (whites-turn? game-state)
    (assoc game-state :turn :b)
    (assoc game-state :turn :w)))

(defn checkmated?
  ([game-state]
   (let [new-boards (moves2boards (native-generate-moves game-state) game-state)]
      (checkmated? game-state new-boards)))
  ([game-state new-boards]
     (checkmated? game-state new-boards (check? game-state)))
  ([game-state new-boards is-check]
     (if (not is-check)
       false
       (every? check? new-boards))))

(defn checkmated-rating [ depth ]
   (if (= 0 (mod depth 2))
     (* -1 MAXRATING)
     MAXRATING))

(defn rate-board [game-state depth]
  (if (checkmated? game-state)
    (checkmated-rating depth)
    (rate game-state)))

(defn min-or-max
  ([c depth is-checkmated]
     (if is-checkmated
       (checkmated-rating depth)
       (min-or-max c depth)))
  ([c depth]
     (if (= 0 (mod depth 2))
       (apply max c)
       (apply min c))))

(defn filter-non-check-moves [game-state possible-moves is-check]
  (if is-check
    (filter #(not (check? (move2board % game-state))) possible-moves)
    possible-moves))

(defn build-tree
  ([game-state max-depth] (build-tree game-state 0 max-depth [] nil))
  ([game-state depth max-depth r step]
     (if (= depth max-depth)
       {:score (rate-board game-state depth) :game-state game-state :former-step step}
       (let [is-check (check? game-state)
             possible-moves (filter-non-check-moves game-state (native-generate-moves game-state) is-check)
             possible-states (moves2boards possible-moves game-state)
             is-checkmated (and is-check (empty? possible-moves))
             subtree  (if (not is-checkmated) (pmap #(build-tree (change-turn (move2board % game-state)) (inc depth) max-depth [] %) possible-moves) nil)
             rates    (flatten (map :score subtree))
             max-rate (min-or-max rates depth is-checkmated)
             rates2moves  (zipmap rates possible-moves)
             max-step (get rates2moves (first (filter #(= max-rate %) rates)))]
             { :score max-rate :max-step max-step :game-state game-state  :former-step step :tree subtree}))))

(defn trace-tree [game-state max-depth]
  (with-open [w (writer "tree.trace")]
    (pprint (build-tree game-state max-depth) w)))

(defn min-max [game-state max-depth]
  (:max-step (build-tree game-state max-depth)))

(defn select-move [game-state]
  (min-max game-state 2))