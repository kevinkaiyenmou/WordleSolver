package com.kevin;

import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
import java.util.Scanner;

import javax.swing.DefaultCellEditor;

import java.io.File;
import java.io.FileWriter;

import java.io.IOException;

import com.diogonunes.jcolor.*;

import org.openqa.selenium.grid.*;
import org.openqa.selenium.net.PortProber;
/**
 * solve() takes the list of words
 * tries the first word of that list
 * makes a sublist based off of 
 */
/**
 * I'm a bit confused on the logic for this program
 * 
 * Create a list of candidate letters for each location in the final word
 * 
 * Use the candidate letters to recursively generate all possible words
 * 
 * At each level of recursion, if the word being formed is not in the
 * dictionary, then terminate the rest of the recursion
 * 
 * If the recursion results in multiple candidate words then eliminate the words
 * that don't include a required letter (they're missing letters that we know
 * will be in the finale), then store the list of words generated and randomly
 * choose one as the next guess
 * 
 */
public class App {
  static final ObjectMapper om = new ObjectMapper();
  static final int WORD_LENGTH = 5;
  static final int NUM_ROUNDS = 6;
  static final String WIN_CONDITION = "22222";
  static final String[] startingWords = new String[] { "react", "adieu", "later", "sired", "tears", "alone", "about",
      "arise", "atone", "irate", "snare", "cream", "paint", "worse", "sauce", "anime", "prowl", "roast", "drape",
      "media" };
  static FiveLetterWords fiveLetterWords;
  static final int[] numTimesLetterAllowed = new int[(int) 'z' - (int) 'a' + 1];

 private static class SimpleAttribute extends Attribute {

    private final String _code;

    /**
     * Constructor. Maps an attribute to an Ansi code.
     *
     * @param code Ansi code that represents the attribute.
     */
    SimpleAttribute(String code) {
      _code = code;
    }

    @Override
    public String toString() {
      return _code;
    }

  }

  private static boolean isWord(String word){
    for(char c : word.toLowerCase().toCharArray()){
      if(c < 'a' || c > 'z'){
        return false;
      }
    }
    return true;
  }

  public static void main(String[] args) throws Exception {
    for (int prefix = 1; prefix <= NUM_ROUNDS; prefix++) {
      File file = new File(prefix + ".txt");
      if (file.exists()) {
        file.delete();
      }
    }
    fiveLetterWords = om.readValue(new File("resources/FiveLetterWords.json"), FiveLetterWords.class);

    // generate random starting word
    int RAND_WORD = new java.util.Random().nextInt(startingWords.length - 1);
    String suggestedWord = startingWords[RAND_WORD];
    // Suggest the starting word
    Scanner scanner = new Scanner(System.in);
    for (int currRound = 1; currRound <= NUM_ROUNDS; currRound++) {
      System.out.println("Suggested word: " + Ansi.colorize(suggestedWord,SimpleAttribute.ITALIC(),SimpleAttribute.BOLD())
          + ". Or, type your own guess. Then, enter the feedback you get from wordle, where "
          + Ansi.colorize("green", Attribute.GREEN_TEXT(), Attribute.BOLD()) + " letters are 2's, "
          + Ansi.colorize("yellow", Attribute.YELLOW_TEXT(), Attribute.BOLD()) + " letters are 1's, and "
          + Ansi.colorize("grey", new SimpleAttribute("90"), Attribute.BOLD())
          + " letters are 0's.\nPress "+Ansi.colorize("enter",Attribute.BOLD())+" to skip this word.");
      // Wait for the user to input the Wordle result (2=green,1=yellow,0=grey)
      String wordleResult = scanner.nextLine();
      if (wordleResult.isEmpty()) {
        System.out.println("Skipping word...");
        currRound--;
        fiveLetterWords.removeWord(suggestedWord);
        RAND_WORD = new java.util.Random().nextInt(fiveLetterWords.getWords().length - 1);
        suggestedWord = fiveLetterWords.getWords()[RAND_WORD];
        continue;
      }
      if (wordleResult.equals(WIN_CONDITION)) {
        System.out.println("Congratuations! You cheated Wordle.");
        scanner.close();
        return;
      }
      if(isWord(wordleResult)){
        currRound--;
        suggestedWord = wordleResult;
        continue;
      }
      int[] result = new int[WORD_LENGTH];
      char[] chars = wordleResult.toCharArray();
      
      for (int index = 0; index < WORD_LENGTH; index++) {
        result[index] = Integer.parseInt(String.valueOf(chars[index]));
        if (result[index] == 2) {
          System.out
              .print(Ansi.colorize(String.valueOf(suggestedWord.charAt(index)), Attribute.GREEN_TEXT(), Attribute.BOLD()));
        } else if (result[index] == 1) {
          System.out
              .print(Ansi.colorize(String.valueOf(suggestedWord.charAt(index)), Attribute.YELLOW_TEXT(), Attribute.BOLD()));
        } else {
          System.out.print(
              Ansi.colorize(String.valueOf(suggestedWord.charAt(index)), new SimpleAttribute("90"), Attribute.BOLD()));
        }
      }
      System.out.println();
      // Process the result and suggest a new word
      solve(result, suggestedWord);
      
      if (fiveLetterWords.getWords().length <= 0) {
        System.out.println("You lost. Wordle cheated.");
        scanner.close();
        return;
      } else if(fiveLetterWords.getWords().length == 1){
        RAND_WORD = 0;
      }else {
        RAND_WORD = new java.util.Random().nextInt(fiveLetterWords.getWords().length - 1);
      }
      suggestedWord = fiveLetterWords.getWords()[RAND_WORD];

      // creates text files 1-6 and prints word list to them after every round

      try {
        final String targetTxt = currRound + ".txt";
        // check if the targetTxt txt file exists; if so, delete it
        FileWriter writer = new FileWriter(targetTxt);
        for (String word : fiveLetterWords.getWords()) {
          writer.write(word + "\n");
        }
        char currLetter = 'a';
        writer.write("\nNumber of times each letter was allowed:\n");
        for(int c : numTimesLetterAllowed){
          writer.write(currLetter++ + ": " + c + "\n");
          numTimesLetterAllowed[(int)currLetter - 1 - (int)'a'] = 0;
        }
        writer.close();
      } catch (IOException e) {
        System.out.println("A problem occured");
      }
    }

    scanner.close();

  }

  // the method receives results from the word entered (green yellow grey are 2 1
  // 0) and the word that was entered.
  // method creates candidateLetters and tries to make words with them
  // (do we have to store all the words that the method creates or can we just use
  // the first one we find.
  // We're only going to be picking one word randomly anyways if we're storing all
  // the words found, and picking the first word we find might cut down on time.)
  // the method passes tells the user to enter in the word it found into wordle
  // and to enter the result, recurse

  public static void solve(int[] wordResults, String wordTried) {

    // trying to figure out what letters are green, yellow, or grey
    for (int i = 0; i < WORD_LENGTH; i++) {
      if (wordResults[i] == 2) {
        // implies that the letter is green (at the correct position in the word)
        // filter fiveLetterWords for words that have the letter at the same position
        numTimesLetterAllowed[(int) wordTried.charAt(i) - (int) 'a']++;
        fiveLetterWords
            .setWords(fiveLetterWords.filter((word, place) -> word.charAt(place) == wordTried.charAt(place), i));
      } else if (wordResults[i] == 1) {
        // implies that the letter is yellow (included in the word)
        // check every letter in the word for the yellow letter
        // create a for each loop that goes through every letter in the word tried
        // if the letter in word tried equals the yellow letter, return true
        // if it doesn't, check if you are at the last letter of the word
        // if you are, return false
        // if you aren't, continue?

        // what if there are more than 1 of the same yellow letter

        char yellowLetter = wordTried.charAt(i);
        numTimesLetterAllowed[(int) yellowLetter - (int) 'a']++;
        fiveLetterWords.setWords(fiveLetterWords.filter((word, place) -> {
          for (int index = 0; index < word.length(); index++) {
            if (word.charAt(index) == yellowLetter && index != place) { // right letter, return true.
              return true;
            }
          }
          return false; // only gets to here if the non of the letters match the yellow letter and is on
                        // the last character?
        }, i));

      } else {
        // implies that the letter is grey (excluded from the word)
        char greyLetter = wordTried.charAt(i);
        fiveLetterWords.setWords(fiveLetterWords.filter((word) -> {
          for (char letter : word.toCharArray()) {
            if (letter == greyLetter) {
              // make sure the letter is not repeated in the word tried
              int timesSeen = 0;
              for (char letterToCheck : word.toCharArray()) {
                // the letter should only appear numTimesLetterAllowed[(int)greyLetter -
                // (int)'a'] times in the word
                // if it appears more than that, it is not a candidate
                if (letterToCheck == greyLetter && timesSeen++ >= numTimesLetterAllowed[(int)greyLetter - (int) 'a']) {
                  return false;
                }
              }
            }
          }
          return true; // the word is a candidate, since it survived the filter
        }));
      }
    }
  }
}