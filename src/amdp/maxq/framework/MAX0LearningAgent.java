package amdp.maxq.framework;

import burlap.behavior.policy.SolverDerivedPolicy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.valuefunction.QValue;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.*;

/**
 * Created by ngopalan on 5/6/16.
 */
public class MAX0LearningAgent implements LearningAgent {

    // Map for values
    Map<GroundedTask, HashMap<HashableState, Double>> qValue = new HashMap<GroundedTask, HashMap<HashableState, Double>>();
    //map for completion function
    Map<GroundedTask ,HashMap<GroundedTask,HashMap<HashableState, Double>>> cValue = new HashMap<GroundedTask ,HashMap<GroundedTask,HashMap<HashableState, Double>>>();

    //map for storing the QProviders
    HashMap<String, QProviderForMAXQ> qProviderMap = new HashMap<String, QProviderForMAXQ>();

    //map for the policy generators for each learner
    HashMap<String, SolverDerivedPolicy> policyMap = new HashMap<String, SolverDerivedPolicy>();

    //map for the action names to grounded tasks!
    HashMap<String, GroundedTask> ActionGroundedTaskMap = new HashMap<String, GroundedTask>();

    //    protected double					epsilon;
    protected Random rand;
    protected double gamma = 0.9;
    protected double VMax = 0.;
    protected double learningRate = 0.1;
    protected int learningEpisodeCount = 0;

    int steps = 0;
    int maxSteps = -1;
    boolean stepsDone = false;
    boolean freezeLearning = false;

    //    List<ArrayList<TaskNode>> taskNodesInLevels = new ArrayList<>();
    TaskNode root;
    HashableStateFactory hsf;
    List<GroundedTask> taskNodesStack = new ArrayList<GroundedTask>();
    int highestCompletedTaskNode;


    /**
     * MAXQ learning agent
     * @param root: the root tasknode
     * @param hashingFactory: a hashing factory to store states
     * @param gamma: discount factor for the Qvalues
     * @param learningRate: learning rate for MAXQ
    //     * @param eps
     */
    public MAX0LearningAgent(TaskNode root, HashableStateFactory hashingFactory, double gamma, double learningRate){
//        this.epsilon = eps;
        rand = RandomFactory.getMapped(0);
        this.gamma = gamma;
        this.learningRate = learningRate;
        this.root = root;
//        this.taskNodesInLevels = taskNodesInLevels;
        this.hsf = hashingFactory;
    }

    public void setRmax(double Rmax){
        this.VMax = Rmax/(1-gamma);
    }

//    public double getEpsilon() {
//        return epsilon;
//    }

    public double getGamma() {
        return gamma;
    }

    public double getVMax() {
        return VMax;
    }

    public double getLearningRate() {
        return learningRate;
    }

//    public void setEpsilon(double epsilon) {
//        this.epsilon = epsilon;
//    }

    public double getCValue(GroundedTask parentTask, GroundedTask childTask, State s){
        return cValue.get(parentTask).get(childTask).get(this.hsf.hashState(s));
    }

    public void setFreezeLearning(boolean freezeLearning) {
        this.freezeLearning = freezeLearning;
    }

    public boolean isFreezeLearning() {
        return freezeLearning;
    }

    public double getVValue(GroundedTask childTask, State s){
        return evaluateMaxNode(childTask,s);
    }


    /**
     * This method provides a QProvider for each task node that can be queried for the qValues of grounded
     * task nodes under it.
     */
    public void setQProviderForTaskNode(TaskNode t){

        QProviderForMAXQ q =  new QProviderForMAXQ(){


            @Override
            public double value(State s) {
                return Helper.maxQ(this, s);
            }

            @Override
            public double qValue(State s, Action a) {

                HashableState hs = hsf.hashState(s);
                // parent grounded task exists with the QProvider
                NonPrimitiveTaskNode currentTaskNode = (NonPrimitiveTaskNode) parentGroundedTask.t;
                TaskNode[] children = currentTaskNode.getChildren();
                for(TaskNode child:children){
                    List<GroundedTask> tempGroundedTaskList = child.getApplicableGroundedTasks(s);
                    for(GroundedTask gt: tempGroundedTaskList){
                        if(gt.action.equals(a)){
                            if (!cValue.containsKey(parentGroundedTask)) {
                                cValue.put(parentGroundedTask, new HashMap<GroundedTask, HashMap<HashableState, Double>>());
                            }
                            if (!cValue.get(parentGroundedTask).containsKey(gt)) {
                                cValue.get(parentGroundedTask).put(gt, new HashMap<HashableState, Double>());
                            }
                            if (!cValue.get(parentGroundedTask).get(gt).containsKey(hs)) {
                                cValue.get(parentGroundedTask).get(gt).put(hs, VMax);
                            }
                            double q = evaluateMaxNode(gt, s) + cValue.get(parentGroundedTask).get(gt).get(hs);
                            return q;
                        }
                    }
                }
                System.err.println("The action does not exist in the list of child tasks!");
                return 0;
            }

            @Override
            public List<QValue> qValues(State s) {
                List<QValue> qValueList = new ArrayList<QValue>();
                NonPrimitiveTaskNode currentTaskNode = (NonPrimitiveTaskNode) parentGroundedTask.t;
                TaskNode[] children = currentTaskNode.getChildren();
                List<GroundedTask> allChildredGroundedTasks = new ArrayList<GroundedTask>();
                for(TaskNode child:children){
                    allChildredGroundedTasks.addAll(child.getApplicableGroundedTasks(s));
                }

                HashableState hs = hsf.hashState(s);

                for(GroundedTask gt : allChildredGroundedTasks) {

                    if (!cValue.containsKey(parentGroundedTask)) {
                        cValue.put(parentGroundedTask, new HashMap<GroundedTask, HashMap<HashableState, Double>>());
                    }
                    if (!cValue.get(parentGroundedTask).containsKey(gt)) {
                        cValue.get(parentGroundedTask).put(gt, new HashMap<HashableState, Double>());
                    }
                    if (!cValue.get(parentGroundedTask).get(gt).containsKey(hs)) {
                        cValue.get(parentGroundedTask).get(gt).put(hs, VMax);
                    }
                    double q = evaluateMaxNode(gt, s) + cValue.get(parentGroundedTask).get(gt).get(hs);
                    qValueList.add(new QValue(s, gt.action,q));

                }
                return qValueList;
            }
        };
        this.qProviderMap.put(t.getName(),q);
    }


    /**
     * This method provides a QProvider for each task node that can be queried for the qValues of grounded
     * task nodes under it.
     */
    public void setSolverDerivedPolicyForTaskNode(TaskNode t, SolverDerivedPolicy policy) {
        this.policyMap.put(t.getName(),policy);
    }



    /**
     * This is an approximation of a GLIE policy (epsilon greedy here) as mentioned in MAXQ
     * @param currentGroundedTask: the chosen grounded task
     * @param s: current state
     * @return
     */
//    public GroundedTask getGLIESubtask(GroundedTask currentGroundedTask, State s){
//        // considering this task node has children it is a non primitive task node
//        NonPrimitiveTaskNode currentTaskNode = (NonPrimitiveTaskNode) currentGroundedTask.t;
//        TaskNode[] children = currentTaskNode.getChildren();
//        List<GroundedTask> allChildredGroundedTasks = new ArrayList<GroundedTask>();
//        for(TaskNode child:children){
//            allChildredGroundedTasks.addAll(child.getApplicableGroundedTasks(s));
//        }
//
//
//        double roll = rand.nextDouble();
//        if(!freezeLearning) {
//            if (roll <= epsilon) {
//                int selected = rand.nextInt(allChildredGroundedTasks.size());
//                GroundedTask gt = allChildredGroundedTasks.get(selected);
//                return gt;
//            }
//        }
////        Map<GroundedTask, Double> qValues = new HashMap<>();
//
//        List<GroundedTask> maxTasks = new ArrayList<GroundedTask>();
//
//        HashableState hs = this.hsf.hashState(s);
//
//        Double maxQ = Double.NEGATIVE_INFINITY;
//        for(GroundedTask gt : allChildredGroundedTasks){
////            double q = getValue(currentGroundedTask, gt, s);
//            if(!cValue.containsKey(currentGroundedTask)){
//                cValue.put(currentGroundedTask,new HashMap<GroundedTask, HashMap<HashableState, Double>>());
//            }
//            if(!cValue.get(currentGroundedTask).containsKey(gt)){
//                cValue.get(currentGroundedTask).put(gt,new HashMap<HashableState, Double>());
//            }
//            if(!cValue.get(currentGroundedTask).get(gt).containsKey(hs)){
//                cValue.get(currentGroundedTask).get(gt).put(hs,VMax);
//            }
//            double q = evaluateMaxNode(gt,s) + cValue.get(currentGroundedTask).get(gt).get(hs);
//            if(maxQ==q){
//                maxTasks.add(gt);
//            }
//            if(maxQ<q){
//                maxTasks.clear();
//                maxTasks.add(gt);
//                maxQ =q;
//            }
//        }
//
//        int selected = rand.nextInt(maxTasks.size());
//        return maxTasks.get(selected);
//    }


    /**
     *This gives the value for a subtask given its parent and current state
     * @param parentTask : parent to the current task
     * @param s : state
     * @return
     */
    public double evaluateMaxNode(GroundedTask parentTask, State s){
        // this task can't be a primitive task, if it is then value should be returned.
        if(parentTask.t.isTaskPrimitive()){
            HashableState hs = this.hsf.hashState(s);
            // if primitive then value for this node exists!
            if(!qValue.containsKey(parentTask)){
                qValue.put(parentTask,new HashMap<HashableState, Double>());
                qValue.get(parentTask).put(hs,VMax);
            }
            if(!qValue.get(parentTask).containsKey(hs)){
                qValue.get(parentTask).put(hs,VMax);
            }
            return qValue.get(parentTask).get(hs);
        }
        List<GroundedTask> maxTasks = new ArrayList<GroundedTask>();
        List<GroundedTask> allChildredGroundedTasks = new ArrayList<GroundedTask>();
        TaskNode[] children = ((NonPrimitiveTaskNode)parentTask.t).getChildren();
        for(TaskNode child:children){
            allChildredGroundedTasks.addAll(child.getApplicableGroundedTasks(s));
        }

        Double maxQ = Double.NEGATIVE_INFINITY;
        HashableState hs = this.hsf.hashState(s);
        for(GroundedTask gt : allChildredGroundedTasks){
//            double q = getValue(parentTask, gt, s) + evaluateMaxNode(gt,s);
            if(!cValue.containsKey(parentTask)){
                cValue.put(parentTask,new HashMap<GroundedTask, HashMap<HashableState, Double>>());
            }
            if(!cValue.get(parentTask).containsKey(gt)){
                cValue.get(parentTask).put(gt,new HashMap<HashableState, Double>());
            }
            if(!cValue.get(parentTask).get(gt).containsKey(hs)){
                cValue.get(parentTask).get(gt).put(hs,VMax);
            }
            double q = cValue.get(parentTask).get(gt).get(hs) + evaluateMaxNode(gt,s);
            if(maxQ==q){
                maxTasks.add(gt);
            }
            if(maxQ<q){
                maxTasks.clear();
                maxTasks.add(gt);
                maxQ =q;
            }
        }
        return maxQ;
    }

//    /**
//     *This gives the value for a subtask given its parent and current state
//     * @param parentTask : parent to the current task
//     * @param childTask : current task
//     * @param s : state
//     * @return
//     */
//    public double getValue(GroundedTask parentTask, GroundedTask childTask, State s){
//        HashableState hs = this.hsf.hashState(s);
//        if(childTask.t.isTaskPrimitive()){
//            // if primitive then value for this node exists!
//            childTask.hashCode();
//            if(!qValue.containsKey(childTask)){
//                qValue.put(childTask,new HashMap<HashableState, Double>());
//                qValue.get(childTask).put(hs,VMax);
//            }
//            if(!qValue.get(childTask).containsKey(hs)){
//                qValue.get(childTask).put(hs,VMax);
//            }
//            return qValue.get(childTask).get(hs);
//        }
//        else{
//            List<GroundedTask> maxTasks = new ArrayList<>();
//            List<GroundedTask> allChildredGroundedTasks = new ArrayList<>();
//            TaskNode[] children = ((NonPrimitiveTaskNode)childTask.t).getChildren();
//            for(TaskNode child:children){
//                allChildredGroundedTasks.addAll(child.getGroundedTasks(s));
//            }
//
//            Double maxQ = Double.NEGATIVE_INFINITY;
//            for(GroundedTask gt : allChildredGroundedTasks){
//                double q = getValue(childTask, gt, s);
//                if(maxQ==q){
//                    maxTasks.add(gt);
//                }
//                if(maxQ<q){
//                    maxTasks.clear();
//                    maxTasks.add(gt);
//                    maxQ =q;
//                }
//            }
//            // to check if things are initialized else add initializations for all states as seen
//            if(!cValue.containsKey(parentTask)){
//                cValue.put(parentTask,new HashMap<GroundedTask, HashMap<HashableState, Double>>());
//            }
//            if(!cValue.get(parentTask).containsKey(childTask)){
//                cValue.get(parentTask).put(childTask,new HashMap<HashableState, Double>());
//            }
//            if(!cValue.get(parentTask).get(childTask).containsKey(hs)){
//                cValue.get(parentTask).get(childTask).put(hs,VMax);
//            }
//            return maxQ + cValue.get(parentTask).get(childTask).get(hs);
//        }
//    }

    /**
     * this is the MAXQ-0 function from the paper
     * @param parentGroundedTask
     * @param env
     * @param ea
     * @param level
     * @return
     */
    public int executeTaskNode(GroundedTask parentGroundedTask, Environment env, Episode ea, int level){

        // maintaining a stack
        if(this.taskNodesStack.size()<=level){
            this.taskNodesStack.add(parentGroundedTask);
        }
        else{
            this.taskNodesStack.set(level, parentGroundedTask);
        }

        if(!parentGroundedTask.t.isTaskPrimitive()){
            //here we fill up the grounded tasks
            TaskNode[] children = ((NonPrimitiveTaskNode)parentGroundedTask.t).getChildren();
            List<GroundedTask> allChildredGroundedTasks = new ArrayList<GroundedTask>();
            for(TaskNode child:children){
                allChildredGroundedTasks.addAll(child.getApplicableGroundedTasks(env.currentObservation()));
            }
            for(GroundedTask gt : allChildredGroundedTasks){
                if(!this.ActionGroundedTaskMap.containsKey(gt.action.actionName())) {
                    this.ActionGroundedTaskMap.put(gt.action.actionName(), gt);
                }
            }
        }

        if(parentGroundedTask.t.isTaskPrimitive()){

            if(steps > maxSteps && maxSteps != -1){
                this.stepsDone = true;
                return 0;
            }
//            ActionType at= ((PrimitiveTaskNode)parentGroundedTask.t).actionType;
//            String str = "";
//            for(int temp = 0;temp<level;temp++){
//                str = str + "       ";
//            }

            Action ga = parentGroundedTask.getAction();
//            System.out.println(ga.actionName());
//            Action ga = at.getGroundedAction((String[])parentGroundedTask.params);

            EnvironmentOutcome eo = env.executeAction(ga);
            //ga.executeIn(env);
            steps++;


            HashableState hs = this.hsf.hashState(eo.o);

            if(!freezeLearning) {
                if (!qValue.containsKey(parentGroundedTask)) {
//                if(a.getName().equals("south")){
//                    System.out.println("hashcode: " + parentGroundedTask.hashCode() + " "
//                            + qValue.containsKey(parentGroundedTask));
//                }
                    qValue.put(parentGroundedTask, new HashMap<HashableState, Double>());
                }
                if (!qValue.get(parentGroundedTask).containsKey(hs)) {
                    qValue.get(parentGroundedTask).put(hs, this.VMax);
                }
                double value = (1 - learningRate) * qValue.get(parentGroundedTask).get(this.hsf.hashState(eo.o))
                        + learningRate * eo.r;
                qValue.get(parentGroundedTask).put(hs, value);
            }
            //record result
            ea.transition(eo);
//            ea.recordTransitionTo(ga, eo.op, eo.r);
            this.highestCompletedTaskNode = this.taskNodesStack.size();
            for(int i=0;i<this.taskNodesStack.size();i++){
                GroundedTask tempTask = this.taskNodesStack.get(i);
                if(tempTask.t.terminal(eo.op,tempTask.action)){
                    this.highestCompletedTaskNode = i;
                    break;
                }
            }
            return 1;
        }
        else{
            int count =0;
            String str = parentGroundedTask.action.actionName();
//            if(((String[])parentGroundedTask.params).length>0){
//                str = str +((String[])parentGroundedTask.params)[0];
//            }
//            System.out.println(str);
            while(!parentGroundedTask.t.terminal(env.currentObservation(),parentGroundedTask.action)){
                HashableState hs = this.hsf.hashState(env.currentObservation());
                //get policy and get task from policy!
                // first set Qprovider
                QProviderForMAXQ QP = this.qProviderMap.get(parentGroundedTask.getT().getName());
                QP.setGt(parentGroundedTask);
                SolverDerivedPolicy p = policyMap.get(parentGroundedTask.t.getName());
                p.setSolver(QP);

                Action ga = p.action(env.currentObservation());
                //TODO: fill up the tasks
                if(!this.ActionGroundedTaskMap.containsKey(ga.actionName())){
                    if(!parentGroundedTask.t.isTaskPrimitive()){
                        //here we fill up the grounded tasks
                        TaskNode[] children = ((NonPrimitiveTaskNode)parentGroundedTask.t).getChildren();
                        List<GroundedTask> allChildredGroundedTasks = new ArrayList<GroundedTask>();
                        for(TaskNode child:children){
                            allChildredGroundedTasks.addAll(child.getApplicableGroundedTasks(env.currentObservation()));
                        }
                        for(GroundedTask gt : allChildredGroundedTasks){
                            if(!this.ActionGroundedTaskMap.containsKey(gt.action.actionName())) {
                                this.ActionGroundedTaskMap.put(gt.action.actionName(), gt);
                            }
                        }
                    }
                }

                GroundedTask nextTask = this.ActionGroundedTaskMap.get(ga.actionName());

                int steps = executeTaskNode(nextTask,env,ea,level+1);
                if(this.stepsDone){
                    return steps+count;
                }
                count+=steps;
                if(this.highestCompletedTaskNode<=level)
                {
                    if(!freezeLearning) {
                        State lastState = ea.state(ea.stateSequence.size() - 1);
                        boolean childTaskComplete = nextTask.t.terminal(lastState, nextTask.action);
                        if (childTaskComplete) {
                            if (!cValue.containsKey(parentGroundedTask)) {
                                cValue.put(parentGroundedTask, new HashMap<GroundedTask, HashMap<HashableState, Double>>());
                            }
                            if (!cValue.get(parentGroundedTask).containsKey(nextTask)) {
                                cValue.get(parentGroundedTask).put(nextTask, new HashMap<HashableState, Double>());
                            }
                            if (!cValue.get(parentGroundedTask).get(nextTask).containsKey(hs)) {
                                cValue.get(parentGroundedTask).get(nextTask).put(hs, VMax);
                            }
                            double value = (1 - learningRate) * cValue.get(parentGroundedTask).get(nextTask).get(hs)
                                    + learningRate * Math.pow(gamma, steps)
                                    * this.evaluateMaxNode(parentGroundedTask, env.currentObservation());
                            cValue.get(parentGroundedTask).get(nextTask).put(hs, value);
                        }
                    }
                    return count;
                }
                if(!freezeLearning) {
                    if (!cValue.containsKey(parentGroundedTask)) {
                        cValue.put(parentGroundedTask, new HashMap<GroundedTask, HashMap<HashableState, Double>>());
                    }
                    if (!cValue.get(parentGroundedTask).containsKey(nextTask)) {
                        cValue.get(parentGroundedTask).put(nextTask, new HashMap<HashableState, Double>());
                    }
                    if (!cValue.get(parentGroundedTask).get(nextTask).containsKey(hs)) {
                        cValue.get(parentGroundedTask).get(nextTask).put(hs, VMax);
                    }
                    double value = (1 - learningRate) * cValue.get(parentGroundedTask).get(nextTask).get(hs)
                            + learningRate * Math.pow(gamma, steps)
                            * this.evaluateMaxNode(parentGroundedTask, env.currentObservation());
                    cValue.get(parentGroundedTask).get(nextTask).put(hs, value);
                }
            }
            return count;
        }


    }


    @Override
    public Episode runLearningEpisode(Environment environment) {
        return runLearningEpisode(environment, -1);
    }

    @Override
    public Episode runLearningEpisode(Environment env, int maxSteps) {
        this.maxSteps = maxSteps;
        this.steps =0;
        this.stepsDone=false;
        Episode ea = new Episode(env.currentObservation());
        System.out.println(learningEpisodeCount);
        if(!this.freezeLearning){
            this.learningEpisodeCount++;
        }
        //behave until a terminal state or max steps is reached
        State curState = env.currentObservation();



        while(!env.isInTerminalState() && (steps < maxSteps || maxSteps == -1)){
            // there is only one root task node in MAXQ
            GroundedTask gtRoot = ((NonPrimitiveTaskNode)(this.root)).getApplicableGroundedTasks(curState).get(0);
            this.taskNodesStack.add(gtRoot);
            int level=0;
            int executedSteps = executeTaskNode(gtRoot, env, ea, level);
//            steps+=executedSteps;
        }

        return ea;
    }





}


