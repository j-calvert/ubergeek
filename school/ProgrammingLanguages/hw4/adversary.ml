
open Stlc2

(* We start with some examples that do not necessarily help you solve
part (e) except to show you how to build and mutate abstract
syntax. You can delete them if you want. *)

(* Here is an example of a function that builds new abstract syntax
every time it is called, but the abstract syntax it builds does not
type-check.  *) 
let make_bad_code () = Apply(ref (Const 9), Const 17)

(* Executing this would raise Stlc2.Unimplemented or Stlc2.TypeError *)
let f() = (Stlc2.typecheck2 (make_bad_code()))

(* Here is an example of a function that uses mutation but (ignoring
typing), keeps the expression equivalent to its input *)
let eta_expand_outermost e =
  match e with
    Apply(r,_) -> r := Lam("z",Int,Apply((ref (!r)), Var("z")))
  | _ -> ()

(* If interpret1 is safe, leave this commented out, else make calling break_a
call interpret1 such that it raises Stlc2.RunTimeError *)
(*let break_a () =*)

(* This is safe because (outside of concurrent execution) we have no opportunity to mess
with the typechecked expression before interpretation *)


(* If interpret2 is safe, leave this commented out, else make calling break_a
call interpret2 such that it raises Stlc2.RunTimeError *)
(* This will typecheck, and is legal, except that we're going to swap out the type the outermost Lam is expecting between
typcheck and interpretation *)
let break_b () = let e = Apply((ref (Lam("x",Arrow(Int, Int) ,
                          Plus(Var("x"), Const 17)))), 
                          Apply((ref (Lam("y",Int,Var("y"))),Const 9))) in
                 Stlc2.typecheck2 e;
                 eta_expand_outermost e;
                 Stlc2.interpret2 e
                   

(* If the function returned from Stlc2.typecheck3 is always safe, leave this
commented out, else make calling break_c call a function returned from
Stlc2.typecheck3 such that it raises Stlc2.RunTimeError *)
(* I was going to put the function below in, but I believe that the fact that we've formed
a closure around our interpreted expression means that this mutation of the outermost lambda
doesn't have an effect on it, i.e., we've shadowed the lambda in e 

let break_c () = let e = Apply((ref (Lam("x",Arrow(Int, Int) ,
                          Plus(Var("x"), Const 17)))), 
                          Apply((ref (Lam("y",Int,Var("y"))),Const 9))) in
                 let f = Stlc2.typecheck3 e in
                 eta_expand_outermost e;
                 f
*)

(* If interpret4 is safe, leave this commented out, else make calling break_d 
call interpret4 such that it raises Stlc2.RunTimeError *)

let break_d () = let r = (ref (Lam("x",Arrow(Int, Int) ,
                          Plus(Var("x"), Const 17)))) in
                          let e = Apply(r, 
                          Apply((ref (Lam("y",Int,Var("y"))),Const 9))) in
                          let f = Stlc2.typecheck4 e in
                          r := Lam("x",Int, Plus(Var("x"), Const 17));
                          Stlc2.interpret4 f


