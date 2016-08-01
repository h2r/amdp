# amdp
Repo for AMDP research. 

This is a repo in transition, the master branch is the latest MAXQ flavoured code on `BURLAP 3.0`. The older functioning AMDP code is on `BURLAP 2.0` on the branch `amdpBurlap2`.

There is older hardcoded code for cleanup world in  amdp.hardcoded.cleanup.CleanupAgent. However, we know have code for constructing arbitary AMDP domains that can be planned by an agent. The most relevant pieces for that are in amdp.framework. Effectively, all AMDP levels > 0 are special Domain implementations that implement special AMDP interfaces. In particular, GroundedAction objects returned by actions in an AMDP must implement AMDPGroundedAction, which requires methods to get a reward function and terminal function (for the goal of that action in the level below). The Domain must also return a StateMapper instance that projects/maps a state from the lower level into the state space of the current level. Finally, we also have a user supply PolicyGenerator instances to an AMDP agent that plan and spit back policies for any given level for a given reward function or terminal function. These are all handed off to AMDPAgent which will do all the hierarhical planning and execution in some environment defined in the ground truth level 0 representation/action set.

For an example of usage, see the CleanupDomainDriver, which uses our constructed AMDP domains for Cleanup World and runs them in an environment. This example also uses a live visualization tool, VisualStackObserver, which shows the state of the environment and the current "policy stack" controlling the agent and their recent actions in that stack.

Make sure you use the burlap.jar included in this repo, as we're using some small features in a slightly later build that has yet to be pushed to burlap master.

You can compile with `ant` but I highly recommend an IDE like IntelliJ.

