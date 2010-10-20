
exception ListError
exception TypeError
exception RunTimeError
exception Unimplemented (* shows you what to change *)

type var = string

type typ =
    Int
  | Arrow  of typ * typ (* Arrow(t1,t2) means function from t1 to t2 *)

type context = (var * typ) list

type exp =
  | Const of int
  | Plus of exp * exp
  | Var of var
  | Lam of var * typ * exp
  | Apply of exp ref * exp
  | Closure of exp * env (* not in source programs *)
and env = (var * exp) list

let empty = []

let extend lst x e = (x,e)::lst

let rec lookup lst y =
  match lst with
    [] -> raise ListError
  | (x,v)::tl -> if x=y then v else lookup tl y

let rec interpret env e =
  match e with
    Const i -> e
  | Plus(e1,e2)  -> 
      let v1 = interpret env e1 in
      let v2 = interpret env e2 in
      (match (v1,v2) with
	Const i1, Const i2 -> Const(i1 + i2)
      |	_ -> raise RunTimeError)
  | Var(x) -> lookup env x
  | Lam _ -> Closure(e,env)
  | Apply(e1,e2) ->
      let v1 = interpret env !e1 in
      let v2 = interpret env e2 in
      (match v1 with
	Closure(Lam(x,_,e3),envc) -> interpret (extend envc x v2) e3
      |	_ -> raise RunTimeError)
  | Closure(_,_) -> e

let rec typecheck ctxt e =
  match e with
    Const i -> Int
  | Plus(e1,e2) -> 
      let t1 = typecheck ctxt e1 in
      let t2 = typecheck ctxt e2 in
      if (t1=Int) && (t2=Int)
      then Int
      else raise TypeError 
  | Var(x) -> (try lookup ctxt x with ListError -> raise TypeError)
  | Lam(x,t,e) ->
      let t1 = typecheck (extend ctxt x t) e in
      Arrow(t,t1)
  | Apply(e1,e2) ->
      let t1 = typecheck ctxt !e1 in
      let t2 = typecheck ctxt e2 in
      (match t1 with
	Arrow(t3,t4) -> if t3=t2 then t4 else raise TypeError
      |	_ -> raise TypeError)
  | Closure(_,_) -> raise TypeError

let interpret e = interpret empty e

let typecheck e = ignore(typecheck empty e) 

let interpret1 e = typecheck e; interpret e

let checked = ref []

let typecheck2 e = typecheck e; checked := (ref e)::!checked

let interpret2 e = let rec interpIfFound checked e2 =
                   match !checked with
                   e1 :: tl -> if(e2 == e1) then interpret !e2
                               else interpIfFound (ref tl) e2
                   | [] -> raise TypeError
                   in interpIfFound checked (ref e)

let typecheck3 e = typecheck e; fun () -> interpret e

type checkedexp = exp

let typecheck4 e = typecheck e; e

let interpret4 e = interpret e;
