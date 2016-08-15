package amdp.tools;

import amdp.amdpframework.AMDPAgent;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.environment.extensions.EnvironmentObserver;
import burlap.visualizer.Visualizer;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class VisualEnvStackObserver implements EnvironmentObserver, StackObserver {

	protected Visualizer envView;
	protected AMDPAgent agent;

	protected JFrame frame;
	protected TextArea area;

	protected int delay;


	public VisualEnvStackObserver(Visualizer envView, AMDPAgent agent, int delay) {
		this.envView = envView;
		this.agent = agent;
		this.delay = delay;
		this.initGUI();
	}

	protected void initGUI(){
		frame = new JFrame();
		frame.setLayout(new BorderLayout());
		this.envView.setPreferredSize(new Dimension(800, 800));
		frame.add(this.envView, BorderLayout.CENTER);

		area = new TextArea();
		area.setPreferredSize(new Dimension(400, 800));
		frame.add(area, BorderLayout.EAST);

		frame.pack();
		frame.setVisible(true);


	}

	@Override
	public void observeEnvironmentActionInitiation(State o, Action action) {
		this.envView.updateState(o);
		this.area.setText(this.getPolicyStackString(this.agent.getPolicyStack()));

		try {
			Thread.sleep(delay);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void observeEnvironmentInteraction(EnvironmentOutcome eo) {
		this.envView.updateState(eo.op);
	}

	@Override
	public void observeEnvironmentReset(Environment resetEnvironment) {

	}

	public void updateState(State s){
		this.envView.updateState(s);
	}


	@Override
	public void updatePolicyStack(List<List<Action>> policyStack) {
		this.area.setText(this.getPolicyStackString(policyStack));
	}



	protected String getPolicyStackString(List<List<Action>> stack){
		StringBuilder buf = new StringBuilder();

		int depth = 0;
		for(int l = stack.size()-1; l >= 0; l--){

			for(Action g : stack.get(l)){
				if(depth > 0){
					buf.append(StringUtils.repeat(" ", 4*depth));
				}
				buf.append("Level ").append(l).append(": ").append(g.toString()).append("\n");
			}

			depth++;
		}

		return buf.toString();
	}

}
