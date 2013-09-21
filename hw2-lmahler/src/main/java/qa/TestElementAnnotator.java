package qa;

/*
 * Put notes here.
 */

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;

import edu.cmu.deiis.types.Question;
import edu.cmu.deiis.types.Answer;

/**
 * The TestElementAnnotator annotates each input document. 
 * For each line of the the input file, it determines if that line is a Question, or an Answer, 
 * and annotates the span accordingly. 
 * For each Answer annotation, the TestElementAnnotator will set its isCorrect feature to yes / no (1/0)
 * depending on whether it is a correct answer or not.
 */
public class TestElementAnnotator extends JCasAnnotator_ImplBase {

  /**
   * @see JCasAnnotator_ImplBase#process(JCas)
   */
  public void process(JCas aJCas) {
    // get document text
    String docText = aJCas.getDocumentText();
    String[] lines = docText.split(System.getProperty("line.separator"));
    for( int i = 0; i < lines.length - 1; i++)
    {
        String[] components = lines[i].split(" ");
        if (components[0]=="Q") {
          Question annotation = new Question(aJCas);
          // Set begin / end of annotation. For Question types, the span starts
          // with the third element of the input file
          annotation.setBegin(lines[i].indexOf(components[1]));
          annotation.setEnd(lines[i].length());
          // Add annotation to JCas index
          annotation.addToIndexes();
        } else {
          Answer annotation = new Answer(aJCas);
          // Set begin / end of annotation. For Answer types, the span starts
          // with the third element of the input file
          annotation.setBegin(lines[i].indexOf(components[2]));
          annotation.setEnd(lines[i].length());
          // For Answer types, the isCorrect value is found in the second field 
          // of the input file (either 0 or 1)
          if (components[1]=="1") {
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
