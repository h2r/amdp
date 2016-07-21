package amdp.maxq.framework;

import burlap.behavior.singleagent.MDPSolverInterface;
import burlap.behavior.valuefunction.QProvider;
import burlap.mdp.core.Domain;
import burlap.mdp.core.action.ActionType;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.model.SampleModel;
import burlap.statehashing.HashableStateFactory;

import java.util.List;


/**
 * This is an interface that allows connecting a TaskNode to a QProvider. This QProvider and
 * Solver interface exists so we can use BURLAP's policy methods from MAXQ. Most of the methods
 * apart from the QProvider ones return 0 in this interface.
 */
public abstract class QProviderForMAXQ implements QProvider, MDPSolverInterface {

    GroundedTask parentGroundedTask;

    public void setGt(GroundedTask gt) {
        this.parentGroundedTask = gt;
    }

    @Override
    public void solverInit(SADomain domain, double gamma, HashableStateFactory hashingFactory) {

    }

    @Override
    public void resetSolver() {

    }

    @Override
    public void setDomain(SADomain domain) {

    }

    @Override
    public void setModel(SampleModel model) {

    }

    @Override
    public SampleModel getModel() {
        return null;
    }

    @Override
    public Domain getDomain() {
        return null;
    }

    @Override
    public void addActionType(ActionType a) {

    }

    @Override
    public void setActionTypes(List<ActionType> actionTypes) {

    }

    @Override
    public List<ActionType> getActionTypes() {
        return null;
    }

    @Override
    public void setHashingFactory(HashableStateFactory hashingFactory) {

    }

    @Override
    public HashableStateFactory getHashingFactory() {
        return null;
    }

    @Override
    public double getGamma() {
        return 0.;
    }

    @Override
    public void setGamma(double gamma) {

    }

    @Override
    public void setDebugCode(int code) {

    }

    @Override
    public int getDebugCode() {
        return 0;
    }

    @Override
    public void toggleDebugPrinting(boolean toggle) {

    }
}