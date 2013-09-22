package qa;

/*
 * Put notes here.
 */

import java.io.StringReader;
import java.util.Iterator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.*;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.PTBTokenizer.PTBTokenizerFactory;

/**
 * The TokenAnnotator annotates each token within each sentence (Question or Answer). 
 */
public class TokenAnnotator extends JCasAnnotator_ImplBase {

  // *************************************************************
  // * process *
  // *************************************************************
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    String docText = aJCas.getDocumentText();

    TokenizerFactory<Word> factory = PTBTokenizerFactory.newTokenizerFactory();
    Tokenizer<Word> tokenizer = factory.getTokenizer(new StringReader(aJCas.getDocumentText()));
    while (tokenizer.hasNext()) {
      Word w = tokenizer.next();
      
      FSIndex qIndex = aJCas.getAnnotationIndex(Question.type);
      Iterator qIter = qIndex.iterator();
      while (qIter.hasNext()) {
        Question q = (Question) qIter.next();
        if ((w.beginPosition() >= q.getBegin()) && (w.endPosition() <= q.getEnd())) {
          Token annotation = new Token(aJCas);
          annotation.setBegin(w.beginPosition());
          annotation.setEnd(w.endPosition());
          annotation.setCasProcessorId("TokenAnnotator");
          annotation.setConfidence(1.0);
          annotation.addToIndexes();          
        }
      }

      FSIndex aIndex = aJCas.getAnnotationIndex(Answer.type);
      Iterator aIter = aIndex.iterator();
      while (aIter.hasNext()) {
        Answer a = (Answer) aIter.next();
        if ((w.beginPosition() >= a.getBegin()) && (w.endPosition() <= a.getEnd())) {
          Token annotation = new Token(aJCas);
          annotation.setBegin(w.beginPosition());
          annotation.setEnd(w.endPosition());
          annotation.setCasProcessorId("TokenAnnotator");
          annotation.setConfidence(1.0);
          annotation.addToIndexes();          
        }
      }

    }
  }
}
  
