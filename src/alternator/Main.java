package alternator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import petrinet.*;

public class Main {
		
	private static PetriNet<Integer> net;
	private static List<Transition<Integer>> enters, exits;

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
					Transition<Integer> res = net.fire(transitions);
					if(enters.contains(res)) {
						
						System.out.print(Thread.currentThread().getName() + ".");
						System.out.print(Thread.currentThread().getName() + ".");
//						System.out.println();
//						System.out.println(net);
//						System.out.println();
						
					}
				}
				throw new InterruptedException();
			} catch (InterruptedException e) {
				t.interrupt();
				System.err.println(t.getName() + " interupted");
			}
		}

	}

	private static void init() {
		/* Id
		 * 	Current State : READY, CRITICAL_SECTION, WAS_LAST
		 * 
		 * Process name:
		 * A				0			1				2
		 * B				3			4				5
		 * C				6			7				8
		 * 
		 */
		Map<Integer, Integer> initial = new HashMap<Integer, Integer>();
		for(int i = 0; i < 9; i++)
			initial.put(i, i % 3 == 0 ? 1 : 0);
		net = new PetriNet<Integer>(initial, false);
		enters = new ArrayList<Transition<Integer>>();
		exits = new ArrayList<Transition<Integer>>();
		
		for(int j = 0; j < 9; j+=3) {
		    enters.add(new Transition<Integer>(
					Map.of(j, 1),
					Arrays.asList(),
					Arrays.asList((j + 4) % 9, (j + 7) % 9, j + 2),
					Map.of(j + 1, 1)
					));
		}
		
		for(int j = 0; j < 9; j+=3) {
			exits.add(new Transition<Integer>(
					Map.of(j + 1, 1),
					Arrays.asList((j + 5) % 9, (j + 8) % 9),
					Arrays.asList(),
					Map.of(j, 1, j + 2, 1)
					));
		}

	}

	public static void main(String[] args) {

		init();
		List<Thread> threads = new ArrayList<Thread>();
		
		for(int i=0; i<3; i++)
			threads.add(new Thread(new NetThread(Arrays.asList(enters.get(i), exits.get(i))), String.valueOf((char)('A' + i))));
		for(int i=0; i<3; i++)
			threads.get(i).start();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Thread t = Thread.currentThread();
			t.interrupt();
			System.err.println(t.getName() + " interupted");
		}

		for(int i=0; i<3; i++)
			threads.get(i).interrupt();
	}

}
