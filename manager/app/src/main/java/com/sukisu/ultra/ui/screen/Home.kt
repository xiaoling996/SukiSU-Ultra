package com.sukisu.ultra.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.system.Os
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Token
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.content.pm.PackageInfoCompat
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.InstallScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.sukisu.ultra.KernelVersion
import com.sukisu.ultra.Natives
import com.sukisu.ultra.Natives.isKPMEnabled
import com.sukisu.ultra.R
import com.sukisu.ultra.getKernelVersion
import com.sukisu.ultra.ksuApp
import com.sukisu.ultra.ui.component.KsuIsValid
import com.sukisu.ultra.ui.component.rememberConfirmDialog
import com.sukisu.ultra.ui.theme.CardConfig
import com.sukisu.ultra.ui.theme.CardConfig.cardElevation
import com.sukisu.ultra.ui.theme.getCardColors
import com.sukisu.ultra.ui.util.checkNewVersion
import com.sukisu.ultra.ui.util.getKpmModuleCount
import com.sukisu.ultra.ui.util.getKpmVersion
import com.sukisu.ultra.ui.util.getModuleCount
import com.sukisu.ultra.ui.util.getSELinuxStatus
import com.sukisu.ultra.ui.util.getSuSFS
import com.sukisu.ultra.ui.util.getSuSFSFeatures
import com.sukisu.ultra.ui.util.getSuSFSVariant
import com.sukisu.ultra.ui.util.getSuSFSVersion
import com.sukisu.ultra.ui.util.getSuperuserCount
import com.sukisu.ultra.ui.util.module.LatestVersionInfo
import com.sukisu.ultra.ui.util.reboot
import com.sukisu.ultra.ui.util.rootAvailable
import com.sukisu.ultra.ui.util.susfsSUS_SU_Mode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.withContext
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Destination<RootGraph>(start = true)
@Composable
fun HomeScreen(navigator: DestinationsNavigator) {
    val context = LocalContext.current
    var isSimpleMode by rememberSaveable { mutableStateOf(false) }
    var isHideVersion by rememberSaveable { mutableStateOf(false) }
    var isHideOtherInfo by rememberSaveable { mutableStateOf(false) }
    var isHideSusfsStatus by rememberSaveable { mutableStateOf(false) }
    var isHideLinkCard by rememberSaveable { mutableStateOf(false) }
    var showKpmInfo by rememberSaveable { mutableStateOf(true) }

    // 从 SharedPreferences 加载简洁模式状态
    LaunchedEffect(Unit) {
        isSimpleMode = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getBoolean("is_simple_mode", false)

        isHideVersion = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getBoolean("is_hide_version", false)

        isHideOtherInfo = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getBoolean("is_hide_other_info", false)

        isHideSusfsStatus = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getBoolean("is_hide_susfs_status", false)

        isHideLinkCard = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getBoolean("is_hide_link_card", false)

        showKpmInfo = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getBoolean("show_kpm_info", true)
    }

    val kernelVersion = getKernelVersion()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val scrollState = rememberScrollState()
    val debounceTime = 100L
    var lastScrollTime by remember { mutableLongStateOf(0L) }

    Scaffold(
        topBar = {
            TopBar(
                kernelVersion,
                onInstallClick = { navigator.navigate(InstallScreenDestination) },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal
        )
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .disableOverscroll()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(scrollState)
                .padding(top = 12.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val isManager = Natives.becomeManager(ksuApp.packageName)
            val ksuVersion = if (isManager) Natives.version else null
            val lkmMode = ksuVersion?.let {
                if (it >= Natives.MINIMAL_SUPPORTED_KERNEL_LKM && kernelVersion.isGKI()) Natives.isLkmMode else null
            }

            StatusCard(kernelVersion, ksuVersion, lkmMode) {
                navigator.navigate(InstallScreenDestination)
            }

            if (isManager && Natives.requireNewKernel()) {
                WarningCard(
                    stringResource(id = R.string.require_kernel_version).format(
                        ksuVersion, Natives.MINIMAL_SUPPORTED_KERNEL
                    )
                )
            }

            if (ksuVersion != null && !rootAvailable()) {
                WarningCard(
                    stringResource(id = R.string.grant_root_failed)
                )
            }

            val checkUpdate =
                LocalContext.current.getSharedPreferences("settings", Context.MODE_PRIVATE)
                    .getBoolean("check_update", true)
            if (checkUpdate) {
                UpdateCard()
            }

            val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
            var clickCount by rememberSaveable { mutableIntStateOf(prefs.getInt("click_count", 0)) }

            if (!isSimpleMode && clickCount < 3) {
                AnimatedVisibility(
                    visible = clickCount < 3,
                    enter = fadeIn() + expandVertically(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    ElevatedCard(
                        colors = getCardColors(MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .shadow(
                                elevation = cardElevation,
                                shape = MaterialTheme.shapes.medium,
                                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    clickCount++
                                    prefs.edit { putInt("click_count", clickCount) }
                                }
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.using_mksu_manager),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            InfoCard()

            if (!isSimpleMode) {
                if (!isHideLinkCard) {
                    ContributionCard()
                    DonateCard()
                    LearnMoreCard()
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.isScrollInProgress }
            .debounce(debounceTime)
            .collect { isScrolling ->
                if (isScrolling) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastScrollTime > debounceTime) {
                        lastScrollTime = currentTime
                    }
                }
            }
    }
}

@Composable
fun UpdateCard() {
    val context = LocalContext.current
    val latestVersionInfo = LatestVersionInfo()
    val newVersion by produceState(initialValue = latestVersionInfo) {
        value = withContext(Dispatchers.IO) {
            checkNewVersion()
        }
    }

    val currentVersionCode = getManagerVersion(context).second
    val newVersionCode = newVersion.versionCode
    val newVersionUrl = newVersion.downloadUrl
    val changelog = newVersion.changelog

    val uriHandler = LocalUriHandler.current
    val title = stringResource(id = R.string.module_changelog)
    val updateText = stringResource(id = R.string.module_update)

    AnimatedVisibility(
        visible = newVersionCode > currentVersionCode,
        enter = fadeIn() + expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = shrinkVertically() + fadeOut()
    ) {
        val updateDialog = rememberConfirmDialog(onConfirm = { uriHandler.openUri(newVersionUrl) })
        WarningCard(
            message = stringResource(id = R.string.new_version_available).format(newVersionCode),
            color = MaterialTheme.colorScheme.surfaceVariant,
            onClick = {
                if (changelog.isEmpty()) {
                    uriHandler.openUri(newVersionUrl)
                } else {
                    updateDialog.showConfirm(
                        title = title,
                        content = changelog,
                        markdown = true,
                        confirm = updateText
                    )
                }
            }
        )
    }
}

@Composable
fun RebootDropdownItem(@StringRes id: Int, reason: String = "") {
    DropdownMenuItem(
        text = { Text(stringResource(id)) },
        onClick = { reboot(reason) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    kernelVersion: KernelVersion,
    onInstallClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val cardColor = MaterialTheme.colorScheme.surfaceContainerLow
    val cardAlpha = CardConfig.cardAlpha

    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = cardColor.copy(alpha = cardAlpha),
            scrolledContainerColor = cardColor.copy(alpha = cardAlpha)
        ),
        actions = {
            var showDropdown by remember { mutableStateOf(false) }
            KsuIsValid {
                IconButton(onClick = {
                    showDropdown = true
                }) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = stringResource(id = R.string.reboot)
                    )

                    DropdownMenu(expanded = showDropdown, onDismissRequest = {
                        showDropdown = false
                    }) {
                        RebootDropdownItem(id = R.string.reboot)

                        val pm = LocalContext.current.getSystemService(Context.POWER_SERVICE) as PowerManager?
                        @Suppress("DEPRECATION")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && pm?.isRebootingUserspaceSupported == true) {
                            RebootDropdownItem(id = R.string.reboot_userspace, reason = "userspace")
                        }
                        RebootDropdownItem(id = R.string.reboot_recovery, reason = "recovery")
                        RebootDropdownItem(id = R.string.reboot_bootloader, reason = "bootloader")
                        RebootDropdownItem(id = R.string.reboot_download, reason = "download")
                        RebootDropdownItem(id = R.string.reboot_edl, reason = "edl")
                    }
                }
            }
        },
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun StatusCard(
    kernelVersion: KernelVersion,
    ksuVersion: Int?,
    lkmMode: Boolean?,
    onClickInstall: () -> Unit = {}
) {
    ElevatedCard(
        colors = getCardColors(MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .shadow(
                elevation = cardElevation,
                shape = MaterialTheme.shapes.large,
                spotColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (rootAvailable() || kernelVersion.isGKI()) {
                        onClickInstall()
                    }
                }
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                ksuVersion != null -> {

                    val workingModeText = when {
                        lkmMode == true -> "LKM"
                        lkmMode == null && kernelVersion.isGKI1() -> "GKI1.0"
                        lkmMode == false || kernelVersion.isGKI() -> "GKI2.0"
                        else -> "N-GKI"
                    }

                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = stringResource(R.string.home_working),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Column(Modifier.padding(start = 20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.home_working),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(Modifier.width(8.dp))

                            // 工作模式标签
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            ) {
                                Text(
                                    text = workingModeText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(Modifier.width(6.dp))

                            // 机器架构标签或者安全模式标签
                            val labelText = if (Natives.isSafeMode) {
                                stringResource(id = R.string.safe_mode)
                            } else {
                                Os.uname().machine
                            }

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            ) {
                                Text(
                                    text = labelText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        val isHideVersion = LocalContext.current.getSharedPreferences("settings", Context.MODE_PRIVATE)
                            .getBoolean("is_hide_version", false)

                        val isHideOtherInfo = LocalContext.current.getSharedPreferences("settings", Context.MODE_PRIVATE)
                            .getBoolean("is_hide_other_info", false)

                        val showKpmInfo = LocalContext.current.getSharedPreferences("settings", Context.MODE_PRIVATE)
                            .getBoolean("show_kpm_info", true)

                        if (!isHideVersion) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.home_working_version, ksuVersion),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!isHideOtherInfo) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.home_superuser_count, getSuperuserCount()),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.home_module_count, getModuleCount()),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val kpmVersion = getKpmVersion()
                            if (kpmVersion.isNotEmpty() && !kpmVersion.startsWith("Error") && showKpmInfo && Natives.version >= Natives.MINIMAL_SUPPORTED_KPM) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.home_kpm_module, getKpmModuleCount()),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                kernelVersion.isGKI() -> {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = stringResource(R.string.home_not_installed),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )

                    Column(Modifier.padding(start = 20.dp)) {
                        Text(
                            text = stringResource(R.string.home_not_installed),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.home_click_to_install),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    Icon(
                        Icons.Outlined.Block,
                        contentDescription = stringResource(R.string.home_unsupported),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )

                    Column(Modifier.padding(start = 20.dp)) {
                        Text(
                            text = stringResource(R.string.home_unsupported),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.home_unsupported_reason),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WarningCard(
    message: String,
    color: Color = MaterialTheme.colorScheme.error,
    onClick: (() -> Unit)? = null
) {
    ElevatedCard(
        colors = getCardColors(color),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .shadow(
                elevation = cardElevation,
                shape = MaterialTheme.shapes.large,
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(onClick?.let { Modifier.clickable { it() } } ?: Modifier)
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun ContributionCard() {
    val uriHandler = LocalUriHandler.current
    val links = listOf("https://github.com/ShirkNeko", "https://github.com/udochina")

    ElevatedCard(
        colors = getCardColors(MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(MaterialTheme.shapes.large)
            .shadow(
                elevation = cardElevation,
                shape = MaterialTheme.shapes.large,
                spotColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val randomIndex = Random.nextInt(links.size)
                    uriHandler.openUri(links[randomIndex])
                }
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.home_ContributionCard_kernelsu),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_click_to_ContributionCard_kernelsu),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun LearnMoreCard() {
    val uriHandler = LocalUriHandler.current
    val url = stringResource(R.string.home_learn_kernelsu_url)

    ElevatedCard(
        colors = getCardColors(MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .shadow(
                elevation = cardElevation,
                shape = MaterialTheme.shapes.large,
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    uriHandler.openUri(url)
                }
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.home_learn_kernelsu),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_click_to_learn_kernelsu),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun DonateCard() {
    val uriHandler = LocalUriHandler.current

    ElevatedCard(
        colors = getCardColors(MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .shadow(
                elevation = cardElevation,
                shape = MaterialTheme.shapes.large,
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    uriHandler.openUri("https://patreon.com/weishu")
                }
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.home_support_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_support_content),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun InfoCard() {
    val lkmMode = Natives.isLkmMode
    val context = LocalContext.current
    val isSimpleMode = LocalContext.current.getSharedPreferences("settings", Context.MODE_PRIVATE)
        .getBoolean("is_simple_mode", false)
    val showKpmInfo = LocalContext.current.getSharedPreferences("settings", Context.MODE_PRIVATE)
        .getBoolean("show_kpm_info", true)

    ElevatedCard(
        colors = getCardColors(MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .shadow(
                elevation = cardElevation,
                shape = MaterialTheme.shapes.large,
                spotColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 16.dp),
        ) withContext@{
            val contents = StringBuilder()
            val uname = Os.uname()

            @Composable
            fun InfoCardItem(
                label: String,
                content: String,
                icon: ImageVector = Icons.Default.Info
            ) {
                contents.appendLine(label).appendLine(content).appendLine()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ){
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            softWrap = true
                        )
                    }
                }
            }

            InfoCardItem(
                stringResource(R.string.home_kernel),
                uname.release,
                icon = Icons.Default.Memory,
            )

            if (!isSimpleMode) {
                val androidVersion = Build.VERSION.RELEASE
                InfoCardItem(
                    stringResource(R.string.home_android_version),
                    androidVersion,
                    icon = Icons.Default.Android,
                )
            }

            val deviceModel = getDeviceModel()
            InfoCardItem(
                stringResource(R.string.home_device_model),
                deviceModel,
                icon = Icons.Default.PhoneAndroid,
            )

            val managerVersion = getManagerVersion(context)
            InfoCardItem(
                stringResource(R.string.home_manager_version),
                "${managerVersion.first} (${managerVersion.second})",
                icon = Icons.Default.SettingsSuggest,
            )

            InfoCardItem(
                stringResource(R.string.home_selinux_status),
                getSELinuxStatus(),
                icon = Icons.Default.Security,
            )

            if (!isSimpleMode) {
                if (lkmMode != true) {
                    val kpmVersion = getKpmVersion()
                    val isKpmConfigured = checkKPMEnabled()

                    // 根据showKpmInfo决定是否显示KPM信息
                    if (showKpmInfo && Natives.version >= Natives.MINIMAL_SUPPORTED_KPM) {
                        val displayVersion = if (kpmVersion.isEmpty() || kpmVersion.startsWith("Error")) {
                            val statusText = if (isKpmConfigured) {
                                stringResource(R.string.kernel_patched)
                            } else {
                                stringResource(R.string.kernel_not_enabled)
                            }
                            "${stringResource(R.string.not_supported)} ($statusText)"
                        } else {
                            "${stringResource(R.string.supported)} ($kpmVersion)"
                        }

                        InfoCardItem(
                            stringResource(R.string.home_kpm_version),
                            displayVersion,
                            icon = Icons.Default.Archive
                        )
                    }
                }
            }

            val isHideSusfsStatus = LocalContext.current.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getBoolean("is_hide_susfs_status", false)

            if ((!isSimpleMode) && (!isHideSusfsStatus)) {
                val suSFS = getSuSFS()
                if (suSFS == "Supported") {
                    val suSFSVersion = getSuSFSVersion()
                    if (suSFSVersion.isNotEmpty()) {
                        val isSUS_SU = getSuSFSFeatures() == "CONFIG_KSU_SUSFS_SUS_SU"
                        val infoText = buildString {
                            append(suSFSVersion)
                            append(if (isSUS_SU) " (${getSuSFSVariant()})" else " (${stringResource(R.string.manual_hook)})")
                            if (isSUS_SU) {
                                val susSUMode = try { susfsSUS_SU_Mode().toString() } catch (_: Exception) { "" }
                                if (susSUMode.isNotEmpty()) {
                                    append(" ${stringResource(R.string.sus_su_mode)} $susSUMode")
                                }
                            }
                        }

                        InfoCardItem(
                            stringResource(R.string.home_susfs_version),
                            infoText,
                            icon = Icons.Default.Storage
                        )
                    }
                }
            }
        }
    }
}

fun getManagerVersion(context: Context): Pair<String, Long> {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)!!
    val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
    return Pair(packageInfo.versionName!!, versionCode)
}

@Preview
@Composable
private fun StatusCardPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        StatusCard(KernelVersion(5, 10, 101), 1, null)
        StatusCard(KernelVersion(5, 10, 101), 20000, true)
        StatusCard(KernelVersion(5, 10, 101), null, true)
        StatusCard(KernelVersion(4, 10, 101), null, false)
    }
}

@Preview
@Composable
private fun WarningCardPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        WarningCard(message = "Warning message")
        WarningCard(
            message = "Warning message ",
            MaterialTheme.colorScheme.outlineVariant,
            onClick = {})
    }
}

private object DeviceModelCache {
    @Volatile
    private var cachedModel: String? = null

    fun getDeviceModel(): String {
        return cachedModel ?: synchronized(this) {
            cachedModel ?: try {
                val systemProperties = Class.forName("android.os.SystemProperties")
                val getMethod = systemProperties.getMethod("get", String::class.java, String::class.java)
                val marketNameKeys = listOf(
                    "ro.product.marketname",          // Xiaomi
                    "ro.vendor.oplus.market.name",    // Oppo, OnePlus, Realme
                    "ro.vivo.market.name",            // Vivo
                    "ro.config.marketing_name"        // Huawei
                )
                var result = Build.DEVICE
                for (key in marketNameKeys) {
                    val marketName = getMethod.invoke(null, key, "") as String
                    if (marketName.isNotEmpty()) {
                        result = marketName
                        break
                    }
                }
                result
            } catch (_: Exception) {
                Build.DEVICE
            }.also { cachedModel = it }
        }
    }
}
// 获取设备型号
@SuppressLint("PrivateApi")
private fun getDeviceModel(): String {
    return DeviceModelCache.getDeviceModel()
}

// 检查KPM是否存在
private fun checkKPMEnabled(): Boolean {
    return isKPMEnabled()
}

@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.disableOverscroll(): Modifier = composed {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this
    } else {
        this
    }
}