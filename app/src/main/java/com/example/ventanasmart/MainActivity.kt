
package com.example.ventanasmart

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VentanaSmartApp()
        }
    }
}

@Composable
fun VentanaSmartApp() {
    var modoOscuro by remember { mutableStateOf(false) }

    val colores = if (modoOscuro) {
        darkColorScheme(
            primary = Color(0xFF80CBC4),
            secondary = Color(0xFF90CAF9),
            background = Color(0xFF101820),
            surface = Color(0xFF1C2833)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF00796B),
            secondary = Color(0xFF1976D2),
            background = Color(0xFFEAF5FA),
            surface = Color.White
        )
    }

    MaterialTheme(colorScheme = colores) {
        PantallaPrincipal(
            modoOscuro = modoOscuro,
            cambiarModoOscuro = { modoOscuro = it }
        )
    }
}

@Composable
fun PantallaPrincipal(
    modoOscuro: Boolean,
    cambiarModoOscuro: (Boolean) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val usuario = auth.currentUser
    val db = FirebaseFirestore.getInstance()
    val contexto = LocalContext.current
    val actividad = contexto as? ComponentActivity

    var mostrarAgregar by remember { mutableStateOf(false) }
    var ventanaSeleccionada by remember { mutableStateOf<String?>(null) }
    var ventanaParaEliminar by remember { mutableStateOf<String?>(null) }
    var mensaje by remember { mutableStateOf("") }

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )
    val scope = rememberCoroutineScope()

    val fondo = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF071B2C),
            Color(0xFF123B56),
            Color(0xFF1B6078)
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxHeight(),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "WindowSmart",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        )
                    )

                    Text(
                        text = usuario?.email ?: "Usuario",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.65f
                        ),
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 4.dp
                        )
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    NavigationDrawerItem(
                        label = { Text("Inicio") },
                        selected = ventanaSeleccionada == null,
                        onClick = {
                            ventanaSeleccionada = null
                            scope.launch { drawerState.close() }
                        },
                        icon = { Text("🏠") }
                    )

                    Text(
                        text = "MIS VENTANAS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.65f
                        ),
                        modifier = Modifier.padding(
                            start = 16.dp,
                            top = 18.dp,
                            bottom = 8.dp
                        )
                    )

                    if (usuario == null) {
                        Text(
                            text = "Inicia sesión para ver tus ventanas.",
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    } else {
                        VentanasDelMenu(
                            usuarioId = usuario.uid,
                            ventanaSeleccionada = ventanaSeleccionada,
                            seleccionar = { id ->
                                ventanaSeleccionada = id
                                scope.launch { drawerState.close() }
                            },
                            solicitarEliminar = { id ->
                                ventanaParaEliminar = id
                            }
                        )
                    }

                    NavigationDrawerItem(
                        label = { Text("Agregar ventana") },
                        selected = false,
                        onClick = {
                            mostrarAgregar = true
                            mensaje = ""
                            scope.launch { drawerState.close() }
                        },
                        icon = { Text("＋") },
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    NavigationDrawerItem(
                        label = { Text("Cerrar sesión") },
                        selected = false,
                        onClick = {
                            auth.signOut()

                            val intent = Intent(
                                contexto,
                                LoginActivity::class.java
                            ).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }

                            contexto.startActivity(intent)
                            actividad?.finish()
                        },
                        icon = { Text("🚪") },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedIconColor =
                                MaterialTheme.colorScheme.error,
                            unselectedTextColor =
                                MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(fondo)
        ) {
            CieloAnimado()

            if (ventanaSeleccionada != null) {
                ControlVentana(
                    ventanaId = ventanaSeleccionada!!,
                    volver = {
                        ventanaSeleccionada = null
                    },
                    abrirMenu = {
                        scope.launch { drawerState.open() }
                    }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            horizontal = 20.dp,
                            vertical = 18.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                scope.launch { drawerState.open() }
                            },
                            contentPadding = PaddingValues(
                                horizontal = 8.dp
                            )
                        ) {
                            Text(
                                text = "☰",
                                fontSize = 29.sp,
                                color = Color.White
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "WindowSmart",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = "Control inteligente de ventanas",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (modoOscuro) "🌙" else "☀️",
                                fontSize = 23.sp
                            )

                            Switch(
                                checked = modoOscuro,
                                onCheckedChange = cambiarModoOscuro
                            )
                        }
                    }

                    Text(
                        text = usuario?.email ?: "Usuario",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    WeatherCard()

                    GlassCard {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Mis ventanas",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Selecciona una ventana para consultar su estado y abrirla o cerrarla.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.75f
                                )
                            )

                            Button(
                                onClick = {
                                    mostrarAgregar = true
                                    mensaje = ""
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("＋ Agregar ventana")
                            }

                            OutlinedButton(
                                onClick = {
                                    if (usuario == null) {
                                        mensaje =
                                            "Inicia sesión para crear una ventana."
                                    } else {
                                        val datos = hashMapOf<String, Any>(
                                            "nombre" to
                                                    "Ventana de demostración",
                                            "estado" to "cerrada",
                                            "lluvia" to false,
                                            "luz" to false,
                                            "motivoCierre" to "manual",
                                            "usuarioId" to usuario.uid,
                                            "simulada" to true,
                                            "wifiConfigurada" to false
                                        )

                                        db.collection("ventanas")
                                            .add(datos)
                                            .addOnSuccessListener {
                                                mensaje =
                                                    "Ventana de demostración creada."
                                            }
                                            .addOnFailureListener { error ->
                                                mensaje =
                                                    "No se pudo crear: " +
                                                            (error.message
                                                                ?: "Error de Firebase")
                                            }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("🧪 Crear ventana de demostración")
                            }

                            Text(
                                text = "La demostración permite probar la interfaz sin conectar el dispositivo físico.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.65f
                                )
                            )

                            if (mensaje.isNotBlank()) {
                                Text(
                                    text = mensaje,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            HorizontalDivider()

                            ListaVentanas(
                                usuarioId = usuario?.uid,
                                seleccionar = { id ->
                                    ventanaSeleccionada = id
                                },
                                solicitarEliminar = { id ->
                                    ventanaParaEliminar = id
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    if (mostrarAgregar) {
        AgregarDispositivoDialog(
            cerrar = { mostrarAgregar = false }
        )
    }

    if (ventanaParaEliminar != null) {
        AlertDialog(
            onDismissRequest = { ventanaParaEliminar = null },
            title = {
                Text(
                    text = "Eliminar ventana",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("¿Seguro que quieres eliminar esta ventana? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = ventanaParaEliminar ?: return@TextButton
                        db.collection("ventanas")
                            .document(id)
                            .delete()
                            .addOnSuccessListener {
                                if (ventanaSeleccionada == id) {
                                    ventanaSeleccionada = null
                                }
                                ventanaParaEliminar = null
                                mensaje = "Ventana eliminada correctamente."
                            }
                            .addOnFailureListener { error ->
                                ventanaParaEliminar = null
                                mensaje = "No se pudo eliminar: " +
                                        (error.message ?: "Error de Firebase")
                            }
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { ventanaParaEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun VentanasDelMenu(
    usuarioId: String,
    ventanaSeleccionada: String?,
    seleccionar: (String) -> Unit,
    solicitarEliminar: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    var ventanas by remember {
        mutableStateOf<List<Map<String, Any>>>(emptyList())
    }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }

    DisposableEffect(usuarioId) {
        val listener = db.collection("ventanas")
            .whereEqualTo("usuarioId", usuarioId)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    error = true
                    cargando = false
                } else {
                    ventanas = snapshot?.documents?.map { documento ->
                        documento.data.orEmpty() + ("id" to documento.id)
                    } ?: emptyList()

                    error = false
                    cargando = false
                }
            }

        onDispose {
            listener.remove()
        }
    }

    when {
        cargando -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        error -> {
            Text(
                text = "No se pudieron cargar las ventanas.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(12.dp)
            )
        }

        ventanas.isEmpty() -> {
            Text(
                text = "Aún no tienes ventanas.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.7f
                ),
                modifier = Modifier.padding(12.dp)
            )
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
            ) {
                items(
                    items = ventanas,
                    key = { it["id"].toString() }
                ) { ventana ->
                    val id = ventana["id"].toString()
                    val nombre =
                        ventana["nombre"] as? String ?: "Mi ventana"
                    val estado =
                        ventana["estado"] as? String ?: "cerrada"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NavigationDrawerItem(
                            label = {
                                Column {
                                    Text(
                                        text = nombre,
                                        maxLines = 1
                                    )

                                    Text(
                                        text = if (estado == "abierta") {
                                            "Abierta"
                                        } else {
                                            "Cerrada"
                                        },
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(
                                            alpha = 0.65f
                                        )
                                    )
                                }
                            },
                            selected = id == ventanaSeleccionada,
                            onClick = { seleccionar(id) },
                            icon = { Text("🪟") },
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(
                            onClick = { solicitarEliminar(id) },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("🗑️", fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherCard() {
    var clima by remember {
        mutableStateOf("Consultando el clima...")
    }

    LaunchedEffect(Unit) {
        clima = obtenerClima()
    }

    GlassCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Clima de hoy",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = clima,
                fontSize = 17.sp,
                lineHeight = 25.sp
            )

            Text(
                text = "Información obtenida mediante Open-Meteo",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.65f
                )
            )
        }
    }
}

@Composable
fun ListaVentanas(
    usuarioId: String?,
    seleccionar: (String) -> Unit,
    solicitarEliminar: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    var ventanas by remember {
        mutableStateOf<List<Map<String, Any>>>(emptyList())
    }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(usuarioId) {
        if (usuarioId == null) {
            ventanas = emptyList()
            cargando = false

            onDispose {}
        } else {
            val listener = db.collection("ventanas")
                .whereEqualTo("usuarioId", usuarioId)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        error = "No se pudieron cargar las ventanas."
                        cargando = false
                    } else {
                        ventanas = snapshot?.documents?.map { documento ->
                            documento.data.orEmpty() + ("id" to documento.id)
                        } ?: emptyList()

                        error = null
                        cargando = false
                    }
                }

            onDispose {
                listener.remove()
            }
        }
    }

    when {
        cargando -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error
            )
        }

        ventanas.isEmpty() -> {
            Text(
                text = "Todavía no tienes ventanas. Puedes crear una de demostración para probar la aplicación.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.7f
                )
            )
        }

        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = ventanas,
                    key = { it["id"].toString() }
                ) { ventana ->
                    VentanaListaCard(
                        ventana = ventana,
                        seleccionar = {
                            seleccionar(ventana["id"].toString())
                        },
                        solicitarEliminar = {
                            solicitarEliminar(ventana["id"].toString())
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VentanaListaCard(
    ventana: Map<String, Any>,
    seleccionar: () -> Unit,
    solicitarEliminar: () -> Unit
) {
    val nombre = ventana["nombre"] as? String ?: "Mi ventana"
    val estado = ventana["estado"] as? String ?: "cerrada"
    val lluvia = ventana["lluvia"] as? Boolean ?: false
    val luz = ventana["luz"] as? Boolean ?: false
    val simulada = ventana["simulada"] as? Boolean ?: false

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { seleccionar() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(
                alpha = 0.92f
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = nombre,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = if (estado == "abierta") "🟢" else "🔒",
                    fontSize = 20.sp
                )

                TextButton(
                    onClick = solicitarEliminar,
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Text("🗑️", fontSize = 18.sp)
                }
            }

            Text(
                text = "Estado: ${estado.replaceFirstChar { it.uppercase() }}",
                fontSize = 14.sp
            )

            Text(
                text = "🌧️ Lluvia: ${if (lluvia) "Detectada" else "No detectada"}",
                fontSize = 13.sp
            )

            Text(
                text = "☀️ Luz: ${if (luz) "Detectada" else "No detectada"}",
                fontSize = 13.sp
            )

            if (simulada) {
                Text(
                    text = "🧪 Modo demostración",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Toca para controlar →",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AgregarDispositivoDialog(
    cerrar: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val usuario = FirebaseAuth.getInstance().currentUser

    val wifiManager = remember {
        context.applicationContext.getSystemService(
            Context.WIFI_SERVICE
        ) as WifiManager
    }

    var redes by remember {
        mutableStateOf<List<ScanResult>>(emptyList())
    }
    var buscando by remember { mutableStateOf(false) }
    var redSeleccionada by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nombreVentana by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }

    fun actualizarRedes() {
        try {
            redes = wifiManager.scanResults
                .filter { it.SSID.isNotBlank() }
                .distinctBy { it.SSID }
                .sortedByDescending { it.level }

            buscando = false

            mensaje = if (redes.isEmpty()) {
                "No se encontraron redes. Intenta buscar nuevamente."
            } else {
                ""
            }
        } catch (_: SecurityException) {
            buscando = false
            mensaje = "No hay permiso para consultar las redes Wi-Fi."
        }
    }

    val permisosWifi = remember {
        buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }.toTypedArray()
    }

    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (resultado.values.all { it }) {
            try {
                buscando = true
                wifiManager.startScan()
                actualizarRedes()
            } catch (_: SecurityException) {
                buscando = false
                mensaje = "No se pudo buscar redes Wi-Fi."
            }
        } else {
            mensaje = "Debes conceder los permisos para buscar redes."
        }
    }

    val receptor = remember {
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {
                actualizarRedes()
            }
        }
    }

    DisposableEffect(context) {
        val filtro = IntentFilter(
            WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                    receptor,
                    filtro,
                    Context.RECEIVER_NOT_EXPORTED
                )
            } else {
                @Suppress("DEPRECATION")
                context.registerReceiver(receptor, filtro)
            }
        } catch (_: Exception) {
            // La pantalla permite continuar aunque no se registre el receptor.
        }

        onDispose {
            try {
                context.unregisterReceiver(receptor)
            } catch (_: Exception) {
                // El receptor podría no haberse registrado.
            }
        }
    }

    fun buscarRedes() {
        val tienePermisos = permisosWifi.all { permiso ->
            ContextCompat.checkSelfPermission(
                context,
                permiso
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (!tienePermisos) {
            permisoLauncher.launch(permisosWifi)
            return
        }

        try {
            buscando = true
            mensaje = ""
            wifiManager.startScan()
            actualizarRedes()
            buscando = true
        } catch (_: SecurityException) {
            buscando = false
            mensaje = "No se pudieron consultar las redes Wi-Fi."
        }
    }

    AlertDialog(
        onDismissRequest = cerrar,
        title = {
            Text(
                text = "Agregar ventana",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nombreVentana,
                    onValueChange = { nombreVentana = it },
                    label = { Text("Nombre de la ventana") },
                    placeholder = {
                        Text("Ej. Ventana del dormitorio")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Redes Wi-Fi disponibles",
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedButton(
                    onClick = { buscarRedes() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (buscando) {
                            "Buscando redes..."
                        } else {
                            "Buscar redes Wi-Fi"
                        }
                    )
                }

                if (redes.isNotEmpty()) {
                    Text(
                        text = "Selecciona una red:",
                        fontSize = 13.sp
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            items = redes,
                            key = { it.SSID }
                        ) { red ->
                            val seleccionada =
                                redSeleccionada == red.SSID

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        redSeleccionada = red.SSID
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (seleccionada) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = red.SSID,
                                            fontWeight = if (seleccionada) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            }
                                        )

                                        Text(
                                            text = when {
                                                red.level >= -55 ->
                                                    "Señal excelente"
                                                red.level >= -67 ->
                                                    "Buena señal"
                                                red.level >= -75 ->
                                                    "Señal regular"
                                                else -> "Señal débil"
                                            },
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (seleccionada) {
                                        Text("✓")
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text("Contraseña Wi-Fi (opcional)")
                    },
                    visualTransformation = if (mostrarPassword) {
                        androidx.compose.ui.text.input.VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                mostrarPassword = !mostrarPassword
                            }
                        ) {
                            Text(
                                if (mostrarPassword) "Ocultar" else "Ver"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Elegir una red todavía no conecta la aplicación con el dispositivo físico. La contraseña no se guarda en Firebase.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.7f
                    )
                )

                if (mensaje.isNotBlank()) {
                    Text(
                        text = mensaje,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (usuario == null) {
                        mensaje = "Debes iniciar sesión primero."
                        return@TextButton
                    }

                    val nombre = nombreVentana.trim()
                        .ifBlank { "Mi ventana" }

                    val datos = hashMapOf<String, Any>(
                        "nombre" to nombre,
                        "estado" to "cerrada",
                        "lluvia" to false,
                        "luz" to false,
                        "motivoCierre" to "manual",
                        "usuarioId" to usuario.uid,
                        "simulada" to true,
                        "wifiConfigurada" to false
                    )

                    if (redSeleccionada.isNotBlank()) {
                        datos["wifiSSID"] = redSeleccionada
                    }

                    db.collection("ventanas")
                        .add(datos)
                        .addOnSuccessListener {
                            cerrar()
                        }
                        .addOnFailureListener { error ->
                            mensaje = "Error al guardar: " +
                                    (error.message ?: "Firebase")
                        }
                }
            ) {
                Text("Agregar para prueba")
            }
        },
        dismissButton = {
            TextButton(onClick = cerrar) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ControlVentana(
    ventanaId: String,
    volver: () -> Unit,
    abrirMenu: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val referencia = remember(ventanaId) {
        db.collection("ventanas").document(ventanaId)
    }

    var nombre by remember { mutableStateOf("Mi ventana") }
    var estado by remember { mutableStateOf("cerrada") }
    var lluvia by remember { mutableStateOf(false) }
    var luz by remember { mutableStateOf(false) }
    var simulada by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf("") }

    DisposableEffect(referencia) {
        val listener = referencia.addSnapshotListener { documento, error ->
            if (error != null) {
                mensaje = "No se pudo leer la ventana."
                cargando = false
            } else if (documento != null && documento.exists()) {
                nombre = documento.getString("nombre") ?: "Mi ventana"
                estado = documento.getString("estado") ?: "cerrada"
                lluvia = documento.getBoolean("lluvia") ?: false
                luz = documento.getBoolean("luz") ?: false
                simulada = documento.getBoolean("simulada") ?: false
                cargando = false
                mensaje = ""
            } else {
                mensaje = "Esta ventana ya no existe."
                cargando = false
            }
        }

        onDispose {
            listener.remove()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = abrirMenu) {
                    Text(
                        text = "☰ Menú",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(onClick = volver) {
                    Text(
                        text = "← Mis ventanas",
                        color = Color.White
                    )
                }
            }

            Text(
                text = "Control de ventana",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            GlassCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = nombre,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (simulada) {
                        Text(
                            text = "🧪 Modo demostración",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (cargando) {
                        CircularProgressIndicator()
                    } else {
                        VentanaAnimada(
                            abierta = estado == "abierta",
                            onClick = {
                                val nuevoEstado =
                                    if (estado == "abierta") {
                                        "cerrada"
                                    } else {
                                        "abierta"
                                    }

                                referencia.update(
                                    mapOf(
                                        "estado" to nuevoEstado,
                                        "motivoCierre" to "manual"
                                    )
                                ).addOnFailureListener { error ->
                                    mensaje = "No se pudo actualizar: " +
                                            (error.message ?: "Error")
                                }
                            }
                        )
                    }
                }
            }

            GlassCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Sensores",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (lluvia) {
                            "🌧️ Lluvia detectada"
                        } else {
                            "🌧️ Sin lluvia detectada"
                        },
                        fontSize = 16.sp
                    )

                    Text(
                        text = if (luz) {
                            "☀️ Luz detectada"
                        } else {
                            "🌙 Poca luz o noche"
                        },
                        fontSize = 16.sp
                    )

                    Text(
                        text = "Estos valores provienen de Firebase. Sin sensores conectados, son datos de prueba.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.7f
                        )
                    )
                }
            }

            if (mensaje.isNotBlank()) {
                Text(
                    text = mensaje,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun VentanaAnimada(
    abierta: Boolean,
    onClick: () -> Unit
) {
    val progreso by animateFloatAsState(
        targetValue = if (abierta) 1f else 0f,
        animationSpec = tween(
            durationMillis = 900,
            easing = FastOutSlowInEasing
        ),
        label = "animacionVentana"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clickable { onClick() }
        ) {
            val ancho = size.width
            val alto = size.height
            val margenX = ancho * 0.12f
            val margenY = alto * 0.10f
            val izquierda = margenX
            val arriba = margenY
            val marcoAncho = ancho - margenX * 2
            val marcoAlto = alto - margenY * 2
            val abajo = arriba + marcoAlto
            val centroX = ancho / 2f
            val grosor = 9.dp.toPx()
            val radio = 10.dp.toPx()

            drawRoundRect(
                color = Color(0x30000000),
                topLeft = Offset(
                    izquierda + 4.dp.toPx(),
                    arriba + 5.dp.toPx()
                ),
                size = Size(marcoAncho, marcoAlto),
                cornerRadius = CornerRadius(radio)
            )

            drawRoundRect(
                color = Color(0xFFD7F2FF),
                topLeft = Offset(izquierda, arriba),
                size = Size(marcoAncho, marcoAlto),
                cornerRadius = CornerRadius(radio)
            )

            val anchoInterior = marcoAncho - grosor * 2
            val altoInterior = marcoAlto - grosor * 2
            val anchoPanel = anchoInterior / 2f
            val desplazamiento = progreso * anchoPanel * 0.72f
            val xIzquierda = izquierda + grosor - desplazamiento
            val xDerecha = centroX + desplazamiento

            drawRoundRect(
                color = Color(0xFF83D7F2),
                topLeft = Offset(xIzquierda, arriba + grosor),
                size = Size(anchoPanel, altoInterior),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(
                    xIzquierda + anchoPanel * 0.18f,
                    arriba + grosor + 12.dp.toPx()
                ),
                end = Offset(
                    xIzquierda + anchoPanel * 0.18f,
                    abajo - grosor - 12.dp.toPx()
                ),
                strokeWidth = 3.dp.toPx()
            )

            drawLine(
                color = Color.White.copy(alpha = 0.55f),
                start = Offset(
                    xIzquierda + anchoPanel * 0.3f,
                    arriba + grosor + 12.dp.toPx()
                ),
                end = Offset(
                    xIzquierda + anchoPanel * 0.45f,
                    arriba + grosor + 50.dp.toPx()
                ),
                strokeWidth = 2.dp.toPx()
            )

            drawRoundRect(
                color = Color(0xFF9BE2F5),
                topLeft = Offset(xDerecha, arriba + grosor),
                size = Size(anchoPanel, altoInterior),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(
                    xDerecha + anchoPanel * 0.78f,
                    arriba + grosor + 12.dp.toPx()
                ),
                end = Offset(
                    xDerecha + anchoPanel * 0.78f,
                    abajo - grosor - 12.dp.toPx()
                ),
                strokeWidth = 3.dp.toPx()
            )

            val tiradorAncho = 4.dp.toPx()
            val tiradorAlto = 27.dp.toPx()
            val tiradorY = arriba + marcoAncho * 0.18f

            drawRoundRect(
                color = Color(0xFF546E7A),
                topLeft = Offset(
                    xIzquierda + anchoPanel - 10.dp.toPx(),
                    tiradorY
                ),
                size = Size(tiradorAncho, tiradorAlto),
                cornerRadius = CornerRadius(3.dp.toPx())
            )

            drawRoundRect(
                color = Color(0xFF546E7A),
                topLeft = Offset(
                    xDerecha + 6.dp.toPx(),
                    tiradorY
                ),
                size = Size(tiradorAncho, tiradorAlto),
                cornerRadius = CornerRadius(3.dp.toPx())
            )

            drawRoundRect(
                color = Color(0xFF455A64),
                topLeft = Offset(izquierda, arriba),
                size = Size(marcoAncho, marcoAlto),
                cornerRadius = CornerRadius(radio),
                style = Stroke(width = grosor)
            )

            if (progreso < 0.1f) {
                drawLine(
                    color = Color(0xFF455A64),
                    start = Offset(centroX, arriba + grosor),
                    end = Offset(centroX, abajo - grosor),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }

        Text(
            text = if (abierta) {
                "🟢 Ventana abierta"
            } else {
                "🔒 Ventana cerrada"
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (abierta) "Cerrar ventana" else "Abrir ventana"
            )
        }

        Text(
            text = "También puedes tocar la ventana para cambiar su estado.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.7f
            )
        )
    }
}

@Composable
fun GlassCard(
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(
                alpha = 0.88f
            )
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = contenido
        )
    }
}

@Composable
fun CieloAnimado() {
    val infiniteTransition = rememberInfiniteTransition(
        label = "cielo"
    )

    val movimiento by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "movimiento"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "☁️",
            fontSize = 70.sp,
            modifier = Modifier
                .offset(x = movimiento.dp, y = 80.dp)
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
                .padding(top = 45.dp, end = 35.dp)
                .alpha(0.18f)
        )
    }
}
