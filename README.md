# amdp
Repo for AMDP research. Current hardcoded cleanup world demo can be run in the class amdp.hardcoded.cleanup.CleanupAgent. 
Gives a demonstration of AMDs working in a stochastic domain with stochastic planners w/ heuristics (Bounded RTDP).
More tweaks could be made but that is a good starting point. Make sure you use the included burlap.jar in this repo, or update your own copy,
because I very recently fixed a bug in Bounded RTDP that was not letting an important parameter be set through the constructor.

You can compile with `ant` but I highly recommend an IDE like IntelliJ.

##Next Steps
We should aim to have a general framework for this code. 
Specifically domains for AMDPs should have actions that return GroundedAction instances that implement an interface that provides a 
RewardFunction and TerminalFunction for executing that action in the lower-level domain. 
An AMDP domain should also be paired with an a "StateMapping" implementation 
(StateMapping is an interface defined in BURLAP proper that specifies a method that takes as input a State and outputs another state) 
that allows it to take a lower-level state and map it into the the AMDP Domain's state, 
rather than using hard coded methods like in the example code.

Finally, we should also have an AMDPAgent class that takes in its constructor (or by some other means) a sequnence of 
AMDP domains and set of PolicyGenerators. It should execute behavior in an environment (like the example code), 
but use the AMDP's domains state mappers and recursion to solve each level of the AMDP task an execute it. The PolicyGenerator should probably
be a general interface that the client will implement and should have a method that takes an AMDPGroundedAction and returns a policy 
(via some planning algorithm) for how to execute that action in the lower-level. This structure will allow the client to swap in
differnet planning algorithms for different AMDP actions and levels.
