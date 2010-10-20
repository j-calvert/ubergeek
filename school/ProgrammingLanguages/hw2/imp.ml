type exp = Int of int | Var of string | Plus of exp * exp | Times of exp * exp
type stmt = Skip | Assign of string * exp | Seq of stmt * stmt
          | If of exp * stmt * stmt | While of exp * stmt | Saveheap of string * exp
          | Restoreheap of string 

type heap = (string * mbr) list and mbr = Subheap of heap | Intv of int

let rec lookup h str =
  match h with
    [] -> 0 (* kind of a cheat *)
  | (s,m)::tl -> 
    match m with
      Subheap(g) -> 0 (* another kind of a cheat *)
    | Intv(i) -> if s=str then i else lookup tl str

let rec restore h str =
  match h with
    (s,m)::tl -> if s=str then 
      match m with
        Subheap(g) -> g
      | Intv(i) -> h
    else restore tl str
  | _ -> h
  

let update h str i = (str,i)::h

let rec interp_e (h : heap) (e : exp) =
  match e with
    Int i       -> i
  | Var str     -> lookup h str
  | Plus(e1,e2) -> (interp_e h e1)+(interp_e h e2)
  | Times(e1,e2)-> (interp_e h e1)*(interp_e h e2)

let rec interp_s (h : heap) (s : stmt) =
  match s with
    Skip          -> h
  | Seq(s1,s2)    -> interp_s (interp_s h s1) s2
  | If(e,s1,s2)   -> if (interp_e h e) <> 0
                     then interp_s h s1 
                     else interp_s h s2
  | Assign(str,e) -> update h str (Intv(interp_e h e))
  | While(e,s1)   -> if (interp_e h e) <> 0
                     then interp_s (interp_s h s1) s
                     else h
  | Saveheap(str,e) -> update h str (Subheap(h))
  | Restoreheap(str) -> restore h str

let mt_heap = [] 

let interp_prog s = 
  lookup (interp_s mt_heap s) "ans"


