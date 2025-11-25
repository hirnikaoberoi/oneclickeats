package com.example.secondapp


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val nameField: EditText = findViewById(R.id.etName)
        val emailField: EditText = findViewById(R.id.etEmail)
        val phoneField: EditText = findViewById(R.id.etPhone)
        val passwordField: EditText = findViewById(R.id.etPassword)
        val signupButton: Button = findViewById(R.id.btnSignup)
        val loginRedirect: TextView = findViewById(R.id.tvLogin)

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        /* signupButton.setOnClickListener {
             val name = nameField.text.toString().trim()
             val email = emailField.text.toString().trim()
             val phone = phoneField.text.toString().trim()
             val password = passwordField.text.toString().trim()

             if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                 Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                 return@setOnClickListener
             }

             val user = mapOf(
                 "name" to name,
                 "email" to email,
                 "phone" to phone,
                 "password" to password // Password is the primary key
             )

             usersRef.child(password).setValue(user).addOnSuccessListener {
                 Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                 // Navigate to Login Page
                 startActivity(Intent(this, MainActivity::class.java))
                 finish()
             }.addOnFailureListener {
                 Toast.makeText(this, "Failed to create account. Try again!", Toast.LENGTH_SHORT).show()
             }
         }*/

        loginRedirect.setOnClickListener {
//            startActivity(Intent(this, LoginActivity::class.java))
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


        signupButton.setOnClickListener {
            val name = nameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val phone = phoneField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create user in Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid

                        if (userId != null) {
                            val user = mapOf(
                                "userId" to userId,
                                "name" to name,
                                "email" to email,
                                "phone" to phone
                            )

                            // Save data in Realtime Database
                            usersRef.child(userId).setValue(user)

                            // Save data in Firestore
                            firestore.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()

                                    // Determine the correct activity based on email format
                                    val intent = when {
                                        email.startsWith("btbt") && email.endsWith("banasthali.in") -> {
                                            Intent(this, MainActivity::class.java)
                                        }
                                        email.endsWith("banasthali.in") -> {
                                            Intent(this, MainActivity::class.java)
                                        }
                                        else -> {
                                            Intent(this, MainActivity::class.java)
                                        }
                                    }

//                                    startActivity(Intent(this, MainActivity::class.java))
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Failed to save in Firestore", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }


    }
}