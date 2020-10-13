TASK 1) Include more indeterminism in the enviroment.

1.1) Randomly scattering some pieces of garbage on the grid.
At the constructor method of the "MarsModel" class we removed the 5 lines that created the garbage in a default position,
replacing them with a loop that place garbage in random positions with the next call: add(GARB,getFreePos()). Which getFreePos()
get the an agent (and object) free and random position.

1.2) Randomly placing the incinerator (together with r2) on the grid.

At the constructor method of the "MarsModel" class we removed the line that place the r2 agent in a default position,
placing it a (free) random position thanks to the next call: getFreePos();

Leaving the code like this:  setAgPos(1, getFreePos());


1.2) Randomly placing r1 on the grid.

The same as the incinetator but with the agent r1: setAgPos(0, getFreePos());



TASK 2) Modify r2 agent to fail as r1 when picking up the garbage.

void burnGarb() {
            // r2 location has garbage
            if (model.hasObject(GARB, getAgPos(1))) {
				if (random.nextBoolean() || berr == BErr) {
                    remove(GARB, getAgPos(1));
                    berr = 0;
                } else {
                    berr++;
                }
            }
        }

+garbage(r2) : true <- !ensure_burn(r2).

+!ensure_burn(S) : garbage(S)
   <- burn(garb);
      !ensure_burn(S).
+!ensure_burn(_).
