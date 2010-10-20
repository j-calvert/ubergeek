package homework4;
import java.util.List;

//Tardos Ch. 5, Pr. 3
public class CardListEvaluator {

	EqCardSet majoritySet(List<Card> cards) {
		if (cards.size() == 0) { return null; }
		if (cards.size() == 1) { return new EqCardSet(cards.get(0)); }
		int halfSize = cards.size() / 2;
		List<Card> lCards = cards.subList(0, halfSize);
		List<Card> rCards = cards.subList(halfSize, cards.size());
		EqCardSet cardSet = merge(lCards, majoritySet(rCards), halfSize);
		if (cardSet != null) { return cardSet; }
		cardSet = merge(rCards, majoritySet(lCards), halfSize);
		if (cardSet != null) { return cardSet; }
		return null;
	}

	EqCardSet merge(List<Card> cards, EqCardSet cardSet, int halfSize) {
		if (cardSet == null) { return null; }
		for (Card card : cards) {
			// Where equals is equivalence provided by card machine
			if (card.equals(cardSet.representative)) { cardSet.size++; }
		}
		// Only return non-null if it's a majority subset of the larger set
		if (cardSet.size > halfSize) { return cardSet; } 
		else { return null; }
	}

	// END ALGORITHM (Data type definitions and helper methods below)	
	static class EqCardSet {
		int size;
		Card representative;

		public EqCardSet(Card card) {
			this.size = 1;
			this.representative = card;
		}
	}

	public static class Card {
		private int equivalence;

		Card(int e) {
			this.equivalence = e;
		}

		public boolean equals(Object obj) {
			return (obj instanceof Card)
					&& ((Card) obj).equivalence == this.equivalence;
		}
	}

	// BELOW IS NOT PART OF THE ALGORITHM
	// Some verification code. Add a value != 2 to the cardIds array for the
	// negative case.
	// Interesting that in the negative case, it's about 50% longer run time.
	// public static void main(String[] args) {
	// int[] cardIds = new int[] { 1, 2, 3, 1, 2, 2, 2, 1, 3, 2, 2, 2, 3 };
	// for (int j = 400; j < 500; j++) {
	// List<Card> cards = new ArrayList<Card>();
	// for (int i = 0; i < cardIds.length * j * 100; i++) {
	// cards.add(new Card(cardIds[i % cardIds.length]));
	// }
	// CardListEvaluator cle = new CardListEvaluator();
	// EquivCardSet cs = cle.majoritySet(cards);
	// System.out.println(""
	// + (cs == null ? "null" : cs.representative.equivalence)
	// + " " + cards.size() + " " + cle.callCount + " "
	// + cards.size() * Math.log(cards.size()) + " "
	// + cle.callCount / (cards.size() * Math.log(cards.size())));
	// }
	// }
}
