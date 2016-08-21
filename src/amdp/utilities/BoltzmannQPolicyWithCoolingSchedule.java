package amdp.utilities;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.PolicyUtils;
import burlap.behavior.policy.SolverDerivedPolicy;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.singleagent.MDPSolverInterface;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.datastructures.BoltzmannDistribution;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

import javax.management.RuntimeErrorException;
import java.util.ArrayList;
import java.util.List;


/**
 * This class implements a Boltzmann policy where the the Q-values represent
 * the components of the Boltzmann distribution. This policy requires a QComputable
 * valueFunction to be passed to it.
 * @author James MacGlashan
 *
 */
public class BoltzmannQPolicyWithCoolingSchedule implements SolverDerivedPolicy, EnumerablePolicy {

    protected QProvider qplanner;
    double								temperature;
    double                              coolingRate = 1.;



    // weather the cooling rate decreases by addition of multiplication
    boolean multiplicativeCoolingRate = true;
    double minValue = Double.MIN_VALUE + 0.0000000001;


    boolean noCoolingNextAction = false;


    /**
     * Initializes with a temperature value. The temperature value controls how greedy the Boltzmann distribution is.
     * The temperature should be positive with values near zero causing the distribution to be more greedy. A high temperature
     * causes the distribution to be more uniform.
     * @param temperature the positive temperature value to use
     */
    public BoltzmannQPolicyWithCoolingSchedule(double temperature){
        this.qplanner = null;
        this.temperature = temperature;
    }


    /**
     * Initializes with a temperature value. The temperature value controls how greedy the Boltzmann distribution is.
     * The temperature should be positive with values near zero causing the distribution to be more greedy. A high temperature
     * causes the distribution to be more uniform.
     * @param temperature the positive temperature value to use
     */
    public BoltzmannQPolicyWithCoolingSchedule(double temperature, double coolingRate){
        this.qplanner = null;
        this.temperature = temperature;
        this.coolingRate = coolingRate;
    }



    /**
     * Initializes with a temperature value and the QComputable valueFunction to use. The temperature value controls how greedy the Boltzmann distribution is.
     * The temperature should be positive with values near zero causing the distribution to be more greedy. A high temperature
     * causes the distribution to be more uniform.
     * @param planner the q-computable valueFunction to use.
     * @param temperature the positive temperature value to use
     */
    public BoltzmannQPolicyWithCoolingSchedule(QProvider planner, double temperature){
        this.qplanner = planner;
        this.temperature = temperature;
    }

    /**
     * Initializes with a temperature value and the QComputable valueFunction to use. The temperature value controls how greedy the Boltzmann distribution is.
     * The temperature should be positive with values near zero causing the distribution to be more greedy. A high temperature
     * causes the distribution to be more uniform.
     * @param planner the q-computable valueFunction to use.
     * @param temperature the positive temperature value to use
     */
    public BoltzmannQPolicyWithCoolingSchedule(QProvider planner, double temperature, double coolingRate){
        this.qplanner = planner;
        this.temperature = temperature;
        this.coolingRate = coolingRate;
    }


    public void setNoCoolingNextAction(boolean noCoolingNextAction) {
        this.noCoolingNextAction = noCoolingNextAction;
    }


    @Override
    public Action action(State s) {
        Action a = PolicyUtils.sampleFromActionDistribution(this, s);
//        if(multiplicativeCoolingRate){
//            this.temperature=this.temperature*this.coolingRate;
//        }
//        else if(!multiplicativeCoolingRate){
//            this.temperature=this.temperature - this.coolingRate;
//        }
        if(multiplicativeCoolingRate && this.temperature >minValue && !noCoolingNextAction){
            this.temperature=this.temperature*this.coolingRate;
        }
        else if(!multiplicativeCoolingRate && this.temperature > minValue&& !noCoolingNextAction){
            this.temperature=this.temperature - this.coolingRate;
        }
        if(this.temperature< minValue && !noCoolingNextAction){
            this.temperature=minValue;
        }
        noCoolingNextAction = false;
        return a;
    }

    @Override
    public double actionProb(State s, Action a) {
        return PolicyUtils.actionProbFromEnum(this, s, a);
    }

    @Override
    public List<ActionProb> policyDistribution(State s) {
        List<QValue> qValues = this.qplanner.qValues(s);
        return this.getActionDistributionForQValues(s, qValues);
    }



    private List<ActionProb> getActionDistributionForQValues(State queryState, List <QValue> qValues){

        List <ActionProb> res = new ArrayList<ActionProb>();

        double [] rawQs = new double[qValues.size()];
        for(int i = 0; i < qValues.size(); i++){
            rawQs[i] = qValues.get(i).q;
        }

        BoltzmannDistribution bd = new BoltzmannDistribution(rawQs, this.temperature);
        double [] probs = bd.getProbabilities();
        for(int i = 0; i < qValues.size(); i++){
            QValue q = qValues.get(i);
            ActionProb ap = new ActionProb(q.a, probs[i]);
            res.add(ap);
        }

        return res;
    }


    @Override
    public void setSolver(MDPSolverInterface solver) {
        if(!(solver instanceof QProvider)){
            throw new RuntimeErrorException(new Error("Planner is not a QComputablePlanner"));
        }
        this.qplanner = (QProvider) solver;
    }


    @Override
    public boolean definedFor(State s) {
        return true; //can always find q-values with default value
    }


}
