
package ffcm.pitnfc.decision;

public class Answer
{
    public String text;
    public DecisionNode node;
    public boolean correct;

    public Answer(String answerText, DecisionNode node)
    {
        this.node = node;
        this.text = answerText;
        correct = false;
    }
}
