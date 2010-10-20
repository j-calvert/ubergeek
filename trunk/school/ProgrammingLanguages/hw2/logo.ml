
exception Unimplemented (* should not be raised after the assignment is done *)

exception AlreadyDone (* for the small-step interpreter *)

let pi = 3.14159265
(*** part a ***)
type move = Home | Forward of float | Turn of float | For of int * (move list)

let makePoly sides len = 
  For(sides, [Forward(len) ; Turn(2.0 *. pi /. (float_of_int sides))]) 

let rec scale scalingFactor logoProgram = 
  let rec recscale scaled orig =
    match orig with
      [] -> scaled
      | hd :: tl ->
        match hd with
          Forward(l) -> recscale (scaled @ [Forward(l *. scalingFactor)]) tl
        | For(i, subProg) -> recscale (scaled @ [For(i, scale scalingFactor subProg)]) tl
        | _ -> recscale (scaled @ [hd]) tl
  in recscale [] logoProgram


(*** part b ***)
let interpLarge (movelist : move list) : (float*float) list = 
  let rec loop movelist x y dir acc =
    match movelist with
      [] -> acc
    | Home::tl -> loop tl 0.0 0.0 0.0 ((0.0,0.0) :: acc)
    | Forward(d)::tl ->  loop tl (x +. d *. (cos dir)) (y +. d *. (sin dir)) dir ((x +. d *. (cos dir), y +. d *. (sin dir)) :: acc)
    | Turn(r)::tl -> loop tl x y (dir +. r) acc
    | For(i, ml) :: tl -> if i = 0 
							 then loop tl x y dir acc 
							 (* else unroll the loop *)
						     else loop (For(i - 1, ml) :: ml @ tl) x y dir acc 
  in List.rev (loop movelist 0.0 0.0 0.0 [(0.0,0.0)])

(*** part c ***)
let interpSmall (movelist : move list) : (float*float) list = 
  let interpSmallStep movelist x y dir : move list * float * float * float = 
  match movelist with
    [] -> raise Unimplemented
  | Home::tl -> (tl, 0.0, 0.0, 0.0)
  | Forward(d)::tl -> (tl, (x +. d *. (cos dir)), (y +. d *. (sin dir)), dir)
  | Turn(r)::tl -> (tl, x, y, (dir +. r))
  | For(i, ml)::tl -> if i = 0
						 then (tl, x, y, dir)
						 else ((For(i - 1, ml) :: ml @ tl), x, y, dir)
  in
  let rec loop movelist x y dir acc =
    match movelist with
	  [] -> acc
	| _ -> let (movelist2, x2, y2, dir2) = interpSmallStep movelist x y dir in
	    if((x <> x2) || (y <> y2))
		  then loop movelist2 x2 y2 dir2 ((x2, y2) :: acc)
		  else loop movelist2 x2 y2 dir2 acc
  in 
  List.rev (loop movelist 0.0 0.0 0.0 [(0.0,0.0)])

(*** part d ***)
(* The large step interpreter will add a location to the trace for each Home or Forward move, 
regardless of whether this move changes the position, whereas the small step interpreter will 
add the location only if this move changes the position.  So there will be a difference in 
trace if and only if there is any move Forward of distance 0 or there is a Home move while the 
current location (x, y) = (0.0, 0.0).*)

(*** part e ***)
let rec interpTrans movelist : float->float->float-> (float * float) list * float = 
  let compose f1 f2 x y d =
    let rec last lst = 
      match lst with
      []    -> None
      | e::[] -> Some e
      | e::tl -> last tl in
      let (locs1, dir1) = f1 x y d in
      let lst = last locs1 in 
      match lst with 
        None -> f2 x y dir1
      | Some (x1, y1) -> let (locs2, dir2) = f2 x1 y1 dir1 in locs1 @ locs2, dir2	in
  match movelist with
    [] -> (fun x y d -> [], d)
  | Home::tl -> let locs, fd = interpTrans tl 0.0 0.0 0.0 in 
                  fun x y dir -> (0.0, 0.0) :: locs, fd
  | Forward(d)::tl -> fun x y dir -> (let x2 = x +. d *. (cos dir) in
                                      let y2 = y +. d *. (sin dir) in
                                      let locs, fd = interpTrans tl x2 y2 dir
                                      in  (x2, y2) :: locs, fd)
  | Turn(r)::tl -> fun x y dir -> (let locs, fd = interpTrans tl x y (dir +. r)
                                      in  locs, fd)
  | For(i, ml)::tl -> if i = 0 then (fun x y d -> interpTrans tl x y d) 
                      else compose (interpTrans ml) (interpTrans (For(i-1, ml)::tl))
                     (* else fun x y d -> ((compose (interpTrans ml) (interpTrans (For(i-1, ml)::tl))) x y d) *)
 

(*** possibly helpful testing code ***)

(* you do not have to use this "testing" code, but you might find it useful *)
(* no need to change more than example_logo_prog *)
let example_logo_prog = [For(2, [For(4, [Forward(1.0); Turn(1.5708)]); Home; Home])]
let ansL = interpLarge example_logo_prog
let ansS = interpSmall example_logo_prog
let ansT = (0.0,0.0)::(fst ((interpTrans example_logo_prog) 0.0 0.0 0.0))

let rec pr lst =
  match lst with
    [] -> ()
  | (x,y)::tl -> 
      print_string("(" ^ (string_of_float x) ^ "," ^ (string_of_float y) ^ ")");
      pr tl

let _ = 
  pr ansL; print_newline (); 
  pr ansS; print_newline ();
  pr ansT; print_newline (); 

