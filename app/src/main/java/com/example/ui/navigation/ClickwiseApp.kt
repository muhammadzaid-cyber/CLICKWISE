package com.example.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.ClickwiseStrings
import com.example.domain.model.AppLanguage
import com.example.ui.components.AnimatedPulseBeacon
import com.example.ui.components.ClickwiseBrandEntrance
import com.example.ui.components.ClickwiseBrandWordmark
import com.example.ui.components.ClickwiseGridBackground
import com.example.ui.components.ClickwiseShieldEmblem
import com.example.ui.screens.AboutTrustScreen
import com.example.ui.screens.CyberAcademyScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MessageScannerScreen
import com.example.ui.screens.ScanHistoryScreen
import com.example.ui.screens.SecurityQuizScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UrlInspectorScreen
import com.example.ui.theme.LocalClickwiseColors
import com.example.ui.viewmodel.ClickwiseViewModel
import kotlinx.coroutines.launch

enum class ClickwiseRoute(
    val route: String,
    val icon: ImageVector,
    val showInBottomBar: Boolean
) {
    DASHBOARD("dashboard", Icons.Filled.Dashboard, true),
    SCANNER("scanner", Icons.Filled.Radar, true),
    URL_INSPECTOR("url_inspector", Icons.Filled.Link, true),
    HISTORY("history", Icons.Filled.History, false),
    ACADEMY("academy", Icons.Filled.School, true),
    QUIZ("quiz", Icons.Filled.Quiz, false),
    ABOUT("about", Icons.Filled.Info, false),
    SETTINGS("settings", Icons.Filled.Settings, false);

    fun title(lang: AppLanguage): String = when (this) {
        DASHBOARD -> ClickwiseStrings.navDashboard(lang)
        SCANNER -> ClickwiseStrings.navScanner(lang)
        URL_INSPECTOR -> ClickwiseStrings.navUrlInspector(lang)
        HISTORY -> ClickwiseStrings.navHistory(lang)
        ACADEMY -> ClickwiseStrings.navAcademy(lang)
        QUIZ -> ClickwiseStrings.navQuiz(lang)
        ABOUT -> ClickwiseStrings.navAbout(lang)
        SETTINGS -> ClickwiseStrings.navSettings(lang)
    }
}

@Composable
fun ClickwiseApp(
    viewModel: ClickwiseViewModel = viewModel()
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val reduceMotion by viewModel.reduceMotion.collectAsStateWithLifecycle()
    val showSplash by viewModel.showEntranceSplash.collectAsStateWithLifecycle()
    val scannerState by viewModel.scannerState.collectAsStateWithLifecycle()
    val urlState by viewModel.urlInspectorState.collectAsStateWithLifecycle()
    val sessionHistory by viewModel.sessionHistory.collectAsStateWithLifecycle()
    val completedLessonIds by viewModel.completedLessonIds.collectAsStateWithLifecycle()
    val quizState by viewModel.quizState.collectAsStateWithLifecycle()

    var highlightedLessonId by remember { mutableStateOf<String?>(null) }
    var sidebarCollapsed by remember { mutableStateOf(false) }

    val layoutDirection = if (language == AppLanguage.UR) LayoutDirection.Rtl else LayoutDirection.Ltr
    val colors = LocalClickwiseColors.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRouteStr = navBackStackEntry?.destination?.route ?: ClickwiseRoute.DASHBOARD.route
    val currentRoute = ClickwiseRoute.entries.firstOrNull { it.route == currentRouteStr } ?: ClickwiseRoute.DASHBOARD

    val navigateTo: (ClickwiseRoute) -> Unit = { dest ->
        navController.navigate(dest.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(modifier = Modifier.fillMaxSize()) {
            ClickwiseGridBackground(reduceMotion = reduceMotion)

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isDesktop = maxWidth >= 960.dp
                val isTablet = maxWidth in 600.dp..959.dp
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                val mainScaffold: @Composable () -> Unit = {
                    Scaffold(
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets.safeDrawing,
                        topBar = {
                            ClickwiseTopBar(
                                currentRoute = currentRoute,
                                language = language,
                                reduceMotion = reduceMotion,
                                showMenuButton = !isDesktop,
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onToggleLanguage = { viewModel.toggleLanguage() },
                                onOpenAbout = { navigateTo(ClickwiseRoute.ABOUT) }
                            )
                        },
                        bottomBar = {
                            if (!isDesktop && !isTablet) {
                                ClickwiseBottomBar(
                                    currentRoute = currentRoute,
                                    language = language,
                                    onSelect = navigateTo
                                )
                            }
                        }
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (isTablet) {
                                ClickwiseTabletRail(
                                    currentRoute = currentRoute,
                                    language = language,
                                    onSelect = navigateTo
                                )
                            }

                            NavHost(
                                navController = navController,
                                startDestination = ClickwiseRoute.DASHBOARD.route,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                enterTransition = {
                                    if (reduceMotion) EnterTransition.None
                                    else fadeIn(tween(200)) + scaleIn(initialScale = 0.985f, animationSpec = tween(220))
                                },
                                exitTransition = {
                                    if (reduceMotion) ExitTransition.None
                                    else fadeOut(tween(150))
                                }
                            ) {
                                composable(ClickwiseRoute.DASHBOARD.route) {
                                    DashboardScreen(
                                        language = language,
                                        reduceMotion = reduceMotion,
                                        completedLessonsCount = completedLessonIds.size,
                                        sessionScansCount = sessionHistory.size,
                                        onStartAnalysis = { navigateTo(ClickwiseRoute.SCANNER) },
                                        onExploreAcademy = { navigateTo(ClickwiseRoute.ACADEMY) },
                                        onOpenUrlInspector = { navigateTo(ClickwiseRoute.URL_INSPECTOR) },
                                        onOpenQuiz = { navigateTo(ClickwiseRoute.QUIZ) },
                                        onLoadSampleInScanner = { scenario ->
                                            viewModel.selectSampleScenario(scenario, language)
                                            navigateTo(ClickwiseRoute.SCANNER)
                                        },
                                        onOpenHighlightedLesson = { lessonId ->
                                            highlightedLessonId = lessonId
                                            navigateTo(ClickwiseRoute.ACADEMY)
                                        }
                                    )
                                }
                                composable(ClickwiseRoute.SCANNER.route) {
                                    MessageScannerScreen(
                                        state = scannerState,
                                        language = language,
                                        reduceMotion = reduceMotion,
                                        onMessageChanged = viewModel::onMessageChanged,
                                        onOptionalUrlChanged = viewModel::onOptionalUrlChanged,
                                        onClearInputs = viewModel::clearScannerInputs,
                                        onSelectSample = { scenario -> viewModel.selectSampleScenario(scenario, language) },
                                        onAnalyze = viewModel::analyzeMessage,
                                        onResetToNewScan = viewModel::resetToNewScan,
                                        onToggleLanguage = viewModel::toggleLanguage,
                                        onOpenAcademyLesson = { lessonId ->
                                            highlightedLessonId = lessonId
                                            navigateTo(ClickwiseRoute.ACADEMY)
                                        }
                                    )
                                }
                                composable(ClickwiseRoute.URL_INSPECTOR.route) {
                                    UrlInspectorScreen(
                                        state = urlState,
                                        language = language,
                                        reduceMotion = reduceMotion,
                                        onUrlChanged = viewModel::onInspectorUrlChanged,
                                        onClear = viewModel::clearUrlInspector,
                                        onInspect = viewModel::inspectUrl
                                    )
                                }
                                composable(ClickwiseRoute.HISTORY.route) {
                                    ScanHistoryScreen(
                                        records = sessionHistory,
                                        language = language,
                                        onOpenRecord = { record ->
                                            viewModel.openHistoryRecord(record)
                                            navigateTo(ClickwiseRoute.SCANNER)
                                        },
                                        onClearHistory = viewModel::clearSessionHistory,
                                        onGoToScanner = { navigateTo(ClickwiseRoute.SCANNER) }
                                    )
                                }
                                composable(ClickwiseRoute.ACADEMY.route) {
                                    CyberAcademyScreen(
                                        language = language,
                                        completedLessonIds = completedLessonIds,
                                        initialSelectedLessonId = highlightedLessonId,
                                        onClearInitialLesson = { highlightedLessonId = null },
                                        onMarkLessonCompleted = viewModel::markLessonCompleted
                                    )
                                }
                                composable(ClickwiseRoute.QUIZ.route) {
                                    SecurityQuizScreen(
                                        state = quizState,
                                        language = language,
                                        onSelectOption = viewModel::selectQuizOption,
                                        onSubmitAnswer = viewModel::submitQuizAnswer,
                                        onNextQuestion = viewModel::nextQuizQuestion,
                                        onRestart = viewModel::restartQuiz
                                    )
                                }
                                composable(ClickwiseRoute.ABOUT.route) {
                                    AboutTrustScreen(language = language)
                                }
                                composable(ClickwiseRoute.SETTINGS.route) {
                                    SettingsScreen(
                                        language = language,
                                        reduceMotion = reduceMotion,
                                        sessionHistoryCount = sessionHistory.size,
                                        onSetLanguage = viewModel::setLanguage,
                                        onSetReduceMotion = viewModel::setReduceMotion,
                                        onClearHistory = viewModel::clearSessionHistory
                                    )
                                }
                            }
                        }
                    }
                }

                if (isDesktop) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        DesktopSidebar(
                            currentRoute = currentRoute,
                            language = language,
                            collapsed = sidebarCollapsed,
                            reduceMotion = reduceMotion,
                            onToggleCollapse = { sidebarCollapsed = !sidebarCollapsed },
                            onSelect = navigateTo
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            mainScaffold()
                        }
                    }
                } else {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerContainerColor = colors.bgSecondary,
                                drawerContentColor = colors.textPrimary,
                                modifier = Modifier.width(290.dp)
                            ) {
                                DrawerNavigationContent(
                                    currentRoute = currentRoute,
                                    language = language,
                                    collapsed = false,
                                    showHeaderBrand = true,
                                    onSelect = { dest ->
                                        navigateTo(dest)
                                        scope.launch { drawerState.close() }
                                    }
                                )
                            }
                        }
                    ) {
                        mainScaffold()
                    }
                }
            }

            ClickwiseBrandEntrance(
                visible = showSplash,
                reduceMotion = reduceMotion,
                onDismiss = viewModel::dismissEntranceSplash
            )
        }
    }
}

@Composable
private fun ClickwiseTopBar(
    currentRoute: ClickwiseRoute,
    language: AppLanguage,
    reduceMotion: Boolean,
    showMenuButton: Boolean,
    onOpenDrawer: () -> Unit,
    onToggleLanguage: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val colors = LocalClickwiseColors.current
    Surface(
        color = colors.bgSecondary.copy(alpha = 0.90f),
        border = BorderStroke(1.dp, colors.borderMuted),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (showMenuButton) {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("topbar_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Open navigation menu",
                            tint = colors.textPrimary
                        )
                    }
                    ClickwiseShieldEmblem(size = 24.dp)
                    Spacer(Modifier.width(10.dp))
                }
                Column {
                    Text(
                        text = currentRoute.title(language),
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AnimatedPulseBeacon(
                            color = colors.limeSignal,
                            reduceMotion = reduceMotion,
                            size = 6.dp
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = ClickwiseStrings.statusEngineBadge(language),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = colors.surfaceDeep,
                    border = BorderStroke(1.dp, colors.borderCyan),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable(onClick = onToggleLanguage)
                        .testTag("topbar_language_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Translate,
                            contentDescription = "Switch language",
                            tint = colors.cyanPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (language == AppLanguage.EN) "اردو" else "EN",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.cyanPrimary
                        )
                    }
                }

                IconButton(
                    onClick = onOpenAbout,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("topbar_about_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "About CLICKWISE",
                        tint = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ClickwiseBottomBar(
    currentRoute: ClickwiseRoute,
    language: AppLanguage,
    onSelect: (ClickwiseRoute) -> Unit
) {
    val colors = LocalClickwiseColors.current
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(color = colors.borderCyan.copy(alpha = 0.28f), thickness = 1.dp)
        NavigationBar(
            containerColor = colors.bgSecondary.copy(alpha = 0.96f),
            contentColor = colors.textPrimary,
            windowInsets = WindowInsets.navigationBars
        ) {
            ClickwiseRoute.entries.filter { it.showInBottomBar }.forEach { dest ->
                val selected = currentRoute == dest
                NavigationBarItem(
                    selected = selected,
                    onClick = { onSelect(dest) },
                    modifier = Modifier.testTag("bottom_nav_${dest.route}"),
                    icon = {
                        Icon(
                            imageVector = dest.icon,
                            contentDescription = dest.title(language)
                        )
                    },
                    label = {
                        Text(
                            text = dest.title(language),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.cyanPrimary,
                        selectedTextColor = colors.cyanPrimary,
                        indicatorColor = colors.cyanPrimary.copy(alpha = 0.16f),
                        unselectedIconColor = colors.textSecondary,
                        unselectedTextColor = colors.textSecondary
                    )
                )
            }
        }
    }
}

@Composable
private fun ClickwiseTabletRail(
    currentRoute: ClickwiseRoute,
    language: AppLanguage,
    onSelect: (ClickwiseRoute) -> Unit
) {
    val colors = LocalClickwiseColors.current
    Row(modifier = Modifier.fillMaxHeight()) {
        NavigationRail(
            containerColor = colors.bgSecondary.copy(alpha = 0.94f),
            contentColor = colors.textPrimary
        ) {
            Spacer(Modifier.height(10.dp))
            ClickwiseRoute.entries.filter { it.showInBottomBar }.forEach { dest ->
                val selected = currentRoute == dest
                NavigationRailItem(
                    selected = selected,
                    onClick = { onSelect(dest) },
                    modifier = Modifier.testTag("bottom_nav_${dest.route}"),
                    icon = {
                        Icon(
                            imageVector = dest.icon,
                            contentDescription = dest.title(language)
                        )
                    },
                    label = {
                        Text(
                            text = dest.title(language),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = colors.cyanPrimary,
                        selectedTextColor = colors.cyanPrimary,
                        indicatorColor = colors.cyanPrimary.copy(alpha = 0.16f),
                        unselectedIconColor = colors.textSecondary,
                        unselectedTextColor = colors.textSecondary
                    )
                )
            }
            Spacer(Modifier.weight(1f))
            HorizontalDivider(
                color = colors.borderMuted,
                modifier = Modifier
                    .width(44.dp)
                    .padding(vertical = 8.dp)
            )
            listOf(ClickwiseRoute.HISTORY, ClickwiseRoute.QUIZ, ClickwiseRoute.SETTINGS).forEach { dest ->
                val selected = currentRoute == dest
                NavigationRailItem(
                    selected = selected,
                    onClick = { onSelect(dest) },
                    modifier = Modifier.testTag("rail_nav_${dest.route}"),
                    icon = {
                        Icon(
                            imageVector = dest.icon,
                            contentDescription = dest.title(language)
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = colors.cyanPrimary,
                        indicatorColor = colors.cyanPrimary.copy(alpha = 0.16f),
                        unselectedIconColor = colors.textSecondary
                    )
                )
            }
            Spacer(Modifier.height(12.dp))
        }
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(colors.borderMuted)
        )
    }
}

@Composable
private fun DesktopSidebar(
    currentRoute: ClickwiseRoute,
    language: AppLanguage,
    collapsed: Boolean,
    reduceMotion: Boolean,
    onToggleCollapse: () -> Unit,
    onSelect: (ClickwiseRoute) -> Unit
) {
    val colors = LocalClickwiseColors.current
    val sidebarWidth by animateDpAsState(
        targetValue = if (collapsed) 78.dp else 258.dp,
        animationSpec = if (reduceMotion) tween(0) else spring(stiffness = 500f),
        label = "sidebarWidth"
    )

    Surface(
        modifier = Modifier
            .width(sidebarWidth)
            .fillMaxHeight(),
        color = colors.bgSecondary.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, colors.borderMuted)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!collapsed) {
                    ClickwiseBrandWordmark(emblemSize = 28.dp)
                } else {
                    ClickwiseShieldEmblem(size = 28.dp)
                }
                IconButton(
                    onClick = onToggleCollapse,
                    modifier = Modifier.testTag("sidebar_collapse_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                        contentDescription = "Toggle sidebar",
                        tint = colors.textSecondary
                    )
                }
            }
            HorizontalDivider(color = colors.borderMuted)
            DrawerNavigationContent(
                currentRoute = currentRoute,
                language = language,
                collapsed = collapsed,
                showHeaderBrand = false,
                onSelect = onSelect
            )
        }
    }
}

@Composable
private fun DrawerNavigationContent(
    currentRoute: ClickwiseRoute,
    language: AppLanguage,
    collapsed: Boolean,
    showHeaderBrand: Boolean,
    onSelect: (ClickwiseRoute) -> Unit
) {
    val colors = LocalClickwiseColors.current
    val coreRoutes = listOf(
        ClickwiseRoute.DASHBOARD,
        ClickwiseRoute.SCANNER,
        ClickwiseRoute.URL_INSPECTOR,
        ClickwiseRoute.HISTORY
    )
    val learnRoutes = listOf(ClickwiseRoute.ACADEMY, ClickwiseRoute.QUIZ)
    val systemRoutes = listOf(ClickwiseRoute.ABOUT, ClickwiseRoute.SETTINGS)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (!collapsed && showHeaderBrand) {
                Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
                    ClickwiseBrandWordmark(emblemSize = 30.dp, showTagline = true)
                }
                HorizontalDivider(color = colors.borderMuted, modifier = Modifier.padding(bottom = 8.dp))
            }

            NavGroupSection(
                label = if (language == AppLanguage.UR) "بنیادی ٹولز" else "OPERATIONS",
                routes = coreRoutes,
                currentRoute = currentRoute,
                language = language,
                collapsed = collapsed,
                onSelect = onSelect
            )
            Spacer(Modifier.height(8.dp))
            NavGroupSection(
                label = if (language == AppLanguage.UR) "تعلیم اور تربیت" else "AWARENESS",
                routes = learnRoutes,
                currentRoute = currentRoute,
                language = language,
                collapsed = collapsed,
                onSelect = onSelect
            )
            Spacer(Modifier.height(8.dp))
            NavGroupSection(
                label = if (language == AppLanguage.UR) "نظام اور اعتماد" else "SYSTEM",
                routes = systemRoutes,
                currentRoute = currentRoute,
                language = language,
                collapsed = collapsed,
                onSelect = onSelect
            )
        }

        if (!collapsed) {
            Column(modifier = Modifier.padding(top = 16.dp, start = 8.dp, end = 8.dp)) {
                HorizontalDivider(color = colors.borderMuted)
                Spacer(Modifier.height(10.dp))
                Text(
                    text = ClickwiseStrings.tagline(language),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.cyanPrimary
                )
            }
        }
    }
}

@Composable
private fun NavGroupSection(
    label: String,
    routes: List<ClickwiseRoute>,
    currentRoute: ClickwiseRoute,
    language: AppLanguage,
    collapsed: Boolean,
    onSelect: (ClickwiseRoute) -> Unit
) {
    val colors = LocalClickwiseColors.current
    if (!collapsed) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary.copy(alpha = 0.72f),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
    routes.forEach { dest ->
        val selected = currentRoute == dest
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .clickable { onSelect(dest) }
                .testTag("drawer_nav_${dest.route}"),
            shape = RoundedCornerShape(10.dp),
            color = if (selected) colors.cyanPrimary.copy(alpha = 0.14f) else Color.Transparent,
            border = if (selected) BorderStroke(1.dp, colors.borderCyan) else null
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (selected) Brush.verticalGradient(listOf(colors.cyanPrimary, colors.indigoAccent))
                            else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                )
                Spacer(Modifier.width(9.dp))
                Icon(
                    imageVector = dest.icon,
                    contentDescription = dest.title(language),
                    tint = if (selected) colors.cyanPrimary else colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
                if (!collapsed) {
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = dest.title(language),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (selected) colors.cyanPrimary else colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
