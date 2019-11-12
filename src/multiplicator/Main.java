package multiplicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import petrinet.*;

public class Main {
	private enum States {
		A, A2, B, RES, FIRST_PHASE, SECOND_PHASE 
	}		
	private static PetriNet<States> net;
	private static List<Transition<States>> transitions;
	private static Transition<States> lastTransition;
	


	private static class NetThread implements Runnable {
		private int counter = 0;
		
		@Override
		public void run() {
			Thread t = Thread.currentThread();
			try {
				while(!t.isInterrupted()) {
					Transition<States> res = net.fire(transitions);
					counter++;
//					System.out.println(net);
				}
				throw new InterruptedException();
			} catch (InterruptedException e) {
				t.interrupt();
				System.err.println("Thread " + t.getName() + " fired " + counter + " times.");
//				System.err.println(t.getName() + " interupted");
			}
		}

	}

	private static void init(int a, int b) {
		Map<States, Integer> initial = new HashMap<States, Integer>();
		initial.put(States.A, a);
		initial.put(States.A2, 0);
		initial.put(States.B, b);
		initial.put(States.RES, 0);
		initial.put(States.FIRST_PHASE, 1);
		initial.put(States.SECOND_PHASE, 0);
		net = new PetriNet<States>(initial, false);

		transitions = new ArrayList<Transition<States>>();

	    transitions.add(new Transition<States>(
					Map.of(States.A, 1),
					Arrays.asList(),
					Arrays.asList(States.SECOND_PHASE),
					Map.of(States.A2, 1)));
	    
	    transitions.add(new Transition<States>(
					Map.of(States.A2, 1),
					Arrays.asList(),
					Arrays.asList(States.FIRST_PHASE),
					Map.of(States.A, 1, States.RES, 1)));

	    transitions.add(new Transition<States>(
					Map.of(States.B, 1),
					Arrays.asList(States.FIRST_PHASE),
					Arrays.asList(States.SECOND_PHASE, States.A),
					Map.of(States.SECOND_PHASE, 1)));

	    transitions.add(new Transition<States>(
					Map.of(),
					Arrays.asList(States.SECOND_PHASE),
					Arrays.asList(States.FIRST_PHASE, States.A2),
					Map.of(States.FIRST_PHASE, 1)));
	    
	    lastTransition = new Transition<States>(
					Map.of(),
					Arrays.asList(),
					Arrays.asList(States.SECOND_PHASE, States.A, States.B),
					Map.of());
	    
	}

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		init(in.nextInt(), in.nextInt());
		in.close();
			
		List<Thread> threads = new ArrayList<Thread>();
		final int numberOfThreads = 10;
		
		for(int i = 0; i < numberOfThreads; i++)
			threads.add(new Thread(new NetThread(), String.valueOf((char)('A' + i))));
		for(int i = 0; i < numberOfThreads; i++)
			threads.get(i).start();
		
		try {
			net.fire(Arrays.asList(lastTransition));
		} catch (InterruptedException e) {
			Thread t = Thread.currentThread();
			t.interrupt();
			System.err.println(t.getName() + " interupted");
		}

		System.out.println("Result: ");
		System.out.println(net.getArcs(States.RES));

		for(int i = 0; i < numberOfThreads; i++)
			threads.get(i).interrupt();
	}

}
