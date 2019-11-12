package petrinet;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

public class Transition<T> {
	private final Map<T, Integer> input;
	private final Collection<T> reset;
	private final Collection<T> inhibitor;
	private final Map<T, Integer> output;

    public Transition(Map<T, Integer> input, Collection<T> reset, Collection<T> inhibitor, Map<T, Integer> output) {
		this.input = input;
		this.reset = reset;
		this.inhibitor = inhibitor;
		this.output = output;
	}

    private int get(Map<T, Integer> state, T key) {
    	return state.getOrDefault(key, 0);
	}
    
	private void put(Map<T, Integer> state, T key, int val) {
		if(val==0)
			state.remove(key);
		else
			state.put(key, val);
	}  
    
    boolean canTransit(final Map<T, Integer> state) {
    	for(Entry<T, Integer> x : input.entrySet())
    		if(get(state, x.getKey()) < x.getValue())
    			return false;
    	for(T x : inhibitor)
    		if(get(state, x) != 0)
    			return false;
    	return true;
    }

	void transit(Map<T, Integer> state) {
    	for(Entry<T, Integer> x : input.entrySet())
    		put(state, x.getKey(), get(state, x.getKey()) - x.getValue());
    	for(T x : reset)
    		put(state, x, 0);
    	for(Entry<T, Integer> x : output.entrySet())
    		put(state, x.getKey(), get(state, x.getKey()) + x.getValue());
    }
    
}