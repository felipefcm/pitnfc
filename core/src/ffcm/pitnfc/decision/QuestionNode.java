
package ffcm.pitnfc.decision;

import java.util.LinkedList;

public class QuestionNode extends DecisionNode
{
    public String question;

    public LinkedList<Answer> answers;

    public QuestionNode(String questionText)
    {
        answers = new LinkedList<Answer>();
        question = questionText;
    }

    public void AddAnswer(Answer answer)
    {
        answers.add(answer);
    }

    public Answer GetAnswer(String answerText)
    {
        for(Answer ans : answers)
            if(ans.text.equalsIgnoreCase(answerText))
                return ans;

        return null;
    }
}
