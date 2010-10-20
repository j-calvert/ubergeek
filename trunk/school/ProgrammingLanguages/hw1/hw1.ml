
(* CSEP505, Winter 2009, Homework 1 *)

(***** Problem 1 *****)

type inttree = Empty | Node of int * inttree * inttree

(* use this function in fromList *)
let rec insert t i =
  match t with
    Empty -> Node(i,Empty,Empty)
  | Node(j,l,r) -> if i=j 
                   then t 
                   else if i < j 
		   then Node(j,insert l i,r)
		   else Node(j,l,insert r i)

(* no need for this function; it is just an example *)
let rec member t i =
  match t with
    Empty -> false
  | Node(j,l,r) -> i=j || (i < j && member l i) || member r i

(* put fromList, sum1, prod1, avg1, map1 and negateAll here *)
(* 1a *)
let rec fromList l =
  match l with
    [ ] -> Empty
  | x::r -> insert (fromList r) x

(* 1b *)
let rec sum1 t =
  match t with
    Empty -> 0
  | Node(j,l,r) -> j + sum1 l + sum1 r

let rec prod1 t =
  match t with
    Empty -> 1
  | Node(j,l,r) -> j * prod1 l * prod1 r

let rec avg1helper t =
  match t with
    Empty -> (0, 0)
  | Node(j,l,r) -> let ll = avg1helper l 
                   and rr = avg1helper r
                   in (1 + fst(ll) + fst(rr), j + snd(ll) + snd(rr))

let avg1 t =
  let tt = avg1helper t
  in snd(tt) / fst(tt)

(* 1c *)
let rec map t m =
  match t with
    Empty -> Empty
  | Node(j, l, r) -> Node(m j, map l m, map r m)

(* 1d *)
let negateAll t = map t (fun x -> -x)
  
let rec fold f a t =
  match t with
    Empty -> a
  | Node(j,l,r) -> fold f (fold f (f a j) l) r

(* put your English answer to 1e here *)
(* 1e *)
(* fold can be used to apply a function f to each node in a tree, and store an accumulation of the results in a variable a.  Empty nodes will leave accumulated value a unaltered, while calls on non-empty nodes will alter the value of a passed to the caller (one level higher in the recursion.  *)

(* put sum2, prod2, and avg2 here *)
(* 1f *)
let sum2 t = fold (fun a j -> a + j) 0 t

let prod2 t = fold (fun a j -> a * j) 1 t

let avg2 t = 
  let nd = fold (fun a j -> (fst(a) + 1, snd(a) + j)) (0,0) t
  in snd(nd) / fst(nd)


(***** Problem 2 *****)
(* put your answer to problem 2 here *)
(* 2a *)
let only_capitals = List.filter (fun s -> s.[0] = Char.uppercase s.[0])

(* 2b *)
let longest_string1 = List.fold_left (fun a b -> if(String.length b > String.length a) then b else a) ""

(* 2c *)
let longest_string2 = List.fold_left (fun a b -> if(String.length b >= String.length a) then b else a) ""

(* 2d *)
let longest_string_helper f = List.fold_left (fun a b -> if(f (String.length a) (String.length b)) then b else a) ""
let longest_string3 = longest_string_helper (fun i j -> j > i)
let longest_string4 = longest_string_helper (fun i j -> j >= i)

(* 2e *)
let longest_capitalized l = longest_string3 (only_capitals l)


(***** Problem 3 *****)
(* put your answer to problem 3 here *)
(* 3a *)
let rec first_answer f l =
  match l with 
    [ ] -> None
  | x :: rest -> if (f x <> None) then f x else first_answer f rest

(* 3b *)
let rec aa_helper f acc l =
  match l with
    [] -> Some acc
    | x :: rest ->
      match f x with
        None -> None
      | Some lst -> aa_helper f (lst @ acc) rest
 
let all_answers f l = aa_helper f [] l
 

(***** Problem 4 *****)

type pattern = 
    Wildcard
  | Variable of string
  | UnitP
  | ConstP of int
  | TupleP of pattern list
  | ConstructorP of string * pattern

type valu = 
    Const of int
  | Unit
  | Tuple of valu list
  | Constructor of string * valu

let rec g v f2 p =
  let r = g v f2 in
  match p with
    Wildcard -> v
  | Variable x -> f2 x
  | TupleP ps -> List.fold_left (fun i p -> (r p) + i) 0 ps
  | ConstructorP(_,p) -> r p
  | _ -> 0

(* put your answer to problem 4 here *)
(* 4a *)
(* g takes a pattern and returns a integer value.  The argument v is a integer constant returned iff the pattern is a wildcard.  The argument f2 is a function that takes a string and returns an integer value which in turn is returned when the pattern is a Variable.  In the case that pattern is that of a Constructor, it recurses on the pattern member of the Constructor.  In the case that the pattern is a tuple, it folds and accumulates on the list of component patterns.  In all other cases it returns 0. *)

(* 4b *)
let count_wildcards = g 1 (fun x -> 0)

(* 4c *)
let count_wildcards = g 1 (fun x -> String.length x)

(* 4d *)
let count_some_var s p =
  let d = (fun x -> if (x = s) then 1 else 0) in
    g 0 d p

(* 4e *)
let rec rec_name_list acc p =
  match p with
    Variable x -> acc @ [x]
  | TupleP ps -> List.fold_left (fun a p -> rec_name_list a p) acc ps
  | ConstructorP(_,p) -> rec_name_list acc p
  | _ -> acc

let rec rec_check_pat l =
  match l with
    [ ] -> true
  | hd :: tl -> (not (List.exists (fun x -> hd = x) tl)) && (rec_check_pat tl)

let check_pat p = rec_check_pat (rec_name_list [ ] p)

(* 4f *)
let rec get_match (v,p) =
  match (v,p) with
    (_, Wildcard) -> Some([])
  | (_, Variable s) -> Some((s, v) ::[])
  | (Unit, UnitP) -> Some([])
  | (Const i, ConstP j) -> if(i = j) then Some([]) else None
  | (Tuple vs, TupleP ps) -> if(List.length vs = List.length ps) then all_answers get_match (List.combine vs ps) else None
  | (Constructor(s1,v), ConstructorP(s2,p)) -> if(s1 = s2) then get_match(v,p) else None
  | _ -> None
  
(* 4g *)
let first_match v = let gm w q = get_match (w,q) in first_answer (gm v)


(***** Challenge Problem 5 *****)

type 'a iterator = Nomore | More of 'a * (unit -> 'a iterator)

let rec iter t =
  let rec f t k =
    match t with
      Empty -> k ()
    | Node(j,l,r) -> More(j, fun () -> f l (fun () -> f r k))
  in f t (fun () -> Nomore)


(* put your English description here *)
(* 5a *)
(* A client of iter would call it for a tree that they want to process all the nodes of, and get back an iterator type, which is essentially a linked list.  They would then use that in a reursive call that terminates when the second element is a (fun () -> Nomore).  I'm not sure why these aren't defined as in "5 --Simpler iterator?--" (see below) *)

(* It works by taking a tree and producing a linked list via recursion that takes place within the constructor of the returned iterator. The tree is traversed depth-first, working from right to left. *)

(* put sum3, prod3, and avg3 here *)
(* 5b *)
let sum3 t =
  let rec loop i itr =
    match itr with 
      Nomore -> i
    | More(j, f) -> loop (i+j) (f())
  in loop 0 (iter t)  

let prod3 t =
  let rec loop i itr =
    match itr with 
      Nomore -> i
    | More(j, f) -> if(j = 0) then 0 else loop (i*j) (f())
  in loop 0 (iter t)  

let avg3 t =
  let rec ndloop i t itr =
    match itr with 
      Nomore -> (i, t)
    | More(j, f) -> ndloop (i+j) (t+1) (f())
  in let nd = ndloop 0 0 (iter t)
     in fst(nd)/snd(nd)


(* 5 --Simpler iterator?-- *)
type 'a iterator2 = Nomore2 | More2 of 'a * 'a iterator2

let rec iter2 t =
  let rec f t k =
    match t with
      Empty -> k 
    | Node(j,l,r) -> More2(j, f l (f r k))
  in f t Nomore2

let sum32 t =
  let rec loop i itr =
    match itr with 
      Nomore2 -> i
    | More2(j, itr2) -> loop (i+j) itr2
  in loop 0 (iter2 t)  

let prod32 t =
  let rec loop i itr =
    match itr with 
      Nomore2 -> i
    | More2(j, itr2) -> if(j = 0) then 0 else loop (i*j) itr2
  in loop 0 (iter2 t)  

let avg32 t =
  let rec ndloop i t itr =
    match itr with 
      Nomore2 -> (i, t)
    | More2(j, itr2) -> ndloop (i+j) (t+1) itr2
  in let nd = ndloop 0 0 (iter2 t)
     in fst(nd)/snd(nd)

(***** Challenge Problem 6 *****)

type typ = 
    Anything
  | UnitT
  | IntT
  | TupleT of typ list
  | Datatype of string

(* put typecheck_patterns (and whatever helper functions) here *)

(***** Testing *****)

(* a little testing for problem 1 -- 
   commented out since the functions do not exist yet 
   (You can/should write similar tests for other problems, but we won't
    grade your tests.) *)

let tr = fromList [0;1;2;3;4;5;6;7;8;9;9;9;1] (* repeats get removed *)
let print_ans f t = print_string (string_of_int (f t)); print_string "\n"
let _ = print_ans sum3 tr
let _ = print_ans prod3 tr
let _ = print_ans avg3 tr
let _ = print_ans sum32 tr
let _ = print_ans prod32 tr
let _ = print_ans avg32 tr

