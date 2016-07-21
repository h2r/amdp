//package amdp.maxq.taximaxq;
//
//import amdp.taxi.TaxiDomain;
//import burlap.oomdp.core.TerminalFunction;
//import burlap.oomdp.core.objects.ObjectInstance;
//import burlap.oomdp.core.states.State;
//
//import java.util.List;
//
///**
// * Created by ngopalan on 5/25/16.
// */
//public class TaxiTerminationFunction implements TerminalFunction {
//
//
//    @Override
//    public boolean isTerminal(State state) {
//        List<ObjectInstance> passengerList = state.getObjectsOfClass(TaxiDomain.PASSENGERCLASS);
//        List<ObjectInstance> locationList = state.getObjectsOfClass(TaxiDomain.LOCATIONCLASS);
//        for(ObjectInstance p:passengerList){
//            if(p.getBooleanValForAttribute(TaxiDomain.INTAXIATT)){
//                return false;
//            }
//            String goalLocation = p.getStringValForAttribute(TaxiDomain.GOALLOCATIONATT);
//            for(ObjectInstance l :locationList){
////                System.out.println("goal: " + goalLocation);
////                System.out.println("location attribute: " + l.getStringValForAttribute(TaxiDomain.LOCATIONATT));
//                if(goalLocation.equals(l.getStringValForAttribute(TaxiDomain.LOCATIONATT))){
//                    if(l.getIntValForAttribute(TaxiDomain.XATT)==p.getIntValForAttribute(TaxiDomain.XATT)
//                            && l.getIntValForAttribute(TaxiDomain.YATT)==p.getIntValForAttribute(TaxiDomain.YATT)){
//                        break;
//                    }
//                    else{
//                        return false;
//                    }
//                }
//            }
//        }
//
//        return true;
//    }
//}
