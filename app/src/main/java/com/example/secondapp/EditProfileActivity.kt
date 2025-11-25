package com.example.secondapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditProfileActivity : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etPhone: EditText

    //    private lateinit var etEmail: EditText
    private lateinit var btnSave: Button
    private lateinit var etCurrentPassword: EditText
    private lateinit var etPassword: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
//        etEmail = findViewById(R.id.etEmail)
        btnSave = findViewById(R.id.btnSave)
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etPassword = findViewById(R.id.etPassword)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadUserProfile()  // Fetch data from Firebase

        btnSave.setOnClickListener {
//            saveUserProfile()
            updateUserProfile()
        }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                etName.setText(document.getString("name"))
                etPhone.setText(document.getString("phone"))
//                etEmail.setText(document.getString("email"))
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun updateUserProfile() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = user.uid
        val newName = etName.text.toString()
        val newPhone = etPhone.text.toString()
        val newPassword = etPassword.text.toString()
        val currentPassword = etCurrentPassword.text.toString()

        if (currentPassword.isEmpty()) {
            Toast.makeText(this, "Please enter your current password", Toast.LENGTH_SHORT).show()
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

        // Step 1: Re-authenticate the user
        user.reauthenticate(credential).addOnSuccessListener {
            // Step 2: Update Firestore (Name, Phone)
            val userRef = db.collection("users").document(userId)
            val updatedUser = mapOf(
                "name" to newName,
                "phone" to newPhone
            )

            userRef.set(updatedUser, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error updating Firestore: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            // Step 3: Update Password (only if user entered a new password)
            if (newPassword.isNotEmpty()) {
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error updating password: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(
                this,
                "Re-authentication failed. Check your password.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}