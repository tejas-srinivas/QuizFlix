package com.example.quizflix;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class QuizResult extends AppCompatActivity {

    private List<QuestionsList> questionsList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        final TextView scoreTV = findViewById(R.id.scoreTV);
        final TextView totalScoreTV = findViewById(R.id.totalScoreTV);
        final TextView correctTV = findViewById(R.id.correctTV);
        final TextView incorrectTV = findViewById(R.id.incorrectTV);
        final AppCompatButton shareBtn = findViewById(R.id.shareBtn);
        final AppCompatButton reTakeQuizBtn = findViewById(R.id.reTakeQuizBtn);

        //retrieving questions from MainActivity
        questionsList = (List<QuestionsList>) getIntent().getSerializableExtra("questions");

        totalScoreTV.setText("/"+questionsList.size());
        scoreTV.setText(getCorrectAnswers() + "");
        correctTV.setText(getCorrectAnswers() + "");
        incorrectTV.setText(String.valueOf(questionsList.size()-getCorrectAnswers()));

        //Share Result
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "QuizFlix");
                sendIntent.putExtra(Intent.EXTRA_TEXT,"My score ="+scoreTV.getText());

                Intent shareIntent = Intent.createChooser(sendIntent,"Share Via");
                startActivity(shareIntent);
            }
        });

        reTakeQuizBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Retake Quiz
                startActivity(new Intent(QuizResult.this,MainActivity.class));
                finish();
            }
        });

    }

    private int getCorrectAnswers() {
        int correctAnswer = 0;
        for(int i=0;i<questionsList.size();i++){
            int getUserSelectedOption = questionsList.get(i).getUserSelectedAnswer();
            int getQuestionAnswer = questionsList.get(i).getAnswer();

            //check if user selected answer matches the correct answer
            if(getUserSelectedOption == getQuestionAnswer){
                correctAnswer++;
            }
        }
        return correctAnswer;
    }
}