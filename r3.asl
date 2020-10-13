at(P) :- pos(P,X,Y) & pos(r3,X,Y).


!check(slots).

+!check(slots) : true
   <- nextRandom(slot);
      !check(slots).
+!check(slots).
