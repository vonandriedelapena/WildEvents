package cit.edu.wildevents

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment

class EditPersonalInfoDialogFragment(
    private val title: String,
    private val currentValue: String,
    private val onSave: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_info, null)
        val editText = view.findViewById<EditText>(R.id.edit_text)
        editText.setText(currentValue)

        builder.setTitle("Edit $title")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val newValue = editText.text.toString()
                onSave(newValue)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        return builder.create()
    }
}

