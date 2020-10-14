// mars robot 1

/* Initial beliefs */

at(P) :- pos(P,X,Y) & pos(r1,X,Y).

battery(10).

/* Initial goal */

!check(slots).

/* Plans */

+!check(slots) : not garbage(r1) & battery(S) & S > 0 & not charge
   <- nextContinous(slot);
   	  -+battery(S-1);
      !check(slots).
+!check(slots) : battery(S) & S == 0 & not charge.
+!check(slots).
	
+battery(0) : true
	<-  -at(_);
		-garbage(r1);
		-pos(last,X,Y);
		.drop_all_intentions;
		!goToChargeStation(charge).

+!goToChargeStation(charge) : true
	<- 
		?pos(r1,X,Y);
      -+pos(last,X,Y);
	  	!at(r4);
		+charge;
   	  !charging(r);
		 !at(last);
		  -at(_);
		  -garbage(r1);
		  -charge;
		  .drop_all_intentions;
		  !check(slots).
	  
+!charging(r): battery(S) & S < 100 & charge
	<- -+battery(S+1);
		.wait(100);
		!charging(r).
	
+!charging(r): battery(S) & S == 100 & charge.
		
@lg[atomic]
+garbage(r1) : not .desire(carry_to(r2)) & battery(S) & S > 0 & not charge
   <- !carry_to(r2).
   
+!carry_to(R) : battery(S) & S > 0 & not charge
   <- // remember where to go back
      ?pos(r1,X,Y);
      -+pos(last,X,Y);
	  // carry garbage to r2
      !take(garb,R);
      // goes back and continue to check
      !at(last);
      !check(slots).

+!take(S,L) : battery(E) & E > 0 & not charge
   <- !ensure_pick(S);
      !at(L);
      drop(S).

+!ensure_pick(S) : garbage(r1) & battery(E) & E > 0 & not charge
   <- pick(garb);
      !ensure_pick(S).
+!ensure_pick(_).

+!at(L) : pos(L,X,Y) & pos(r1,X,Y).
+!at(L) <- ?pos(L,X,Y);
           move_towards(X,Y);
           !at(L).
