package petrinet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class PetriNet<T> {

	private Map<T, Integer> state;
	boolean fair;
	

    public PetriNet(Map<T, Integer> initial, boolean fair) {
		this.state = initial;
		this.fair = fair;
		
		state.values().removeIf(value -> value == 0);
	}

    public Set<Map<T, Integer>> reachable(Collection<Transition<T>> transitions) {
    	Set<Map<T, Integer>> res = new HashSet<Map<T, Integer>>();
    	Stack<Map<T, Integer>> toCheck = new Stack<Map<T, Integer>>();
    	
    	toCheck.add(state);
    	
    	while(!toCheck.empty()) {
    		Map<T, Integer> newState = toCheck.pop();
//    		System.out.println(newState);
    		if(res.add(newState)) {
	    		for(Transition<T> t : transitions)
	    			if(t.canTransit(newState)) {
	    				Map<T, Integer> copy = new HashMap<>(newState);
	    				t.transit(copy);
	    				toCheck.add(copy);
	    			}
    		}
    	}
    	
    	return res;
    }

    public synchronized Transition<T> fire(Collection<Transition<T>> transitions) throws InterruptedException {
    	while(true) {
        	for(Transition<T> t : transitions) {
        		if(t.canTransit(state)) {
        			t.transit(state);
        			notifyAll();
        			return t;
        		}
        	}
        	wait();
    	}
    }
    
    public String toString() {
    	return state.toString();
    }
    
    public Integer getArcs(T key) {
    	return state.get(key);
    }


}