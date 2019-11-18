package petrinet;

import java.util.*;
import java.util.concurrent.Semaphore;

public class PetriNet<T> {
	
	/// Current state of the net
	private Map<T, Integer> state;

	/// If is fair then chosen to awake (from the threads stopped by fire method) is the one, that waits the longest.
	private boolean fair;
	/// Mutex of access to fire function
	private Semaphore mutex;

	/// Queue of threads to awake (used if fair)
	private List<Semaphore> waiting = new LinkedList<>();


	public PetriNet(Map<T, Integer> initial, boolean fair) {
		this.state = initial;
		this.fair = fair;
		
		mutex = new Semaphore(1, true);
		waiting.add(new Semaphore(1));
		
		state.values().removeIf(value -> value == 0);
	}
	
	/// Returns set of all possible net's states, that can be reached from current state using transitions.
	/// It assumes that the calculated set is finite.
	public Set<Map<T, Integer>> reachable(Collection<Transition<T>> transitions) {
		Set<Map<T, Integer>> res = new HashSet<>();
		Stack<Map<T, Integer>> toCheck = new Stack<>();
		
		try {
			mutex.acquire();
		} catch (InterruptedException e) {
			System.err.println("reachable interrupted");
			e.printStackTrace();
			return null;
		}
		
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
		mutex.release();
		
		return res;
	}
	
	/// If any of transitions is possible, fires and returns it. Else returns null.
	private Transition<T> tryFire(Collection<Transition<T>> transitions) {
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
	public Transition<T> fire(Collection<Transition<T>> transitions) throws InterruptedException {
		if (fair) {
			mutex.acquire();
			Semaphore myTurn = waiting.get(waiting.size() - 1); // There is always one more semaphore to simplify
			waiting.add(new Semaphore(0));
			mutex.release();

			while (true) {
				boolean acquired = false;
				try {
					myTurn.acquire();
					acquired = true;
					mutex.acquire();
				} catch (InterruptedException e) {  // If interrupted, i have to remove myself from queue
					mutex.acquireUninterruptibly();

					waiting.remove(myTurn);
					if(acquired || myTurn.availablePermits() > 0)
						waiting.get(1 + waiting.indexOf(myTurn)).release();
					mutex.release();
					throw e;
				}

				Transition<T> res = tryFire(transitions);
				if (res != null) {
					waiting.remove(myTurn);
					waiting.get(0).release();
					mutex.release();
					return res;
				}
				waiting.get(1 + waiting.indexOf(myTurn)).release();

				mutex.release();
			}
		} else {
			while (true) {
				mutex.acquire();
				Transition<T> res = tryFire(transitions);
				mutex.release();
				if (res != null) {
					return res;
				}
			}

		}


	}
	
	public String toString() {
		return state.toString();
	}
	
	public Integer getArcs(T key) {
		return state.getOrDefault(key, 0);
	}
}