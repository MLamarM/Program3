
// Program 3 – Sentiment Analysis
// Name: Lamar
// Date: 10/11/2026
// CS201 – Problem Solving & Programming II

// This program reads sentiment.txt, loads word sentiment values,
// processes 8 review files, computes original sentiment,
// formats reviews to <= 80 characters per line,
// and generates more-positive and more-negative versions.

import java.io.*;
import java.util.*;

public class Program3 {

    // -----------------------------
    // Data structure for sentiment words
    // -----------------------------
    static class WordSentiment {
        String word;     // lowercase word
        double value;    // sentiment value

        WordSentiment(String w, double v) {
            this.word = w;
            this.value = v;
        }
    }

    // -----------------------------
    // Data structure for review words
    // -----------------------------
    static class ReviewWord {
        String original;     // original word with punctuation
        String stripped;     // lowercase, punctuation removed
        double sentiment;    // sentiment value

        ReviewWord(String o, String s, double v) {
            this.original = o;
            this.stripped = s;
            this.sentiment = v;
        }
    }

    public static void main(String[] args) throws Exception {

        // Load sentiment lists
        List<WordSentiment> allWords = new ArrayList<>();
        List<WordSentiment> positiveWords = new ArrayList<>();
        List<WordSentiment> negativeWords = new ArrayList<>();

        readSentimentList("sentiment.txt", allWords, positiveWords, negativeWords);

        // Output file for all reviews
        PrintWriter out = new PrintWriter(new FileWriter("reviews_output.txt"));

        // List of review files
        String[] reviewFiles = {
            "review1.txt", "review2.txt", "review3.txt", "review4.txt",
            "review5.txt", "review6.txt", "review7.txt", "review8.txt"
        };

        // Process each review file
        for (String file : reviewFiles) {

            out.println("PROCESSING FILE: " + file);
            out.println();

            File f = new File(file);
            if (!f.exists()) {
                out.println("ERROR: Could not open review file.");
                out.println("\n\n\n");
                continue;
            }

            // Read entire review into one string
            String reviewText = readWholeFile(file);

            // Split into tokens
            String[] tokens = reviewText.split("\\s+");

            // Build ReviewWord objects
            List<ReviewWord> reviewWords = new ArrayList<>();
            for (String t : tokens) {
                String stripped = stripWord(t);
                double val = findSentiment(stripped, allWords);
                reviewWords.add(new ReviewWord(t, stripped, val));
            }

            // Compute original sentiment
            double originalSentiment = computeSentiment(reviewWords);

            // Print formatted review
            out.println("FORMATTED REVIEW:");
            format80(tokens, out);
            out.println();
            out.printf("ORIGINAL SENTIMENT: %.2f%n%n", originalSentiment);

            // More positive version
            out.println("WORDS UPDATED TO BE MORE POSITIVE:");
            makeMorePositive(reviewWords, positiveWords, out);
            out.println();

            // More negative version
            out.println("WORDS UPDATED TO BE MORE NEGATIVE:");
            makeMoreNegative(reviewWords, negativeWords, out);
            out.println();

            // 3 blank lines between reviews
            out.println();
            out.println();
            out.println();
        }

        out.close();
    }

    // -----------------------------
    // Read sentiment.txt
    // -----------------------------
    static void readSentimentList(String filename,
                                  List<WordSentiment> all,
                                  List<WordSentiment> pos,
                                  List<WordSentiment> neg) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = br.readLine()) != null) {

            // Each line is "word,value"
            String[] parts = line.split(",");
            if (parts.length != 2) continue;

            String word = parts[0].toLowerCase();
            double value = Double.parseDouble(parts[1]);

            WordSentiment ws = new WordSentiment(word, value);
            all.add(ws);

            // Positive threshold > 1.25
            if (value > 1.25) pos.add(ws);

            // Negative threshold < -1.25
            if (value < -1.25) neg.add(ws);
        }

        br.close();
    }

    // -----------------------------
    // Read entire file into a string
    // -----------------------------
    static String readWholeFile(String filename) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line).append(" ");
        }

        br.close();
        return sb.toString().trim();
    }

    // -----------------------------
    // Strip punctuation + lowercase
    // -----------------------------
    static String stripWord(String w) {
        // Remove leading punctuation
        while (w.length() > 0 && !Character.isLetterOrDigit(w.charAt(0))) {
            w = w.substring(1);
        }

        // Remove trailing punctuation
        while (w.length() > 0 && !Character.isLetterOrDigit(w.charAt(w.length() - 1))) {
            w = w.substring(0, w.length() - 1);
        }

        return w.toLowerCase();
    }

    // -----------------------------
    // Find sentiment value for a word
    // -----------------------------
    static double findSentiment(String word, List<WordSentiment> all) {
        for (WordSentiment ws : all) {
            if (ws.word.equals(word)) return ws.value;
        }
        return 0.0; // neutral if not found
    }

    // -----------------------------
    // Compute total sentiment
    // -----------------------------
    static double computeSentiment(List<ReviewWord> words) {
        double total = 0;
        for (ReviewWord rw : words) {
            total += rw.sentiment;
        }
        return total;
    }

    // -----------------------------
    // Format review to <= 80 chars per line
    // -----------------------------
    static void format80(String[] tokens, PrintWriter out) {
        StringBuilder line = new StringBuilder();

        for (String t : tokens) {

            // If adding this word exceeds 80 chars, print line
            if (line.length() + t.length() + 1 > 80) {
                out.println(line.toString());
                line = new StringBuilder(t);
            } else {
                if (line.length() == 0) line.append(t);
                else line.append(" ").append(t);
            }
        }

        // Print last line
        if (line.length() > 0) out.println(line.toString());
    }

    // -----------------------------
    // Replace negative words with positive ones
    // -----------------------------
    static void makeMorePositive(List<ReviewWord> words,
                                 List<WordSentiment> pos,
                                 PrintWriter out) {

        boolean replaced = false;
        Random r = new Random();

        for (ReviewWord rw : words) {

            // Negative enough?
            if (rw.sentiment < -1.0 && !pos.isEmpty()) {

                WordSentiment replacement = pos.get(r.nextInt(pos.size()));

                out.printf("%s %.2f  %s %.2f%n",
                        rw.stripped, rw.sentiment,
                        replacement.word, replacement.value);

                replaced = true;
            }
        }

        if (!replaced) {
            out.println("No words were negative enough to be replaced.");
        }
    }

    // -----------------------------
    // Replace positive words with negative ones
    // -----------------------------
    static void makeMoreNegative(List<ReviewWord> words,
                                 List<WordSentiment> neg,
                                 PrintWriter out) {

        boolean replaced = false;
        Random r = new Random();

        for (ReviewWord rw : words) {

            // Positive enough?
            if (rw.sentiment > 1.0 && !neg.isEmpty()) {

                WordSentiment replacement = neg.get(r.nextInt(neg.size()));

                out.printf("%s %.2f  %s %.2f%n",
                        rw.stripped, rw.sentiment,
                        replacement.word, replacement.value);

                replaced = true;
            }
        }

        if (!replaced) {
            out.println("No words were positive enough to be replaced.");
        }
    }
}
