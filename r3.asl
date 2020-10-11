at(P) :- pos(P,X,Y) & pos(r3,X,Y).


!check(slots).

+!check(slots) : true
   <- nextTopDownContinous(slot);
      !check(slots).
+!check(slots).
