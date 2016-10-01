//package amdp.cleanupturtlebot.cleanupl0discrete;
//
//import amdp.amdpframework.AMDPPolicyGenerator;
//import amdp.amdpframework.GroundedTask;
//import amdp.amdpframework.NonPrimitiveTaskNode;
//import amdp.amdpframework.TaskNode;
//import amdp.tools.StackObserver;
//import burlap.behavior.policy.Policy;
//import burlap.behavior.singleagent.Episode;
//import burlap.debugtools.DPrint;
//import burlap.mdp.core.action.Action;
//import burlap.mdp.core.oo.ObjectParameterizedAction;
//import burlap.mdp.core.oo.state.generic.GenericOOState;
//import burlap.mdp.core.state.State;
//import burlap.mdp.singleagent.environment.Environment;
//import burlap.mdp.singleagent.environment.EnvironmentOutcome;
//import burlap.mdp.singleagent.oo.OOSADomain;
//import org.apache.commons.lang3.StringUtils;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class AMDPAgent {
//
//
//	List<AMDPPolicyGenerator> PolicyGenerators;
//	Map<String, GroundedTask> actionToGroundedTaskMap = new HashMap<String, GroundedTask>();
//	Map<String, OOSADomain> actionToDomainMap = new HashMap<String, OOSADomain>();
//
//	// This is a stack of states storing states at each level.
//	List<State> StateStack = new ArrayList<State>();
//
//	GroundedTask rootGroundedTask;
//
//	protected int debugCode = 3214986;
//
//	protected int stepCount = 0;
//
//	protected int maxLevel;
//
//	protected List<List<Action>> policyStack;
//
//	protected StackObserver onlineStackObserver;
//
//
//
//
//	public AMDPAgent(GroundedTask rootGroundedTask, List<AMDPPolicyGenerator> inputPolicyGenerators){
//
//		this.rootGroundedTask = rootGroundedTask;
//
//		this.actionToGroundedTaskMap.put(rootGroundedTask.action.actionName(), rootGroundedTask);
//		this.actionToDomainMap.put(rootGroundedTask.action.actionName(),rootGroundedTask.groundedDomain());
//
//
//
////		if(inputDomainList.size()!=inputPolicyGenerators.size()){
////			System.err.print("The number of domains ("+ inputDomainList + "), is not equal to the number of policy generators(" + inputPolicyGenerators+")");
////			System.exit(-10);
////		}
//
//
//		this.PolicyGenerators = inputPolicyGenerators;
//
//		for (int i = 0; i < this.PolicyGenerators.size(); i++) {
//			StateStack.add(new GenericOOState());
//			}
//		this.maxLevel = this.PolicyGenerators.size()-1;
//
//		this.policyStack = new ArrayList<List<Action>>(PolicyGenerators.size());
//		for(int i = 0; i < PolicyGenerators.size(); i++){
//			this.policyStack.add(new ArrayList<Action>());
//		}
//	}
//
//	public List<List<Action>> getPolicyStack() {
//		return policyStack;
//	}
//
//	public StackObserver getOnlineStackObserver() {
//		return onlineStackObserver;
//	}
//
//	public void setOnlineStackObserver(StackObserver onlineStackObserver) {
//		this.onlineStackObserver = onlineStackObserver;
//	}
//
//	public Episode actUntilTermination(Environment env){
//
//		return this.actUntilTermination(env, -1);
//
//	}
//
//	public Episode actUntilTermination(Environment env, int maxSteps){
//
//		State baseState = env.currentObservation();
//
//		StateStack.set(0, baseState);
//
//
//
//		Episode ea = new Episode(baseState);
//
//		for(int i = 1; i< PolicyGenerators.size();i++){
//			AMDPPolicyGenerator p = this.PolicyGenerators.get(i);
//			baseState = p.generateAbstractState(baseState);
//			StateStack.set(i, baseState);
//		}
//
//
//		//TODO: need a root task to start decomposing!
//		decompose(env, PolicyGenerators.size()-1, rootGroundedTask, maxSteps, ea);
//
//
//		return ea;
//	}
//
//
//
//	protected void decompose(Environment env, int level, GroundedTask gt, int maxSteps, Episode ea){
//		State s = StateStack.get(level);
//
//
//
//		Policy pi = PolicyGenerators.get(level).generatePolicy(s, gt);
//		if(level !=0){
//
//
//			while(!gt.terminalFunction().isTerminal(s) && (stepCount < maxSteps || maxSteps == -1)){
//				TaskNode[] childTaskNodes = ((NonPrimitiveTaskNode)gt.t).childTaskNodes;
////					List<GroundedTask> childGroundedTaskList = gt.t.getApplicableGroundedTasks(s);
//
//				addTasksToMap(childTaskNodes, s, level);
//				Action a = pi.action(s);
//				//TODO: get child grounded task
//				String str = StringUtils.repeat("	", maxLevel - level);
//				str = str + a.toString();
//				DPrint.cl(debugCode , str);
//				this.policyStack.get(level).add(a);
//				if(this.onlineStackObserver != null){
//					this.onlineStackObserver.updatePolicyStack(this.policyStack);
//				}
//				String tempStr ="";
//				if(a instanceof ObjectParameterizedAction){
//					String[] params = ((ObjectParameterizedAction)a).getObjectParameters();
//					if(params!=null) {
//						for (int i = 0; i < params.length; i++) {
//							tempStr = tempStr + "_" + params[i];
//						}
//					}
//				}
//
//				decompose(env, level - 1, actionToGroundedTaskMap.get(a.actionName() + tempStr + "_" + level), maxSteps, ea);
//				s = StateStack.get(level);
//			}
//		}
//		else{
//			while((!env.isInTerminalState() && !gt.terminalFunction().isTerminal(s) )&& (stepCount < maxSteps || maxSteps == -1)){
//				// this is a grounded action at the base level
//				Action ga = pi.action(s);
//				this.policyStack.get(level).add(ga);
//				if(this.onlineStackObserver != null){
//					this.onlineStackObserver.updatePolicyStack(this.policyStack);
//				}
//
//				EnvironmentOutcome eo = env.executeAction(ga);
//
//				String str = StringUtils.repeat("	", maxLevel - level);
//				str = str + ga.toString();
//				DPrint.cl(debugCode , str);
//				ea.transition(eo);
////				ea.recordTransitionTo(ga, eo.op, eo.r);
//				StateStack.set(level, eo.op);
//				s = eo.op;
//				stepCount++;
//			}
//
//		}
//
//		if(level < PolicyGenerators.size() -1){
//			// project state up and getting new next state after running a policy to termination
//			StateStack.set(level+1, PolicyGenerators.get(level+1).generateAbstractState(StateStack.get(level)));
//		}
//
//		this.policyStack.get(level).clear();
//	}
//
//	private void addTasksToMap(TaskNode[] childTaskNodes, State s, int level) {
//		List<GroundedTask> childGroundedTaskList = new ArrayList<GroundedTask>();
//		for(int i=0;i<childTaskNodes.length;i++){
//			TaskNode t = childTaskNodes[i];
//			 childGroundedTaskList.addAll(t.getApplicableGroundedTasks(s));
//		}
//		for(GroundedTask gt:childGroundedTaskList){
//			Action a =gt.action;
//			String tempStr ="";
//			if(a instanceof ObjectParameterizedAction){
//				String[] params = ((ObjectParameterizedAction)a).getObjectParameters();
//				if(params!=null) {
//					for (int i = 0; i < params.length; i++) {
//						tempStr = tempStr + "_" + params[i];
//					}
//				}
//			}
//			if(!actionToGroundedTaskMap.containsKey(a.actionName() + tempStr + "_" + level)){
//				actionToGroundedTaskMap.put(a.actionName() + tempStr + "_" + level, gt);
//			}
//		}
//	}
//
//
//
//
//}
