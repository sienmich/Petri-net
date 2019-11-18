package multiplicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import petrinet.*;

public class Main {
	/// Number of threads running
	private static final int THREADS = 4;
	
	/// Enum of possible places in PetriNet
	private enum Places {
		A, A2, B, RES, FIRST_PHASE, SECOND_PHASE
	}
	
	private static PetriNet<Places> net;
	/// List of possible transitions
	private static List<Transition<Places>> transitions;
	/// The last transitions
	private static Transition<Places> lastTransition;
	
	/// Thread that fires all possible transitions and counts them
	private static class NetThread implements Runnable {
		private int counter = 0;
		
		@Override
		public void run() {
			Thread t = Thread.currentThread();
			try {
				while (!t.isInterrupted()) {
					net.fire(transitions);
					counter++;
				}
				throw new InterruptedException();
			} catch (InterruptedException e) {
				t.interrupt();
				System.out.println("Thread " + t.getName() + " fired " + counter + " times");
			}
		}
	}
	
	/// Initializing function, that creates the net, transitions and lastTransition
	private static void init(int a, int b) {
		Map<Places, Integer> initial = new HashMap<Places, Integer>();
		initial.put(Places.A, a);
		initial.put(Places.B, b);
		initial.put(Places.FIRST_PHASE, 1);
		net = new PetriNet<Places>(initial, true);
		
		transitions = new ArrayList<Transition<Places>>();
		
		transitions.add(new Transition<>(
                Map.of(Places.A, 1),
                Arrays.asList(),
                Arrays.asList(Places.SECOND_PHASE),
                Map.of(Places.A2, 1)));
		
		transitions.add(new Transition<>(
                Map.of(Places.A2, 1),
                Arrays.asList(),
                Arrays.asList(Places.FIRST_PHASE),
                Map.of(Places.A, 1, Places.RES, 1)));
		
		transitions.add(new Transition<>(
                Map.of(Places.B, 1),
                Arrays.asList(Places.FIRST_PHASE),
                Arrays.asList(Places.SECOND_PHASE, Places.A),
                Map.of(Places.SECOND_PHASE, 1)));
		
		transitions.add(new Transition<>(
                Map.of(),
                Arrays.asList(Places.SECOND_PHASE),
                Arrays.asList(Places.FIRST_PHASE, Places.A2),
                Map.of(Places.FIRST_PHASE, 1)));
		
		lastTransition = new Transition<>(
                Map.of(),
                Arrays.asList(),
                Arrays.asList(Places.SECOND_PHASE, Places.A, Places.B),
                Map.of());
		
	}
	
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		init(in.nextInt(), in.nextInt());
		in.close();
		
		List<Thread> threads = new ArrayList<Thread>();
		
		for (int i = 0; i < THREADS; i++)
			threads.add(new Thread(new NetThread(), String.valueOf((char) ('A' + i))));
		for (int i = 0; i < THREADS; i++)
			threads.get(i).start();
		
		try {
			net.fire(Arrays.asList(lastTransition));
		} catch (InterruptedException e) {
			Thread t = Thread.currentThread();
			t.interrupt();
			System.err.println(t.getName() + " interrupted");
		}
		
		System.out.println("Result: " + net.getArcs(Places.RES));
		
		for (int i = 0; i < THREADS; i++)
			threads.get(i).interrupt();
	}
	
}
