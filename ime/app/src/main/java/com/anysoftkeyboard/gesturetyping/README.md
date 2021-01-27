
This document describes the gesture typing system implemented by the following pull request:
https://github.com/AnySoftKeyboard/AnySoftKeyboard/pull/1870

Our gesture typing system is based on common sense and a little bit of machine-learning.
It's composed of three major components:
* A word pruning algorithm
* An ideal word-gesture generator
* A word comparison algorithm


When a user makes a gesture on the keyboard we have to pick the one word it represents out of a few hundred thousand candidates in the dictionary.

# Ideal Gesture Concept

An "ideal" gesture for a word is one that goes through the center of every-one of its keys.
Additionally for  words with identical consecutive letters ("double letters"), the ideal gesture includes a loop on that letter, to differentiate words like to/too, poll/pool, etc ...

# Pruning

The role of the pruning component is to remove most of these candidates in the least computationally expensive way.
A good pruning algorithm should have a very high specificity (number of irrelevant words removed), a high sensitivity (number of good candidates kept) and ideally be in O(1).
Ours is implemented in two steps, it prunes words by their start and end letters, then by their length.
When the pruner is instantiated, a tree of words is created that arranges words by their start and end letter, then when a user makes a gestures the two closest letters to the start of the gesture are stored, as well as the two letters closest to the end. The 4 combinations of keys is then used to lookup candidates in the tree of words. This procedure has a specificity of above 99%, meaning for a dictionary of 400k words, it would get rid of about 396k of them in O(1).
The second steps calculates the length of the ideal gestures of every remaining word (O(n)) and only keeps those that are within a threshold of the length of the user gesture. Because it is in O(n) it should only ever be applied after the first pruner. The  length threshold is a parameter which is calculated from a training dataset of gestures to give the highest sensitivity and specificity, it is expressed as a coefficient of the key radius.

# Word comparison

After the pruning we need a high precision algorithm to find the final n candidates out of the remaining few thousands. We calculate the distance between the user gesture and the ideal gesture for each candidate word and give them a confidence score based on how close they are in shape and position. These distances are assumed to be normally distributed and are transformed into probabilities using a gaussian PDF with a standard deviation derived from the training set. Finally the candidates are weighed by their frequency of use in the target language and the n best candidates are returned.

# Future improvements

All improvements should be assessed on the training and testing datasets available in the original pull request.
There are some obvious low hanging fruits that can improve this system without changing its principle of action.

* The ideal gesture isn't ideal or human-like. For example while tracing a gesture if the previous letter and subsequent letter are both lower on the keyboard than the current letter then a user would only graze the lower part of the current letter with their thumb rather than go smack-through its center. The ideal can be made more human like using the training dataset to identify these patterns.

* The length-pruning algorithm doesn't need to be in O(n), it can be made more efficient by pre-calculating ideal gesture lengths and sorting them in a linked list. Finding words within a length threshold of the user gesture would then be much more efficient.

* Both pruning algorithms can be fine-tuned to improve sensitivity and specificity.

* The training/testing datasets could be improved by recruiting users to trace the sample sentences. It'd allow us to find better parameters.

* The ideal gestures could be pre-computed if it makes sense memory wise. Perhaps only for the most frequent words.
