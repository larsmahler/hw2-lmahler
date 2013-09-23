package qa;

/*
 * Put notes here.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * The TokenAnnotator annotates each token within each sentence (Question or Answer). 
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
          int complete=0;
          int startPos=q.getEnd();
          int endPos=0;
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
          if (complete==1) {
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
          int complete=0;
          int startPos=a.getEnd();
          int endPos=0;
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
          if (complete==1) {
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
  
