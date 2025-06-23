package com.example.metar_decoder

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * Aktywność rejestracji nowego użytkownika.
 *
 * Pozwala użytkownikowi utworzyć konto przez Firebase Authentication.
 * Sprawdza poprawność wpisanych danych i zgodność haseł, a po udanej rejestracji przekierowuje do okna logowania.
 */
class RegisterActivity : AppCompatActivity() {

    /** Instancja FirebaseAuth do obsługi rejestracji użytkowników. */
    private lateinit var auth: FirebaseAuth

    /**
     * Wywoływane podczas tworzenia aktywności. Inicjalizuje widoki i obsługuje kliknięcia przycisków.
     *
     * @param savedInstanceState Stan zapisany aktywności, jeśli taki istnieje.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val goToLoginButton = findViewById<Button>(R.id.goToLoginButton)

        // Obsługa rejestracji
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                Toast.makeText(this, "Uzupełnij wszystkie pola!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Hasła nie są zgodne!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Rejestracja udana!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Błąd rejestracji: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }

        // Przycisk do powrotu na ekran logowania
        goToLoginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
