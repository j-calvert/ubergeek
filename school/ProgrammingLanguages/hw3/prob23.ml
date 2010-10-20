(****** Note: Problem 1 does not use Caml; see the assignment *****)
(****** Note: Problem 4 is in another file; see the assignment ****)

exception Unimplemented
exception RuntimeTypeError
exception RuntimeTypeError2
exception BadSourceProgram
exception BadPrecomputation

(* a simple string-set library (the Caml library has a fancier way) *)
(* no changes necessary -- use in problem 3 *)

let empty_set = []
let add str lst = if List.mem str lst then lst else str::lst
let remove str lst = List.filter (fun x -> x <> str) lst
let rec union lst1 lst2 = 
   match lst1 with
     [] -> lst2
   | hd::tl -> add hd (union tl lst2)

(* abstract syntax for language manipulated in this file *)

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

and env = (string * exp) list

(******* Problem 2: complete this function *********)
let rec interp f env e =
  let interp = interp f in
  match e with
    Var s -> List.assoc s env (* do the lookup *)
  | Lam(s,e2,opt) -> Closure(s,e2,f env opt) (*store env!*) 
  | Closure _ -> e (* closures are values *)
  | Apply(e1,e2) ->
      let v1 = interp env e1 in
      let v2 = interp env e2 in
      (match v1 with
	      Closure(s,e3,env2) -> interp((s,v2)::env2) e3
        | _ -> raise RuntimeTypeError)
  | Plus(e1,e2) ->
      let v1 = interp env e1 in
      let v2 = interp env e2 in
      (match v1 with 
        Int i1 ->
          (match v2 with
            Int i2 -> Int(i1 + i2)
            | _ -> raise RuntimeTypeError2)
        | _ -> raise RuntimeTypeError2)
  | Iszero e1 ->
     let v1 = interp env e1 in
      (match v1 with 
        Int i1 -> if i1 = 0 then True else False
        | _ -> raise RuntimeTypeError)
  | If(c,t,fl) -> 
     let c1 = interp env c in
       (match c1 with 
        True -> interp env t
        | False -> interp env fl
        | _ -> raise RuntimeTypeError)
  | Pair(e1, e2) -> Pair(interp env e1, interp env e2)
  | First e1 ->
     let v = interp env e1 in
     (match v with 
      Pair(a, b) -> interp env a
      | _ -> raise RuntimeTypeError2)
  | Second e1 ->
     let v = interp env e1 in
     (match v with 
      Pair(a, b) -> interp env b
      | _ -> raise RuntimeTypeError)
  | _ -> e (* Int, True, and False are expressions which are also values that are not interpreted (in addition to closures) *)
	
let interp1 = interp (fun x _ -> x) []

(****** Problem 3: complete this function *******)
(****** Problem 3 also has some English parts; see the assignment *****)
let rec computeFreeVars e = 
  match e with
    Var str -> (e, str::[])
    | Lam(s,e2,opt) -> let (e3, lst) = computeFreeVars e2 in
      let llst = remove s lst in
      (Lam(s, e3, Some(llst)), llst)
    | Closure(s,e2,env) -> let(e3, lst) = computeFreeVars e2 in
      (Closure(s,e3,env), remove s lst) (* maybe the remove is redundant, since we remove the bounded var in the Lam case...but no harm *)
    | Apply(e1, e2) -> let(e3,lst3) = computeFreeVars e1 in
      let (e4,lst4) = computeFreeVars e2 in
      (Apply(e3,e4), union lst3 lst4)
    | Plus(e1, e2) -> let(e3,lst3) = computeFreeVars e1 in
      let (e4,lst4) = computeFreeVars e2 in
      (Plus(e3,e4), union lst3 lst4)
    | Iszero e1 -> let(e2, lst) = computeFreeVars e1 in
      (Iszero e2, lst)
    | If(e1,e2,e3) -> let(e4,lst4) = computeFreeVars e1 in
      let (e5,lst5) = computeFreeVars e2 in
      let (e6,lst6) = computeFreeVars e3 in
      (If(e4,e5,e6), union (union lst4 lst5) lst6)
    | Pair(e1, e2) -> let(e3,lst3) = computeFreeVars e1 in
      let (e4,lst4) = computeFreeVars e2 in
      (Pair(e3,e4), union lst3 lst4)
    | First(e1) -> let (e2, lst) = computeFreeVars e1 in
      (First(e2), lst)
    | Second(e1) -> let (e2, lst) = computeFreeVars e1 in
      (Second(e2), lst)
    | _ -> (e, []) (* No free vars or recursion needed for Int, True, and False *)

let interp2 = interp (fun (env:env) opt ->
  match opt with
     None -> raise BadPrecomputation
   | Some lst -> List.map (fun s -> (s, List.assoc s env)) lst)
  []

(******* Problem 5a: explain this function *********)

let interp3 = interp (fun (env : env) _ -> []) []

(****** Problem 5b (challenge problem): explain the next two functions ******)

let rec depthToExp s varlist exp =
   match varlist with
    [] -> raise BadSourceProgram
  | hd::tl -> if s=hd then First exp else depthToExp s tl (Second exp)
   
let rec translate varlist exp = 
   match exp with
   Var s -> depthToExp s varlist (Var "arg")
 | Lam(s,e2,_) -> Pair(Lam("arg",translate (s::varlist) e2, None), 
                       match varlist with [] -> Int 0 | _ -> Var "arg")
 | Closure _ -> raise BadSourceProgram
 | Apply(e1,e2) -> 
   let e1' = translate varlist e1 in
   let e2' = translate varlist e2 in
   (* hint: let f = (e1',e2') in f.1.1 (f.2, f.1.2) *)
   Apply(Lam("f",Apply(First(First(Var "f")), 
                       Pair(Second(Var "f"),Second(First(Var "f")))),None),
         Pair(e1',e2'))
 | Int _ -> exp
 | True  -> exp
 | False -> exp
 | Pair(e1,e2) -> Pair(translate varlist e1, translate varlist e2)
 | Plus(e1,e2) -> Plus(translate varlist e1, translate varlist e2)
 | First e1 -> First(translate varlist e1)
 | Second e1 -> Second(translate varlist e1)
 | Iszero e1 -> Iszero(translate varlist e1)
 | If(e1,e2,e3) -> If(translate varlist e1,
                      translate varlist e2,
                      translate varlist e3)

(********** examples and testing ***********)

(* no need to change anything here:
  * we have provided 2 tests 
  * you may wish to write code to investigate intermediate results 
    (such as the result of computeFreeVars) 
 *)

(* first test -- simple use of currying to add 17 and 19 *)
let ex1 = (Apply(Apply(Lam("x",Lam("y", Plus(Var"x",Var "y"),None),None), 
		      Int 17),
		Int 19))

 
(* second test -- uses fix to write a recursive function that is used to
   sum the numbers from 1 to 1000 *)

(* first some helper metafunctions to make the program easier to write *)
let lam x e = Lam(x,e,None)
let app e1 e2 = Apply(e1,e2)
let vx = Var "x"
let vy = Var "y"
let vf = Var "f"

(* now fix as defined in class *)
let fix = 
   let e = lam "x" (app vf (lam "y" (app (app vx vx) vy))) in
   lam "f" (app e e)

(* now sum, written so it can be an argument to fix *)
let sum = 
  lam "f" (lam "x" (If(Iszero vx,
		       Int 0,
		       Plus(vx, app vf (Plus(vx, Int (-1)))))))

(* now the whole program -- the recursive function applied to 1000 *)
let ex2 = (app (app fix sum) (Int 1000))

(* now testing -- ex1 evaluates to 36, ex2 to 500500 *)

let ans1 = interp1 ex1
let ans2 = interp1 ex2

let ans3 = interp2 (fst (computeFreeVars ex1))
let ans4 = interp2 (fst (computeFreeVars ex2))

let ans5 = interp3 (translate [] ex1)
let ans6 = interp3 (translate [] ex2)

let runtests () =
  let p i = 
    print_string (match i with Int i -> string_of_int i | _ -> "WRONG");
    print_newline () in
  List.iter p [ans1; ans2; ans3; ans4; ans5; ans6]

let _ = runtests
