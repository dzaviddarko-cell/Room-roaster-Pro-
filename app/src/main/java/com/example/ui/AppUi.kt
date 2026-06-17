package com.example.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.RoastRecord
import com.example.utils.SpeechManager
import com.example.viewmodel.RoastUiState
import com.example.viewmodel.RoastViewModel
import android.speech.tts.Voice

@Composable
fun RoomRoasterApp(viewModel: RoastViewModel, speechManager: SpeechManager) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController, viewModel)
        }
        composable("result") {
            ResultScreen(navController, viewModel, speechManager)
        }
        composable("history") {
            HistoryScreen(navController, viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: RoastViewModel) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    val roomTypes = listOf("Bedroom", "Living Room", "Kitchen", "Bathroom", "Office")
    var selectedRoomType by remember { mutableStateOf(roomTypes[0]) }
    var roomDropdownExpanded by remember { mutableStateOf(false) }

    val roastLevels = listOf("Light", "Medium", "Spicy")
    var selectedRoastLevel by remember { mutableStateOf(roastLevels[1]) }
    var roastDropdownExpanded by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                selectedImageUri = it
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    bitmap = ImageDecoder.decodeBitmap(source)
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
            }
        }
    )

    LaunchedEffect(uiState) {
        if (uiState is RoastUiState.Success) {
            navController.navigate("result")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Room Roaster", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = { navController.navigate("history") }) {
                        Icon(Icons.Filled.History, contentDescription = "History")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Upload a photo of your space and we'll brutally analyze it.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )

            // Image Picker Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Selected Room",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.ImageSearch,
                            contentDescription = "Pick Photo",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap to Select Photo", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Room Type Selection
            ExposedDropdownMenuBox(
                expanded = roomDropdownExpanded,
                onExpandedChange = { roomDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedRoomType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Room Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = roomDropdownExpanded,
                    onDismissRequest = { roomDropdownExpanded = false }
                ) {
                    roomTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedRoomType = type
                                roomDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Roast Level Selection
            ExposedDropdownMenuBox(
                expanded = roastDropdownExpanded,
                onExpandedChange = { roastDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedRoastLevel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Intensity Level") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roastDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = roastDropdownExpanded,
                    onDismissRequest = { roastDropdownExpanded = false }
                ) {
                    roastLevels.forEach { level ->
                        DropdownMenuItem(
                            text = { Text(level) },
                            onClick = {
                                selectedRoastLevel = level
                                roastDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (uiState is RoastUiState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        bitmap?.let {
                            viewModel.roastRoom(it, selectedRoomType, selectedRoastLevel)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = bitmap != null,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Text("Roast My Room", style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp))
                }
            }

            if (uiState is RoastUiState.Error) {
                Text(
                    text = (uiState as RoastUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(navController: NavController, viewModel: RoastViewModel, speechManager: SpeechManager) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var isPlaying by remember { mutableStateOf(false) }
    var availableVoices by remember { mutableStateOf<List<Voice>>(emptyList()) }
    var selectedVoice by remember { mutableStateOf<Voice?>(null) }
    var voiceDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        availableVoices = speechManager.getAvailableVoices()
        selectedVoice = availableVoices.firstOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("The Verdict") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetState()
                        navController.popBackStack()
                    }) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { padding ->
        if (uiState is RoastUiState.Success) {
            val roast = (uiState as RoastUiState.Success).roast
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = roast.roastText,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Badge(containerColor = MaterialTheme.colorScheme.primary) { Text(roast.roomType) }
                            Badge(containerColor = MaterialTheme.colorScheme.secondary) { Text(roast.roastLevel) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Voice Controls
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.SettingsVoice, contentDescription = "Voice Settings")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reader Voice", style = MaterialTheme.typography.bodyLarge)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        ExposedDropdownMenuBox(
                            expanded = voiceDropdownExpanded,
                            onExpandedChange = { voiceDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedVoice?.name ?: "Default Voice",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = voiceDropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = voiceDropdownExpanded,
                                onDismissRequest = { voiceDropdownExpanded = false }
                            ) {
                                availableVoices.forEach { voice ->
                                    DropdownMenuItem(
                                        text = { Text(voice.name) },
                                        onClick = {
                                            selectedVoice = voice
                                            voiceDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    if (isPlaying) {
                                        speechManager.stop()
                                    } else {
                                        speechManager.speak(roast.roastText, selectedVoice)
                                    }
                                    isPlaying = !isPlaying
                                },
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            ) {
                                Icon(if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow, contentDescription = "Play/Stop")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isPlaying) "Stop" else "Listen")
                            }

                            Button(
                                onClick = {
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Room Roast AI told me: \"${roast.roastText}\"")
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                    context.startActivity(shareIntent)
                                },
                                modifier = Modifier.weight(1f).padding(start = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = MaterialTheme.colorScheme.onTertiary)
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = "Share")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController, viewModel: RoastViewModel) {
    val history by viewModel.roastHistory.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Burn Book") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { padding ->
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No roasts yet. Go fetch some trauma.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history) { roast ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        roast.roastText,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row {
                                        Badge { Text(roast.roomType) }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Badge(containerColor = MaterialTheme.colorScheme.secondary) { Text(roast.roastLevel) }
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteRoast(roast.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete Roast", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
