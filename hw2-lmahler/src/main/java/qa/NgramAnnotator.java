package qa;

/*
 * The NgramAnnotator converts sequences of Token annotations into Ngrams.
 * 
 * A parameter allows the user to specify the which NGram orders will be created.
 *  - Ex: 1: setting the parameter to "1,2,3" will create unigrams, bigrams, and trigrams.
 *  - Ex: 2: setting the parameter to "2,4" would create bigrams and 4-grams.
 * 
 * Each time an NGram annotation is created:
 *  - The start feature is set equal to the start of the first Token in the NGram.
 *  - The end feature is set equal to the end of the last Token in the NGram.
 *  - The elements feature is an array that contains pointers to each Token in the NGram.
 *  - The elementsType feature is set to “Token”. (Note: this could become parameterized in future
 *    releases, if types other than Tokens are used in NGram models)
 *  - The ngramOrder feature tells whether the NGram is a unigram (“1”), bigram (“2”), etc – this 
 *    may be used in future releases for machine learning models that weight the NGrams differently.
 *  - The source feature indicates whether the NGram came from a Question or an Answer. This is used in downstream
 *    processing to compare Answer NGrams against their corresponding Question NGrams.
 */

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.*;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.cas.*;
import org.apache.uima.cas.text.*;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.deiis.types.*;

/**
 * The NgramAnnotator converts sequences of Token annotations into Ngrams.
 * <br>
 * A parameter allows the user to specify the which NGram orders will be created.
 * <br> - Ex: 1: setting the parameter to "1,2,3" will create unigrams, bigrams, and trigrams.
 * <br> - Ex: 2: setting the parameter to "2,4" would create bigrams and 4-grams.
 * <br>
 * <br>Each time an NGram annotation is created:
 * <br> - The start feature is set equal to the start of the first Token in the NGram.
 * <br> - The end feature is set equal to the end of the last Token in the NGram.
 * <br> - The elements feature is an array that contains pointers to each Token in the NGram.
 * <br> - The elementsType feature is set to “Token”. (Note: this could become parameterized in future releases, if types other than Tokens are used in NGram models)
 * <br> - The ngramOrder feature tells whether the NGram is a unigram (“1”), bigram (“2”), etc – this may be used in future releases for machine learning models that weight the NGrams differently.
 * <br> - The source feature indicates whether the NGram came from a Question or an Answer. This is used in downstream processing to compare Answer NGrams against their corresponding Question NGrams.
 */
public class NgramAnnotator extends JCasAnnotator_ImplBase {
  private Integer[] ngramOrders;

  /**
   * @see AnalysisComponent#initialize(UimaContext)
   */
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    // Get config. parameter values
    ngramOrders = (Integer[]) aContext.getConfigParameterValue("NgramOrder");
  }

  // *************************************************************
  // * process *
  // *************************************************************
  public void process(JCas aJCas) throws AnalysisEngineProcessException {

    for (int ngo : ngramOrders) {
      // 1) Iterate over Questions first, and create tokens
      AnnotationIndex qIndex = aJCas.getAnnotationIndex(Question.type);
      FSIterator<AnnotationFS> qIter = qIndex.iterator();
      // Loop over each Question
      while (qIter.hasNext()) {
        AnnotationFS q = (Question) qIter.next();

        AnnotationIndex tIndex = aJCas.getAnnotationIndex(Token.type);
        FSIterator<AnnotationFS> tIter = tIndex.subiterator(q);
        // Then loop over each left-most token in the Ngram
        while (tIter.hasNext()) {
          int complete = 0;
          int startPos = q.getEnd();
          int endPos = 0;
          String temp;
          FSArray tokenArray = new FSArray(aJCas, ngo);

          FSIterator<AnnotationFS> t2Iter = tIter.copy();
          // Starting from the left-most token in the Ngram, loop over
          // each of the other tokens to get start / end positions (for
          // the Ngram), and populate the tokenArray, which will be
          // used to populate the Ngram.elements feature.
          for (int i = 0; i < ngo; i++) {
            complete = 0;
            if (t2Iter.hasNext()) {
              Annotation t = (Annotation) t2Iter.next();
              temp = t.getCoveredText();
              startPos = Math.min(startPos, t.getBegin());
              endPos = Math.max(endPos, t.getEnd());
              tokenArray.set(i, t);
              complete = 1;
            } else {
              break;
            }
          }
          if (complete == 1) {
            // Create NGram and insert into index
            NGram annotation = new NGram(aJCas);
            annotation.setBegin(startPos);
            annotation.setEnd(endPos);
            annotation.setCasProcessorId("NgramAnnotator");
            annotation.setConfidence(1.0);
            annotation.setElements(tokenArray);
            annotation.setNgramOrder(ngo);
            annotation.setElementType("Token");
            annotation.setSource("Question");
            annotation.addToIndexes();
          }

          tIter.next();
        }
      }

      // 2) Iterate over Answer second, and create tokens
      AnnotationIndex aIndex = aJCas.getAnnotationIndex(Answer.type);
      FSIterator<AnnotationFS> aIter = aIndex.iterator();
      // Loop over each Answer
      while (aIter.hasNext()) {
        AnnotationFS a = (Answer) aIter.next();

        AnnotationIndex tIndex = aJCas.getAnnotationIndex(Token.type);
        FSIterator<AnnotationFS> tIter = tIndex.subiterator(a);
        // Then loop over each left-most token in the Ngram
        while (tIter.hasNext()) {
          int complete = 0;
          int startPos = a.getEnd();
          int endPos = 0;
          String temp;
          FSArray tokenArray = new FSArray(aJCas, ngo);

          FSIterator<AnnotationFS> t2Iter = tIter.copy();
          // Starting from the left-most token in the Ngram, loop over
          // each of the other tokens to get start / end positions (for
          // the Ngram), and populate the tokenArray, which will be
          // used to populate the Ngram.elements feature.
          for (int i = 0; i < ngo; i++) {
            complete = 0;
            if (t2Iter.hasNext()) {
              Annotation t = (Annotation) t2Iter.next();
              temp = t.getCoveredText();
              startPos = Math.min(startPos, t.getBegin());
              endPos = Math.max(endPos, t.getEnd());
              tokenArray.set(i, t);
              complete = 1;
            } else {
              break;
            }
          }
          if (complete == 1) {
            // Create NGram and insert into index
            NGram annotation = new NGram(aJCas);
            annotation.setBegin(startPos);
            annotation.setEnd(endPos);
            annotation.setCasProcessorId("NgramAnnotator");
            annotation.setConfidence(1.0);
            annotation.setElements(tokenArray);
            annotation.setNgramOrder(ngo);
            annotation.setElementType("Token");
            annotation.setSource("Answer");
            annotation.addToIndexes();
          }

          tIter.next();
        }
      }

    }

  }
}
