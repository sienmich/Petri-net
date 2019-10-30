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
    
    public boolean canTransit(final Map<T, Integer> state) {
    	for(Entry<T, Integer> x : input.entrySet())
    		if(state.get(x.getKey()) < x.getValue())
    			return false;
    	for(T x : inhibitor)
    		if(state.get(x) != 0)
    			return false;
    	return true;
    }
    
    public void transit(Map<T, Integer> state) {
    	for(Entry<T, Integer> x : input.entrySet())
    		state.put(x.getKey(), state.get(x.getKey()) - x.getValue());
    	for(T x : reset)
    		state.put(x, 0);
    	for(Entry<T, Integer> x : output.entrySet())
    		state.put(x.getKey(), state.get(x.getKey()) + x.getValue());
    }    
    
}