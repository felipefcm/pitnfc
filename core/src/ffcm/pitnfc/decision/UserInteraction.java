
package ffcm.pitnfc.decision;

import com.badlogic.gdx.Gdx;

import ffcm.pitnfc.SpeechRecognitionInterface;
import ffcm.pitnfc.TTSInterface;

public class UserInteraction
{
    private DecisionNode startNode;
    private DecisionNode currentNode;

    private TTSInterface ttsInterface;
    private SpeechRecognitionInterface srInterface;

    public enum InteractionState
    {
        NotStarted,
        Asking,
        WaitingAnswer,
        Finishing
    }

    public enum Shape
    {
        Equilateral,
        Isosceles,
        Square,
        Rectangle
    }

    private QuestionNode numSides;
    private QuestionNode sidesEqual;
    private QuestionNode sidesLen;

    private ObjectiveNode triEq;
    private ObjectiveNode triIso;
    private ObjectiveNode quad;
    private ObjectiveNode rect;

    private Answer threeAns;
    private Answer threeAltAns;
    private Answer fourAns;
    private Answer fourAltAns;

    private Answer sidesEqualYesAns;
    private Answer sidesEqualNoAns;

    private Answer sidesLenYesAns;
    private Answer sidesLenNoAns;

    private final String[] possibleAnswers =
    {
        "Três", "Quatro", "Sim", "Não", "3", "4"
    };

    private InteractionState state;

    public UserInteraction(TTSInterface ttsInterface, SpeechRecognitionInterface srInterface)
    {
        startNode = null;
        currentNode = null;

        this.ttsInterface = ttsInterface;
        this.srInterface = srInterface;

        state = InteractionState.NotStarted;
    }

    public void Init()
    {
        numSides = new QuestionNode("Quantos lados a figura tem?");
        sidesEqual = new QuestionNode("Determine se os três lados são iguais");
        sidesLen = new QuestionNode("Determine se lados possuem o mesmo comprimento");

        triEq = new ObjectiveNode("Isto é um triângulo equilátero. Os três lados possuem o mesmo comprimento");
        triIso = new ObjectiveNode("Isto é um triângulo isósceles. De seus 3 lados, dois possuem o mesmo comprimento");
        quad = new ObjectiveNode("Isto é um quadrado. Seus quatro lados são iguais");
        rect = new ObjectiveNode("Isto é um retângulo: lados opostos iguais e paralelos");

        threeAns = new Answer("Três", sidesEqual);
        threeAltAns = new Answer("3", sidesEqual);
        fourAns = new Answer("Quatro", sidesLen);
        fourAltAns = new Answer("4", sidesLen);

        sidesEqualYesAns = new Answer("Sim", triEq);
        sidesEqualNoAns = new Answer("Não", triIso);

        sidesLenYesAns = new Answer("Sim", quad);
        sidesLenNoAns = new Answer("Não", rect);

        numSides.AddAnswer(threeAns);
        numSides.AddAnswer(threeAltAns);
        numSides.AddAnswer(fourAns);
        numSides.AddAnswer(fourAltAns);

        sidesEqual.AddAnswer(sidesEqualYesAns);
        sidesEqual.AddAnswer(sidesEqualNoAns);

        sidesLen.AddAnswer(sidesLenYesAns);
        sidesLen.AddAnswer(sidesLenNoAns);

        startNode = numSides;
        currentNode = startNode;
    }

    public void ReceivedSpeech(String[] text)
    {
        if(state != InteractionState.WaitingAnswer)
        {
            Gdx.app.log("PIT_NFC", "Skipping ReceivedSpeech because not WaitingAnswer");
            return;
        }

        String answer = null;

        for(int i = 0; i < text.length; ++i)
        {
            if(IsPossibleAnswer(text[i]))
                answer = text[i];
        }

        if(answer == null)
        {
            //not among the possible answers, ask to repeat
            ttsInterface.SpeakBlocker("Por favor repita sua resposta");
            srInterface.StartListening();

            return;
        }

        //it is a possible answer, check if it is correct

        QuestionNode question = (QuestionNode) currentNode;

        Answer questionAnswer = question.GetAnswer(answer);

        if(questionAnswer == null)
        {
            //not a valid answer for the question
            ttsInterface.SpeakBlocker("Resposta incorreta, pense mais e responda novamente");
            srInterface.StartListening();

            return;
        }

        if(!questionAnswer.correct)
        {
            ttsInterface.SpeakBlocker("Resposta incorreta, pense mais e responda novamente");
            srInterface.StartListening();

            return;
        }

        DecisionNode nextNode = questionAnswer.node;

        if(nextNode instanceof ObjectiveNode)
        {
            //final node, congratulate and restart
            ObjectiveNode objNode = (ObjectiveNode) nextNode;

            state = InteractionState.Finishing;
            ttsInterface.SpeakBlocker(objNode.text);
            state = InteractionState.NotStarted;
        }
        else
        {
            //another question
            ttsInterface.SpeakBlocker("Resposta correta");

            currentNode = nextNode;

            state = InteractionState.Asking;
            ttsInterface.SpeakBlocker(((QuestionNode) currentNode).question);

            state = InteractionState.WaitingAnswer;
            srInterface.StartListening();
        }
    }

    public void ReceivedTag(String tagText)
    {
        if(state != InteractionState.NotStarted)
            Gdx.app.log("PIT_NFC", "ReceivedTag while not in NotStarted state, resetting");

        state = InteractionState.NotStarted;

        if(tagText.equalsIgnoreCase("equilatero"))
        {
            //currentShape = Shape.Equilateral;

            threeAns.correct = true;
            threeAltAns.correct = true;
            fourAns.correct = false;
            fourAltAns.correct = false;

            sidesEqualYesAns.correct = true;
            sidesEqualNoAns.correct = false;
        }
        else if(tagText.equalsIgnoreCase("isosceles"))
        {
            //currentShape = Shape.Isosceles;

            threeAns.correct = true;
            threeAltAns.correct = true;
            fourAns.correct = false;
            fourAltAns.correct = false;

            sidesEqualYesAns.correct = false;
            sidesEqualNoAns.correct = true;
        }
        else if(tagText.equalsIgnoreCase("quadrado"))
        {
            //currentShape = Shape.Square;

            threeAns.correct = false;
            threeAltAns.correct = false;
            fourAns.correct = true;
            fourAltAns.correct = true;

            sidesLenYesAns.correct = true;
            sidesLenNoAns.correct = false;
        }
        else if(tagText.equalsIgnoreCase("retangulo"))
        {
            //currentShape = Shape.Rectangle;

            threeAns.correct = false;
            threeAltAns.correct = false;
            fourAns.correct = true;
            fourAltAns.correct = true;

            sidesLenYesAns.correct = false;
            sidesLenNoAns.correct = true;
        }
        else
        {
            ttsInterface.Speak("Etiqueta não reconhecida");
            return;
        }

        StartInteraction();
    }

    private void StartInteraction()
    {
        srInterface.StopListening(); //just to be sure
        currentNode = startNode;

        state = InteractionState.Asking;
        ttsInterface.SpeakBlocker(((QuestionNode) startNode).question);

        state = InteractionState.WaitingAnswer;
        srInterface.StartListening();
    }

    private boolean IsPossibleAnswer(String answer)
    {
        for(int i = 0; i < possibleAnswers.length; ++i)
            if(possibleAnswers[i].equalsIgnoreCase(answer))
                return true;

        return false;
    }

    public InteractionState GetState()
    {
        return state;
    }
}
