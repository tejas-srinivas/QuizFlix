package com.example.quizflix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private final List<QuestionsList> questionsLists = new ArrayList<>();
    private TextView quizTimer;
    private RelativeLayout option1Layout,option2Layout,option3Layout,option4Layout;
    private TextView option1TV,option2TV,option3TV,option4TV;
    private ImageView option1Icon,option2Icon,option3Icon,option4Icon;
    private TextView totalQuestionTV;
    private TextView currentQuestion;

    private TextView questionTV;
    private final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://quizflix-61e2f-default-rtdb.firebaseio.com/");
    //countdown timer
    private CountDownTimer countDownTimer;



    //current question position. By default 0 = first-question
    private int currentQuestionPosition = 0;

    //selected option number - value must be between 1-4
    private int selectedOption = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        quizTimer = findViewById(R.id.quizTimer);
        option1Layout = findViewById(R.id.option1Layout);
        option2Layout = findViewById(R.id.option2Layout);
        option3Layout = findViewById(R.id.option3Layout);
        option4Layout = findViewById(R.id.option4Layout);

        option1TV = findViewById(R.id.option1TV);
        option2TV = findViewById(R.id.option2TV);
        option3TV = findViewById(R.id.option3TV);
        option4TV = findViewById(R.id.option4TV);

        option1Icon = findViewById(R.id.option1Icon);
        option2Icon = findViewById(R.id.option2Icon);
        option3Icon = findViewById(R.id.option3Icon);
        option4Icon = findViewById(R.id.option4Icon);

        questionTV = findViewById(R.id.questionTV);
        totalQuestionTV = findViewById(R.id.totalQuestionTV);
        currentQuestion = findViewById(R.id.currentQuestionTV);



        // instructions for Quiz
        InstructionsDialog instructionsDialog = new InstructionsDialog(MainActivity.this);
        instructionsDialog.setCancelable(false);
        instructionsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        instructionsDialog.show();

        final AppCompatButton nextBtn = findViewById(R.id.nextQuestionBtn);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final int getQuizTime = Integer.parseInt(snapshot.child("time").getValue(String.class));
                for(DataSnapshot questions : snapshot.child("questions").getChildren()){
                    String getQuestion = questions.child("question").getValue(String.class);
                    String getOption1 = questions.child("option1").getValue(String.class);
                    String getOption2 = questions.child("option2").getValue(String.class);
                    String getOption3 = questions.child("option3").getValue(String.class);
                    String getOption4 = questions.child("option4").getValue(String.class);
                    int getanswer = Integer.parseInt(questions.child("answer").getValue(String.class));

                    QuestionsList questionsList = new QuestionsList(getQuestion,getOption1,getOption2,getOption3,getOption4,getanswer);
                    questionsLists.add(questionsList);
                }
                //total questions to textview
                totalQuestionTV.setText("/"+questionsLists.size());

                //start timer
                startQuizTimer(getQuizTime);

                //start with first question
                selectQuestion(currentQuestionPosition);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,"Failed to get data from Firebase",Toast.LENGTH_SHORT).show();
            }
        });

        option1Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedOption = 1;

                selectOption(option1Layout,option1Icon);
            }
        });

        option2Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedOption = 2;

                selectOption(option2Layout,option2Icon);

            }
        });

        option3Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedOption = 3;

                selectOption(option3Layout,option3Icon);

            }
        });

        option4Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedOption = 4;

                selectOption(option4Layout,option4Icon);

            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check if user has selected an option or not
                if(selectedOption !=0){
                    // set the user selected option
                    questionsLists.get(currentQuestionPosition).setUserSelectedAnswer(selectedOption);

                    // reset selected option to 0
                    selectedOption = 0;
                    currentQuestionPosition++; //increase current question postion ,getting next question

                    if(currentQuestionPosition < questionsLists.size()){
                        selectQuestion(currentQuestionPosition); // select next question
                    }
                    else{

                        // list has no questions
                        countDownTimer.cancel(); //stop timer
                        finishQuiz();
                    }

                }
                else{
                    Toast.makeText(MainActivity.this,"Please select an option",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void finishQuiz(){

        //create Result activity
        Intent intent = new Intent(MainActivity.this,QuizResult.class);

        //create bundle to pass quizQuestionsList
        Bundle bundle = new Bundle();
        bundle.putSerializable("questions",(Serializable) questionsLists);

        intent.putExtras(bundle);
        startActivity(intent);

        finish();
    }

    private void startQuizTimer(int maxTimeInSeconds){

        countDownTimer = new CountDownTimer(maxTimeInSeconds * 1000,1000) {
            @Override
            public void onTick(long millsUntilFinished) {
                long getHour = TimeUnit.MILLISECONDS.toHours(millsUntilFinished);
                long getMinute = TimeUnit.MILLISECONDS.toMinutes(millsUntilFinished);
                long getSecond = TimeUnit.MILLISECONDS.toSeconds(millsUntilFinished);

                String generateTime = String.format(Locale.getDefault(),"%02d:%02d:%02d",getHour,
                        getMinute-TimeUnit.HOURS.toMinutes(getHour),
                        getSecond-TimeUnit.MINUTES.toSeconds(getMinute));

                quizTimer.setText(generateTime);
            }

            @Override
            public void onFinish() {
                finishQuiz();
            }
        };

        countDownTimer.start();
    }

    private void selectQuestion(int questionListPosition){

        //reset options for new question
        resetOptions();
        //getting the questions with details in the frontend
        questionTV.setText(questionsLists.get(questionListPosition).getQuestion());
        option1TV.setText(questionsLists.get(questionListPosition).getOption1());
        option2TV.setText(questionsLists.get(questionListPosition).getOption2());
        option3TV.setText(questionsLists.get(questionListPosition).getOption3());
        option4TV.setText(questionsLists.get(questionListPosition).getOption4());
        currentQuestion.setText("Question"+(questionListPosition+1));

    }

    private void resetOptions() {

        option1Layout.setBackgroundResource(R.drawable.round_back_white50_10);
        option2Layout.setBackgroundResource(R.drawable.round_back_white50_10);
        option3Layout.setBackgroundResource(R.drawable.round_back_white50_10);
        option4Layout.setBackgroundResource(R.drawable.round_back_white50_10);

        option1Icon.setImageResource(R.drawable.round_back_white50_100);
        option2Icon.setImageResource(R.drawable.round_back_white50_100);
        option3Icon.setImageResource(R.drawable.round_back_white50_100);
        option4Icon.setImageResource(R.drawable.round_back_white50_100);

    }

    private void selectOption(RelativeLayout selectedOptionLayout,ImageView selectedOptionIcon){
        // reset options to select now
        resetOptions();
        selectedOptionIcon.setImageResource(R.drawable.checked);
        selectedOptionLayout.setBackgroundResource(R.drawable.round_back_selected_option);
    }
}