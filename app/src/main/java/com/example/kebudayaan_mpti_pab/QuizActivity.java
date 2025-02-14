package com.example.kebudayaan_mpti_pab;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PersistableBundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "extraScore";

    private static final long COUNTDOWN_IN_MILLIS = 30000;

    private static final String KEY_SCORE ="keyScore";
    private static final String KEY_QUESTION_COUNT = "keyQuestionCount";
    private static final String KEY_MILLIS_LEFT = "keyMillisLeft";
    private static final String KEY_ANSWERED = "keyAnswered";
    private static final String KEY_QUESTION_LIST = "keyQuestionList";

    private TextView textViewQuestion;
    private TextView textViewScore;
    private TextView textViewQuestionCount;
    private TextView textViewCountDown;
    private TextView textViewDifficulty;
    private RadioGroup rbGroup;
    private RadioButton rb1;
    private RadioButton rb2;
    private RadioButton rb3;
    private Button buttonConfirmNext;

    private ColorStateList textColorDefaultRb;
    private ColorStateList textColorDefaultCd;


    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    private ArrayList<Question> questionList;
    private int questionCounter;
    private int questionCountTotal;
    private Question currentQuestion;
    private int score;
    private boolean answered;

    private long backPressedTime;
    public static String category;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        textViewQuestion = findViewById(R.id.text_view_question);
        textViewScore = findViewById(R.id.text_view_score);
        textViewQuestionCount = findViewById(R.id.text_view_question_count);
        textViewCountDown = findViewById(R.id.text_view_countdown);
        textViewDifficulty = findViewById(R.id.text_view_difficulty);
        rbGroup = findViewById(R.id.radio_group);
        rb1 = findViewById(R.id.radio_button1);
        rb2 = findViewById(R.id.radio_button2);
        rb3 = findViewById(R.id.radio_button3);
        buttonConfirmNext = findViewById(R.id.button_confirm_next);

        textColorDefaultRb = rb1.getTextColors();
        textColorDefaultCd = textViewCountDown.getTextColors();

        Intent intent = getIntent();
        category = intent.getStringExtra(StartingScreenActivity.EXTRA_CATEGORY);

        textViewDifficulty.setText("Kategori: "+ category);

        if (savedInstanceState == null){
            QuizDbHelper dbHelper = new QuizDbHelper(this);
            questionList = dbHelper.getQuestions(category);
            questionCountTotal = questionList.size();
            Collections.shuffle(questionList);

            showNextQuestion();
        } else {
            questionList = savedInstanceState.getParcelableArrayList(KEY_QUESTION_LIST);
            questionCountTotal = questionList.size();
            questionCounter = savedInstanceState.getInt(KEY_QUESTION_COUNT);
            currentQuestion = questionList.get(questionCounter - 1);
            score = savedInstanceState.getInt(KEY_SCORE);
            timeLeftInMillis = savedInstanceState.getLong(KEY_MILLIS_LEFT);
            answered = savedInstanceState.getBoolean(KEY_ANSWERED);
            if (!answered) {
                startCountDown();
            } else {
                updateCountDownText();
                showSolution();
            }
        }



        buttonConfirmNext.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(!answered){
                    if(rb1.isChecked()|| rb2.isChecked() || rb3.isChecked()) {
                        checkAnswer();
                    } else {
                        Toast.makeText(QuizActivity.this, "Pilih salah satu jawaban", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showNextQuestion();
                }
            }
        });
    }

    private void showNextQuestion(){
        rb1.setTextColor(textColorDefaultRb);
        rb2.setTextColor(textColorDefaultRb);
        rb3.setTextColor(textColorDefaultRb);
        rbGroup.clearCheck();

        if (questionCounter < questionCountTotal) {
            currentQuestion = questionList.get(questionCounter);
            textViewQuestion.setText(currentQuestion.getQuestion());
            rb1.setText(currentQuestion.getOption1());
            rb2.setText(currentQuestion.getOption2());
            rb3.setText(currentQuestion.getOption3());
            questionCounter++;
            textViewQuestionCount.setText("Pertanyaan: " + questionCounter + "/" + questionCountTotal);
            answered = false;
            buttonConfirmNext.setText("Jawab");

            timeLeftInMillis = COUNTDOWN_IN_MILLIS;
            startCountDown();
        } else {
            finishQuiz();
        }
    }

    private void startCountDown(){
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {

                timeLeftInMillis = 0;
                updateCountDownText();
                checkAnswer();

            }
        }.start();
    }
    private void updateCountDownText(){
        int minutes = (int)(timeLeftInMillis/1000)/60;
        int seconds = (int)(timeLeftInMillis/1000)%60;

        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes,seconds);

        textViewCountDown.setText(timeFormatted);

        if(timeLeftInMillis<10000){
            textViewCountDown.setTextColor(Color.RED);
        } else{
            textViewCountDown.setTextColor(textColorDefaultCd);
        }
    }

    private void checkAnswer() {
        answered = true;

        countDownTimer.cancel();

        RadioButton rbSelected = findViewById(rbGroup.getCheckedRadioButtonId());
        int answerNr = rbGroup.indexOfChild(rbSelected) + 1;
        if (answerNr == currentQuestion.getAnswerNr()) {
            score++;
            textViewScore.setText("Nilai: " + score);
        }
        showSolution();
    }
    private void showSolution() {
        rb1.setTextColor(Color.RED);
        rb2.setTextColor(Color.RED);
        rb3.setTextColor(Color.RED);
        switch (currentQuestion.getAnswerNr()) {
            case 1:
                rb1.setTextColor(Color.GREEN);
                textViewQuestion.setText("Opsi jawaban 1 benar");
                break;
            case 2:
                rb2.setTextColor(Color.GREEN);
                textViewQuestion.setText("Opsi jawaban 2 benar");
                break;
            case 3:
                rb3.setTextColor(Color.GREEN);
                textViewQuestion.setText("Opsi jawaban 3 benar");
                break;
        }
        if (questionCounter < questionCountTotal) {
            buttonConfirmNext.setText("Lanjut");
        } else {
            buttonConfirmNext.setText("Selesai");
        }
    }
    private void finishQuiz() {
        final Context context = this;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View formElementsView = inflater.inflate(R.layout.data_input_form, null, false);

        final EditText edtName = (EditText) formElementsView.findViewById(R.id.edtName);
        final EditText edtKategori = (EditText) formElementsView.findViewById(R.id.edtKategori);
        final EditText edtNilai = (EditText) formElementsView.findViewById(R.id.edtNilai);
        edtKategori.setText(category);
        edtNilai.setText(String.valueOf(score));


        new AlertDialog.Builder(context)
                .setView(formElementsView)
                .setTitle("Tambah Data")
                .setPositiveButton("Tambah", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        String name = edtName.getText().toString();


                        CatatanData catatanData = new CatatanData();
                        catatanData.name = name;
                        catatanData.kategori = category;
                        catatanData.nilai = String.valueOf(score) ;


                        boolean createSuccessful = new TableControllerCatatan(context).create(catatanData);

                        if(createSuccessful){
                            Toast.makeText(context, "Data tersimpan", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(context, "Tidak dapat menyimpan data", Toast.LENGTH_SHORT).show();
                        }
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(EXTRA_SCORE, score);
                        setResult(RESULT_OK, resultIntent );

                        finish();


                        //dialog.cancel(); //keknya bgaina ini harus d ubah

                    }
                }).show();

//        finish();
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime+2000 > System.currentTimeMillis()){
            finishQuiz();
        } else{
            Toast.makeText(this, "Tekan sekali lagi untuk mengakhiri kuis", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(countDownTimer!= null){
            countDownTimer.cancel();

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SCORE, score);
        outState.putInt(KEY_QUESTION_COUNT, questionCounter);
        outState.putLong(KEY_MILLIS_LEFT, timeLeftInMillis);
        outState.putBoolean(KEY_ANSWERED, answered);
        outState.putParcelableArrayList(KEY_QUESTION_LIST, questionList);
    }
}