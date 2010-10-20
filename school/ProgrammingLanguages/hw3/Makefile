# We are not bothering to track dependencies because everything is small
# enough to recompile at will.

OCAMLC=ocamlc

all: 
	$(OCAMLC) -c -g prob23.mli
	$(OCAMLC) -c -g prob4.mli
	$(OCAMLC) -c -g prob23.ml
	$(OCAMLC) -c -g prob4.ml
	$(OCAMLC) -c -g main.ml
	$(OCAMLC) -o hw3 prob23.cmo prob4.cmo main.cmo # order matters!

clean:
	-rm *.cmo *.cmi hw3
