package com.example.secondapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailField: EditText = findViewById(R.id.etLoginEmail)
        val passwordField: EditText = findViewById(R.id.etLoginPassword)
        val loginButton: Button = findViewById(R.id.btnLogin)
        val signupRedirect: TextView = findViewById(R.id.tvSignup)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()
        // Firebase Database reference
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        signupRedirect.setOnClickListener {
//            startActivity(Intent(this, SignupActivity::class.java))
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Fetch user data by iterating through the database
            /*usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var loginSuccess = false

                    // Iterate through all users to find a match
                    for (userSnapshot in snapshot.children) {
                        val userEmail = userSnapshot.child("email").value.toString()
                        val userPassword = userSnapshot.key // Assuming password is the primary key

                        if (userEmail == email && userPassword == password) {
                            loginSuccess = true
                            break
                        }
                    }

                    if (loginSuccess) {
                        Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                        Log.d("LoginActivity", "User logged in successfully: $email")

                        if (email.matches(Regex("^btbt.*@banasthali\\.in$"))) {
                            // Student email format detected, redirect to Student UI
                            val intent = Intent(this@LoginActivity, StudentActivity::class.java)
                            startActivity(intent)
                        } else if (email.matches(Regex(".*@gmail\\.com$"))) {
                            // Staff email format detected, redirect to Staff UI
                            val intent = Intent(this@LoginActivity, StaffActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@LoginActivity, "Invalid Email Format", Toast.LENGTH_SHORT).show()
                            return
                        }

                        finish()
                       /* // Navigate to the next screen
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()*/
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid Email or Password", Toast.LENGTH_SHORT).show()
                        Log.e("LoginActivity", "Login failed for user: $email")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LoginActivity, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", "Database error: ${error.message}")
                }
            })
        }*/



            // Authenticate user with FirebaseAuth
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                        Log.d("LoginActivity", "User logged in successfully: $email")

                        // Redirect based on email format
                        if (email.matches(Regex("^btbt.*@banasthali\\.in$"))) {
                            startActivity(Intent(this, MainActivity::class.java))
                        } else if (email.matches(Regex(".*@banasthali\\.in$"))) {
                            startActivity(Intent(this, MainActivity::class.java))
                        } else {
                            Toast.makeText(this, "Invalid Email Format", Toast.LENGTH_SHORT).show()
                            return@addOnCompleteListener
                        }

                        finish() // Close LoginActivity after successful login
                    } else {
                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        Log.e("LoginActivity", "Login failed: ${task.exception?.message}")
                    }
                }
        }


    }
}
