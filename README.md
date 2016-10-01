# amdp
Repo for AMDP research. 

The master branch is the latest DAG hierarchy code on `BURLAP 3.0`. The older functioning AMDP code is on `BURLAP 2.0` on the branch `amdpBurlap2.

Each goal description has multiple subgoals. These subgoals are achieved as a subtask with an AMDP associated with them. This leads to a DAG formation of subtasks. Each AMDP is coded as a BURLAP domain. When the AMDP is used as a subtask a child parent hierarchy is described for them in the AMDP runner file. Example AMDP runner files are `amdp.cleanupamdpdomains.cleanupamdp.CleanupDriver` and `amdp.taxiamdpdomains.taxiamdp.TaxiAMDPDriver`. There is corresponding MAXQ hierarchies for these domains within the codebase.



