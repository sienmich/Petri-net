package petrinet;

import java.util.*;

public class PetriNet<T> {
	
	/// Current state of the net
	private Map<T, Integer> state;
	/// If is fair then chosen to awake (from the threads stopped by fire method) is the one, that waits the longest.
	private boolean fair;
	/// Queue of threads to awake
	private Queue<Thread> waiting = new LinkedList<>();
	
	public PetriNet(Map<T, Integer> initial, boolean fair) {
		this.state = initial;
		this.fair = fair;
		
		state.values().removeIf(value -> value == 0);
	}
	
	/// Returns set of all possible net's states, that can be reached from current state using transitions.
	/// It assumes that the calculated set is finite.
	public synchronized Set<Map<T, Integer>> reachable(Collection<Transition<T>> transitions) {
		Set<Map<T, Integer>> res = new HashSet<>();
		Stack<Map<T, Integer>> toCheck = new Stack<>();
		
		toCheck.add(state);
		
		while (!toCheck.empty()) {
			Map<T, Integer> newState = toCheck.pop();
			if (res.add(newState)) {
				for (Transition<T> t : transitions)
					if (t.canTransit(newState)) {
						Map<T, Integer> copy = new HashMap<>(newState);
						t.transit(copy);
						toCheck.add(copy);
					}
			}
		}
		
		return res;
	}
	
	/// If any of transitions is possible, fires and returns it. Else returns null.
	private synchronized Transition<T> tryFire(Collection<Transition<T>> transitions) {
		for (Transition<T> t : transitions) {
			if (t.canTransit(state)) {
				t.transit(state);
				return t;
			}
		}
		return null;
	}
	
	/// Tries to fire any of the transitions.
	/// If none are possible goes to sleep.
	/// If there is one possible, fires it and awakes other threads.
	/// If the net is fair then chosen to awake (from the threads stopped) is the one, that waits the longest.
	public synchronized Transition<T> fire(Collection<Transition<T>> transitions) throws InterruptedException {
		Thread thread = Thread.currentThread();
		while (true) {
			Transition<T> res = tryFire(transitions);
			if (res != null) {
				notifyAll();
				return res;
			}
			
			if (fair) {
				waiting.add(thread);
			}
			
			wait();
			
			if (fair) {
				while (waiting.peek() != thread) {
					wait();
				}
				waiting.poll();
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