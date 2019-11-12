package alternator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import petrinet.*;

public class Main {
	/// Number of threads running
	private static final int THREADS = 3;
	/// Number of places in PetriNet
	private static final int PLACES = 3 * THREADS;
	
	private static PetriNet<Integer> net;
	/// Transitions which are entering/exiting critical section
	private static List<Transition<Integer>> enters, exits;

	/// Thread that enters and exits critical section with net.fire(), in witch it prints "A.A."
	private static class NetThread implements Runnable {
		Collection<Transition<Integer>> transitions;

		public NetThread(Collection<Transition<Integer>> transitions) {
			this.transitions = transitions;
		}

		@Override
		public void run() {
			Thread t = Thread.currentThread();
			
			try {
				while(!t.isInterrupted()) {
					if(enters.contains(net.fire(transitions))) { // Entered critical section
						System.out.print(t.getName() + ".");
						System.out.print(t.getName() + ".");
					}
				}
				throw new InterruptedException();
			} catch (InterruptedException e) {
				t.interrupt();
			}
		}

	}

	/// Initializing function, that creates the net and possible transitions
	private static void init() {
		/* Table of net's place id number and corresponding meaning
		 * Example: 1 - process A is in critical section
		 * 
		 * Current State : READY, CRITICAL_SECTION, WAS_LAST
		 * 
		 * Process name:
		 * A				0			1				2
		 * B				3			4				5
		 * C				6			7				8
		 * 
		 */
		Map<Integer, Integer> initial = new HashMap<Integer, Integer>();
		for(int i = 0; i < PLACES; i++)
			initial.put(i, (i % 3 == 0 || i == 2) ? 1 : 0);	// i == 2 <-> at the beginning lets assume that A was last 
		
		net = new PetriNet<Integer>(initial, false);
		enters = new ArrayList<Transition<Integer>>();
		exits = new ArrayList<Transition<Integer>>();
		
		for(int j = 0; j < PLACES; j+=3) {
			enters.add(new Transition<Integer>(
					Map.of(j, 1),
					Arrays.asList(),
					IntStream.concat(
							IntStream.iterate((j+4) % PLACES, i -> (i + 3) % PLACES).limit(THREADS),
							IntStream.of(j + 2)
							).boxed().collect(Collectors.toList()),
					Map.of(j + 1, 1)
					));
			exits.add(new Transition<Integer>(
					Map.of(j + 1, 1),
					IntStream.iterate((j+5) % PLACES, i -> (i + 3) % PLACES).limit(THREADS - 1)
							.boxed().collect(Collectors.toList()),
					Arrays.asList(),
					Map.of(j, 1, j + 2, 1)
					));
		}

	}

	/// Calculates all reachable net's states and checks if they are safe (in terms of critical section)
	private static void checkReachable() {
		Set<Map<Integer, Integer>> reachable = net.reachable(Stream.concat(enters.stream(), exits.stream())
                .collect(Collectors.toList()));
		
		System.out.println("There are " + reachable.size() + " reachable states.");
		for (Map<Integer, Integer> state : reachable) {
			int inCriticalSection = 0;
			
			for(int i = 0; i < THREADS; i++)
				inCriticalSection += state.getOrDefault(3 * i + 1, 0);
			
			if(inCriticalSection > 1) {
				System.out.println("ERROR: Two threads could end up in critical section.");
		        System.exit(2);
			}
		}
		System.out.println("All states are safe (in terms of critical section).");
	}
	
	/// Runs NetThreads for 30s 
	private static void runThreads() {
		System.out.println("Running:");
		
		List<Thread> threads = new ArrayList<Thread>();
		for(int i=0; i < THREADS; i++)
			threads.add(new Thread(new NetThread(Arrays.asList(enters.get(i), exits.get(i))),
									String.valueOf((char)('A' + i))));
		for(int i=0; i < THREADS; i++)
			threads.get(i).start();

		try {
			Thread.sleep(30 * 1000);
		} catch (InterruptedException e) {
			Thread t = Thread.currentThread();
			t.interrupt();
			System.err.println(t.getName() + " interupted");
		}

		for(int i=0; i < THREADS; i++)
			threads.get(i).interrupt();
		
	}
	
	public static void main(String[] args) {
		init();
		
		checkReachable();
		
		runThreads();
	}
}
