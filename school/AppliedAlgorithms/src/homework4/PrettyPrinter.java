package homework4;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Tardos Ch. 6, Pr. 6
public class PrettyPrinter {

	int width; // The column width of the document
	List<Word> words; // The words in the document
	// The table of optimal documents (per choice of number of lines and max
	// size of subset of words)
	Map<WordsLines, Doc> optDocs = new HashMap<WordsLines, Doc>();

	public Doc prettyPrint() {
		optDocs.put(new WordsLines(0, 0), new Doc());
		for (int numWords = 1; numWords < words.size() + 1; numWords++) {
			optDocs.put(new WordsLines(numWords, 0), null);
			boolean finished = false; // Used to avoid un-neccessary recursions in inner loop
			for (int numLines = 1; numLines < words.size() + 1; numLines++) {
				WordsLines coords = new WordsLines(numWords, numLines);
				optDocs.put(coords, null);
				if (!finished) {
					Doc optDoc = findOptDoc(numWords, numLines);
					WordsLines prevCoords = new WordsLines(numWords - 1, numLines);
					optDocs.put(coords, optDoc);
					if (optDocs.get(coords) != null
							&& optDocs.get(prevCoords) != null
							&& optDocs.get(coords).slack() 
							> optDocs.get( prevCoords).slack()) {
						// Our slack is getting worse for this count of words
						// with additional lines
						finished = true;
					}
				}
			}
		}
		// Now find the best line count for the given complete list of words
		Doc bestOptDoc = null;
		for (int numLines = 1; numLines < words.size() + 1; numLines++) {
			WordsLines coords = new WordsLines(words.size() + 1, numLines);
			if (optDocs.get(coords) != null) {
				if (bestOptDoc == null
						|| (optDocs.get(coords).slack() < bestOptDoc.slack())) {
					bestOptDoc = optDocs.get(coords);
				}
			}
		}
		return bestOptDoc;
	}

	// Compares options using previously computed values
	public Doc findOptDoc(int numWords, int numLines) {
		Doc bestOptDoc = optDocs.get(new WordsLines(numLines - 1, numWords));
		int usedThisLine = -1; // Start at offset -1 because the first word has
		// no leading space
		int i = numWords - 1;
		while (usedThisLine < width) {
			usedThisLine += words.get(--i).length;
			Doc thisOptDoc = optDocs.get(new WordsLines(numLines - 1, i));
			if (thisOptDoc != null) {
				if (bestOptDoc == null
						|| bestOptDoc.slack() > thisOptDoc.slack()
								+ squared(width - usedThisLine)) {
					bestOptDoc = thisOptDoc;
					bestOptDoc.lines.add(new Line(words.subList(i, numWords)));
				}
			}
		}
		return bestOptDoc;
	}

	// END ALGORITHM (Data type definitions and helper methods below)	
	public static class WordsLines {
		int numWords;
		int numLines;

		WordsLines(int numWords, int numLines) {
			this.numWords = numWords;
			this.numLines = numLines;
		}

		@Override
		public int hashCode() { return 10000000 * numLines + numLines; }
	}

	// A document with choices of subsets made
	class Doc {
		List<Line> lines = new ArrayList<Line>();

		int slack() {
			int s = 0;
			for (Line line : lines) { s += squared(width - line.used()); }
			return s;
		}
	}

	int squared(int n) {
		return (int) Math.pow(n, 2);
	}

	static class Line {
		List<Word> words;

		Line(List<Word> words) {
			this.words = words;
		}

		int used() {
			if (words.size() == 0) { return 0; }
			int used = -1;
			for (Word word : words) { used += word.length + 1; }
			return used;
		}
	}

	static class Word {
		int length;
	}

}
