package com.example.ventanasmart

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Si ya hay una sesión iniciada, entra directamente
        if (auth.currentUser != null) {
            entrar()
            return
        }

        setContent {
            VentanaSmartLogin()
        }
    }

    private fun entrar() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun crearCuenta(
        email: String,
        password: String,
        onResult: (String?) -> Unit
    ) {

        if (email.isBlank() || password.isBlank()) {
            onResult("Completa todos los campos")
            return
        }

        auth.createUserWithEmailAndPassword(
            email.trim(),
            password
        ).addOnCompleteListener { task ->

            if (task.isSuccessful) {
                entrar()
            } else {
                onResult(
                    task.exception?.message
                        ?: "No se pudo crear la cuenta"
                )
            }
        }
    }

    private fun iniciarSesion(
        email: String,
        password: String,
        onResult: (String?) -> Unit
    ) {

        if (email.isBlank() || password.isBlank()) {
            onResult("Completa todos los campos")
            return
        }

        auth.signInWithEmailAndPassword(
            email.trim(),
            password
        ).addOnCompleteListener { task ->

            if (task.isSuccessful) {
                entrar()
            } else {
                onResult(
                    task.exception?.message
                        ?: "Correo o contraseña incorrectos"
                )
            }
        }
    }

    @Composable
    fun VentanaSmartLogin() {

        var modoRegistro by remember {
            mutableStateOf(false)
        }

        var email by remember {
            mutableStateOf("")
        }

        var password by remember {
            mutableStateOf("")
        }

        // Mostrar u ocultar contraseña
        var mostrarPassword by remember {
            mutableStateOf(false)
        }

        var mensaje by remember {
            mutableStateOf("")
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF071B2C),
                            Color(0xFF123B56),
                            Color(0xFF1B6078)
                        )
                    )
                )
        ) {

            CieloLogin()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "VentanaSmart",
                    fontSize = 34.sp,
                    color = Color.White
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = if (modoRegistro)
                        "Crear una cuenta"
                    else
                        "Control inteligente de tu ventana",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 15.sp
                )

                Spacer(
                    modifier = Modifier.height(30.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            Color.White.copy(alpha = 0.12f)
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {

                        Text(
                            text = if (modoRegistro)
                                "Crear cuenta"
                            else
                                "Iniciar sesión",
                            fontSize = 25.sp,
                            color = Color.White
                        )

                        Spacer(
                            modifier = Modifier.height(20.dp)
                        )

                        // CORREO
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                mensaje = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Correo electrónico")
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor =
                                    Color.White.copy(alpha = 0.7f),
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor =
                                    Color.White.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(
                            modifier = Modifier.height(14.dp)
                        )

                        // CONTRASEÑA
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                mensaje = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Contraseña")
                            },
                            singleLine = true,

                            visualTransformation =
                                if (mostrarPassword) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },

                            // BOTÓN 👁️
                            trailingIcon = {

                                IconButton(
                                    onClick = {
                                        mostrarPassword =
                                            !mostrarPassword
                                    }
                                ) {

                                    Text(
                                        text =
                                            if (mostrarPassword)
                                                "🙈"
                                            else
                                                "👁️",
                                        fontSize = 22.sp
                                    )
                                }
                            },

                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor =
                                    Color.White.copy(alpha = 0.7f),
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor =
                                    Color.White.copy(alpha = 0.5f)
                            )
                        )

                        Spacer(
                            modifier = Modifier.height(20.dp)
                        )

                        // BOTÓN PRINCIPAL
                        Button(
                            onClick = {

                                if (modoRegistro) {

                                    crearCuenta(
                                        email,
                                        password
                                    ) { error ->

                                        mensaje =
                                            error ?: ""
                                    }

                                } else {

                                    iniciarSesion(
                                        email,
                                        password
                                    ) { error ->

                                        mensaje =
                                            error ?: ""
                                    }
                                }
                            },

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp),

                            shape = RoundedCornerShape(18.dp)
                        ) {

                            Text(
                                text =
                                    if (modoRegistro)
                                        "Crear cuenta"
                                    else
                                        "Iniciar sesión",

                                fontSize = 17.sp
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        // CAMBIAR ENTRE LOGIN Y REGISTRO
                        TextButton(
                            onClick = {

                                modoRegistro =
                                    !modoRegistro

                                mensaje = ""
                                password = ""
                                mostrarPassword = false
                            },

                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text(
                                text =
                                    if (modoRegistro)
                                        "Ya tengo una cuenta"
                                    else
                                        "Crear una cuenta"
                            )
                        }

                        // MENSAJE DE ERROR
                        if (mensaje.isNotEmpty()) {

                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )

                            Text(
                                text = mensaje,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CieloLogin() {

        val infiniteTransition =
            rememberInfiniteTransition(
                label = "cielo"
            )

        val movimiento by infiniteTransition.animateFloat(
            initialValue = -20f,
            targetValue = 20f,

            animationSpec =
                infiniteRepeatable(
                    animation = tween(
                        durationMillis = 5000
                    ),
                    repeatMode = RepeatMode.Reverse
                ),

            label = "movimiento"
        )

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            Text(
                text = "☁️",
                fontSize = 70.sp,

                modifier = Modifier
                    .offset(
                        x = movimiento.dp,
                        y = 80.dp
                    )
                    .alpha(0.18f)
            )

            Text(
                text = "☁️",
                fontSize = 90.sp,

                modifier = Modifier
                    .offset(
                        x = (movimiento * -0.5f).dp,
                        y = 500.dp
                    )
                    .alpha(0.12f)
            )

            Text(
                text = "☀️",
                fontSize = 90.sp,

                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = 45.dp,
                        end = 35.dp
                    )
                    .alpha(0.18f)
            )
        }
    }
}