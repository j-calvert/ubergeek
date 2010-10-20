# We are not really tracking dependencies because everything is small
# enough to recompile at will.

# See ocamldep and man make for how to write a "real" Makefile

OCAMLC=ocamlc

all: clean prob1 prob3

prob1: prob1.ml
	$(OCAMLC) -o prob1 prob1.ml

prob3: 
	$(OCAMLC) -c stlc.mli
	$(OCAMLC) -c stlc.ml
	$(OCAMLC) -c stlc2.mli
	$(OCAMLC) -c stlc2.ml
	$(OCAMLC) -c adversary.ml
	$(OCAMLC) -o prob3 stlc2.cmo adversary.cmo 

clean:
	-rm *.cmo *.cmi prob1 prob3
