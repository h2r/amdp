package amdp.maxq.taximaxq;

import amdp.taxi.TaxiDomain;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngopalan on 7/21/16.
 */
public class RewardDecompositionForTaxi {

    public RewardDecompositionForTaxi(){};

    public List<Pair<String, Double>> getRewardDecomposition(EnvironmentOutcome eo){
        if(eo.r == -1.0 || eo.r == +20.0){
            return new ArrayList<Pair<String, Double>>();
        }

        // wrong pickup?

        if(eo.a.actionName().equals(TaxiDomain.ACTION_PICKUP)){
//            Pair<String, Double>
        }

        // wrong place?

        //correct


        return new ArrayList<Pair<String, Double>>();

    }


}
