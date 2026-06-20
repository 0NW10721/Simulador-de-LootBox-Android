package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ChestHistory
import com.example.data.Reward
import com.example.ui.ChestState
import com.example.ui.LootboxViewModel
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Vibrant Colors Palette for Cosmic styling
val OffBlack = Color(0xFF0C091A)
val DeepPurpleCard = Color(0xFF191235)
val MythicMagenta = Color(0xFFFF2A5F)
val GoldGlow = Color(0xFFFFC400)
val PurpleEpic = Color(0xFFBB86FC)
val CommonMint = Color(0xFF00E5FF)
val CardBorder = Color(0xFF2D235C)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.systemBars
                ) { innerPadding ->
                    val viewModel: LootboxViewModel = viewModel()
                    LootboxMainScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

fun getRarityColor(rarity: String): Color {
    return when (rarity) {
        "MITICO" -> MythicMagenta
        "LENDARIO" -> GoldGlow
        "EPICO" -> PurpleEpic
        else -> CommonMint
    }
}

fun getRarityName(rarity: String): String {
    return when (rarity) {
        "MITICO" -> "Mítico"
        "LENDARIO" -> "Lendário"
        "EPICO" -> "Épico"
        else -> "Comum"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LootboxMainScreen(
    viewModel: LootboxViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val chestState by viewModel.chestState.collectAsState()
    val wonReward by viewModel.wonReward.collectAsState()
    val showTenRewardsDialog by viewModel.showTenRewardsDialog.collectAsState()
    val tenWonRewards by viewModel.tenWonRewards.collectAsState()

    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(OffBlack, Color(0xFF130E2B))
                )
            )
    ) {
        // App top tab header layout
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "✨ Simulador de LootBox ✨",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 22.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // High Fidelity Animated Tab Bar
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DeepPurpleCard,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GoldGlow
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        // Keep open tabs static during unlocking sequence for safety
                        if (chestState == ChestState.IDLE || chestState == ChestState.REVEALED) {
                            selectedTab = 0
                        }
                    },
                    text = {
                        Text(
                            text = "Abrir Baú",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) GoldGlow else Color.LightGray
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Main",
                            tint = if (selectedTab == 0) GoldGlow else Color.LightGray
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        if (chestState == ChestState.IDLE || chestState == ChestState.REVEALED) {
                            selectedTab = 1
                        }
                    },
                    text = {
                        Text(
                            text = "Prêmios",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) PurpleEpic else Color.LightGray
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Rewards",
                            tint = if (selectedTab == 1) PurpleEpic else Color.LightGray
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        if (chestState == ChestState.IDLE || chestState == ChestState.REVEALED) {
                            selectedTab = 2
                        }
                    },
                    text = {
                        Text(
                            text = "Histórico",
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 2) CommonMint else Color.LightGray
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "History",
                            tint = if (selectedTab == 2) CommonMint else Color.LightGray
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> OpeningChestTab(viewModel = viewModel)
                    1 -> RewardsTab(viewModel = viewModel)
                    2 -> StatsHistoryTab(viewModel = viewModel)
                }
            }
        }

        // Expanded Bulk Multi-Open Reveal Dialog
        if (showTenRewardsDialog && tenWonRewards.isNotEmpty()) {
            TenRewardsDialog(
                rewards = tenWonRewards,
                onDismiss = { viewModel.dismissTenRewards() }
            )
        }
    }
}

// ======================= TAB 1: CHEST OPENING VIEW =======================
@Composable
fun OpeningChestTab(viewModel: LootboxViewModel) {
    val chestState by viewModel.chestState.collectAsState()
    val wonReward by viewModel.wonReward.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper stats banner display
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepPurpleCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎯 Chaves de Probabilidade",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProbabilityBullet(label = "Comum", chance = "84.5%", color = CommonMint, icon = "🪵")
                    ProbabilityBullet(label = "Épico", chance = "12%", color = PurpleEpic, icon = "🔮")
                    ProbabilityBullet(label = "Lendário", chance = "3%", color = GoldGlow, icon = "✨")
                    ProbabilityBullet(label = "Mítico", chance = "0.5%", color = MythicMagenta, icon = "💀")
                }
            }
        }

        // Central Animation Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedChest(
                chestState = chestState,
                wonReward = wonReward,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Lower Reward details panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .height(130.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = chestState == ChestState.REVEALED && wonReward != null,
                enter = scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                wonReward?.let { reward ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DeepPurpleCard.copy(alpha = 0.95f))
                            .border(BorderStroke(2.dp, getRarityColor(reward.rarity)), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Surface(
                            color = getRarityColor(reward.rarity).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(100),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "  ${getRarityName(reward.rarity).uppercase()}  ",
                                color = getRarityColor(reward.rarity),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        Text(
                            text = reward.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Item liberado com sucesso!",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = chestState == ChestState.IDLE,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Toque no botão para conjurar prêmios",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Qual será sua sorte hoje?",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = chestState == ChestState.SHAKING || chestState == ChestState.BURSTING,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Abrindo a LootBox...",
                        color = GoldGlow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Canalizando energia arcana da recompensa...",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Actions Panels
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (chestState == ChestState.REVEALED) {
                Button(
                    onClick = { viewModel.resetChest() },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldGlow),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Coletar Recompensa 🎁",
                        color = OffBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(0.95f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.openChest() },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CommonMint,
                            disabledContainerColor = CardBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isProcessing) "Abrindo..." else "Abrir 1x",
                            color = if (isProcessing) Color.Gray else OffBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.openTenChests() },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldGlow,
                            disabledContainerColor = CardBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .border(
                                if (!isProcessing) BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)) else BorderStroke(0.dp, Color.Transparent),
                                RoundedCornerShape(12.dp)
                            ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isProcessing) "Abrindo..." else "Abrir 10x ✨",
                            color = if (isProcessing) Color.Gray else OffBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProbabilityBullet(
    label: String,
    chance: String,
    color: Color,
    icon: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color.copy(alpha = 0.15f), CircleShape)
                .border(BorderStroke(1.dp, color), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 11.sp)
        }
        Column {
            Text(text = label, color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = chance, color = color, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
    }
}

// Particle System Field
@Composable
fun ParticleField(chestState: ChestState, rarity: String) {
    if (chestState == ChestState.IDLE) return

    val particles = remember { mutableStateListOf<Particle>() }

    LaunchedEffect(chestState, rarity) {
        val random = java.util.Random()
        while (true) {
            withFrameNanos {
                val iterator = particles.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.x += (Math.cos(p.angle) * p.speed).toFloat()
                    p.y += (Math.sin(p.angle) * p.speed).toFloat()
                    p.speed *= 0.95f
                    p.life -= p.decay
                    if (p.life <= 0f) {
                        iterator.remove()
                    }
                }

                // Particles frequency matching state
                val spawnRate = when (chestState) {
                    ChestState.SHAKING -> 1
                    ChestState.BURSTING -> 10
                    ChestState.REVEALED -> 1
                    else -> 0
                }

                for (i in 0 until spawnRate) {
                    val angle = random.nextDouble() * 2 * Math.PI
                    val speed = if (chestState == ChestState.BURSTING) {
                        random.nextFloat() * 14f + 3f
                    } else {
                        random.nextFloat() * 5f + 1f
                    }
                    val size = random.nextFloat() * 14f + 5f
                    val life = 1.0f
                    val decay = random.nextFloat() * 0.04f + 0.015f

                    particles.add(
                        Particle(
                            x = 0f,
                            y = if (chestState == ChestState.REVEALED) -110f else 0f,
                            angle = angle,
                            speed = speed,
                            size = size,
                            color = when (rarity) {
                                "LENDARIO" -> if (random.nextBoolean()) GoldGlow else Color(0xFFFF8F00)
                                "EPICO" -> if (random.nextBoolean()) PurpleEpic else Color(0xFFE040FB)
                                else -> if (random.nextBoolean()) CommonMint else Color.White
                            },
                            life = life,
                            decay = decay
                        )
                    )
                }
            }
            delay(16) // Target 60 fps ticks
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp)
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f + 25f

        particles.forEach { p ->
            drawCircle(
                color = p.color.copy(alpha = p.life),
                radius = p.size * p.life,
                center = androidx.compose.ui.geometry.Offset(cx + p.x, cy + p.y)
            )
        }
    }
}

class Particle(
    var x: Float,
    var y: Float,
    val angle: Double,
    var speed: Float,
    val size: Float,
    val color: Color,
    var life: Float,
    val decay: Float
)

// Animated complete chest
@Composable
fun AnimatedChest(
    chestState: ChestState,
    wonReward: Reward?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ChestCycle")

    // Shake motions logic
    val shakeRotation by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shaking_rot"
    )

    val shakeX by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(45, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shaking_x"
    )

    val shakeY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(35, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shaking_y"
    )

    // Breathing motion for chest state
    val breatheValue by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    // Floating bobbing coordinates when won reward is displayed
    val floatYPercent by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hover_movement"
    )

    // Spring interpolation selectors
    val generalScale by animateFloatAsState(
        targetValue = when (chestState) {
            ChestState.IDLE -> breatheValue
            ChestState.SHAKING -> 1.05f
            ChestState.BURSTING -> 1.2f
            ChestState.REVEALED -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "genScale"
    )

    val lidShiftY by animateDpAsState(
        targetValue = when (chestState) {
            ChestState.IDLE -> 0.dp
            ChestState.SHAKING -> 0.dp
            ChestState.BURSTING -> (-24).dp
            ChestState.REVEALED -> (-60).dp
        },
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "lidShift"
    )

    val lidRotationZ by animateFloatAsState(
        targetValue = when (chestState) {
            ChestState.REVEALED -> -30f
            else -> 0f
        },
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessLow
        ),
        label = "lidRot"
    )

    val rewardSizeScale by animateFloatAsState(
        targetValue = if (chestState == ChestState.REVEALED) 1.5f else 0f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "rewardSize"
    )

    val rewardFloatingShiftY by animateDpAsState(
        targetValue = if (chestState == ChestState.REVEALED) (-100).dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "rewardShift"
    )

    val auraColor = getRarityColor(wonReward?.rarity ?: "COMUM")

    Box(
        modifier = modifier
            .size(260.dp)
            .graphicsLayer {
                if (chestState == ChestState.SHAKING) {
                    rotationZ = shakeRotation
                    translationX = shakeX
                    translationY = shakeY
                }
                scaleX = generalScale
                scaleY = generalScale

                if (chestState == ChestState.REVEALED) {
                    translationY = floatYPercent
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Glowing radial light aura background
        if (chestState == ChestState.BURSTING || chestState == ChestState.REVEALED) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val glowBrush = Brush.radialGradient(
                            colors = listOf(
                                auraColor.copy(alpha = 0.55f),
                                auraColor.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = size.center,
                            radius = size.width * 0.75f
                        )
                        drawCircle(brush = glowBrush, radius = size.width * 0.75f)
                    }
            )
        }

        // Particle Emitters Overlay
        ParticleField(chestState = chestState, rarity = wonReward?.rarity ?: "COMUM")

        // Drawer alignment of structural chest elements
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 45.dp)
        ) {
            // Chest Lid (Tampa)
            Box(
                modifier = Modifier
                    .width(135.dp)
                    .height(46.dp)
                    .offset(y = lidShiftY)
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0.5f, 1.0f) // Pivot at the bottom center of the lid
                        rotationZ = lidRotationZ
                    }
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF8D5B2C), Color(0xFF5E391D))
                        ),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .border(
                        BorderStroke(3.dp, Color(0xFFFFD700)),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Gold Wood Bands decoration
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.25f)
                        .background(Color(0xFFFFD700).copy(alpha = 0.7f))
                ) {}
                
                // Front seal block
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(13.dp)
                        .background(Color(0xFFDFB831), RoundedCornerShape(2.dp))
                        .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(2.dp))
                )
            }

            // Chest Base (Corpo)
            Box(
                modifier = Modifier
                    .width(132.dp)
                    .height(68.dp)
                    .offset(y = (-3).dp) // Close joingap
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF5E391D), Color(0xFF382211))
                        ),
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .border(
                        BorderStroke(3.dp, Color(0xFFFFD700)),
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                // Internal glow effect while open
                if (chestState == ChestState.BURSTING || chestState == ChestState.REVEALED) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.35f)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(auraColor.copy(alpha = 0.8f), Color.Transparent)
                                )
                            )
                    )
                }

                // Mechanical lock plate
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(28.dp)
                        .offset(y = (-4).dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFD700), Color(0xFFC79E1B))
                            ),
                            shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
                        )
                        .border(
                            1.dp, Color(0xFFFFEA80),
                            shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Lock core keyhole
                    Box(
                        modifier = Modifier
                            .size(7.dp, 11.dp)
                            .background(
                                if (chestState == ChestState.REVEALED || chestState == ChestState.BURSTING) auraColor else Color.Black,
                                shape = RoundedCornerShape(100)
                            )
                    )
                }
            }
        }

        // Rising reward aura node representation
        if (wonReward != null && (chestState == ChestState.REVEALED || chestState == ChestState.BURSTING)) {
            Box(
                modifier = Modifier
                    .offset(y = rewardFloatingShiftY)
                    .graphicsLayer {
                        scaleX = rewardSizeScale
                        scaleY = rewardSizeScale
                    }
                    .size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Large Outer Glow Ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(auraColor.copy(alpha = 0.5f), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )

                // Colored Core Rim Circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(8.dp, CircleShape)
                        .background(DeepPurpleCard, CircleShape)
                        .border(BorderStroke(2.dp, auraColor), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = wonReward.icon,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


// ======================= TAB 2: REWARDS MANAGEMENT VIEW =======================
@Composable
fun RewardsTab(viewModel: LootboxViewModel) {
    val rewards by viewModel.allRewards.collectAsState()

    var nameInput by remember { mutableStateOf("") }
    var rarityInput by remember { mutableStateOf("COMUM") }
    var selectedEmoji by remember { mutableStateOf("⚔️") }
    var customEmojiInput by remember { mutableStateOf("") }

    val presetEmojis = listOf("🐲", "⚔️", "🛡️", "👑", "💎", "💍", "🔮", "🧪", "📜", "🪙", "🪵", "🍎", "🔑", "🦄", "🌟", "💰", "🪄", "🏹")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // Drop addition dashboard
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepPurpleCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🛠️ Criar Nova Recompensa",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Input name
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nome do Item") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = GoldGlow,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = GoldGlow,
                        unfocusedLabelColor = Color.LightGray
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Selector rarity chips
                Text(
                    text = "Grau de Raridade",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("COMUM", "EPICO", "LENDARIO", "MITICO").forEach { rarity ->
                        val isSelected = rarityInput == rarity
                        val color = getRarityColor(rarity)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { rarityInput = rarity },
                            color = if (isSelected) color.copy(alpha = 0.25f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) color else CardBorder
                            )
                        ) {
                            Text(
                                text = getRarityName(rarity),
                                color = if (isSelected) color else Color.LightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick emoji picker list
                Text(
                    text = "Escolha ou Digite o Ícone (Emoji)",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presetEmojis) { emoji ->
                        val isSelected = selectedEmoji == emoji && customEmojiInput.isEmpty()
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isSelected) PurpleEpic.copy(alpha = 0.3f) else Color.Transparent,
                                    CircleShape
                                )
                                .border(
                                    BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) PurpleEpic else CardBorder
                                    ),
                                    CircleShape
                                )
                                .clickable { 
                                    selectedEmoji = emoji 
                                    customEmojiInput = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom icon input text field
                OutlinedTextField(
                    value = customEmojiInput,
                    onValueChange = { newVal ->
                        // Allow short string, typical for an emoji or symbol character
                        if (newVal.length <= 6) { 
                            customEmojiInput = newVal
                            if (newVal.isNotEmpty()) {
                                selectedEmoji = newVal
                            } else {
                                selectedEmoji = "⚔️"
                            }
                        }
                    },
                    label = { Text("Ou digite seu próprio emoji ou ícone") },
                    placeholder = { Text("Ex: 🌟, 💰, 🦄") },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(PurpleEpic.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = selectedEmoji, fontSize = 16.sp)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PurpleEpic,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = PurpleEpic,
                        unfocusedLabelColor = Color.LightGray,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Submit button trigger
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            viewModel.addCustomReward(nameInput, rarityInput, selectedEmoji)
                            nameInput = "" // Reset state
                            customEmojiInput = ""
                            selectedEmoji = "⚔️"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldGlow),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    enabled = nameInput.isNotBlank()
                ) {
                    Text(
                        text = "Salvar Recompensa no Baú 💾",
                        color = OffBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Pool rewards header listing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pool Atual de Recompensas (${rewards.size})",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            TextButton(
                onClick = { viewModel.restoreDefaultRewards() },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restaurar Padrões",
                    tint = GoldGlow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Restaurar Padrões",
                    color = GoldGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Rewards list
        if (rewards.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Nenhuma recompensa ativa no banco de dados.",
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.restoreDefaultRewards() },
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleEpic),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Restaurar Recompensas Padrão", color = Color.White)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(rewards) { reward ->
                    RewardCard(reward = reward, onDelete = { viewModel.deleteReward(reward) })
                }
            }
        }
    }
}

@Composable
fun RewardCard(reward: Reward, onDelete: () -> Unit) {
    val rarityColor = getRarityColor(reward.rarity)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DeepPurpleCard)
            .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(12.dp))
            .drawBehind {
                // Colored left accent bar to avoid AI slop layouts
                drawRect(
                    color = rarityColor,
                    size = size.copy(width = 5.dp.toPx())
                )
            }
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Emoji circle icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(rarityColor.copy(alpha = 0.12f), CircleShape)
                    .border(BorderStroke(1.dp, rarityColor.copy(alpha = 0.4f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = reward.icon, fontSize = 22.sp)
            }

            // Info text block
            Column {
                Text(
                    text = reward.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = getRarityName(reward.rarity),
                        color = rarityColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (reward.isCustom) {
                        Surface(
                            color = PurpleEpic.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = " Custom ",
                                color = PurpleEpic,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Delete button for all reward items
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Excluir",
                tint = Color(0xFFFF5252)
            )
        }
    }
}

// ======================= TAB 3: STATS HISTORY VIEW =======================
@Composable
fun StatsHistoryTab(viewModel: LootboxViewModel) {
    val history by viewModel.allHistory.collectAsState()
    val stats by viewModel.stats.collectAsState()

    var showClearConfirm by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Analytics panel
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepPurpleCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📊 Métricas de Sorte Real",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Big opened tally
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(GoldGlow.copy(alpha = 0.15f), CircleShape)
                                .border(BorderStroke(2.dp, GoldGlow), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stats.totalOpened.toString(),
                                color = GoldGlow,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            )
                        }
                        Text(
                            text = "Baús Abertos",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Separation line
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(80.dp)
                            .background(CardBorder)
                    )

                    // Distribution listing calculations
                    Column(
                        modifier = Modifier.weight(1f).padding(start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val mythicPercent = if (stats.totalOpened > 0) {
                            (stats.mythicCount * 100f / stats.totalOpened)
                        } else 0f

                        val legendaryPercent = if (stats.totalOpened > 0) {
                            (stats.legendaryCount * 100f / stats.totalOpened)
                        } else 0f

                        val epicPercent = if (stats.totalOpened > 0) {
                            (stats.epicCount * 100f / stats.totalOpened)
                        } else 0f

                        val commonPercent = if (stats.totalOpened > 0) {
                            (stats.commonCount * 100f / stats.totalOpened)
                        } else 0f

                        RarityProgressBar(label = "Mítico (💀)", count = stats.mythicCount, percent = mythicPercent, color = MythicMagenta)
                        RarityProgressBar(label = "Lendário (✨)", count = stats.legendaryCount, percent = legendaryPercent, color = GoldGlow)
                        RarityProgressBar(label = "Épico (🔮)", count = stats.epicCount, percent = epicPercent, color = PurpleEpic)
                        RarityProgressBar(label = "Comum (🪵)", count = stats.commonCount, percent = commonPercent, color = CommonMint)
                    }
                }
            }
        }

        // Backup and Data Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DeepPurpleCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "💾 Gestão de Backup",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Gere ou restaure o backup das suas recompensas customizadas e histórico de sorte.",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val context = LocalContext.current
                    val clipboardManager = LocalClipboardManager.current

                    Button(
                        onClick = {
                            val backupStr = viewModel.exportBackup()
                            if (backupStr.isNotEmpty()) {
                                clipboardManager.setText(AnnotatedString(backupStr))
                                Toast.makeText(context, "Backup copiado para a área de transferência! 👍", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Erro ao gerar backup.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleEpic),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Exportar Code", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    Button(
                        onClick = { showImportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CommonMint),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = OffBlack)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Importar Code", color = OffBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }

        // History logs header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(imageVector = Icons.Default.List, contentDescription = "History", tint = Color.LightGray)
                Text(
                    text = "Livro de Recompensas Obtidas",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            if (history.isNotEmpty()) {
                TextButton(onClick = { showClearConfirm = true }) {
                    Text(text = "Limpar Tudo", color = Color(0xFFFF5252), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Dynamic Height Audit Logs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 400.dp)
        ) {
            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Nenhum baú foi aberto ainda.", color = Color.Gray, fontSize = 14.sp)
                        Text(
                            text = "Vá para a aba inicial e tente sua sorte!",
                            color = Color.DarkGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(history) { record ->
                        HistoryRecordRow(record = record)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Author Credits (Não atrapalha o usuário)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.dp, CardBorder.copy(alpha = 0.5f)), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "👑",
                    fontSize = 11.sp
                )
                Text(
                    text = "Criado por ONW",
                    color = Color.LightGray.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }

    if (showImportDialog) {
        var importText by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }
        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Importar Código de Backup", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column {
                    Text(
                        text = "Cole o código JSON de seu backup abaixo para reescrever as recompensas e histórico atuais.",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { 
                            importText = it
                            isError = false
                        },
                        label = { Text("Código JSON Backup") },
                        placeholder = { Text("Cole aqui...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CommonMint,
                            unfocusedBorderColor = CardBorder,
                            focusedLabelColor = CommonMint,
                            unfocusedLabelColor = Color.LightGray,
                            focusedPlaceholderColor = Color.DarkGray,
                            unfocusedPlaceholderColor = Color.DarkGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                    if (isError) {
                        Text(
                            text = "Código de backup inválido ou com formato incorreto.",
                            color = Color(0xFFFF5252),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importText.isBlank()) {
                            isError = true
                        } else {
                            viewModel.importBackup(importText) { success ->
                                if (success) {
                                    Toast.makeText(context, "Backup importado com sucesso! 🎉", Toast.LENGTH_SHORT).show()
                                    showImportDialog = false
                                } else {
                                    isError = true
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CommonMint)
                ) {
                    Text("Importar", color = OffBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = DeepPurpleCard
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Zerar Histórico?", fontWeight = FontWeight.Bold, color = Color.White) },
            text = { Text("Você perderá todas as estatísticas e registros de baús abertos até agora. Esta ação é definitiva.", color = Color.LightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearHistory()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("Confirmar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = DeepPurpleCard
        )
    }
}

@Composable
fun RarityProgressBar(
    label: String,
    count: Int,
    percent: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "$count (${String.format("%.1f", percent)}%)",
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
                .height(4.dp)
                .background(CardBorder, RoundedCornerShape(100))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percent / 100f)
                    .background(color, RoundedCornerShape(100))
            )
        }
    }
}

@Composable
fun HistoryRecordRow(record: ChestHistory) {
    val rarityColor = getRarityColor(record.rewardRarity)
    val timeFormatted = remember(record.timestamp) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        sdf.format(Date(record.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DeepPurpleCard.copy(alpha = 0.6f))
            .border(BorderStroke(1.dp, CardBorder.copy(alpha = 0.6f)), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(rarityColor.copy(alpha = 0.1f), CircleShape)
                    .border(BorderStroke(1.dp, rarityColor.copy(alpha = 0.3f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = record.rewardIcon, fontSize = 18.sp)
            }

            Column {
                Text(
                    text = record.rewardName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = getRarityName(record.rewardRarity),
                    color = rarityColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }

        // Timestamp
        Text(
            text = timeFormatted,
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


// ======================= COMPONENT: 10 CHEST OPENING DIALOG =======================
@Composable
fun TenRewardsDialog(
    rewards: List<Reward>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .shadow(16.dp, RoundedCornerShape(20.dp))
                .border(BorderStroke(2.dp, GoldGlow), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = OffBlack),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✨ Multi-Abertura: 10 Baús ✨",
                    color = GoldGlow,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Aqui estão as suas recompensas arcanas:",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Layout display of the 10 rewards in a sleek responsive layout
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(rewards) { reward ->
                            val color = getRarityColor(reward.rarity)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DeepPurpleCard)
                                    .border(BorderStroke(1.dp, color.copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(text = reward.icon, fontSize = 20.sp)
                                    Text(
                                        text = reward.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = getRarityName(reward.rarity),
                                    color = color,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldGlow),
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Coletar Tudo! 🎒",
                        color = OffBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
