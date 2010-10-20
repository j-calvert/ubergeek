
type env (* abstract type ensures no client makes a Closure manually *)

type exp = Var of string 
         | Lam of string * exp * (string list option)
	 | Apply of exp * exp
	 | Closure of string * exp * env
	 | Int of int
	 | Plus of exp * exp
	 | Iszero of exp
	 | True
	 | False
	 | If of exp * exp * exp
	 | Pair of exp * exp
	 | First of exp
	 | Second of exp

val interp1 : exp -> exp

val runtests : unit -> unit
