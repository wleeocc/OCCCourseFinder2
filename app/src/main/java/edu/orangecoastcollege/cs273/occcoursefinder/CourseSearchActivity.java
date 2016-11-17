package edu.orangecoastcollege.cs273.occcoursefinder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class CourseSearchActivity extends AppCompatActivity {

    private DBHelper db;
    private List<Instructor> allInstructorsList;
    private List<Course> allCoursesList;
    private List<Offering> allOfferingsList;
    private List<Offering> filteredOfferingsList; //new stuff

    private EditText courseTitleEditText;
    private Spinner instructorSpinner;
    private ListView offeringsListView;

    private OfferingListAdapter offeringListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_search);

        deleteDatabase(DBHelper.DATABASE_NAME);
        db = new DBHelper(this);
        db.importCoursesFromCSV("courses.csv");
        db.importInstructorsFromCSV("instructors.csv");
        db.importOfferingsFromCSV("offerings.csv");

        allOfferingsList = db.getAllOfferings();
        filteredOfferingsList = new ArrayList<>(allOfferingsList);
        allInstructorsList = db.getAllInstructors();
        allCoursesList = db.getAllCourses();

        courseTitleEditText = (EditText) findViewById(R.id.courseTitleEditText);
        courseTitleEditText.addTextChangedListener(courseTitleTextWatcher); ///////// new line

        instructorSpinner = (Spinner) findViewById(R.id.instructorSpinner);
        ArrayAdapter<String> instructorSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getInstructorNames()); /////
        instructorSpinner.setAdapter(instructorSpinnerAdapter); ////////////
        instructorSpinner.setOnItemSelectedListener(instructorSpinnerListener); /////////// connecting

        offeringsListView = (ListView) findViewById(R.id.offeringsListView);
        offeringListAdapter =
                new OfferingListAdapter(this, R.layout.offering_list_item, filteredOfferingsList);
        offeringsListView.setAdapter(offeringListAdapter);


        // how to go through database // not using this for sake of speed
        /*
       Cursor instructorNamesCursor = db.getInstructorNamesCursor();
        SimpleCursorAdapter cursorAdapter =
                new SimpleCursorAdapter(this,
                        android.R.layout.simple_spinner_item,
                        instructorNamesCursor,
                        new String[] {DBHelper.FIELD_LAST_NAME},
                        new int[] {android.R.id.text1}, 0);
        cursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        instructorSpinner.setAdapter(cursorAdapter);
        */
    }

    private String[] getInstructorNames(){
        String[] instructorNames = new String[allInstructorsList.size() + 1];
        instructorNames[0] = "[Select Instructor]";
        for (int i = 1; i < instructorNames.length; i++){
            instructorNames[i] = allInstructorsList.get(i - 1).getFullName();
        }
        return instructorNames;
    }


    public TextWatcher courseTitleTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String input = charSequence.toString().toLowerCase();
            if (input.equals("")){
                // Repopulate the list adapter with all offerings
                offeringListAdapter.clear();
                for (Offering offering : allOfferingsList)
                    offeringListAdapter.add(offering); // also adds to filtered offering list
            }
            else{
                offeringListAdapter.clear();
                for (Offering offering : allOfferingsList) {
                    // If the course title starts with user input, add it to the list adapter
                    Course course = offering.getCourse();
                    //String instructorName = String.valueOf(instructorSpinner.getSelectedItem()); ///// in conjunction
                    if (course.getTitle().toLowerCase().contains(input)) // or contains
                        offeringListAdapter.add(offering);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };
    public AdapterView.OnItemSelectedListener instructorSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
            String selectedInstructorName = String.valueOf(adapterView.getItemAtPosition(position)); //////////??
            offeringListAdapter.clear();
            if (selectedInstructorName.equals("[Select Instructor]")){
                for (Offering offering : allOfferingsList){
                    offeringListAdapter.add(offering);
                }
            }
            else
            {
                for (Offering offering :allOfferingsList){
                    Instructor instructor = offering.getInstructor();
                    if (instructor.getFullName().equals(selectedInstructorName)){
                        offeringListAdapter.add(offering);
                    }
                }

            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            adapterView.setSelection(0);
        }
    };
    public void reset(View view){
        courseTitleEditText.setText("");
        instructorSpinner.setSelection(0);

    }

}
