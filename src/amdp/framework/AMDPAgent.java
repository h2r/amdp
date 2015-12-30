package amdp.framework;

import java.util.List;

import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;

public class AMDPAgent{
	
	List<AMDPDomain> DomainList;
	List<AMDPPolicyGenerator> PolicyGenerators;
	
	RewardFunction rf;
	TerminalFunction tf;
	
	public AMDPAgent(List<AMDPDomain> inputDomainList, List<AMDPPolicyGenerator> inputPolicyGenerators, RewardFunction inputRF, TerminalFunction inputTF){
		this.DomainList = inputDomainList;
		this.PolicyGenerators = inputPolicyGenerators;
		this.rf = inputRF;
		this.tf = inputTF;
	}

}
