package org.sairaa.scholarquiz.ui.Moderator.Question;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.sairaa.scholarquiz.AppInfo;
import org.sairaa.scholarquiz.R;
import org.sairaa.scholarquiz.SharedPreferenceConfig;
import org.sairaa.scholarquiz.model.LessonQuizModel;
import org.sairaa.scholarquiz.model.QuestionAnswerModel;
import org.sairaa.scholarquiz.model.QuizModel;
import org.sairaa.scholarquiz.ui.Moderator.QuizModeratorActivity;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class QuestionListActivity extends AppCompatActivity {


    private Button addNewQuestion, publish;
    private ArrayList<QuizModel> questionListModels;
    private String quizId;
    private ModeratorQuestionListAdapter adapter;
    private int questionNo = 0;

    private SharedPreferenceConfig sharedPreferenceConfig;

    AlertDialog.Builder alertBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);

        Intent intent = getIntent();
        // the read write option to check whether the buttons AddNewQuiz and Publish
        // to remain active or not
        // 0 to write operation
        int readWrite = intent.getIntExtra("readWrite",0);
        final String channelId = intent.getStringExtra("channelId");
        quizId = intent.getStringExtra("quizId");
        final String quizName = intent.getStringExtra("quizName");
        Toast.makeText(QuestionListActivity.this,"channel Id "+channelId+"quiz id : "+quizId+" quiz name : "+quizName,Toast.LENGTH_SHORT).show();

        sharedPreferenceConfig = new SharedPreferenceConfig(getApplicationContext());


        addNewQuestion = findViewById(R.id.mod_question_list_add_new_question);
        publish = findViewById(R.id.mod_quiz_publish);

        if(readWrite == 200){
            // inactive the buttons
            // no edit or not allowed to add new question to published quiz
            addNewQuestion.setVisibility(View.INVISIBLE);
            publish.setVisibility(View.INVISIBLE);
        }else{
            addNewQuestion.setVisibility(View.VISIBLE);
            publish.setVisibility(View.VISIBLE);
        }


        // setting adapter
        final ListView questionListView = (ListView)findViewById(R.id.mod_quiz_question_listview);
        questionListModels = new ArrayList<>();

//        questionListModels.add(new QuizModel(1,"How are you ?","Fine","Well","Good","Very Fine",1));
//        questionListModels.add(new QuizModel(2,"How are you ?","Fine","Well","Good","Very Fine",1));

        // retriveing question from quiz database structure and add it to List view
//        adapter.clear();
//        addQuestionToList();
        adapter = new ModeratorQuestionListAdapter(QuestionListActivity.this,questionListModels);
        questionListView.setAdapter(adapter);

        questionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Check readWrite or Only Read
                if(addNewQuestion.getVisibility() == View.VISIBLE){
                    // Moderator can edit the question
                    QuizModel questionAnswer = (QuizModel)questionListView.getItemAtPosition(position);
                    Intent intent = new Intent(QuestionListActivity.this,QuestionAddActivity.class);
                    intent.putExtra("edit",111);
                    intent.putExtra("questionNo",questionAnswer.getQuestionNo());
                    intent.putExtra("question",questionAnswer.getQuestion());
                    intent.putExtra("option1",questionAnswer.getOption1());
                    intent.putExtra("option2",questionAnswer.getOption2());
                    intent.putExtra("option3",questionAnswer.getOption3());
                    intent.putExtra("option4",questionAnswer.getOption4());
                    intent.putExtra("answerOption",questionAnswer.getAnswerOption());
                    intent.putExtra("quizId",quizId);
                    startActivity(intent);
                }else{
                    Toast.makeText(QuestionListActivity.this,"Already Published Can't edited",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //
        addNewQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QuestionListActivity.this,QuestionAddActivity.class);
//                intent.putExtra("channelId",channelId);
                intent.putExtra("edit",222);
                intent.putExtra("quizId",quizId);
//                intent.putExtra("quizName",quizName);
                intent.putExtra("questionNo",questionNo+1);
//                Toast.makeText(QuestionListActivity.this," llll"+questionNo,Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });

        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!questionListModels.isEmpty()){
                    alertBuilder = new AlertDialog.Builder(QuestionListActivity.this);
                    alertBuilder.setTitle("Publishing Quiz");
                    alertBuilder.setMessage("Do you really want to bublish the quiz for now");
                    alertBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                        Toast.makeText(QuestionListActivity.this," Yes"+channelId+":"+quizId+" : "+quizName+" : "+user.getUid().toString(),Toast.LENGTH_SHORT).show();
                            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                            AppInfo.databaseReference.child("ChannelQuiz")
                                    .child(channelId)
                                    .child(quizId)
                                    .setValue(new LessonQuizModel(quizName,currentDateTimeString))
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(QuestionListActivity.this,"Quiz Published Succesfully",Toast.LENGTH_SHORT).show();
                                                adapter.clear();
                                                sharedPreferenceConfig.writePublishedOrNot(true);
                                                sharedPreferenceConfig.writeNewQuizName(null);
                                                finish();
                                            }
                                            else {
                                                Toast.makeText(QuestionListActivity.this,"Quiz Not Published",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            dialogInterface.dismiss();
                        }
                    });
                    alertBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(QuestionListActivity.this," NO",Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertBuilder.create();
                    alertDialog.show();
                }else {
                    Toast.makeText(QuestionListActivity.this," No Question Added. Can not be published",Toast.LENGTH_SHORT).show();
                }


            }
        });




    }

    @Override
    protected void onPause() {
        super.onPause();
        adapter.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.clear();
        addQuestionToList();
        adapter.notifyDataSetChanged();
        Toast.makeText(QuestionListActivity.this,"onResume question",Toast.LENGTH_SHORT).show();
    }

    private void addQuestionToList() {
        AppInfo.databaseReference.child("Quiz").child(quizId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot questionListSnapshot : dataSnapshot.getChildren()) {
                    QuizModel qModel = questionListSnapshot.getValue(QuizModel.class);
                    qModel.questionNo = Integer.parseInt(questionListSnapshot.getKey().toString());
                    //question number is global and passed to the questionAddActivity to know the question no
                    questionNo = qModel.questionNo;
//                    Toast.makeText(QuestionListActivity.this,"quiz "+qModel.getQuestionNo()+qModel.getQuestion(),Toast.LENGTH_SHORT).show();
                    questionListModels.add(qModel);
                    adapter.notifyDataSetChanged();

//                    QuestionAnswerModel qModel = questionListSnapshot.getValue(QuestionAnswerModel.class);
//                    Toast.makeText(QuestionListActivity.this,"quiz "+qModel.getQuestion(),Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
