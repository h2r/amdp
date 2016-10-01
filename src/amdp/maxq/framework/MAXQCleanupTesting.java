package amdp.maxq.framework;

import amdp.maxq.framework.*;
import amdp.utilities.BoltzmannQPolicyWithCoolingSchedule;
import burlap.behavior.policy.SolverDerivedPolicy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.valuefunction.QValue;
import burlap.debugtools.DPrint;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.oo.ObjectParameterizedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Created by ngopalan on 5/6/16.
 */
public class MAXQCleanupTesting implements LearningAgent {

    //    protected double					epsilon;
    protected Random rand;
    protected double gamma = 0.9;
    protected double VMax = 0.;
    protected double learningRate = 0.1;
    protected int learningEpisodeCount = 0;
    public static int debugCode = 235867;

    // Map for values
    Map<GroundedTask, HashMap<HashableState, Double>> qValue = new HashMap<GroundedTask, HashMap<HashableState, Double>>();
    //map for completion function
    //    Map<GroundedTask ,HashMap<GroundedTask,HashMap<HashableState, Double>>> C = new HashMap<GroundedTask ,HashMap<GroundedTask,HashMap<HashableState, Double>>>();
    CValuesStore C = new CValuesStore(VMax);

    //cTildeMap
    CValuesStore CTilde = new CValuesStore(VMax);
    //    Map<GroundedTask ,HashMap<GroundedTask,HashMap<HashableState, Double>>> CTilde = new HashMap<GroundedTask ,HashMap<GroundedTask,HashMap<HashableState, Double>>>();

    //map for storing the QProviders
    HashMap<String, QProviderForMAXQ> qProviderMap = new HashMap<String, QProviderForMAXQ>();

    //map for the policy generators for each learner
    HashMap<String, SolverDerivedPolicy> policyMap = new HashMap<String, SolverDerivedPolicy>();

    //map for the action names to grounded tasks!
    HashMap<String, GroundedTask> ActionGroundedTaskMap = new HashMap<String, GroundedTask>();



    int steps = 0;
    int maxSteps = -1;
    boolean stepsDone = false;
    boolean freezeLearning = false;

    //    List<ArrayList<TaskNode>> taskNodesInLevels = new ArrayList<>();
    TaskNode root;
    HashableStateFactory hsf;
    List<GroundedTask> taskNodesStack = new ArrayList<GroundedTask>();
    int highestCompletedTaskNode;

    int numberOfBackups = 0;
    int maxNumberOfBackups = -1;
    boolean learningStopped = false;

    //TODO: need to add a feature not pick greedily from non-tested actions when not learning!


    /**
     * MAXQ learning agent
     * @param root: the root tasknode
     * @param hashingFactory: a hashing factory to store states
     * @param gamma: discount factor for the Qvalues
     * @param learningRate: learning rate for MAXQ
    //     * @param eps
     */
    public MAXQCleanupTesting(TaskNode root, HashableStateFactory hashingFactory, double gamma, double learningRate){
        //        this.epsilon = eps;
        rand = RandomFactory.getMapped(0);
        this.gamma = gamma;
        this.learningRate = learningRate;
        this.root = root;
        //        this.taskNodesInLevels = taskNodesInLevels;
        this.hsf = hashingFactory;
    }

    public MAXQCleanupTesting(TaskNode root, HashableStateFactory hashingFactory, double gamma, double learningRate, int maxBackups){
        //        this.epsilon = eps;
        rand = RandomFactory.getMapped(0);
        this.gamma = gamma;
        this.learningRate = learningRate;
        this.root = root;
        //        this.taskNodesInLevels = taskNodesInLevels;
        this.hsf = hashingFactory;
        this.maxNumberOfBackups = maxBackups;
    }



    public void setRmax(double Rmax){
        this.VMax = Rmax/(1-gamma);
        this.C  = new CValuesStore(VMax);
        this.CTilde  = new CValuesStore(VMax);
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

    public double getC(GroundedTask parentTask, GroundedTask childTask, State s){
        if(((NonPrimitiveTaskNode)parentTask.getT()).hasHashingFactory()){
            return C.get(parentTask, childTask, ((NonPrimitiveTaskNode)parentTask.getT()).hashedState(s,childTask));
        }
        return C.get(parentTask, childTask, this.hsf.hashState(s));
    }

    public void setFreezeLearning(boolean freezeLearning) {
        this.freezeLearning = freezeLearning;
    }

    public boolean isFreezeLearning() {
        return freezeLearning;
    }

    public double getVValue(GroundedTask childTask, State s){
        return evaluateMaxNode(childTask,s).getRight();
    }

    public int getNumberOfBackups() {
        return numberOfBackups;
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
                // the qValues being returned are those from CTilde and not C, evaluate will return true estimates of C itself
                //TODO: we might be wrong here and this might be a C value

                HashableState hs ;//= hsf.hashState(s);
                // parent grounded task exists with the QProvider
                NonPrimitiveTaskNode currentTaskNode = (NonPrimitiveTaskNode) parentGroundedTask.t;



                TaskNode[] children = currentTaskNode.getChildren();
                for(TaskNode child:children){
                    List<GroundedTask> tempGroundedTaskList = child.getApplicableGroundedTasks(s);
                    for(GroundedTask gt: tempGroundedTaskList){
                        if(gt.action.equals(a)){
                            if(((NonPrimitiveTaskNode)parentGroundedTask.getT()).hasHashingFactory()){
                                hs =  ((NonPrimitiveTaskNode)parentGroundedTask.getT()).hashedState(s,gt);
                            }
                            else {
                                hs = hsf.hashState(s);
                            }
                            double q = evaluateMaxNode(gt, s).getRight() + CTilde.get(parentGroundedTask,gt, hs);
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

                for(GroundedTask gt : allChildredGroundedTasks) {
                    double q = this.qValue(s,gt.action);

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
     *This gives the value for a subtask given its parent and current state
     * @param parentTask : parent to the current task
     * @param s : state
     * @return
     */
    public Pair<Action, Double> evaluateMaxNode(GroundedTask parentTask, State s){
        // this task can't be a primitive task, if it is then value should be returned.
        //TODO: need to add pseudo reward function check here??? dunno what this todo is all about
        int level = parentTask.level;
        if(parentTask.t.isTaskPrimitive()){
            HashableState hs;// = this.hsf.hashState(s);
            //TODO: query primitive node for hs
            if(((PrimitiveTaskNode)parentTask.getT()).hasHashingFactory()){
                hs =  ((PrimitiveTaskNode)parentTask.getT()).hashedState(s);
            }
            else {
                hs = this.hsf.hashState(s);
            }

            // if primitive then value for this node exists!
            if(!qValue.containsKey(parentTask)){
                qValue.put(parentTask,new HashMap<HashableState, Double>());
                qValue.get(parentTask).put(hs,VMax);
            }
            if(!qValue.get(parentTask).containsKey(hs)){
                qValue.get(parentTask).put(hs,VMax);
            }
            return new MutablePair(parentTask.action,qValue.get(parentTask).get(hs));
        }


        // in a non-primitive task list grounded tasks
        List<GroundedTask> maxTasks = new ArrayList<GroundedTask>();
        List<Double> maxValues = new ArrayList<Double>();
        List<GroundedTask> allChildrenGroundedTasks = new ArrayList<GroundedTask>();
        TaskNode[] children = ((NonPrimitiveTaskNode)parentTask.t).getChildren();
        for(TaskNode child:children){
            allChildrenGroundedTasks.addAll(child.getApplicableGroundedTasks(s));
        }
        for(GroundedTask gt : allChildrenGroundedTasks){
            if(!this.ActionGroundedTaskMap.containsKey(actionName(gt.action,level+1))) {
                this.ActionGroundedTaskMap.put(actionName(gt.action,level+1), gt);
            }
        }

        Double maxQ = Double.NEGATIVE_INFINITY;
        HashableState hs;// = this.hsf.hashState(s);
        for(GroundedTask gt : allChildrenGroundedTasks){
            if(((NonPrimitiveTaskNode)parentTask.getT()).hasHashingFactory()){
                hs =  ((NonPrimitiveTaskNode)parentTask.getT()).hashedState(s,gt);
            }
            else {
                hs = this.hsf.hashState(s);
            }
            double subtreeEvaluation  = evaluateMaxNode(gt,s).getRight();
            double explorationQ = CTilde.get(parentTask, gt, hs) + subtreeEvaluation;
            double returnedQ = C.get(parentTask, gt, hs) + subtreeEvaluation;
            if(maxQ==explorationQ){
                maxTasks.add(gt);
                maxValues.add(returnedQ);
            }
            if(maxQ<explorationQ){
                maxTasks.clear();
                maxTasks.add(gt);
                maxValues.clear();
                maxValues.add(returnedQ);
                maxQ = explorationQ;
            }
        }
        // return the true value to the outside subtree of the chosen explored action!
        double maxReturnQ =Double.NEGATIVE_INFINITY;
        int maxValueIndex = -1;
        for(int i =0;i< maxValues.size();i++){
            if(maxValues.get(i) > maxReturnQ){
                maxValueIndex = i;
            }
        }
        return new MutablePair<Action, Double>(maxTasks.get(maxValueIndex).action,maxValues.get(maxValueIndex));
    }


    /**
     * this is the MAXQ-Q function from the paper
     * @param parentGroundedTask
     * @param env
     * @param ea
     * @param level
     * @return List of observed states
     */
    public List<State> MAXQ_Q(GroundedTask parentGroundedTask, Environment env, Episode ea, int level){
        // maintaining a stack
        if(this.taskNodesStack.size()<=level){
            this.taskNodesStack.add(parentGroundedTask);
        }
        else{
            this.taskNodesStack.set(level, parentGroundedTask);
        }

        // if not primitive get all possible children grounded tasks for current state
        if(!parentGroundedTask.t.isTaskPrimitive()){
            //here we fill up the grounded tasks to the action map
            TaskNode[] children = ((NonPrimitiveTaskNode)parentGroundedTask.t).getChildren();
            List<GroundedTask> allChildrenGroundedTasks = new ArrayList<GroundedTask>();
            for(TaskNode child:children){
                allChildrenGroundedTasks.addAll(child.getApplicableGroundedTasks(env.currentObservation()));
            }
            for(GroundedTask gt : allChildrenGroundedTasks){
                if(!this.ActionGroundedTaskMap.containsKey(actionName(gt.action,level+1))) {
                    this.ActionGroundedTaskMap.put(actionName(gt.action,level+1), gt);
                }
            }
        }

        if(parentGroundedTask.t.isTaskPrimitive()){

            //            if(ea.discountedReturn(1.0)<-6000){
            //                System.out.println("many negative actions being taken!");
            //            }

            if((steps > maxSteps && maxSteps != -1) || (!freezeLearning && learningStopped)){
                //if max steps have been taken return empty array!
                this.stepsDone = true;
                return new ArrayList<State>();
            }
            Action ga = parentGroundedTask.getAction();
            EnvironmentOutcome eo = env.executeAction(ga);
            steps++;




            HashableState hs;// = this.hsf.hashState(eo.o);

            if(((PrimitiveTaskNode)parentGroundedTask.getT()).hasHashingFactory()){
                hs =  ((PrimitiveTaskNode)parentGroundedTask.getT()).hashedState(eo.o);
            }
            else {
                hs = this.hsf.hashState(eo.o);
            }

            if(!freezeLearning) {
                if (!qValue.containsKey(parentGroundedTask)) {
                    qValue.put(parentGroundedTask, new HashMap<HashableState, Double>());
                }
                if (!qValue.get(parentGroundedTask).containsKey(hs)) {
                    qValue.get(parentGroundedTask).put(hs, this.VMax);
                }
                //TODO: call reward decomposition function here that seperates rewards on a node basis!

                double value = (1 - learningRate) * qValue.get(parentGroundedTask).get(hs)
                        + learningRate * eo.r;


                if((numberOfBackups<maxNumberOfBackups || maxNumberOfBackups==-1)) {
                    numberOfBackups++;
                    qValue.get(parentGroundedTask).put(hs, value);
                }
                if(numberOfBackups==maxNumberOfBackups && maxNumberOfBackups!=-1){
                    learningStopped=true;
                }
            }

            //record result
            ea.transition(eo);



            this.highestCompletedTaskNode = this.taskNodesStack.size();
            // to see if a higher task node has been completed
            for(int i=0;i<this.taskNodesStack.size();i++){
                GroundedTask tempTask = this.taskNodesStack.get(i);
                if(tempTask.t.terminal(eo.op,tempTask.action)){
                    this.highestCompletedTaskNode = i;
                    break;
                }
            }
            List<State> returnStateList = new ArrayList<State>();
            returnStateList.add(eo.o.copy());
            return returnStateList;
        }
        else{
            // return state list is a linked list as the sequence will be prepended to it
            List<State> returnStateList = new ArrayList<State>();

            while(!parentGroundedTask.t.terminal(env.currentObservation(),parentGroundedTask.action)){
                State startState =env.currentObservation().copy();

                //                HashableState hashedStartState  = this.hsf.hashState(startState);

                //get policy and get task from policy!
                // first set Qprovider
                QProviderForMAXQ QP = this.qProviderMap.get(parentGroundedTask.getT().getName());
                QP.setGt(parentGroundedTask);
                //                SolverDerivedPolicy p = policyMap.get(parentGroundedTask.t.getName());
                SolverDerivedPolicy p;
                if(!freezeLearning){
                    p = policyMap.get(parentGroundedTask.t.getName());
                }
                else {
                    // this happens when testing!
                    //                    p = new GreedyDeterministicQPolicy();
                    p = policyMap.get(parentGroundedTask.t.getName());
                    ((BoltzmannQPolicyWithCoolingSchedule)p).setNoCoolingNextAction(true);
                }
                p.setSolver(QP);


                Action ga = p.action(env.currentObservation());


                if(!this.ActionGroundedTaskMap.containsKey(actionName(ga,level+1))){
                    if(!parentGroundedTask.t.isTaskPrimitive()){
                        //here we fill up the grounded tasks
                        TaskNode[] children = ((NonPrimitiveTaskNode)parentGroundedTask.t).getChildren();
                        List<GroundedTask> allChildredGroundedTasks = new ArrayList<GroundedTask>();
                        for(TaskNode child:children){
                            allChildredGroundedTasks.addAll(child.getApplicableGroundedTasks(startState));
                        }
                        for(GroundedTask gt : allChildredGroundedTasks){
                            if(!this.ActionGroundedTaskMap.containsKey(actionName(gt.action,level+1))) {
                                this.ActionGroundedTaskMap.put(actionName(gt.action,level+1), gt);
                            }
                        }
                    }
                }

                GroundedTask currentGroundedSubTask = this.ActionGroundedTaskMap.get(actionName(ga,level+1));




                List<State> childSeq = MAXQ_Q(currentGroundedSubTask,env,ea,level+1);
                returnStateList.addAll(new ArrayList<State>(childSeq));
                if(this.stepsDone){
                    return returnStateList;
                }

                //if a higher task is terminated we update only the tasks in the stack that were finished!!
                if(this.highestCompletedTaskNode<=level)
                {
                    if(!freezeLearning) {

                        State lastState = env.currentObservation();
                        HashableState hashedLastState;//

                        boolean childTaskComplete = currentGroundedSubTask.t.terminal(lastState, currentGroundedSubTask.action);

                        // choose max action from the current subtask
                        Action a_star = evaluateMaxNode(parentGroundedTask,lastState).getLeft();

                        GroundedTask gt_star = this.ActionGroundedTaskMap.get(actionName(a_star,level+1));
                        //                        int N = 1;

//                        if(gt_star.action.actionName().split(":")[0].equals("navigate")){
//                            System.out.println("here");
//                        }



                        if(((NonPrimitiveTaskNode)parentGroundedTask.getT()).hasHashingFactory()){
                            hashedLastState =  ((NonPrimitiveTaskNode)parentGroundedTask.getT()).hashedState(lastState,gt_star);
                        }
                        else {
                            hashedLastState = this.hsf.hashState(lastState);
                        }

                        if (childTaskComplete) {
                            for(int i=childSeq.size()-1;i>=0;i--) {
                                //update CTildeValue if values not present

                                State currentState = childSeq.get(i);
                                HashableState hashedCurrentState;// = this.hsf.hashState(currentState);

                                if(((NonPrimitiveTaskNode)parentGroundedTask.getT()).hasHashingFactory()){
                                    hashedCurrentState =  ((NonPrimitiveTaskNode)parentGroundedTask.getT()).hashedState(currentState,currentGroundedSubTask);
                                }
                                else {
                                    hashedCurrentState = this.hsf.hashState(currentState);
                                }


                                double updateCTildeValue=0.;
                                double updateCValue=0.;



                                if(parentGroundedTask.t.terminal(lastState,parentGroundedTask.action)){
                                    // if the parent task is finished the completion function only updates w.r.t. the pseudo reward!
                                    updateCTildeValue = (1 - learningRate)
                                            * CTilde.get(parentGroundedTask,currentGroundedSubTask,hashedCurrentState)
                                            + learningRate * Math.pow(gamma, childSeq.size()-i)
                                            * (((NonPrimitiveTaskNode)parentGroundedTask.t).pseudoRewardFunction(lastState,currentGroundedSubTask));

                                    // this value is slowly moving to 0. as the part missing the operation is + learningRate*0.
                                    updateCValue = (1 - learningRate)
                                            *C.get(parentGroundedTask,currentGroundedSubTask,hashedCurrentState);
                                }
                                else{
                                    double V_aStar_sTick  = this.evaluateMaxNode(gt_star, lastState).getRight();
                                    // SARSA kind of update for CTilde
                                    updateCTildeValue = (1 - learningRate)
                                            * CTilde.get(parentGroundedTask, currentGroundedSubTask, hashedCurrentState)
                                            + learningRate * Math.pow(gamma, childSeq.size()-i)
                                            * (((NonPrimitiveTaskNode)parentGroundedTask.t).pseudoRewardFunction(lastState,currentGroundedSubTask)
                                            + this.CTilde.get(parentGroundedTask, gt_star, hashedLastState)
                                            + V_aStar_sTick);

                                    updateCValue = (1 - learningRate)
                                            *C.get(parentGroundedTask, currentGroundedSubTask, hashedCurrentState)
                                            + learningRate * Math.pow(gamma, childSeq.size()-i)
                                            *(C.get(parentGroundedTask, gt_star, hashedLastState)
                                            + V_aStar_sTick);

                                }



                                if((numberOfBackups<maxNumberOfBackups || maxNumberOfBackups==-1)) {
                                    numberOfBackups++;
                                    CTilde.put(parentGroundedTask, currentGroundedSubTask, hashedCurrentState, updateCTildeValue);
                                    C.put(parentGroundedTask, currentGroundedSubTask, hashedCurrentState, updateCValue);
                                }
                                if(numberOfBackups==maxNumberOfBackups && maxNumberOfBackups!=-1){
                                    learningStopped=true;
                                }

                            }
                        }
                    }
                    return returnStateList;
                }
                // since no higher tasks terminated update currently terminated task
                if(!freezeLearning) {

                    State lastState = env.currentObservation();
                    HashableState hashedLastState;//  =this.hsf.hashState(lastState);

                    // choose max action from the current subtask
                    Action a_star = evaluateMaxNode(parentGroundedTask,lastState).getLeft();

                    GroundedTask gt_star = this.ActionGroundedTaskMap.get(actionName(a_star,level+1));
                    //                    int N = 1;
                    if(((NonPrimitiveTaskNode)parentGroundedTask.getT()).hasHashingFactory()){
                        hashedLastState =  ((NonPrimitiveTaskNode)parentGroundedTask.getT()).hashedState(lastState,gt_star);
                    }
                    else {
                        hashedLastState = this.hsf.hashState(lastState);
                    }


                    for(int i=0;i<childSeq.size();i++) {
                        //update CTildeValue
                        State currentState = childSeq.get(i);
                        HashableState hashedCurrentState;// = this.hsf.hashState(currentState);

                        if(((NonPrimitiveTaskNode)parentGroundedTask.getT()).hasHashingFactory()){
                            hashedCurrentState =  ((NonPrimitiveTaskNode)parentGroundedTask.getT()).hashedState(currentState,currentGroundedSubTask);
                        }
                        else {
                            hashedCurrentState = this.hsf.hashState(currentState);
                        }

                        double updateCTildeValue=0.;
                        double updateCValue=0.;

                        if(parentGroundedTask.t.terminal(lastState,parentGroundedTask.action)){
                            // if the parent task is finished the completion function only updates w.r.t. the pseudo reward!
                            updateCTildeValue = (1 - learningRate)
                                    * CTilde.get(parentGroundedTask, currentGroundedSubTask, hashedCurrentState)
                                    + learningRate * Math.pow(gamma, childSeq.size()-i)
                                    * (((NonPrimitiveTaskNode)parentGroundedTask.t).pseudoRewardFunction(lastState,currentGroundedSubTask));

                            // this value is slowly moving to 0. as the part missing the operation is + learningRate*0.
                            updateCValue = (1 - learningRate)
                                    *C.get(parentGroundedTask, currentGroundedSubTask, hashedCurrentState);
                        }
                        else{
                            double V_aStar_sTick  = this.evaluateMaxNode(gt_star, lastState).getRight();
                            // SARSA kind of update for CTilde
                            updateCTildeValue = (1 - learningRate)
                                    * CTilde.get(parentGroundedTask, currentGroundedSubTask, hashedCurrentState)
                                    + learningRate * Math.pow(gamma, childSeq.size()-i)
                                    * (((NonPrimitiveTaskNode)parentGroundedTask.t).pseudoRewardFunction(lastState,currentGroundedSubTask)
                                    + this.CTilde.get(parentGroundedTask, gt_star, hashedLastState)
                                    + V_aStar_sTick);

                            updateCValue = (1 - learningRate)
                                    *C.get(parentGroundedTask, currentGroundedSubTask, hashedCurrentState)
                                    + learningRate * Math.pow(gamma, childSeq.size()-i)
                                    *(C.get(parentGroundedTask, gt_star, hashedLastState)
                                    + V_aStar_sTick);

                        }


                        if((numberOfBackups<maxNumberOfBackups || maxNumberOfBackups==-1)){
                            numberOfBackups++;
                            CTilde.put(parentGroundedTask, currentGroundedSubTask, hashedCurrentState, updateCTildeValue);
                            C.put(parentGroundedTask, currentGroundedSubTask, hashedCurrentState, updateCValue);
                        }
                        if(numberOfBackups==maxNumberOfBackups && maxNumberOfBackups!=-1){
                            learningStopped=true;
                        }
                        //                        N++;
                    }

                }
            }
            return returnStateList;
        }


    }



    public int numberOfParams(){
        int numParams =0;

        int CSize = C.numberOfParams();
        String str = "Params in C " + CSize;
        DPrint.cl(debugCode , str);



        int CTildeSize = CTilde.numberOfParams();
        str = "Params in CTilde " + CTildeSize;
        DPrint.cl(debugCode , str);

        for(GroundedTask gt : qValue.keySet()){
            numParams += qValue.get(gt).size();
        }

        int VSize = numParams;
        str = "Params in V: " +VSize;
        DPrint.cl(debugCode , str);

        return numParams + CTildeSize + CSize;
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
        String str = ""+learningEpisodeCount;
        DPrint.cl(debugCode , str);

        if(!this.freezeLearning){
            this.learningEpisodeCount++;
        }
        //behave until a terminal state or max steps is reached
        State curState = env.currentObservation();



        while(!env.isInTerminalState() && (steps < maxSteps || maxSteps == -1) && !this.stepsDone){
            // there is only one root task node in MAXQ
            GroundedTask gtRoot = ((NonPrimitiveTaskNode)(this.root)).getApplicableGroundedTasks(curState).get(0);
            this.taskNodesStack.add(gtRoot);
            int level=0;
            int executedSteps = MAXQ_Q(gtRoot, env, ea, level).size();
        }

        return ea;
    }

    public void setVmax(double vmax) {
        this.VMax = vmax;
    }

    public String actionName(GroundedTask gt, int level){
        String s = gt.action.actionName();
        if(gt.action instanceof ObjectParameterizedAction){
            String[] params = ((ObjectParameterizedAction)gt.action).getObjectParameters();
            for(String param :params){
                s+="_"+param;
            }
        }
        s+="_"+level;
        return s;
    }

    public String actionName(Action action, int level){
        String s = action.actionName();
        if(action instanceof ObjectParameterizedAction){
            String[] params = ((ObjectParameterizedAction)action).getObjectParameters();
            for(String param :params){
                s+="_"+param;
            }
        }
        s+="_"+level;
        return s;
    }
}
