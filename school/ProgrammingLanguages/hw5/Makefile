# We are not really tracking dependencies because everything is small
# enough to recompile at will.

# See ocamldep and man make for how to write a "real" Makefile

OCAMLC=ocamlc

all: clean
	$(OCAMLC) hw5.mli
	$(OCAMLC) -vmthread -o hw5 threads.cma hw5.ml

clean:
	-rm *.cmo *.cmi hw5
