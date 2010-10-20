
exception Unimplemented
exception BadSourceProgram

type exp = Var of string 
         | Lam of string * exp
	 | Apply of exp * exp
	 | Int of int
	 | Plus of exp * exp
	 | A of exp
	 | B of exp
	 | Match of exp * string * exp * string * exp
	 | Pair of exp * exp
	 | First of exp
	 | Second of exp

(* a helper function that was useful in the Match case of translate *)
let makeLet s e1 e2 = Prob23.Apply(Prob23.Lam(s,e2,None),e1)

(******** Problem 4: complete this function ********)
let rec translate e = 
  match e with
    Var s -> Prob23.Var s 
  | Lam(s,e1) -> Prob23.Lam(s,translate e1,None)
  | Apply(e1,e2) -> Prob23.Apply(translate e1, translate e2)
  | Int i -> Prob23.Int i
  | Pair(e1,e2) -> Prob23.Pair(translate e1, translate e2)
  | First e1 -> Prob23.First(translate e1)
  | Second e1 -> Prob23.Second(translate e1)
  | Plus(e1,e2) -> Prob23.Plus(translate e1, translate e2)
  | Match(e1,s2,e2,s3,e3)-> let e1' = translate e1 in        (* I would in-line most of these, but for reasons I don't understand *)
                            let e1'' = Prob23.Second(e1') in (* ocamlc complains about makeLet having too many args if I don't *)
                            let st = "__tmp1" in 
                            let e2' = translate e2 in
                            let vst = Prob23.Var(st) in
                            let e3' = translate e3 in
                            Prob23.If(Prob23.First(e1'), 
                                makeLet s2 e1'' (makeLet st e2' vst) ,
                                makeLet s3 e1'' (makeLet st e3' vst))
  | A e1 -> Prob23.Pair(Prob23.True, translate e1)
  | B e1 -> Prob23.Pair(Prob23.False, translate e1)
 
(* to run a program, translate then interpret *)
let interp e = Prob23.interp1 (translate e)

(********** examples and testing ***********)

(* no need to change anything here; we have provided 1 very simply test *)

(* evaluates to Prob23.Int 7 *)
let ans1 = interp (Apply(
                    Lam("x", Match(Var "x", "y", Plus(Int 3, Var "y"), "z", Var "z")), 
                  A(Int 4)))

let runtests () = 
  let p i = 
    print_string (match i with Prob23.Int i -> string_of_int i | _ -> "WRONG");
    print_newline () in
  p ans1


