
(* CSEP505, Winter 2009, Homework 5 *)

(* No need to change anything in this file, though you might comment-out
   the interface for problem 2 while working on problem 1 or vice-versa *)

(* problem 1 *)
type acct 
val mkAcct : unit -> acct
val get : acct->float->float
val put : acct->float->float

(* problem 2 
type se_lock1
val new_selock1   : unit -> se_lock1
val shared_do1    : se_lock1 -> (unit -> 'a) -> 'a
val exclusive_do1 : se_lock1 -> (unit -> 'a) -> 'a

type se_lock2
val new_selock2   : unit -> se_lock2
val shared_do2    : se_lock2 -> (unit -> 'a) -> 'a
val exclusive_do2 : se_lock2 -> (unit -> 'a) -> 'a
*)

(* problem 4 (challenge problem) *)
(*
type barrier
val new_barrier : int -> barrier
val wait : barrier -> unit
*)
