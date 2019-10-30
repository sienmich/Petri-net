package alternator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import petrinet.*;

public class Main {
	private enum States {
		AReady, AInCritical,
		BReady, BInCritical,
		CriticalEmpty
	};

	public static void main(String[] args) {

		Map<States, Integer> initial = new HashMap<States, Integer>();
		
		initial.put(States.AReady, 1);
		initial.put(States.AInCritical, 0);
		initial.put(States.BReady, 1);
		initial.put(States.BInCritical, 0);
		initial.put(States.CriticalEmpty, 1);
		
		PetriNet<States> net = new PetriNet<States>(initial, false);
		
		Transition<States> A1 = new Transition<States>(
				Map.of(States.AReady, 1, States.CriticalEmpty, 1),
				Arrays.asList(),
				Arrays.asList(),
				Map.of(States.AInCritical, 1)
				);
		Transition<States> A2 = new Transition<States>(
				Map.of(States.AInCritical, 1),
				Arrays.asList(),
				Arrays.asList(),
				Map.of(States.AReady, 1, States.CriticalEmpty, 1)
				);
		
		Transition<States> B1 = new Transition<States>(
				Map.of(States.BReady, 1, States.CriticalEmpty, 1),
				Arrays.asList(),
				Arrays.asList(),
				Map.of(States.BInCritical, 1)
				);
		Transition<States> B2 = new Transition<States>(
				Map.of(States.BInCritical, 1),
				Arrays.asList(),
				Arrays.asList(),
				Map.of(States.BReady, 1, States.CriticalEmpty, 1)
				);
		

		System.out.println(net.reachable(Arrays.asList(A1, A2, B1, B2)));

	}

}
