package ca.unb.mobiledev.group18project.ui.courses

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ca.unb.mobiledev.group18project.MainActivity
import ca.unb.mobiledev.group18project.R
import ca.unb.mobiledev.group18project.databinding.FragmentCoursesHistoryBinding
import ca.unb.mobiledev.group18project.entities.Course
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CoursesHistoryFragment : CoursesFragment() {

    private var _binding: FragmentCoursesHistoryBinding? = null

    private lateinit var mCourseViewModel: CoursesViewModel
    private lateinit var mListView: ListView

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mCourseViewModel = ViewModelProvider(this).get(CoursesViewModel::class.java)

        _binding = FragmentCoursesHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mListView = binding.coursesHistory

        mCourseViewModel.getAllCompleteCourses().observe(viewLifecycleOwner) {
            SearchCompleteCourses()
        }

        updateFutureClasses()

        return root
    }

    override fun onResume() {
        super.onResume()
        // Hide the action bar
        (activity as? MainActivity)?.hideBottomNav()
    }

    override fun onPause() {
        super.onPause()
        (activity as? MainActivity)?.showBottomNav()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun BuildDialog(title: String, course: Course?, new: Boolean) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_course, null)
        val editTextCourse = dialogView.findViewById<EditText>(R.id.editTextCourseName)
        val chTextCourse = dialogView.findViewById<EditText>(R.id.editTextCourseCH)
        val startDateButton = dialogView.findViewById<Button>(R.id.startDate)
        val endDateButton = dialogView.findViewById<Button>(R.id.endDate)
        val infoText = dialogView.findViewById<EditText>(R.id.infoTextView)

        var selectedStartDate = ""
        var selectedEndDate = ""

        startDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                // Note: Month is 0-based, so add 1 for display
                selectedStartDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                startDateButton.text = selectedStartDate
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

            datePickerDialog.show()
        }

        endDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                // Note: Month is 0-based, so add 1 for display
                selectedEndDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                endDateButton.text = selectedEndDate
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

            datePickerDialog.show()
        }


        if(!new){
            // Set existing values
            editTextCourse.setText(course?.name)
            chTextCourse.setText(course?.ch.toString())
            selectedStartDate = course?.startDate.toString() // Format: "YYYY-MM-DD"
            startDateButton.text = selectedStartDate
            selectedEndDate = course?.endDate.toString() // Format: "YYYY-MM-DD"
            endDateButton.text = selectedEndDate
            infoText.setText(course?.info.toString())
        }

        builder.setView(dialogView)
            .setTitle(title)
            .setPositiveButton("Submit") { _, _ ->

                try{
                    val name = editTextCourse.text.toString()
                    val ch = chTextCourse.text.toString()
                    val info = infoText.text.toString()

                    if (name == "" || ch.toIntOrNull() == null || selectedStartDate == "" || selectedEndDate == "") {
                        Toast.makeText(binding.root.context, "Data entered is incomplete/incorrect format. Data has not been saved.", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    if (new) {
                        Toast.makeText(binding.root.context, "New Data Entry", Toast.LENGTH_SHORT).show()
                        mCourseViewModel.insert(name, ch.toInt(), selectedStartDate, selectedEndDate, info)
                    } else {
                        Toast.makeText(binding.root.context, "Updated Data Entry", Toast.LENGTH_SHORT).show()
                        course?.name = name
                        course?.ch = ch.toInt()
                        course?.startDate = selectedStartDate
                        course?.endDate = selectedEndDate
                        course?.info = info
                        mCourseViewModel.update(course!!)
                    }
                updateFutureClasses()
                } catch(e: Exception){
                    Toast.makeText(binding.root.context, "Something Went Wrong. Please ensure correct format", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
               }
            }
            .setNegativeButton("Cancel", null)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    fun SearchCompleteCourses() {
        mCourseViewModel.getAllCompleteCourses().observe(viewLifecycleOwner) { courses ->
            val adapter = CoursesAdapter(requireContext(), courses, mCourseViewModel, this)
            mListView.adapter = adapter
        }
    }

    fun updateFutureClasses(){
        //Temporary Placed here to update past dates
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)

        mCourseViewModel.updateFutureDates(currentDate)
    }
}