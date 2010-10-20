
(* CSEP505, Winter 2009, Homework 5 *)

(* remember to compile with ocamlc -vmthread -o hw5 threads.cma hw5.ml
   (see the Makefile) 

   This works for Dan under cygwin on Windws (and on Linux).  In the
   past, some Windows users have suggested that this worked for them instead:
   ocamlc -thread unix.cma threads.cma hw5.ml -o hw5.exe
   This is fine -- the issues are all linking and library-finding gunk.
*)

open Event (* library for the CML primitives *)

exception BadLockState (* for problem 2 *)

(************* put problem 1 here ***************)

type acct = float channel * float channel * float channel * float channel
let mkAcct () = 
  let depCh = new_channel() in
  let wdrCh = new_channel() in 
  let dOutCh = new_channel() in
  let wOutCh = new_channel() in
  let rec loop bal = 
    let (newBal, wasDep) =
    sync(choose([
      wrap (receive depCh) (fun f -> ((bal +. f), true));
      wrap (receive wdrCh) (fun f -> ((bal -. f), false))]))
    in if(wasDep)
       then sync(send dOutCh newBal)
       else sync(send wOutCh newBal);
       loop newBal;
  in ignore(Thread.create loop 0.0);
(depCh, wdrCh, dOutCh, wOutCh)

let get acct amt = 
  let (depCh, wdrCh, dOutCh, wOutCh) = acct in
    sync (send wdrCh amt); sync (receive wOutCh)

let put acct amt = 
  let (depCh, wdrCh, dOutCh, wOutCh) = acct in
    sync (send depCh amt); sync (receive dOutCh)

(********* complicated way to do se_locks *******)

type se_lock1 = unit channel channel * unit channel channel * unit channel

let new_selock1 () =
  let share_ch     = new_channel() in
  let exclusive_ch = new_channel() in
  let release_ch   = new_channel() in
  let rec unlocked () =
    sync (choose [ wrap (receive share_ch)
		     (fun c -> 
		       sync (send c ());
		       shared 1 []);
		   wrap (receive exclusive_ch)
		     (fun c ->
		       sync (send c ());
		       exclusive [] []);
		   wrap (receive release_ch)
		     (fun () -> raise BadLockState) ])
  and shared count waiting_exclusive =
    sync (choose [ wrap (receive share_ch) 
		     (fun c -> 
		       sync (send c ());
		       shared (count + 1) waiting_exclusive);
		   wrap (receive exclusive_ch)
		     (fun c -> 
		       shared count (c::waiting_exclusive));
		   wrap (receive release_ch)
		     (fun () ->
		       match count, waiting_exclusive with
		       | 1, []     -> unlocked ()
		       | 1, hd::tl -> sync (send hd ()); exclusive [] tl
		       | _, _      -> shared (count - 1) waiting_exclusive) ])
  and exclusive waiting_shared waiting_exclusive =
    sync (choose [ wrap (receive share_ch)
		     (fun c ->
		       exclusive (c::waiting_shared) waiting_exclusive);
		   wrap (receive exclusive_ch)
		     (fun c ->
		       exclusive waiting_shared (c::waiting_exclusive));
		   wrap (receive release_ch)
		     (fun () ->
		       match waiting_shared, waiting_exclusive with
		       | [],[]     -> unlocked ()
		       | [],hd::tl -> sync (send hd ()); exclusive [] tl
		       | _::_,_    -> List.iter (fun c -> sync (send c ())) 
			                        waiting_shared;
			              shared (List.length waiting_shared) 
			                     waiting_exclusive) ])
  in
  ignore(Thread.create unlocked ());
  (share_ch,exclusive_ch,release_ch)

let with_lock1 start_ch end_ch th =
  let c = new_channel() in
  sync (send start_ch c);
  sync (receive c);
  try let ans = th () in sync (send end_ch ()); ans
  with x -> sync (send end_ch ()); raise x

let shared_do1    (c1,_,c2) th = with_lock1 c1 c2 th
let exclusive_do1 (_,c1,c2) th = with_lock1 c1 c2 th

(************ end of complicated way ***********)

(* don't change the type definition *)
type se_lock2 = unit channel * unit channel * unit channel 

(************* put problem 2 here ***************)

let new_selock2 () =
  let share_ch     = new_channel() in
  let exclusive_ch = new_channel() in
  let release_ch   = new_channel() in
  let rec unlocked ()  =
    sync (choose([wrap (receive share_ch) (fun () -> shared 1);
                  wrap (receive exclusive_ch) (fun () -> exclusive())]))
  and shared count =
    sync (choose([wrap (receive share_ch) (fun () -> shared (count + 1));
                  wrap (receive release_ch) 
                    (fun () -> if(count = 1) then unlocked () 
                               else shared (count - 1))]))
  and exclusive () = sync (receive release_ch); unlocked()
  in ignore(Thread.create unlocked ());
  (share_ch,exclusive_ch,release_ch)

(* don't change the rest *)

let with_lock2 start_ch end_ch th =
  sync (send start_ch ());
  try let ans = th () in sync (send end_ch ()); ans
  with x -> sync (send end_ch ()); raise x

let shared_do2    (c1,_,c2) th = with_lock2 c1 c2 th
let exclusive_do2 (_,c1,c2) th = with_lock2 c1 c2 th

(************** put problem 4 (challenge problem) here *********)

