package com.kevin;
import java.util.ArrayList;
import java.util.function.*;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FiveLetterWords {
    @JsonProperty("words")
    private String[] words;

    /**
    * Return an array of words that matches the condition imposed by the  predicate, <code>p</code>, at the character index, <code>i</code>.
    *
    * @param  p  The predicate to that tests a string against an index.
    * @param  i The index to test the predicate at.
    * @return      <code>True</code> if the predicate is true for all words at the index, <code>False</code> otherwise.
    */
    public String[] filter(BiPredicate<String, Integer> p, int i) {
        // example of a predicate: (word,len) -> word.length() == len
        ArrayList<String> result = new ArrayList<String>();
        for (String word : words) {
            if (p.test(word,i)) {
                result.add(word);
            }
        }
        return result.toArray(new String[result.size()]);
    }
    public String[] filter(Predicate<String> p) {
        // example of a predicate: (word,len) -> word.length() == len
        ArrayList<String> result = new ArrayList<String>();
        for (String word : words) {
            if (p.test(word)) {
                result.add(word);
            }
        }
        return result.toArray(new String[result.size()]);
    }
    public String[] getWords(){
        return words;
    }
    public void setWords(String[] words){
        this.words = words;
    }

    public void removeWord(String word){
        words = filter((w) -> !w.equals(word));
    }
}
