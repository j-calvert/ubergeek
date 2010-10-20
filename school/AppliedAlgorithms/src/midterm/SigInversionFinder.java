package midterm;

import java.util.ArrayList;
import java.util.List;

/**
 * Public countInversion method takes a List of distinct integers and returns an
 * InvCounted object which is the List sorted such that there are no significant
 * inversions, with the number of significant inversions required to obtain this
 * list.
 */
public class SigInversionFinder {

public InvCounted countInversions(List<Integer> elements) {
	if (elements.size() == 0) {
		return new InvCounted();
	}
	if (elements.size() == 1) {
		return new InvCounted(elements.get(0));
	}
	int halfSize = elements.size() / 2;
	InvCounted lElements = countInversions(elements.subList(0, halfSize));
	InvCounted rElements = countInversions(elements.subList(halfSize,
			elements.size()));
	return countAndMerge(lElements, rElements);
}

private InvCounted countAndMerge(InvCounted lElements, InvCounted rElements) {
	int i = 0;
	int j = 0;
	InvCounted ret = new InvCounted();
	while (i < lElements.size() && j < rElements.size()) {
		if (2 * lElements.get(i) > rElements.get(j)) {
			// Need a significant inversion
			ret.add(rElements.get(j));
			// Add an inversion for each remaining element in lElements
			ret.inversions += lElements.size() - i;
			j++;
		} else {
			ret.add(lElements.get(i));
			i++;
		}
	}
	// Complete trivial portion of the merge
	ret.addAll(lElements.subList(i, lElements.size()));
	ret.addAll(rElements.subList(j, rElements.size()));
	return ret;
}

// END ALGORITHM (Data type definition below)
static class InvCounted extends ArrayList<Integer> {
	int inversions = 0;

	InvCounted() {
	};

	InvCounted(Integer i) {
		add(i);
	}
}

}
