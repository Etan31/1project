package com.example.a1project;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class EditSchedule extends AppCompatActivity implements AdapterView.OnItemSelectedListener,DeleteDialogFragment.DeleteDialogListener {
    //      TODO: Fix the bug of the BackPressed, it should be redirected to SettingsFragment instead of Schedulefragment
    //      TODO: uses unchecked or unsafe operations.

    private Spinner garbageTypeSpinner;
    private Spinner repeatTimeSpinner;

    private DataSnapshot scheduleSnapshot;

    private TableLayout dataTableLayout;
    private LinearLayout linearLayoutInputs;
    Button backBtn2;

    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_schedule);


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        linearLayoutInputs = findViewById(R.id.linearLayout_inputs);

        backBtn2 = findViewById(R.id.backBtn2);
        backBtn2.setOnClickListener(v -> BackPressed());


        Button deleteButton = findViewById(R.id.btn_delete);

        initAndPopulateSpinners();

        garbageTypeSpinner = findViewById(R.id.spinner_typeofgarbage);
        repeatTimeSpinner = findViewById(R.id.spinner_doesNotRepeat);


        LinearLayout linearLayoutButtons = findViewById(R.id.linearLayout_buttons);
        Button updateButton = linearLayoutButtons.findViewById(R.id.btn_update);

        garbageTypeSpinner = findViewById(R.id.spinner_typeofgarbage);
        repeatTimeSpinner = findViewById(R.id.spinner_doesNotRepeat);

        TextInputLayout dateInputLayout = findViewById(R.id.layout_addSched_date);
        TextInputLayout addressInputLayout = findViewById(R.id.layout_addSched_address);

// Initialize views in the onCreate method
        AutoCompleteTextView dateAutoCompleteTextView = dateInputLayout.findViewById(R.id.addSched_date);
        AutoCompleteTextView addressAutoCompleteTextView = addressInputLayout.findViewById(R.id.addSched_address);

        garbageTypeSpinner = findViewById(R.id.spinner_typeofgarbage);
        repeatTimeSpinner = findViewById(R.id.spinner_doesNotRepeat);




        // Set up the OnClickListener for the Update Button
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the updated values from input fields and spinners
                String updatedDate = dateAutoCompleteTextView.getText().toString();
                String updatedAddress = addressAutoCompleteTextView.getText().toString();
                String updatedGarbageType = garbageTypeSpinner.getSelectedItem().toString();
                String updatedRepeatType = repeatTimeSpinner.getSelectedItem().toString();

                // Update the data in the Firebase database
                // Use the unique key of the clicked row to identify and update the data
                DatabaseReference updateRef = FirebaseDatabase.getInstance().getReference("schedules")
                        .child(Objects.requireNonNull(scheduleSnapshot.getKey()));

                updateRef.child("date").setValue(updatedDate);
                updateRef.child("address").setValue(updatedAddress);
                updateRef.child("garbageType").setValue(updatedGarbageType);
                updateRef.child("repeatType").setValue(updatedRepeatType);

                // Display a toast message for successful update
                showToast("Row updated successfully");
                linearLayoutInputs.setVisibility(View.GONE);
            }
        });



        calendar = Calendar.getInstance();
        dateAutoCompleteTextView.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year1, month1, day1) -> {
                // Display the selected date in the format mm/dd/yyyy
                String selectedDate = String.format(Locale.US, "%02d/%02d/%d", month1 + 1, day1, year1);
                dateAutoCompleteTextView.setText(selectedDate);
            }, year, month, day);

            datePicker.show();
            Log.d("DatePicker", "Date picker dialog opened.");
        });


//       spinner for the scheduled places
        Spinner dropDownSpinnerForLocation = findViewById(R.id.DropDown_spinner_for_location);
        dropDownSpinnerForLocation.setOnItemSelectedListener(this);
        fetchFirebaseDataAndPopulateSpinner(dropDownSpinnerForLocation);

        updateTableWithFilteredData((String) dropDownSpinnerForLocation.getSelectedItem());


//        for the displaying of the schedule to the table
        dataTableLayout = findViewById(R.id.data_table_layout);
        DatabaseReference schedulesRef = FirebaseDatabase.getInstance().getReference("schedules");

        schedulesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Clear existing data rows
                dataTableLayout.removeAllViews();

                // Get the selected address from the spinner
                String selectedAddress = (String) dropDownSpinnerForLocation.getSelectedItem();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String date = snapshot.child("date").getValue(String.class);
                    String garbageType = snapshot.child("garbageType").getValue(String.class);
                    String repeatType = snapshot.child("repeatType").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);

                    // Check if the schedule's address matches the selected address
                    if (selectedAddress != null && selectedAddress.equals(address)) {
                        scheduleSnapshot = snapshot;
                        // Create a new TableRow for the data entry
                        TableRow dataRow = new TableRow(EditSchedule.this); // Use the activity context

                        // Create TextViews for the data
                        TextView dateTextView = new TextView(EditSchedule.this); // Use the activity context
                        dateTextView.setText(date);
                        dateTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        dateTextView.setGravity(Gravity.START);
                        dateTextView.setPadding(10, 10, 5, 10);

                        TextView garbageTypeTextView = new TextView(EditSchedule.this); // Use the activity context
                        garbageTypeTextView.setText(garbageType);
                        garbageTypeTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        garbageTypeTextView.setGravity(Gravity.START);
                        garbageTypeTextView.setPadding(10, 10, 5, 10);

                        // Add the TextViews to the dataRow
                        dataRow.addView(dateTextView);
                        dataRow.addView(garbageTypeTextView);

                // Add an OnClickListener to the dataRow
                        dataRow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Toggle the visibility of linearLayout_inputs
                                if (linearLayoutInputs.getVisibility() == View.GONE) {
                                    linearLayoutInputs.setVisibility(View.VISIBLE);

                                    // Set values from the clicked row to input fields and spinners
                                    dateAutoCompleteTextView.setText(date);
                                    addressAutoCompleteTextView.setText(address);
                                    setSpinnerSelection(garbageTypeSpinner, garbageType);
                                    setSpinnerSelection(repeatTimeSpinner, repeatType);

                                }
                            }


                            private void setSpinnerSelection(Spinner spinner, String value) {
                                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
                                if (adapter != null) {
                                    int position = adapter.getPosition(value);
                                    if (position != -1) {
                                        spinner.setSelection(position);
                                    }
                                }
                            }

                        });


                        // Add the dataRow to the dataTableLayout (inside the ScrollView)
                dataTableLayout.addView(dataRow);
            }
        }
    }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the DeleteDialogFragment when the delete button is clicked
                showDeleteDialog();
            }

            private void showDeleteDialog() {
                DeleteDialogFragment deleteDialog = new DeleteDialogFragment();
                deleteDialog.show(getSupportFragmentManager(), "delete_dialog");
            }
        });




    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void initAndPopulateSpinners() {
        // Initialize spinners
        Spinner spinnerTypeOfGarbage = findViewById(R.id.spinner_typeofgarbage);
        ArrayAdapter<CharSequence> typeOfGarbageAdapter = ArrayAdapter.createFromResource(this, R.array.garbageTypes, android.R.layout.simple_spinner_item);
        typeOfGarbageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTypeOfGarbage.setAdapter(typeOfGarbageAdapter);

        Spinner spinnerDoesNotRepeat = findViewById(R.id.spinner_doesNotRepeat);
        ArrayAdapter<CharSequence> doesNotRepeatAdapter = ArrayAdapter.createFromResource(this, R.array.doesNotRepeat_array, android.R.layout.simple_spinner_item);
        doesNotRepeatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDoesNotRepeat.setAdapter(doesNotRepeatAdapter);

        // Set OnItemSelectedListener for the spinners
        spinnerTypeOfGarbage.setOnItemSelectedListener(this);
        spinnerDoesNotRepeat.setOnItemSelectedListener(this);
    }

    private void BackPressed() {
        //                working but redirected to the schedule fragment instead of setting fragment
        Intent intent = new Intent(this, Admin_Home_activity.class);
        startActivity(intent);
        finish();

    }

    //    displaying List of Address on the firebase realtime database to the spinner
    private void fetchFirebaseDataAndPopulateSpinner(Spinner spinner) {
        // Assuming you have a reference to your Firebase database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("schedules");

        // Listen for changes in the data
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> uniqueAddresses = new ArrayList<>();

                // Iterate through the data and extract unique addresses
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String address = snapshot.child("address").getValue(String.class);

                    // Check if the address is not already in the list
                    if (address != null && !uniqueAddresses.contains(address)) {
                        uniqueAddresses.add(address);
                    }
                }
                if (uniqueAddresses.isEmpty()) {
                    uniqueAddresses.add("No Schedule");
                }

                // Create an ArrayAdapter with the unique addresses
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        EditSchedule.this,
                        android.R.layout.simple_spinner_item,
                        uniqueAddresses
                );

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }


    private void updateTableWithFilteredData(String selectedAddress) {
        DatabaseReference schedulesRef = FirebaseDatabase.getInstance().getReference("schedules");

        TextInputLayout dateInputLayout = findViewById(R.id.layout_addSched_date);
        TextInputLayout addressInputLayout = findViewById(R.id.layout_addSched_address);

// Initialize views in the onCreate method
        AutoCompleteTextView dateAutoCompleteTextView = dateInputLayout.findViewById(R.id.addSched_date);
        AutoCompleteTextView addressAutoCompleteTextView = addressInputLayout.findViewById(R.id.addSched_address);

        schedulesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear existing data rows
                dataTableLayout.removeAllViews();

                for (DataSnapshot scheduleSnapshot : dataSnapshot.getChildren()) {
                    String date = scheduleSnapshot.child("date").getValue(String.class);
                    String garbageType = scheduleSnapshot.child("garbageType").getValue(String.class);
                    String repeatType = scheduleSnapshot.child("repeatType").getValue(String.class);
                    String address = scheduleSnapshot.child("address").getValue(String.class);

                    // Check if the schedule's address matches the selected address
                    if (selectedAddress != null && selectedAddress.equals(address)) {
                        // Create a new TableRow for the data entry
                        TableRow dataRow = new TableRow(EditSchedule.this); // Use the activity context

                        // Create TextViews for the data
                        TextView dateTextView = new TextView(EditSchedule.this); // Use the activity context
                        dateTextView.setText(date);
                        dateTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        dateTextView.setGravity(Gravity.START);
                        dateTextView.setPadding(10, 10, 5, 10);

                        TextView garbageTypeTextView = new TextView(EditSchedule.this); // Use the activity context
                        garbageTypeTextView.setText(garbageType);
                        garbageTypeTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                        garbageTypeTextView.setGravity(Gravity.START);
                        garbageTypeTextView.setPadding(10, 10, 5, 10);

                        // Add the TextViews to the dataRow
                        dataRow.addView(dateTextView);
                        dataRow.addView(garbageTypeTextView);

                        // Add an OnClickListener to the dataRow
                        dataRow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Toggle the visibility of linearLayout_inputs
                                if (linearLayoutInputs.getVisibility() == View.VISIBLE) {
                                    linearLayoutInputs.setVisibility(View.GONE);
                                } else {
                                    linearLayoutInputs.setVisibility(View.VISIBLE);

                                    // Set values from the clicked row to input fields and spinners
                                    dateAutoCompleteTextView.setText(date);
                                    addressAutoCompleteTextView.setText(address);
                                    setSpinnerSelection(garbageTypeSpinner, garbageType);
                                    setSpinnerSelection(repeatTimeSpinner, repeatType);
                                }
                            }

                            private void setSpinnerSelection(Spinner spinner, String value) {
                                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
                                if (adapter != null) {
                                    int position = adapter.getPosition(value);
                                    if (position != -1) {
                                        spinner.setSelection(position);
                                    }
                                }
                            }
                        });

                        // Add the dataRow to the dataTableLayout (inside the ScrollView)
                        dataTableLayout.addView(dataRow);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Get the selected address from the spinner
        String selectedAddress = (String) parent.getSelectedItem();

        // call the method with the selected address from the spinner
        updateTableWithFilteredData(selectedAddress);
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onDeleteConfirmed(boolean deleteThisEvent, boolean deleteThisAndFollowingEvents) {
        // Handle the delete confirmation
        if (deleteThisEvent) {
            // Delete only this event
        } else if (deleteThisAndFollowingEvents) {
            // Delete this and following events
        }
        // Add your logic to perform the delete operation

    }

    @Override
    public void onDeleteCancelled() {
    }
}