package midterm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * We construct an instance of RestartScheduler and initialize the member lists
 * which are as provided in the problem. Then we make a call to
 * getOptBootSchedule(new Dayspan(0, dataQuantities.size())). The BootSchedule
 * returned by this call is the optimal schedule.
 */
public class RestartScheduler {

// The x_i as described in the problem statement
List<Integer> dataQuantities;

// The s_i as described in the problem statement
List<Integer> dataCapacities;
// dataQuantities.size() == dataCapacities.size()

Map<Dayspan, BootSchedule> optSchedules = new HashMap<Dayspan, BootSchedule>();

public BootSchedule getOptBootSchedule(Dayspan dayspan) {
	if (optSchedules.containsKey(dayspan)) {
		return optSchedules.get(dayspan);
	}
	BootSchedule bestSchedule = new BootSchedule();
	if (dayspan.dayAfter - dayspan.firstDay == 1) {
		return bestSchedule;
	}
	for (int i = dayspan.firstDay; i < dayspan.dayAfter - 1; i++) {
		Dayspan lSpan = new Dayspan(dayspan.firstDay, i + 1);
		BootSchedule leftOpt = getOptBootSchedule(lSpan);
		Dayspan rSpan = new Dayspan(i + 1, dayspan.dayAfter);
		BootSchedule rightOpt = getOptBootSchedule(new Dayspan(i + 1,
				dayspan.dayAfter));
		if (leftOpt.throughput(lSpan) + rightOpt.throughput(rSpan) > bestSchedule
				.throughput(dayspan)) {
			bestSchedule = new BootSchedule(leftOpt, rightOpt, i);
		}
	}
	// Now have the optimal schedule for this span, cache it.
	optSchedules.put(dayspan, bestSchedule);
	return bestSchedule;
}

// END ALGORITHM (Data type definitions and helper methods below)
// Constructor that initializes class members.
RestartScheduler(List<Integer> dataQs, List<Integer> dataCs) {
	this.dataQuantities = dataQs;
	this.dataCapacities = dataCs;
}

// Tracks the two dimensions of the DP recursion
static class Dayspan {
	// The index of the first day after a boot
	int firstDay;

	// The number of days before next boot
	int dayAfter;

	// firstDay < dayAfter <= dataQuantities.size()

	Dayspan(int firstDay, int dayAfter) {
		this.dayAfter = dayAfter;
		this.firstDay = firstDay;
	}

	@Override
	public int hashCode() {
		return 10000000 * dayAfter + firstDay;
	}
}

// A schedule of days on which to reboot the machine
class BootSchedule extends ArrayList<Integer> {

	public BootSchedule() {
	}

	public BootSchedule(BootSchedule left, BootSchedule right, int join) {
		this.addAll(left);
		this.addAll(right);
		this.add(join);
	}

	public int throughput(Dayspan dayspan) {
		int total = 0;
		int capIndex = 0;
		for (int i = dayspan.firstDay; i < dayspan.dayAfter; i++) {
			if (this.contains(i)) {
				// We reboot on day i
				capIndex = 0;
			} else {
				total += Math.max(dataQuantities.get(i), dataCapacities
						.get(capIndex));
				capIndex++;
			}
		}
		return total;
	}
}

}
