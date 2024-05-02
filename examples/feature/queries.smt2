; start Z3 query
(declare-fun x0 () (Array (_ BitVec 32) (_ BitVec 8)))
(assert (not (= #x0a (select x0 #x00000000))))
(check-sat)
(reset)
; end Z3 query

; start Z3 query
(declare-fun x0 () (Array (_ BitVec 32) (_ BitVec 8)))
(assert (not (not (= #x0a (select x0 #x00000000)))))
(check-sat)
(reset)
; end Z3 query

; start Z3 query
(declare-fun x0 () (Array (_ BitVec 32) (_ BitVec 8)))
(assert (not (not (= #x0a (select x0 #x00000000)))))
(check-sat)
(reset)
; end Z3 query

; start Z3 query
(declare-fun x0 () (Array (_ BitVec 32) (_ BitVec 8)))
(assert (not (= #x0a (select x0 #x00000000))))
(check-sat)
(reset)
; end Z3 query


