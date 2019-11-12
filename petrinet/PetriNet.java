package petrinet;

import java.util.*;

public class PetriNet<T> {

	private Map<T, Integer> state;
	private boolean fair;
	private Queue<Thread> queue = new LinkedList<>();

	

    public PetriNet(Map<T, Integer> initial, boolean fair) {
		this.state = initial;
		this.fair = fair;
		
		state.values().removeIf(value -> value == 0);
	}

    public Set<Map<T, Integer>> reachable(Collection<Transition<T>> transitions) {
    	Set<Map<T, Integer>> res = new HashSet<>();
    	Stack<Map<T, Integer>> toCheck = new Stack<>();
    	
    	toCheck.add(state);
    	
    	while(!toCheck.empty()) {
    		Map<T, Integer> newState = toCheck.pop();
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

    private synchronized Transition<T> tryFire(Collection<Transition<T>> transitions) {
		for(Transition<T> t : transitions) {
			if(t.canTransit(state)) {
				t.transit(state);
				return t;
			}
		}
		return null;
	}

    public synchronized Transition<T> fire(Collection<Transition<T>> transitions) throws InterruptedException {
    	while(true) {
			Transition<T> res = tryFire(transitions);
			if(res != null) {
				notifyAll();
				return res;
			}
			if(fair) {
				Thread thread = Thread.currentThread();
				queue.add(thread);
				wait();
				while(queue.peek() != thread) {
					wait();
				}
				queue.poll();
			}
			else {
				wait();
			}
    	}
    }
    
    public String toString() {
    	return state.toString();
    }
    
    public Integer getArcs(T key) {
    	return state.get(key);
    }


}