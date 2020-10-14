!check(slots).

+!check(slots) : true
   <- nextRandom(slot);
   	  dropNewGarb(slot);
      !check(slots).
+!check(slots).
