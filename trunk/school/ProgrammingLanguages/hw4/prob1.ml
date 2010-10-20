
exception Unimplemented
exception RuntimeTypeError
exception DoesNotTypecheck of string

(****** Syntax for our language, including types (do not change) *****)

type exp = Var of string 
         | Lam of string * typ * exp
	 | Letrec of typ * string *  string * typ * exp
	 | Apply of exp * exp
	 | Closure of string * exp * (env ref)
	 | Int of int
	 | Plus of exp * exp
	 | Iszero of exp
	 | True
	 | False
	 | If of exp * exp * exp
	 | RecordE of (string * exp) list
	 | RecordV of (string * (exp ref)) list (* all exps are value *)
	 | Get of exp * string
	 | Set of exp * string * exp
	 | Cast of exp * typ

and env = (string * exp) list

and access = Read | Write | Both

and typ = IntT
        | BoolT
        | ArrowT of typ * typ
	| RecordT of (string * typ * access) list

and ctxt = (string * typ) list

(****** Interpreter for our language (do not change) *****)

let rec interp env e =
  match e with
    Var s -> (try List.assoc s env with Not_found -> raise RuntimeTypeError)
  | Lam(s,_,e2) -> Closure(s,e2,ref env) 
  | Letrec(_,f,x,_,e) ->
      let r = ref env in
      let c = Closure(x,e,r) in
      let _ = r := (f,c)::(!r) in
      c
  | Apply(e1,e2) ->
      let v1 = interp env e1 in
      let v2 = interp env e2 in
      (match v1 with
	Closure(s,e3,env2) -> interp((s,v2)::(!env2)) e3
      | _ -> raise RuntimeTypeError)
  | Closure _ -> e 
  | Int _ -> e
  | Plus(e1,e2) ->
      (match interp env e1, interp env e2 with
	(Int i, Int j) -> Int (i+j)
      | _ -> raise RuntimeTypeError)
  | Iszero e ->
      (match interp env e with
	Int 0 -> True
      |	Int _ -> False
      |	_     -> raise RuntimeTypeError)
  | True -> e
  | False -> e
  | If(e1,e2,e3) ->
      (match interp env e1 with
	True  -> interp env e2
      | False -> interp env e3
      | _     -> raise RuntimeTypeError)
  | RecordE lst -> RecordV (List.map (fun (s,e) -> (s,ref(interp env e))) lst)
  | RecordV _ -> e
  | Get(e,s) -> 
      (match interp env e with
	RecordV lst -> 
	  (try !(List.assoc s lst) with Not_found -> raise RuntimeTypeError)
      | _ -> raise RuntimeTypeError)
  | Set(e1,s,e2) ->
      (match interp env e1 with	
	RecordV lst -> 
	  let r=try List.assoc s lst with Not_found -> raise RuntimeTypeError in
	  let ans = interp env e2 in 
	  r := ans; ans
      | _ -> raise RuntimeTypeError)
  | Cast(e,t) -> interp env e (* no run-time effect *)

let interp e = interp [] e

(***** helper functions provided to you (do not change) *****)
let fields_unique lst = (*raise exception if same field name appears > 1 time*)
  let rec loop lst1 lst2 =
    match lst2 with
      [] -> ()
    | (s,t,a)::tl -> 
	if (List.exists (fun (s2,_,_) -> s2=s) lst1)
	then raise (DoesNotTypecheck "")
	else loop ((s,t,a)::lst1) tl in
  loop [] lst

(* checks field_unique for every contained record-type *)
let rec checkType t = 
  match t with
    IntT  -> ()
  | BoolT -> ()
  | ArrowT(t1,t2) -> checkType t1; checkType t2
  | RecordT lst -> fields_unique lst; List.iter (fun (_,t,_) -> checkType t) lst

(* (string * typ * access) list -> string -> (typ * access) option *)
let rec getFieldType lst str = 
  match lst with
    [] -> None
  | (s,t,a)::tl -> if s=str then Some (t,a) else getFieldType tl str

(******** Problem 1: complete subtype and typecheck *********)

let rec subtype t1 t2 = (* true if t1 is a subtype of t2 *)
  
  match t1,t2 with
    IntT,IntT -> true
  | BoolT,BoolT -> true
  | ArrowT(t3,t4), ArrowT(t5,t6) -> (subtype t4 t6)  && (subtype t5 t3)
  | RecordT lst1, RecordT lst2 ->
     (match lst2 with 
     [] -> true
     | (s, t, a) :: tl -> 
        (match getFieldType lst1 s with
          None -> false (* field of t2 not in t1 *)
        | Some(u, b) -> if((not (subtype u t)) || (a = Read && b <> Read) || (a = Write && b <> Write))
                        (* membership is covariant and subtype can only restrict access *)
                        then false
                        else
          subtype (RecordT lst1) (RecordT tl) ))(*recurse on the tail of lst2 *)
  | _ -> false

let rec typecheck ctxt e = (* return type of e under ctxt *)
  (* we did 4 cases for you *)
  match e with
    Var s -> (try List.assoc s ctxt with Not_found -> raise (DoesNotTypecheck "Var"))
  | Lam(s,t,e) -> checkType t; ArrowT(t,typecheck ((s,t)::ctxt) e)
  | Closure _ -> raise (DoesNotTypecheck "not a source program")
  | Plus(e1,e2) ->
      if subtype (typecheck ctxt e1) IntT && subtype (typecheck ctxt e2) IntT
      then IntT
      else raise (DoesNotTypecheck "Plus")
	| Int(i) -> IntT
	| Iszero(e1) -> if (typecheck ctxt e1 = IntT) 
                  then BoolT 
                  else raise (DoesNotTypecheck "Iszero")
	| True -> BoolT
	| False -> BoolT
  | Letrec(t1,f,x,t2,e) -> (* typ * string *  string * typ * exp *)
                          checkType t1; checkType t2; 
                          if(subtype (typecheck ((x,t1)::ctxt) e) t2)
                          then ArrowT(t1,t2)
                          else raise (DoesNotTypecheck "Function return type invalid")
	| If(e1, e2, e3) -> if(typecheck ctxt e1 = BoolT) 
                      then (
                        let (t2, t3) = (typecheck ctxt e2, typecheck ctxt e3) in  
                          if(subtype t2 t3) then t3 else t2
                      ) else raise (DoesNotTypecheck "If")
	| RecordV _ -> raise (DoesNotTypecheck "not a source program")
	| Get(e1, s) -> 
     (match typecheck ctxt e1 with
        RecordT lst ->
            	  (fields_unique lst;
                 match getFieldType lst s with 
                   None -> raise (DoesNotTypecheck "NPE")
                   | Some(t, a) -> if(a = Write) 
                                   then raise (DoesNotTypecheck "Access Denied") 
                                   else checkType t; t)
      | _ -> raise (DoesNotTypecheck "Get")
     )
	| Set(e1, s, e2) ->
     (match typecheck ctxt e1 with
        RecordT lst ->
            	  (fields_unique lst;
                 match getFieldType lst s with 
                   None -> raise (DoesNotTypecheck "NPE")
                   | Some(t, a) -> if(a = Read) 
                                   then raise (DoesNotTypecheck "Access Denied") 
                                   else checkType t; 
                                     if(not (subtype (typecheck ctxt e2) t)) 
                                     then raise (DoesNotTypecheck "Implicit Type Cast Exception (for Set)"); 
                                     t
                )
      | _ -> raise (DoesNotTypecheck "Set")
     )
	| RecordE lst -> RecordT(List.map (fun (s, e1) -> (s, typecheck ctxt e1, Both)) lst)
	| Cast(e1, t) -> checkType t; 
                    if(not (subtype (typecheck ctxt e1) t))
                    then raise (DoesNotTypecheck "Type Cast Exception")
                    else t
	| Apply(e1,e2) -> (match typecheck ctxt e1 with ArrowT(t1, t2) -> checkType t1; checkType t2; 
                          if(subtype t2 (typecheck ctxt e2))
                          then ArrowT(t1,t2)
                          else raise (DoesNotTypecheck "Bad Application")
                     | _ -> raise (DoesNotTypecheck "Can only apply an Arrow to an expression") )

let typecheck e = typecheck [] e

(********** examples and testing ***********)

(* we have provided two tests that should type-check, but you can make small
   changes to them that should cause them not to type-check *)

(* first some helper metafunctions to make examples easier to write *)
let lam x t e = Lam(x,t,e)
let app e1 e2 = Apply(e1,e2)
let vx = Var "x"
let vy = Var "y"
let vf = Var "f"

(* first test -- puts sum of two fields in second field *)
(* (\ x : {l1=int,read, l2=int,both}. x.l2 := x.l1 + x.l2) {l3=12,l2=17,l1=19}*)

let ex1 = 
  app (lam "x" (RecordT [("l1",IntT,Read);("l2",IntT,Both)])
	 (Set(vx,"l2",Plus(Get(vx,"l1"),Get(vx,"l2")))))
      (RecordE [("l3",Int 12);("l2",Int 17);("l1",Int 19)])

let t1 = typecheck ex1
let v1 = interp ex1

(* second test -- uses letrec to implement multiplication of positive nums *)
let ex2 = 
  app (Letrec(IntT,"f","x",(RecordT [("l1",IntT,Read);("l2",IntT,Read)]),
	app (lam "minus1" IntT
	       (If(Iszero (Var "minus1"),
		   Get(vx,"l2"),
		   Plus(Get(vx,"l2"),
			app vf (RecordE[("l1", Var "minus1");
					 ("l2", Get(vx,"l2"))])))))
  	     (Plus(Get(vx,"l1"),Int(-1)))))
       (RecordE[("l1", Int 7); ("l2", Int 6)])

let t2 = typecheck ex2
let v2 = interp ex2
let _  = assert (v2 = Int 42)

(******************* for problem 2 ****************)

let t3 = typecheck (Lam("x",RecordT[("l",IntT,Read)],
			RecordE[("l1",Get(Var("x"),"l")); ("l2",Var("x"))]))
let t4 = typecheck (Lam("x",RecordT[("l",IntT,Both)],
			RecordE[("l1",Get(Var("x"),"l")); ("l2",Var("x"))]))

