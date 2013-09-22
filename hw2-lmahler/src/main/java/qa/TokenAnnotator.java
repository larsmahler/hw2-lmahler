package qa;

/*
 * Put notes here.
 */

import java.io.StringReader;
import java.util.Iterator;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.tutorial.RoomNumber;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.process.PTBTokenizer.PTBTokenizerFactory;
import edu.stanford.nlp.process.Tokenizer;
import edu.cmu.deiis.types.*;

/**
 * The TokenAnnotator annotates each token within each sentence (Question or Answer). 
 */
public class TokenAnnotator extends JCasAnnotator_ImplBase {

  /**
   * @see JCasAnnotator_ImplBase#process(JCas)
   */
  public void process(JCas aJCas) {
    // get document text
    FSIndex qIndex = aJCas.getAnnotationIndex(Question.type);
    Iterator qIter = qIndex.iterator();
    while (qIter.hasNext()) {
      Question question = (Question) qIter.next();
      TokenizerFactory<Word> factory = PTBTokenizerFactory.newTokenizerFactory();
      Tokenizer<Word> tokenizer = factory.getTokenizer(new StringReader(question.getCoveredText()));
      String[tokens] = tokenizer.tokenize();
    }
    String docText = aJCas.getDocumentText();
    String[] lines = docText.split(System.getProperty("line.separator"));
    for( int i = 0; i < lines.length; i++)
    {
        String[] components = lines[i].split(" +");  //Use " +" to split one OR MORE spaces
        int offset = docText.indexOf(lines[i]);
        if (components[0].equals("Q")) {
          Question annotation = new Question(aJCas);
          // Set begin / end of annotation. For Question types, the span starts
          // with the third element of the input file
          annotation.setBegin(lines[i].indexOf(components[1]));
          annotation.setEnd(lines[i].length());
          annotation.setCasProcessorId("TestElementAnnotator");
          annotation.setConfidence(1.0);
          // Add annotation to JCas index
          annotation.addToIndexes();
        } else {
          Answer annotation = new Answer(aJCas);
          // Set begin / end of annotation. For Answer types, the span starts
          // with the third element of the input file
          annotation.setBegin(offset + lines[i].indexOf(components[2]));
          annotation.setEnd(offset + lines[i].length());
          annotation.setCasProcessorId("TestElementAnnotator");
          annotation.setConfidence(1.0);
          // For Answer types, the isCorrect value is found in the second field 
          // of the input file (either 0 or 1)
          if (components[1].equals("1")) {
            annotation.setIsCorrect(true);
          } else {
            annotation.setIsCorrect(false);
          }
          // Add annotation to JCas index
          annotation.addToIndexes();          
        }
    }    
  }

}
