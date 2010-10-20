
exception TypeError
exception RunTimeError

type var = string

type typ =
    Int
  | Arrow  of typ * typ (* Arrow(t1,t2) means function from t1 to t2 *)

type env
type exp =
  | Const of int
  | Plus of exp * exp
  | Var of var
  | Lam of var * typ * exp
  | Apply of exp ref * exp
  | Closure of exp * env (* not in source programs *)

val interpret1 : exp -> exp

val typecheck2 : exp -> unit

val interpret2 : exp -> exp

val typecheck3 : exp -> (unit -> exp)

type checkedexp

val typecheck4 : exp -> checkedexp

val interpret4 : checkedexp -> exp
