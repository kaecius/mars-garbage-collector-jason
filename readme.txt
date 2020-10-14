TASK 1) Include more indeterminism in the enviroment.

1.1) Randomly scattering some pieces of garbage on the grid.
At the constructor method of the "MarsModel" class we removed the 5 lines that created the garbage in a default position,
replacing them with a loop that place garbage in random positions with the next call: add(GARB,getFreePos()). Which getFreePos()
get the an agent (and object) free and random position.

1.2) Randomly placing the incinerator (together with r2) on the grid.
At the constructor method of the "MarsModel" class we removed the line that place the r2 agent in a default position,
placing it in a (free) random position thanks to the next call: getFreePos();

Leaving the code like this:  setAgPos(1, getFreePos());


1.3) Randomly placing r1 on the grid.
The same as the incinetator but with the agent r1: setAgPos(0, getFreePos());



TASK 2) Modify r2 agent to fail as r1 when picking up the garbage.
R2 agents could fail up to 3 times when trying to pick the garbage that r1 left in his position.

In "MarsEnv.java":
We define the variable "berr", that stores the number of tries of burning garb made by the incinerator, and we initialize the variable BErr to 2, that sets the maximum error that the incinerator can make. Then, we redefined the function "burnGarb()", in such a way that it would burn the garbage based on a random Boolean, or if the tries that he made were maximum (berr == BErr); if those conditions are not filled, the incinerator would fail the burning, increasing the berr variable by 1.

Ending up like this: (esto no se si tenemos que ponerlo)
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

In "r2.asl":
We defined the new behaviour of r2 in this way, in which the agent will recursivelly activate the !ensure_burn(S) -------  until the call to the burn(garb) function actually burns the garbage.

+garbage(r2) : true <- !ensure_burn(r2).

+!ensure_burn(S) : garbage(S)
   <- burn(garb);
      !ensure_burn(S).
+!ensure_burn(_).

TASK 3) Modify r1 agent for changing its searching path
a) Scanning top-down instead of left-right.
In "MarsEnv.java":
We made the function "nextSlotTopDown()" that takes the actual position of r1 agent and changes it to the one bellow, instead of when is the lowest position of the grid (last row) that it changes it to the new column at the first position.

In "r1.asl":
We change the "check" plan, that will move the agent to the next position whenever there's no garbage in r1's position, in this case the next position will be set by the "nextSlotTopDown()" function.

+!check(slots) : not garbage(r1)
   <- nextTopDown(slot);
      !check(slots).
+!check(slots).

b) Scanning continuously.

The function "nextSlotTopDownContinously()" will behave like the "nextSlotTopDown()" function,  in contrast, this will work in the same way until it arrives to the last position (last row, last column), that it will change the position of the agent r1 to the first position of the grid (first row, first column).

In "r1.asl":
We change the "check" plan, that will move the agent to the next position whenever there's no garbage in r1's position, in this case the next position will be set by the "nextSlotTopDownContinously()" function.

+!check(slots) : not garbage(r1)
   <- nextTopDownContinously(slot);
      !check(slots).
+!check(slots).


4) Include a new crazy Robot r3 that moves and produces garbage randomly.
First of all we defined the new r3 agent in the "mars.mas2j" file.
After that, we created the "r3.asl" folder, where we defined the plans for the agent, and his "belief" aswell:

at(P) :- pos(P,X,Y) & pos(r3,X,Y).

!check(slots).

+!check(slots) : true
   <- nextRandom(slot);
      !check(slots).
+!check(slots).

This agent moves randomly by default.

After that, we throw ourselves to the "MarsEnv.java" file:
	1- We set the initial position of the agent as random in the constructor method of the class "MarsModel"
		setAgPos(2, getFreePos());
	2- We defined the method "nextRandom()" which moves the agent to a random possition and drops garbage in that new position.


5) Include a new task at your choice.

