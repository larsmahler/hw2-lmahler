package qa;

/*
 * Put notes here.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.*;
import org.apache.uima.cas.*;
import org.apache.uima.cas.text.*;

import edu.cmu.deiis.types.*;

/**
 * The TokenAnnotator annotates each token within each sentence (Question or Answer). 
 */
public class AnswerScorer extends JCasAnnotator_ImplBase {

  // *************************************************************
  // * process *
  // *************************************************************
  public void process(JCas aJCas) throws AnalysisEngineProcessException {

    // Iterate over Questions
    Map<String, Integer> qngMap = new HashMap<String, Integer>();
    AnnotationIndex qIndex = aJCas.getAnnotationIndex(Question.type);
    FSIterator<AnnotationFS> qIter = qIndex.iterator();
    while (qIter.hasNext()) {
      AnnotationFS q = (Question) qIter.next();

      // Within each Question, count the occurrence of each NGram
      AnnotationIndex qngIndex = aJCas.getAnnotationIndex(NGram.type);
      FSIterator<AnnotationFS> qngIter = qngIndex.subiterator(q);
      while (qngIter.hasNext()) {
        AnnotationFS qng = qngIter.next();
        String ngramText = qng.getCoveredText();
        int count = qngMap.containsKey(ngramText) ? qngMap.get(ngramText) : 0;
        qngMap.put(ngramText, count + 1);
      }
    }
    
    // Iterate over Answers and measure closeness
    AnnotationIndex aIndex = aJCas.getAnnotationIndex(Answer.type);
    FSIterator<AnnotationFS> aIter = aIndex.iterator();
    while (aIter.hasNext()) {
      AnnotationFS a = (Answer) aIter.next();
      
      // Within each Answer, count how many of its NGrams
      // exactly match NGrams in the Question
      int matching_ngrams = 0;
      int total_ngrams = 0;
      AnnotationIndex angIndex = aJCas.getAnnotationIndex(NGram.type);
      FSIterator<AnnotationFS> angIter = angIndex.subiterator(a);
      while (angIter.hasNext()) {
        AnnotationFS ang = angIter.next();
        total_ngrams += 1;
        String ngramText = ang.getCoveredText();
        if (qngMap.containsKey(ngramText)) {
          matching_ngrams += 1;
        }
      }
      
      // Create AnswerScore and insert into index
      AnswerScore annotation = new AnswerScore(aJCas);
      annotation.setBegin(a.getBegin());
      annotation.setEnd(a.getEnd());
      annotation.setCasProcessorId("AnswerScorer");
      annotation.setScore(matching_ngrams / (double) total_ngrams);
      annotation.setConfidence(1);
      annotation.setAnswer((Answer) a);
      annotation.addToIndexes();
      
    }
    
  }
}
  
  
