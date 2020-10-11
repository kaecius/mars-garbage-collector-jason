// mars robot 2
+garbage(r2) : true <- !ensure_burn(r2).

+!ensure_burn(S) : garbage(S)
   <- burn(garb);
      !ensure_burn(S).
+!ensure_burn(_).

