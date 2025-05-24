package com.example.saka.auth

import com.google.firebase.auth.FirebaseAuth

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Connexion d'un utilisateur avec email et mot de passe.
     * Renvoie true si succès, false et un message d'erreur sinon.
     */
    fun signIn(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    /**
     * Déconnexion de l'utilisateur actuel.
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Renvoie l'ID (UID) de l'utilisateur connecté, ou null s'il n'est pas connecté.
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Enregistrement d'un nouvel utilisateur.
     * Renvoie true si succès, false et un message d'erreur sinon.
     */
    fun registerUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    /**
     * Vérifie si un utilisateur est actuellement connecté.
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
