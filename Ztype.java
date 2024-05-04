import tester.*;

import tester.Tester;

import java.awt.Color;

import javalib.funworld.World;

import javalib.funworld.WorldScene;

import javalib.worldimages.FontStyle;

import javalib.worldimages.RectangleImage;

import javalib.worldimages.TextImage;

import javalib.worldcanvas.*;

import java.util.Random;

import javalib.worldimages.*;

import javalib.funworld.*;


import java.awt.Color;

import java.util.Random;

//implements the class ZTypeWorld
class ZTypeWorld extends World {

  Random rand;

  ILoWord listOfWords;

  IWord currentWord;

  int timer;

  int lives;

  int score;

// Constructor
  ZTypeWorld(Random rand, ILoWord listOfWords, IWord currentWord, int timer, int lives, int score) {
    this.rand = rand;
    this.listOfWords = listOfWords;
    this.currentWord = currentWord;
    this.timer = timer;
    this.lives = lives;
    this.score = score;
  }

// Constructor
  ZTypeWorld(Random rand) {
    this.rand = rand;
    this.listOfWords = new Utils().randomWord(10, rand);
    this.currentWord = new InactiveWord("", 0, 0);
    this.timer = 10;
    this.lives = 1;
    this.score = 0;
  }

// Constructor
  ZTypeWorld() {
    this.rand = new Random();
    this.listOfWords = new Utils().randomWord(10, rand);
    this.currentWord = new InactiveWord("", 0, 0);
    this.timer = 10;
  }
  
  // draws the WorldScene
  public WorldScene makeScene() {
    return this.listOfWords.draw(new WorldScene(500, 500));
  }

  public WorldScene lastScene(String msg) {
    return new WorldScene(300, 300)
        .placeImageXY(new RectangleImage(300, 300, "solid", Color.BLACK), 150, 150)
        .placeImageXY(new TextImage("Game Over", Color.RED), 100, 100);
  }

  // every tick it moves words down and every tenth tick adds new word
  public World onTick() {
    if (this.listOfWords.bottomOfScreen()) {
      return this.endOfWorld("Game Over!!");
    }
    
    if (this.timer % 10 == 0) {
      return new ZTypeWorld(this.rand,
          this.listOfWords.moveWordsDown()
              .addToEnd(new InactiveWord(new Utils().randomSix(rand),
                  rand.nextInt(IUtils.BACKGROUND_WIDTH), 0)),
          this.currentWord, timer + 1, this.lives, this.score);
    }
    return new ZTypeWorld(this.rand, this.listOfWords.moveWordsDown(), this.currentWord, timer + 1,
        this.lives, this.score);
  }

// when a key is pressed makes new world and if active check and reduces
  public World onKeyEvent(String key) {
    if (this.listOfWords.isActive()) {
      return new ZTypeWorld(this.rand, this.listOfWords.checkAndReduce(key).remove(),
          this.currentWord, this.timer, this.lives, this.score);
    }
    else {
      return new ZTypeWorld(this.rand, this.listOfWords.changeWord(key).remove(), this.currentWord,
          this.timer, this.lives, this.score);
    }
  }
}

// implements the IUtils interface
interface IUtils {

// constants
  int BACKGROUND_HEIGHT = 600;
  int BACKGROUND_WIDTH = 600;
  String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
}

//implements the Utils class
class Utils implements IUtils {

// produces a six letter random word
  String randomSix(Random rand) {
    return randomSixAcc(rand, "", 6);
  }

// accumulator that produces a string of random letters
  String randomSixAcc(Random rand, String acc, int count) {
    if (count == 0) {
      return acc;
    }
    else {
      int index = rand.nextInt(26);
      return randomSixAcc(rand, acc + ALPHABET.substring(index, index + 1), count - 1);
    }
  }

// generates random word with place on screen
  ILoWord randomWord(int length, Random rand) {
    if (length == 0) {
      return new MtLoWord();
    }
    else {
      return new ConsLoWord(
          new InactiveWord(this.randomSix(rand), rand.nextInt(IUtils.BACKGROUND_WIDTH), 0),
          this.randomWord(length - 1, rand));
    }
  }
}

//represents a list of words
interface ILoWord {
//draws all the words onto the WorldScene
  WorldScene draw(WorldScene w);

  // removes an empty words from list
  ILoWord remove();

  // moves words down the screen
  ILoWord moveWordsDown();

  // adds IWord to end of a list of words (ILoWord
  ILoWord addToEnd(IWord w);

  //reduces active words in a ILoWord by removing the first letter
  // if the given string matches the first letter
  ILoWord checkAndReduce(String letter);

  // checks if word is active or inactive
  boolean isActive();

  // if inactive word make a word active 
  // if matches letter & if active reduce
  ILoWord changeWord(String letter);

  // checks if words made it to bottom of screen
  boolean bottomOfScreen();
}

//represents an empty list of words
class MtLoWord implements ILoWord {
  //reduces active words in a ILoWord by removing the first letter
  // if the given string matches the first letter
  public ILoWord checkAndReduce(String letter) {
    return this;
  }

  // draws all of the words in this ILoWord onto the given WorldScene
  public WorldScene draw(WorldScene w) {
    return w;
  }

  // produces an ILoWord with the given IWord added at the end
  public ILoWord addToEnd(IWord w) {
    return new ConsLoWord(w, this);
  }

  // removes an empty words from list
  public ILoWord remove() {
    return this;
  }

  // moves words down the screen
  public ILoWord moveWordsDown() {
    return this;
  }

  // checks if word is active or inactive
  public boolean isActive() {
    return false;
  }

  // if inactive word make a word active if matches letter and if active reduce
  public ILoWord changeWord(String letter) {
    return this;
  }

  // checks if words made it to bottom of screen
  public boolean bottomOfScreen() {
    return false;
  }
}

//implements the class ConsLoWord
class ConsLoWord implements ILoWord {

  IWord first;
  ILoWord rest;

  ConsLoWord(IWord first, ILoWord rest) {
    this.first = first;
    this.rest = rest;
  }

  // draws all the words onto the WorldScene
  public WorldScene draw(WorldScene w) {
    return this.rest.draw(this.first.drawWord(w));
  }

  // produces an ILoWord with the given IWord added at the end
  public ILoWord addToEnd(IWord w) {
    return new ConsLoWord(this.first, this.rest.addToEnd(w));
  }

  // if inactive word make a word active 
  // if matches letter and if active reduce
  public ILoWord changeWord(String letter) {
    if (this.first.helperCheckAndReduce(letter).beActive()) {
      return new ConsLoWord(this.first.helperCheckAndReduce(letter), this.rest);
    }
    else {
      return new ConsLoWord(this.first, this.rest.changeWord(letter));
    }
  }

  // checks if word is active or inactive
  public boolean isActive() {
    if (this.first.beActive()) {
      return true;
    }
    else {
      return this.rest.isActive();
    }
  }

  // removes empty words from list
  public ILoWord remove() {
    if (this.first.isEmpty()) {
      return this.rest;
    }
    else {
      return new ConsLoWord(this.first, this.rest.remove());
    }
  }

  // moves words down the screen
  public ILoWord moveWordsDown() {
    return new ConsLoWord(this.first.moveDown(), this.rest.moveWordsDown());
  }

//reduces active words in a ILoWord by removing the first letter
  // if the given string matches the first letter
  public ILoWord checkAndReduce(String letter) {
    if (this.first.beActive()) {
      return new ConsLoWord(this.first.helperCheckAndReduce(letter),
          this.rest.checkAndReduce(letter));
    }
    else {
      return new ConsLoWord(this.first, this.rest.checkAndReduce(letter));
    }
  }

// checks if the word have reached the bottom of screen
  public boolean bottomOfScreen() {
    if (this.first.reachedEnd()) {
      return true;
    }
    else {
      return this.rest.bottomOfScreen();
    }
  }
  
}

//represents a word in the ZType game
interface IWord {

 // checks if words y coordinate reached bottom of screen
  boolean reachedEnd();

  // draws word on word string
  WorldScene drawWord(WorldScene w);

  // moves the word down a coordinate
  IWord moveDown();

  // checks if word is non empty and 
  // if first letter equals letter and
  // if so makes new word without first letter
  IWord helperCheckAndReduce(String letter);

  // checks if the word is active
  boolean beActive();

  // checks if a word is empty
  boolean isEmpty();
}

//represents an active word in the ZType game
class ActiveWord implements IWord {
  String word;
  int x;
  int y;

  ActiveWord(String word, int x, int y) {
    this.word = word;
    this.x = x;
    this.y = y;
  }

// checks if y coordinate reached bottom of screen
  public boolean reachedEnd() {
    return this.y >= Utils.BACKGROUND_HEIGHT;
  }

  // checks if the word is active
  public boolean beActive() {
    return true;
  }

  // checks if a word is empty
  public boolean isEmpty() {
    return this.word.equals("");
  }

  // draws a word on the WorldScene
  public WorldScene drawWord(WorldScene w) {
    return w.placeImageXY(new TextImage(this.word, Color.RED), this.x, this.y);
  }

  // moves the word down a coordinate
  public IWord moveDown() {
    return new ActiveWord(this.word, this.x, this.y + 1);
  }

  //checks if word is non empty and 
  // if first letter equals letter and
  // if so makes new word without first letter
  public IWord helperCheckAndReduce(String s) {
    if (this.word.length() > 0 && 
        this.word.substring(0, 1).equals(s)) {
      return new ActiveWord(this.word.substring(1, this.word.length()),
          this.x, this.y);
    }
    return this;
  }
}

//represents an inactive word in the ZType game
class InactiveWord implements IWord {
  String word;
  int x;
  int y;

  InactiveWord(String word, int x, int y) {
    this.word = word;
    this.x = x;
    this.y = y;
  }

  // checks if words y coordinate reached bottom of screen
  public boolean reachedEnd() {
    return this.y >= Utils.BACKGROUND_HEIGHT;
  }

  //checks if word is non empty and 
  // if first letter equals letter and
  // if so makes new word without first letter
  public IWord helperCheckAndReduce(String s) {
    if (this.word.length() > 0 && this.word.substring(0, 1).equals(s)) {
      return new ActiveWord(this.word.substring(1, this.word.length()), this.x, this.y);
    }
    return this;
  }

  // checks if a word is empty
  public boolean isEmpty() {
    return this.word.equals("");

  }

  // checks if the word is active
  public boolean beActive() {
    return false;
  }

  // draws a word on the WorldScene
  public WorldScene drawWord(WorldScene w) {
    return w.placeImageXY(new TextImage(this.word, Color.BLUE), this.x, this.y);
  }

  // moves the word down a coordinate
  public IWord moveDown() {
    return new InactiveWord(this.word, this.x, this.y + 1);
  }
  
}

// All examples and tests for ILoWord
class ExamplesWordLists {
  // empty list of words
  ILoWord mtWord = new MtLoWord();
  // Inactive words
  IWord word1 = new InactiveWord("apple", 100, 100);
  IWord word2 = new InactiveWord("banana", 200, 30);
  IWord word3 = new InactiveWord("orange", 300, 400);
  IWord word4 = new InactiveWord("grape", 70, 400);
  IWord word5 = new InactiveWord("strawberry", 50, 400);
  IWord word6 = new InactiveWord("pineapple", 50, 40);
  // Active words
  IWord word7 = new ActiveWord("peach", 150, 200);
  IWord word8 = new ActiveWord("watermelon", 250, 300);
  IWord word9 = new ActiveWord("kiwi", 350, 50);
  IWord word10 = new ActiveWord("blueberry", 70, 150);
  IWord word11 = new ActiveWord("raspberry", 200, 250);
  IWord word12 = new ActiveWord("blackberry", 400, 400);
  // Empty lists
  ILoWord empty = new MtLoWord();
  // Lists with multiple words
  // Examples of scenes
  WorldScene blankScene = new WorldScene(500, 500)
      .placeImageXY(new RectangleImage(250, 250, "solid", Color.GRAY), 250, 250);
  WorldScene expectedSceneInactiveWords = blankScene
      .placeImageXY(new TextImage("apple", Color.BLUE), 100, 100)
      .placeImageXY(new TextImage("banana", Color.BLUE), 200, 30)
      .placeImageXY(new TextImage("orange", Color.BLUE), 300, 400)
      .placeImageXY(new TextImage("grape", Color.BLUE), 70, 400)
      .placeImageXY(new TextImage("strawberry", Color.BLUE), 50, 400)
      .placeImageXY(new TextImage("pineapple", Color.BLUE), 50, 40);
  WorldScene expectedSceneActiveWords = blankScene
      .placeImageXY(new TextImage("peach", Color.RED), 150, 200)
      .placeImageXY(new TextImage("watermelon", Color.RED), 250, 300)
      .placeImageXY(new TextImage("kiwi", Color.RED), 350, 50)
      .placeImageXY(new TextImage("blueberry", Color.RED), 70, 150)
      .placeImageXY(new TextImage("raspberry", Color.RED), 200, 250)
      .placeImageXY(new TextImage("blackberry", Color.RED), 400, 400);

// Tests for the Utils class
  Utils u = new Utils();
  
//Tests the isEmpty method
  boolean testIsEmpty(Tester t) {
    return t.checkExpect(new InactiveWord("",100,100).isEmpty(), true)
        && t.checkExpect(word1.isEmpty(), false)
        && t.checkExpect(word7.isEmpty(), false);
}

//Tests the addToEnd method
  boolean testAddToEnd(Tester t) {
    // Adds a word to the end that's already in the list
    return t.checkExpect(new ConsLoWord(word1, empty).addToEnd(word1),
        new ConsLoWord(word1, new ConsLoWord(word1, empty)))
        // Adds a new word to the end
        && t.checkExpect(new ConsLoWord(word1, empty).addToEnd(word2),
            new ConsLoWord(word1, new ConsLoWord(word2, empty)))
        // Invoked by empty list
        && t.checkExpect(empty.addToEnd(word1),
            new ConsLoWord(word1, new ConsLoWord(word1, empty)));
  }

//Tests the helperCheckAndReduce method
  boolean testHelperCheckAndReduce(Tester t) {
    // Checks an empty case
    return t.checkExpect(new ActiveWord("", 100, 100).helperCheckAndReduce("m"), empty)
        // Checks a non-empty invoked on an empty string
        && t.checkExpect(word2.helperCheckAndReduce(""), word2)
        // Checks where it removes the first letter
        && t.checkExpect(word1.helperCheckAndReduce("a"), 
            new ActiveWord("pple", 100, 100))
        // Checks where it doesn't remove the first letter
        && t.checkExpect(word7.helperCheckAndReduce("f"), word7);
  }

//Tests the checkAndReduce method
  boolean testCheckAndReduce(Tester t) {
    // Reduces all items in list since both start with inputted letter
    return t.checkExpect(new ConsLoWord(word1, new ConsLoWord(word1, empty)).checkAndReduce("a"),
        new ConsLoWord(new ActiveWord("pple", 100, 100),
            new ConsLoWord(new ActiveWord("pple", 100, 100), empty)))
        // Doesn't reduce any items since first letter doesn't match any
        && t.checkExpect(new ConsLoWord(word2, empty).checkAndReduce("Z"),
            new ConsLoWord(word2, empty));
  }

//Tests the beActive method
  boolean testBeActive(Tester t) {
    return t.checkExpect(word10.beActive(), true) && t.checkExpect(word1.beActive(), false);
  }

//Tests the isActive method
  boolean testIsActive(Tester t) {
    return t.checkExpect(new ConsLoWord(word10, empty).isActive(), true)
        && t.checkExpect(new ConsLoWord(word1, empty).isActive(), false)
        && t.checkExpect(empty.isActive(), false);
  }

//Tests the onTick method
  boolean testOnTick(Tester t) {
    return t.checkExpect(new ZTypeWorld().onTick(), new ZTypeWorld())
        && t.checkExpect(new ZTypeWorld().onTick(), new ZTypeWorld())
        && t.checkExpect(new ZTypeWorld().onTick(), new ZTypeWorld());
  }

//Tests the moveDown method
  boolean testMoveDown(Tester t) {
    return t.checkExpect(word7.moveDown(), new ActiveWord("peach", 150, 201))
        && t.checkExpect(word1.moveDown(), new InactiveWord("apple", 100, 101));
  }

//Tests the moveWordsDown method
  boolean testMoveWordsDown(Tester t) {
    return t.checkExpect(new ConsLoWord(word1, new ConsLoWord(word2, empty)).moveWordsDown(),
        new ConsLoWord(new InactiveWord("apple", 100, 101),
            new ConsLoWord(new InactiveWord("banana", 200, 31), empty)))
        && t.checkExpect(empty.moveWordsDown(), empty);
  }

//Tests the remove method
  boolean testRemove(Tester t) {
    return t.checkExpect(empty.remove(), empty)
        && t.checkExpect(new ConsLoWord(word1, new ConsLoWord(word2, empty)).remove(),
            new ConsLoWord(word1, new ConsLoWord(word2, empty)))
        && t.checkExpect(new ConsLoWord(word1, empty).remove(), empty);
  }

//Tests the changeWord method
  boolean testChangeWord(Tester t) {
    return t.checkExpect(new ConsLoWord(word2, new ConsLoWord(word3, empty)).changeWord("p"),
        new ConsLoWord(word2, new ConsLoWord(word3, empty)))
        && t.checkExpect(new ConsLoWord(word1, empty).changeWord("a"),
            new ConsLoWord(new ActiveWord("pple", 100, 100), empty))
        && t.checkExpect(empty.changeWord("p"), empty);
  }

//Tests onKeyEvent method
  boolean testOnKeyEvent(Tester t) {
    return t.checkExpect(new ZTypeWorld().onKeyEvent("l"), new ZTypeWorld())
        && t.checkExpect(new ZTypeWorld().onKeyEvent("f"), new ZTypeWorld())
        && t.checkExpect(new ZTypeWorld().onKeyEvent(""), new ZTypeWorld());
  }

//Tests draw method
  boolean testDraw(Tester t) {
    return t.checkExpect(new ConsLoWord(word1, new ConsLoWord(word2, empty)).draw(blankScene),
        expectedSceneInactiveWords) && t.checkExpect(empty.draw(blankScene), blankScene);
  }

//Tests drawWord method
  boolean testDrawWord(Tester t) {
    return t.checkExpect(word1.drawWord(blankScene), expectedSceneInactiveWords);
  }

//Tests the lastScene method
  boolean testLastScene(Tester t) {
    return t.checkExpect(new ZTypeWorld().lastScene("Game Over!"),
        new WorldScene(300, 300)
            .placeImageXY(new RectangleImage(300, 300, "solid", Color.BLACK), 150, 150)
            .placeImageXY(new TextImage("Game Over!", Color.RED), 100, 100));
  }

//Tests the bottomOfScreen method
  boolean testBottomOfScreen(Tester t) {
    return t.checkExpect(new ConsLoWord(word1, empty).bottomOfScreen(), false)
        && t.checkExpect(new ConsLoWord(word4, empty).bottomOfScreen(), false)
        && t.checkExpect(empty.bottomOfScreen(), false);
  }

//Tests the reachedEnd method
  boolean testReachedEnd(Tester t) {
    return t.checkExpect(word1.reachedEnd(), false) && t.checkExpect(word5.reachedEnd(), false)
        && t.checkExpect(new InactiveWord("", 0, Utils.BACKGROUND_HEIGHT).reachedEnd(), true);
  }

// Tests the randomSix method
  boolean testRandomSix(Tester t) {
    return t.checkExpect(u.randomSix(new Random(0)), "ssxvnj")
        && t.checkExpect(u.randomSix(new Random(10)), 0)
        && t.checkExpect(u.randomSix(new Random(20)), 0);
  }

// Tests the randomSixAcc method
  boolean testRandomSixAcc(Tester t) {
    // Tests when RandomCount is 0
    return t.checkExpect(u.randomSixAcc(new Random(0), "", 0), "")
        // Tests when count is 1 and letters are inputted
        && t.checkExpect(u.randomSixAcc(new Random(10), "abc", 1), "abcp")
        // Tests when count is 6 like in game
        && t.checkExpect(u.randomSixAcc(new Random(20), "", 6), "xsvphp");
  }

// Tests the randomWord method
  boolean testRandomWord(Tester t) {
    return t.checkExpect(u.randomWord(0, new Random(0)), new MtLoWord())
        && t.checkExpect(u.randomWord(6, new Random(10)), "");
  }

//Runs ZType game
  boolean testBigBang(Tester t) {
    ZTypeWorld world = new ZTypeWorld(new Random());
    double tickRate = .1;
    return world.bigBang(IUtils.BACKGROUND_WIDTH, IUtils.BACKGROUND_HEIGHT, tickRate);
  }

//Runs empty ZType game (non-functional)
  boolean testWorldScene(Tester t) {
    return t.checkExpect(empty.draw(blankScene), blankScene);
  }
}